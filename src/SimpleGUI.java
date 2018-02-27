import javax.swing.*;

import jm.JMC;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Write;

import javax.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Graphics.*;
import java.awt.event.*;

import java.awt.Insets;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.*;
import javax.swing.*;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jm.music.data.*;
import jm.music.*;
import jm.util.*;
import jm.util.View;
import jm.gui.show.*;

public class SimpleGUI extends JFrame implements JMC {

	Score score;

	int[] pitchSop = { C3, C3, E4, D4, G4, A4, C4, D4, E4, D4, F4, E4, A4, G4, E4 };
	double[] rhythmSop = { Q, Q, DC, Q, C, C, C, C, C, C, C, C, M, C, Q };

	public SimpleGUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void go() throws Exception {
		Drawpanel = new Mypanel();
		JFrame frame = new JFrame("Chasing Line");

		frame.getContentPane().add(BorderLayout.CENTER, Drawpanel);
		frame.setSize(300, 300);
		frame.setVisible(true);

		// nt[] pitchSop = { C3, C3, E4, D4, G4, A4, C4, D4, E4, D4, F4, E4, A4, G4, E4
		// };
		// double[] rhythmSop = { Q, Q, DC, Q, C, C, C, C, C, C, C, C, M, C, Q };

		// create the jMusic phrase objects
		Phrase soprano = new Phrase();

		// add the notes to each phrase
		soprano.addNoteList(pitchSop, rhythmSop);

		// create jMusic parts

		Part s = new Part("Tuba", HORN, 1);

		// add the phrases to the parts
		s.addPhrase(soprano);

		// create a score
		score = new Score("Chorale");

		// add the parts to the score
		score.addPart(s);

		// display the result for the world to see
		// save score as a MIDI file
		Write.midi(score, "Chorale.mid");

		File myMidiFile = new File("Chorale.mid");
		Sequence sequence = MidiSystem.getSequence(myMidiFile);
		final Sequencer sequencer = MidiSystem.getSequencer();
		sequencer.open();
		sequencer.setSequence(sequence);

		// final JProgressBar progress = new JProgressBar(0, (int)
		// sequencer.getMicrosecondLength());

		double maxLength;
		maxLength = (double) (sequencer.getMicrosecondLength() / 1000000);

		double interval = frame.getWidth() / maxLength;

		ActionListener updateListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// progress.setValue((int) sequencer.getMicrosecondPosition());
				xpos += 1;

				Drawpanel.paintImage(xpos, ypos);
			}
		};

		// double x = (sequencer.getMicrosecondLength()/sequencer.getTempoInMPQ())/1000;
		Timer timer = new Timer(60, updateListener);

		sequencer.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage event) {
				if (event.getType() == 47) {
					sequencer.stop();
					timer.stop();

				}
			}
		});

		sequencer.start();
		timer.start();

	}

	class Mypanel extends JPanel {
		
		@Override
		public void paint(Graphics g) {
			
		}
		public void paintImage(int xpost, int ypost) {

			Graphics d = getGraphics();

			Graphics2D g2 = (Graphics2D) d;
			d.clearRect(0, 0, this.getWidth(), this.getHeight());
			g2.setColor(Color.black);
			
			paintNotes(pitchSop, rhythmSop);
			
			g2.draw(new Line2D.Double(xpost, 0, xpost, this.getHeight()));
			repaint();
			this.validate();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		public void paintNotes(int[] note, double[] rythym) {

			Graphics d = getGraphics();
			Graphics2D g2 = (Graphics2D) d;
			g2.setColor(Color.black);
			double cursor = 0;
			for (int i = 0; i < note.length; i++) {

				g2.fill(new Rectangle2D.Double(cursor, note[i], rythym[i]*10, 1));
				cursor += rythym[i]*10;

			}

		}

	} // end the inner class

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.err.println("Look and feel not set");
		}

		SimpleGUI win = new SimpleGUI();
		try {
			win.go();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Mypanel Drawpanel;
	private int xpos = 0;
	private int ypos = 0;
} // close SimpleGUI class