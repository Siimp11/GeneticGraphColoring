package pl.edu.agh.gcp.parentSelector;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Interfejs do wyboru rodziców z populacji dla nastepnego potomka
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface ParentSelector {
    /**
     * Wybiera rodziców z populacji
     * @param population
     * @return para rodziców
     */
    public Pair<Chromosome> selectParents(Population population);
}
