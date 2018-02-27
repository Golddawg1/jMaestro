import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jm.JMC;
import jm.music.data.*;

public class TrackNotes implements JMC {
	static Phrase notes;
	Part p;
	static List<Integer> startTimes;
	static List<Note> noteList;

	TrackNotes() {

		p = new Part("Tuba", TROMBONE, 1);
		startTimes = new ArrayList<Integer>();
		noteList = new ArrayList<Note>();

	}

	TrackNotes(Part instrument) {

		notes = instrument.getPhrase(0);
		p = instrument;
		startTimes = new ArrayList<Integer>();
		noteList = new ArrayList<Note>();

	}

	public TrackNotes createMusic() {

		for (int i = 0; i < notes.getNoteArray().length; i++) {
			startTimes.add((int) notes.getNoteStartTime(i));
		}
		noteList = Arrays.asList(notes.getNoteArray());
		return this;
	}

	public void setNoteFrequency(double f, int index) {

		noteList.get(index).setFrequency(f);

	}

	public void setNoteLength(double d, int index) {

		noteList.get(index).setRhythmValue(d);

	}

	public void setNoteStartPosition(int index) {
		notes.getNote(index);
	}

	public void deleteNote(Note note, double time) {

		int deleteMe = noteAt(note, time);
		noteList.remove(deleteMe);
		startTimes.remove(deleteMe);

	}

	public double getStartTime(int index) {
		return startTimes.get(index);

	}

	public Note getNote(int index) {
		return noteList.get(index);

	}

	public Part getPart() {

		for (int i = 0; i < noteList.size(); i++) {

			System.out.println("adding" + noteList.get(i) + "at " + startTimes.get(i));
			p.addNote(noteList.get(i), startTimes.get(i));

		}
		return p;
	}

	public void addNote(Note n, int s) {
		noteList.add(n);
		startTimes.add(s);
	}

	public static int noteAt(Note note, double st) {

		int n;
		for (int i = 0; i < noteList.size(); i++) {
			if (note == noteList.get(i) && st == startTimes.get(i)) {
				n = i;
				return n;
			}
		}
		return -1;
	}

}
