package pl.edu.agh.gcp.resultSelector;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

/**
 * Interfejs do wybierania wynikowego Chromosomu z populacji
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface BestResultSelector {
    /**
     * Wybiera wynikowy Chromosom z populacji
     * @param population
     * @return wynikowy chromosom
     */
    public Chromosome selectResult(Population population);

}
