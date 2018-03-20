import javafx.scene.shape.Rectangle;
import jm.*;
import jm.music.data.*;

public class MIDINoteBar extends Rectangle implements JMC {

	Note n;
	double startTime;
	MIDIPane pane;

	MIDINoteBar(double x, double y, double width, double height, Note note, double time) {
		super();

		n = note;
		startTime = time;
		this.setX(x * 20 + .1);
		this.setY(y * -1 + 127);
		this.setWidth(width * 20 - 1);
		this.setHeight(2);
		// this.setArcHeight(25);
		// this.setArcWidth(25);

		//
		// if (n.getRhythmValue() == QN) {
		// this.setWidth(width / 4);
		// }

	}

	public double getStartTime() {
		return startTime;
	}

	public Note getNote()

	{
		return n;
	}

	public String noteInfo() {

		String n = new String("The note is: " + this.getNote() + " at " + this.getStartTime());
		return n;
	}

	public void setMNB(MIDINoteBar newNote) {
		this.setX(newNote.getStartTime());
		this.setY(newNote.getNote().getPitch() * -1 + 127);
		this.setWidth(newNote.getNote().getRhythmValue() * 20 - .5);
		this.startTime = newNote.getStartTime();
		this.n = newNote.getNote();

		// timeList.get(q), nt.getPitch(), nt.getRhythmValue(), 3, tempNote,
		// timeList.get(q));
	}

	public void setNote(Note newNote) {
		n = newNote;
		this.setY(newNote.getPitch() * -1 + 127);
		this.setWidth(newNote.getRhythmValue() * 20 - .5);

	}
	
	public void setPitch(int newNote) {
	
		int temp = newNote * -1 + 127;
		
		n.setPitch(temp);
		this.setY(newNote);

	}
	
	public void setStartfromX(double newX) {
		
		this.startTime = newX/20;
		this.setX(newX);
		n.getMyPhrase().setStartTime(startTime);

	}

	public void setStartTime(double newTime) {
		this.startTime = newTime;
		n.getMyPhrase().setStartTime(newTime);
		this.setX(newTime * 20);

	}

	public void setPane(MIDIPane p) {
		pane = p;

	}

	public MIDIPane getPane() {
		return pane;
	}

	public void clear() {

		Phrase temp = n.getMyPhrase();
		temp.getMyPart().removePhrase(temp);
		n.setDuration(0);
		n.setPitch(REST);
		startTime = 0;
		this.setWidth(0);
		this.setHeight(0);

	}

}