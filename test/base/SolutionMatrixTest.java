package base;

import base.ArrayMatrix;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class SolutionMatrixTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ArrayMatrix m;
    private static final char valA = 'A';
    private static final char valB = 'B';
    private static final char valC = 'C';
    private static final char valD = 'D';

    @Before
    public void setUp() throws Exception {
        m = new ArrayMatrix(2, 2);
        m.set(0, 0, valA);
        m.set(0, 1, valB);
        m.set(1, 0, valC);
        m.set(1, 1, valD);
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
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        m.removeRow(-1);

        thrown.expect(ArrayIndexOutOfBoundsException.class);
        m.removeRow(2);

        m.removeRow(0);
        m.removeRow(0);

        thrown.expect(ArrayIndexOutOfBoundsException.class);
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
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        m.removeColumn(-1);

        thrown.expect(ArrayIndexOutOfBoundsException.class);
        m.removeColumn(2);

        m.removeColumn(0);
        m.removeColumn(0);

        thrown.expect(ArrayIndexOutOfBoundsException.class);
        m.removeColumn(0);
    }

    @Test
    public void invalidUndoThrows(){
        thrown.expect(IllegalStateException.class);
        m.undo();
    }

    @Test
    public void undoRow() {
        m.removeRow(0);
        assertEquals(valC, m.get(0, 0));
        assertEquals(valD, m.get(0, 1));

        m.undo();
        assertEquals(valA, m.get(0 , 0));
        assertEquals(valB, m.get(0 , 1));
    }

    @Test
    public void undoCol() {
        m.removeColumn(0);
        assertEquals(valB, m.get(0, 0));
        assertEquals(valD, m.get(1, 0));

        m.undo();
        assertEquals(valA, m.get(0 , 0));
        assertEquals(valC, m.get(1 , 0));
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
}