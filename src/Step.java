/**
 * Created by Yuliy on 10.07.2017.
 */
public class Step {
    private final int row;
    private final int col;
    private final int variation;


    public Step(final int row, final int col, final int variation) {
        this.row = row;
        this.col = col;
        this.variation = variation;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getVariation() {
        return variation;
    }
}
