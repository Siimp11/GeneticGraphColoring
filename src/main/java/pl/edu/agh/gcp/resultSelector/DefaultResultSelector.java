package pl.edu.agh.gcp.resultSelector;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;

/**
 * Domyślna implementacja wybierania wynikoweg Chromosomu z populacji
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class DefaultResultSelector implements BestResultSelector {

    /**
     * {@inheritDoc}
     */
    @Override
    public Chromosome selectResult(Population population) {
	int index=0;
	int min=population.get(0).getFitness();
	int tmp;
	for(int i=1;i<population.size();i++){
	    tmp=population.get(i).getFitness();
	    if(tmp<min){
		min=tmp;
		index=i;
	    }
	}
	return population.get(index);
    }

}
