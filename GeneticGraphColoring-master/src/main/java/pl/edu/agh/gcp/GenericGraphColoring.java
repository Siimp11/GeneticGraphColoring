package pl.edu.agh.gcp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.agh.gcp.crossover.Crossover;
import pl.edu.agh.gcp.crossover.DefaultCrossover;
import pl.edu.agh.gcp.mutator.ColourUnifier;
import pl.edu.agh.gcp.mutator.Mutator;
import pl.edu.agh.gcp.parentSelector.DefaultParentSelector;
import pl.edu.agh.gcp.parentSelector.ParentSelector;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import pl.edu.agh.gcp.resultSelector.DefaultResultSelector;
import pl.edu.agh.gcp.resultSelector.ResultSelector;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Generyczny algorytm kolorowania grafu. Wymaga wierzcholków implementujączych interfejs <b>{@link Comparable}</b>
 * 
 * @author Daniel Tyka
 * @version 1.0
 *
 */
public class GenericGraphColoring extends DefaultGeneticAlgorithm {
	/**
	 * Klasa wewnętrzna do obliczenia wielowątkowo oceny chromosomów
	 * 
	 * @author Daniel Tyka
	 * @version 1.0
	 */
	private class DoCountFitness implements Callable<Chromosome> {
		private Chromosome ch;

		public DoCountFitness(Chromosome ch) {
			this.ch = ch;
		}

		@Override
		public Chromosome call() throws Exception {
			GenericGraphColoring.this.fitnessFunction(ch);
			return null;
		}

	}

	/**
	 * Klasa wewnętrzna do wielowątkowego rozmnażania populacji
	 * 
	 * @author Daniel Tyka
	 * @version 1.0
	 */
	private class DoCrossover implements Callable<Chromosome> {

		@Override
		public Chromosome call() throws Exception {
			return GenericGraphColoring.this.crossoverFunction();
		}

	}

	/**
	 * Klasa wewnetrzna do wielowątkowej mutacji
	 * 
	 * @author Daniel Tyka
	 * @version 1.0
	 */
	private class DoMutate implements Callable<Chromosome> {
		private Chromosome ch;

		public DoMutate(Chromosome ch) {
			this.ch = ch;
		}

		@Override
		public Chromosome call() throws Exception {
			properties.mutator.mutateFunction(ch, GenericGraphColoring.this.graph, GenericGraphColoring.this.vertex,
					GenericGraphColoring.this.edges);
			return null;
		}

	}

	/**
	 * Klasa zbierajaca do kupy wszystkie opcje algorytmu
	 * 
	 * @author Daniel Tyka
	 * @version 1.0
	 */
	private static class AlgorithmProperties {
		/**
		 * Wielkość populacji
		 */
		public int populationSize = 500;
		/**
		 * Waga krawędzi łączących wierzchołki o takim samym kolorze - funkcja oceniająca chromosomy
		 */
		public int badEdgeWeight = 5;
		/**
		 * Waga ilości użytych kolorów - funkcja oceniająca chromosomy
		 */
		public int colorsUsedWeight = 2;
		/**
		 * Limit iteracji
		 */
		public int iterationsLimit = 200;
		/**
		 * Ilość wątków
		 */
		public int threads = Runtime.getRuntime().availableProcessors();
		/**
		 * ParentSelector do wybierania rodziców nasępnego chromosomu
		 */
		public ParentSelector parentSelector = new DefaultParentSelector();
		/**
		 * Crossover do tworzenia kolejnych potomkow rodziców
		 */
		public Crossover crossover = new DefaultCrossover();
		/**
		 * Mutator to mutowania chromosomow
		 */
		public Mutator mutator = new ColourUnifier();
		/**
		 * ResultSelector do wybierania wyniku
		 */
		public ResultSelector resultSelector = new DefaultResultSelector();
	}

	/**
	 * Opcje algorytmu
	 */
	private AlgorithmProperties properties = new AlgorithmProperties();
	/**
	 * Limit ilości użytych kolorów (numeracja kolorów od 0)
	 */
	private int colorLimit = 0;
	/**
	 * Licznik iteracji
	 */
	private int iterationsCounter = 0;

	/**
	 * Graf do pokolorowania
	 */
	private Graph<Object, Object> graph;
	/**
	 * Tablica wierzchołków grafu (posortowana - do binary search)
	 */
	private Object[] vertex;
	/**
	 * Kolekcja krawędzi grafu
	 */
	private Collection<Object> edges;
	/**
	 * ilość wierzchołków grafu
	 */
	private int vertexCount;
	/**
	 * Populacja chromosomów
	 */
	private Population population;

	private Random random = new Random();
	private ExecutorService taskExecutor;
	private CompletionService<Chromosome> taskCompletionService;

	/**
	 * Konstruktor
	 * 
	 * @param graph
	 *            na którym będzie działał algorytm
	 */
	public GenericGraphColoring(Graph<Object, Object> graph) {
		this.graph = graph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preProcess() {
		vertex = graph.getVertices().toArray();
		Arrays.sort(vertex);
		vertexCount = vertex.length;
		edges = graph.getEdges();
		int tmp;
		for (Object o : vertex) {
			tmp = graph.getNeighborCount(o);
			if (tmp > colorLimit)
				colorLimit = tmp;
		}
		colorLimit += 1;

		taskExecutor = Executors.newFixedThreadPool(properties.threads);
		taskCompletionService = new ExecutorCompletionService<Chromosome>(taskExecutor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void startPopulation() {
		population = new Population(properties.populationSize);
		for (int count = 0; count < properties.populationSize; count++) {
			Chromosome ch = new Chromosome(vertexCount);
			for (int i = 0; i < vertexCount; i++) {
				ch.addColoring(i, random.nextInt(colorLimit));
			}
			population.add(ch);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean breakCondition() {
		iterationsCounter++;
		return iterationsCounter > properties.iterationsLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void fitness() {
		int taskCounter = 0;
		for (Chromosome ch : population) {
			if (!ch.isCounted()) {
				taskCompletionService.submit(new DoCountFitness(ch));
				taskCounter++;
			}
		}
		while (taskCounter > 0) {
			try {
				taskCompletionService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			taskCounter--;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void crossover() {
		int taskCounter = properties.populationSize;
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
		population = new Population(properties.populationSize);
		for (int i = 0; i < taskCounter; i++) {
			population.add(childs[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void mutate() {
		int taskCounter = 0;
		for (Chromosome ch : population) {
			if (properties.mutator.mutate(ch)) {
				taskCompletionService.submit(new DoMutate(ch));
				taskCounter++;
			}
		}
		while (taskCounter > 0) {
			try {
				taskCompletionService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			taskCounter--;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess() {
		Chromosome ch = properties.resultSelector.selectResult(population);
		System.out.println("Best child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors()
				+ " badEdges=" + ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
		clean();
	}

	/**
	 * Funkcja czyszcząca
	 */
	private void clean() {
		taskExecutor.shutdown();
	}

	/**
	 * Funkcja do obliczenia przystosowania jednego Chromosomu.
	 * 
	 * @param ch
	 *            - chromosom dla którego trzeba obliczyć przystosowanie
	 */
	private void fitnessFunction(Chromosome ch) {
		int fit = properties.badEdgeWeight * countBadEdges(ch) + properties.colorsUsedWeight * countColors(ch);
		ch.setFitness(fit);
	}

	/**
	 * Funkcja która wybiera rodziców uzywając {@link ParentSelector} i generuje potomka używając {@link Crossover}
	 * 
	 * @see #setParentSelector(ParentSelector)
	 * @see #setCrossover(Crossover)
	 * @return wygenerowany potomek
	 */
	private final Chromosome crossoverFunction() {
		Pair<Chromosome> parents = properties.parentSelector.selectParents(population);
		return properties.crossover.crossoverFunction(parents.getFirst(), parents.getSecond());
	}

	/**
	 * Oblicza ilosc kolorów użytych w chromosomie
	 * 
	 * @param ch
	 *            - chromosom dla ktorego mamy obliczyc ilosc kolorów
	 * @return ilość kolorów w chromosomie
	 */
	private final int countColors(Chromosome ch) {
		if(ch.getColors()>=0)
			return ch.getColors();
		int colors = 0;
		boolean tab[] = new boolean[ch.size()];
		for (int i : ch.getColoringTab()) {
			tab[i] = true;
		}
		for (boolean b : tab) {
			if (b)
				colors++;
		}
		ch.setColors(colors);
		return colors;
	}

	/**
	 * Oblicza ilosc złych krawędzi w chromosomie
	 * 
	 * @param ch
	 *            - chromosom dla ktorego mamy obliczyc ilosc złych krawędzi
	 * @return ilość złych krawędzi w chromosomie
	 */
	private final int countBadEdges(Chromosome ch) {
		if(ch.getBadEdges()>=0)
			return ch.getBadEdges();
		int badEdges = 0;
		int index1;
		int index2;
		for (Object e : edges) {
			Pair<Object> vert = graph.getEndpoints(e);
			index1 = Arrays.binarySearch(vertex, vert.getFirst());
			index2 = Arrays.binarySearch(vertex, vert.getSecond());
			if (ch.get(index1) == (ch.get(index2)))
				badEdges++;
		}
		ch.setBadEdges(badEdges);
		return badEdges;
	}

	/**
	 * Ustawia parametr - <b>wielkość populacji</b>
	 * 
	 * @param populationSize
	 */
	public void setPopulationSize(int populationSize) {
		properties.populationSize = populationSize;
	}

	/**
	 * Ustawia parametr - <b>wagę złych krawędzi</b> do funkcji oceny Chromosomu
	 * 
	 * @param badEdgeWeight
	 */
	public void setBadEdgeWeight(int badEdgeWeight) {
		properties.badEdgeWeight = badEdgeWeight;
	}

	/**
	 * Ustawia parametr - <b>wagę ilości uzytych kolorów</b> do funkcji oceny Chromosomu
	 * 
	 * @param colorsUsedWeight
	 */
	public void setColorsUsedWeight(int colorsUsedWeight) {
		properties.colorsUsedWeight = colorsUsedWeight;
	}

	/**
	 * Ustawia parametr - <b>limit iteracji</b>
	 * 
	 * @param iterationsLimit
	 */
	public void setIterationsLimit(int iterationsLimit) {
		properties.iterationsLimit = iterationsLimit;
	}

	/**
	 * Ustawia parametr - <b>ilość użytych wątków</b>
	 * 
	 * @param threads
	 */
	public void setThreads(int threads) {
		properties.threads = threads;
	}

	/**
	 * Ustawia parametr - <b>ParentSelector</b> używany do wybierania rodziców podczas generowania potomków
	 * 
	 * @see ParentSelector
	 * @param parentSelector
	 */
	public void setParentSelector(ParentSelector parentSelector) {
		properties.parentSelector = parentSelector;
	}

	/**
	 * Ustawia parametr - <b>Crossover</b> używany do generowania potomków
	 * 
	 * @see Crossover
	 * @param crossover
	 */
	public void setCrossover(Crossover crossover) {
		properties.crossover = crossover;
	}

	/**
	 * Ustawia parametr - <b>Mutator</b> używany mutowania potomków
	 * 
	 * @see Mutator
	 * @param mutator
	 */
	public void setMutator(Mutator mutator) {
		properties.mutator = mutator;
	}

	/**
	 * Ustawia graf na którym ma działać algorytm
	 * 
	 * @param graph
	 */
	public void setGraph(Graph<Object, Object> graph) {
		if (graph == null)
			throw new NullPointerException("Graph cannot be null.");
		this.graph = graph;
	}

	public static void main(String[] args) {
		Graph<Object, Object> graph = new UndirectedSparseGraph<Object, Object>();
		int n = 10;
		for (int i = 0; i < n; i++) {
			graph.addVertex(Integer.valueOf(i));
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				graph.addEdge(Integer.valueOf(n * i + j), Integer.valueOf(i), Integer.valueOf(j));
			}
		}
		long start = System.currentTimeMillis();
		GenericGraphColoring gcp = new GenericGraphColoring(graph);
		gcp.run();
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + (time / 1000) + "s");
	}
}