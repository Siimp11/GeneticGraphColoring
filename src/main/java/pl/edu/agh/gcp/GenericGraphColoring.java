package pl.edu.agh.gcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.agh.gcp.crossover.Crossover;
import pl.edu.agh.gcp.crossover.DefaultCrossover;
import pl.edu.agh.gcp.dimacs.DimacsParser;
import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.mutator.Mutator;
import pl.edu.agh.gcp.mutator.RandomMutator;
import pl.edu.agh.gcp.parentSelector.DefaultParentSelector;
import pl.edu.agh.gcp.parentSelector.ParentSelector;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import pl.edu.agh.gcp.populationGenerator.PopulationGenerator;
import pl.edu.agh.gcp.populationGenerator.RandomPopulation;
import pl.edu.agh.gcp.resultSelector.DefaultResultSelector;
import pl.edu.agh.gcp.resultSelector.ResultSelector;
import edu.uci.ics.jung.graph.Graph;
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
		private Mutator m;

		public DoMutate(Chromosome ch, Mutator m) {
			this.ch = ch;
			this.m=m;
		}

		@Override
		public Chromosome call() throws Exception {
			m.mutateFunction(ch,GenericGraphColoring.this.colorLimit, GenericGraphColoring.this.graph, GenericGraphColoring.this.vertex,
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
		 * Lista Mutatorów to mutowania chromosomow - wykonywane po kolei na każdym chromosomie
		 */
		public ArrayList<Mutator> mutatorsList = new ArrayList<Mutator>();
		/**
		 * ResultSelector do wybierania wyniku
		 */
		public ResultSelector resultSelector = new DefaultResultSelector();
		/**
		 * Generator populacji startowej
		 */
		public PopulationGenerator populationGenerator = new RandomPopulation();
	}

	/**
	 * Klasa do obserwowania wyniku działania algorytmu. Jak skonczy się działanie algorytmu informuje o wyniku. Jako wynik zwraca {@link Chromosome}
	 * @author Daniel Tyka
	 *
	 */
	private static class ObservableResult extends Observable{
		/**
		 * Ustawia wynik algorytmu i powiadamia obserwatorów
		 * @param wynik
		 */
		public void setResult(Chromosome result){
			setChanged();
			notifyObservers(result);
		}
	}

	/**
	 * Klasa do oserwowania statystyk (min,avg i max). W każdej iteracji informuje o aktualnych statystykach. Jako wynik zwraca {@link GenericGraphColoring.Stats}
	 * @author Daniel Tyka
	 * @version 1.0
	 *
	 */
	private static class ObservableStats extends Observable{
		/**
		 * Ustawia statystyki i informuje obserwujących
		 * @param statystyki
		 */
		public void setStats(Stats stats){
			setChanged();
			notifyObservers(stats);
		}
	}
	
	/**
	 * Klasa ze statystykami do rysowania wykresu. Zawiera minimum przystosowania, średnie przystosowanie i maksymalne.
	 * @author Daniel Tyka
	 * @version 1.0
	 *
	 */
	public static class Stats{
		/**
		 * numer iteracji
		 */
		private int iteration;
		/**
		 * minimalne przystosowanie
		 */
		private int min;
		/**
		 * średnie przystosowanie
		 */
		private int avg;
		/**
		 * maksymalne przystosowanie
		 */
		private int max;
		
		/**
		 * Konstruktor
		 * @param iteration
		 * @param min
		 * @param avg
		 * @param max
		 */
		public Stats(int iteration, int min, int avg, int max){
			this.iteration = iteration;
			this.min=min;
			this.avg=avg;
			this.max=max;
		}
		
		/**
		 * Zwraca {@link #iteration}
		 * @return numer iteracji
		 */
		public int getIteration(){
			return this.iteration;
		}
		
		/**
		 * Zwraca {@link #min}
		 * @return minimalne przystosowanie
		 */
		public int getMin(){
			return min;
		}
		
		/**
		 * Zwraca {@link #avg}
		 * @return średnie przystosowanie
		 */
		public int getAvg(){
			return avg;
		}
		
		/**
		 * Zwraca {@link #max}
		 * @return maksymalne przystosownaie
		 */
		public int getMax(){
			return max;
		}
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

	private ExecutorService taskExecutor;
	private CompletionService<Chromosome> taskCompletionService;
	private ObservableResult observableResult=new ObservableResult();
	private ObservableStats observableStats=new ObservableStats();

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
		iterationsCounter=0;
		vertex = graph.getVertices().toArray();
		Arrays.sort(vertex);
		vertexCount = vertex.length;
		edges = graph.getEdges();
		colorLimit=0;
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
		population=properties.populationGenerator.generatePopulation(properties.populationSize, vertexCount, colorLimit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean breakCondition() {
		if(observableStats.countObservers()>0){
			observableStats.setStats(countStats());
		}
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
		Iterator<Mutator> it = properties.mutatorsList.iterator();
		Mutator mutator;
		int taskCounter = 0;
		boolean changed = false;
		
		while(it.hasNext()){
			mutator = it.next();
			taskCounter = 0;
			changed=false;
			for (Chromosome ch : population) {
				if (mutator.mutate(ch)) {
					changed = true;
					taskCompletionService.submit(new DoMutate(ch,mutator));
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
			if(changed && it.hasNext()){
				fitness();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess() {
		Chromosome ch = properties.resultSelector.selectResult(population);
		observableResult.setResult(ch);
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
		for(int i=0;i<tab.length;i++)
			tab[i]=false;
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
			if (ch.get(index1)==(ch.get(index2)))
				badEdges++;
		}
		ch.setBadEdges(badEdges);
		return badEdges;
	}

	/**
	 * Funkcja obliczająca statystyki - minimalne, średnie i maksymalne przystosowanie
	 * @return statystyki
	 */
	private Stats countStats(){
		int min=0;
		int max=0;
		int avg=0;
		int count=0;
		Chromosome ch;
		Iterator<Chromosome> it = population.iterator();
		if(it.hasNext()){
			ch=it.next();
			avg=max=min=ch.getFitness();
			count=1;
		}
		while(it.hasNext()){
			ch=it.next();
			count++;
			avg+=ch.getFitness();
			if(min>ch.getFitness())
				min=ch.getFitness();
			if(max<ch.getFitness())
				max=ch.getFitness();
		}
		if(count>0)
			avg/=count;
		return new Stats(iterationsCounter, min, avg, max);
	}
	
	/**
	 * Ustawia parametr - <b>wielkość populacji</b>
	 * 
	 * @param populationSize
	 */
	public void setPopulationSize(int populationSize) {
		if (populationSize < 1)
			throw new IllegalArgumentException("populationSize cannot be less than 1.");
		properties.populationSize = populationSize;
	}

	/**
	 * Ustawia parametr - <b>wagę złych krawędzi</b> do funkcji oceny Chromosomu
	 * 
	 * @param badEdgeWeight
	 */
	public void setBadEdgeWeight(int badEdgeWeight) {
		if (badEdgeWeight < 0)
			throw new IllegalArgumentException("badEdgeWeight cannot be less than 0.");
		properties.badEdgeWeight = badEdgeWeight;
	}

	/**
	 * Ustawia parametr - <b>wagę ilości uzytych kolorów</b> do funkcji oceny Chromosomu
	 * 
	 * @param colorsUsedWeight
	 */
	public void setColorsUsedWeight(int colorsUsedWeight) {
		if (colorsUsedWeight < 0)
			throw new IllegalArgumentException("colorsUsedWeight cannot be less than 0.");
		properties.colorsUsedWeight = colorsUsedWeight;
	}

	/**
	 * Ustawia parametr - <b>limit iteracji</b>
	 * 
	 * @param iterationsLimit
	 */
	public void setIterationsLimit(int iterationsLimit) {
		if (iterationsLimit < 0)
			throw new IllegalArgumentException("iterationsLimit cannot be less than 0.");
		properties.iterationsLimit = iterationsLimit;
	}

	/**
	 * Ustawia parametr - <b>ilość użytych wątków</b>
	 * 
	 * @param threads
	 */
	public void setThreads(int threads) {
		if (threads < 1)
			throw new IllegalArgumentException("threads cannot be less than 1.");
		properties.threads = threads;
	}

	/**
	 * Ustawia parametr - <b>ParentSelector</b> używany do wybierania rodziców podczas generowania potomków
	 * 
	 * @see ParentSelector
	 * @param parentSelector
	 */
	public void setParentSelector(ParentSelector parentSelector) {
		if (parentSelector == null)
			throw new NullPointerException("parentSelector cannot be null.");
		properties.parentSelector = parentSelector;
	}

	/**
	 * Ustawia parametr - <b>Crossover</b> używany do generowania potomków
	 * 
	 * @see Crossover
	 * @param crossover
	 */
	public void setCrossover(Crossover crossover) {
		if (crossover == null)
			throw new NullPointerException("Crossover cannot be null.");
		properties.crossover = crossover;
	}

	/**
	 * Ustawia parametr - <b>Mutator</b> używany mutowania potomków
	 * 
	 * @see Mutator
	 * @param mutator
	 */
	public void addMutator(Mutator mutator) {
		if (mutator == null)
			throw new NullPointerException("mutator cannot be null.");
		properties.mutatorsList.add(mutator);
	}
	
	/**
	 * Zwraca liste mutatorów
	 * @return lista mutatorów
	 */
	public ArrayList<Mutator> getMutators(){
		return properties.mutatorsList;
	}
	
	/**
	 * Ustawia parametr - <b>PopulationGenerator</b> używany generowania populacji startowej
	 * @see PopulationGenerator
	 * @param populationGenerator
	 */
	public void setPopulationGenerator(PopulationGenerator populationGenerator){
		if (populationGenerator == null)
			throw new NullPointerException("populationGenerator cannot be null.");
		properties.populationGenerator=populationGenerator;
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

	/**
	 * Dodaje obserwatora wyniku
	 * @param obserwator
	 */
	public void addResultObserver(Observer observer){
		observableResult.addObserver(observer);
	}
	
	/**
	 * Dodaje obserwatora statystyk
	 * @param obserwator
	 */
	public void addStatsObserver(Observer observer){
		observableStats.addObserver(observer);
	}
	
	public static void main(String[] args) {
		/*Graph<Object, Object> graph = new UndirectedSparseGraph<Object, Object>();
		int n = 130;
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
		gcp.setMutator(new ColourUnifier());
		gcp.setPopulationGenerator(new UnifiedColorsPopulation());
		gcp.setPopulationSize(500);
		gcp.setBadEdgeWeight(5);
		gcp.setColorsUsedWeight(2);
		gcp.setIterationsLimit(200);
		gcp.run();
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + (time / 1000) + "s");*/
		
		DimacsParser test = new DimacsParser(GenericGraphColoring.class.getClassLoader().getResource("test.col").getPath());
		
		try {
			test.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		GenericGraphColoring gcp = new GenericGraphColoring(test.getGraph());
		
		gcp.addMutator(new RandomMutator(50, 100));
		gcp.addMutator(new ColorUnifier2(50, 100));
		gcp.setPopulationSize(500);
		gcp.setIterationsLimit(200);
		gcp.setBadEdgeWeight(5);
		gcp.setColorsUsedWeight(2);
		gcp.addResultObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Chromosome ch = (Chromosome)arg;
				System.out.println("Best child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors()
						+ " badEdges=" + ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
			}
		});
		gcp.addStatsObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Stats s = (Stats) arg;
				System.out.println("iteration: "+s.getIteration()+" min: "+s.getMin()+" avg: "+s.getAvg()+" max: "+s.getMax());
			}
		});
		gcp.run();
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + (time / 1000) + "s");
	}
}
