package base;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import static base.Node.Direction.*;

public class LinkedMatrix implements SolutionMatrix {
    private Node topLeftCorner;
    private final Deque<UndoableAction> history = new LinkedList<>();


    /**
     * @param initialState {@code Object[][]} row/column
     */
    public void init(final Object[][] initialState) {
        if (initialState.length == 0) {
            return;
        }

        topLeftCorner = new Node();
        topLeftCorner.setTag("top left corner");

        Node current = topLeftCorner;
        for (int c = 0; c < initialState[0].length; ++c) {
            Node newNode = new Node();
            newNode.setTag(new HeaderTag(c));

            current.setInDirection(RIGHT, newNode);
            newNode.setInDirection(LEFT, current);
            current = newNode;
        }
        current.setInDirection(RIGHT, topLeftCorner);
        topLeftCorner.setInDirection(LEFT, current);

        for (int r = initialState.length - 1; r >= 0; --r) {
            Node nodeAbove = topLeftCorner;
            Node rowAnchor = new Node();
            rowAnchor.setTag(new HeaderTag(r));

            Node nodeToLeft = rowAnchor;

            Node nodeBelow = topLeftCorner.getInDirection(BOTTOM);
            rowAnchor.setInDirection(BOTTOM, nodeBelow);
            nodeBelow.setInDirection(TOP, rowAnchor);
            nodeAbove.setInDirection(BOTTOM, rowAnchor);
            rowAnchor.setInDirection(TOP, nodeAbove);
            for (int c = 0; c < initialState[r].length; ++c) {
                nodeAbove = nodeAbove.getInDirection(RIGHT);
                nodeBelow = nodeAbove.getInDirection(BOTTOM);

                Object currValue = initialState[r][c];
                if (currValue != null) {
                    current = new Node();
                    current.setTag(currValue);

                    current.setInDirection(BOTTOM, nodeBelow);
                    nodeBelow.setInDirection(TOP, current);
                    nodeAbove.setInDirection(BOTTOM, current);
                    current.setInDirection(TOP, nodeAbove);

                    nodeToLeft.setInDirection(RIGHT, current);
                    current.setInDirection(LEFT, nodeToLeft);

                    nodeToLeft = current;
                }
            }

            nodeToLeft.setInDirection(RIGHT, rowAnchor);
            rowAnchor.setInDirection(LEFT, nodeToLeft);
        }

        history.clear();
    }

    /**
     * Iterable over a line (vertical or horizontal) created by walking from one node to the next until
     * it reaches the beginning or @null. Will create an endless loop if matrix is not constructed
     * properly
     *
     * @param start     starting node (where to iterate from)
     * @param direction direction to iterate over ({@link Node.Direction#BOTTOM} and {@link Node.Direction#TOP}
     *                  will iterate vertically and {@link Node.Direction#LEFT} and {@link Node.Direction#RIGHT}
     *                  will iterate horizontally)
     * @return Iterable which is a view on a specific line in the matrix
     */
    private Iterable<Node> getLineInDirection(final Node start, final Node.Direction direction) {
        return () -> new Iterator<Node>() {
            Node nextNode = start;

            @Override
            public boolean hasNext() {
                return nextNode != null;
            }

            @Override
            public Node next() {
                Node currentNode = nextNode;
                if (currentNode != null) {
                    nextNode = currentNode.getInDirection(direction);
                    if (nextNode == start) {
                        nextNode = null;
                    }
                }

                return currentNode;
            }
        };
    }

    @Override
    public Object get(int row, int column) {

        //find column anchor
        Node columnAnchor = getAnchor(Type.COLUMN, column);

        //collect all nodes in this column
        ArrayList<Node> nodesInColumn = new ArrayList<>();
        for (Node n : getLineInDirection(columnAnchor, BOTTOM)) {
            if (n.getTag() != null) {
                nodesInColumn.add(n);
            }
        }


        //find row anchor
        Node rowAnchor = getAnchor(Type.ROW, row);

        //collect all nodes in row
        ArrayList<Node> nodesInRow = new ArrayList<>();
        for (Node n : getLineInDirection(rowAnchor, RIGHT)) {
            if (n.getTag() != null) {
                nodesInRow.add(n);
            }
        }

        //find first element which is in both - the row and the column
        for (Node n : nodesInRow) {
            if (nodesInColumn.contains(n)) {
                return n.getTag();
            }
        }

        return null;
    }

    @Override
    public void removeRow(final int row) {
        remove(Type.ROW, getAnchor(Type.ROW, row));
    }

    @Override
    public void removeColumn(final int col) {
        remove(Type.COLUMN, getAnchor(Type.COLUMN, col));
    }

    private Node getAnchor(final Type type, final int index) {
        if(index < 0){
            throw new IndexOutOfBoundsException(String.format("Attempting to access column/row %d. This column/row does not exist.", index));
        }

        final Node.Direction walkingDirection;

        if (type == Type.ROW) {
            walkingDirection = BOTTOM;
        } else {
            walkingDirection = RIGHT;
        }

        //find column anchor
        Node anchor = topLeftCorner;
        for (int i = 0; i <= index; i++) {
            anchor = anchor.getInDirection(walkingDirection);
            if (anchor == topLeftCorner) {//we looped around: This column/row does not exist
                throw new IndexOutOfBoundsException(String.format("Attempting to access column/row %d. This column/row does not exist.", index));
            }
        }

        return anchor;
    }

    private void remove(@NotNull final Type type, final Node startFrom) {
        //remember anchor element to undo it
        history.push(new RemoveAction(type, startFrom));

        //walk every item
        final Node.Direction orthogonalDirectionA;
        final Node.Direction orthogonalDirectionB;
        final Node.Direction walkingDirection;

        if (type == Type.ROW) {
            orthogonalDirectionA = TOP;
            orthogonalDirectionB = BOTTOM;
            walkingDirection = RIGHT;

        } else if (type == Type.COLUMN) {
            orthogonalDirectionA = LEFT;
            orthogonalDirectionB = RIGHT;
            walkingDirection = BOTTOM;
        } else {
            //make the compiler happy
            throw new IllegalStateException(String.format("Work on type %s is not supported.", type));
        }


        for (Node n : getLineInDirection(startFrom, walkingDirection)) {

            //bridge every item's neighbours in the corresponding direction
            Node orthogonalNodeA = n.getInDirection(orthogonalDirectionA);
            Node orthogonalNodeB = n.getInDirection(orthogonalDirectionB);

            orthogonalNodeA.setInDirection(orthogonalDirectionB, orthogonalNodeB);
            orthogonalNodeB.setInDirection(orthogonalDirectionA, orthogonalNodeA);
        }
    }

    @Override
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Cannot undo.");
        }

        UndoableAction undoAction = history.pop();

        if(undoAction instanceof RemoveAction) {
            RemoveAction undoRemoveAction = (RemoveAction) undoAction;

            //walk every item
            final Node.Direction orthogonalDirectionA;
            final Node.Direction orthogonalDirectionB;
            final Node.Direction walkingDirection;

            if (undoRemoveAction.actionType == Type.ROW) {
                orthogonalDirectionA = TOP;
                orthogonalDirectionB = BOTTOM;
                walkingDirection = RIGHT;

            } else if (undoRemoveAction.actionType == Type.COLUMN) {
                orthogonalDirectionA = LEFT;
                orthogonalDirectionB = RIGHT;
                walkingDirection = BOTTOM;
            } else {
                //make the compiler happy
                throw new IllegalStateException(String.format("Work on type %s is not supported.", undoRemoveAction.actionType));
            }

            for (Node n : getLineInDirection(undoRemoveAction.anchorElement, walkingDirection)) {
                //insert every item's between its previous neighbours in the corresponding direction
                Node orthogonalNodeA = n.getInDirection(orthogonalDirectionA);
                Node orthogonalNodeB = n.getInDirection(orthogonalDirectionB);

                orthogonalNodeA.setInDirection(orthogonalDirectionB, n);
                orthogonalNodeB.setInDirection(orthogonalDirectionA, n);
            }
        }else if(undoAction instanceof MultipleUndoAction){
            for (int i = 0; i < ((MultipleUndoAction) undoAction).undoActionsCount; i++) {
                undo();
            }
        }
    }

    @Override
    public boolean canUndo() {
        return !history.isEmpty();
    }

    @Override
    public int getRowCount() {
        return count(Type.ROW);
    }

    @Override
    public int getColumnCount() {
        return count(Type.COLUMN);
    }

    @Override
    public int getRowId(int rowPosition) {
        return ((HeaderTag) getAnchor(Type.ROW, rowPosition).getTag()).id;
    }

    @Override
    public int getColumnId(int columnPosition) {
        return ((HeaderTag) getAnchor(Type.COLUMN, columnPosition).getTag()).id;
    }

    @Override
    public void clearRowAndAffectedColumns(int rowPosition) {
        Node rowAnchor = getAnchor(Type.ROW, rowPosition);

        int performedOperationsCount = 0;

        Node currentNode = rowAnchor.getInDirection(RIGHT);
        while (currentNode != rowAnchor) {
            currentNode = currentNode.getInDirection(RIGHT);

            //remove all affected columns
            remove(Type.COLUMN, currentNode);
            performedOperationsCount++;

            //for each affected column remove all rows affected by that
            // but be careful not to remove the header row
            for (Node n : getLineInDirection(currentNode, BOTTOM)) {
                if (!(n.getTag() instanceof HeaderTag)) {
                    remove(Type.ROW, n);
                    performedOperationsCount++;
                }
            }
        }

        history.push(new MultipleUndoAction(performedOperationsCount));
    }

    private int count(@NotNull final Type type) {
        final Node.Direction targetDirection;
        if (type == Type.ROW) {
            targetDirection = BOTTOM;

        } else if (type == Type.COLUMN) {
            targetDirection = RIGHT;

        } else {
            //make the compiler happy
            throw new IllegalStateException(String.format("Work on type %s is not supported.", type));
        }

        int itemCount = 0;
        Iterator<Node> lineIter = getLineInDirection(topLeftCorner, targetDirection).iterator();
        while (lineIter.hasNext()) {
            lineIter.next();
            ++itemCount;
        }

        return itemCount - 1; //we do not count top left corner element which was returned first
    }

    private interface UndoableAction{}

    private static class RemoveAction implements UndoableAction{
        private final Type actionType;
        private final Node anchorElement;

        private RemoveAction(Type removeActionType, Node removeAnchorElement) {
            this.actionType = removeActionType;
            this.anchorElement = removeAnchorElement;
        }
    }

    private static class MultipleUndoAction implements UndoableAction{
        private final int undoActionsCount;

        private MultipleUndoAction(int undoActionsCount) {
            this.undoActionsCount = undoActionsCount;
        }
    }

    private enum Type {
        ROW,
        COLUMN
    }

    private static class HeaderTag {
        final int id;

        private HeaderTag(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "HeaderTag{" + id + '}';
        }
    }
}