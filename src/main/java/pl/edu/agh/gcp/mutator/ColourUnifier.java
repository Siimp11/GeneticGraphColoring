package pl.edu.agh.gcp.mutator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

/**
 * Mutator modyfikujący zakres wykorzystanych kolorów. Nie wpływa na ilość
 * błędnie pokolorowanych wierzchołków, jedynie zastępuje obecnie stosowane
 * kolory nowymi, w taki sposób, że dla grafu pokolorowanego przy pomocy k
 * kolorów, będą one mieściły się w zbiorze &lt;0,k-1&gt;. Relacja większy-mniejszy
 * pomiędzy wartościami kolorów zostaje zachowana.
 * Ważne: jest thread-safe. Jedna instancja tej klasy może być jednocześnie wykorzystywane w wielu wątkach bez konieczności synchronizacji.
 * 
 * @author Łukasz Marek
 *
 */
public class ColourUnifier implements Mutator {
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
	 * @param chanceNumerator licznik
	 * @param chanceDenominator mianownik
	 */
	public ColourUnifier(int chanceNumerator, int chanceDenominator) {
		this.chanceNumerator=chanceNumerator;
		this.chanceDenominator=chanceDenominator;
	}

	/**
	 * Funkcja sprawdzająca, czy należy mutować. Mutacja zachodzi przy pomyślnym losowaniu (szansa 1/2) lub w sytuacji,
	 *  gdy chromosom będący argumentem ma wszystkie wierzchołki pokolorowane prawidłowo.
	 *  @param chromosome chromosom
	 */
	@Override
	public boolean mutate(Chromosome chromosome) {
		if ((random.nextInt(chanceDenominator) < chanceNumerator) || chromosome.getBadEdges() == 0)
			return true;
		else
			return false;

	}
	/**
	 * Funkcja pomocnicza. Sprawdza, czy podany kolor występuje już w spisie wykorzystanych w grafie kolorów.
	 * @param colours Tablica zawierająca spis kolorów zastosowanych w grafie.
	 * @param colour Poszukiwany kolor.
	 * @return True, jeśli kolor już jest na liście. W przeciwnym razie false.
	 */
	private boolean contains(int[] colours, int colour) {
		for (int i = 0; i < colours.length; i++)
			if (colours[i] == colour)
				return true;
		return false;
	}
	/**
	 * Funkcja mutująca.
	 */
	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit,
			Graph<Object, Object> graph, Object[] vertex,
			Collection<Object> edges) {
		int[] translator = new int[chromosome.getColors()];
		for (int i = 0; i < translator.length; i++)
			translator[i] = -1;
		int[] colours = chromosome.getColoringTab();
		int index = 0;
		for (int i = 0; i < colours.length; i++) {
			if (!contains(translator, colours[i])) {
				translator[index] = colours[i];
				index++;
				if (index >= translator.length)
					break;
			}
		}
		Arrays.sort(translator);
		boolean[] hasBeenChanged = new boolean[colours.length];
		for (int i = 0; i < colours.length; i++)
			hasBeenChanged[i] = false;
		for (int i = 0; i < translator.length; i++) {
			for (int j = 0; j < hasBeenChanged.length; j++) {
				if (translator[i] == chromosome.getColoringTab()[j]
						&& !hasBeenChanged[j]) {
					chromosome.getColoringTab()[j] = i;
					hasBeenChanged[j] = true;
				}
			}
		}
		return;

	}

}
