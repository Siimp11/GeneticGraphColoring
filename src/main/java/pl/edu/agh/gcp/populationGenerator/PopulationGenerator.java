package pl.edu.agh.gcp.populationGenerator;

import pl.edu.agh.gcp.population.Population;

public interface PopulationGenerator {
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit);
}
