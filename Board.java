package minesweeper.Main;

import java.util.Random;

/**
 * This class focuses on consolidating the board and containing the proper
 * numerical values/set conditions. Aside from the standard setters and getters, 
 * the three sections that contain algorithms are the constructor (2nd), addBombs, 
 * and numSurroundingBombs.
 * 
 * The algorithm in determining the position of items is as follows.
 *  - The x coordinate will be: VALUE / 16
 *  - The y coordinate will be: VALUE % 16
 * For example, cell number 23 will be in row 1 (23/16 = 1) and column 7 (23%16 = 7)
 * 
 *  Note: this class was made specifically for this version of Minesweeper. 
 * 		 As such, coordinates are between 0 and 15. Dimensions of board are 16x16.
 * 		 By default, set to 30 total bombs.
 *
 */
public class Board {
	
	private int rows = 16;
	private int columns = 16;
	private int totalBombs = 30;
	private Cell[][] currentBoard;
	
	/**
	 * Default constructor. If nothing is specified, create and initialize a random
	 * board.
	 */
	public Board() {
		currentBoard = new Cell[rows][columns];
		initializeBoard();
	}
	
	/**
	 * Overloaded constructor. Takes in a string representation of a board state
	 * (state that contains the numerical values, not the visibility and flag status)
	 * and creates a board.
	 * @param stringBoard
	 */
	public Board(String stringBoard) {
		if (stringBoard == null || stringBoard.length() != rows * columns) {
			throw new IllegalArgumentException();
		}
		
		currentBoard = new Cell[rows][columns];
		for (int i = 0; i < rows * columns; i++) {
			int row = i / columns;
			int col = i % columns;
			currentBoard[row][col] = new Cell(row, col, 
					Integer.parseInt(stringBoard.substring(i, i + 1)));
		} 
	}
	
	/**
	 * Initializes the board by calling the two functions.
	 * Important to call bombs before numbers (numbers relies on bombs)
	 */
	public void initializeBoard() {
		addBombs();
		addNumbers();
	}
	
	/**
	 * Uses a random number generator to determine placements of bombs.
	 * Ensures that 30 unique positions are generated.
	 */
	public void addBombs() {
		Random r = new Random();
		
		int tempCountBombs = 0;
		while (tempCountBombs < totalBombs) {
			int index = r.nextInt(rows * columns);
			
			//Checks to make sure that randomly generated position isn't already taken
			if (currentBoard[index / rows][index % columns] == null) {
				currentBoard[index / rows][index % columns] = new Cell(index / rows, index % columns, 9);
				tempCountBombs++;
			}
		}
	}
	
	/**
	 * After the bombs are in place, numbers are added. Only provides numerical 
	 * count for cells that aren't bombs.
	 */
	
	public void addNumbers() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (currentBoard[i][j] == null) {
					currentBoard[i][j] = new Cell(i, j, numSurroundingBombs(i,j));
				}
			}
		}
	}
	
	/**
	 * For a given cell, returns the number of surrounding bombs (0-8).
	 * Accounts for corner and edge cells
	 * 
	 * @param x the x coordinate of the cell
	 * @param y the y coordinate of the cell
	 * @return the number of bombs in the cell's immediate vicinity (0-8)
	 * @throws IllegalArgumentException if given coordinate is invalid
	 */
	public int numSurroundingBombs(int x, int y) {
		
		if (x < 0 || y < 0 || x >= rows || y >= columns) {
			throw new IllegalArgumentException();
		}
		
		int currentBombCount = 0;
		
		for (int i = x-1; i <= x+1; i++) {
			for (int j = y-1; j <= y+1; j++) {
				if ((i != x || j != y) && i >= 0 && j >= 0 && i < rows && j < columns) {
					if (currentBoard[i][j] != null && currentBoard[i][j].getValue() == 9) {
						currentBombCount++;
					}

				}
			}
		}
		
		return currentBombCount;
	}
	
	/**
	 * Getter function returning whether or not cell is a bomb.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return boolean representing whether or not cell is a bomb
	 */
	public boolean isBomb(int x, int y) {
		return (currentBoard[x][y].getValue() == 9);
	}
	
	/**
	 * Getter function returning the numerical value of the cell (0-9)
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return number of the cell
	 */
	public int getNumOfCell(int x, int y) {
		return (currentBoard[x][y].getValue());
	}
	
	/**
	 * Getter function returning whether or not cell is visible (clicked).
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return boolean representing whether or not cell is visible
	 */
	public boolean getVisibilityOfCell(int x, int y) {
		return currentBoard[x][y].isVisible();
	}
	
	/**
	 * Setter function that changes the visibility of a cell to given parameter
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param b the boolean value representing new visibility of cell
	 */
	public void changeVisibilityOfCell(int x, int y, boolean b) {
		currentBoard[x][y].setVisible(b);
	}
	
	/**
	 * Getter function returning whether or not cell is flagged.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return boolean representing whether or not cell is flagged
	 */
	public boolean getFlaggedOfCell(int x, int y) {
		return currentBoard[x][y].isFlagged();
	}
	
	/**
	 * Setter function that changes the flagged status of a cell to given parameter
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param b the boolean value representing new flagged status of cell
	 */
	public void changeFlaggedOfCell(int x, int y, boolean b) {
		currentBoard[x][y].setFlagged(b);
	}
	
	/**
	 * @return String representation of the board (numerical values only)
	 */
	public String toString() {
		String stringBoardSolution = "";
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				stringBoardSolution += currentBoard[i][j].getValue();
			}
			stringBoardSolution += "\n";
		}
		
		return stringBoardSolution;
	}
}
