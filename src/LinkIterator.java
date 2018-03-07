import java.util.Iterator;

public class LinkIterator implements Iterator<Node> {
    private Node start;
    private Node current;
    private final Node.Direction iterationDirection;

    public LinkIterator(Node start, Node.Direction iterationDirection) {
        this.start = start;
        this.current = start;
        this.iterationDirection = iterationDirection;
    }

    @Override
    public boolean hasNext() {
        return current != null && current.getInDirection(iterationDirection) != start;
    }

    @Override
    public Node next() {
        if(hasNext()){
            current = current.getInDirection(iterationDirection);
        }
        return current;
    }

    @Override
    public void remove() {
        throw new RuntimeException("This operation is not allowed on this iterator.");
    }
}
