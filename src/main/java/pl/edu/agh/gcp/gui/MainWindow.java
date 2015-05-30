package pl.edu.agh.gcp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.collections15.Transformer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import pl.edu.agh.gcp.GenericGraphColoring;
import pl.edu.agh.gcp.GenericGraphColoring.Stats;
import pl.edu.agh.gcp.dimacs.DimacsParser;
import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.mutator.RandomMutator;
import pl.edu.agh.gcp.population.Chromosome;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
/**
 * 
 * @author JakubSzczepankiewicz
 *
 */
public class MainWindow extends JFrame{

	private static final long serialVersionUID = 1L;


	/**
	 * Obiekty swinga
	 */
	private JPanel contentPane;
	private JPanel topMenu;
	private JButton btnLoadGraph;
	private JButton btnStart;
	
	/**
	 * Obiekty związane z wizualizacją grafem
	 */
	private Layout<Object, Object> graphLayout;
	private BasicVisualizationServer<Object,Object> visualizationServer;
	
	/**
	 * Obiekty związane z wizualizacją wykresu
	 */
	private final String chartTitleString = "Wykres przystosowania od iteracji";
	private JFreeChart resultChart;
	private ChartPanel chartPanel;
	private XYSeriesCollection xySeriesCollection;
	private XYSeries seriesMin;
	private XYSeries seriesAvg;
	private XYSeries seriesMax;
	
	/**
	 * Obiekty związane z algorytmem
	 */
	private GenericGraphColoring graphColoring;			//algorytm
	private DimacsParser parser;						//parser plików dimacs
	private Graph<Object, Object> currentGraph=null;	//obecnie wyświetlany graf
	
	public MainWindow(){
        initialize();
        
		/*parser = new DimacsParser("test.col");
		try {
			parser.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentGraph = parser.getGraph();
		graphLayout.setGraph(currentGraph);*/
	}
	
	/**
	 * Inicjalizacja komponentów
	 */
	private void initialize(){
		setTitle("Kolorowanie wierzchołkowe grafu");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000,600);
		setLayout(null);
		
		topMenu = new JPanel();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		
		/**
		 * Przycisk do wczytywania grafu z pliku
		 */
		btnLoadGraph = new JButton("Wczytaj graf (DIMACS)");
		btnLoadGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//TODO
				JFileChooser c = new JFileChooser();
				
				c.setCurrentDirectory(new java.io.File("."));
				c.setDialogTitle("Wybierz plik w standardzie DIMACS");
				c.setFileSelectionMode(JFileChooser.FILES_ONLY);
				c.setAcceptAllFileFilterUsed(true);
				
				if(c.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION){
					File file = c.getSelectedFile();
					parser = new DimacsParser(file.getPath()); //TODO konstruktor z obiektem File
					try {
						parser.load();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currentGraph = parser.getGraph();
				graphLayout.setGraph(currentGraph);
				
				
				//TODO button
				clearChartData();
			}
		});
		btnLoadGraph.setSize(150,30);
		topMenu.add(btnLoadGraph);
		
		/**
		 * Przycisk uruchamiający algorytm
		 */
		btnStart = new JButton("Rozpocznij");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(currentGraph!=null){
					clearChartData();
					graphColoring = new GenericGraphColoring(currentGraph);
					graphColoring.addMutator(new RandomMutator(50, 100));
					graphColoring.addMutator(new ColorUnifier2(50, 100));
					graphColoring.setPopulationSize(500);
					graphColoring.setIterationsLimit(200);
					graphColoring.setBadEdgeWeight(5);
					graphColoring.setColorsUsedWeight(2);
					
					graphColoring.addResultObserver(new Observer() {
						@Override
						public void update(Observable o, Object arg) {
							Chromosome ch = (Chromosome)arg;
							//TODO wyskakujące okienko z wynikiem
							colorVisualisedGraph(ch.getColors(), ch.getColoringTab());
							System.out.println("Best child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors()
									+ " badEdges=" + ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
						}
					});
					graphColoring.addStatsObserver(new Observer() {
						@Override
						public void update(Observable o, Object arg) {
							Stats s = (Stats) arg;
							seriesMin.add(s.getIteration(), s.getMin());
							seriesAvg.add(s.getIteration(), s.getAvg());
							seriesMax.add(s.getIteration(), s.getMax());
							
							System.out.println("iteration: "+s.getIteration()+" min: "+s.getMin()+" avg: "+s.getAvg()+" max: "+s.getMax());
						}
					});
					
					graphColoring.run();
				}

			}
		});
		btnStart.setSize(150,30);
		topMenu.add(btnStart);
		
		contentPane.add(topMenu, BorderLayout.NORTH);
		/**
		 * Wyświetlanie grafu
		 */
		graphLayout = new SpringLayout(new UndirectedSparseGraph<Object, Object>());
		graphLayout.setSize(new Dimension(800,800)); // sets the initial size of the layout space
		visualizationServer = new BasicVisualizationServer<Object,Object>(graphLayout);
		visualizationServer.setPreferredSize(new Dimension(850,850)); //Sets the viewing area size
        contentPane.add(visualizationServer, BorderLayout.CENTER);
        
        /**
         * Wykres
         */
        chartPanel = createNewChart();
        contentPane.add(chartPanel, BorderLayout.EAST);
        
        pack();
	}
	
	//Czyszczenie danych na wykresie
	private void clearChartData(){
		seriesMin.clear(); 
		seriesAvg.clear();
		seriesMax.clear();
	}
	
	/**
	 * Transformer kolorów wierzchołków
	 * @author JakubSzczepankiewicz
	 *
	 */
	private static class VertexPaintTransformer implements Transformer<Object,Paint> {

        private final PickedInfo<Object> pi;
        private final Color[] palette;
        private final int[] coloringTab;
        
        VertexPaintTransformer (PickedInfo<Object> pi, int colorsUsed, int[] coloringTab ) { 
            super();
            if (pi == null)
                throw new IllegalArgumentException("PickedInfo instance must be non-null");
            this.pi = pi;
            
            palette = new Color[colorsUsed];
    		Random rand = new Random();
    		for(int i=0;i<colorsUsed;i++){
    			float r = rand.nextFloat();
    			float g = rand.nextFloat();
    			float b = rand.nextFloat();
    	    	palette[i]=new Color(r,g,b);
    	    }
    		this.coloringTab = coloringTab;
        }

        @Override
	    public Paint transform(Object i) {
	        return palette[coloringTab[(Integer)i]];
	    }
    }
	
	/**
	 * Metoda kolorująca wierzchołki obecnie wyświetlanego grafu
	 * @param colorsUsed - ilość użytych kolorów
	 * @param coloringTab - tablica kolorów posortowanych wierzchołków
	 */
	private void colorVisualisedGraph(int colorsUsed, final int[] coloringTab){
		visualizationServer.getRenderContext().setVertexFillPaintTransformer(new VertexPaintTransformer(visualizationServer.getPickedVertexState(), colorsUsed, coloringTab));
		//visualizationServer.repaint();
	}
	/**
	 * Generuje nowy wykres
	 * @return
	 */
	private ChartPanel createNewChart(){
        xySeriesCollection = new XYSeriesCollection();
        seriesMin = new XYSeries("Min");
        seriesAvg = new XYSeries("Avg");
        seriesMax = new XYSeries("Max");
        xySeriesCollection.addSeries(seriesMin);
        xySeriesCollection.addSeries(seriesAvg);
        xySeriesCollection.addSeries(seriesMax);
        resultChart = ChartFactory.createXYLineChart(chartTitleString, "Iteracja", "Przystosowanie", xySeriesCollection, PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) resultChart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.green);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
		return new ChartPanel(resultChart);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
					frame.validate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
