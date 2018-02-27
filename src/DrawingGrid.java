import java.util.Random;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jm.*;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.Write;

public class DrawingGrid extends Application implements JMC {

	boolean showHoverCursor = true;

	int rows = 127;
	int columns = 64;
	double width = 800;
	double height = 600;
	int numberOfQuartersForAMeasure = 4;
	TrackNotes tn;

	Part part;
	Phrase phrase;

	Grid grid;

	@Override
	public void start(Stage primaryStage) {
		try {

			VBox content = new VBox(5);
			ScrollPane scroller = new ScrollPane(content);
			scroller.setFitToWidth(false);
			scroller.setFitToHeight(false);

			Button addButton = new Button("Write");
			addButton.setOnAction(e -> {
				Score score = new Score("Chorale");
				score.removeAllParts();
				part.removeAllPhrases();
				part = tn.getPart();
				part.setInstrument(HARP);
				// add the parts to the score
				score.addPart(part);
				score.setTempo(120);

				// display the result for the world to see
				// save score as a MIDI file
				Write.midi(score, "Chorale.mid");

			});

			Button playButton = new Button("Play!");
			playButton.setOnAction(e -> {

				Play.mid("Chorale.mid");

			});

			Button importButton = new Button("Import");
			importButton.setOnAction(e -> {

				importSong("Africa.mid");

			});
			Scene scene = new Scene(new BorderPane(scroller, addButton, playButton, importButton, null), width, height);

			part = new Part("Piano", HARP, 0);

			// create the jMusic phrase objects

			tn = new TrackNotes(part);
			// add the notes to each phrase

			// create grid
			grid = new Grid(columns, rows, 2000, 2000);

			MouseGestures mg = new MouseGestures();

			// fill grid
			for (int row = 0; row < rows; row++) {
				for (int column = 0; column < columns; column++) {

					Cell cell = new Cell(column, row);

					mg.makePaintable(cell);

					grid.add(cell, column, row);
				}
			}

			content.getChildren().addAll(grid);

			// create scene and stage
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importSong(String s) {

		Score toto = ReadMIDI.importMIDItoScore("Africa.mid");
		Part[] temp = toto.getPartArray();
		for (int i = 0; i < 1; i++) {

			Note[] n = null;
			Phrase p = temp[i].getPhrase(0);

			// for(int i =0; i < p.getNoteArray().length; i++)

			n = p.getNoteArray();

			for (int u = 0; u < 120; u++) {
				int row = ((n[i].getPitch() * -1) + 127);
				int col = (int) p.getNoteStartTime(u) - 32;
				Cell c = grid.cells[row][col];
				addNote(c, n[i].getDuration());
			}

		}

	}

	public void addNote(Cell c, double noteLength) {
		Note tempNote = new Note(c.row * -1 + 127, noteLength);
		// Play.midi(tempNote);
		if (c.getChildren().isEmpty()) {
			c.getChildren().add(new MIDINoteBar(0, 0, 10, 10, tempNote, c.column));
			tn.addNote(tempNote, c.column);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private class Grid extends Pane {

		int rows;
		int columns;

		double width;
		double height;

		Cell[][] cells;

		public Grid(int columns, int rows, double width, double height) {

			this.columns = columns;
			this.rows = rows;
			this.width = width;
			this.height = height;

			cells = new Cell[rows][columns];

		}

		/**
		 * Add cell to array and to the UI.
		 */
		public void add(Cell cell, int column, int row) {

			cells[row][column] = cell;

			double w = width / columns;
			double h = height / rows;
			double x = w * column;
			double y = h * row;

			cell.setLayoutX(x);
			cell.setLayoutY(y);
			cell.setPrefWidth(w);
			cell.setPrefHeight(h);
			cell.setOpacity(.9);

			getChildren().add(cell);

		}

		public Cell getCell(int column, int row) {
			return cells[row][column];
		}

		/**
		 * Unhighlight all cells
		 */
		public void unhighlight() {
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < columns; col++) {
					cells[row][col].unhighlight(cells[row][col]);
				}
			}
		}
	}

	private class Cell extends StackPane {

		int column;
		int row;

		public Cell(int column, int row) {

			this.column = column;
			this.row = row;

			getStyleClass().add("cell");

			setOpacity(0.9);
		}

		public void highlight(Cell c) {
			// ensure the style is only once in the style list
			getStyleClass().remove("cell-highlight");

			// add style
			getStyleClass().add("cell-highlight");

			addNote(c, QN);

		}

		public void unhighlight(Cell c) {
			double time = 0;
			Note noteToRemove = null;
			getStyleClass().remove("cell-highlight");
			if (c.getChildren().get(0) != null) {
				Node removeNode = c.getChildren().get(0);
				if (removeNode instanceof MIDINoteBar) {
					time = ((MIDINoteBar) removeNode).getStartTime();
					noteToRemove = ((MIDINoteBar) removeNode).getNote();
				}

				c.getChildren().clear();

				tn.deleteNote(noteToRemove, time);
			}
		}

		public void hoverHighlight() {
			// ensure the style is only once in the style list
			getStyleClass().remove("cell-hover-highlight");

			// add style
			getStyleClass().add("cell-hover-highlight");
		}

		public void hoverUnhighlight() {
			getStyleClass().remove("cell-hover-highlight");
		}

		public String toString() {
			return this.column + "/" + this.row;
		}
	}

	public class MouseGestures {

		public void makePaintable(Node node) {

			// that's all there is needed for hovering, the other code is just for painting
			if (showHoverCursor) {
				node.hoverProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {

						//System.out.println(observable + ": " + newValue);

						if (newValue) {
							((Cell) node).hoverHighlight();
						} else {
							((Cell) node).hoverUnhighlight();
						}

					}

				});
			}

			node.setOnMousePressed(onMousePressedEventHandler);
			node.setOnDragDetected(onDragDetectedEventHandler);
			node.setOnMouseDragEntered(onMouseDragEnteredEventHandler);

		}

		EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

			Cell cell = (Cell) event.getSource();

			if (event.isPrimaryButtonDown()) {
				cell.highlight(cell);
			} else if (event.isSecondaryButtonDown()) {
				cell.unhighlight(cell);
			}
		};

		EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

			PickResult pickResult = event.getPickResult();
			Node node = pickResult.getIntersectedNode();

			if (node instanceof Cell) {

				Cell cell = (Cell) node;

				if (event.isPrimaryButtonDown()) {
					cell.highlight(cell);
				} else if (event.isSecondaryButtonDown()) {
					cell.unhighlight(cell);
				}

			}

		};

		EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
		};

		EventHandler<MouseEvent> onDragDetectedEventHandler = event -> {

			Cell cell = (Cell) event.getSource();
			cell.startFullDrag();

		};

		EventHandler<MouseEvent> onMouseDragEnteredEventHandler = event -> {

			Cell cell = (Cell) event.getSource();

			if (event.isPrimaryButtonDown()) {
				cell.highlight(cell);
			} else if (event.isSecondaryButtonDown()) {
				cell.unhighlight(cell);
			}

		};

	}

}