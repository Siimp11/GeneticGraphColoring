package pl.edu.agh.gcp.population;

import java.util.Comparator;

public class ChromosomeComparator implements Comparator<Chromosome> {

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
	return Integer.compare(o1.getFitness(), o2.getFitness());
    }

}
