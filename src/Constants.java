import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.Synthesizer;

public class Constants {

	public static Map<Integer, String> midiTable;

	static void table(Synthesizer s) {
		midiTable = new HashMap<Integer, String>();

		s.getAvailableInstruments();

		for (int i = 0; i < s.getAvailableInstruments().length; i++) {

			midiTable.put(i, s.getAvailableInstruments()[i].getName());

		}
		

	}

}
