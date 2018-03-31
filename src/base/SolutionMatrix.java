package base;

public interface SolutionMatrix {
    void removeRow(int row);
    void removeColumn(int col);
    void undo() throws IllegalStateException;
    boolean canUndo();
    int getRowCount();
    int getColumnCount();
}
