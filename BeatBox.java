package beatBox;

// Creating a virtual beat box
// Version 1.0
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.awt.event.*;
import java.io.*;

public class BeatBox {
	
	JPanel mainPanel;
	ArrayList<JCheckBox> checkBoxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	
	// The names of the instruments for the labels in the GUI
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", 
			"Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
			"High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
			"Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
			"Open Hi Conga"};
	// The actual drum channels for the above instruments that the midi will recognize
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args) {
		new BeatBox().buildGUI();
	}

	// Builds the virtual Beat Box GUI
	public void buildGUI() {
		theFrame = new JFrame("Fruity Loops");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		// For aesthetic purposes  
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		checkBoxList = new ArrayList<JCheckBox>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("START");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("STOP");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo +");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo -");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton save = new JButton("SAVE");
		save.addActionListener(new SaveListener());
		buttonBox.add(save);
		
		JButton restore = new JButton("RESTORE");
		restore.addActionListener(new RestoreListener());
		buttonBox.add(restore);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i < 16; i++) {
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		// Create our checkboxes and make sure they are not checked yet
		// Add them to our arraylist and then add them to the panel
		for(int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkBoxList.add(c);
			mainPanel.add(c);
		}
		
		setUpMidi();
		
		theFrame.setBounds(50, 50, 300, 300);
		theFrame.pack();
		theFrame.setVisible(true);
	}
	
	// Setting up our Midi
	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch(Exception e) {e.printStackTrace();}
		
	}
	
	// Here we convert checkbox states information into midi events
	// and add them to the track
	public void buildTrackAndStart() {
		int[] trackList = null;
		
		// Delete the previous track and start new
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for(int i = 0; i < 16; i++) {
			trackList = new int[16];
			
			int key = instruments[i];
			
			for(int j = 0; j < 16; j++) {
				JCheckBox jc = checkBoxList.get(j + 16*i);
				if(jc.isSelected()) {
					trackList[j] = key;
				} else 
					trackList[j] = 0;
			}
		
			makeTracks(trackList);
			track.add(makeEvent(176, 1, 127, 0, 16));
		}
		
		track.add(makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	// Start Button Listener
	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			buildTrackAndStart();
		}
	}
	
	// Stop Button Listener
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
		}
	}
	
	// Uptempo Button Listener
	public class MyUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
		}
	}
	
	// Downtempo Button Listener
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * .97));
		}
	}
	
	// Save Button Listener
	public class SaveListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = new boolean[256];
			
			for(int i = 0; i < 256; i++) {
				JCheckBox check = checkBoxList.get(i);
				if(check.isSelected()) {
					checkboxState[i] = true;
				}
			}
			try {
				FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
				ObjectOutputStream obj = new ObjectOutputStream(fileStream);
				obj.writeObject(checkboxState);
				obj.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// Restore button Listener
	public class RestoreListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean[] checkboxState = null;
			
			try {
				FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
				ObjectInputStream obj = new ObjectInputStream(fileIn);
				checkboxState = (boolean[]) obj.readObject();
				obj.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			for(int i = 0; i < 256; i++) {
				JCheckBox check = checkBoxList.get(i);
				if(checkboxState[i]) {
					check.setSelected(true);
				} else
					check.setSelected(false);
			}
			sequencer.stop();
			buildTrackAndStart();
		}
	}
	
	// Make the event for one instrument at a time
	public void makeTracks(int[] list) {
		for(int i = 0; i < 16; i++) {
			int key = list[i];
			if(key != 0) {
				track.add(makeEvent(144, 9, key, 100, i)); //NoteOn
				track.add(makeEvent(128, 9, key, 100, i+1)); //NoteOff
			}
		}
	}
	
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
		} catch(Exception e) {e.printStackTrace();}
		return event;
	}
	
	
}
