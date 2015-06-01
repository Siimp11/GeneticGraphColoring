package pl.edu.agh.gcp.mutator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Mutator który naprawia błędy w chromosomie. Zmienia kolory na końcu błędnej krawędzi na taki który jest dostępny -> nie powoduje błędów
 * @author Daniel Tyka
 * @version 1.0
 */
public class FixBadEdges implements Mutator {
	protected Random random = new Random();
	protected int chanceNumerator;
	protected int chanceDenominator;

	/**
	 * Konstruktor. Szansa na mutacje chromosomu z błędami jest równa chanceNumerator/chanceDenominator
	 * 
	 * @param chanceNumerator
	 *                licznik
	 * @param chanceDenominator
	 *                mianownik
	 */
	public FixBadEdges(int chanceNumerator, int chanceDenominator) {
		this.chanceNumerator = chanceNumerator;
		this.chanceDenominator = chanceDenominator;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean mutate(Chromosome chromosome) {
		if(chromosome.getBadEdges()>0 && random.nextInt(chanceDenominator) < chanceNumerator)
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		int index1, index2, unusedColorCount, counter, chosenColor;
		Object vert1, vert2;
		Collection<Object> neighbours;
		boolean[] colorsUsed = new boolean[colorLimit];
		mainLoop:
		for(Object edge: edges){ // dla każdej krawędzi
			Pair<Object> ends=graph.getEndpoints(edge); // znajdujemy jej końce
			vert1 = ends.getFirst();
			vert2 = ends.getSecond();
			index1 = Arrays.binarySearch(vertex, vert1); //znajdujemy indeksy wierzchołka w tablicy
			index2 = Arrays.binarySearch(vertex, vert2);
			if (chromosome.get(index1)==(chromosome.get(index2))){//sprawdzamy czy wierzchołki mają ten sam kolor
				for(int i =0;i<colorsUsed.length;i++){
					colorsUsed[i]=false;
				}
				if(random.nextInt(2)==0){ //random swap
					int tmp=index1;
					index1=index2;
					index2=tmp;
					Object o= vert1;
					vert1=vert2;
					vert2=o;
				}
				neighbours = graph.getNeighbors(vert1); // bierzemy sąsiadów jednego wierzchołka
				for(Object v: neighbours){ //oznaczamy używane kolory
					colorsUsed[chromosome.get(Arrays.binarySearch(vertex, v))]=true;
				}
				unusedColorCount=0;
				for(boolean b:colorsUsed){ //zliczamy użyte kolory
					if(!b)
						unusedColorCount++;
				}
				if(unusedColorCount==0) //jeżeli wszystkie kolory zostały użyte i nie da się wybrać - kontynuujemy główna pętle
					continue mainLoop;
				counter=random.nextInt(unusedColorCount); // który z kolei nieużyty kolor wybrać
				for(chosenColor=0;chosenColor<colorsUsed.length;chosenColor++){ // przegladamy tablice i wybieramy n-ty nieużyty
					if(!colorsUsed[chosenColor])
						counter--;
					if(counter==-1)
						break;
				}
				chromosome.addColoring(index1, chosenColor);
				chromosome.setFitnessUncounted();
			}
		}
	}

	

}
