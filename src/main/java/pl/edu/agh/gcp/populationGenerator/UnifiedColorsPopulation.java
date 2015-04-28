package pl.edu.agh.gcp.populationGenerator;


import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

public class UnifiedColorsPopulation implements PopulationGenerator {
	private RandomPopulation randomPopulation = new RandomPopulation();
	private ColorUnifier2 colorUnifier = new ColorUnifier2(1,1);

	@Override
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit) {
		Population population = randomPopulation.generatePopulation(populationSize, chromosomeSize, colorLimit);
		for(Chromosome ch: population){
			colorUnifier.mutateFunction(ch, colorLimit, null, null, null);
		}
		return population;
	}

}
