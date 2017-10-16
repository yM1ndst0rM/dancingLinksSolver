import com.sun.istack.internal.NotNull;

/**
 * Created by Yuliy on 13.07.2017.
 */
public class Output {

    private static final String BLANK = "     ";
    private static final String SINGLE_VERTICAL_LINK = "  |  ";
    private static final String SINGLE_HORIZONTAL_LINK = "-";
    private static final String HORIZONTAL_LINK_BLANK = " ";
    private static final int NOT_SET = -1;

    public static String sparseMatrixToString(@NotNull Node topLeftCorner) {
        Node currentElem = topLeftCorner;
        int columns = 0;
        do {
            ++columns;
            currentElem = currentElem.getInDirection(Node.Direction.RIGHT);
        } while (currentElem != topLeftCorner);

        int rows = 0;
        do {
            ++rows;
            currentElem = currentElem.getInDirection(Node.Direction.BOTTOM);
        } while (currentElem != topLeftCorner);

        Node[][] matrix = new Node[rows][columns];

        for (int row = 0; row < rows; ++row) {
            Node rowHeader = currentElem;
            int column = 0;

            //first fill in all elements in the correct row disregarding column
            for (; column < columns; ++column) {
                matrix[row][column] = currentElem;
                currentElem = currentElem.getInDirection(Node.Direction.RIGHT);
                if (currentElem == rowHeader) {
                    break;
                }
            }

            for (int currentColumn = column; currentColumn > 0; currentColumn--) {
                Node currentElemInRow = matrix[row][currentColumn];
                //now move each element in the row to its respective column
                int targetColumn = NOT_SET;
                for (int associatedRow = row - 1; associatedRow >= 0 && targetColumn == NOT_SET; associatedRow--) {
                    for (int associatedColumn = currentColumn; associatedColumn < columns && targetColumn == NOT_SET; associatedColumn++) {
                        Node associatedElem = matrix[associatedRow][associatedColumn];
                        if (associatedElem != null &&
                                (associatedElem.getInDirection(Node.Direction.BOTTOM) == currentElemInRow
                                        || currentElemInRow.getInDirection(Node.Direction.TOP) == associatedElem)) {
                            targetColumn = associatedColumn;
                        }
                    }
                }

                if (targetColumn != NOT_SET) {
                    matrix[row][targetColumn] = currentElemInRow;
                    matrix[row][currentColumn] = null;
                }
            }
        }


        StringBuilder out = new StringBuilder();

        for (int outputRow = 0; outputRow < rows; outputRow++){
            out.append(printSingleRow(matrix[outputRow])).append('\n');
        }

        return out.toString();
    }

    private static String printSingleRow(@NotNull Node[] row) {
        StringBuilder out = new StringBuilder();
        for (Node rowElem : row) {
            if (rowElem == null) {
                out.append(BLANK);
            } else {
                Node topElem = rowElem.getInDirection(Node.Direction.TOP);
                if (topElem == null || topElem == rowElem || topElem.getInDirection(Node.Direction.BOTTOM) != rowElem) {
                    out.append(BLANK);
                } else {
                    out.append(SINGLE_VERTICAL_LINK);
                }
            }
        }

        out.append('\n');

        for (Node rowElem : row) {
            if (rowElem == null) {
                out.append(BLANK);
            } else {
                Node leftElem = rowElem.getInDirection(Node.Direction.LEFT);
                if (leftElem == null || leftElem == rowElem || leftElem.getInDirection(Node.Direction.RIGHT) != rowElem) {
                    out.append(HORIZONTAL_LINK_BLANK);
                } else {
                    out.append(SINGLE_HORIZONTAL_LINK);
                }

                out.append(singleNodeToString(rowElem));

                Node rightElem = rowElem.getInDirection(Node.Direction.RIGHT);
                if (rightElem == null || rightElem == rowElem || rightElem.getInDirection(Node.Direction.LEFT) != rowElem) {
                    out.append(HORIZONTAL_LINK_BLANK);
                } else {
                    out.append(SINGLE_HORIZONTAL_LINK);
                }
            }
        }

        out.append('\n');

        for (Node rowElem : row) {
            if (rowElem == null) {
                out.append(BLANK);
            } else {
                Node bottomElem = rowElem.getInDirection(Node.Direction.BOTTOM);
                if (bottomElem == null || bottomElem == rowElem || bottomElem.getInDirection(Node.Direction.TOP) != rowElem) {
                    out.append(BLANK);
                } else {
                    out.append(SINGLE_VERTICAL_LINK);
                }
            }
        }

        return out.toString();
    }

    public static String singleNodeToString(@NotNull Node n) {
        return String.format("(%s)", n.getTag() != null ? n.getTag() : 'O');
    }
}
