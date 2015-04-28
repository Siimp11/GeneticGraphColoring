package pl.edu.agh.gcp.mutator;

import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

public class RandomMutator implements Mutator {
	private static int N = 20;
	private Random random = new Random();
	private int chanceNumerator;
	private int chanceDenominator;

	/**
	 * Konstruktor. Szansa na mutacje jest równa chanceNumerator/chanceDenominator
	 * 
	 * @param chanceNumerator
	 *                licznik
	 * @param chanceDenominator
	 *                mianownik
	 */
	public RandomMutator(int chanceNumerator, int chanceDenominator) {
		this.chanceNumerator = chanceNumerator;
		this.chanceDenominator = chanceDenominator;
	}

	@Override
	public boolean mutate(Chromosome chromosome) {
		if (random.nextInt(chanceDenominator) < chanceNumerator)
			return true;
		else
			return false;
	}

	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		int changes = (chromosome.size()+N-1)/N;
		int[] tab = chromosome.getColoringTab();
		for(int i=0;i<changes;i++)
			tab[random.nextInt(tab.length)]=random.nextInt(colorLimit);
		chromosome.setFitnessUncounted();
	}

}
