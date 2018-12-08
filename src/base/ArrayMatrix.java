package base;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class ArrayMatrix implements SolutionMatrix {
    private final Deque<Object[][]> history = new LinkedList<>();
    private Object[][] currentState = new Character[0][];

    @Override
    public void init(final Object[][] initialState) {
        currentState = new Object[initialState.length + 1][]; //+1 to account for headers

        //insert column ids
        currentState[0] = new Object[initialState[0].length + 1];
        for (int i = 0; i < currentState[0].length; i++) {
            currentState[0][i] = i - 1;
        }

        //fill rows
        for (int i = 1; i < currentState.length; i++) {
            currentState[i] = new Object[initialState[i - 1].length + 1]; //+1 to account for headers
            currentState[i][0] = i - 1; //insert row id in first position

            if (initialState[i - 1].length >= 0) {
                System.arraycopy(initialState[i - 1], 0, currentState[i], 1, initialState[i - 1].length);
            }
        }

        history.clear();
    }

    @Override
    public Object get(int row, int col) {
        return currentState[row + 1][col + 1];
    }

    @Override
    public void removeRow(final int row) {
        final int actualRowIndex = row + 1; //because of the header
        if (actualRowIndex >= currentState.length || actualRowIndex < 1) {
            throw new ArrayIndexOutOfBoundsException(row);
        }

        history.push(currentState);

        Object[][] newMatrix = new Object[currentState.length - 1][];
        int offset;
        for (int i = 0; i < newMatrix.length; ++i) {
            if (i >= actualRowIndex) {
                offset = 1;
            } else {
                offset = 0;
            }

            newMatrix[i] = Arrays.copyOf(currentState[i + offset], currentState[i + offset].length);
        }

        currentState = newMatrix;
    }

    @Override
    public void removeColumn(final int col) {
        final int actualColIndex = col + 1; //because of the header
        if (actualColIndex >= currentState.length || actualColIndex < 1) {
            throw new ArrayIndexOutOfBoundsException(col);
        }

        history.push(currentState);

        Object[][] newMatrix = new Object[currentState.length][currentState[0].length - 1];
        int offset;
        for (int i = 0; i < newMatrix.length; ++i) {
            for (int j = 0; j < newMatrix[i].length; ++j) {
                if (j >= actualColIndex) {
                    offset = 1;
                } else {
                    offset = 0;
                }

                newMatrix[i][j] = currentState[i][j + offset];
            }
        }

        currentState = newMatrix;
    }

    @Override
    public void undo() throws IllegalStateException {
        if (!canUndo()) {
            throw new IllegalStateException("Cannot undo.");
        }

        currentState = history.pop();
    }

    @Override
    public boolean canUndo() {
        return !history.isEmpty();
    }

    @Override
    public int getRowCount() {
        return currentState.length - 1;
    }

    @Override
    public int getColumnCount() {
        return currentState.length > 0 ? currentState[0].length - 1 : 0;
    }

    @Override
    public int getRowId(int rowPosition) {
        return (int) currentState[rowPosition + 1][0];
    }

    @Override
    public int getColumnId(int columnPosition) {
        return (int) currentState[0][columnPosition + 1];
    }

    public void set(int row, int col, Object value) {
        this.currentState[row + 1][col + 1] = value;
    }
}
