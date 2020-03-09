package database;

public class Statistic {
	private String[] columnLabels, rowLabels;
	private int[][] data;
	
	public Statistic(String[] columnLabels, String[] rowLabels, int[][] data) {
		this.columnLabels = columnLabels;
		this.rowLabels = rowLabels;
		this.data = data;
	}

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
