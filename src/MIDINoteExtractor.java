import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jm.JMC;
import jm.audio.Instrument;
import jm.music.data.*;
import jm.util.Read;

public class MIDINoteExtractor implements JMC {

	MouseGestures midibar = new MouseGestures();
	ArrayList<MIDIPane> panes = new ArrayList<MIDIPane>();
	ArrayList<Part> allParts = new ArrayList<Part>();
	public int tempo = 90;

	ArrayList<MIDINoteBar> mnbs = new ArrayList<MIDINoteBar>();

	public MIDINoteExtractor() {

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
			tempo = (int) toto.getTempo();
		}
		ArrayList<MyThead> threads = new ArrayList<MyThead>();

		Part[] temp = toto.getPartArray();
		for (int i = 0; i < toto.getPartArray().length; i++) {

			MyThead thead = new MyThead(temp, i);
			threads.add(thead);
			thead.start();
		}

		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).join();
			panes.add(threads.get(i).getMP());
		}

		toto.setTitle(file.getName());
		toto.setTempo(tempo);
		return toto;
	}

	public ArrayList<MIDIPane> getPaneList() {
		return panes;
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
		pane.setMaxHeight(127);
		pane.setPrefHeight(127);
		pane.setBackground(
				new Background(new BackgroundFill(Color.LIGHTGOLDENRODYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
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

				MIDINoteBar n = new MIDINoteBar(timeList.get(q), nt.getPitch(), nt.getRhythmValue(), 3, tempNote,
						timeList.get(q));
				midibar.makeDraggable(n);
				n.setPane(pane);
				part.addNote(n.getNote(), n.getStartTime());
				notes.add(n);

			}
		}

		allParts.add(part);
		pane.addNotes(notes);
		pane.getChildren().addAll(notes);

		s = part;

		return pane;
	}

	public Part[] getPart() {

		for (int i = 0; i < allParts.size(); i++) {
			for (int j = 0; j < allParts.get(i).getPhraseArray().length; j++) {
				for (int q = 0; q < allParts.get(i).getPhraseArray()[j].getNoteArray().length; q++) {
					if (allParts.get(i).getPhrase(j).getNote(q).getPitch() == REST) {

						allParts.get(i).removePhrase(j);
					}
				}
			}
		}
		return allParts.toArray(new Part[allParts.size()]);
	}

	private class MouseGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;

		public void makeDraggable(Node node) {
			node.setOnMousePressed(circleOnMousePressedEventHandler);
			node.setOnMouseDragged(circleOnMouseDraggedEventHandler);
		}

		EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

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

				else if (t.getButton() == MouseButton.SECONDARY) {
					for (int i = 0; i < mnbs.size(); i++) {
						mnbs.get(i).setFill(Color.BLACK);
					}
					mnbs.clear();

				}
			}

		};

		EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent t) {

				double offsetX = t.getSceneX() - orgSceneX;
				double offsetY = t.getSceneY() - orgSceneY;

				double newTranslateX = orgTranslateX + offsetX;
				double newTranslateY = orgTranslateY + offsetY;

				if (t.getSource() instanceof MIDINoteBar) {

					MIDINoteBar p = mnbs.get(0);

					double x = p.getPane().getTranslateX();
					double y = p.getPane().getTranslateY();

					double newX = (int) (t.getX() - x);
					int newY = (int) (t.getY() - y);

					if (newY >= 0 && newY < 127 && newX >= 0 && newX < p.getPane().getWidth()) {
						p.setX(newX);
						p.setY(newY);

						p.setPitch(newY);

						p.setStartfromX(newX);
					}
					System.out.println(p.noteInfo());

				} else {

					Node p = ((Node) (t.getSource()));
				}

			}
		};

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