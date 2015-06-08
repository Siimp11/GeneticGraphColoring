package pl.edu.agh.gcp.parentSelector;

import java.util.Arrays;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Turniejowy sposób wyboru rodziców. Najpierw wybiera 5 losowych osobników z populacji, a 2 najlepszych z tej grupy staje się rodzicami.
 * @author daniel
 *
 */
public class TournamentParentSelector implements ParentSelector {
    private Random random = new Random();

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Chromosome> selectParents(Population population) {
	Chromosome[] tab = new Chromosome[5];
	for (int i = 0; i < 5; i++) {
	    tab[i] = population.get(random.nextInt(population.size()));
	}
	Arrays.sort(tab);
	return new Pair<Chromosome>(tab[0],tab[1]);
	
    }

}
