package pl.edu.agh.gcp.mutator;

import java.util.ArrayList;
import java.util.Collection;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

public class MutatorsList implements Mutator {
	private ArrayList<Mutator> mutatorsList = new ArrayList<Mutator>();

	@Override
	public boolean mutate(Chromosome chromosome) {
		return !mutatorsList.isEmpty();
	}

	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		for(Mutator m : mutatorsList){
			if(m.mutate(chromosome))
				m.mutateFunction(chromosome, colorLimit, graph, vertex, edges);
		}
	}
	
	public void addMutator(Mutator mutator){
		mutatorsList.add(mutator);
	}

}
