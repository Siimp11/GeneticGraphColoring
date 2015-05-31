package pl.edu.agh.gcp.mutator;

import java.util.Collection;
import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.graph.Graph;

public class UsedColor implements Mutator {
	private static int N = 20;
	private Random random = new Random();
	private int chanceNumerator;
	private int chanceDenominator;
	private int usedColorNumerator;
	private int usedColorDenominator;

	public UsedColor(int chanceNumerator, int chanceDenominator, int usedColorNumerator, int usedColorDenominator) {
		this.chanceNumerator = chanceNumerator;
		this.chanceDenominator = chanceDenominator;
		this.usedColorNumerator = usedColorNumerator;
		this.usedColorDenominator = usedColorDenominator;
	}

	@Override
	public boolean mutate(Chromosome chromosome) {
		if (random.nextInt(chanceDenominator) < chanceNumerator)
			return true;
		else
			return false;
	}

	@Override
	public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges) {
		int changes = (chromosome.size() + N - 1) / N;
		boolean[] usedColors = new boolean[colorLimit];
		int[] tab = chromosome.getColoringTab();
		for (int color : tab) {
			usedColors[color] = true;
		}
		boolean usedColor; // czy losować użyte kolory czy nie
		int colors; // ilość użytych kolorów / nie użytych - zależnie od usedColor
		if ((colorLimit == chromosome.getColors()) || (random.nextInt(usedColorDenominator) < usedColorNumerator)) {
			usedColor = true;
			colors = chromosome.getColors();
		} else {
			usedColor = false;
			colors = colorLimit-chromosome.getColors();
		}
		for (int i = 0; i < changes; i++) {
			int colorNumber = random.nextInt(colors); // który kolor z kolei wybrać
			int counter=0; //numer wybranego koloru
			while(colorNumber>=0){
				if(usedColors[counter]==usedColor)
					colorNumber--;
				counter++;
			}
			counter--;
			tab[random.nextInt(tab.length)]=counter;
		}
		chromosome.setFitnessUncounted();
	}

}
