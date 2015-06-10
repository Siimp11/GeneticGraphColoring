package pl.edu.agh.gcp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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

import pl.edu.agh.gcp.GenericGraphColoring.Stats;
import pl.edu.agh.gcp.StageGraphColoring;
import pl.edu.agh.gcp.dimacs.DimacsParser;
import pl.edu.agh.gcp.mutator.ColorUnifier2;
import pl.edu.agh.gcp.mutator.FixBadEdgesImproved;
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
	/******************/
	private JPanel topMenu;
	private JButton btnLoadGraph;
	private JButton btnStart;
	private JPanel settingsPanel;
	
	private JPanel settingsStageOnePanel;
	private JSpinner populationSpinnerStageOne;
	private JSpinner iterationSpinnerStageOne;
	
	private JPanel settingsStageTwoPanel;
	private JSpinner populationSpinnerStageTwo;
	private JSpinner iterationSpinnerStageTwo;
	
	private JPanel settingsStageThreePanel;
	private JSpinner populationSpinnerStageThree;
	private JSpinner iterationSpinnerStageThree;
	
	/******************/
	private JPanel resultPanel;
	private JLabel fitnessLabel;
	private JLabel colorsLabel;
	private JLabel badEdgesLabel;
	
	private final String fitnessLabelPrefix = "Przystosowanie: ";
	private final String colorsLabelPrefix = "Użyte kolory: ";
	private final String badEdgesLabelPrefix = "Błędne krawędzie: ";
	
	/******************/
	/**
	 * Obiekty związane z wizualizacją grafu
	 */
	private Layout<Object, Object> graphLayout;
	private BasicVisualizationServer<Object,Object> visualizationServer;
	
	/******************/
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
	
	/******************/
	/**
	 * Obiekty związane z algorytmem
	 */
	private StageGraphColoring graphColoring;			//algorytm
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
		
		/**
		 * Główny panel
		 */
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		
		/**********************************************************************************************************
		
		/**
		 * Górne menu z przyciskami
		 */
		topMenu = new JPanel();
		
		/**
		 * Przycisk do wczytywania grafu z pliku
		 */
		btnLoadGraph = new JButton("Wczytaj graf (DIMACS)");
		btnLoadGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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
						e.printStackTrace();
					}
				}
				if(parser.getGraph() != null){
					currentGraph = parser.getGraph();
					graphLayout.setGraph(currentGraph);
					visualizationServer.getRenderContext().setVertexFillPaintTransformer(new DefaultVertexPaintTransformer());
					clearChartData();
				}
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
					graphColoring = new StageGraphColoring(currentGraph);
					
					commitSpinnerEdits();
					
					//graphColoring.setStage1_PopulationSize(200);
					//graphColoring.setStage1_IterationsLimit(300);
					graphColoring.setStage1_PopulationSize((Integer)populationSpinnerStageOne.getValue());
					graphColoring.setStage1_IterationsLimit((Integer)iterationSpinnerStageOne.getValue());
					graphColoring.addStage1_Mutator(new RandomMutator(40, 100));
					graphColoring.addStage1_Mutator(new ColorUnifier2(50, 100));
					
					//graphColoring.setStage2_PopulationSize(100);
					//graphColoring.setStage2_IterationsLimit(200);
					graphColoring.setStage2_PopulationSize((Integer)populationSpinnerStageTwo.getValue());
					graphColoring.setStage2_IterationsLimit((Integer)iterationSpinnerStageTwo.getValue());
					graphColoring.addStage2_Mutator(new RandomMutator(35, 100));
					graphColoring.addStage2_Mutator(new FixBadEdgesImproved(80, 100));
					graphColoring.addStage2_Mutator(new ColorUnifier2(50, 100));
					
					//graphColoring.setStage3_PopulationSize(500);
					//graphColoring.setStage3_IterationsLimit(500);
					graphColoring.setStage3_PopulationSize((Integer)populationSpinnerStageThree.getValue());
					graphColoring.setStage3_IterationsLimit((Integer)iterationSpinnerStageThree.getValue());
					graphColoring.addStage3_Mutator(new RandomMutator(50, 100));
					graphColoring.addStage3_Mutator(new FixBadEdgesImproved(70, 100));
					graphColoring.addStage3_Mutator(new ColorUnifier2(50, 100));
					
					graphColoring.setBadEdgeWeight(5);
					graphColoring.setColorsUsedWeight(2);
					
					graphColoring.addResultObserver(new Observer() {
						@Override
						public void update(Observable o, Object arg) {
							Chromosome ch = (Chromosome)arg;
							colorVisualisedGraph(ch.getColors(), ch.getColoringTab());
							updateLabels(ch);
							
							System.out.println("Best child found: fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors()
									+ " badEdges=" + ch.getBadEdges() + " coloring=" + Arrays.toString(ch.getColoringTab()));
							for(String s : graphColoring.getRaport())
								System.out.println(s);
							
							JOptionPane.showMessageDialog(contentPane, "fitness=" + ch.getFitness() + " colorsUsed=" + ch.getColors()
									+ " badEdges=" + ch.getBadEdges(), "Wynik", JOptionPane.INFORMATION_MESSAGE);
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
					
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							graphColoring.run();
						}
					}).start();
					
				}

			}
		});
		btnStart.setSize(150,30);
		topMenu.add(btnStart);
		
		/**
		 * Panel ustawień ilości populacji i iteracji
		 */
		settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
		
		settingsStageOnePanel = new JPanel();
		settingsStageOnePanel.setLayout(new BoxLayout(settingsStageOnePanel, BoxLayout.LINE_AXIS));
		populationSpinnerStageOne = new JSpinner(new SpinnerNumberModel(200, 0, 800, 1));
		iterationSpinnerStageOne = new JSpinner(new SpinnerNumberModel(300, 0, 800, 1));
		settingsStageOnePanel.add(new JLabel("Etap I   "));
		settingsStageOnePanel.add(populationSpinnerStageOne);
		settingsStageOnePanel.add(iterationSpinnerStageOne);
		
		settingsStageTwoPanel = new JPanel();
		settingsStageTwoPanel.setLayout(new BoxLayout(settingsStageTwoPanel, BoxLayout.LINE_AXIS));
		populationSpinnerStageTwo = new JSpinner(new SpinnerNumberModel(100, 0, 800, 1));
		iterationSpinnerStageTwo = new JSpinner(new SpinnerNumberModel(200, 0, 800, 1));
		settingsStageTwoPanel.add(new JLabel("Etap II  "));
		settingsStageTwoPanel.add(populationSpinnerStageTwo);
		settingsStageTwoPanel.add(iterationSpinnerStageTwo);
		
		settingsStageThreePanel = new JPanel();
		settingsStageThreePanel.setLayout(new BoxLayout(settingsStageThreePanel, BoxLayout.LINE_AXIS));
		populationSpinnerStageThree = new JSpinner(new SpinnerNumberModel(500, 0, 800, 1));
		iterationSpinnerStageThree = new JSpinner(new SpinnerNumberModel(500, 0, 800, 1));
		settingsStageThreePanel.add(new JLabel("Etap III "));
		settingsStageThreePanel.add(populationSpinnerStageThree);
		settingsStageThreePanel.add(iterationSpinnerStageThree);

		settingsPanel.add(new JLabel("Ilość populacji                       Ilość iteracji", SwingConstants.CENTER)); //TODO sry lenistwo xD
		settingsPanel.add(settingsStageOnePanel);
		settingsPanel.add(settingsStageTwoPanel);
		settingsPanel.add(settingsStageThreePanel);
		
		topMenu.add(settingsPanel);
		
		/**
		 * Panel z wynikiem kolorowania
		 */
        resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(3,1));
        
        fitnessLabel = new JLabel();
        fitnessLabel.setText(fitnessLabelPrefix);
        fitnessLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultPanel.add(fitnessLabel);
        
        colorsLabel = new JLabel();
        colorsLabel.setText(colorsLabelPrefix);
        colorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultPanel.add(colorsLabel);
        
        badEdgesLabel = new JLabel();
        badEdgesLabel.setText(badEdgesLabelPrefix);
        badEdgesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultPanel.add(badEdgesLabel);
        
        topMenu.add(resultPanel);
		contentPane.add(topMenu, BorderLayout.NORTH);
		/**********************************************************************************************************
		/**
		 * Wyświetlanie grafu
		 */
		graphLayout = new SpringLayout<Object, Object>(new UndirectedSparseGraph<Object, Object>());
		graphLayout.setSize(new Dimension(800,800)); // sets the initial size of the layout space
		visualizationServer = new BasicVisualizationServer<Object,Object>(graphLayout);
		visualizationServer.setPreferredSize(new Dimension(850,850)); //Sets the viewing area size
		
        contentPane.add(visualizationServer, BorderLayout.CENTER);
        /**********************************************************************************************************
        /**
         * Wykres
         */
        chartPanel = createNewChart();
        
        contentPane.add(chartPanel, BorderLayout.EAST);
        /**********************************************************************************************************/
		
        pack();
	}
	
	//Czyszczenie danych na wykresie
	private void clearChartData(){
		seriesMin.clear(); 
		seriesAvg.clear();
		seriesMax.clear();
	}
	
	/**
	 * Transformer kolorów wierzchołków dla danego pokolorowania
	 * @author JakubSzczepankiewicz
	 *
	 */
	//TODO ustawienie domyślnego transformera po wczytaniu nowego grafu z pliku
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
	 * Domyślny transformer kolorów wierzchołków
	 * @author JakubSzczepankiewicz
	 *
	 */
	private static class DefaultVertexPaintTransformer implements Transformer<Object,Paint> {
		@Override
		public Paint transform(Object i){
			return Color.GRAY;
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
	 * Aktualizuje wynik wyświetlany w stopce
	 * @param ch
	 */
	private void updateLabels(Chromosome ch){
		fitnessLabel.setText(fitnessLabelPrefix+Integer.toString(ch.getFitness()));
		colorsLabel.setText(colorsLabelPrefix+Integer.toString(ch.getColors()));
		badEdgesLabel.setText(badEdgesLabelPrefix+Integer.toString(ch.getBadEdges()));
	}
	
	/**
	 * Wprowadza zmiany w spinnerach wpisane przez użytkownika
	 */
	private void commitSpinnerEdits(){
		try {
			populationSpinnerStageOne.commitEdit();
			iterationSpinnerStageOne.commitEdit();
			populationSpinnerStageTwo.commitEdit();
			iterationSpinnerStageTwo.commitEdit();
			populationSpinnerStageThree.commitEdit();
			iterationSpinnerStageThree.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Generuje nowy czysty wykres
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
