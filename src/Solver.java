import base.SolutionMatrix;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuliy on 04.07.2017.
 */
public class Solver {
    public static void solve(int sideLen, int variationsCount, SolutionMatrix m, SolutionListener l){
        if (sideLen <= 0)
            throw new IllegalArgumentException("Matrix side length param may not be less then or equal 0. Current value: " + sideLen);
        if (variationsCount <= 0)
            throw new IllegalArgumentException("variationsCount param may not be less then or equal 0. Current value: " + sideLen);

        //create base matrix

        //Count available spaces using lower triangle matrix as there is no need
        // to account for the rest of the matrix for symmetry reasons
        int availableSpacesCount = Utils.elemSum(sideLen);

        //conditions are:
        // - Every row/column (one condition for symmetry reasons) contains every variation exactly once.
        // - Every space is occupied at most once
        int conditionsCount = sideLen * variationsCount + availableSpacesCount;

        //possible steps are:
        // For every available space occupy it with every possible variation in turn
        int possibleStepsCount = availableSpacesCount * variationsCount;

        //this will help retrace what steps were taken, when a solution is found
        Step[] possibleSteps = new Step[possibleStepsCount];

        //actual matrix holding the layout of the problem
        String[][] solutionsMatrix = new String[possibleStepsCount][conditionsCount];

        int optionIndex = 0;
        for (int row = 0; row < sideLen; row++) {
            for (int column = 0; column < row; column++) {
                for (int variation = 0; variation < variationsCount; variation++, optionIndex++) {
                    possibleSteps[optionIndex] = new Step(row, column, variation);

                    solutionsMatrix[optionIndex][variation * sideLen + row] =
                            String.format("row/column %d occupied by variation %d", row, variation);

                    solutionsMatrix[optionIndex][variation * sideLen + column] =
                            String.format("row/column %d occupied by variation %d", column, variation);

                    solutionsMatrix[optionIndex][variationsCount * sideLen + Utils.elemPos(row, column)] =
                            String.format("position %d (row: %d, col: %d) occupied by variation %d", Utils.elemPos(row, column), row, column, variation);
                }
            }
        }

        m.init(solutionsMatrix);


        //solve it
        nextSolveStep(possibleSteps, l, sideLen * variationsCount, 0, m, new ArrayList<>());
    }

    private static void nextSolveStep(
            @NotNull final Step[] possibleSteps,
            @NotNull final SolutionListener l,
            final int firstOptionalConditionId,
            final int lastStepPosition,
            @NotNull final SolutionMatrix m,
            @NotNull final List<Step> history){

        if(m.getColumnCount() == 0 || m.getColumnId(0) >= firstOptionalConditionId){
            l.onSolutionFound(history);
            return;
        }

        int rowCount = m.getRowCount();
        for (int i = lastStepPosition; i < rowCount; i++) {
            history.add(possibleSteps[m.getRowId(i)]);
            m.clearRowAndAffectedColumns(i);

            nextSolveStep(possibleSteps, l, firstOptionalConditionId, i, m, history);

            m.undo();
            history.remove(history.size() - 1);
        }
    }

    public interface SolutionListener{
        void onSolutionFound(List<Step> stepsToSolution);
    }

 /*   private void fillMatrix(int sideLen, int variations) {
        int posCount = getPosCount(sideLen);

        for (int variationIndex = 0; variationIndex < variations; variationIndex++) {
            //calculate where conditions of this specific variation are stored
            int minVariationIndex = variationIndex * sideLen;
            int maxVariationIndex = minVariationIndex + sideLen;
            for (int row = 0; row < sideLen; ++row) {
                for (int column = 0; column <= row; column++) {
                    Node currentColumnHeader = headerRow.getInDirection(Node.Direction.RIGHT);
                    Node lastRowElem = new Node(); //row header elem
                    Node firstRowElem = lastRowElem;

                    firstRowElem.setTag(new Step(row, column, variationIndex));

                    //set column elements - new rows will be inserted as first element under header
                    firstRowElem.setInDirection(Node.Direction.BOTTOM, headerRow.getInDirection(Node.Direction.BOTTOM));
                    headerRow.getInDirection(Node.Direction.BOTTOM).setInDirection(Node.Direction.TOP, firstRowElem);

                    firstRowElem.setInDirection(Node.Direction.TOP, headerRow);
                    headerRow.setInDirection(Node.Direction.BOTTOM, firstRowElem);

                    int columnIndex = 0;
                    do {
                        int currentVariationIndex = columnIndex - minVariationIndex;
                        if (currentVariationIndex > 0 && (currentVariationIndex == row || currentVariationIndex == column)) {
                            //special simplification for symmetry reasons:
                            //      blocking a row blocks also the corresponding column;
                            //      blocking a column blocks also the corresponding row
                            //this allows to simplify conditions to only verify complete coverage of either rows or columns (both would yield same results)
                            Node newElem = new Node();

                            //set row elements
                            lastRowElem.setInDirection(Node.Direction.RIGHT, newElem);
                            newElem.setInDirection(Node.Direction.LEFT, lastRowElem);
                            firstRowElem.setInDirection(Node.Direction.LEFT, newElem);
                            newElem.setInDirection(Node.Direction.RIGHT, firstRowElem);

                            lastRowElem = newElem;

                            //set column elements - new rows will be inserted as first element under header
                            newElem.setInDirection(Node.Direction.BOTTOM, currentColumnHeader.getInDirection(Node.Direction.BOTTOM));
                            currentColumnHeader.getInDirection(Node.Direction.BOTTOM).setInDirection(Node.Direction.TOP, newElem);

                            newElem.setInDirection(Node.Direction.TOP, currentColumnHeader);
                            currentColumnHeader.setInDirection(Node.Direction.BOTTOM, newElem);
                        }

                        ++columnIndex;
                        currentColumnHeader = currentColumnHeader.getInDirection(Node.Direction.RIGHT);
                    } while (currentColumnHeader != headerRow && columnIndex < maxVariationIndex);
                }
            }
        }
    }


    public void solve(@NotNull List<Node> path){
        Node nextRow = headerRow;
        while(headerRow != (nextRow = nextRow.getInDirection(Node.Direction.BOTTOM))) {

            walkRows(nextRow, true);

            path.add(nextRow);

            if (headerRow.getInDirection(Node.Direction.RIGHT) == headerRow) {
                //there are no columns left: we have a solution
                onSolutionFound(path);
            } else {
                //onSolutionFound(path);
                solve(path);
            }

            path.remove(path.size() - 1);

            walkRows(nextRow, false);

        }
        //we have no more rows to take so this is not a solution
    }

    private void walkRows(@NotNull Node rowHeader, boolean detach){
        Node nextRowElem = rowHeader.getInDirection(Node.Direction.RIGHT);
        while (nextRowElem != rowHeader) {
            final Node columnHeaderElem = nextRowElem.getInDirection(Node.Direction.TOP);

            final Node headerLeftElem = columnHeaderElem.getInDirection(Node.Direction.LEFT);
            final Node headerRightElem = columnHeaderElem.getInDirection(Node.Direction.RIGHT);

            if (detach) {
                headerLeftElem.setInDirection(Node.Direction.RIGHT, headerRightElem);
                headerRightElem.setInDirection(Node.Direction.LEFT, headerLeftElem);
            }else {
                headerLeftElem.setInDirection(Node.Direction.RIGHT, columnHeaderElem);
                headerRightElem.setInDirection(Node.Direction.LEFT, columnHeaderElem);
            }

            Node anotherRow = nextRowElem.getInDirection(Node.Direction.BOTTOM);
            while (anotherRow != columnHeaderElem) {
                Node otherRowElem = anotherRow.getInDirection(Node.Direction.RIGHT);
                while (otherRowElem != anotherRow) {
                    Node upperElem = otherRowElem.getInDirection(Node.Direction.TOP);
                    Node lowerElem = otherRowElem.getInDirection(Node.Direction.BOTTOM);

                    if (detach) {
                        upperElem.setInDirection(Node.Direction.BOTTOM, lowerElem);
                        lowerElem.setInDirection(Node.Direction.TOP, upperElem);
                    }else{
                        upperElem.setInDirection(Node.Direction.BOTTOM, otherRowElem);
                        lowerElem.setInDirection(Node.Direction.TOP, otherRowElem);
                    }

                    otherRowElem = otherRowElem.getInDirection(Node.Direction.RIGHT);
                }
            }

            nextRowElem = nextRowElem.getInDirection(Node.Direction.RIGHT);
        }
    }

    private int getPosCount(int sideLen) {
        return (sideLen * (sideLen - 1)) / 2;
    }


    private void onSolutionFound(@NotNull List<Node> path){
        ArrayList<Step> solution = new ArrayList<>(path.size());
        for (Node n :path) {
            solution.add((Step) n.getTag());
        }

        StringBuilder out = new StringBuilder();
        for (int row = 0; row < this.sideLen; row++) {
            out.append('|');

            for (int column = 0; column < this.sideLen; column++) {
                if(row == column){
                    out.append('\\');
                    continue;
                }

                out.append('-');

                for (Iterator<Step> iterator = solution.iterator(); iterator.hasNext(); ) {
                    Step s = iterator.next();
                    if ((s.getRow() == column && s.getCol() == row)
                            || (s.getRow() == row && s.getCol() == column)) {
                        out.replace(out.length() - 1, out.length(), String.valueOf(s.getVariation()));
                        if (column < row) {
                            iterator.remove();
                        }
                        break;
                    }
                }
            }

            out.append('|').append('\n');
        }

        System.out.println(out.toString());
    }
*/
}
