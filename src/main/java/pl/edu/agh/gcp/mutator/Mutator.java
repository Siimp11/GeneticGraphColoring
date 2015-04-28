package pl.edu.agh.gcp.mutator;

import java.util.Collection;

import edu.uci.ics.jung.graph.Graph;
import pl.edu.agh.gcp.population.Chromosome;

/**
 * Mutator z funkcją do mutowania pojedynczych chromosomów
 * 
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public interface Mutator {
    /**
     * Czy wykonać mutację danego chromosomu. 
     * <p>W tym momencie chromosom ma już policzone wartości Fitness, Colors oraz BadEdges<p>
     * 
     * @param chromosom
     *            na którym ma być wykonana mutacja
     * @return true or false
     */
    public boolean mutate(Chromosome chromosome);

    /**
     * Funkcja do mutowania podanego chromosmu
     * <p>
     * Chromosom na którym ma być wykonana mutacja ma już policzone wartości Fitness, Colors i BadEdges. Jeżeli wprowadzasz zmiany w chromosomie, które mogą
     * zmienić te wartości trzeba zlecić ich ponowne policzenie funkcją {@link Chromosome#setFitnessUncounted()},
     * {@link Chromosome#setColorsUncounted()} lub {@link Chromosome#setBadEdgesUncounted()} w zależności czy wszystko mogło ulec zmianie czy tylko
     * ilość kolorów / ilość złych krawędzi
     * </p>
     * 
     * @param chromosome
     *            - chromosom do mutacji
     * @param graph
     *            - graf na którym działa algorytm
     * @param vertex
     *            - posortowana tablica wierzchołków grafu
     * @param edges
     *            - kolekcja krawędzi grafu (to co zwróci graph.getEdges(); )
     */
    public void mutateFunction(Chromosome chromosome, int colorLimit, Graph<Object, Object> graph, Object[] vertex, Collection<Object> edges);
}
