package minesweeper.Main;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.*;

/**
 * This class holds the primary game logic for how the different buttons, cells, and 
 * bombs interact with one another. As you will see, most of the functions depend and
 * rely closely on each other.
 * 
 */
public class GameCourt extends JPanel {

	private static final long serialVersionUID = 1L;

	//Depends on the locations of the user's project
	private static final String DEFAULT_PATH = "";
	private static String path = "";

	private static final String VISIBLE = "V";
	private static final String FLAGGED = "F";
	private static final String HIDDEN = "H";

	private Board board;
	private JButton[][] cellButtons;
	private JLabel status;
	private JLabel timerLabel;
	private JLabel flagLabel;

	/*
	 * Stores positions of flagged cells (16 * row + column)
	 * row = value / 16, column = value % 16
	 */
	private ArrayList<Integer> flaggedCells = new ArrayList<Integer>();
	private int currentTime = 0;
	private int flagsRemaining = 30;
	private boolean gameInPlay = false;
	//nonBombCellsRemaining = rows * columns - totalNumberOfBombs = 16*16-30
	private int nonBombCellsRemaining = 226;
	private LinkedList<Move> moveHistory = new LinkedList<Move>();


	/**
	 * Constructs the GameCourt and sets the statuses accordingly. Game on!
	 * 
	 * @param gameStatus the status at the bottom of the window, updates the state of the game (W/L)
	 * @param timeStatus the status at the top right, updates the elapsed time
	 * @param flagStatus the status at the top left, updates the remaining flags
	 */
	public GameCourt(JLabel gameStatus, JLabel timeStatus, JLabel flagStatus) {
		/*
		 * Timer that updates the label every second
		 */
		Timer timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tick();
			}
		});
		timer.start();

		gameInPlay = true;

		this.status = gameStatus;
		this.timerLabel = timeStatus;
		this.flagLabel = flagStatus;
		repaint();
	}

	/**
	 * Generates the initial board and 256 buttons. The board is created with random
	 * cells, bombs, and numbers. Each button is assigned to a position in the grid
	 * and correlates with positions in the board.
	 * Listeners (action listeners for clicks, mouse listeners for right-clicks) are
	 * initialized and implemented.
	 * 
	 * @return JPanel depicting the board/grid layout
	 */
	public JPanel makeAndAddBoard() {
		board = new Board();

		JPanel myBoard = new JPanel(new GridLayout(16, 16));
		cellButtons = new JButton[16][16];

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				cellButtons[i][j] = new JButton();
				cellButtons[i][j].setPreferredSize(new Dimension(100, 100));
				cellButtons[i][j].setVisible(true);

				myBoard.add(cellButtons[i][j]);

				// Registers "clicks" on the cell
				cellButtons[i][j].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (int i = 0; i < 16; i++) {
							for (int j = 0; j < 16; j++) {

								//Only register a click if found correct button, cell is unflagged,
								//cell is not already uncovered, and game is still going on
								if (cellButtons[i][j] == e.getSource() && 
										!board.getFlaggedOfCell(i, j) &&
										!board.getVisibilityOfCell(i, j) && gameInPlay) {
									showClickedCell(i,j);
								}

							}
						}
					}
				});

				// Registers "right-clicks" on the cell (for flags)
				cellButtons[i][j].addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						for (int i = 0; i < 16; i++) {
							for (int j = 0; j < 16; j++) {

								//Only register a right-click if found correct button, cell is
								//not already uncovered, and game is still going on
								if (cellButtons[i][j] == e.getSource() && gameInPlay && 
										!board.getVisibilityOfCell(i, j)) {
									flagCell(i,j);
								}
							}
						}
					}

					@Override
					public void mousePressed(MouseEvent e) {
					}

					@Override
					public void mouseReleased(MouseEvent e) {
					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}
				});

			}
		}
		return myBoard;
	} 

	/**
	 * Called upon click of a cell. This method is only entered for the very first cell
	 * (not the byproducts). This was created after initial draft to ensure easier 
	 * testing.
	 * @param x the x coordinate of the cell to show
	 * @param y the y coordinate of the cell to show
	 */
	public Icon getIcon(int index) {
		int w = 50;
		int h = 50;
		Image image = new ImageIcon(getClass().getResource("/icons/" + index + ".png")).getImage();
		Icon ic = new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
		return ic;
	}
	
	public void showClickedCell(int x, int y) {
		showCell(x, y);
		addMoveToHistory(x, y, 0);
	}
	
	/**
	 * Uncover the value of a given cell. Adjusts depending on the value of the cell
	 * Basic rundown:
	 * 	- If bomb, lose game
	 *  - If 0, show nearby cells
	 *  - Otherwise, show numerical value
	 *  
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @throws IllegalArgumentException if given arguments do not correspond with valid cell
	 */
	public void showCell(int x, int y) {
		if (outOfBoundsCheck(x, y)) {
			throw new IllegalArgumentException();
		}
		//Can only show cell if cell is hidden, unflagged, and game is still going on
		if (!board.getVisibilityOfCell(x, y) && !board.getFlaggedOfCell(x, y) && gameInPlay) {
			if (board.getNumOfCell(x, y) == 9) {
				loseGame();
			}
			else {
				if (board.getNumOfCell(x, y) == 0) {
					showMultipleCells(x, y);
				}
				else {
					cellButtons[x][y].setIcon(getIcon(board.getNumOfCell(x, y)));
					board.changeVisibilityOfCell(x, y, true);
					nonBombCellsRemaining--;
				}
				if (checkWinGame()) {
					winGame();
				}
			}
		}
	}
	

	/**
	 * Uncover the values of multiple nearby cells (because current cell is 0, 
	 * so all surrounding cells are non-bomb)
	 * Invariant: arguments will always lead to valid cell since this function
	 * is only called in showClickedCell (above), which checks for outOfBounds
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void showMultipleCells(int x, int y) {
		if (!board.getVisibilityOfCell(x, y)) {
			board.changeVisibilityOfCell(x, y, true);
			cellButtons[x][y].setIcon(getIcon(board.getNumOfCell(x, y)));
			nonBombCellsRemaining--;
			repaint();

			//iterates through all 8 adjacent cells
			for (int i = x-1; i <= x+1; i++) {
				for (int j = y-1; j <= y+1; j++) {
					if ((i != x || j != y) && i >= 0 && j >= 0 && i < 16 && j < 16) {
						//Recursive call to reveal nearby cells of empty cell
						if (board.getNumOfCell(i, j) == 0 && !board.getVisibilityOfCell(i, j)) {
							showMultipleCells(i, j);
						}
						else if (!board.getVisibilityOfCell(i, j)) {
							showCell(i, j);
						}
					}
				}
			}
		}
	}

	/**
	 * Cover the value of a given cell. Used when trying to undo a recent move.
	 * Adjusts depending on the value of the cell (if 0, must adjust for neighbors)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @throws IllegalArgumentException if given arguments do not correspond with valid cell
	 */
	public void hideClickedCell(int x, int y) {
		if (outOfBoundsCheck(x, y)) {
			throw new IllegalArgumentException();
		}
		if (board.getVisibilityOfCell(x, y) && gameInPlay) {
			board.changeVisibilityOfCell(x, y, false);
			if (board.getNumOfCell(x, y) == 0) {
				hideMultipleCells(x, y);
				nonBombCellsRemaining++;

			}
			else {
				cellButtons[x][y].setIcon(getIcon(333));
				nonBombCellsRemaining++;
			}
		}
		repaint();
	}

	/**
	 * Cover the values of multiple nearby cells (because current cell is 0, 
	 * so all surrounding cells are non-bomb and were revealed earlier)
	 * Invariant: arguments will always lead to valid cell since this function
	 * is only called in hideClickedCell (above), which checks for outOfBounds
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void hideMultipleCells(int x, int y) {
		if (board.getVisibilityOfCell(x, y)) {
			board.changeVisibilityOfCell(x, y, false);
			cellButtons[x][y].setIcon(getIcon(333));
			nonBombCellsRemaining++;

			//iterates through all 8 adjacent cells
			for (int i = x-1; i <= x+1; i++) {
				for (int j = y-1; j <= y+1; j++) {
					if ((i != x || j != y) && i >= 0 && j >= 0 && i < 16 && j < 16) {
						//Recursive call to hide nearby cells of empty cell
						if (board.getNumOfCell(i, j) == 0 && board.getVisibilityOfCell(i, j)) {
							hideMultipleCells(i, j);
						}
						else if (board.getVisibilityOfCell(i, j)) {
							hideClickedCell(i, j);
						}
					}
				}
			}
		}
		repaint();
	}

	/**
	 * Reveals all the bombs in the game. Occurs when user clicks on bomb and loses the game
	 */
	public void revealAllBombs() {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				if (board.getNumOfCell(i, j) == 9) {
					cellButtons[i][j].setIcon(getIcon(666));
					board.changeVisibilityOfCell(i, j, true);
				}
			}
		}
		repaint();
	}

	/**
	 * Hides all the bombs in the game. Occurs when the user undo's a recent loss.
	 */
	public void hideAllBombs() {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				if (board.getNumOfCell(i, j) == 9) {
					cellButtons[i][j].setIcon(getIcon(333));
					board.changeVisibilityOfCell(i, j, false);
				}
			}
		}
		repaint();
	}
	
	/**
	 * This function will flag/unflag a cell of the board.
	 * @param i the x coordinate of the cell to flag
	 * @param j the y coordinate of the cell to flag
	 */
	public void flagCell(int i, int j) {
		if (board.getFlaggedOfCell(i, j)) {
			cellButtons[i][j].setIcon(getIcon(333));
			flaggedCells.remove(Integer.valueOf(16 * i + j));
			addMoveToHistory(i, j, 2);
		}
		else {
			cellButtons[i][j].setIcon(getIcon(24));
			flaggedCells.add(16 * i + j);
			addMoveToHistory(i, j, 1);
		}
		board.changeFlaggedOfCell(i, j, !board.getFlaggedOfCell(i, j));
		flagLabel.setText("Flags Remaining: "+
				(flagsRemaining-flaggedCells.size()));
		repaint();
	} 

	/**
	 * Simple helper function to adjust the game status and other small facets upon loss
	 */
	public void loseGame() {
		revealAllBombs();
		status.setText("You lose!");
		gameInPlay = false;
	}

	/**
	 * Simple helper function to check if user has uncovered all nonBomb cells (a win!)
	 * @return
	 */
	public boolean checkWinGame() {
		return nonBombCellsRemaining <= 0;
	}

	/**
	 * Simple helper function to adjust the game status and other small facets upon win
	 */
	public void winGame() {
		status.setText("You win!");
		gameInPlay = false;
	}

	/**
	 * Default reset. Called when user wants to play a new, randomized game.
	 * Occurs when user presses "reset" button.
	 * Updates all game statuses and counters
	 */
	public void reset() {
		gameInPlay = true;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				cellButtons[i][j].setIcon(getIcon(333));
				board.changeVisibilityOfCell(i, j, false);
				board.changeFlaggedOfCell(i, j, false);

			}
		}

		board = new Board();
		moveHistory = new LinkedList<Move>();
		currentTime = 0;
		nonBombCellsRemaining = 16*16-30;
		flagsRemaining = 30;
		flaggedCells = new ArrayList<Integer>();

		status.setText("Running...");
		flagLabel.setText("Flags Remaining: "+flagsRemaining);
		timerLabel.setText("Current Time (sec): "+Integer.toString(currentTime));
		repaint();
	}

	/**
	 * Overloaded reset. Called when user wants to play an imported game.
	 * Occurs when user imports a game using the "import" button.
	 * Updates all game statuses and counters based on those found in the import file
	 * @param ss the numerical board representation, contains the values (0-9) of all 256 cells 
	 * @param vs the visibility board representation; V = visible, F = flagged, H = hidden
	 * @param t
	 */
	public void resetWithGivenState(String ss, String vs, String t) {
		gameInPlay = true;
		board = new Board(ss);
		moveHistory = new LinkedList<Move>();
		flaggedCells = new ArrayList<Integer>();
		currentTime = Integer.parseInt(t);
		nonBombCellsRemaining = 16*16-30;
		flagsRemaining = 30;
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				//Each cell corresponds to a specific position in the string
				String current = vs.substring(16 * i + j, 16 * i + j + 1);
				if (current.equals(VISIBLE)) {
					//If a bomb is visible, game must have been lost. Update statuses accordingly
					if (board.getNumOfCell(i, j) == 9) {
						cellButtons[i][j].setIcon(getIcon(666));
						gameInPlay = false;
						status.setText("You lose!");
					}
					else {
						cellButtons[i][j].setIcon(getIcon(board.getNumOfCell(i, j)));
						nonBombCellsRemaining--;
					}
					board.changeVisibilityOfCell(i, j, true);
					board.changeFlaggedOfCell(i, j, false);
				}
				else if (current.equals(FLAGGED)) {
					cellButtons[i][j].setIcon(getIcon(24));
					board.changeVisibilityOfCell(i, j, false);
					board.changeFlaggedOfCell(i, j, true);
					flaggedCells.add(16 * i + j);

				}
				else {
					cellButtons[i][j].setIcon(getIcon(333));
					board.changeVisibilityOfCell(i, j, false);
					board.changeFlaggedOfCell(i, j, false);
				}
			}
		}

		//Update statuses accordingly
		if (checkWinGame()) {
			winGame();
		}
		else if (gameInPlay == false) {
			status.setText("You lose!");
		}
		else {
			status.setText("Running...");
		}

		timerLabel.setText("Current Time (sec): "+Integer.toString(currentTime));
		flagLabel.setText("Flags Remaining: "+(flagsRemaining - flaggedCells.size()));
		repaint();
	}

	public void undo() {
		if (moveHistory.size() > 0) {
			Move recentMove = moveHistory.pop();
			int xCoor = recentMove.getxCoor();
			int yCoor = recentMove.getyCoor();

			// User wants to undo a click on cell
			if (recentMove.getMoveType() == 0) {
				// Most recent move caused game to end
				if (!gameInPlay) {
					gameInPlay = true;
					if (status.getText().equals("You lose!")) {
						hideAllBombs();
						for (Integer index : flaggedCells) {
							cellButtons[index / 16][index % 16].setIcon(getIcon(24));
						}
					}
					else {
						cellButtons[xCoor][yCoor].setIcon(getIcon(333));
						board.changeVisibilityOfCell(xCoor, yCoor, false);
					}

					status.setText("Running...");
				}
				else {
					hideClickedCell(xCoor, yCoor);
				}
			}
			//User wants to undo flagging a cell
			else if (recentMove.getMoveType() == 1) {
				cellButtons[xCoor][yCoor].setIcon(getIcon(333));
				board.changeFlaggedOfCell(xCoor, yCoor, false);
				flaggedCells.remove(Integer.valueOf(16 * xCoor + yCoor));
				flagLabel.setText("Flags Remaining: "+(flagsRemaining - flaggedCells.size()));
			}
			//User wants to undo unflagging a cell
			else if (recentMove.getMoveType() == 2) { 
				cellButtons[xCoor][yCoor].setIcon(getIcon(24));
				board.changeFlaggedOfCell(xCoor, yCoor, true);
				flaggedCells.add(16 * xCoor + yCoor);
				flagLabel.setText("Flags Remaining: "+(flagsRemaining - flaggedCells.size()));
			}
			else {
				System.out.println("ERROR: SOMETHING WENT UNACCOUNTED FOR");
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "No more moves to undo!");
		}
		repaint();
	}

	/**
	 * Creates a file choosing menu where users can choose the file and/or directory
	 * @return String version of the targeted path
	 */
	public String chooseFileAndGetPath() {
		JFileChooser choose = new JFileChooser();
		choose.setCurrentDirectory(new java.io.File(DEFAULT_PATH));
		choose.setDialogTitle("Choose File");
		int returnVal = choose.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path = choose.getSelectedFile().getAbsolutePath();
		}
		return path;
	}
	
	/**
	 * The import game function called by the Game.java file and button.
	 * Breaking this up allows for more separation and easier testing.
	 */
	public void importGameStateMediate() {
		importGameState(chooseFileAndGetPath());
	}

	/**
	 * Imports a .txt file of game.
	 * Reads the 2 boards and the timer and calls reset accordingly.
	 * @param filepath the path of the .txt file to import
	 * @return boolean representing whether or not import was successful
	 */
	public boolean importGameState(String filepath) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));

			String visibilityState = "";
			for (int i = 0; i < 16; i++) {
				visibilityState += br.readLine();
			}

			br.readLine();

			String solutionState = "";
			for (int i = 0; i < 16; i++) {
				solutionState += br.readLine();
			}

			br.readLine();

			String time = br.readLine();

			resetWithGivenState(solutionState, visibilityState, time);

			br.close();

			return true;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, "Invalid File!");
		}
		return false;
	}

	/**
	 * Exports the current game into a .txt file and puts file in project folder
	 * The .txt file contains the following:
	 *  - Board representation of the numerical values of each cell
	 *  - Board representation of the visibility of each cell
	 *  - The current time
	 */
	public void exportGameState() {
		JOptionPane.showMessageDialog(null, "File saved to the files folder!");
		writeStringsToFile(getVisibilityBoard());
	}
	
	/**
	 * This function simply translates the current visibility of all 256 cells on the 
	 * board into a String. The String contains 16 rows of 16 columns each, with
	 * a V being visible, F being flagged, and H being hidden.
	 * @return String representation of the visibility of all cells in the board
	 */
	public String getVisibilityBoard() {
		String s = "";

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				if (board.getVisibilityOfCell(i, j)) {
					s += VISIBLE;
				}
				else if (board.getFlaggedOfCell(i, j)) {
					s += FLAGGED;
				}
				else {
					s += HIDDEN;
				}
			}
			s += "\n";
		}
		return s;
	}

	/**
	 * Turns the string representations into files with the file name 
	 * being the unique timestamp. 
	 * @param stringToWrite the String containing 2 boards and current time
	 */
	public void writeStringsToFile(String stringToWrite) {
		BufferedWriter br = null;

		try {
			String timeAndFilePath = "Minesweeper"+
					new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss").format(new Date());
			br = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(timeAndFilePath+".txt"), "utf-8"));
			br.write(stringToWrite + "\n");
			br.write(board.toString() + "\n"+Integer.toString(currentTime));

			br.flush();
			br.close();
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Adds a user's move to the history. Allows easier implementation of "undo"
	 * Legend:
	 * 	0 = User recently revealed a cell
	 *  1 = User recently flagged a cell
	 *  2 = User recently unflagged a cell
	 * 
	 * @param x the x coordinate of the cell
	 * @param y the y coordinate of the cell
	 * @param type the type of move that was done (above)
	 */
	private void addMoveToHistory(int x, int y, int type) {
		moveHistory.addFirst(new Move(x, y, type));
	}

	/**
	 * Simple helper function that checks if given coordinates are valid
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the boolean of whether or not given coordinates is a valid cell
	 */
	private boolean outOfBoundsCheck(int x, int y) {
		return (x < 0 || x > 15 || y < 0 || y > 15);
	}
	
	/*
	 * Getter methods (primarily accessed for testing purposes)
	 */
	
	/**
	 * @return an array containing the move history 
	 */
	public Object[] getMoveHistory() {
		return moveHistory.toArray();
	}
	
	/**
	 * @return an array containing the flagged cell indices 
	 */
	public Object[] getFlaggedCells() {
		return flaggedCells.toArray();
	}
	
	/**
	 * @return an integer representing the number of non bomb cells remaining
	 */
	public int getNonBombCellsRemaining() {
		return nonBombCellsRemaining;
	}
	
	/**
	 * @return a boolean representing the gameInPlay status
	 */
	public boolean getGameInPlay() {
		return gameInPlay;
	}
	
	/**
	 * @return an integer representing the number of flags remaining
	 */
	public int getFlagsRemaining() {
		return flagsRemaining - flaggedCells.size();
	}
	
	/**
	 * @return an integer representing the current time elapsed
	 */
	public int getCurrentTime() {
		return currentTime;
	}
	
	/**
	 * @return a String of the current status of the game;
	 */
	public String getGameStatus() {
		return status.getText();
	}
	
	/**
	 * This method is called every time the timer defined in the constructor triggers.
	 * Here, updates the timer label every second
	 */
	void tick() {
		if (gameInPlay) {
			currentTime++;
			timerLabel.setText("Current Time (sec): "+Integer.toString(currentTime));
		}

		// update the display
		repaint();
	}
}
