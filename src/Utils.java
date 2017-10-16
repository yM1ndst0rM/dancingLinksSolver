/**
 * Created by Yuliy on 16.10.2017.
 */
public class Utils {
    public static int elemPos(int row, int col){
        return ((row - 1) * row)/2 + col;
    }

    public static int elemSum(int sideLen){
        return elemPos(sideLen, 0);
    }
}
