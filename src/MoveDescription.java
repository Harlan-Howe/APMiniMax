import java.util.Arrays;

public class MoveDescription
{
    private int whichPlayerIsMoving; // GamePanel.HUMAN or GamePanel.COMPUTER
    private int[] startPos, destinationPos;   // (row, col)
    private int whatIsPickedUp; // the score that is on the destination square

    public MoveDescription(int whichPlayerIsMoving, int[] startPos, int[] destinationPos, int whatIsPickedUp)
    {
        this.whichPlayerIsMoving = whichPlayerIsMoving;
        this.startPos = startPos;
        this.destinationPos = destinationPos;
        this.whatIsPickedUp = whatIsPickedUp;
    }

    public int getWhichPlayerIsMoving() {
        return whichPlayerIsMoving;
    }

    public int[] getStartPos() {
        return startPos;
    }

    public int[] getDestinationPos() {
        return destinationPos;
    }

    public int getWhatIsPickedUp() {
        return whatIsPickedUp;
    }

    @Override
    public String toString() {
        return "MoveDescription{" +
                "whichPlayerIsMoving=" + whichPlayerIsMoving +
                ", startPos=" + Arrays.toString(startPos) +
                ", destinationPos=" + Arrays.toString(destinationPos) +
                ", whatIsPickedUp=" + whatIsPickedUp +
                '}';
    }
}
