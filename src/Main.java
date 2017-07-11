import java.util.ArrayList;

/**
 * Created by Yuliy on 11.07.2017.
 */
public class Main {
    public static void main(String[] args){
        Solver solver = new Solver(4, 3);
        solver.fillMatrix();
        solver.solve(new ArrayList<>());
    }
}
