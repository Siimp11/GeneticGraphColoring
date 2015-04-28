package pl.edu.agh.gcp.populationGenerator;

import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

public class RandomPopulation implements PopulationGenerator {
	private Random random = new Random();

	@Override
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit) {
		Population population = new Population(populationSize);
		for (int count = 0; count < populationSize; count++) {
			Chromosome ch = new Chromosome(chromosomeSize);
			for (int i = 0; i < chromosomeSize; i++) {
				ch.addColoring(i, random.nextInt(colorLimit));
			}
			population.add(ch);
		}
		return population;
	}

}
