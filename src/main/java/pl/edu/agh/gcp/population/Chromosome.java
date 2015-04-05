package pl.edu.agh.gcp.population;

import java.util.Arrays;

public class Chromosome implements Comparable<Chromosome>, Cloneable {
    /**
     * ocena kolorowania grafu. Zawsze >=0, a -1 ozncza ze jeszcze nie została obliczon
     */
    private int fitness = -1;
    private int colors = -1;
    private int badEdges = -1;
    private int[] coloring;

    public Chromosome(int size) {
	coloring = new int[size];
    }

    public boolean isCounted() {
	return fitness >= 0;
    }

    public void setFitness(int fitness) {
	this.fitness = fitness;
    }

    public int getFitness() {
	return fitness;
    }

    public int getColors() {
	return colors;
    }

    public void setColors(int colors) {
	this.colors = colors;
    }

    public int getBadEdges() {
	return badEdges;
    }

    public void setBadEdges(int badEdges) {
	this.badEdges = badEdges;
    }

    public void addColoring(int index, int color) {
	coloring[index] = color;
    }

    public int size() {
	return coloring.length;
    }

    public Integer get(int index) {
	return coloring[index];
    }

    public int[] getColoringTab() {
	return coloring;
    }

    @Override
    public int compareTo(Chromosome ch) {
	return Integer.compare(fitness, ch.fitness);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	Chromosome ch = (Chromosome) super.clone();
	ch.coloring = Arrays.copyOf(coloring, coloring.length);
	return ch;
    }

}
