import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameFrame extends JFrame implements ActionListener
{
    private final GamePanel mainPanel;

    private JButton upArrowButton, downArrowButton, leftArrowButton, rightArrowButton;
    private JButton resetButton;
    private JLabel humanScoreLabel, computerScoreLabel, indicatorLabel;

    public GameFrame()
    {
        super("Gobble up!");
        setSize(2*GamePanel.LEFT_MARGIN+GamePanel.NUM_ROWS_AND_COLS*GamePanel.CELL_SIZE+100,
                2*GamePanel.TOP_MARGIN+GamePanel.NUM_ROWS_AND_COLS*GamePanel.CELL_SIZE+150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createControls(), BorderLayout.NORTH);
        setupListeners();
        mainPanel = new GamePanel(this);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

    }

    public Box createControls()
    {
        Box result = Box.createHorizontalBox();
        JPanel ArrowButtonPanel = new JPanel(new GridLayout(3,3));
        upArrowButton = new JButton("^");
        downArrowButton = new JButton("v");
        leftArrowButton = new JButton("<");
        rightArrowButton = new JButton(">");
        ArrowButtonPanel.add(new JLabel(" "));
        ArrowButtonPanel.add(upArrowButton);
        ArrowButtonPanel.add(new JLabel(" "));
        ArrowButtonPanel.add(leftArrowButton, BorderLayout.WEST);
        indicatorLabel = new JLabel("Player",JLabel.CENTER);
        ArrowButtonPanel.add(indicatorLabel);
        indicatorLabel.setForeground(Color.BLUE);
        ArrowButtonPanel.add(rightArrowButton, BorderLayout.EAST);
        ArrowButtonPanel.add(new JLabel(" "));
        ArrowButtonPanel.add(downArrowButton, BorderLayout.SOUTH);
        ArrowButtonPanel.add(new JLabel(" "), BorderLayout.CENTER);
        result.add(ArrowButtonPanel);
        resetButton = new JButton("Reset");
        result.add(resetButton);
        humanScoreLabel = new JLabel ("0");
        humanScoreLabel.setForeground(Color.BLUE);
        result.add(humanScoreLabel);
        result.add(new JLabel("-"));
        computerScoreLabel = new JLabel ("0");
        computerScoreLabel.setForeground(Color.RED);
        result.add(computerScoreLabel);
        result.add(Box.createHorizontalStrut(10));

        return result;
    }

    public void setupListeners()
    {
        upArrowButton.addActionListener(this);
        downArrowButton.addActionListener(this);
        leftArrowButton.addActionListener(this);
        rightArrowButton.addActionListener(this);
        resetButton.addActionListener(this);
    }
    @Override
    public void actionPerformed(ActionEvent aEvt)
    {
        if (aEvt.getSource() == resetButton)
            mainPanel.reset();
        if (aEvt.getSource() == upArrowButton)
            mainPanel.moveHumanBy(-1,0);
        if (aEvt.getSource() == downArrowButton)
            mainPanel.moveHumanBy(+1,0);
        if (aEvt.getSource() == leftArrowButton)
            mainPanel.moveHumanBy(0,-1);
        if (aEvt.getSource() == rightArrowButton)
            mainPanel.moveHumanBy(0,+1);
    }

    /**
     * receives an integer with various bits intended to indicate whether the arrow buttons should be activated. We are
     * doing bitwise AND statements (single &) and comparing the result with zero to get a boolean, which we set as the
     * enabled state of the corresponding button.
     * @param flags - an integer, the lowest four bits of which correspond to the requested states for the Up, Down,
     *              Left, Right buttons, respectively.
     */
    public void activateArrows(int flags)
    {
        upArrowButton.setEnabled((flags & GamePanel.UP_FLAG) != 0);
        downArrowButton.setEnabled((flags & GamePanel.DOWN_FLAG) != 0);
        leftArrowButton.setEnabled((flags & GamePanel.LEFT_FLAG) != 0);
        rightArrowButton.setEnabled((flags & GamePanel.RIGHT_FLAG) != 0);
        repaint();
    }

    /**
     * updates the numbers shown in the scores label
     * @param scores - a two element array of (human score, computer score)
     */
    public void updateScores(int[] scores)
    {
        humanScoreLabel.setText(""+scores[0]);
        computerScoreLabel.setText(""+scores[1]);
        repaint();
    }

    /**
     * updates the indicator for whose turn it is, or game over.
     * @param whoseTurn - 0 = Human; 1 = Computer; anything else is game over.
     */
    public void updateIndicator(int whoseTurn)
    {
        if (whoseTurn==GamePanel.HUMAN)
        {
            indicatorLabel.setText("Human");
            indicatorLabel.setForeground(Color.BLUE);
        }
        else if (whoseTurn==GamePanel.COMPUTER)
        {
            indicatorLabel.setText("Computer");
            indicatorLabel.setForeground(Color.RED);
        }
        else
        {
            indicatorLabel.setText("Game Over.");
            indicatorLabel.setForeground(Color.BLACK);
        }
    }
}
