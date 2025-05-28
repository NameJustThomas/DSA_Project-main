package minesweeper.Main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * The main Game class that specifies the frame and widgets of the GUI
 * 
 *
 */
public class Game extends JPanel implements Runnable {

	private static final long serialVersionUID = -925518549680460124L;

	@Override
	public void run() {
		
		// Top-level frame in which game components live
		JFrame frame = new JFrame("Minesweeper");
		frame.setLocation(100, 100);
		
		int currentTime = 0;
		final JLabel timerLabel = new JLabel("Current Time (sec): " + currentTime);
		
		int flagsRemaining = 30;
		final JLabel flagLabel = new JLabel("Flags Remaining: " + flagsRemaining);
		
		//Status panel
		final JPanel status_panel = new JPanel();
		frame.add(status_panel, BorderLayout.SOUTH);
		final JLabel status = new JLabel("Running...");
		status_panel.add(status);
		
		//Main playing area
		final GameCourt court = new GameCourt(status, timerLabel, flagLabel);
		final JPanel myBoard = court.makeAndAddBoard();
		frame.add(myBoard, BorderLayout.CENTER);
		
		//Control panel
		final JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		menuBar.setLayout(new GridLayout(1, 4, 5, 3));
		JMenu file = new JMenu("File");
		menuBar.add(file);
        
        //Button allowing user to reset game and start over
        final JButton reset = new JButton("Reset Game");
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	court.reset();
            }
        });
        menuBar.add(reset);
        
        //Button that allows user to import text file of game
        final JMenuItem openGame = new JMenuItem("Open");
        openGame.setIconTextGap(33);
        openGame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	court.importGameStateMediate();
            }
        });
        file.add(openGame);
        
        //Button that allows user to export text file of game
        final JMenuItem save = new JMenuItem("Save");
        save.setIconTextGap(33);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	court.exportGameState();
            }
        });
        file.add(save);
        
        final JButton undo = new JButton("Undo");
        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	court.undo();
            }
        });
        menuBar.add(undo);
        
        //Label showing the current time spent on the puzzle
        menuBar.add(timerLabel);
        
      //Label showing current number of flags remaining
        menuBar.add(flagLabel);
		
        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 750);
        frame.setVisible(true);
        
        //Game on! >=)
        court.reset();
	}
	
	/**
     * Main method run to start and run the game. Initializes the GUI elements specified in Game and
     * runs it. IMPORTANT: Do NOT delete! You MUST include this in your final submission.
     */
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}
