/**
 * Created by Yuliy on 04.07.2017.
 */
public class Solver {
    private Node headerRow;

    public Solver(int columns) {
        if(columns <= 0) throw new IllegalArgumentException("Columns param may not be less then or equal 0. Current value: " + columns);

        Node current = null;
        for (int i = 0; i < columns; ++i){
            if(current == null){
                current = new Node();
                headerRow = current;
            }else {
                Node n = new Node();
                current.setInDirection(Node.Direction.RIGHT, n);
                n.setInDirection(Node.Direction.LEFT, current);
                current = n;
            }
        }
    }
}
