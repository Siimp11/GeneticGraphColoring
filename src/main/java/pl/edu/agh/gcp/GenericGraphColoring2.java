package pl.edu.agh.gcp;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import edu.uci.ics.jung.graph.Graph;

public class GenericGraphColoring2 extends GenericGraphColoring {
	private Population parents = null;
	private Random random = new Random();

	public GenericGraphColoring2(Graph<Object, Object> graph) {
		super(graph);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void crossover() {
		double gaussRandom=0;
		population.sort();
		population.countMeanSD();
		mainLoop:
		while(true){
			for(int i=0;i<20;i++) {
				gaussRandom = (random.nextGaussian() * population.getStandardDeviation()) + population.getMean();
				// sprawdza czy przynajmniej dwa się mieszczą i czy przynajmniej jedno dziecko zostanie wygenerowane
				if ((double)population.get(1).getFitness() < gaussRandom
						&& (double)population.get(population.size()-1).getFitness() >= gaussRandom)
					break mainLoop;
			} 
			mutate();
			fitness();
			population.sort();
			population.countMeanSD();
		}
		int parentsCount = 0;
		for (Chromosome ch : population) {
			if ((double)ch.getFitness() < gaussRandom)
				parentsCount++;
			else
				break;
		}
		population = population.subPopulation(0, parentsCount);
		int taskCounter = properties.populationSize - population.size();
		Chromosome[] childs = new Chromosome[taskCounter];
		for (int i = 0; i < taskCounter; i++) {
			taskCompletionService.submit(new DoCrossover());
		}
		for (int i = 0; i < taskCounter; i++) {
			try {
				childs[i] = taskCompletionService.take().get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		parents = population; // zamiana żeby mutacja wykonywała się tylko na nowych potomkach. Póżniej trzeba je połączyć
		
		population = new Population(childs.length);
		for (int i = 0; i < taskCounter; i++) {
			population.add(childs[i]);
		}
	}

	@Override
	protected void mutate() {
		super.mutate();
		if(parents!=null && parents.size()>0)
			population.addAll(parents);
		parents = null;
	}
}
