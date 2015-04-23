package pl.edu.agh.gcp.population;

import java.util.Arrays;

/**
 * Reprezentacja pojedynczego chromosomu
 * 
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class Chromosome implements Comparable<Chromosome>, Cloneable {
    /**
     * ocena kolorowania grafu. Zawsze >=0, a -1 ozncza ze jeszcze nie została obliczon
     */
    private int fitness = -1;
    /**
     * Ilość użytych kolorów, zawsze >=0, -1 oznacza że nie została jeszcze policzona
     */
    private int colors = -1;
    /**
     * Ilość złych krawędzi, zawsze >=0, -1 oznacza że nie została jeszcze policzona
     */
    private int badEdges = -1;
    /**
     * Pokolorowanie grafu
     */
    private int[] coloring;

    /**
     * Konstruktor
     * 
     * @param size
     *            - ilośc wierzchołków w grafie
     */
    public Chromosome(int size) {
	coloring = new int[size];
    }

    /**
     * Czy funkcja przystosowania została policzona dla tego chromosomu
     * 
     * @return true or false
     */
    public boolean isCounted() {
	return fitness >= 0;
    }

    /**
     * Ustawia przystosowanie tego chromosomu
     * 
     * @param fitness
     */
    public void setFitness(int fitness) {
	if (fitness < 0)
	    throw new IllegalArgumentException("Fitness must be >= 0");
	else
	    this.fitness = fitness;
    }
    
    /**
     * Ustawia wartość pól Fitness, Colors oraz BadEdges na -1, co oznacza że nie zostały obliczone.
     */
    public void setFitnessUncounted(){
	fitness=-1;
	colors=-1;
	badEdges=-1;
    }

    /**
     * Zwraca wartość funkcji przystosowania, -1 w przypadku jeżeli nie zostałą jeszcze obliczona
     * 
     * @return wartość funkcji przystosowania
     */
    public int getFitness() {
	return fitness;
    }

    /**
     * Zwraca ilość kolorów, -1 w przypadku jeżeli nie zostały jeszcze policzone
     * 
     * @return ilość kolorów
     */
    public int getColors() {
	return colors;
    }

    /**
     * Ustawia ilośc kolorów w chromosomie
     * 
     * @param colors
     */
    public void setColors(int colors) {
	if (colors < 0)
	    throw new IllegalArgumentException("Colors must be >= 0");
	else
	    this.colors = colors;
    }
    
    /**
     * Ustawia pola Fitness oraz Colors na -1, co oznacza że nie zostały policzone
     */
    public void setColorsUncounted(){
	colors=-1;
	fitness=-1;
    }

    /**
     * Zwraca ilość złych krawędzi w chromosomie, -1 w przypadku gdy nie zostały jeszcze policzone
     * 
     * @return ilość złych krawędzi w chromosomie
     */
    public int getBadEdges() {
	return badEdges;
    }

    /**
     * Ustawia ilość żłych krawędzi
     * 
     * @param badEdges
     */
    public void setBadEdges(int badEdges) {
	if (badEdges < 0)
	    throw new IllegalArgumentException("Bad edges must be >= 0");
	else
	    this.badEdges = badEdges;
    }
    
    /**
     * Ustawia pola Fitness oraz BadEdges na -1, co oznacza że nie zostały policzone
     */
    public void setBadEdgesUncounted(){
	badEdges=-1;
	fitness=-1;
    }

    /**
     * Wstawia kolorowanie danego wierzchołka w grafie
     * 
     * @param index
     * @param color
     */
    public void addColoring(int index, int color) {
	coloring[index] = color;
    }

    /**
     * Wielkość tablicy z kolorowaniem = ilość wierzchołków w grafie
     * 
     * @return
     */
    public int size() {
	return coloring.length;
    }

    /**
     * Zwraca kolor danego wierzchołka w grafie
     * 
     * @param index
     * @return kolor danego wierzchołka
     */
    public Integer get(int index) {
	return coloring[index];
    }

    /**
     * Zwraca tablicę z kolorowaniem grafu
     * 
     * @return tablica z kolorowaniem grafu
     */
    public int[] getColoringTab() {
	return coloring;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Chromosome ch) {
	return Integer.compare(fitness, ch.fitness);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	Chromosome ch = (Chromosome) super.clone();
	ch.coloring = Arrays.copyOf(coloring, coloring.length);
	return ch;
    }

}
