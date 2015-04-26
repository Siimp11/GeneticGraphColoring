package pl.edu.agh.gcp.population;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Populacja chromosomów
 * @see Chromosome
 * @author daniel
 *
 */
public class Population implements Iterable<Chromosome> {
    /**
     * populacja - lista chromosomów
     */
    private ArrayList<Chromosome> population;

    /**
     * Konstruktor
     * @param size - wielkośc populacji
     */
    public Population(int size) {
	population = new ArrayList<Chromosome>(size);
    }

    /**
     * Dodaj chromosom do populacji
     * @param chromosom
     */
    public void add(Chromosome chromosome) {
	population.add(chromosome);
    }

    /**
     * Zwraca n-ty chromosom
     * @param index
     * @return chromosom
     */
    public Chromosome get(int index) {
	return population.get(index);
    }

    /**
     * Usuwa chromosom o podanym indeksie
     * @param index
     * @return usiniety chromosom
     */
    public Chromosome remove(int index) {
	return population.remove(index);
    }
    
    /**
     * wielskosc populacji
     * @return
     */
    public int size(){
	return population.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Chromosome> iterator() {
	return population.iterator();
    }

}
