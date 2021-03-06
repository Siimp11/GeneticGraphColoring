package pl.edu.agh.gcp.crossover;

import java.util.Random;

import pl.edu.agh.gcp.population.Chromosome;

/**
 * Operator krzyżowania rodziców z dwoma punktami podziału
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class TwoPointCrossover implements Crossover {
    private Random random = new Random();

    /**
     * {@inheritDoc}
     */
    @Override
    public Chromosome crossoverFunction(Chromosome parent1, Chromosome parent2) {
	int split1 = random.nextInt(parent1.size());
	int split2 = random.nextInt(parent1.size());
	if (split2 < split1) {
	    int tmp = split2;
	    split2 = split1;
	    split1 = tmp;
	}
	Chromosome child = null;
	try {
	    child = (Chromosome) parent1.clone();
	} catch (CloneNotSupportedException e) {
	}
	child.setFitnessUncounted();
	for (int i = split1; i <= split2; i++) {
	    child.addColoring(i, parent2.get(i));
	}
	return child;
    }

}
