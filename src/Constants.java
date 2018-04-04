import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.Synthesizer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Constants {
	/*
	 * This uses the system synthesizer to get it's available instruments and then
	 * gets each name and puts it with its corresponding int value
	 */
	public static Map<Integer, String> midiTable;

	public static Map<Integer, String> pitchTable;

	static ObservableList observableList;
	static ListView mListView;

	static ObservableList addObservableList;
	static ListView addListView;

	static void table(Synthesizer s) {

		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						midiTable = new LinkedHashMap<Integer, String>();

						observableList = FXCollections.observableArrayList();
						addObservableList = FXCollections.observableArrayList();

						mListView = new ListView(observableList);
						addListView = new ListView(addObservableList);
						s.getAvailableInstruments();

						for (int i = 0; i < s.getAvailableInstruments().length; i++) {

							midiTable.put(i, s.getAvailableInstruments()[i].getName().toUpperCase());

						}

						midiTable.put(666, "DRUMS");

						for (String inst : midiTable.values()) {
							mListView.getItems().add(inst);
							addListView.getItems().add(inst);
						}

					}
				});

				return null;
			}
		};

		BasicOpsTest.executor.execute(task);

	}

	private void handleItemClicks() {
		mListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String selectedItem = mListView.getSelectionModel().getSelectedItem().toString();
				System.out.println(selectedItem);
			}
		});
	}

	public static int getKeyFromValue(String value) {
		for (int o : midiTable.keySet()) {
			if (midiTable.get(o).equals(value)) {
				return o;
			}
		}
		return 0;
	}

	public static void pitchTable() {

		pitchTable = new LinkedHashMap<Integer, String>();
		String letter = "C";
		int interval = -1;
		int midiValue = 0;
		for (int o = 0; o < 11; o++) {
			for (int i = 0; i < 12; i++) {

				switch (i) {
				case (0):
					letter = "C";
					break;
				case (1):
					letter = "C#";
					break;
				case (2):
					letter = "D";
					break;
				case (3):
					letter = "D#";
					break;
				case (4):
					letter = "E";
					break;
				case (5):
					letter = "F";
					break;
				case (6):
					letter = "F#";
					break;
				case (7):
					letter = "G";
					break;
				case (8):
					letter = "G#";
					break;
				case (9):
					letter = "A";
					break;
				case (10):
					letter = "A#";
					break;
				case (12):
					letter = "B";
					break;

				}
				pitchTable.put(midiValue, letter + interval);
				midiValue++;
			}

			interval++;
		}

		for (int name : pitchTable.keySet()) {

			int key = name;
			String value = pitchTable.get(name).toString();
			System.out.println(key + " " + value);

		}

	}
}
