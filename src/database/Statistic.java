package database;

/**
* A model class for the statistics made from information in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class Statistic {
	private String[] columnLabels, rowLabels;
	private int[][] data;
	
	/**
	 * Constructor of the model class Statistic.
	 * @param columnLabels Is the name of the columns.
	 * @param rowLabels Is the name of the rows.
	 * @param data Is the minutes put in a matrix to match the columnLabels and rowLabels.
	 */
	public Statistic(String[] columnLabels, String[] rowLabels, int[][] data) {
		this.columnLabels = columnLabels;
		this.rowLabels = rowLabels;
		this.data = data;
	}

	/**
	 * Gets a string array of the column names.
	 * @return This returns an array of the column names.
	 */
	public String[] getColumnLabels() {
		return columnLabels;
	}
	
	/**
	 * Gets a string array of the row names.
	 * @return This returns an array of the row names.
	 */
	public String[] getRowLabels() {
		return rowLabels;
	}
	
	/**
	 * Gets a 2d array, or matrix, of the data.
	 * @return This returns the data in a matrix / 2d array.
	 */
	public int[][] getData() {
		return data;
	}
}
