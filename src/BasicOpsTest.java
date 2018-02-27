import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Vector;
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
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jm.JMC;
import jm.music.data.*;
import jm.music.*;
import jm.util.*;
import jm.gui.show.*;

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

	GridPane content;

	ArrayList<Pane> paneList = new ArrayList();

	ScrollPane scroller;

	MIDINoteExtractor midiex;

	Score toto;

	double totalTime;

	Rectangle rect;

	Timeline barTimeLine;

	File workingSongFile;

	TextField tempoField;

	Label tempoLabel;
	Sequencer sequencer;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Drawing Operations Test");

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		// canvas = new Pane();
		// Canvas canvas2 = new Canvas(800, 100);

		content = new GridPane();
		midiex = new MIDINoteExtractor();

		content.setVgap(5);

		// HBox musicContents = new HBox(2);
		// HBox.setHgrow(canvas, Priority.ALWAYS);
		// musicContents.setMaxWidth(60000);

		// musicContents.getChildren().addAll(button1, button2);

		// content.getChildren().add(musicContents);

		rect = new Rectangle(0, 0, 2, 400);
		// StackPane.setAlignment(rect, Pos.TOP_LEFT);
		// root.getChildren().addAll(rect, glass);

		// layout.getChildren().addAll(content, rect);

		// glass.setStyle("-fx-background-color: rgba(0, 0, 70, 0.5);
		// -fx-background-radius: 10;");

		// root.getChildren().addAll(content, rect);

		scroller = new ScrollPane();
		scroller.setFitToWidth(false);
		scroller.setFitToHeight(false);
		scroller.setMinWidth(width);

		// scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Horizontal
		// scroll bar
		// scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical
		// scroll bar

		// glass.getChildren().addAll(p, rect);

		//

		// root.getChildren().addAll(scroller, rect);
		// VBox.setVgrow(canvas, Priority.ALWAYS);

		sequencer = MidiSystem.getSequencer();

		Button playButton = new Button("Play!");
		playButton.setOnAction(e -> {

			Thread thread = new Thread() {
				public void run() {

					try {
						toto = midiex.extract(workingSongFile.toPath());
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					rect.setHeight(content.getHeight());
					toto.empty();
					toto.addPartList(midiex.getPart());
					toto.setTempo(midiex.tempo);
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

					if (sequencer.isRunning()) {
						sequencer.stop();
						barTimeLine.stop();
					} else {

						sequencer.start();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						totalTime = sequencer.getMicrosecondLength();
						timeLine();
						barTimeLine.play();

					}

					sequencer.addMetaEventListener(new MetaEventListener() {
						public void meta(MetaMessage event) {
							if (event.getType() == 47) {
								// Sequencer is done playing
							}

						}
					});

				}

			};

			thread.start();

			try {
				thread.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});

		Button addButton = new Button("+");
		addButton.setOnAction(e -> {

			MIDINoteBar temp = midiex.getMNB();
			Note n = temp.getNote();
			n.setPitch(n.getPitch() + 1);
			temp.setNote(n);
			midiex.setMNB(temp);

		});
//		
//		Button deleteButton = new Button("Delete");
//		deleteButton.setOnAction(e -> {
//
//			midiex.getMNB().clear();
//			
//
//		});


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

		ScrollPane scrollPane = new ScrollPane();
		Pane pane = new Pane();
		// pane.setMinHeight(1000);
		// pane.setMinWidth(1000);
		scrollPane.setContent(pane);

		pane.getChildren().add(content);
		pane.getChildren().add(rect);
		root.getChildren().add(scrollPane);

		Label test = new Label("Test");
		tempoField = new TextField("128");
		tempoLabel = new Label("Tempo:");
		BorderPane innerBP = new BorderPane(root, null, null, null, test);

		tempoField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,3}?")) {
					tempoField.setText(oldValue);
				}

				else {
					tempoField.setText(newValue);
					midiex.tempo = (Integer.parseInt(newValue));
				}
			}
		});

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

		Button importButton = new Button("Import");
		importButton.setOnAction(e -> {

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
			rect.setHeight(content.getHeight());

		});

		Label ol = new Label("Outer Layer");
		HBox menu = new HBox(playButton, tempoLabel, tempoField);
		BorderPane bp = new BorderPane(innerBP, menu, addButton, importButton, ol);

		Scene scene = new Scene(bp, width, height);

		// Scene scene = new Scene(new BorderPane(layout, playButton, null, null, null),
		// width, height);

		// Stage stage = new Stage();

		primaryStage.setScene(scene);
		primaryStage.show();

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

		synth();

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

	public void timeLine() {
		Duration songTime = Duration.millis(totalTime / 1000);
		barTimeLine = new Timeline(
				new KeyFrame(Duration.seconds(0), new KeyValue(rect.translateXProperty(), content.getTranslateX()),
						new KeyValue(rect.translateYProperty(), 0)),

				new KeyFrame(Duration.millis((totalTime / 2) / 1000),
						new KeyValue(rect.translateXProperty(), (content.getWidth() / 2) - 1),
						new KeyValue(rect.translateYProperty(), 0)),

				new KeyFrame(songTime, new KeyValue(rect.translateXProperty(), content.getWidth() - 1),
						new KeyValue(rect.translateYProperty(), 0)));

		System.out.println(totalTime / 1000);

	}

	private void paintNotes() {
		ArrayList<MIDIPane> panes = midiex.getPaneList();

		for (int i = 0; i < panes.size(); i++) {
			content.add(panes.get(curr_pane), 0, curr_pane);
			curr_pane++;

		}
	}

	// private double trackLength(double[] x) {
	// double temp = 0;
	//
	// for (int i = 0; i < x.length; i++) {
	// temp += x[i] * 10;
	// }
	// return temp;
	// }

	public void synth() throws Exception {

		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();

		// Soundbank sb = synth.getDefaultSoundbank();/*getSoundbank(new
		// File("./soundbank-deluxe.gm"));*/

		// synth.loadAllInstruments(sb);

		synth.unloadAllInstruments(synth.getDefaultSoundbank());
		 synth.loadAllInstruments(MidiSystem.getSoundbank(new File("Donkey Kong Country 2014.sf2")));
		//synth.loadAllInstruments(MidiSystem.getSoundbank(new File("Square.sf2")));

		sequencer.getTransmitter().setReceiver(synth.getReceiver());

	}

}