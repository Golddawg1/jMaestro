
import java.awt.Insets;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import jm.JMC;
import jm.music.data.*;
import jm.music.*;
import jm.util.*;
import jm.util.View;
import jm.gui.show.*;

/**
 * An example which creates a Bach chorale
 * 
 * @author Andrew Sorensen
 */

public final class music2 extends Application implements JMC {

	Score score;

	public static void main(String[] args) {

		Application.launch(args);

	}

	static void deleteNote(Phrase p, int note) {
		Note n = new Note(REST, p.getNote(note).getRhythmValue());
		p.setNote(n, note);

	}

	public GridPane addGridPane() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		return grid;
	}

	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub

		// set up the pitches and durations of each line
		int[] pitchSop = { REST, G4, E4, D4, G4, A4, C4, D4, E4, D4, F4, E4, A4, G4, E4 };
		double[] rhythmSop = { C, C, DC, Q, C, C, C, C, C, C, C, C, M, C, Q };

		// create the jMusic phrase objects
		Phrase soprano = new Phrase();

		// add the notes to each phrase
		soprano.addNoteList(pitchSop, rhythmSop);

		// create jMusic parts

		deleteNote(soprano, 4);
		deleteNote(soprano, 3);
		deleteNote(soprano, 2);

		Part s = new Part("Tuba", TROMBONE, 1);

		// add the phrases to the parts
		s.addPhrase(soprano);

		// create a score
		score = new Score("Chorale");

		// add the parts to the score
		score.addPart(s);

		// display the result for the world to see
		// save score as a MIDI file
		Write.midi(score, "Chorale.mid");

		//Play.mid("Chorale.mid");
		View.internal(score);
		//View.au("Chorale.mid");
		// Ticker q = new Ticker("Chorale");

		// q.play();

		// ShowScore sco = new ShowScore(score);
//
//		Pane pane = new Pane();
//		stage.setTitle("Swing in JavaFX");
//		stage.setScene(new Scene(pane, 250, 150));
//		stage.show();
		
       File myMidiFile = new File("Chorale.mid");
        Sequence sequence = MidiSystem.getSequence(myMidiFile);
        final Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.setSequence(sequence) ;
        
        

        
		Runnable r = new Runnable() {
            public void run() {
                final JProgressBar progress = new JProgressBar(0,(int)sequencer.getMicrosecondLength()); 
                ActionListener updateListener = new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        progress.setValue((int)sequencer.getMicrosecondPosition());
                    }
                };
                Timer timer = new Timer(40,updateListener); 
                sequencer.start();
                timer.start();
                JOptionPane.showMessageDialog(null, progress);
                sequencer.close();
                timer.stop();
            }
        };
        SwingUtilities.invokeLater(r);
    }

	}

