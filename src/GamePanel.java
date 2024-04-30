import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;

public class GamePanel extends JPanel
{
    // constants (thus the "final") ...................................................
    public static final int NUM_ROWS_AND_COLS = 6;  // num boxes in grid
    public static final int CELL_SIZE = 60;  // pixels per box
    public static final int LEFT_MARGIN = 20;
    public static final int TOP_MARGIN = 20;
    // the following "flags" are used to send information to the Frame about which arrow buttons to activate.
    public static final int UP_FLAG = 1;
    public static final int DOWN_FLAG = 2;
    public static final int LEFT_FLAG = 4;
    public static final int RIGHT_FLAG = 8;
    // the changes to a row, col to go up, left, down, right, respectively.
    public static final int[][] DELTAS = {{-1,0},{0,-1},{+1,0},{0,+1}};

    // These are being used a LOT... for whose turn it is, which score goes up, which position to change, etc.
    public static final int HUMAN = 0;
    public static final int COMPUTER = 1;
    // How many steps ahead is the computer looking to try to find the best course of action?
    public static final int MAX_STEPS_TO_LOOK = 5;
    // the font we'll be using to draw the numbers.
    private final Font myFont = new Font("Arial", Font.BOLD, CELL_SIZE-10);
    private final Image robotIcon, humanIcon;
    // .......................................................................................
    // Class variables -----------------------------------------------------------------------
    private int[][] myGrid; // the numbers we're storing.
    // this is a link to the Frame this panel lives in... so that we can tell it about changes
    // to the button and status indicators.
    private final GameFrame parent;
    private final int[][] playerPositions;  // (HUMAN, COMPUTER) x (row, col)
    private int[] scores;  // (HUMAN, COMPUTER)

    // used to create a little delay before the computer starts thinking, so that we can see the player's move and turn
    // indicator before it freezes up to think.
    private final java.util.Timer computerTurnTimer;

    public GamePanel(GameFrame p)
    {
        super();
        parent = p;
        playerPositions = new int[2][2];
        computerTurnTimer = new java.util.Timer();
        humanIcon = (new ImageIcon("HumanIcon.png")).getImage();// https://www.freepik.com/icon/user_6107173
        robotIcon = (new ImageIcon("RobotIcon.png")).getImage(); // Source: https://www.freepik.com/icon/robot_8006396
        reset();
    }

    /**
     * randomizes the numbers on the field, places the players in their start positions, resets the scores to zero,
     * sets the HUMAN to move first, and updates the parent screen of any changes it needs to indicate.
     */
    public void reset() {
        myGrid = new int[NUM_ROWS_AND_COLS][NUM_ROWS_AND_COLS];
        for (int i = 1; i < NUM_ROWS_AND_COLS*NUM_ROWS_AND_COLS-1; i++)
            myGrid[i/NUM_ROWS_AND_COLS][i%NUM_ROWS_AND_COLS] = (int)(Math.random()*90+10);
        playerPositions[HUMAN][0] = 0;
        playerPositions[HUMAN][1] = 0;
        playerPositions[COMPUTER][0] = NUM_ROWS_AND_COLS-1;
        playerPositions[COMPUTER][1] = NUM_ROWS_AND_COLS-1;
        parent.activateArrows(checkForPossibleHumanMoves());
        repaint();
        parent.updateIndicator(HUMAN);
        scores = new int[2];
        parent.updateScores(scores);
    }

    /**
     * draws the grid and player avatars.
     * @param g the <code>Graphics</code> object to protect
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        for (int r = 0; r< NUM_ROWS_AND_COLS; r++)
            for (int c=0; c<NUM_ROWS_AND_COLS; c++) {
                // fill the alternating boxes
                if ((r + c) % 2 == 0)
                    g.setColor(Color.LIGHT_GRAY);
                else
                    g.setColor(Color.DARK_GRAY);
                g.fillRect(LEFT_MARGIN + CELL_SIZE * c, TOP_MARGIN + CELL_SIZE * r, CELL_SIZE, CELL_SIZE);
                // draw the box outlines
                g.setColor(Color.BLACK);
                g.drawRect(LEFT_MARGIN + CELL_SIZE * c, TOP_MARGIN + CELL_SIZE * r, CELL_SIZE, CELL_SIZE);
                // draw numbers
                if (myGrid[r][c] > 0) {
                    if ((r + c) % 2 == 0)
                        g.setColor(new Color(0,200, 0));
                    else
                        g.setColor(new Color(0, 96, 0));
                    g.fillRect(LEFT_MARGIN + c * CELL_SIZE + 2, TOP_MARGIN + r * CELL_SIZE + 2,
                            CELL_SIZE-4, CELL_SIZE-4);
                    if ((r + c) % 2 == 0)
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.DARK_GRAY);
                    int size = (CELL_SIZE-4)*(100-myGrid[r][c])/100;
                    g.fillRect(LEFT_MARGIN + c * CELL_SIZE + CELL_SIZE/2 - size/2,
                               TOP_MARGIN + r * CELL_SIZE + CELL_SIZE/2 - size/2,
                            size, size);
                    g.setColor(Color.GRAY);
                    g.setFont(myFont);
                    int width = g.getFontMetrics(myFont).stringWidth("" + myGrid[r][c]);
                    g.drawString("" + myGrid[r][c],
                            LEFT_MARGIN + c * CELL_SIZE + (CELL_SIZE - width) / 2,
                            TOP_MARGIN + (r + 1) * CELL_SIZE - 10);
                }
            }
            // draw human avatar
        g.drawImage(humanIcon,LEFT_MARGIN+playerPositions[HUMAN][1]*CELL_SIZE+5,
                TOP_MARGIN+playerPositions[HUMAN][0]*CELL_SIZE+5, this);
//            g.setColor(Color.BLUE);
//            g.fillOval(LEFT_MARGIN+playerPositions[HUMAN][1]*CELL_SIZE+5,
//                    TOP_MARGIN+playerPositions[HUMAN][0]*CELL_SIZE+5,
//                    CELL_SIZE-10, CELL_SIZE-10);
            // draw computer avatar
//            g.setColor(Color.RED);
//            g.fillOval(LEFT_MARGIN+playerPositions[COMPUTER][1]*CELL_SIZE+5,
//                    TOP_MARGIN+playerPositions[COMPUTER][0]*CELL_SIZE+5,
//                    CELL_SIZE-10, CELL_SIZE-10);
            g.drawImage(robotIcon,LEFT_MARGIN+playerPositions[COMPUTER][1]*CELL_SIZE+5,
                    TOP_MARGIN+playerPositions[COMPUTER][0]*CELL_SIZE+5, this);

    }

    /**
     * determines which direction(s) the player is allowed to move next turn. (This is used to deactivate the arrow
     * buttons on the main screen for any that aren't legal.)
     * This makes use of the constants:
     * UP_FLAG = 1
     * DOWN_FLAG = 2
     * LEFT_FLAG = 4
     * RIGHT_FLAG = 8
     * @return the sum of the flags for which the player may move.
     */
    public int checkForPossibleHumanMoves()
    {
        int result = 0;
        int humanRow = playerPositions[HUMAN][0];
        int humanCol = playerPositions[HUMAN][1];
        int computerRow = playerPositions[COMPUTER][0];
        int computerCol = playerPositions[COMPUTER][1];

        if (humanRow>0 && (humanCol != computerCol || humanRow-1 != computerRow))
            result+= UP_FLAG;

        if (humanCol>0 && (humanRow != computerRow || humanCol-1 != computerCol))
            result+= LEFT_FLAG;

        if (humanRow<NUM_ROWS_AND_COLS-1 && (humanCol != computerCol || humanRow+1 != computerRow))
            result+= DOWN_FLAG;

        if (humanCol<NUM_ROWS_AND_COLS-1 && (humanRow != computerRow || humanCol+1 != computerCol))
            result+= RIGHT_FLAG;

        return result;
    }

    /**
     * follows the instructions in the MoveDescription to change the given player's position, "gobble" up any non-zero
     * number in the destination grid, and update the score.
     * @param move - a description of what to do.
     */
    public void makeMove(MoveDescription move)
    {
        scores[move.getWhichPlayerIsMoving()]+=move.getWhatIsPickedUp();
        playerPositions[move.getWhichPlayerIsMoving()] = move.getDestinationPos();
        myGrid[move.getDestinationPos()[0]][move.getDestinationPos()[1]] = 0;
        changeRewardsBy(-1);
    }

    /**
     * reverses the instructions in the MoveDescription to change the given player's position back, "regurgitate"
     * whatever number was captured (and return it to the board), and update the score after points are removed.
     * @param move - a description of what to undo.
     */
    public void undoMove(MoveDescription move)
    {
        scores[move.getWhichPlayerIsMoving()]-=move.getWhatIsPickedUp();
        playerPositions[move.getWhichPlayerIsMoving()] = move.getStartPos();
        changeRewardsBy(+1);
        myGrid[move.getDestinationPos()[0]][move.getDestinationPos()[1]] = move.getWhatIsPickedUp();
    }

    public void changeRewardsBy(int n)
    {
        for (int r=0; r< myGrid.length; r++)
            for (int c=0; c<myGrid[0].length; c++)
                if (myGrid[r][c] != 0)
                    myGrid[r][c] += n;
    }

    /**
     * The human has pressed one of the enabled buttons, and has requested that we change the position of the player.
     * (i.e., to make a move.)
     * Generates a move description to correspond to this request and sends it to makeMove(). Also freezes the button,
     * updates any indicators and tells the Timer to call computersTurn() in 0.1 seconds from now.
     * @param deltaR - the requested change in the player's row
     * @param deltaC - the requested change in the player's column
     *
     */
    public void moveHumanBy(int deltaR, int deltaC)
    {
        int[] destination = new int[2];
        destination[0]=playerPositions[HUMAN][0] + deltaR;
        destination[1]=playerPositions[HUMAN][1] + deltaC;
        MoveDescription move = new MoveDescription(HUMAN,playerPositions[HUMAN],destination,
                myGrid[destination[0]][destination[1]]);
        myGrid[destination[0]][destination[1]]=0;
        makeMove(move);
        parent.updateScores(scores);
        parent.activateArrows(0);
        if (isGameOver())
        {
            parent.updateIndicator(-1);
            repaint();
            return;
        }
        parent.updateIndicator(COMPUTER);
        repaint();
        // this is a complicated compound statement that you don't need to know/understand, but it means to run
        // computersTurn() in a separate "thread" in 100 milliseconds from now.
        computerTurnTimer.schedule(new TimerTask() {@Override public void run(){computersTurn();}}, 100);
    }

    /**
     * Pick the best move for the computer. Make that move, update the scoreboard and indicators and activate the arrow
     * buttons for the human player to indicate that it's the human's turn.
     */
    public void computersTurn()
    {
        MoveDescription computersBestMove = bestMoveForComputer();
        makeMove(computersBestMove);
        parent.updateScores(scores);
        if (isGameOver())
        {
            parent.updateIndicator(-1);
            repaint();
        }
        parent.activateArrows(checkForPossibleHumanMoves());
        parent.updateIndicator(HUMAN);
        repaint();
    }

    /**
     * consider all the moves (N,S,E,W) that the computer might make this turn. For each one, determine its
     * "score rating" (i.e. how much higher is the computer's score than the human's?) Pick the move that has the
     * highest rating, and return it.
     * @return the moveDescription that represents the optimal move for the computer
     */
    public MoveDescription bestMoveForComputer()
    {
        ArrayList<MoveDescription> options = getPossibleMovesForPlayer(COMPUTER);
        int bestRanking = -99999;
        MoveDescription bestMove = options.get(0);
        for (MoveDescription move :options)
        {
            int ranking = getLowestScoreRankingForHumanMove(move, MAX_STEPS_TO_LOOK);
            if (ranking>bestRanking)
            {
                bestRanking = ranking;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * Temporarily make the human's move that was given.
     * *
     * If the number of steps left to look is zero, then find the score ranking (how much more the computer has than
     * the player), undo the move, and return that ranking.
     * *
     * Otherwise, consider all the moves that the computer might make, and find their scores ranking (see next
     * paragraph). Identify the highest score ranking -- i.e. the best value if the computer moves wisely.
     * *
     * But those rankings are determined by the best move that the human can make in response.... so we get them
     * from calling the BestHumanMove in response to each of the potential Computer moves... but since we've just looked
     * at a step, that should impact the numStepsLeftToLook that we send to that method.
     * *
     * Then undo the temporary move and return this highest ranking.
     *
     * @param move - a description of a move that the human might make.
     * @param numStepsLeftToLook - how many more steps should we look ahead?
     * @return the best score that we think the computer can get.
     */
    public int getHighestScoreRankingForComputerMove(MoveDescription move, int numStepsLeftToLook)
    {
        // since this can get a little confusing, I'm making sure that you get the right kind of move here.
        if(HUMAN != move.getWhichPlayerIsMoving())
            throw new RuntimeException("Hey, the given move should be the human moving, then we're looking for a the" +
                    " best computer move.\n"+move);

        int scoreRanking = 0;
        makeMove(move);
        // TODO: Consider base case


        // TODO: find all the possible MoveDescriptions that the computer might select. (Hint: there's a method for
        //  this.)
        //  Then loop through those options and call this method's twin, giving them one of the computer's moves, and
        //  make sure that you are getting one step closer to the base case. Each time you do, you'll get a number.
        //  You want to set scoreRanking to the largest of these numbers.


        undoMove(move);
        return scoreRanking;
    }

    /**
     * Temporarily make the computer's move that is given.
     * *
     * If the number of steps left to look is zero, then find the score ranking (how much more the computer has than
     * the player), undo the move, and return that ranking.
     * *
     * Otherwise, consider all the moves that the human might make, and consider their score ranking (see next
     * paragraph). Since we assume that the human is smart (????), we'll guess that the human is going to pick the one
     * with the lowest score ranking for the computer (i.e., the computer's score - human's score). So pick the lowest
     * ranking.
     * *
     * But those rankings are determined by the best move that the computer can make in response.... so we get them
     * from calling the BestComputerMove in response to each of the potential Human moves... but since we've just looked
     * at a step, that should impact the numStepsLeftToLook that we send to that method.
     * *
     * Then undo the temporary move and return this lowest ranking.
     *
     * @param move a description of a move that the computer might make.
     * @param numStepsLeftToLook - how many more steps should we look ahead?
     * @return - the best score we think that the computer can get... given that the human is trying to make it low.
     */
    public int getLowestScoreRankingForHumanMove(MoveDescription move, int numStepsLeftToLook)
    {
        // since this can get a little confusing, I'm making sure that you get the right kind of move here.
        if(COMPUTER != move.getWhichPlayerIsMoving())
            throw new RuntimeException("Hey, the given move should be the computer moving, then we're looking for a the" +
                    " best human move.\n"+move);

        int scoreRanking = 0;
        makeMove(move);

        // TODO: consider the base case for this method


        // TODO: find all the possible MoveDescriptions that the human might select. (Hint: there's a method for
        //  this.)
        //  Then loop through those options and call this method's twin, giving them one of the human's moves, and
        //  make sure that you are getting one step closer to the base case. Each time you do, you'll get a number.
        //  You want to set scoreRanking to the smallest of these numbers.



        undoMove(move);
        return scoreRanking;
    }

    /**
     * determines whether the given player is at the given location (i.e., blocking the other player)
     * @param whichPlayer - the player whose position we are wondering about
     * @param pos - the location (row, col) under consideration
     * @return - whether that player (human or computer) is in the given location
     */
    public boolean isPlayerInPosition(int whichPlayer, int[] pos)
    {

        return Arrays.equals(playerPositions[whichPlayer],pos);

    }

    /**
     * is the given position one within the bounds of the game?
     * @param position - a (row, col) to consider
     * @return - whether it is in bounds.
     */
    public boolean isInBounds(int[] position)
    {
        return position[0]>-1 && position[1] >-1 && position[0] < NUM_ROWS_AND_COLS && position[1] < NUM_ROWS_AND_COLS;
    }

    /**
     * get a list of legal moves that the given player can make (in random order)
     * @param whichPlayer - the player (HUMAN or COMPUTER) that we are considering
     * @return - a List of MoveDescriptions, each one describing a legal move.
     */
    public ArrayList<MoveDescription> getPossibleMovesForPlayer(int whichPlayer)
    {
        ArrayList<MoveDescription> options = new ArrayList<>();
        for (int[] del: DELTAS)
        {
            int[] candidatePosition = new int[2];
            candidatePosition[0] = playerPositions[whichPlayer][0]+del[0];
            candidatePosition[1] = playerPositions[whichPlayer][1]+del[1];

            if (isInBounds(candidatePosition) && !isPlayerInPosition(1-whichPlayer,candidatePosition))
            {
                options.add( new MoveDescription(whichPlayer,playerPositions[whichPlayer],candidatePosition,
                        myGrid[candidatePosition[0]][candidatePosition[1]]));
            }
        }
        // shuffle the list by randomly removing items and putting them at the end of the list.
        for (int i=0; i<8; i++)
        {
            options.add(options.remove((int)(Math.random()*options.size())));
        }
        return options;
    }

    /**
     * determines whether all the numbers have been "gobbled up."
     * @return - whether all the values in myGrid are zero
     */
    public boolean isGameOver()
    {
        for (int[] row: myGrid)
            for (int cell: row)
                if (cell != 0)
                    return false;
        return true;
    }

}
