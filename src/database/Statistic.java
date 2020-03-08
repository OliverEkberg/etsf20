package database;

public class Statistic {
	private String[] columnLabels, rowLabels;
	private int[][] data;
	
	public String[] getColumnLabels() {
		return columnLabels;
	}
	
	public String[] getRowLabels() {
		return rowLabels;
	}
	
	public int[][] getData() {
		return data;
	}
}
