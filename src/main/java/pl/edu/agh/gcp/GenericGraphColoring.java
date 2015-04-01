package pl.edu.agh.gcp;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.population.Population;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GenericGraphColoring extends GeneticAlgorithm {
    /**
     * Klasa wewnętrzna do obliczenia wielowątkowo oceny chromosomów
     * @author Daniel Tyka
     * @version 1.0
     */
    private class CountFitness implements Callable<Chromosome>{
	private Chromosome ch;
	public CountFitness(Chromosome ch) {
	    this.ch=ch;
	}

	@Override
	public Chromosome call() throws Exception {
	    ch.setFitness(GenericGraphColoring.this.fitness(ch));
	    return null;
	}
	
    }
    /**
     * Wielkość populacji
     */
    private int populationSize;
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
    private Graph<Integer, Integer> graph;
    /**
     * Zbiór wierzchołków grafu
     */
    private Collection<Integer> vertex;
    /**
     * ilość wierzchołków grafu
     */
    private int vertexCount;

    private Population population;

    private Random random = new Random();
    private ExecutorService taskExecutor;
    private CompletionService<Chromosome> taskCompletionService;

    public GenericGraphColoring(Graph<Integer, Integer> graph, int populationSize, int iterationsLimit, int badEdgeWeight, int colorsUsedWeight,
	    int numberOfThreads) {
	this.graph = graph;
	vertex = graph.getVertices();
	vertexCount = vertex.size();
	this.populationSize = populationSize;
	this.badEdgeWeight = badEdgeWeight;
	this.colorsUsedWeight = colorsUsedWeight;
	this.iterationsLimit = iterationsLimit;
	taskExecutor = Executors.newFixedThreadPool(numberOfThreads);
	taskCompletionService = new ExecutorCompletionService<Chromosome>(taskExecutor);
    }
    
    public GenericGraphColoring(Graph<Integer, Integer> graph) {
	this(graph,100,20000,5,2,Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void startingPopulation() {
	population = new Population(populationSize);
	// TODO Auto-generated method stub

    }

    @Override
    protected boolean breakCondition() {
	iterationsCounter++;
	return iterationsCounter > iterationsLimit;
    }

    @Override
    protected void fitnessFunction() {
	int taskCounter=0;
	for(Chromosome ch : population){
	    if(!ch.isCounted()){
		taskCompletionService.submit(new CountFitness(ch));
		taskCounter++;
	    }
	}
	while(taskCounter>0){
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
	// TODO Auto-generated method stub

    }

    @Override
    protected void mutate() {
	// TODO Auto-generated method stub

    }

    @Override
    protected void postProcess() {
	// TODO Auto-generated method stub
    }

    private int fitness(Chromosome ch) {
	return badEdgeWeight*countBadEdges(ch) + colorsUsedWeight*countColors(ch);
    }
    
    private final int countColors(Chromosome ch){
	//TODO policzyc ilość użytych kolorów
	return 0;
    }
    
    private final int countBadEdges(Chromosome ch){
	// TODO policzyc ilość błędów
	return 0;
    }

    public static void main(String[] args) {
	Graph<Integer, Integer> graph = new UndirectedSparseGraph<Integer, Integer>();
	graph.addVertex(Integer.valueOf(1));
    }

}
