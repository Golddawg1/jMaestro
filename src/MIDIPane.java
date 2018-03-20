import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jm.JMC;
import jm.music.data.Part;

interface NoteChangeListener {
	void noteChanged();
}

public class MIDIPane extends Pane implements JMC {

	MIDIPane instance = this;

	ArrayList<MIDINoteBar> notes = new ArrayList<MIDINoteBar>();

	String partName = "Error";
	
	int instrument = 0;
	
	Part myPart;
	
	Label e;
	public MIDIPane(Part s) {
		super();
		myPart = s;
		MouseGestures mg = new MouseGestures();
		mg.makeClickable(this);
		int temp = s.getChannel();

		if (temp == 9) {
			partName = "Drums";
		}

		else {
			instrument = s.getInstrument();
			partName = Constants.midiTable.getOrDefault(s.getInstrument(), "Error");
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

	public void editNote(MIDINoteBar n, MIDINoteBar newNote) {
		n = newNote;

	}

	public String getPartInstrument() {
		return partName;
	}
	
	public void setInstrument(int i) {
		
		Platform.runLater(
				  () -> {
						instrument = i;
						myPart.setInstrument(instrument);
						partName = Constants.midiTable.getOrDefault(i, "Error");
						e.setText(partName);
				  }
				);
	
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

				if (t.getButton() == MouseButton.PRIMARY) {
					orgSceneX = t.getSceneX();
					orgSceneY = t.getSceneY();

					if (t.getSource() instanceof MIDIPane) {
						
						Node p = ((Node) (t.getSource()));
						
						double x = p.getLayoutX();
						double y = p.getTranslateY();
						

						System.out.println("X="+(int)(t.getX()-x) + " Y=" + (int)(t.getY()-y) + "");
						
						System.out.println(((MIDIPane) p).getPartInstrument());

					} else {

						// Node p = ((Node) (t.getSource()));

						// orgTranslateX = p.getTranslateX();
						// orgTranslateY = p.getTranslateY();

					}
				}

				else if (t.getButton() == MouseButton.SECONDARY) {

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
