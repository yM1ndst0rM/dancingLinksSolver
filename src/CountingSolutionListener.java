import java.util.List;

public class CountingSolutionListener implements Solver.SolutionListener {
    private int solutionCount = 0;

    @Override
    public void onSolutionFound(List<Step> stepsToSolution) {
        solutionCount++;
    }

    public void printCurrentSolutionCount(){
        System.out.println("Number of solutions found: " + solutionCount);
    }
}
