package pl.edu.agh.gcp.crossover;

import pl.edu.agh.gcp.population.Chromosome;

/**
 * Interfejs klas generujących potomków na podstawie podanych rodziców
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface Crossover {
    
    /**
     * Generuje nowy chromosom na podstawie podanych rodzicow
     * @param parent1
     * @param parent2
     * @return nowy chromosom
     */
    public Chromosome crossoverFunction(Chromosome parent1, Chromosome parent2);
}
