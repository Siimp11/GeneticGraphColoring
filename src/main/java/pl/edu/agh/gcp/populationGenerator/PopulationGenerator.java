package pl.edu.agh.gcp.populationGenerator;

import pl.edu.agh.gcp.population.Population;

/**
 * Interfejs do generacji populacji startowej
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface PopulationGenerator {
	/**
	 * Generuje populację startową
	 * @param populationSize - wielkość populacji
	 * @param chromosomeSize - wielkość pojedynczego chromosomu - ilość kolorów w chromosomie
	 * @param colorLimit - lomit ilości kolorów do użycia
	 * @return Populacja startowa
	 */
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit);
}
