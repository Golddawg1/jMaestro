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

	static ObservableList observableList;
	static ListView mListView;

	static void table(Synthesizer s) {

		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						midiTable = new LinkedHashMap<Integer, String>();

						observableList = FXCollections.observableArrayList();
						mListView = new ListView(observableList);
						s.getAvailableInstruments();

						for (int i = 0; i < s.getAvailableInstruments().length; i++) {

							midiTable.put(i, s.getAvailableInstruments()[i].getName().toUpperCase());

						}

						for (String inst : midiTable.values()) {
							mListView.getItems().add(inst);
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

	public static Object getKeyFromValue(Object value) {
		for (Object o : midiTable.keySet()) {
			if (midiTable.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

}
