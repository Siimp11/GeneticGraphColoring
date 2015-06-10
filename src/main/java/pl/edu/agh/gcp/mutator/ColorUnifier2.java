package pl.edu.agh.gcp.mutator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Usprawniona wersja operatora {@link ColourUnifier}. Gwarantuje, że pierwszy wierzcholek bedzie zawsze miał kolor o numerze 0, a każdy następny 
 * kolor już użyty lub o jeden większy od aktualnego maksymalnego.
 * @author Daniel Tyka
 * @version 2.0
 */
public class ColorUnifier2 implements Mutator {
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
	 * Konstruktor. Szansa na mutacje jest równa <code>chanceNumerator/chanceDenominator</code>
	 * 
	 * @param chanceNumerator
	 *                licznik
	 * @param chanceDenominator
	 *                mianownik
	 */
	public ColorUnifier2(int chanceNumerator, int chanceDenominator) {
		this.chanceNumerator = chanceNumerator;
		this.chanceDenominator = chanceDenominator;
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
		int colorCounter=0;
		int[] colorTab = chromosome.getColoringTab();
		LinkedList<Pair<Integer>> translationList = new LinkedList<Pair<Integer>>();
		colorCheck:
		for(int color:colorTab){
			for(Pair<Integer> translate: translationList){
				if(translate.getFirst().intValue()==color)
					continue colorCheck;
			}
			translationList.add(new Pair<Integer>(color, colorCounter));
			colorCounter++;
		}
		for(int i=0;i<colorTab.length;i++){
			for(Pair<Integer> translate: translationList){
				if(translate.getFirst().intValue()==colorTab[i]){
					colorTab[i]=translate.getSecond().intValue();
					break;
				}
			}
		}
	}

}
