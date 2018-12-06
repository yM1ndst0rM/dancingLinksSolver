package base;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class ArrayMatrix implements SolutionMatrix {
    private final Deque<Character[][]> history = new LinkedList<>();
    private Character[][] currentState = new Character[0][];

    @Override
    public void init(final Character[][] initialState) {
        currentState = new Character[initialState.length][];
        for (int i = 0; i < currentState.length; i++) {
            currentState[i] = Arrays.copyOf(initialState[i], initialState[i].length);
        }

        history.clear();
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

        Character[][] newMatrix = new Character[getRowCount() - 1][];
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

        Character[][] newMatrix = new Character[getRowCount()][getColumnCount() - 1];
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
