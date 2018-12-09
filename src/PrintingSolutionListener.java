import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrintingSolutionListener implements Solver.SolutionListener {
    private final int sideLen;

    public PrintingSolutionListener(int sideLen) {
        this.sideLen = sideLen;
    }

    @Override
    public void onSolutionFound(List<Step> stepsToSolution){
        System.out.println(getSolutionAsPrintable(sideLen, new ArrayList<>(stepsToSolution)));
    }

    public static String getSolutionAsPrintable(final int sideLen, List<Step> stepsToSolution) {
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < sideLen; row++) {
            out.append('|');

            for (int column = 0; column < sideLen; column++) {
                if (row == column) {
                    out.append('\\');
                    continue;
                }

                out.append('-');

                for (Iterator<Step> iterator = stepsToSolution.iterator(); iterator.hasNext(); ) {
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

        return out.toString();
    }
}
