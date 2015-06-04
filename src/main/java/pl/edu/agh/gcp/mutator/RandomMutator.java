package pl.edu.agh.gcp.mutator;

import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

public class RandomMutator implements Mutator {
	private static final int N = 20; 
	private int changesDiv;
	private Random random = new Random();
	private int chanceNumerator;
	private int chanceDenominator;

	/**
	 * Konstruktor. Szansa na mutacje jest równa chanceNumerator/chanceDenominator.<br/>
	 * ilość zmian = długość_chromosomu/changesDiv   -> zaokrąglana w górę
	 * 
	 * @param chanceNumerator
	 *                licznik
	 * @param chanceDenominator
	 *                mianownik
	 * @param changesDiv
	 * 		  od niego zależy ilość zmian
	 */
	public RandomMutator(int chanceNumerator, int chanceDenominator, int changesDiv) {
		this.chanceNumerator = chanceNumerator;
		this.chanceDenominator = chanceDenominator;
		this.changesDiv=changesDiv;
	}
	
	public RandomMutator(int chanceNumerator, int chanceDenominator) {
		this(chanceNumerator,chanceDenominator,N);
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
		int changes = (chromosome.size()+changesDiv-1)/changesDiv;
		int[] tab = chromosome.getColoringTab();
		for(int i=0;i<changes;i++)
			tab[random.nextInt(tab.length)]=random.nextInt(colorLimit);
		chromosome.setFitnessUncounted();
	}

}
