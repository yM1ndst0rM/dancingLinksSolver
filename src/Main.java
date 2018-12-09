import base.ArrayMatrix;

/**
 * Created by Yuliy on 11.07.2017.
 */
public class Main {
    public static void main(String[] args){
        final int sideLen = 8;
        final int variationsCount = 3;

        //Solver.SolutionListener listener = new PrintingSolutionListener(sideLen);
        Solver.SolutionListener listener = new CountingSolutionListener();
        Solver.solve(sideLen, variationsCount, new ArrayMatrix(), listener);

        ((CountingSolutionListener) listener).printCurrentSolutionCount();
    }
}
