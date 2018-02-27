import jm.JMC;
import jm.music.data.*;
import jm.music.tools.*;
import jm.util.*;

/**
 * A class which reads in MIDI files and extracts aspects of them to create a
 * new score
 * 
 * @author Andrew Brown
 */

public final class ReadMIDI implements JMC {
	/**
	 * Main method where all good Java programs start
	 */
	public static void main(String[] args) {

	}

	static Score importMIDItoScore(String s) {

		Score theScore = new Score("Temporary score");
		Score newScore = new Score(s);

		// read the MIDI files made earlier as input
		Read.midi(theScore, s);

		// get the part from it
		Part[] partArray = theScore.getPartArray();

		// add it to the new score
		for (int i = 0; i < partArray.length; i++) {
			newScore.addPart(partArray[i]);
		}

		theScore.empty();

		// now we can write the new score to a MIDI file

		return newScore;

	}

}
