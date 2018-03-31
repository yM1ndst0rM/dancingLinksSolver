package base;

import static base.Node.Direction.*;

public class LinkedMatrix implements SolutionMatrix {
    private final Node topLeftCorner = new Node();

    /**
     *
     * @param initialState {@code char[][]} row/column
     */
    public LinkedMatrix(final char[][] initialState) {
        Node current = topLeftCorner;
        for(int r = 0; r < initialState.length; ++r){
            Node newNode = new Node();
            newNode.setTag(String.format("Row %d", r));
            current.setInDirection(BOTTOM, newNode);
            current = newNode;
        }
        current.setInDirection(BOTTOM, topLeftCorner);

        for(int r = 0; r < initialState.length; ++r){
            Node newNode = new Node();
            newNode.setTag(String.format("Row %d", r));
            current.setInDirection(BOTTOM, newNode);
            current = newNode;
        }


    }


    @Override
    public void removeRow(int row) {

    }

    @Override
    public void removeColumn(int col) {

    }

    @Override
    public void undo() {

    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }
}
