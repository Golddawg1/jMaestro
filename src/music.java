
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.*;

import jm.JMC;
import jm.music.data.*;
import jm.util.*;

/**
 * An example which creates a Bach chorale
 * @author Andrew Sorensen
 */

public final class music implements JMC{
	public static void main(String[] args){
		// set up the pitches and durations of each line
		int[] pitchSop = {REST,G4,E4,D4,G4,A4,C4,D4,E4,D4,F4,E4,A4,G4,E4};
		double[] rhythmSop = {C,C,DC,Q,C,C,C,C,C,C,C,C,M,C,C};
		int[] pitchAlto = {E4,D4,C4,A3,B3,C4,D4,C4,D4,CS4};
		double[] rhythmAlto = {C,C,SB,C,C,C,C,SB,C,C};
		int[] pitchTenor = {G3,C4,B3,A3,G3,F3,E3,G3,G3,C4,B3,A3,B3,A3};
		double[] rhythmTenor = {M,C,Q,Q,C,C,C,C,M,C,C,M,C,C};
		int[] pitchBass = {C3,B2,A2,G2,F2,E2,F2,A2,G2,C3,B2,A2,G2,F2,F3,E3,A3};
		double[] rhythmBass = {C,C,C,Q,Q,C,C,C,C,C,C,C,C,C,C,C,C};
		
		//create the jMusic phrase objects
		Phrase soprano = new Phrase();
		Phrase alto = new Phrase();
		Phrase tenor = new Phrase();
		Phrase bass = new Phrase();
		
		// add the notes to each phrase
		soprano.addNoteList(pitchSop, rhythmSop);
		alto.addNoteList(pitchAlto, rhythmAlto);
		tenor.addNoteList(pitchTenor, rhythmTenor);
		bass.addNoteList(pitchBass, rhythmBass);
		
		// create jMusic parts
		Part s = new Part("Soprano", FLUTE, 1);
		Part a = new Part("Alto", GUITAR, 2);
		Part t = new Part("Tenor", HARP, 3);
		Part b = new Part("Bass", TUBA, 4);
		
		// add the phrases to the parts
		s.addPhrase(soprano);
		a.addPhrase(alto);
		t.addPhrase(tenor);
		b.addPhrase(bass);
		
		//create a score
		Score score = new Score("Chorale");
		
		//add the parts to the score
		score.addPart(s);
		score.addPart(a);
		score.addPart(t);
		score.addPart(b);
		
		//display the result for the world to see
		// save score as a MIDI file
		Write.midi(score, "Chorale.mid");
		//Ticker q = new Ticker("Chorale");
		
	

		//q.play();
		
		try {
			testing test = new testing();
			
			test.addTrack(score);
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	
		
		
	}
}
	