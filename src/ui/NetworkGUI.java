package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import logging.FileLogger;
import model.DirectionalNetwork;
import model.EdgeInterface;
import model.Graph;
import model.GraphParser;
import model.Link;
import model.Network;
import model.NetworkType;
import model.Node;
import model.OmnidirectionalNetwork;
import model.Vertex;
import model.WeightedGraph;

/**
 * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
 * @version 1.0
 * @since 2011-09-10
 */
public class NetworkGUI extends JPanel implements ActionListener {

	// Set logging true if this is a compiled class, false if the class is
	// in a jar file.
	private static boolean logging = !NetworkGUI.class.getProtectionDomain()
			.getCodeSource().getLocation().toString().contains("jar");

	private static final long serialVersionUID = 1L;

	// Setup Control Group
	private JTextField rangeUpdateTextField;

	// Performance Control Group
	private JTextField pathFromTextField;
	private JTextField pathToTextField;
	private JTextField pathLengthTextField;
	private JTextField pathLengthHopsTextField;

	// Statistics Control Group
	private JTextField averageAngleTextField;
	private JTextField averageRangeTextField;

	private JTextField averageSPLTextField;
	private JTextField averageSPLHopsTextField;

	private JTextField graphDiameterTextField;
	private JTextField graphDiameterHopsTextField;

	private JTextField totalEnergyUseTextField;

	// Class members.
	private JCanvas canvas;
	private DirectionalNetwork dirNet = null;
	private OmnidirectionalNetwork omniNet = null;
	private NetworkType selectedNetwork = null;

	// Keep the vertex type generic so we can draw both the physical network
	// (Node) and a logical network (Sensor).
	private WeightedGraph<? extends Vertex, Link> currentGraph = null;

	public static void main(String[] args) {
		// Schedule a job for the event-dispatch thread to create & show the ui.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				NetworkGUI.initializeGUI();
			}
		});
	}

	private static void initializeGUI() {

		// Set the system look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			if (logging) {
				// LOGGING
				FileLogger.log(Level.WARNING, NetworkGUI.class.getName()
						+ ": initializeGUI; Error loading System Look & Feel.");
			}
		}

		// Create and set up the main window.
		JFrame rootFrame = new JFrame("NetworkDemo: Andrew Wylie");
		rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rootFrame.setSize(800, 600);
		rootFrame.setResizable(false);

		NetworkGUI content = new NetworkGUI();
		content.setOpaque(true);
		rootFrame.setContentPane(content);

		// Display the window.
		rootFrame.pack();
		rootFrame.setVisible(true);
	}

	public NetworkGUI() {

		// Set the layout of the main (this) panel.
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		GridBagConstraints c;

		// Create the optionList JPanel. It will hold all of the buttons and
		// controls vertically on the left side of the page.
		JPanel optionList = new JPanel();
		optionList.setLayout(new BoxLayout(optionList, BoxLayout.PAGE_AXIS));
		optionList.setAlignmentY(Component.TOP_ALIGNMENT);

		// Create the canvas panel. The networks will be drawn on it.
		canvas = new JCanvas();
		canvas.setPreferredSize(new Dimension(600, 600));
		canvas.setTransform(AffineTransform.getTranslateInstance(300, 300));
		canvas.setAlignmentY(Component.TOP_ALIGNMENT);
		canvas.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Network"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// ///////////// Draw Network Control Group ////////////////////////////
		// /////////////////////////////////////////////////////////////////////

		// In this group:
		// controls to draw a network graph

		// Create the labels for the graph types (directional, omnidirectional)
		JLabel dirLabel = new JLabel("Directional Network");
		JLabel omniLabel = new JLabel("Omnidirectional Network");

		// Create the radiobuttons for displaying the/a graph.
		JRadioButton drawDirPhysical = new JRadioButton("Input Graph");
		JRadioButton drawDirLogical = new JRadioButton("Logical Network");
		JRadioButton drawDirSameRange = new JRadioButton(
				"Logical Network Oriented, Homogeneous Range");
		JRadioButton drawDirDiffRange = new JRadioButton(
				"Logical Network Oriented, Heterogeneous Range");

		JRadioButton drawOmniPhysical = new JRadioButton("Input Graph");
		JRadioButton drawOmniLogical = new JRadioButton("Logical Network");
		JRadioButton drawOmniSameRange = new JRadioButton(
				"Logical Network Oriented, Homogeneous Range");
		JRadioButton drawOmniDiffRange = new JRadioButton(
				"Logical Network Oriented, Heterogeneous Range");

		// Create a button group for the buttons.
		ButtonGroup drawNetworkButtonGroup = new ButtonGroup();
		drawNetworkButtonGroup.add(drawDirPhysical);
		drawNetworkButtonGroup.add(drawDirLogical);
		drawNetworkButtonGroup.add(drawDirSameRange);
		drawNetworkButtonGroup.add(drawDirDiffRange);
		drawNetworkButtonGroup.add(drawOmniPhysical);
		drawNetworkButtonGroup.add(drawOmniLogical);
		drawNetworkButtonGroup.add(drawOmniSameRange);
		drawNetworkButtonGroup.add(drawOmniDiffRange);

		drawDirPhysical.setActionCommand("drawDirPhysical");
		drawDirLogical.setActionCommand("drawDirLogical");
		drawDirSameRange.setActionCommand("drawDirSameRange");
		drawDirDiffRange.setActionCommand("drawDirDiffRange");
		drawOmniPhysical.setActionCommand("drawOmniPhysical");
		drawOmniLogical.setActionCommand("drawOmniLogical");
		drawOmniSameRange.setActionCommand("drawOmniSameRange");
		drawOmniDiffRange.setActionCommand("drawOmniDiffRange");

		drawDirPhysical.addActionListener(this);
		drawDirLogical.addActionListener(this);
		drawDirSameRange.addActionListener(this);
		drawDirDiffRange.addActionListener(this);
		drawOmniPhysical.addActionListener(this);
		drawOmniLogical.addActionListener(this);
		drawOmniSameRange.addActionListener(this);
		drawOmniDiffRange.addActionListener(this);

		// Add the controls to the host panel.
		JPanel drawNetworkPanel = new JPanel();

		drawNetworkPanel.setLayout(new GridLayout(0, 1));
		drawNetworkPanel.add(dirLabel);
		drawNetworkPanel.add(drawDirPhysical);
		drawNetworkPanel.add(drawDirLogical);
		drawNetworkPanel.add(drawDirSameRange);
		drawNetworkPanel.add(drawDirDiffRange);
		drawNetworkPanel.add(omniLabel);
		drawNetworkPanel.add(drawOmniPhysical);
		drawNetworkPanel.add(drawOmniLogical);
		drawNetworkPanel.add(drawOmniSameRange);
		drawNetworkPanel.add(drawOmniDiffRange);

		drawNetworkPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Draw Network"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// /////////////// Setup Control Group /////////////////////////////////
		// /////////////////////////////////////////////////////////////////////

		// In this group:
		// controls to update range

		JLabel rangeUpdateLabel = new JLabel("Set Sensor Range:");
		rangeUpdateTextField = new JTextField(4);
		JButton applySetupButton = new JButton("Apply Changes");
		JButton resetSetupButton = new JButton("Reset");

		rangeUpdateTextField.setHorizontalAlignment(JTextField.RIGHT);

		applySetupButton.setActionCommand("applySetup");
		resetSetupButton.setActionCommand("resetSetup");
		applySetupButton.addActionListener(this);
		resetSetupButton.addActionListener(this);

		// Create the layout constraints object.
		c = new GridBagConstraints();

		// Set initial properties for the constraints.
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 0, 5);

		JPanel setupPanel = new JPanel();
		setupPanel.setLayout(new GridBagLayout());

		setupPanel.add(rangeUpdateLabel, c);
		c.gridx += 1;
		setupPanel.add(rangeUpdateTextField, c);
		c.gridx -= 1;
		c.gridy += 1;
		c.gridwidth = 2;
		setupPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
		c.gridwidth = 1;
		c.gridy += 1;
		setupPanel.add(applySetupButton, c);
		c.gridx += 1;
		setupPanel.add(resetSetupButton, c);

		setupPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Setup"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// ///////// Performance Control Group /////////////////////////////////
		// /////////////////////////////////////////////////////////////////////

		// In this group:
		// shortest path (draw) & its length. given a -> b
		// Length of a route

		JButton pathButton = new JButton("Get Path");
		pathButton.setActionCommand("getPath");
		pathButton.addActionListener(this);

		JButton resetPathButton = new JButton("Reset");
		resetPathButton.setActionCommand("resetPath");
		resetPathButton.addActionListener(this);

		JLabel pathLabel = new JLabel("Get Shortest Path:");

		JLabel pathFromLabel = new JLabel("From Node (name):");
		pathFromLabel.setFont(new Font(getFont().getName(), 0, 11));
		pathFromTextField = new JTextField(2);
		pathFromTextField.setHorizontalAlignment(JTextField.RIGHT);

		JLabel pathToLabel = new JLabel("To Node (name):");
		pathToLabel.setFont(new Font(getFont().getName(), 0, 11));
		pathToTextField = new JTextField(2);
		pathToTextField.setHorizontalAlignment(JTextField.RIGHT);

		// Length
		JLabel pathLengthLabel = new JLabel("Length:");
		pathLengthLabel.setFont(new Font(getFont().getName(), 0, 11));
		pathLengthTextField = new JTextField(6);
		pathLengthTextField.setHorizontalAlignment(JTextField.RIGHT);
		pathLengthTextField.setEditable(false);

		// Length (Hops)
		JLabel pathLengthHopsLabel = new JLabel("Length (Hops):");
		pathLengthHopsLabel.setFont(new Font(getFont().getName(), 0, 11));
		pathLengthHopsTextField = new JTextField(6);
		pathLengthHopsTextField.setHorizontalAlignment(JTextField.RIGHT);
		pathLengthHopsTextField.setEditable(false);

		// Create the layout constraints object.
		c = new GridBagConstraints();

		// Set initial properties for the constraints.
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 0, 5);

		JPanel performancePanel = new JPanel();
		performancePanel.setLayout(new GridBagLayout());

		c.gridwidth = 2;
		performancePanel.add(pathLabel);

		c.gridwidth = 1;
		c.gridy += 1;
		performancePanel.add(pathFromLabel, c);
		c.gridx += 1;
		performancePanel.add(pathFromTextField, c);

		c.gridx -= 1;
		c.gridy += 1;
		performancePanel.add(pathToLabel, c);
		c.gridx += 1;
		performancePanel.add(pathToTextField, c);

		c.gridx -= 1;
		c.gridy += 1;
		performancePanel.add(pathLengthLabel, c);
		c.gridx += 1;
		performancePanel.add(pathLengthTextField, c);

		c.gridx -= 1;
		c.gridy += 1;
		performancePanel.add(pathLengthHopsLabel, c);
		c.gridx += 1;
		performancePanel.add(pathLengthHopsTextField, c);

		c.gridwidth = 2;
		c.gridy++;
		c.gridx -= 1;
		performancePanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
		c.gridwidth = 1;
		c.gridy += 1;
		performancePanel.add(pathButton, c);
		c.gridx += 1;
		performancePanel.add(resetPathButton, c);

		performancePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Performance"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// ///////////////////// Statistics Control Group //////////////////////
		// /////////////////////////////////////////////////////////////////////

		// In this group:
		// average sensor range
		// average sensor angle
		// total energy use
		// graph diameter

		// Create text & text boxes to display the averages.

		// Average angle
		JLabel averageAngleLabel = new JLabel("Average Angle:");
		averageAngleLabel.setFont(new Font(getFont().getName(), 0, 11));
		averageAngleTextField = new JTextField(7);
		averageAngleTextField.setHorizontalAlignment(JTextField.RIGHT);
		averageAngleTextField.setEditable(false);

		// Average Range
		JLabel averageRangeLabel = new JLabel("Average Range:");
		averageRangeLabel.setFont(new Font(getFont().getName(), 0, 11));
		averageRangeTextField = new JTextField(7);
		averageRangeTextField.setHorizontalAlignment(JTextField.RIGHT);
		averageRangeTextField.setEditable(false);

		// Average Shortest Path Length
		JLabel averageSPLLabel = new JLabel("Avg. Shortest Path Length:");
		averageSPLLabel.setFont(new Font(getFont().getName(), 0, 11));
		averageSPLTextField = new JTextField(5);
		averageSPLTextField.setHorizontalAlignment(JTextField.RIGHT);
		averageSPLTextField.setEditable(false);

		// Average Shortest Path Length (Hops)
		JLabel averageSPLHopsLabel = new JLabel(
				"Avg. Shortest Path Length (Hops):");
		averageSPLHopsLabel.setFont(new Font(getFont().getName(), 0, 11));
		averageSPLHopsTextField = new JTextField(5);
		averageSPLHopsTextField.setHorizontalAlignment(JTextField.RIGHT);
		averageSPLHopsTextField.setEditable(false);

		// Graph Diameter
		JLabel graphDiameterLabel = new JLabel("Graph Diameter:");
		graphDiameterLabel.setFont(new Font(getFont().getName(), 0, 11));
		graphDiameterTextField = new JTextField(7);
		graphDiameterTextField.setHorizontalAlignment(JTextField.RIGHT);
		graphDiameterTextField.setEditable(false);

		// Graph Diameter (Hops)
		JLabel graphDiameterHopsLabel = new JLabel("Graph Diameter (Hops):");
		graphDiameterHopsLabel.setFont(new Font(getFont().getName(), 0, 11));
		graphDiameterHopsTextField = new JTextField(7);
		graphDiameterHopsTextField.setHorizontalAlignment(JTextField.RIGHT);
		graphDiameterHopsTextField.setEditable(false);

		// Total Energy Use
		JLabel totalEnergyUseLabel = new JLabel("Total Energy Use (10^3):");
		totalEnergyUseLabel.setFont(new Font(getFont().getName(), 0, 11));
		totalEnergyUseTextField = new JTextField(7);
		totalEnergyUseTextField.setHorizontalAlignment(JTextField.RIGHT);
		totalEnergyUseTextField.setEditable(false);

		// Create the layout constraints object.
		c = new GridBagConstraints();

		// Set initial properties for the constraints.
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 0, 5);

		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new GridBagLayout());

		statisticsPanel.add(averageAngleLabel, c);
		c.gridx += 1;
		statisticsPanel.add(averageAngleTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(averageRangeLabel, c);
		c.gridx += 1;
		statisticsPanel.add(averageRangeTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(averageSPLLabel, c);
		c.gridx += 1;
		statisticsPanel.add(averageSPLTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(averageSPLHopsLabel, c);
		c.gridx += 1;
		statisticsPanel.add(averageSPLHopsTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(graphDiameterLabel, c);
		c.gridx += 1;
		statisticsPanel.add(graphDiameterTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(graphDiameterHopsLabel, c);
		c.gridx += 1;
		statisticsPanel.add(graphDiameterHopsTextField, c);

		c.gridy += 1;
		c.gridx -= 1;
		statisticsPanel.add(totalEnergyUseLabel, c);
		c.gridx += 1;
		statisticsPanel.add(totalEnergyUseTextField, c);

		statisticsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Statistics"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// /////////////////// Options Control Group ///////////////////////////
		// /////////////////////////////////////////////////////////////////////

		// In this group:
		// load physical network from file

		// File open button
		JButton loadGraphButton = new JButton("Load Graph");
		loadGraphButton.setActionCommand("loadGraph");
		loadGraphButton.addActionListener(this);

		JPanel optionsPanel = new JPanel();

		optionsPanel.add(loadGraphButton);

		optionsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Options"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// /////////////////////////////////////////////////////////////////////
		// ///////////////// OptionList Pane ///////////////////////////////////
		// /////////////////////////////////////////////////////////////////////

		// Add all of the components to the main content pane.
		add(Box.createRigidArea(new Dimension(5, 0)));
		add(optionList);
		add(Box.createRigidArea(new Dimension(5, 0)));
		add(canvas);

		optionList.add(drawNetworkPanel);
		optionList.add(setupPanel);
		optionList.add(performancePanel);
		optionList.add(statisticsPanel);
		optionList.add(optionsPanel);
	}

	DecimalFormat numFormatter = new DecimalFormat("###,###,##0.00");

	public void actionPerformed(ActionEvent e) {

		// Action code for the options menu.
		if ("loadGraph".equals(e.getActionCommand())) {

			final JFileChooser fileChooser = new JFileChooser();
			int retVal = fileChooser.showOpenDialog(this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				// Get the file.
				File file = fileChooser.getSelectedFile();

				// Parse the file for the physical network.
				WeightedGraph<Node, Link> pn = parseGraph(file);

				// Run the orientation algorithm.
				if (pn != null) {
					dirNet = new DirectionalNetwork(pn);
					omniNet = new OmnidirectionalNetwork(pn);
					JOptionPane.showMessageDialog(this.getRootPane(),
							"Network loaded!");
				}
			}

			// Draw the network on load if there is one selected.
			if (selectedNetwork != null) {
				if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = dirNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = omniNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				}
			}
		}

		// Draw a graph on the drawing action commands.
		if (dirNet != null) {

			if ("drawDirPhysical".equals(e.getActionCommand())) {
				currentGraph = dirNet.getPhysicalNetwork();

			} else if ("drawDirLogical".equals(e.getActionCommand())) {
				currentGraph = dirNet.getPhysicalNetworkMst();

			} else if ("drawDirSameRange".equals(e.getActionCommand())) {
				currentGraph = dirNet.createOptimalNetwork(true);

			} else if ("drawDirDiffRange".equals(e.getActionCommand())) {
				currentGraph = dirNet.createOptimalNetwork(false);
			}

			if ("drawDirPhysical".equals(e.getActionCommand())
					|| "drawDirLogical".equals(e.getActionCommand())
					|| "drawDirSameRange".equals(e.getActionCommand())
					|| "drawDirDiffRange".equals(e.getActionCommand())) {

				drawGraph(currentGraph);
				// Keep track of which network should be visible.
				selectedNetwork = NetworkType.DIRECTIONAL;
			}
		}

		// Draw a graph on the drawing action commands.
		if (omniNet != null) {

			if ("drawOmniPhysical".equals(e.getActionCommand())) {
				currentGraph = omniNet.getPhysicalNetwork();

			} else if ("drawOmniLogical".equals(e.getActionCommand())) {
				currentGraph = omniNet.getPhysicalNetworkMst();

			} else if ("drawOmniSameRange".equals(e.getActionCommand())) {
				currentGraph = omniNet.createOptimalNetwork(true);

			} else if ("drawOmniDiffRange".equals(e.getActionCommand())) {
				currentGraph = omniNet.createOptimalNetwork(false);
			}

			if ("drawOmniPhysical".equals(e.getActionCommand())
					|| "drawOmniLogical".equals(e.getActionCommand())
					|| "drawOmniSameRange".equals(e.getActionCommand())
					|| "drawOmniDiffRange".equals(e.getActionCommand())) {

				drawGraph(currentGraph);
				// Keep track of which network should be visible.
				selectedNetwork = NetworkType.OMNIDIRECTIONAL;
			}
		}

		// A graph should be loaded before any actual commands are available.
		if (dirNet == null || omniNet == null) {
			JOptionPane.showMessageDialog(this.getRootPane(),
					"You must load a network graph before analyzing it.");
			return;
		}

		// Action event code for the shortest path retrieval.
		if ("getPath".equals(e.getActionCommand())) {

			// There must be a network currently selected.
			if (selectedNetwork == null) {
				JOptionPane.showMessageDialog(this.getRootPane(),
						"A network must be selected for"
								+ " which to find a path in.");
				return;
			}

			// Code for the shortest specified path button.
			if (pathFromTextField.getText().equals("")
					|| pathToTextField.getText().equals("")) {
				// Create dialog to inform user to enter info.
				JOptionPane.showMessageDialog(this.getRootPane(),
						"Enter sensor names to find"
								+ " the shortest path between them.");
				return;
			}

			// Reset any path currently drawn.
			drawGraph(currentGraph);

			// Set up some temporary variables to prevent code repetition.
			String from = pathFromTextField.getText();
			String to = pathToTextField.getText();
			int splh = 0;
			float spl = 0f;
			List<? extends Vertex> sp = null;

			// Get the route length.
			if (selectedNetwork != null) {
				if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {

					splh = currentGraph.getShortestPathLengthHops(from, to);
					spl = currentGraph.getShortestPathLength(from, to);
					sp = currentGraph.getShortestPath(from, to);

				} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {

					splh = currentGraph.getShortestPathLengthHops(from, to);
					spl = currentGraph.getShortestPathLength(from, to);
					sp = currentGraph.getShortestPath(from, to);
				}
			}

			pathLengthHopsTextField.setText(numFormatter.format(splh));
			pathLengthTextField.setText(numFormatter.format(spl));

			// Create a drawable object to add to the canvas object.
			if (sp != null) {

				Polyline polyline = new Polyline();
				polyline.setColor(Color.red);

				// Draw the path.
				for (int i = 0; i < sp.size(); i++) {
					Vertex v = sp.get(i);
					Point vPoint = new Point((int) v.getX(), (int) v.getY());
					polyline.add(vPoint);
				}

				canvas.add(polyline);
			}
		}

		// Action event code for the sensor range updating.
		if ("applySetup".equals(e.getActionCommand())) {

			// Update Range.
			String newRangeText = rangeUpdateTextField.getText();

			// There must be a network currently selected.
			if (selectedNetwork == null) {

				JOptionPane.showMessageDialog(this.getRootPane(),
						"A network must be selected for"
								+ " which to update sensor range.");
				return;
			}

			if (selectedNetwork != null) {

				// Check the value validity.
				if (newRangeText.equals("")) {
					// Create dialog to inform user to enter info.
					JOptionPane.showMessageDialog(this.getRootPane(),
							"Enter sensor range to be set.");
					return;
				}

				// TODO exception handling
				float newRange = Float.parseFloat(newRangeText);

				if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = dirNet.createNetwork(newRange);
					drawGraph(currentGraph);
				} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = omniNet.createNetwork(newRange);
					drawGraph(currentGraph);
				}
			}

		} else if ("resetSetup".equals(e.getActionCommand())) {

			// There must be a network currently selected.
			if (selectedNetwork == null) {
				JOptionPane.showMessageDialog(this.getRootPane(),
						"A network must be selected for"
								+ " which to reset sensor range.");
				return;
			}

			rangeUpdateTextField.setText("");

			if (selectedNetwork != null) {
				if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = dirNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = omniNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				}
			}
		} else if ("resetPath".equals(e.getActionCommand())) {

			// There must be a network currently selected.
			if (selectedNetwork == null) {
				JOptionPane.showMessageDialog(this.getRootPane(),
						"A network must be selected for a path to be found.");
				return;
			}

			pathFromTextField.setText("");
			pathToTextField.setText("");
			pathLengthTextField.setText("");
			pathLengthHopsTextField.setText("");

			if (selectedNetwork != null) {
				if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = dirNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {
					currentGraph = omniNet.createOptimalNetwork(true);
					drawGraph(currentGraph);
				}
			}
		}

		// On any event we want to update the network statistics.
		if (selectedNetwork != null) {
			if (NetworkType.DIRECTIONAL.equals(selectedNetwork)) {
				updateUiStatistics(dirNet, currentGraph);
			} else if (NetworkType.OMNIDIRECTIONAL.equals(selectedNetwork)) {
				updateUiStatistics(omniNet, currentGraph);
			}
		}

	}

	// populate the ui fields with information about the current network
	private void updateUiStatistics(Network net,
			WeightedGraph<? extends Vertex, Link> wg) {

		// Update the normal graph statistics.
		String fAvgAngle = numFormatter.format(net.getAverageAngle());
		String fAvgRange = numFormatter.format(net.getAverageRange());
		double totEnergy = net.getTotalEnergyUse() / 1000;
		averageAngleTextField.setText(fAvgAngle);
		averageRangeTextField.setText(fAvgRange);
		totalEnergyUseTextField.setText(numFormatter.format(totEnergy));

		// Update the average shortest path values.
		float ASPL = wg.getAverageShortestPathLength();
		float ASPLH = wg.getAverageShortestPathLengthHops();
		averageSPLTextField.setText(numFormatter.format(ASPL));
		averageSPLHopsTextField.setText(numFormatter.format(ASPLH));

		// Update the graph diameter.
		String fDiam = numFormatter.format(wg.getDiameter());
		String fDiamHops = numFormatter.format(wg.getDiameterHops());
		graphDiameterTextField.setText(fDiam);
		graphDiameterHopsTextField.setText(fDiamHops);
	}

	// Redraw the input graph.
	private void drawGraph(Graph<? extends Vertex, ? extends EdgeInterface> g) {
		canvas.clear();
		canvas.add(g);
	}

	// This function loads a physical network from a file. It handles the
	// tokenization, parsing, and object creation.
	private WeightedGraph<Node, Link> parseGraph(File file) {
		GraphParser graphParser = new GraphParser();
		return graphParser.parse(file);
	}

}
