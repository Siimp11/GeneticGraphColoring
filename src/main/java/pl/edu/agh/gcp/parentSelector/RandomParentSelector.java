package pl.edu.agh.gcp.parentSelector;

import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Całkowicie losowy sposób wyboru rodziców.
 * @author Daniel Tyka
 *
 */
public class RandomParentSelector implements ParentSelector {
	private Random random = new Random();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Chromosome> selectParents(Population population) {
		Chromosome parent1 = population.get(random.nextInt(population.size()));
		Chromosome parent2 = population.get(random.nextInt(population.size()));
		return new Pair<Chromosome>(parent1,parent2);
	}

}
