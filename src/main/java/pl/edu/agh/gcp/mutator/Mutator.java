package pl.edu.agh.gcp.mutator;

import edu.uci.ics.jung.graph.Graph;
import pl.edu.agh.gcp.population.Chromosome;

/**
 * Mutator z funkcją do mutowania pojedynczych chromosomów
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface Mutator {
    /**
     * Czy wykonać mutację danego chromosomu
     * @param chromosom na którym ma byś wykonana mutacja
     * @return true or false
     */
    public boolean mutate(Chromosome ch);
    /**
     * Funkcja do mutowania podanego chromosmu
     * @param chromosom do zmutowania
     * @param graph
     */
    public void mutateFunction(Chromosome ch, Graph<Object,Object> graph);
}
