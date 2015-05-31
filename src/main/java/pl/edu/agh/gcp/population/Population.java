package pl.edu.agh.gcp.population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Populacja chromosomów
 * @see Chromosome
 * @author daniel
 *
 */
public class Population implements Iterable<Chromosome> {
    /**
     * populacja - lista chromosomów
     */
    private ArrayList<Chromosome> population;
    /**
     * średnia przystosowania populacji
     */
    double mean=0;
    /**
     * odchylenie standardowe przystosowania populacji
     */
    double sd=0;

    /**
     * Konstruktor na podstawie kolekcji chromosomów
     * @param chromosomy
     */
    public Population(Collection<Chromosome> chromosomes){
    	population = new ArrayList<Chromosome>();
    	for(Chromosome ch : chromosomes){
    		population.add(ch);
    	}
    }
    
    /**
     * Konstruktor
     * @param size - wielkośc populacji
     */
    public Population(int size) {
    	population = new ArrayList<Chromosome>(size);
    }
    
    public Population(){
    	population=new ArrayList<Chromosome>();
    }

    /**
     * Dodaj chromosom do populacji
     * @param chromosom
     */
    public void add(Chromosome chromosome) {
    	population.add(chromosome);
    }

    /**
     * Dodaje do populacji wszytkie chromosomy które są w podanej populacji
     * @param other populacja
     */
    public void addAll(Population other){
    	population.addAll(other.population);
    }
    
    /**
     * Zwraca n-ty chromosom
     * @param index
     * @return chromosom
     */
    public Chromosome get(int index) {
	return population.get(index);
    }

    /**
     * Usuwa chromosom o podanym indeksie
     * @param index
     * @return usiniety chromosom
     */
    public Chromosome remove(int index) {
	return population.remove(index);
    }
    
    /**
     * wielskosc populacji
     * @return
     */
    public int size(){
	return population.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Chromosome> iterator() {
	return population.iterator();
    }

    /**
     * Oblicza średnią i odchylenie standardowe
     */
    public void countMeanSD(){
    	mean=0;
    	sd=0;
    	double tmp;
    	for(Chromosome ch : population){
    		mean+=ch.getFitness();
    	}
    	mean/=size();
    	for(Chromosome ch : population){
    		tmp=(mean-ch.getFitness());
    		sd+=(tmp*tmp);
    	}
    	sd=Math.sqrt(sd/(size()-1));
    }
    
    /**
     * Średnia. Trzeba najpierw policzyc - {@link #countMeanSD()}
     * @return średnia
     */
    public double getMean(){
    	return mean;
    }
    
    /**
     * Odchylenie standardowe. Trzeba najpierw policzyc - {@link #countMeanSD()}
     * @return odchylenie standardowe
     */
    public double getStandardDeviation(){
    	return sd; 	
    }
    
    /**
     * Sortuje populacje. Chromosom najlepszy (z najmniejszym przystosowaniem) będzie pierwszy.
     */
    public void sort(){
    	population.sort(new ChromosomeComparator());
    }
    
    /**
     * Zwraca pod-populacje
     * @param fromIndex - indeks początku (włącznie)
     * @param toIndex - indeks końca
     * 
     * @return pod-populacja
     */
    public Population subPopulation(int fromIndex, int toIndex){
    	Population subPop = new Population(toIndex-fromIndex);
    	for(int i =fromIndex;i<toIndex;i++){
    		subPop.add(population.get(i));
    	}
    	return subPop;
    }
}
