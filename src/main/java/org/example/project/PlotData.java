package org.example.project;

public class PlotData {
    private int[] xValues;
    private double[] yValues;

    public PlotData() {}

    public PlotData(int[] xValues, double[] yValues) {
        this.xValues = xValues;
        this.yValues = yValues;
    }

    public int[] getXValues() {
        return xValues;
    }

    public void setXValues(int[] xValues) {
        this.xValues = xValues;
    }

    public double[] getYValues() {
        return yValues;
    }

    public void setYValues(double[] yValues) {
        this.yValues = yValues;
    }
}
