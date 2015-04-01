package pl.edu.agh.gcp;

/**
 * Klasa abstrakcyjna algorytmu generycznego - wzorzec strategii
 * 
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public abstract class GeneticAlgorithm {

    /**
     * Generuje populację początkową
     */
    protected abstract void startingPopulation();

    /**
     * Warunek stopu
     * 
     * @return true jeśli pętla ma się zakończyć
     */
    protected abstract boolean breakCondition();

    /**
     * Funkcja oceny osobników populacji
     */
    protected abstract void fitnessFunction();

    /**
     * Krzyżowanie
     */
    protected abstract void crossover();

    /**
     * Mutacja
     */
    protected abstract void mutate();

    /**
     * Ewentualny postprocessing (domyślnie pusta implementacja)
     */
    protected void postProcess() {
    }

    /**
     * Działanie algorytmu generycznego
     */
    public final void gaRun() {
	startingPopulation();
	fitnessFunction();
	while (!breakCondition()) {
	    crossover();
	    mutate();
	    fitnessFunction();
	}
	postProcess();
    }
}
