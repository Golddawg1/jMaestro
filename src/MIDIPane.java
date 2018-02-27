import java.util.ArrayList;
import java.util.List;

import javafx.collections.*;
import javafx.scene.layout.Pane;
import jm.JMC;

interface NoteChangeListener {
	void noteChanged();
}

public class MIDIPane extends Pane implements JMC {

	MIDIPane instance = this;
	
	ArrayList<MIDINoteBar> notes = new ArrayList<MIDINoteBar>();

	public MIDIPane() {
		super();

		ObservableList<MIDINoteBar> alert = FXCollections.observableList(new ArrayList<MIDINoteBar>());

		alert.addListener(new ListChangeListener<MIDINoteBar>() {
			@Override
			public void onChanged(ListChangeListener.Change change) {
				drawNotes(instance);
			}

			private void drawNotes(MIDIPane instance) {
				
				
			}
		});
	}

	public void addNotes(ArrayList<MIDINoteBar> n) {
		notes.addAll(n);
	}

	public void addNote(MIDINoteBar n) {
		notes.add(n);
	}

	public void deleteNote(MIDINoteBar n) {
		notes.remove(n);
	}
	
	public void editNote(MIDINoteBar n, MIDINoteBar newNote) {
		n = newNote;
		
	}

}
