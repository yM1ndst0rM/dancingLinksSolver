package base;

import java.util.Collection;

public interface SolutionMatrix {
    void init(Object[][] initialState);
    Object get(int row, int column);
    void removeRow(int row);
    void removeColumn(int col);
    void undo() throws IllegalStateException;
    boolean canUndo();
    int getRowCount();
    int getColumnCount();
    int getRowId(int rowPosition);
    int getColumnId(int columnPosition);
    void clearRowAndAffectedColumns(int rowPosition);
    Collection<Integer> getRowsAffectedByColumn(int columnPosition);
}
