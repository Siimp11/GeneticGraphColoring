package pl.edu.agh.gcp.dimacs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * Parser plików w formacie DIMACS
 * @author JakubSzczepankiewicz
 *
 */
public class DimacsParser {
	/**
	 * Ścieżka do pliku
	 */
	private String filename;
	/**
	 * Wczytany graf
	 */
	private Graph<Object, Object> graph;
	/**
	 * Ilość wierzchołków
	 */
	private int vertices;
	
	/**
	 * Konstruktor
	 * @param filename - ścieżka do pliku w formacie DIMACS
	 */
	public DimacsParser(String filename) {
		this.filename = filename;
		graph = new UndirectedSparseGraph<Object, Object>();
	}
	
	/**
	 * Parsowanie pliku
	 * @return wczytany graf
	 * @throws Exception w przypadku niepoprawnego formatu lub błędu IOException
	 */
	public Graph<Object, Object> load() throws Exception{ //TODO IOException
		if(graph.getVertexCount() != 0) return null;
		
		File file = new File(filename);
		
		FileReader fileReader = null;
		BufferedReader reader = null;
		
		try {
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			String line = null;
			while((line = reader.readLine()) != null) {
				parseLine(line);
			}
		}finally {
			if(reader != null) reader.close();
			if(fileReader != null) fileReader.close();
		}
		
		return graph;
	}
	
	/**
	 * Zwraca wczytany graf
	 * @return graf
	 */
	public Graph<Object, Object> getGraph() {
		return graph;
	}
	
	/**
	 * Parsuje linię z pliku
	 * @param linia z pliku
	 * @throws Exception
	 */
	private void parseLine(String line) throws Exception{
		if(line.charAt(0) == 'c') return;
		else if(line.charAt(0) == 'p') {
			String[] data = line.split(" ");
			if(data.length != 4) throw new Exception("Invalid line: "+line); //TODO nie wiem jaki tu ma byc wyjatek xD Runtime?
			
			vertices = Integer.parseInt(data[2]);
			
			for(int i=0; i<vertices; i++){
				graph.addVertex(Integer.valueOf(i));
			}
			return;
			
		} else if(line.charAt(0) == 'e') {
			String[] data = line.split(" ");
			if(data.length != 3) throw new Exception("Invalid line: "+line); //TODO nie wiem jaki tu ma byc wyjatek xD Runtime?
			
			int e1 = Integer.parseInt(data[1]);
			int e2 = Integer.parseInt(data[2]);
			graph.addEdge(Integer.valueOf(vertices * e1 + e2), Integer.valueOf(e1), Integer.valueOf(e2));
		}
	}
}
