package pl.edu.agh.gcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import pl.edu.agh.gcp.crossover.Crossover;
import pl.edu.agh.gcp.dimacs.DimacsParser;
import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.mutator.FixBadEdgesImproved;
import pl.edu.agh.gcp.mutator.Mutator;
import pl.edu.agh.gcp.mutator.RandomMutator;
import pl.edu.agh.gcp.parentSelector.ParentSelector;
import pl.edu.agh.gcp.population.Chromosome;
import pl.edu.agh.gcp.populationGenerator.PopulationGenerator;
import edu.uci.ics.jung.graph.Graph;

public class StageGraphColoring extends GenericGraphColoring {

	/**
	 * Obserwator statystyk wszytskich etapów
	 * 
	 * @author Daniel Tyka
	 *
	 */
	private class StageStatsObserver implements Observer {
		@Override
		public void update(Observable o, Object arg) {
			Stats s = (Stats) arg;
			s.setIteration(iterationsCounter);
			iterationsCounter++;
			StageGraphColoring.this.observableStats.setStats(s);
		}
	}

	/**
	 * Obserwator wyników z każdego etapu
	 * @author daniel
	 *
	 */
	private class StageResultObserver implements Observer {

		@Override
		public void update(Observable o, Object arg) {
			Chromosome ch = (Chromosome) arg;
			raport.add("Child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors() + " badEdges="
						+ ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
			StageGraphColoring.this.stageResult = ch;
		}

	}

	/**
	 * Licznik iteracji
	 */
	protected int iterationsCounter = 0;
	
	/**
	 * Raport z działania algorytmu w postaci listy stringów. Umieszcza w nim wyniki poszczególnych etapów
	 */
	private ArrayList<String> raport;

	/**
	 * Poprzedni wynik
	 */
	private Chromosome lastResult=null;
	/**
	 * Wynik działania aktualnego etapu
	 */
	private Chromosome stageResult=null;
	/**
	 * Ostateczny wynik algorytmu
	 */
	private Chromosome result=null;

	/**
	 * Etapy algorytmu
	 */
	private GenericGraphColoring stage1;
	private GenericGraphColoring stage2;
	private GenericGraphColoring stage3;

	public StageGraphColoring(Graph<Object, Object> graph) {
		super(graph);
		Observer statsObserver = this.new StageStatsObserver();
		Observer resultObserver = this.new StageResultObserver();
		stage1 = new GenericGraphColoring(graph);
		stage1.addStatsObserver(statsObserver);
		stage1.addResultObserver(resultObserver);
		stage2 = new GenericGraphColoring(graph);
		stage2.addStatsObserver(statsObserver);
		stage2.addResultObserver(resultObserver);
		stage3 = new GenericGraphColoring(graph);
		stage3.addStatsObserver(statsObserver);
		stage3.addResultObserver(resultObserver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preProcess() {
		int colorLimit;
		iterationsCounter = 0;
		result=lastResult=stageResult=null;
		raport=new ArrayList<String>();
		
		
		raport.add("Stage1"); // Pierwsze przyliżenie
		stage1.unsetColorLimit();
		stage1.run();
		if(stageResult.getBadEdges()>0){
			raport.add("Stage1 warning: Nie znaleziono rozwiązania bez błędów - zwiększ populacje lub/i limit iteracji");
		}
		
		
		raport.add("Stage2"); // Szybkie szukanie
		do{
			lastResult=stageResult;
			colorLimit=stageResult.getColors();
			stage2.setColorLimit(colorLimit-1); //Próba znalezienia rozwiązania z mniejsza liczbą kolorów
			stage2.run();
		}while(stageResult.getBadEdges()==0);
		stageResult=lastResult; // Cofnięcie się do poprzendiego rozwiązania	
		colorLimit=lastResult.getColors();
		
		raport.add("Stage3"); // Dokładniejsze szukanie
		do{
			lastResult=stageResult;
			colorLimit=stageResult.getColors();
			stage3.setColorLimit(colorLimit-1); //Próba znalezienia rozwiązania z mniejsza liczbą kolorów
			stage3.run();
		}while(stageResult.getBadEdges()==0);
		result=lastResult;
		raport.add("End.");
		raport.add("Final result: fitness=" + result.getFitness() + " colorsUsed=" + result.getColors() + " badEdges="
				+ result.getBadEdges() + " coloring=" + Arrays.toString(result.getColoringTab()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void startPopulation() {
	}

	/**
	 * Zawsze zwraca true {@inheritDoc}
	 */
	@Override
	protected boolean breakCondition() {
		return true;
	}

	/**
	 * Pusta implementacja {@inheritDoc}
	 */
	@Override
	protected void fitness() {
	}

	/**
	 * Pusta implementacja {@inheritDoc}
	 */
	@Override
	protected void crossover() {
	}

	/**
	 * Pusta implementacja {@inheritDoc}
	 */
	@Override
	protected void mutate() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess() {
		observableResult.setResult(result);
	}

	/**
	 * Pusta implementacja. Nic nie robi
	 * 
	 * @param populationSize
	 */
	public void setPopulationSize(int populationSize) {
	}

	/**
	 * Ustawia parametr - <b>wagę złych krawędzi</b> do funkcji oceny Chromosomu
	 * 
	 * @param badEdgeWeight
	 */
	public void setBadEdgeWeight(int badEdgeWeight) {
		super.setBadEdgeWeight(badEdgeWeight);
		stage1.setBadEdgeWeight(badEdgeWeight);
		stage2.setBadEdgeWeight(badEdgeWeight);
		stage3.setBadEdgeWeight(badEdgeWeight);
	}

	/**
	 * Ustawia parametr - <b>wagę ilości uzytych kolorów</b> do funkcji oceny Chromosomu
	 * 
	 * @param colorsUsedWeight
	 */
	public void setColorsUsedWeight(int colorsUsedWeight) {
		super.setColorsUsedWeight(colorsUsedWeight);
		stage1.setColorsUsedWeight(colorsUsedWeight);
		stage2.setColorsUsedWeight(colorsUsedWeight);
		stage3.setColorsUsedWeight(colorsUsedWeight);
	}

	/**
	 * Pusta implementacja. Nic nie robi
	 * 
	 * @param iterationsLimit
	 */
	public void setIterationsLimit(int iterationsLimit) {
	}

	/**
	 * Ustawia parametr - <b>ilość użytych wątków</b>
	 * 
	 * @param threads
	 */
	public void setThreads(int threads) {
		super.setThreads(threads);
		stage1.setThreads(threads);
		stage2.setThreads(threads);
		stage3.setThreads(threads);
	}

	/**
	 * Ustawia parametr - <b>ParentSelector</b> używany do wybierania rodziców podczas generowania potomków
	 * 
	 * @see ParentSelector
	 * @param parentSelector
	 */
	public void setParentSelector(ParentSelector parentSelector) {
		super.setParentSelector(parentSelector);
		stage1.setParentSelector(parentSelector);
		stage2.setParentSelector(parentSelector);
		stage3.setParentSelector(parentSelector);

	}

	/**
	 * Ustawia parametr - <b>Crossover</b> używany do generowania potomków
	 * 
	 * @see Crossover
	 * @param crossover
	 */
	public void setCrossover(Crossover crossover) {
		super.setCrossover(crossover);
		stage1.setCrossover(crossover);
		stage2.setCrossover(crossover);
		stage3.setCrossover(crossover);
	}

	/**
	 * Pusta implementacja. Nic nie robi
	 */
	public void addMutator(Mutator mutator) {
	}

	/**
	 * Pusta implementacja. Zwraca zawsze null
	 * 
	 * @return zawsze null
	 */
	public ArrayList<Mutator> getMutators() {
		return null;
	}

	/**
	 * Ustawia parametr - <b>PopulationGenerator</b> używany generowania populacji startowej
	 * 
	 * @see PopulationGenerator
	 * @param populationGenerator
	 */
	public void setPopulationGenerator(PopulationGenerator populationGenerator) {
		super.setPopulationGenerator(populationGenerator);
		stage1.setPopulationGenerator(populationGenerator);
		stage2.setPopulationGenerator(populationGenerator);
		stage3.setPopulationGenerator(populationGenerator);
	}

	/**
	 * Ustawia graf na którym ma działać algorytm
	 * 
	 * @param graph
	 */
	public void setGraph(Graph<Object, Object> graph) {
		super.setGraph(graph);
		stage1.setGraph(graph);
		stage2.setGraph(graph);
		stage3.setGraph(graph);
	}

	/**
	 * Pusta implementacja. Nic nie robi
	 * 
	 * @param colorLimit
	 */
	public void setColorLimit(int colorLimit) {
	}

	/**
	 * Pusta implementacja. Nic nie robi
	 */
	public void unsetColorLimit() {
	}

	/**
	 * Dodaje obserwatora wyniku
	 * 
	 * @param obserwator
	 */
	public void addResultObserver(Observer observer) {
		observableResult.addObserver(observer);
	}

	/**
	 * Dodaje obserwatora statystyk
	 * 
	 * @param obserwator
	 */
	public void addStatsObserver(Observer observer) {
		observableStats.addObserver(observer);
	}

	/**
	 * Ustawia limit iteracji w pierwszym etapie algorytmu
	 * 
	 * @param iterationsLimit
	 *                - limit iteracji
	 */
	public void setStage1_IterationsLimit(int iterationsLimit) {
		if (iterationsLimit < 0)
			throw new IllegalArgumentException("iterationsLimit cannot be less than 0.");
		stage1.setIterationsLimit(iterationsLimit);
	}

	/**
	 * Ustawia limit iteracji w drugim etapie algorytmu
	 * 
	 * @param iterationsLimit
	 *                - limit iteracji
	 */
	public void setStage2_IterationsLimit(int iterationsLimit) {
		if (iterationsLimit < 0)
			throw new IllegalArgumentException("iterationsLimit cannot be less than 0.");
		stage2.setIterationsLimit(iterationsLimit);
	}

	/**
	 * Ustawia limit iteracji w trzecim etapie algorytmu
	 * 
	 * @param iterationsLimit
	 *                - limit iteracji
	 */
	public void setStage3_IterationsLimit(int iterationsLimit) {
		if (iterationsLimit < 0)
			throw new IllegalArgumentException("iterationsLimit cannot be less than 0.");
		stage3.setIterationsLimit(iterationsLimit);
	}

	/**
	 * Ustawia wielkość populacji w pierwszym etapie algorytmu
	 * 
	 * @param populationSize
	 *                - wielkość populacji
	 */
	public void setStage1_PopulationSize(int populationSize) {
		if (populationSize < 2)
			throw new IllegalArgumentException("populationSize cannot be less than 2.");
		stage1.setPopulationSize(populationSize);
	}

	/**
	 * Ustawia wielkość populacji w pierwszym etapie algorytmu
	 * 
	 * @param populationSize
	 *                - wielkość populacji
	 */
	public void setStage2_PopulationSize(int populationSize) {
		if (populationSize < 2)
			throw new IllegalArgumentException("populationSize cannot be less than 2.");
		stage1.setPopulationSize(populationSize);
	}

	/**
	 * Ustawia wielkość populacji w pierwszym etapie algorytmu
	 * 
	 * @param populationSize
	 *                - wielkość populacji
	 */
	public void setStage3_PopulationSize(int populationSize) {
		if (populationSize < 2)
			throw new IllegalArgumentException("populationSize cannot be less than 2.");
		stage1.setPopulationSize(populationSize);
	}

	/**
	 * Dodaje mutator w pierwszym etapie algorytmu
	 * 
	 * @param mutator
	 */
	public void addStage1_Mutator(Mutator mutator) {
		if (mutator == null)
			throw new NullPointerException("mutator cannot be null.");
		stage1.addMutator(mutator);
	}

	/**
	 * Dodaje mutator w drugim etapie algorytmu
	 * 
	 * @param mutator
	 */
	public void addStage2_Mutator(Mutator mutator) {
		if (mutator == null)
			throw new NullPointerException("mutator cannot be null.");
		stage2.addMutator(mutator);
	}

	/**
	 * Dodaje mutator w trzecim etapie algorytmu
	 * 
	 * @param mutator
	 */
	public void addStage3_Mutator(Mutator mutator) {
		if (mutator == null)
			throw new NullPointerException("mutator cannot be null.");
		stage3.addMutator(mutator);
	}

	/**
	 * Zwraca raport z przebiegu działania algorytmu.
	 * @return raport
	 */
	public ArrayList<String> getRaport(){
		return raport;
	}
	
	public static void main(String[] args) {
		DimacsParser test = new DimacsParser(GenericGraphColoring.class.getClassLoader().getResource("queen7_7.col").getPath());

		try {
			test.load();
		} catch (Exception e) {
			e.printStackTrace();
		}

		long start = System.currentTimeMillis();
		StageGraphColoring scp = new StageGraphColoring(test.getGraph());
		
		scp.setStage1_PopulationSize(200);
		scp.setStage1_IterationsLimit(300);
		scp.addStage1_Mutator(new RandomMutator(40, 100));
		scp.addStage1_Mutator(new ColorUnifier2(50, 100));
		
		scp.setStage2_PopulationSize(100);
		scp.setStage2_IterationsLimit(200);
		scp.addStage2_Mutator(new RandomMutator(35, 100));
		scp.addStage2_Mutator(new FixBadEdgesImproved(80, 100));
		scp.addStage2_Mutator(new ColorUnifier2(50, 100));
		
		scp.setStage3_PopulationSize(500);
		scp.setStage3_IterationsLimit(500);
		scp.addStage3_Mutator(new RandomMutator(50, 100));
		scp.addStage3_Mutator(new FixBadEdgesImproved(70, 100));
		scp.addStage3_Mutator(new ColorUnifier2(50, 100));
		

		scp.setBadEdgeWeight(5);
		scp.setColorsUsedWeight(2);
		scp.addResultObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Chromosome ch = (Chromosome) arg;
				System.out.println("Best child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors() + " badEdges="
						+ ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
			}
		});
		scp.addStatsObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Stats s = (Stats) arg;
				System.out.println("iteration: " + s.getIteration() + " min: " + s.getMin() + " avg: " + s.getAvg() + " max: "
						+ s.getMax());
			}
		});
		scp.run();
		for(String s : scp.getRaport()){
			System.out.println(s);
		}
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + (time / 1000) + "s");
	}
}
