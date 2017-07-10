import com.sun.istack.internal.NotNull;

/**
 * Created by Yuliy on 04.07.2017.
 */
public class Node {
    public enum Direction {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }

    private Node top;
    private Node bottom;
    private Node left;
    private Node right;

    private Object tag;

    public Node() {
        left = this;
        top = this;
        right = this;
        bottom = this;
    }

    public Node getInDirection(@NotNull Direction d){
        switch (d){
            case LEFT:
                return left;
            case TOP:
                return top;
            case RIGHT:
                return right;
            case BOTTOM:
                return bottom;
        }

        return null;
    }

    public void setInDirection(@NotNull Direction d, Node n){
        switch (d){
            case LEFT:
                left = n;
                break;
            case TOP:
                top = n;
                break;
            case RIGHT:
                right = n;
                break;
            case BOTTOM:
                bottom = n;
                break;
        }
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
