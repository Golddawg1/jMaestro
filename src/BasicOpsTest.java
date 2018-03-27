import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jm.JMC;
import jm.music.data.*;
import jm.music.*;
import jm.util.*;
import jm.gui.show.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import java.util.concurrent.*;

public class BasicOpsTest extends Application implements JMC {

	Score score;
	GraphicsContext gc = null;
	float xpos;
	double totalLength = 0;
	double test = 0;
	double cursor;

	double width = 800;
	double height = 600;

	Canvas canvas;

	int curr_pane = 0;

	static GridPane content;

	ArrayList<Pane> paneList = new ArrayList();

	static MIDINoteExtractor midiex;

	static Score toto;

	static double totalTime;

	static Rectangle rect;

	static Timeline barTimeLine;
	static Timeline scrollingTimeLine;

	File workingSongFile;

	TextField tempoField;

	Label tempoLabel;
	static Sequencer sequencer;

	BorderPane bp;
	BorderPane innerBP;

	ArrayList<Part> parts;

	ArrayList<String> partNames;

	Synthesizer synth;

	ArrayList<MIDIPane> panes;

	Stage primaryStage;

	static ZoomableScrollPane scrollPane;

	int den, num;

	MeasurePane measures;

	VBox HUDcontent;

	static double currentTime;

	static double currentRectX;

	static Button playButton;

	static boolean isPlaying;

	static MIDIPane myCurrentMidiPane;

	static ExecutorService executor;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle("jMaestro");
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		Screen screen = Screen.getPrimary();
		javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

		primaryStage.setX(bounds.getMinX());
		primaryStage.setY(bounds.getMinY());
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());

		// canvas = new Pane();
		// Canvas canvas2 = new Canvas(800, 100);

		content = new GridPane();
		midiex = new MIDINoteExtractor();

		content.setVgap(5);

		rect = new Rectangle(0, 0, 1, 400);

		sequencer = MidiSystem.getSequencer();

		synth();
		playButton = new Button("Play!");
		playButton.setOnAction(e -> {

			if (toto == null) {
				try {
					toto = midiex.extract(workingSongFile.toPath());
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

			}

			if (sequencer.isRunning()) {

				isPlaying = false;
				Task task = new Task<Void>() {
					@Override
					public Void call() {

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								playButton.setText("Play");
							}
						});

						return null;
					}
				};

				executor.execute(task);

				stopAction();
			} else {
				isPlaying = true;
				toto.empty();
				toto.addPartList(midiex.getPart());
				rect.setHeight(content.getHeight() + 20);
				toto.setTempo(midiex.tempo);

				Task task = new Task<Void>() {
					@Override
					public Void call() {

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								toto.addPartList(midiex.getPart());
								rect.setHeight(content.getHeight() + 20);
								toto.setTempo(midiex.tempo);
								playButton.setText("Stop");
							}
						});

						return null;
					}
				};

				executor.execute(task);

				synthesize();

				startAction();
			}

			if (currentTime == totalTime) {
				playButton.setText("Play");
			}

		});

		Button resetButton = new Button("←|");
		resetButton.setOnAction(e -> {

			currentRectX = 0;
			currentTime = 0;
			rect.translateXProperty().set(currentRectX);

		});

		Button addButton = new Button("+");
		addButton.setOnAction(e -> {

			ArrayList<MIDINoteBar> temp = midiex.getMNB();
			for (int i = 0; i < temp.size(); i++) {
				Note n = temp.get(i).getNote();
				n.setPitch(n.getPitch() + 1);
				temp.get(i).setNote(n);
				// midiex.setMNB(temp);

			}

		});

		Button deleteButton = new Button("Delete");
		deleteButton.setOnAction(e -> {

			ArrayList<MIDINoteBar> temp = midiex.getMNB();
			for (int i = 0; i < temp.size(); i++) {

				midiex.getMNB().get(i).clear();
			}

		});

		StackPane root = new StackPane();
		root.setAlignment(Pos.TOP_LEFT);

		Pane pane = new Pane();
		scrollPane = new ZoomableScrollPane(pane);

		HUDcontent = new VBox();

		pane.getChildren().addAll(HUDcontent, rect);
		root.getChildren().add(scrollPane);

		tempoField = new TextField("128");
		tempoLabel = new Label("Tempo:");
		innerBP = new BorderPane(root, null, deleteButton, null, null);

		tempoField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,3}?")) {
					tempoField.setText(oldValue);
				}

				else {

					tempoField.setText(newValue);

					midiex.tempo = (Integer.parseInt(newValue));

					currentTime = sequencer.getMicrosecondPosition();
					currentRectX = rect.getTranslateX();
					setSequencer(currentRectX);

				}

			}

		});

		FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				bp.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #0091FF, #661a33)");
				return null;
			}
		});

		executor.execute(task);

		Label ol = new Label("Outer Layer");
		bp = new BorderPane(innerBP, null, addButton, null, ol);

		MenuBar menuBar = new MenuBar();

		// --- Menu File
		Menu menuFile = new Menu("File");

		MenuItem importFile = new MenuItem("Import");
		importFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								importAction();

							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		menuFile.getItems().addAll(importFile);

		// --- Menu Edit
		Menu menuEdit = new Menu("Edit");

		MenuItem edit = new MenuItem("Edit");
		edit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				editAction();
			}

			@SuppressWarnings("unchecked")
			private void editAction() {

				Task task = new Task<Void>() {
					@Override
					public Void call() {

						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								Stage dialog = new Stage();
								dialog.initModality(Modality.APPLICATION_MODAL);
								dialog.initOwner(primaryStage);
								// VBox dialogVbox = new VBox(20);
								//
								HBox listViewPanel = new HBox();
								listViewPanel.setSpacing(10);

								// the text to be displayed when clicking on a new item in the list.

								final Text label = new Text("Nothing Selected.");
								if (myCurrentMidiPane != null) {
									label.setText(myCurrentMidiPane.getPartInstrument());
								} else {
									label.setText("Nothing Selected");
								}
								label.setFont(Font.font(null, FontWeight.BOLD, 16));

								FilteredList<String> filteredData = new FilteredList<>(Constants.observableList,
										s -> true);

								TextField filterInput = new TextField();
								filterInput.textProperty().addListener(obs -> {
									String filter = filterInput.getText();
									if (filter == null || filter.length() == 0) {
										filteredData.setPredicate(s -> true);
									} else {
										filteredData.setPredicate(s -> s.contains(filter.toUpperCase()));
									}

									Constants.mListView.setItems(filteredData);
								});

								// create a list of items.

								Constants.mListView.getSelectionModel().selectedItemProperty()
										.addListener(new ChangeListener<String>() {

											public void changed(ObservableValue<? extends String> observable,
													String oldValue, String newValue) {
												// change the label text value to the newly selected
												// item.
												label.setText(newValue);
												if (myCurrentMidiPane != null)
													myCurrentMidiPane
															.setInstrument((int) Constants.getKeyFromValue(newValue));
											}
										});
								Group instedit = new Group();
								listViewPanel.getChildren().addAll(Constants.mListView, label, filterInput);
								instedit.getChildren().addAll(listViewPanel);

								Scene dialogScene = new Scene(instedit, 400, 400);
								dialog.setScene(dialogScene);
								dialog.show();
							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		menuEdit.getItems().addAll(edit);

		menuBar.getMenus().addAll(menuFile, menuEdit);
		bp.setTop(menuBar);

		HBox menu = new HBox(playButton, resetButton, tempoLabel, tempoField);
		VBox holder = new VBox(menuBar, menu);
		bp.setTop(holder);
		Scene scene = new Scene(bp, width, height);

		primaryStage.setScene(scene);
		primaryStage.show();

		content.setStyle("-fx-background-color: linear-gradient(from 25% 0% to 100% 100%, #dc143c, #661a33)");
		bp.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #0091FF, #000000)");

	}

	// Starts the sequencer at the current time, remakes the timelines for the
	// rectangle and scrolling pane
	private static void startAction() {

		Task task = new Task<Void>() {
			@Override
			public Void call() {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (isPlaying) {

							sequencer.setMicrosecondPosition((long) currentTime);
							sequencer.start();

							// System.out.println("Total time is: " + totalTime);
							timeLine();
							scrollingBarLock();

							barTimeLine.play();
							scrollingTimeLine.play();
						}
					}
				});

				return null;
			}
		};

		executor.execute(task);

	}

	// Stops the playing if music and timelines, sets the current x and current time
	// to be where song was stopped
	private static void stopAction() {
		Task task = new Task<Void>() {
			@Override
			public Void call() {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						currentTime = sequencer.getMicrosecondPosition();
						currentRectX = rect.getTranslateX();

						sequencer.stop();
						if (barTimeLine != null)
							barTimeLine.stop();
						if (scrollingTimeLine != null)
							scrollingTimeLine.stop();
						playButton.setText("Play");
					}
				});

				return null;
			}
		};

		executor.execute(task);

	}

	// Converts an x value to a microsecond value in order to use with the sequencer
	public static long convertToMicro(long curr) {

		long ratio = (long) (curr / content.getWidth() * totalTime);
		System.out.println(ratio / 1000000);
		return ratio;
	}

	// Does all the things to make the sequencer start at a certain location
	public static void setSequencer(double curr) {

		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						stopAction();
						System.out.println("The x is: " + curr);
						rect.translateXProperty().set(curr);
						System.out.println("The x for rect is: " + rect.getTranslateX());
						currentRectX = curr;
						currentTime = convertToMicro((long) curr);
						sequencer.setMicrosecondPosition((long) currentTime);
						System.out
								.println("The sequencer is at microsecond length: " + sequencer.getMicrosecondLength());
						startAction();

					}
				});

				return null;
			}
		};

		executor.execute(task);

	}

	// the timeline for the rect
	public static void timeLine() {
		Duration songTime = Duration.millis((totalTime - currentTime) / 1000);
		barTimeLine = new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(rect.translateXProperty(), currentRectX),
						new KeyValue(rect.translateYProperty(), 0)),

				// new KeyFrame(Duration.millis((totalTime / 2) / 1000),
				// new KeyValue(rect.translateXProperty(), (content.getWidth() / 2) - 1),
				// new KeyValue(rect.translateYProperty(), 0)),

				new KeyFrame(songTime, new KeyValue(rect.translateXProperty(), content.getWidth() - 5),
						new KeyValue(rect.translateYProperty(), 0)));

		System.out.println(totalTime / 1000);

	}

	// the timeline to force scrolling
	public static void scrollingBarLock() {
		Duration songTime = Duration.millis((totalTime - currentTime) / 1000);

		scrollingTimeLine = new Timeline(
				new KeyFrame(Duration.millis(0),
						new KeyValue(scrollPane.hvalueProperty(), currentRectX / content.getWidth())),
				// new KeyFrame(Duration.millis((totalTime / 2) / 1000),
				// new KeyValue(scroller.hvalueProperty(), (scroller.getHmax() / 2) - 1)),

				new KeyFrame(songTime, new KeyValue(scrollPane.hvalueProperty(), 1.0)));

	}

	// Initially creates the notes
	private void paintNotes() {
		panes = midiex.getPaneList();

		for (int i = 0; i < panes.size(); i++) {

			content.add(panes.get(curr_pane), 0, curr_pane);
			curr_pane++;

		}
	}

	// This handles everything to import a song
	public void importAction() {

		currentRectX = 0;
		currentTime = 0;
		rect.translateXProperty().set(currentRectX);
		// bp.setStyle("-fx-background-color: #F0591E;");

		midiex = new MIDINoteExtractor();
		Path s = null;

		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MIDI files (*.mid)", "*.mid");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showOpenDialog(primaryStage);
		if (file != null) {
			s = file.toPath();
		}
		workingSongFile = file;
		s = workingSongFile.toPath();

		try {
			toto = midiex.extract(s);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		curr_pane = 0;
		content.getChildren().clear();
		paneList.clear();
		paintNotes();

		tempoField.setText("" + midiex.tempo);
		rect.setHeight(content.getHeight() + 20);

		GridPane contentParts = new GridPane();
		Group partGroup = new Group();

		contentParts.setVgap(127 * 2);
		innerBP.setLeft(contentParts);

		double lastBeat = toto.getEndTime();

		num = toto.getNumerator();
		den = toto.getDenominator();
		synthesize();

		System.out.println(totalTime / 1000000 + " *" + midiex.tempo + "lastBeat is " + lastBeat);
		double numMeasures = ((totalTime / 1000000) / 60 * midiex.tempo) / num;
		measures = new MeasurePane(num, den, numMeasures);

		System.out.println(numMeasures + "this many measures");

		Pane pane = new Pane();
		scrollPane = new ZoomableScrollPane(pane);

		HUDcontent = new VBox();

		StackPane root = new StackPane();
		root.setAlignment(Pos.TOP_LEFT);

		pane.getChildren().addAll(HUDcontent, rect);
		root.getChildren().add(scrollPane);

		innerBP.setCenter(root);

		measures.setMaxWidth(content.getChildren().get(0).getTranslateX());
		HUDcontent.getChildren().clear();
		HUDcontent.getChildren().addAll(measures, content);

	}

	public void synth() throws Exception {

		synth = MidiSystem.getSynthesizer();
		synth.open();

		Soundbank sb = synth.getDefaultSoundbank();/*
													 * getSoundbank(new // File("./soundbank-deluxe.gm"));
													 */

		synth.loadAllInstruments(sb);

		synth.unloadAllInstruments(synth.getDefaultSoundbank());
		// synth.loadAllInstruments(MidiSystem.getSoundbank(new File("Donkey Kong
		// Country 2014.sf2")));
		// synth.loadAllInstruments(MidiSystem.getSoundbank(new File("Square.sf2")));

		sequencer.getTransmitter().setReceiver(synth.getReceiver());

		Constants.table(synth);

	}

	private static void synthesize() {
		Write.midi(toto, "Chorale.mid");
		File myMidiFile = new File("Chorale.mid");
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(myMidiFile);
		} catch (InvalidMidiDataException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			sequencer.open();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sequencer.setSequence(sequence);
		} catch (InvalidMidiDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		totalTime = sequencer.getMicrosecondLength();
	}

}