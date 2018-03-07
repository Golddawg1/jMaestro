import javafx.scene.shape.Rectangle;
import jm.*;
import jm.music.data.*;

public class MIDINoteBar extends Rectangle implements JMC {

	Note n;
	double startTime;
	// double x, y, w, h;

	MIDINoteBar(double x, double y, double width, double height, Note note, double time) {
		super();

		n = note;
		startTime = time;
		this.setX(x * 20);
		this.setY(y * -1 + 127);
		this.setWidth(width * 20 - .5);
		this.setHeight(3);
		this.setArcHeight(25);
		this.setArcWidth(25);

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

	public void setStartTime(double newTime) {
		this.startTime = newTime;
		this.setX(newTime * 20);

	}

	public void clear() {

		n.setDuration(0);
		n.setPitch(REST);
		startTime = 0;
		this.setWidth(0);
		this.setHeight(0);

	}

}