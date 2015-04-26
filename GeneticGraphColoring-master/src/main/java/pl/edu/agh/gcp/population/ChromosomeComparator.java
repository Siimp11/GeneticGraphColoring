package pl.edu.agh.gcp.population;

import java.util.Comparator;

/**
 * Porównuje wartości funkcji przystosowania chromosomów
 * @author Daniel Tyka
 *
 */
public class ChromosomeComparator implements Comparator<Chromosome> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Chromosome o1, Chromosome o2) {
	return Integer.compare(o1.getFitness(), o2.getFitness());
    }

}
