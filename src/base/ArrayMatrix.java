package base;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class ArrayMatrix implements SolutionMatrix {
    private final Deque<char[][]> history;
    private char[][] currentState;

    public ArrayMatrix(int initialRowCount, int initialColCount) {
        history = new LinkedList<>();
        currentState = new char[initialRowCount][initialColCount];
    }

    public char get(int row, int col) {
        return currentState[row][col];
    }

    @Override
    public void removeRow(int row) {
        if (row >= currentState.length || row < 0) {
            throw new ArrayIndexOutOfBoundsException(row);
        }

        history.push(currentState);

        char[][] newMatrix = new char[getRowCount() - 1][];
        int offset;
        for (int i = 0; i < newMatrix.length; ++i) {
            if (i >= row) {
                offset = 1;
            } else {
                offset = 0;
            }

            newMatrix[i] = Arrays.copyOf(currentState[i + offset], currentState[i + offset].length);
        }

        currentState = newMatrix;
    }

    @Override
    public void removeColumn(int col) {
        if (col >= currentState.length || col < 0) {
            throw new ArrayIndexOutOfBoundsException(col);
        }

        history.push(currentState);

        char[][] newMatrix = new char[getRowCount()][getColumnCount() - 1];
        int offset;
        for (int i = 0; i < newMatrix.length; ++i) {
            for (int j = 0; j < newMatrix[i].length; ++j) {
                if (j >= col) {
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
        return currentState.length;
    }

    @Override
    public int getColumnCount() {
        return currentState.length > 0 ? currentState[0].length : 0;
    }

    public void set(int row, int col, char value) {
        this.currentState[row][col] = value;
    }
}
