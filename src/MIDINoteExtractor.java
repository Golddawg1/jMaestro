import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jm.JMC;
import jm.audio.Instrument;
import jm.music.data.*;
import jm.util.Read;

public class MIDINoteExtractor implements JMC {

	static MouseGestures midibar;
	ArrayList<MIDIPane> panes = new ArrayList<MIDIPane>();
	ArrayList<Part> allParts = new ArrayList<Part>();
	LinkedBlockingQueue<MIDIPane> undoList;

	public int tempo = 90;

	static ArrayList<MIDINoteBar> mnbs = new ArrayList<MIDINoteBar>();

	public MIDINoteExtractor() {
		midibar = new MouseGestures();
	}

	/*
	 * Take in a score and run extract notes for every part and then return the
	 * score
	 */
	public Score extract(Path s) throws InterruptedException {

		allParts.clear();
		Score toto = null;
		File file = new File(s.toUri());
		if (file.isFile()) {
			toto = Read.midiOrJmWithNoMessaging(file);
			// Read.midi(toto, file.getAbsolutePath());
			if (toto != null) {
				tempo = (int) toto.getTempo();
			}
		}
		ArrayList<MyThead> threads = new ArrayList<MyThead>();

		if (toto != null) {
			Part[] temp = toto.getPartArray();
			for (int i = 0; i < toto.getPartArray().length; i++) {

				MyThead thead = new MyThead(temp, i);
				threads.add(thead);
				thead.start();

				threads.get(i).join();
				panes.add(threads.get(i).getMP());
			}

			// for (int i = 0; i < threads.size(); i++) {
			//
			// }

			toto.setTitle(file.getName());
		}
		return toto;
	}

	public ArrayList<MIDIPane> getPaneList() {
		return panes;
	}

	public void addPane(MIDIPane m) {
		panes.add(m);
	}

	public ArrayList<MIDINoteBar> getMNB() {
		return mnbs;
	}

	public void delete(MIDINoteBar mnb) {

		for (int i = 0; i < mnbs.size(); i++) {

			mnbs.get(i).clear();

		}

		mnb.clear();
	}

	public void setMNB(MIDINoteBar mnb) {
		mnbs.add(mnb);
	}

	/*
	 * This makes a MIDINoteBar for every note in every phrase in part s. Each note
	 * is made into it's own phrase that carries its own start time
	 */
	public MIDIPane extractNotes(Part s) {

		Part part = s;

		// part.setChannel(s.getChannel());
		// part.setInstrument(s.getInstrument());
		// part.setTempo(s.getTempo());
		// part.setRhythmValue(s.getShortestRhythmValue());

		Phrase[] p = s.getPhraseArray();
		MIDIPane pane;

		// for(int i =0; i < p.getNoteArray().length; i++)

		pane = new MIDIPane(part);
		part.empty();
		pane.setMaxHeight(127);
		pane.setPrefHeight(127);
		pane.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));
		ArrayList<MIDINoteBar> notes = new ArrayList<MIDINoteBar>();

		ArrayList<Note> noteList = new ArrayList<Note>();
		ArrayList<Double> timeList = new ArrayList<Double>();
		for (int i = 0; i < p.length; i++) {

			for (int j = 0; j < p[i].getNoteArray().length; j++) {

				if (p[i].getNote(j).getPitch() != REST) {
					noteList.add(p[i].getNote(j));
					timeList.add(p[i].getNoteStartTime(j));
				}
			}
		}

		for (int q = 0; q < noteList.size(); q++) {

			Note nt = noteList.get(q);
			if (nt.getPitch() == REST) {

				nt = null;
			}

			else {
				Note tempNote = new Note(nt.getPitch(), nt.getRhythmValue());

				double rhythm = nt.getRhythmValue();
				rhythm = Math.round(rhythm * 10000);
				rhythm = rhythm / 10000;

				MIDINoteBar n = new MIDINoteBar(timeList.get(q), nt.getPitch(), rhythm, 3, tempNote, timeList.get(q));
				midibar.makeDraggable(n);
				n.setPane(pane);
				part.addNote(n.getNote(), n.getStartTime());
				notes.add(n);

				System.out.println("IN CREATE NOTE");
			}
		}

		allParts.add(part);
		pane.addNotes(notes);
		pane.getChildren().addAll(notes);

		s = part;
		// part.setTitle(Constants.getKeyFromValue(part.getInstrument()) + "");
		return pane;
	}

	public Part[] getPart() {

		Map<String, Integer> channelMap = new LinkedHashMap<String, Integer>();

		for (int i = 0; i < allParts.size(); i++) {
			for (int j = 0; j < allParts.get(i).getPhraseArray().length; j++) {
				for (int q = 0; q < allParts.get(i).getPhraseArray()[j].getNoteArray().length; q++) {
					if (allParts.get(i).getPhrase(j).getNote(q).getPitch() == REST) {

						allParts.get(i).removePhrase(j);
					}
				}
			}
		}

		int next = 0;
		for (int i = 0; i < allParts.size(); i++) {

			if (allParts.get(i).getChannel() == 9) {
				channelMap.put("DRUMS", 9);
			} else {

				String inst = (String) Constants.midiTable.get(allParts.get(i).getInstrument());
				if (!channelMap.containsKey(inst)) {

					if (next == 9)
						next = 10;
					channelMap.put(inst, next);
					next++;
				}
			}
		}

		for (Entry<String, Integer> entry : channelMap.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}

		for (int q = 0; q < allParts.size(); q++) {
			Part temp = allParts.get(q);
			int inst = temp.getInstrument();

			System.out.println(inst + " the inst");

			String instName = (String) Constants.midiTable.get(allParts.get(q).getInstrument());
			System.out.println(allParts.size() + "this si the instrument");
			if (allParts.get(q).getChannel() != 9)
				allParts.get(q).setChannel(channelMap.get(instName));

		}

		return allParts.toArray(new Part[allParts.size()]);

	}

	public void addPart(Part p) {

		allParts.add(p);
	}

	class MouseGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;

		public void makeDraggable(Node node) {
			node.setOnMousePressed(circleOnMousePressedEventHandler);
			node.setOnMouseDragged(circleOnMouseDraggedEventHandler);
		}

		EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				Task task = new Task<Void>() {
					@Override
					public Void call() {

						Platform.runLater(new Runnable() {
							@Override
							public void run() {

								if (t.getButton() == MouseButton.PRIMARY && t.isAltDown()) {

									BasicOpsTest.noteEdit(mnbs.get(0));

								}
								if (t.getButton() == MouseButton.PRIMARY) {
									orgSceneX = t.getSceneX();
									orgSceneY = t.getSceneY();

									if (t.getSource() instanceof MIDINoteBar) {

										MIDINoteBar p = ((MIDINoteBar) (t.getSource()));
										p.setFill(Color.RED);
										mnbs.add(p);

										System.out.println(p.noteInfo());

									} else {

										Node p = ((Node) (t.getSource()));

										// orgTranslateX = p.getTranslateX();
										// orgTranslateY = p.getTranslateY();

									}
								}

								if (t.getButton() == MouseButton.SECONDARY) {
									unselectMNBS();

								}

							}

						});

						return null;
					}
				};

				BasicOpsTest.executor.submit(task);

			}

		};

		EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {
				if (t.isShiftDown()) {
					Task task = new Task<Void>() {
						@Override
						public Void call() {

							Platform.runLater(new Runnable() {
								@Override
								public void run() {

									double offsetX = t.getSceneX() - orgSceneX;
									double offsetY = t.getSceneY() - orgSceneY;

									if (t.getSource() instanceof MIDINoteBar) {

										if (mnbs.size() > 0) {
											MIDINoteBar p = mnbs.get(0);

											if (mnbs.get(0).pane == BasicOpsTest.myCurrentMidiPane) {

												double x = p.getPane().getTranslateX();
												double y = p.getPane().getTranslateY();

												double newX = (int) (t.getX() - x);
												int newY = (int) (t.getY() - y);
												newY = newY * -1 + 127;

												if (newY >= 0 && newY < 127) {
													p.setX(newX);
													p.setY(newY);

													p.setPitch(newY);

													p.setStartfromX(newX);
												}
												System.out.println(p.noteInfo());
											}

										} else {

											Node p = ((Node) (t.getSource()));
										}
									}
								}
							});

							return null;
						}
					};

					BasicOpsTest.executor.execute(task);

				}
			}
		};

	}

	public static void unselectMNBS() {

		for (int i = 0; i < mnbs.size(); i++) {
			mnbs.get(i).setFill(Color.BLACK);
		}
		mnbs.clear();

	}

	/*
	 * Handles the the thread for extract notes
	 */
	private class MyThead extends Thread {
		int k;
		Part[] p;
		MIDIPane mp;

		public MyThead(Part[] t, int i) {
			k = i;
			p = t;
		}

		@Override
		public void run() {
			mp = extractNotes(p[k]);

		}

		public MIDIPane getMP() {
			return mp;
		}
	}

}