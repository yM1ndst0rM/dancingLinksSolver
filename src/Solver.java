import base.SolutionMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        nextSolveStep(possibleSteps, l, sideLen * variationsCount, m, new ArrayList<>());
    }

    private static void nextSolveStep(
            @NotNull final Step[] possibleSteps,
            @NotNull final SolutionListener l,
            final int firstOptionalConditionId,
            @NotNull final SolutionMatrix m,
            @NotNull final List<Step> history){

        if(m.getColumnCount() == 0 || m.getColumnId(0) >= firstOptionalConditionId){
            l.onSolutionFound(Collections.unmodifiableList(history));
            return;
        }


        Collection<Integer> rowsAffectedByColumn = m.getRowsAffectedByColumn(0);
        for (int currentRowIndex: rowsAffectedByColumn) {
            history.add(possibleSteps[m.getRowId(currentRowIndex)]);
            m.clearRowAndAffectedColumns(currentRowIndex);

            nextSolveStep(possibleSteps, l, firstOptionalConditionId, m, history);

            m.undo();
            history.remove(history.size() - 1);
        }
    }

    public interface SolutionListener{
        void onSolutionFound(List<Step> stepsToSolution);
    }
}
