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
	private String filename;
	private Graph<Object, Object> graph;
	private int vertices;
	
	public DimacsParser(String filename) {
		this.filename = filename;
		graph = new UndirectedSparseGraph<Object, Object>();
	}
	
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
	
	public Graph<Object, Object> getGraph() {
		return graph;
	}
	
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
