
import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.util.*;

public class drum implements JMC {

	public static void main(String[] args) {
		Score pattern1 = new Score("JMDemo - Kit");
		Part drums = new Part("Drum Kit", 0, 9);
		Phrase phrBD = new Phrase(0.0);
		Phrase phrSD = new Phrase(0.0);
		Phrase phrHH = new Phrase(0.0);

		// Let us know things have started
		System.out.println("Creating drum patterns . . .");

		// make bass drum
		for (short i = 0; i < 4; i++) {
			Note note = new Note(36, C);
			phrBD.addNote(note);
			Note rest = new Note(REST, C); // rest
			phrBD.addNote(rest);
		}

		// make snare drum
		for (short i = 0; i < 4; i++) {
			Note rest = new Note(REST, C); // rest
			phrSD.addNote(rest);
			Note note = new Note(38, C);
			phrSD.addNote(note);
		}

		// make hats
		for (short i = 0; i < 15; i++) {
			Note note = new Note(42, Q);
			phrHH.addNote(note);
		}
		Note note = new Note(46, Q); // open hi hat
		phrHH.addNote(note);

		// loop the drum pattern for 16 bars
		Mod.repeat(phrBD, 7);
		Mod.repeat(phrSD, 7);
		Mod.repeat(phrHH, 7);

		// add phrases to the instrument (part)
		drums.addPhrase(phrBD);
		drums.addPhrase(phrSD);
		drums.addPhrase(phrHH);

		// add the drum part to a score.
		pattern1.addPart(drums);

		Play.midi(pattern1);

	}
}
