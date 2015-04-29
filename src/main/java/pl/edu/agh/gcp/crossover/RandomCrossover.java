package pl.edu.agh.gcp.crossover;

import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;

public class RandomCrossover implements Crossover {
	private Random random = new Random();

	@Override
	public Chromosome crossoverFunction(Chromosome parent1, Chromosome parent2) {
		Chromosome child = new Chromosome(parent1.size());
		int[] childTab = child.getColoringTab();
		int[] parent1Tab = parent1.getColoringTab();
		int[] parent2Tab = parent2.getColoringTab();
		for (int i = 0; i < childTab.length; i++) {
			childTab[i] = (random.nextInt(2) == 0 ? parent1Tab[i] : parent2Tab[i]);
		}
		return child;
	}

}
