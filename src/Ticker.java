import java.io.File;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public class Ticker {

	Sequencer sequencer = null;
	Sequence mySeq = null;
	Ticker(String s) {

		// Get default sequencer.
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (sequencer == null) {
			// Error -- sequencer device is not supported.
			// Inform user and return...
		} else {
			// Acquire resources and make operational.
			try {
				sequencer.open();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			File myMidiFile = new File(s + ".mid");
			// Construct a Sequence object, and
			// load it into my sequencer.
			 mySeq = MidiSystem.getSequence(myMidiFile);
			sequencer.setSequence(mySeq);
		} catch (Exception e) {
			// Handle error and/or return
		}

	}

	long setTickPosition(long x) {

		sequencer.setTickPosition(x);

		return x;

	}

	long getTickLength() {

		return sequencer.getTickLength();
	}

	void play() {
		sequencer.start();

	}

	void stop() {
		sequencer.stop();
	}

	public void setBPM(float bpm) {

		sequencer.setTempoInBPM(bpm);
	}

	void setTrackSolo(int track, boolean mute) {

		sequencer.setTrackSolo(track, mute);
	}

	boolean getTrackSolo(int track) {

		return sequencer.getTrackSolo(track);
	}

	void setTrackMute(int track, boolean mute) {

		sequencer.setTrackMute(track, mute);
	}

	boolean getTrackMute(int track) {

		return sequencer.getTrackMute(track);
	}
	
	void addTrack(MidiEvent m)
	{
		mySeq.createTrack().add(m);
	
	}
	
}
