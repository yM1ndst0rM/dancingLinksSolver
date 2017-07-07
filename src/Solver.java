/**
 * Created by Yuliy on 04.07.2017.
 */
public class Solver {
    private Node headerRow;

    public Solver(int sideLen, int variations) {
        if(sideLen <= 0) throw new IllegalArgumentException("Matrix side length param may not be less then or equal 0. Current value: " + sideLen);
        if(variations <= 0) throw new IllegalArgumentException("Variations param may not be less then or equal 0. Current value: " + sideLen);

        int headerRowLen = variations * sideLen;
        Node current = null;
        for (int i = 0; i < headerRowLen; ++i){
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

    public void fillMatrix(int sideLen, int variations){
        int posCount = getPosCount(sideLen);

        for (int i = 0; i < variations; i++) {
            //calculate where conditions of this specific variation are stored
            int minVariationIndex = i * sideLen;
            int maxVariationIndex = minVariationIndex + sideLen;
            for (int row = 0; row < sideLen; ++row) {
                for (int column = 0; column <= row ; column++) {
                    Node currentColumnHeader = headerRow;
                    Node lastRowElem = null;
                    Node firstRowElem = null;
                    int columnIndex = 0;
                    do{
                        int currentVariationIndex = columnIndex - minVariationIndex;
                        if(currentVariationIndex > 0 && (currentVariationIndex == row || currentVariationIndex == column)){
                            //special simplification for symmetry reasons:
                            //      blocking a row blocks also the corresponding column;
                            //      blocking a column blocks also the corresponding row
                            //this allows to simplify conditions to only verify complete coverage of either rows or columns (both would yield same results)
                            Node newElem = new Node();

                            //set row elements
                            if(firstRowElem != null && lastRowElem != null/*condition inserted for clarity - always true when reached*/){
                                lastRowElem.setInDirection(Node.Direction.RIGHT, newElem);
                                newElem.setInDirection(Node.Direction.LEFT, lastRowElem);
                                firstRowElem.setInDirection(Node.Direction.LEFT, newElem);
                                newElem.setInDirection(Node.Direction.RIGHT, firstRowElem);
                            }else {
                                firstRowElem = newElem;
                            }

                            lastRowElem = newElem;

                            //set column elements - new rows will be inserted as first element under header
                            newElem.setInDirection(Node.Direction.BOTTOM, currentColumnHeader.getInDirection(Node.Direction.BOTTOM));
                            currentColumnHeader.getInDirection(Node.Direction.BOTTOM).setInDirection(Node.Direction.TOP, newElem);

                            newElem.setInDirection(Node.Direction.TOP, currentColumnHeader);
                            currentColumnHeader.setInDirection(Node.Direction.BOTTOM, newElem);
                        }

                        ++columnIndex;
                        currentColumnHeader = currentColumnHeader.getInDirection(Node.Direction.RIGHT);
                    }while (currentColumnHeader != headerRow && columnIndex < maxVariationIndex);
                }
            }
        }
    }

    private int getPosCount(int sideLen){
        return (sideLen * (sideLen - 1)) / 2;
    }
}
