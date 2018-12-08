package base;

public interface SolutionMatrix {
    void init(Object[][] initialState);
    Object get(int row, int column);
    void removeRow(int row);
    void removeColumn(int col);
    void undo() throws IllegalStateException;
    boolean canUndo();
    int getRowCount();
    int getColumnCount();
}
