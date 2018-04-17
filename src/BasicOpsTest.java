
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import java.util.concurrent.Callable;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.InvalidMidiDataException;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;

import javafx.geometry.Pos;
import javafx.scene.Group;

import javafx.scene.Scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jm.JMC;
import jm.constants.Pitches;
import jm.music.data.*;

import jm.util.*;

import java.util.Optional;

import javafx.event.ActionEvent;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.concurrent.*;

public class BasicOpsTest extends Application implements JMC, Pitches {

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

	ArrayList<Pane> paneList = new ArrayList<Pane>();

	static MIDINoteExtractor midiex;

	static Score toto;

	static double totalTime;

	static Rectangle rect;

	static volatile Timeline barTimeLine;
	static volatile Timeline scrollingTimeLine;

	private static volatile Timeline time;

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

	static Stage primaryStage;

	static ZoomableScrollPane scrollPane;

	static int den;
	static int num;

	static MeasurePane measures;

	static VBox HUDcontent;

	static double currentTime;

	static double currentRectX;

	static Button playButton;

	static boolean isPlaying;

	static MIDIPane myCurrentMidiPane;

	volatile static ExecutorService executor;

	boolean first = false;

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

		Constants.pitchTable();
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

		Button addButton = new Button("Add Track");
		addButton.setOnAction(e -> {

			addTrack();

			// ArrayList<MIDINoteBar> temp = midiex.getMNB();
			// for (int i = 0; i < temp.size(); i++) {
			// Note n = temp.get(i).getNote();
			// n.setPitch(n.getPitch() + 1);
			// temp.get(i).setNote(n);
			// midiex.setMNB(temp);
			//
			// }

		});

		Button deleteButton = new Button("Delete Notes");
		deleteButton.setOnAction(e -> {

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete?");
			Optional<ButtonType> action = alert.showAndWait();

			if (action.get() == ButtonType.OK) {

				ArrayList<MIDINoteBar> temp = midiex.getMNB();
				for (int i = 0; i < temp.size(); i++) {

					midiex.getMNB().get(i).clear();
				}
			}

			if (action.get() == ButtonType.CANCEL) {
				alert.close();
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
		innerBP = new BorderPane(root, null, null, null, null);

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

		VBox editContainer = new VBox();
		editContainer.getChildren().addAll(addButton, deleteButton);
		bp = new BorderPane(innerBP, null, editContainer, null, null);

		MenuBar menuBar = new MenuBar();

		// --- Menu File
		Menu menuFile = new Menu("File");

		MenuItem newFile = new MenuItem("New File");
		newFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								toto = new Score();
								String path = System.getProperty("user.dir");
								File file = new File(path + "/Untitled.mid");
								importAction(file.toPath());

							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		MenuItem importFile = new MenuItem("Import");
		importFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								FileChooser fileChooser = new FileChooser();

								FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
										"MIDI files (*.mid)", "*.mid");
								fileChooser.getExtensionFilters().add(extFilter);
								Path s = null;
								File file = fileChooser.showOpenDialog(primaryStage);
								if (file != null) {
									s = file.toPath();
								}
								importAction(s);

							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		MenuItem soundFont = new MenuItem("Use Soundfont");
		soundFont.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								FileChooser fileChooser = new FileChooser();

								FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
										"soundfont files (*.sf2)", "*.sf2");
								fileChooser.getExtensionFilters().add(extFilter);
								Path s = null;
								File file = fileChooser.showOpenDialog(primaryStage);
								if (file != null) {
									s = file.toPath();
								}
								soundFont(s);

							}

							private void soundFont(Path s) {
								synth.unloadAllInstruments(synth.getDefaultSoundbank());
								try {
									synth.loadAllInstruments(MidiSystem.getSoundbank(s.toFile()));
								} catch (InvalidMidiDataException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		MenuItem saveFile = new MenuItem("Save");
		saveFile.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								saveAction();

							}

							private void saveAction() {

								FileChooser fileChooser = new FileChooser();

								FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
										"MIDI files (*.mid)", "*.mid");
								fileChooser.getExtensionFilters().add(extFilter);

								File file = fileChooser.showSaveDialog(primaryStage);

								fileChooser.setTitle("Save Image");

								if (file != null) {
									Write.midi(toto, file.getAbsolutePath());
								}

							}
						});

						return null;
					}
				};

				executor.execute(task);

			}
		});

		menuFile.getItems().addAll(newFile, importFile, saveFile, soundFont);

		// --- Menu Edit
		Menu menuEdit = new Menu("Edit");

		MenuItem edit = new MenuItem("Edit");
		edit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				editAction();
			}

			@SuppressWarnings("unchecked")
			private void editAction() {
				ListView<String> temp = Constants.mListView;
				ObservableList<String> oList = Constants.observableList;
				Task<Void> task = new Task<Void>() {
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

								FilteredList<String> filteredData = new FilteredList<>(oList, s -> true);

								TextField filterInput = new TextField();
								filterInput.textProperty().addListener(obs -> {
									String filter = filterInput.getText();
									if (filter == null || filter.length() == 0) {
										filteredData.setPredicate(s -> true);
									} else {
										filteredData.setPredicate(s -> s.contains(filter.toUpperCase()));
									}

									temp.setItems(filteredData);
								});

								// create a list of items.

								temp.getSelectionModel().selectedItemProperty()
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
								listViewPanel.getChildren().addAll(temp, label, filterInput);
								instedit.getChildren().addAll(listViewPanel);

								Scene dialogScene = new Scene(instedit, 800, 400);
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

		MenuItem measureEdit = new MenuItem("Delete Track");
		measureEdit.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				if (myCurrentMidiPane != null) {
					myCurrentMidiPane.myPart.empty();
					toto.removePart(myCurrentMidiPane.myPart);
					Task task = new Task<Void>() {
						@Override
						public Void call() {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									panes.remove(myCurrentMidiPane);
									content.getChildren().clear();
									paintNotes();
								}
							});

							return null;
						}
					};

					executor.execute(task);

				}
			}
		});

		menuEdit.getItems().addAll(edit, measureEdit);

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

							double numMeasures = ((totalTime / 1000000) / 60 * midiex.tempo) / num;
							measures = new MeasurePane(num, den, numMeasures);
							System.out.println(numMeasures + "NUMBER OF MEASURES");

							// content.setPrefWidth(content.getChildren().get(0).getLayoutBounds().getWidth());

							HUDcontent.getChildren().clear();
							HUDcontent.getChildren().addAll(measures, content);

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

	public void addTrack() {

		Task task = new Task<Void>() {
			@Override
			public Void call() {

				Platform.runLater(new Runnable() {

					@SuppressWarnings("unchecked")
					@Override
					public void run() {

						ObservableList oList = Constants.addObservableList;

						ListView temp = Constants.addListView;

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

						FilteredList<String> filteredData = new FilteredList<>(oList, s -> true);

						TextField filterInput = new TextField();
						filterInput.textProperty().addListener(obs -> {
							String filter = filterInput.getText();
							if (filter == null || filter.length() == 0) {
								filteredData.setPredicate(s -> true);
							} else {
								filteredData.setPredicate(s -> s.contains(filter.toUpperCase()));
							}

							temp.setItems(filteredData);
						});

						// create a list of items.
						if (first == false) {
							temp.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

								public void changed(ObservableValue<? extends String> observable, String oldValue,
										String newValue) {
									// change the label text value to the newly selected
									// item.
									label.setText(newValue);
									Part p = new Part();
									midiex.addPart(p);
									MIDIPane t = new MIDIPane(p);
									toto.clean();
									toto.addPartList(midiex.getPart());

									if (t != null)
										t.setInstrument((int) Constants.getKeyFromValue(newValue));
									midiex.addPane(t);
									paintNotes();

									System.out.print(toto.toString());
								}
							});
						}
						Group instedit = new Group();
						listViewPanel.getChildren().addAll(temp, label, filterInput);
						instedit.getChildren().addAll(listViewPanel);

						Scene dialogScene = new Scene(instedit, 800, 400);
						dialog.setScene(dialogScene);
						dialog.show();

						first = true;

					}
				});

				return null;
			}
		};

		executor.execute(task);
	}

	private void flushExecutor() {

		executor.shutdown();
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	}

	public static void noteEdit(MIDINoteBar m) {

		Stage dialog = new Stage();
		dialog.initModality(Modality.NONE);
		dialog.initOwner(primaryStage);

		Group noteEdit = new Group();

		VBox box = new VBox();
		box.setSpacing(5);

		Note temp = m.getNote();

		// DURATION
		HBox duration = new HBox();
		Label durationLabel = new Label("The Duration is: ");
		double rhythm = temp.getRhythmValue();
		rhythm = Math.round(rhythm * 100);
		rhythm = rhythm / 100;
		TextField durationTF = new TextField(rhythm + "");
		durationTF.textProperty().addListener((observable, oldValue, newValue) -> {
			double d;
			try {
				d = Double.parseDouble(newValue);
				m.setDuration(d);
			} catch (NullPointerException | NumberFormatException ex) {
				// Not valid double
			}

		});
		duration.getChildren().addAll(durationLabel, durationTF);

		// PITCH

		HBox pitch = new HBox();
		Label pitchLabel = new Label("The Pitch is: ");

		TextField pitchTF = new TextField(Constants.getKeyFromValuePitch(m.getNote().getPitch()));

		pitchTF.textProperty().addListener((observable, oldValue, newValue) -> {

			newValue = newValue.toUpperCase();
			if (Constants.pitchTable.containsKey(newValue)) {
				m.setPitch(Constants.pitchTable.get(newValue));
			}

		});
		pitch.getChildren().addAll(pitchLabel, pitchTF);

		box.getChildren().addAll(duration, pitch);
		Scene dialogScene = new Scene(box, 400, 400);
		dialog.setScene(dialogScene);
		dialog.show();

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

						playButton.setText("Play");
					}
				});

				return null;
			}
		};

		executor.execute(task);
		currentTime = sequencer.getMicrosecondPosition();
		currentRectX = rect.getTranslateX();

		sequencer.stop();
		if (barTimeLine != null)
			barTimeLine.stop();
		if (scrollingTimeLine != null)
			scrollingTimeLine.stop();

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

				new KeyFrame(songTime, new KeyValue(rect.translateXProperty(), content.getWidth() - 1),
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
		content.getChildren().clear();
		panes = midiex.getPaneList();
		curr_pane = 0;
		for (int i = 0; i < panes.size(); i++) {

			System.out.println("Your size is " + i);

			content.add(panes.get(i), 0, i);
			curr_pane++;

		}
	}

	// This handles everything to import a song
	public void importAction(Path s) {

		flushExecutor();
		currentRectX = 0;
		currentTime = 0;
		rect.translateXProperty().set(currentRectX);
		// bp.setStyle("-fx-background-color: #F0591E;");

		midiex = new MIDINoteExtractor();

		workingSongFile = s.toFile();
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

		// measures.setMaxWidth(content.getChildren().get(0).getTranslateX());
		HUDcontent.getChildren().clear();
		HUDcontent.getChildren().addAll(measures, content);

		// panes.get(0).prefWidthProperty().bind(measures.widthProperty());

		System.out.println(measures.widthProperty() + "WIDTH PROPERTY");
		// midiex.extendPanes();

	}

	public void synth() throws Exception {
		synth = MidiSystem.getSynthesizer();
		synth.open();

		Soundbank sb = synth.getDefaultSoundbank();

		synth.loadAllInstruments(sb);

		// synth.unloadAllInstruments(synth.getDefaultSoundbank());

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
