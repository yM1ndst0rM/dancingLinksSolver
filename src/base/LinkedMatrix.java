package base;

import com.sun.istack.internal.NotNull;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import static base.Node.Direction.*;

public class LinkedMatrix implements SolutionMatrix {
    private Node topLeftCorner = new Node();
    private final Deque<RemoveAction> history = new LinkedList<>();


    /**
     * @param initialState {@code char[][]} row/column
     */
    public void init(final Character[][] initialState) {
        if (initialState.length == 0) {
            return;
        }

        topLeftCorner = new Node();

        Node current = topLeftCorner;
        for (int c = 0; c < initialState[0].length; ++c) {
            Node newNode = new Node();
            newNode.setTag(String.format("Column %d", c));

            newNode.setInDirection(BOTTOM, newNode);
            newNode.setInDirection(TOP, newNode);

            current.setInDirection(RIGHT, newNode);
            newNode.setInDirection(LEFT, current);
            current = newNode;
        }
        current.setInDirection(RIGHT, topLeftCorner);
        topLeftCorner.setInDirection(LEFT, current);

        Node nodeAbove = topLeftCorner;
        for (int r = initialState.length - 1; r >= 0; ++r) {
            Node rowAnchor = new Node();
            rowAnchor.setTag(String.format("Row %d", r));

            Node nodeToLeft = rowAnchor;

            Node nodeBelow = topLeftCorner.getInDirection(BOTTOM);
            rowAnchor.setInDirection(BOTTOM, nodeBelow);
            nodeBelow.setInDirection(TOP, rowAnchor);
            nodeAbove.setInDirection(BOTTOM, rowAnchor);
            rowAnchor.setInDirection(TOP, nodeAbove);
            for (int c = 0; c < initialState[r].length; ++c) {
                nodeAbove = nodeAbove.getInDirection(RIGHT);
                nodeBelow = nodeAbove.getInDirection(BOTTOM);

                Character currValue = initialState[r][c];
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
        current.setInDirection(BOTTOM, topLeftCorner);
        topLeftCorner.setInDirection(TOP, current);

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
            Node currentNode = start;

            @Override
            public boolean hasNext() {
                return currentNode != null && currentNode.getInDirection(direction) != start;
            }

            @Override
            public Node next() {
                if (currentNode != null) {
                    currentNode = currentNode.getInDirection(direction);
                }

                return currentNode;
            }
        };
    }

    @Override
    public char get(int row, int column) {

    }

    @Override
    public void removeRow(final int row) {
        remove(Type.ROW, row);
    }

    @Override
    public void removeColumn(final int col) {
        remove(Type.COLUMN, col);
    }

    private void remove(@NotNull final Type type, final int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        //find correct anchor element

        final Node.Direction anchorWalkingDirection;
        if (type == Type.ROW) {
            anchorWalkingDirection = BOTTOM;

        } else if (type == Type.COLUMN) {
            anchorWalkingDirection = RIGHT;
        } else {
            //make the compiler happy
            throw new IllegalStateException(String.format("Work on type %s is not supported.", type));
        }

        Node anchorNode = topLeftCorner;
        int currentAnchorIndex = 0;
        while (true) {
            anchorNode = anchorNode.getInDirection(anchorWalkingDirection);
            if (anchorNode == topLeftCorner) {
                //we have reached the outer limit of the array before finding
                //the anchor with the correct index
                throw new ArrayIndexOutOfBoundsException(index);
            }
            if (currentAnchorIndex == index) break;
            currentAnchorIndex++;
        }


        //remember anchor element to undo it
        history.push(new RemoveAction(type, anchorNode));

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


        for (Node n : getLineInDirection(anchorNode, walkingDirection)) {

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

        RemoveAction undoAction = history.pop();

        //walk every item
        final Node.Direction orthogonalDirectionA;
        final Node.Direction orthogonalDirectionB;
        final Node.Direction walkingDirection;

        if (undoAction.actionType == Type.ROW) {
            orthogonalDirectionA = TOP;
            orthogonalDirectionB = BOTTOM;
            walkingDirection = RIGHT;

        } else if (undoAction.actionType == Type.COLUMN) {
            orthogonalDirectionA = LEFT;
            orthogonalDirectionB = RIGHT;
            walkingDirection = BOTTOM;
        } else {
            //make the compiler happy
            throw new IllegalStateException(String.format("Work on type %s is not supported.", undoAction.actionType));
        }

        for (Node n : getLineInDirection(undoAction.anchorElement, walkingDirection)) {
            //insert every item's between its previous neighbours in the corresponding direction
            Node orthogonalNodeA = n.getInDirection(orthogonalDirectionA);
            Node orthogonalNodeB = n.getInDirection(orthogonalDirectionB);

            orthogonalNodeA.setInDirection(orthogonalDirectionB, n);
            orthogonalNodeB.setInDirection(orthogonalDirectionA, n);
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

    private int count(@NotNull final Type type) {
        final Node.Direction targetDirection;
        if (type == Type.ROW) {
            targetDirection = RIGHT;

        } else if (type == Type.COLUMN) {
            targetDirection = BOTTOM;

        } else {
            //make the compiler happy
            throw new IllegalStateException(String.format("Work on type %s is not supported.", type));
        }

        //walk the outermost row/column (the anchors)
        // until the starting point is reached and
        // count the steps

        Node currentNode = topLeftCorner;
        int itemCount = 0;
        while (true) {//potential infinite loop if the the "array" is misconstructed
            currentNode = currentNode.getInDirection(targetDirection);
            if (currentNode == topLeftCorner) break;

            itemCount++;
        }

        return itemCount;
    }

    private static class RemoveAction {
        public final Type actionType;
        public final Node anchorElement;

        private RemoveAction(Type removeActionType, Node removeAnchorElement) {
            this.actionType = removeActionType;
            this.anchorElement = removeAnchorElement;
        }
    }

    private enum Type {
        ROW,
        COLUMN
    }
}