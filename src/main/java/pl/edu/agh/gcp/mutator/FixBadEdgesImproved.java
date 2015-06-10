package pl.edu.agh.gcp.mutator;

import java.util.Arrays;
import java.util.Collection;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Usprawniona wersja {@link FixBadEdges}. Stara się użyc już użytych kolorów w chromosomie do naprawy błędu.
 * @author Daniel Tyka
 * @version 1.0
 */
public class FixBadEdgesImproved extends FixBadEdges implements Mutator {

	/**
	 * Konstruktor. Szansa na mutacje jest równa <code>chanceNumerator/chanceDenominator</code>.
	 * 
	 * @param chanceNumerator
	 *                licznik
	 * @param chanceDenominator
	 *                mianownik
	 */
	public FixBadEdgesImproved(int chanceNumerator, int chanceDenominator) {
		super(chanceNumerator, chanceDenominator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		int index1, index2, unusedColorCount, counter, chosenColor;
		Object vert1, vert2;
		Collection<Object> neighbours;
		boolean[] clrsUsdNeighbour = new boolean[colorLimit];
		boolean[] clrsUsdChromosome = new boolean[colorLimit];
		for (int color : chromosome.getColoringTab()) { // kolory uzyte w całym chromosomie
			clrsUsdChromosome[color] = true;
		}
		mainLoop: for (Object edge : edges) { // dla każdej krawędzi
			Pair<Object> ends = graph.getEndpoints(edge); // znajdujemy jej końce
			vert1 = ends.getFirst();
			vert2 = ends.getSecond();
			index1 = Arrays.binarySearch(vertex, vert1); // znajdujemy indeksy wierzchołka w tablicy
			index2 = Arrays.binarySearch(vertex, vert2);
			if (chromosome.get(index1) == (chromosome.get(index2))) {// sprawdzamy czy wierzchołki mają ten sam kolor
				for (int i = 0; i < clrsUsdNeighbour.length; i++) {
					clrsUsdNeighbour[i] = false;
				}
				if (random.nextInt(2) == 0) { // random swap
					int tmp = index1;
					index1 = index2;
					index2 = tmp;
					Object o = vert1;
					vert1 = vert2;
					vert2 = o;
				}
				neighbours = graph.getNeighbors(vert1); // bierzemy sąsiadów jednego wierzchołka
				for (Object v : neighbours) { // oznaczamy używane kolory
					clrsUsdNeighbour[chromosome.get(Arrays.binarySearch(vertex, v))] = true;
				}
				unusedColorCount = 0;
				for (boolean b : clrsUsdNeighbour) { // zliczamy nieużyte kolory
					if (!b)
						unusedColorCount++;
				}
				if (unusedColorCount == 0) // jeżeli wszystkie kolory zostały użyte i nie da się wybrać - kontynuujemy główna pętle
					continue mainLoop;
				if ((colorLimit-unusedColorCount) < chromosome.getColors()) { // priorytetyzuje uzyte kolory
					counter = random.nextInt(chromosome.getColors() - (colorLimit-unusedColorCount)); // który z kolei uzyty kolor w
													     // chromosomie wybrać
					for (chosenColor = 0; chosenColor < clrsUsdNeighbour.length; chosenColor++) { // wybieramy n-ty
						// kolor który jest w chromosomie ale nie ma go w sąsiadach
						if (!clrsUsdNeighbour[chosenColor] && clrsUsdChromosome[chosenColor])
							counter--;
						if (counter == -1)
							break;
					}
				} else {
					counter = random.nextInt(unusedColorCount); // który z kolei nieużyty kolor wybrać
					for (chosenColor = 0; chosenColor < clrsUsdNeighbour.length; chosenColor++) { // wybieramy n-ty nieużyty
						// kolor którego nie ma go w sąsiadach
						if (!clrsUsdNeighbour[chosenColor])
							counter--;
						if (counter == -1)
							break;
					}
				}
				chromosome.addColoring(index1, chosenColor);
				chromosome.setFitnessUncounted();

			}
		}
	}

}
