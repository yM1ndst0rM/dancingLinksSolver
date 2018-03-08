public interface SolutionMatrix {
    char get(int row, int col);
    void removeRow(int row);
    void removeColumn(int col);
    void undo();
    boolean canUndo();
    int getRowCount();
    int getColumnCount();
}
