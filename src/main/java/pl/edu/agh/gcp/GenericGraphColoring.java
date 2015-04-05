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

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
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
    private class CountFitness implements Callable<Chromosome> {
	private Chromosome ch;

	public CountFitness(Chromosome ch) {
	    this.ch = ch;
	}

	@Override
	public Chromosome call() throws Exception {
	    ch.setFitness(GenericGraphColoring.this.fitness(ch));
	    return null;
	}

    }

    /**
     * Klasa wewnętrzna do wielowątkowego rozmnażania populacji
     * 
     * @author Daniel Tyka
     * @version 1.0
     */
    private class Crossover implements Callable<Chromosome> {

	@Override
	public Chromosome call() throws Exception {
	    return GenericGraphColoring.this.crossoverFunction();
	}

    }

    /**
     * Wielkość populacji
     */
    private int populationSize;
    /**
     * Limit ilości użytych kolorów (numeracja kolorów od 0)
     */
    private int colorLimit = 0;
    /**
     * Waga krawędzi łączących wierzchołki o takim samym kolorze - funkcja oceniająca chromosomy
     */
    private int badEdgeWeight;
    /**
     * Waga ilości użytych kolorów - funkcja oceniająca chromosomy
     */
    private int colorsUsedWeight;
    /**
     * Limit iteracji
     */
    private int iterationsLimit;
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
     * Zbiór krawędzi grafu
     */
    private Collection<Object> edges;
    /**
     * ilość wierzchołków grafu
     */
    private int vertexCount;

    private Population population;

    private Random random = new Random();
    private ExecutorService taskExecutor;
    private CompletionService<Chromosome> taskCompletionService;

    public GenericGraphColoring(Graph<Object, Object> graph, int populationSize, int iterationsLimit, int badEdgeWeight, int colorsUsedWeight) {
	this.graph = graph;

	this.populationSize = populationSize;
	this.badEdgeWeight = badEdgeWeight;
	this.colorsUsedWeight = colorsUsedWeight;
	this.iterationsLimit = iterationsLimit;
    }

    public GenericGraphColoring(Graph<Object, Object> graph) {
	this(graph, 100, 30000, 5, 2);
    }

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
	
	taskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	taskCompletionService = new ExecutorCompletionService<Chromosome>(taskExecutor);
    }

    @Override
    protected void startPopulation() {
	population = new Population(populationSize);
	for (int count = 0; count < populationSize; count++) {
	    Chromosome ch = new Chromosome(vertexCount);
	    for (int i = 0; i < vertexCount; i++) {
		ch.addColoring(i, random.nextInt(colorLimit));
	    }
	    population.add(ch);
	}
    }

    @Override
    protected boolean breakCondition() {
	iterationsCounter++;
	return iterationsCounter > iterationsLimit;
    }

    @Override
    protected void fitnessFunction() {
	int taskCounter = 0;
	for (Chromosome ch : population) {
	    if (!ch.isCounted()) {
		taskCompletionService.submit(new CountFitness(ch));
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

    @Override
    protected void crossover() {
	int taskCounter = populationSize;
	Chromosome[] childs = new Chromosome[taskCounter];
	for (int i = 0; i < taskCounter; i++) {
	    taskCompletionService.submit(new Crossover());
	}
	for (int i = 0; i < taskCounter; i++) {
	    try {
		childs[i] = taskCompletionService.take().get();
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}
	population=new Population(populationSize);
	for (int i = 0; i < taskCounter; i++) {
	    population.add(childs[i]);
	}
    }

    @Override
    protected void mutate() {
	// TODO Auto-generated method stub

    }

    @Override
    protected void postProcess() {
	int index=0;
	int min=population.get(0).getFitness();
	int tmp;
	for(int i=1;i<populationSize;i++){
	    tmp=population.get(i).getFitness();
	    if(tmp<min){
		min=tmp;
		index=i;
	    }
	}
	Chromosome ch = population.get(index);
	System.out.println("Best child found: fitness="+min+" colorsUsed="+ch.getColors()+" badEdges="+ch.getBadEdges()+" coloring="+Arrays.toString(ch.getColoringTab()));
	clean();
    }

    private int fitness(Chromosome ch) {
	return badEdgeWeight * countBadEdges(ch) + colorsUsedWeight * countColors(ch);
    }

    private final Chromosome crossoverFunction() {
	Chromosome[] tab = new Chromosome[5];
	for (int i = 0; i < 5; i++) {
	    tab[i] = population.get(random.nextInt(populationSize));
	}
	Arrays.sort(tab);
	// Najlepsze chromosomy z wylosowanej grupy. W przypadku gdy wylosuje sie wiecej niz 2 o takim samym prystosowaniu powinno wybrac losowe ale mi sie nie chce pisac tego xD
	Chromosome parent1 = tab[0];
	Chromosome parent2 = tab[1];
	if (random.nextInt(2) == 1) { // Swap or not to swap. That is The Question.
	    Chromosome tmp = parent1;
	    parent1 = parent2;
	    parent2 = tmp;
	}
	int split1 = random.nextInt(vertexCount);
	int split2 = random.nextInt(vertexCount);
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
	child.setBadEdges(-1);
	child.setColors(-1);
	child.setFitness(-1);
	for (int i = split1; i <= split2; i++) {
	    child.addColoring(i, parent2.get(i));
	}
	return child;
    }

    private final int countColors(Chromosome ch) {
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

    private final int countBadEdges(Chromosome ch) {
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
    
    private void clean(){
	taskExecutor.shutdown();
    }

    public static void main(String[] args) {
	Graph<Object, Object> graph = new UndirectedSparseGraph<Object, Object>();
	for (int i = 0; i < 10; i++) {
	    graph.addVertex(Integer.valueOf(i));
	    if (i != 0)
		graph.addEdge(Integer.valueOf(i), Integer.valueOf(i), Integer.valueOf(0));
	}
	System.out.println("Processors used: "+Runtime.getRuntime().availableProcessors());
	long start = System.currentTimeMillis();
	GenericGraphColoring gcp = new GenericGraphColoring(graph, 1000, 200, 5, 2);
	gcp.gaRun();
	long time = System.currentTimeMillis()-start;
	System.out.println("Time: "+(time/1000)+"s");
    }

}
