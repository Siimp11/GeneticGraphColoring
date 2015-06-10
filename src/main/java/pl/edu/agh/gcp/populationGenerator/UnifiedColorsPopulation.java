package pl.edu.agh.gcp.populationGenerator;

import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

/**
 * Generuje populację ze zunifikowanymi kolorami - mutacja {@link ColorUnifier2}
 * @see ColorUnifier2
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class UnifiedColorsPopulation implements PopulationGenerator {
	private RandomPopulation randomPopulation = new RandomPopulation();
	/**
	 * Operator unifikujący kolory w wygenerowanych chromosomach
	 */
	private ColorUnifier2 colorUnifier = new ColorUnifier2(1,1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit) {
		Population population = randomPopulation.generatePopulation(populationSize, chromosomeSize, colorLimit);
		for(Chromosome ch: population){
			colorUnifier.mutateFunction(ch, colorLimit, null, null, null);
		}
		return population;
	}

}
