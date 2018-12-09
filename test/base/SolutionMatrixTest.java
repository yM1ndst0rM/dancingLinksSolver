package base;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SolutionMatrixTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SolutionMatrix m;
    private static final char valA = 'A';
    private static final char valB = 'B';
    private static final char valC = 'C';
    private static final char valD = 'D';

    private static final Character[][] initialState =
            {
                    {valA, valB},
                    {valC, valD}
            };

    private static final Integer[][] solutionMatrixForAdvancedDeletionTests =
            {
                    {1, null, null, null, null},
                    {null, 1, null, null, null},
                    {null, null, 1, null, null},
                    {1, null, 1, null, 1},
                    {null, null, null, null, 1}
            };

    @Parameterized.Parameters
    public static Collection<SolutionMatrix> data() {
        return Arrays.asList(
                new ArrayMatrix(),
                new LinkedMatrix()
        );
    }

    public SolutionMatrixTest(SolutionMatrix m) {
        this.m = m;
    }

    @Before
    public void setUp() throws Exception {
        m.init(initialState);
    }

    @Test
    public void removeRow() {
        m.removeRow(0);
        assertEquals(1, m.getRowCount());
        assertEquals(2, m.getColumnCount());
        assertEquals(valC, m.get(0, 0));
        assertEquals(valD, m.get(0, 1));
    }

    @Test
    public void removeIllegalRowThrows() {
        thrown.expect(IndexOutOfBoundsException.class);
        m.removeRow(-1);

        thrown.expect(IndexOutOfBoundsException.class);
        m.removeRow(2);


        m.removeRow(0);
        m.removeRow(0);

        thrown.expect(IndexOutOfBoundsException.class);
        m.removeRow(0);
    }

    @Test
    public void removeColumn() {
        m.removeColumn(0);
        assertEquals(2, m.getRowCount());
        assertEquals(1, m.getColumnCount());
        assertEquals(valB, m.get(0, 0));
        assertEquals(valD, m.get(1, 0));
    }

    @Test
    public void removeIllegalColumnThrows() {
        thrown.expect(IndexOutOfBoundsException.class);
        m.removeColumn(-1);

        thrown.expect(IndexOutOfBoundsException.class);
        m.removeColumn(2);

        m.removeColumn(0);
        m.removeColumn(0);

        thrown.expect(IndexOutOfBoundsException.class);
        m.removeColumn(0);
    }

    @Test
    public void invalidUndoThrows() {
        thrown.expect(IllegalStateException.class);
        m.undo();
    }

    @Test
    public void undoRow() {
        m.removeRow(0);
        assertEquals(valC, m.get(0, 0));
        assertEquals(valD, m.get(0, 1));

        m.undo();
        assertEquals(valA, m.get(0, 0));
        assertEquals(valB, m.get(0, 1));
    }

    @Test
    public void undoCol() {
        m.removeColumn(0);
        assertEquals(valB, m.get(0, 0));
        assertEquals(valD, m.get(1, 0));

        m.undo();
        assertEquals(valA, m.get(0, 0));
        assertEquals(valC, m.get(1, 0));
    }

    @Test
    public void canUndo() {
        assertFalse(m.canUndo());

        m.removeColumn(0);
        assertTrue(m.canUndo());

        m.removeRow(0);
        assertTrue(m.canUndo());

        m.undo();
        assertTrue(m.canUndo());

        m.undo();
        assertFalse(m.canUndo());
    }

    @Test
    public void getRowCount() {
        assertEquals(2, m.getRowCount());

        m.removeRow(1);
        assertEquals(1, m.getRowCount());
    }

    @Test
    public void getColumnCount() {
        assertEquals(2, m.getColumnCount());

        m.removeColumn(1);
        assertEquals(1, m.getColumnCount());
    }

    @Test
    public void getColumnId() {
        assertEquals(0, m.getColumnId(0));
        assertEquals(1, m.getColumnId(1));

        //nothing should change here
        m.removeRow(0);
        assertEquals(0, m.getColumnId(0));
        assertEquals(1, m.getColumnId(1));

        //column shift one to the left
        m.removeColumn(0);
        assertEquals(1, m.getColumnId(0));

        //column ids get correctly restored after undo
        m.undo();
        assertEquals(0, m.getColumnId(0));
        assertEquals(1, m.getColumnId(1));
    }

    @Test
    public void getColumnIdForInvalidPositionThrows() {
        m.removeColumn(0);
        thrown.expect(IndexOutOfBoundsException.class);
        m.getColumnId(1);
    }

    @Test
    public void getRowId() {
        assertEquals(0, m.getRowId(0));
        assertEquals(1, m.getRowId(1));

        //nothing should change here
        m.removeColumn(0);
        assertEquals(0, m.getRowId(0));
        assertEquals(1, m.getRowId(1));

        //row shift one up
        m.removeRow(0);
        assertEquals(1, m.getRowId(0));

        //row ids get correctly restored after undo
        m.undo();
        assertEquals(0, m.getRowId(0));
        assertEquals(1, m.getRowId(1));
    }

    @Test
    public void getRowIdForInvalidPositionThrows() {
        m.removeRow(0);
        thrown.expect(IndexOutOfBoundsException.class);
        m.getRowId(1);
    }

    @Test
    public void clearRowAndAffectedColumns1() {
        m.init(solutionMatrixForAdvancedDeletionTests);

        m.clearRowAndAffectedColumns(0);
        assertEquals(3, m.getRowCount());
        assertEquals(4, m.getColumnCount());

        assertEquals(1, m.getRowId(0));
        assertEquals(2, m.getRowId(1));
        assertEquals(4, m.getRowId(2));

        assertEquals(1, m.getColumnId(0));
        assertEquals(2, m.getColumnId(1));
        assertEquals(3, m.getColumnId(2));
        assertEquals(4, m.getColumnId(3));
    }

    @Test
    public void clearRowAndAffectedColumns2() {
        m.init(solutionMatrixForAdvancedDeletionTests);

        m.clearRowAndAffectedColumns(1);
        assertEquals(4, m.getRowCount());
        assertEquals(4, m.getColumnCount());

        assertEquals(0, m.getRowId(0));
        assertEquals(2, m.getRowId(1));
        assertEquals(3, m.getRowId(2));
        assertEquals(4, m.getRowId(3));

        assertEquals(0, m.getColumnId(0));
        assertEquals(2, m.getColumnId(1));
        assertEquals(3, m.getColumnId(2));
        assertEquals(4, m.getColumnId(3));
    }

    @Test
    public void clearRowAndAffectedColumns3() {
        m.init(solutionMatrixForAdvancedDeletionTests);

        m.clearRowAndAffectedColumns(3);
        assertEquals(1, m.getRowCount());
        assertEquals(2, m.getColumnCount());

        assertEquals(1, m.getRowId(0));

        assertEquals(1, m.getColumnId(0));
        assertEquals(3, m.getColumnId(1));
    }

    @Test
    public void clearRowAndAffectedColumnUndo() {
        m.init(solutionMatrixForAdvancedDeletionTests);

        m.clearRowAndAffectedColumns(3);
        m.undo();

        assertEquals(5, m.getRowCount());
        assertEquals(5, m.getColumnCount());

        assertEquals(0, m.getRowId(0));
        assertEquals(1, m.getRowId(1));
        assertEquals(2, m.getRowId(2));
        assertEquals(3, m.getRowId(3));
        assertEquals(4, m.getRowId(4));

        assertEquals(0, m.getColumnId(0));
        assertEquals(1, m.getColumnId(1));
        assertEquals(2, m.getColumnId(2));
        assertEquals(3, m.getColumnId(3));
        assertEquals(4, m.getColumnId(4));
    }

    @Test
    public void clearInvalidRowAndAffectedColumnsThrows() {
        thrown.expect(IndexOutOfBoundsException.class);
        m.clearRowAndAffectedColumns(-1);

        thrown.expect(IndexOutOfBoundsException.class);
        m.clearRowAndAffectedColumns(2);
    }

    @Test
    public void clearRowAndAffectedColumn_MoreColumnsThanRows() {
        Integer[][] solutionMatrix =
                {
                        {1, null, null, null, null, null, null, null},
                        {null, 1, null, null, null, null, 1, null}
                };

        m.init(solutionMatrix);

        m.clearRowAndAffectedColumns(0);

        assertEquals(1, m.getRowCount());
        assertEquals(7, m.getColumnCount());

        assertEquals(1, m.getRowId(0));

        assertEquals(1, m.getColumnId(0));
        assertEquals(2, m.getColumnId(1));
        assertEquals(3, m.getColumnId(2));
        assertEquals(4, m.getColumnId(3));
        assertEquals(5, m.getColumnId(4));
        assertEquals(6, m.getColumnId(5));
        assertEquals(7, m.getColumnId(6));
    }


    @Test
    public void clearRowAndAffectedColumn_MoreRowsThanColumns() {
        Integer[][] solutionMatrix =
                {
                        {1, null},
                        {null, null},
                        {null, null},
                        {null, null},
                        {1, 1},
                        {null, null},
                        {null, 1},
                        {1, null},
                        {null, 1}
                };

        m.init(solutionMatrix);

        m.clearRowAndAffectedColumns(0);

        assertEquals(6, m.getRowCount());
        assertEquals(1, m.getColumnCount());

        assertEquals(1, m.getRowId(0));
        assertEquals(2, m.getRowId(1));
        assertEquals(3, m.getRowId(2));
        assertEquals(5, m.getRowId(3));
        assertEquals(6, m.getRowId(4));
        assertEquals(8, m.getRowId(5));

        assertEquals(1, m.getColumnId(0));
    }
}