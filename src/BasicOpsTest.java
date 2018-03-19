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
import javafx.event.EventHandler;
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
import javafx.scene.text.TextAlignment;

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

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle("jMaestro");

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
				stopAction();
			} else {
				isPlaying = true;
				startAction();
			}

			sequencer.addMetaEventListener(new MetaEventListener() {
				public void meta(MetaMessage event) {
					if (event.getType() == 47) {
						playButton.setText("Play");
					}

				}
			});

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

		// root.getChildren().add(scroller);

		// Pane sp = new Pane();
		// StackPane scroll = new StackPane(sp);
		// sp.getChildren().addAll(content, rect);
		// scroller.setContent(sp);
		// scroll.getChildren().add(scroller);
		//
		// rect.toFront();

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
					
					stopAction();

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

		Executor executor = Executors.newSingleThreadScheduledExecutor();
		executor.execute(task);

		try {
			task.get(5, TimeUnit.SECONDS);
		} catch (Exception ex) {
			// Handle your exception
		}

		tempoLabel.setAlignment(Pos.CENTER);

		// scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> {
		// double xTranslate = newValue.doubleValue()
		// * (scrollPane.getViewportBounds().getHeight() - content.getHeight());
		// content.translateXProperty().setValue(-xTranslate);
		// });
		// // Allow horizontal scrolling of fixed element:
		// scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
		// double yTranslate = newValue.doubleValue()
		// * (scrollPane.getViewportBounds().getHeight() - content.getHeight());
		// content.translateYProperty().setValue(-yTranslate);
		// });

		// GridPane.setHgrow(content, Priority.ALWAYS);
		// GridPane.setVgrow(content, Priority.ALWAYS);

		// scroller.minWidthProperty().bind(content.minWidthProperty());

		// content.getChildren().add(rect);
		// root.getChildren().addAll(content);

		int delay = 1000; // delay for 5 sec.
		int interval = midiex.tempo * 10 / 4; // iterate every sec.
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {

				Random random = new Random();
				int nextInt = random.nextInt(256 * 256 * 256);
				String colorCode1 = String.format("#%06x", nextInt);

				int nextInt2 = random.nextInt(256 * 256 * 256);
				String colorCode2 = String.format("#%06x", nextInt2);

				content.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%," + colorCode1 + ", "
						+ colorCode2 + ")");
			}
		}, delay, interval);

		Label ol = new Label("Outer Layer");
		bp = new BorderPane(innerBP, null, addButton, null, ol);

		MenuBar menuBar = new MenuBar();

		// --- Menu File
		Menu menuFile = new Menu("File");
		MenuItem importFile = new MenuItem("Import");
		importFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				importAction();
			}
		});

		menuFile.getItems().addAll(importFile);

		// --- Menu Edit
		Menu menuEdit = new Menu("Edit");

		menuBar.getMenus().addAll(menuFile, menuEdit);
		bp.setTop(menuBar);

		HBox menu = new HBox(playButton, tempoLabel, tempoField);
		VBox holder = new VBox(menuBar, menu);
		bp.setTop(holder);
		Scene scene = new Scene(bp, width, height);

		// Scene scene = new Scene(new BorderPane(layout, playButton, null, null, null),
		// width, height);

		// Stage stage = new Stage();

		primaryStage.setScene(scene);
		primaryStage.show();

		// rect.layoutYProperty().bind(
		// // to vertical scroll shift (which ranges from 0 to 1)
		// scroller.vvalueProperty()
		// // multiplied by (scrollableAreaHeight - visibleViewportHeight)
		// .multiply(
		// content.heightProperty()
		// .subtract(
		// new ScrollPaneViewPortWidthBinding(scroller))));

		content.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #dc143c, #661a33)");
		bp.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #0091FF, #661a33)");

		// MouseGestures mg = new MouseGestures();
		// mg.makeDraggable(rect);
		// add some borders to visualise the element' locations

		// this is where the transparency is achieved:
		// the three layers must be made transparent
		// (i) make the VBox transparent (the 4th parameter is the alpha)
		// p.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
		// (ii) set the scene fill to transparent

		// (iii) set the stage background to transparent

		// root.getChildren().add(canvas);
		// root.getChildren().add(ticker);
		// primaryStage.setScene(new Scene(root));
		// primaryStage.show();

		// int[] pitchSop = { C3, G4, E4, D4, G4, A4, C4, D4, E4, D4, F4, E4, A4, G4, E4
		// };
		// double[] rhythmSop = { C, C, DC, Q, C, C, C, C, C, C, C, C, M, C, Q };
		// Part soprano = new Part();
		// // create the jMusic phrase objects
		// Phrase soprano = new Phrase();
		//
		// // add the notes to each phrase
		// soprano.addNoteList(pitchSop, rhythmSop);
		//
		// // create jMusic parts
		//
		// Part s = new Part("Tuba", TROMBONE, 1);
		//
		// // add the phrases to the parts
		// s.addPhrase(soprano);
		//
		// // create a score
		// score = new Score("Chorale");
		//
		// // add the parts to the score
		// score.addPart(s);
		//
		// // display the result for the world to see
		// // save score as a MIDI file
		// Write.midi(score, "Chorale.mid");
		// Play.midi(score);

		// content.getChildren().add(rect);

		// gc = canvas.getGraphicsContext2D();
		// gc.setFill(Color.CORNSILK);
		// gc.fillRect(canvas.getLayoutX(), canvas.getLayoutY(), canvas.getWidth(),
		// canvas.getHeight());
		// canvas.setOpacity(1);

		//
		// AnimationTimer timer = new AnimationTimer() {
		// @Override
		// public void handle(long now) {
		// gc = canvas.getGraphicsContext2D();
		// gc.setFill(Color.CORNSILK);
		// gc.fillRect(0, 0, 300, 300);
		// paintNotes(pitchSop, rhythmSop);
		//
		// }
		// };

		// sequencer.start();
		// // timer.start();
		// timeline.play();
		//
		// sequencer.addMetaEventListener(new MetaEventListener() {
		// public void meta(MetaMessage event) {
		// if (event.getType() == 47) {
		// sequencer.stop();
		// // timer.stop();
		//
		// }
		// }
		// });

		// ScrollPane sp = new ScrollPane();
		//
		// root.getChildren().add(canvas);
		// root.getChildren().add(rect);
		// primaryStage.setScene(new Scene(root));
		// primaryStage.show();

		// Runnable r = new Runnable() {
		// public void run() {
		//
		// ActionListener updateListener = new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		//
		// Line x = new Line(xpos, 0, xpos, 300);
		//
		// }
		// };
		// Timer timer = new Timer(40, updateListener);
		// sequencer.start();
		// timer.start();
		// sequencer.close();
		// timer.stop();
		// }
		// };
		// SwingUtilities.invokeLater(r);
	}

	private static void startAction() {

		if (isPlaying) {
			toto.empty();
			toto.addPartList(midiex.getPart());
			rect.setHeight(content.getHeight());
			toto.setTempo(midiex.tempo);
			playButton.setText("Stop");
			synthesize();

			sequencer.setMicrosecondPosition((long) currentTime);
			sequencer.start();

			// System.out.println("Total time is: " + totalTime);
			timeLine();
			scrollingBarLock();

			barTimeLine.play();
			scrollingTimeLine.play();
		}

	}

	private static void stopAction() {

		currentTime = sequencer.getMicrosecondPosition();
		currentRectX = rect.getTranslateX();

		sequencer.stop();
		barTimeLine.stop();
		scrollingTimeLine.stop();
		playButton.setText("Play");

	}

	public static long convertToMicro(long curr) {

		long ratio = (long) (curr / content.getWidth() * totalTime);
		System.out.println(ratio / 1000000);
		return ratio;
	}

	public static void setSequencer(double curr) {

		stopAction();
		System.out.println("The x is: " + curr);
		rect.translateXProperty().set(curr);
		System.out.println("The x for rect is: " + rect.getX());
		currentRectX = curr;
		currentTime = convertToMicro((long) curr);
		sequencer.setMicrosecondPosition((long) currentTime);
		System.out.println("The sequencer is at microsecond length: " + sequencer.getMicrosecondLength());
		startAction();
	}

	public static void timeLine() {
		Duration songTime = Duration.millis((totalTime - currentTime) / 1000);
		barTimeLine = new Timeline(
				new KeyFrame(Duration.millis(0), new KeyValue(rect.translateXProperty(), currentRectX),
						new KeyValue(rect.translateYProperty(), 0)),

				// new KeyFrame(Duration.millis((totalTime / 2) / 1000),
				// new KeyValue(rect.translateXProperty(), (content.getWidth() / 2) - 1),
				// new KeyValue(rect.translateYProperty(), 0)),

				new KeyFrame(songTime, new KeyValue(rect.translateXProperty(), content.getWidth() - 1),
						new KeyValue(rect.translateYProperty(), 0)));

		System.out.println(totalTime / 1000);

	}

	public static void scrollingBarLock() {
		Duration songTime = Duration.millis((totalTime - currentTime) / 1000);

		scrollingTimeLine = new Timeline(
				new KeyFrame(Duration.millis(0),
						new KeyValue(scrollPane.hvalueProperty(), currentRectX / content.getWidth())),
				// new KeyFrame(Duration.millis((totalTime / 2) / 1000),
				// new KeyValue(scroller.hvalueProperty(), (scroller.getHmax() / 2) - 1)),

				new KeyFrame(songTime, new KeyValue(scrollPane.hvalueProperty(), 1.0)));

	}

	private void paintNotes() {
		panes = midiex.getPaneList();

		for (int i = 0; i < panes.size(); i++) {
			content.add(panes.get(curr_pane), 0, curr_pane);
			curr_pane++;

		}
	}

	public void importAction() {

		// bp.setStyle("-fx-background-color: #F0591E;");
		if (toto != null) {
			toto.clean();

			toto.setTempo(midiex.tempo);
		}
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