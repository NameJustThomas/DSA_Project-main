package minesweeper.Main;



/**
 * This class focuses on each individual cell of the Minesweeper game.
 * Cell itself does not contain much aside from a few getters and setters.
 * Each cell also stores a boolean variable indicating its visibility and 
 * flagged status.
 * 
 * Note: this class was made specifically for this version of Minesweeper. 
 * 		 As such, coordinates are between 0 and 15.
 *
 */
public class Cell {
	
	private int xCoor;
	private int yCoor;
	private int value;			//0-8 if empty/next to bomb, 9 if bomb
	private boolean visible = false;	//true if visible, false if hidden
	private boolean flagged = false;
	
	/**
	 * Creates a cell.
	 * 
	 * @param x the x coordinate value of the cell
	 * @param y the y coordinate value of the cell
	 * @param val the numerical value of the cell indicating neighboring bombs (9 if bomb)
	 * @throws IllegalArgumentException if the given parameters are not valid 
	 * (x,y must be between 0 and 15, val must be between 0 and 9)
	 * 
	 */
	public Cell(int x, int y, int val) {
		if (x < 0 || y < 0 || val < 0 || val > 9 || x > 15 || y > 15) {
			throw new IllegalArgumentException();
		}
		
		xCoor = x;
		yCoor = y;
		value = val;
	}

	/**
	 * @return the xCoor
	 */
	public int getxCoor() {
		return xCoor;
	}

	/**
	 * @return the yCoor
	 */
	public int getyCoor() {
		return yCoor;
	}

	/**
	 * @return the value of the cell
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the visibility of the cell
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visibility to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the flagged status of the cell
	 */
	public boolean isFlagged() {
		return flagged;
	}

	/**
	 * @param flagged the flagged to set
	 */
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}
	
}
