package pl.edu.agh.gcp.mutator;

import java.util.Collection;

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
    public void mutateFunction(Chromosome chromosome, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
	// TODO Auto-generated method stub
	
    }

}
