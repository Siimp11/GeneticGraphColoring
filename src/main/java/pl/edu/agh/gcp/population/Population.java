package pl.edu.agh.gcp.population;

import java.util.ArrayList;
import java.util.Iterator;

public class Population implements Iterable<Chromosome> {
    /*
     * Pola: zbiór/lista(lub cokolwiek innego) chromosomów ew. graf
     */
    private ArrayList<Chromosome> population;

    public Population(int size) {
	population = new ArrayList<Chromosome>(size);
    }

    public void add(Chromosome ch) {
	population.add(ch);
    }

    public Chromosome get(int index) {
	return population.get(index);
    }

    public Chromosome remove(int index) {
	return population.remove(index);
    }

    @Override
    public Iterator<Chromosome> iterator() {
	return population.iterator();
    }

}
