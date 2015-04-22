package pl.edu.agh.gcp.mutator;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

/**
 * Pusta implementacja Mutatora
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
    public void mutateFunction(Chromosome ch, Graph<Object, Object> graph) {
    }

}
