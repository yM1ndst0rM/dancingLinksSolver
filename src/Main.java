import base.LinkedMatrix;

/**
 * Created by Yuliy on 11.07.2017.
 */
public class Main {
    public static void main(String[] args){
        PrintingSolutionListener listener = new PrintingSolutionListener(4);
        Solver.solve(4, 3, new LinkedMatrix(), listener);
    }
}
