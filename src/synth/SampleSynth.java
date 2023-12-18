package synth;

import java.util.HashSet;
import java.util.Set;

import synth.ChannelNote.STAGE;

/**
 * Very simple 'Sample-based Synthesizer'
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class SampleSynth {
    
    private final int sampleRate;
    private final int numChannels;
    private final Instrument[] bankInstruments = new Instrument[256];
    private final Instrument[] channelInstruments;
    private final ChannelNote[] notes;
    private final Set<Integer> activeNotes = new HashSet<>();
    private final Set<Integer> finishedNotes = new HashSet<>();
    private int drumChannelId = 9;

    public SampleSynth(int sampleRate, int numChannels) {
        this.sampleRate = sampleRate;
        this.numChannels = numChannels;
        channelInstruments = new Instrument[numChannels];
        notes = new ChannelNote[(numChannels << 7) + 127];
        for (int ch = 0; ch < numChannels; ch++) {
            for (int midiNote = 0; midiNote < 128; midiNote++) {
                int noteId = (ch << 7) + midiNote;
                notes[noteId] = new ChannelNote(this);
            }
        }
    }

    public void setBankInstrument(int instrumentId, Instrument instrument) {
        bankInstruments[instrumentId] = instrument;
    }

    public int getSampleRate() {
        return sampleRate;
    }
    
    public int getNumChannels() {
        return numChannels;
    }

    public void changeChannelInstrument(int channelId, int bankInstrumentId) {
        channelInstruments[channelId] = bankInstruments[bankInstrumentId];
    }

    public int getDrumChannelId() {
        return drumChannelId;
    }

    public void setDrumChannelId(int drumChannelId) {
        this.drumChannelId = drumChannelId;
    }

    public void registerDrumInstrument(int midiNote, Instrument drumInstrument) {
        int noteId = (drumChannelId << 7) + midiNote;
        notes[noteId].noteOn(drumInstrument, midiNote, 127);
        notes[noteId].noteOff();
    }

    public void noteOn(int channelId, int midiNote, int velocity) {
        int noteId = (channelId << 7) + midiNote;
        if (channelId == drumChannelId) {
            notes[noteId].noteOn(midiNote, velocity);
        }
        else {
            Instrument instrument = channelInstruments[channelId];
            notes[noteId].noteOn(instrument, midiNote, velocity);
        }
        activeNotes.add(noteId);
    }

    public void noteOff(int channelId, int midiNote) {
        int noteId = (channelId << 7) + midiNote;
        if (activeNotes.contains(noteId)) {
            notes[noteId].noteOff();
        }
    }
    
    public byte getNextSample() {
        int mixedSample = 0;
        for (int noteId : activeNotes) {
            ChannelNote note = notes[noteId];
            int noteSample = (note.getNextSample() & 0xff) - 128;
            mixedSample = Math.max(Math.min(mixedSample + noteSample / 2, 127), -128);
            if (note.getStage() == STAGE.NONE) {
                finishedNotes.add(noteId);
            }
        }
        if (!finishedNotes.isEmpty()) {
            activeNotes.removeAll(finishedNotes);
            finishedNotes.clear();
        }
        return (byte) (mixedSample + 128);
    }
        
}
