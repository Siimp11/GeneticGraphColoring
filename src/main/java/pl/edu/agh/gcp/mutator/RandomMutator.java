package pl.edu.agh.gcp.mutator;

import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

/**
 * Operator losowej mutacji.
 * @author daniel
 *
 */
public class RandomMutator implements Mutator {
	/**
	 * Dzielnik. Ilość zmian to 100/N % w zaokrągleniu w górę
	 */
	private static final int N = 20; 
	private int changesDiv;
	private Random random = new Random();
	/**
	 * Licznik szansy na mutację
	 */
	private int chanceNumerator;
	/**
	 * Mianownik szansy na mutację
	 */
	private int chanceDenominator;

	/**
	 * Konstruktor. Szansa na mutacje jest równa <code>chanceNumerator/chanceDenominator</code>.<br>
	 * <code>ilość zmian = długość_chromosomu/changesDiv</code>   -&gt; zaokrąglana w górę
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
	
	/**
	 * Konstruktor z domyślną wartością <code>changesDiv={@link #N}</code>
	 * @param chanceNumerator licznik
	 * @param chanceDenominator mianownik
	 */
	public RandomMutator(int chanceNumerator, int chanceDenominator) {
		this(chanceNumerator,chanceDenominator,N);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean mutate(Chromosome chromosome) {
		if (random.nextInt(chanceDenominator) < chanceNumerator)
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		int changes = (chromosome.size()+changesDiv-1)/changesDiv;
		int[] tab = chromosome.getColoringTab();
		for(int i=0;i<changes;i++)
			tab[random.nextInt(tab.length)]=random.nextInt(colorLimit);
		chromosome.setFitnessUncounted();
	}

}
