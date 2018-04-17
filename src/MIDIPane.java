import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;

interface NoteChangeListener {
	void noteChanged();
}

public class MIDIPane extends Pane implements JMC {

	MIDIPane instance = this;

	ArrayList<MIDINoteBar> notes = new ArrayList<MIDINoteBar>();

	String partName = "Error";

	int instrument = 0;

	Part myPart;

	double Pan;

	Label e;

	boolean selected;

	private final Set<KeyCode> pressedKeys = new HashSet<>();

	public MIDIPane(Part s) {
		super();
		myPart = s;

		Pan = s.getPan();
		MouseGestures mg = new MouseGestures();
		mg.makeClickable(this);
		int temp = s.getChannel();

		this.setMaxHeight(127);
		this.setPrefHeight(127);
		this.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));

		// this.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
		// this.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

		if (temp == 9) {
			partName = "Drums";
			myPart.setTitle("Drums");
		}

		else {
			instrument = s.getInstrument();

			partName = Constants.midiTable.getOrDefault(s.getInstrument(), "Error");

			myPart.setTitle(partName);
		}
		e = new Label(partName);
		e.setAlignment(Pos.TOP_LEFT);
		this.getChildren().add(e);

	}

	public void addNotes(ArrayList<MIDINoteBar> n) {
		notes.addAll(n);
	}

	public void addNote(MIDINoteBar n) {
		notes.add(n);

	}

	public void deleteNote(MIDINoteBar n) {
		n.getNote().getMyPhrase().empty();
		notes.remove(n);

	}

	public void setPan(double n) {
		Pan = n;
		myPart.setPan(n);

	}

	public void editNote(MIDINoteBar n, MIDINoteBar newNote) {
		n = newNote;

	}

	public String getPartInstrument() {
		return partName;
	}

	public void setInstrument(int i) {

		instrument = i;
		myPart.setInstrument(instrument);
		partName = Constants.midiTable.getOrDefault(i, "Error");
		myPart.setTitle(partName);
		e.setText(partName);

	}

	public void setSelected(boolean sel) {

		selected = sel;
		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (instance != null) {
							if (sel == false) {
								instance.setBackground(new Background(
										new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));
							}

							else {
								instance.setBackground(new Background(
										new BackgroundFill(Color.LIGHTCYAN, CornerRadii.EMPTY, Insets.EMPTY)));

							}
						}
					}
				});

				return null;
			}
		};

		BasicOpsTest.executor.execute(task);

	}

	private class MouseGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;

		public void makeClickable(Node node) {
			node.setOnMousePressed(circleOnMousePressedEventHandler);
			node.setOnMouseDragged(circleOnMouseDraggedEventHandler);
		}

		EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				if (t.getButton() == MouseButton.PRIMARY && t.isShiftDown()) {
					System.out.println("Shift clicked");
				}

				if (t.getButton() == MouseButton.PRIMARY && t.isShiftDown()) {
					orgSceneX = t.getSceneX();
					orgSceneY = t.getSceneY();

					if (t.getSource() instanceof MIDIPane) {

						Node z = ((Node) (t.getSource()));
						MIDIPane p = (MIDIPane) z;
						if (p != null && BasicOpsTest.myCurrentMidiPane != null
								&& p != BasicOpsTest.myCurrentMidiPane) {

							BasicOpsTest.myCurrentMidiPane.setSelected(false);

						}
						double x = p.getLayoutX();
						double y = p.getTranslateY();

						System.out.println("X=" + (int) (t.getX() - x) + " Y=" + (int) (t.getY() - y) + "");

						System.out.println(((MIDIPane) p).getPartInstrument() + "with the pan" + Pan);

						BasicOpsTest.myCurrentMidiPane = (MIDIPane) p;

						BasicOpsTest.myCurrentMidiPane.setSelected(true);

					}

				} else if (t.getButton() == MouseButton.SECONDARY && t.isShiftDown()) {
					orgSceneX = t.getSceneX();
					orgSceneY = t.getSceneY();

					if (t.getSource() instanceof MIDIPane) {

						if (BasicOpsTest.myCurrentMidiPane != null)
							BasicOpsTest.myCurrentMidiPane.setSelected(false);

					}

					// Node p = ((Node) (t.getSource()));

					// orgTranslateX = p.getTranslateX();
					// orgTranslateY = p.getTranslateY();

				}

				else if (t.getButton() == MouseButton.PRIMARY && t.isShortcutDown()) {

					instance = BasicOpsTest.myCurrentMidiPane;
					double x = instance.getTranslateX();
					double y = instance.getTranslateY();

					int fx = (int) (t.getX() - x);
					int fy = (int) (t.getY() - y);

					if (y >= 0 && y < 127 && x >= 0 && x < instance.getWidth()) {
						double length = 1;
						Note note = new Note();
						note.setPitch((fy * -1) + 127);
						Phrase phrase = new Phrase();
						phrase.add(note);
						phrase.setStartTime(fx / 20);
						MIDINoteBar n = new MIDINoteBar(fx / 20, fy * -1 + 127, length, 3, note, fx / 20);
						MIDINoteExtractor.midibar.makeDraggable(n);
						n.setPane(instance);
						instance.addNote(n);
						myPart.add(phrase);

						Task task = new Task<Void>() {
							@Override
							public Void call() {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										instance.getChildren().add(n);
									}
								});

								return null;
							}
						};

						BasicOpsTest.executor.execute(task);

						System.out.println("You addded" + note.toString());
					}

				}

			}

		};

		EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				double offsetX = t.getSceneX() - orgSceneX;
				double offsetY = t.getSceneY() - orgSceneY;

				// double newTranslateX = orgTranslateX + offsetX;
				// double newTranslateY = orgTranslateY + offsetY;

				if (t.getSource() instanceof MIDINoteBar) {

					// MIDINoteBar p = ((MIDINoteBar) (t.getSource()));
					//
					// currentNote = p;
					//
					// // p.setX(newTranslateX);
					// // p.setY(newTranslateY);
					// System.out.println(p.noteInfo());

				} else {

					Node p = ((Node) (t.getSource()));
				}

			}
		};

	}
}
