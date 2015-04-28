package pl.edu.agh.gcp.populationGenerator;


import java.util.LinkedList;

import edu.uci.ics.jung.graph.util.Pair;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

public class UnifiedColorsPopulation implements PopulationGenerator {
	private RandomPopulation randomPopulation = new RandomPopulation();

	@Override
	public Population generatePopulation(int populationSize, int chromosomeSize, int colorLimit) {
		Population population = randomPopulation.generatePopulation(populationSize, chromosomeSize, colorLimit);
		for(Chromosome ch: population){
			int colorCounter=0;
			int[] colorTab = ch.getColoringTab();
			LinkedList<Pair<Integer>> translationList = new LinkedList<Pair<Integer>>();
			colorCheck:
			for(int color:colorTab){
				for(Pair<Integer> translate: translationList){
					if(translate.getFirst().intValue()==color)
						continue colorCheck;
				}
				translationList.add(new Pair<Integer>(color, colorCounter));
				colorCounter++;
			}
			for(int i=0;i<colorTab.length;i++){
				for(Pair<Integer> translate: translationList){
					if(translate.getFirst().intValue()==colorTab[i]){
						colorTab[i]=translate.getSecond().intValue();
						break;
					}
				}
			}
			ch.setColors(colorCounter);
		}
		return population;
	}

}
