package pl.edu.agh.gcp;

/**
 * Klasa abstrakcyjna algorytmu generycznego - wzorzec strategii
 * 
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public abstract class DefaultGeneticAlgorithm implements GenericAlgorithm {

    /**
     * Ewentualny preprocesing (domyślnie pusta implementacja)
     */
    protected void preProcess() {
    }

    /**
     * Generuje populację początkową
     */
    protected abstract void startPopulation();

    /**
     * Warunek stopu
     * 
     * @return true jeśli pętla ma się zakończyć
     */
    protected abstract boolean breakCondition();

    /**
     * Funkcja oceny osobników populacji
     */
    protected abstract void fitness();

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
     * {@inheritDoc}
     */
    @Override
    public final void run() {
	preProcess();
	startPopulation();
	fitness();
	while (!breakCondition()) {
	    crossover();
	    mutate();
	    fitness();
	}
	postProcess();
    }

}
