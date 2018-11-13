package base;

import com.sun.istack.internal.NotNull;

import java.util.Deque;
import java.util.LinkedList;

import static base.Node.Direction.*;

public class LinkedMatrix implements SolutionMatrix {
    private final Node topLeftCorner = new Node();
    private final Deque<RemoveAction> history = new LinkedList<>();


    /**
     * @param initialState {@code char[][]} row/column
     */
    public LinkedMatrix(final char[][] initialState) {
        if (initialState.length == 0) {
            return;
        }

        Node current = topLeftCorner;
        for (int r = 0; r < initialState.length; ++r) {
            Node newNode = new Node();
            newNode.setTag(String.format("Row %d", r));
            current.setInDirection(BOTTOM, newNode);
            newNode.setInDirection(TOP, current);
            current = newNode;
        }
        current.setInDirection(BOTTOM, topLeftCorner);
        topLeftCorner.setInDirection(TOP, current);

        current = topLeftCorner;
        for (int c = 0; c < initialState[0].length; ++c) {
            Node newNode = new Node();
            newNode.setTag(String.format("Column %d", c));
            current.setInDirection(RIGHT, newNode);
            newNode.setInDirection(LEFT, current);
            current = newNode;
        }
        current.setInDirection(RIGHT, topLeftCorner);
        topLeftCorner.setInDirection(LEFT, current);
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
            if(anchorNode == topLeftCorner){
                //we have reached the outer limit of the array before finding
                //the anchor with the correct index
                throw new ArrayIndexOutOfBoundsException(index);
            }
            if(currentAnchorIndex == index) break;
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

        Node currentNode = anchorNode;

        do {
            //bridge every item's neighbours in the corresponding direction
            Node orthogonalNodeA = currentNode.getInDirection(orthogonalDirectionA);
            Node orthogonalNodeB = currentNode.getInDirection(orthogonalDirectionB);

            orthogonalNodeA.setInDirection(orthogonalDirectionB, orthogonalNodeB);
            orthogonalNodeB.setInDirection(orthogonalDirectionA, orthogonalNodeA);

            currentNode = currentNode.getInDirection(walkingDirection);
        } while (currentNode != anchorNode);//potential infinite loop if the array is constructed poorly

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

        Node currentNode = undoAction.anchorElement;

        do {
            //insert every item's between its previous neighbours in the corresponding direction
            Node orthogonalNodeA = currentNode.getInDirection(orthogonalDirectionA);
            Node orthogonalNodeB = currentNode.getInDirection(orthogonalDirectionB);

            orthogonalNodeA.setInDirection(orthogonalDirectionB, currentNode);
            orthogonalNodeB.setInDirection(orthogonalDirectionA, currentNode);

            currentNode = currentNode.getInDirection(walkingDirection);
        } while (currentNode != undoAction.anchorElement);//potential infinite loop if the array is constructed poorly

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