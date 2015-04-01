package pl.edu.agh.gcp.population;

public class Chromosome {
    /**
     * ocena kolorowania grafu. Zawsze >=0, a -1 ozncza ze jeszcze nie została obliczon
     */
    int fitness = -1;
    
    public boolean isCounted(){
	return fitness>=0;
    }
    
    public void setFitness(int fitness){
	this.fitness = fitness;
    }
}
