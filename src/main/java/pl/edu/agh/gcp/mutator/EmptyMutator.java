package pl.edu.agh.gcp.mutator;

import java.util.Collection;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

/**
 * Pusta implementacja Mutatora. Szansa na mutację równa 0%. Pusta implementacja funkcji mutującej.
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class EmptyMutator implements Mutator {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mutate(Chromosome ch) {
	return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {	
    }

}
