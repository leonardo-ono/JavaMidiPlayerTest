import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import midi.Midi;
import midi.MidiTrack;
import synth.Instrument;
import synth.Sample;
import synth.SampleSynth;

public class App {
    public static void main(String[] args) throws Exception {
        
        SampleSynth synth = new SampleSynth(44100, 16);
        
        // load instruments
        try (BufferedReader br = new BufferedReader(new InputStreamReader(App.class.getResourceAsStream("/res/instruments.txt")))) {
            String line;

            int instrument_number = 0;
            String instrument_name = "";
            double envelope_attack = 0;
            double envelope_decay = 0;
            double envelope_hold = 0.0;
            double envelope_sustain_level = 0.0;
            double envelope_release = 0.0;
            String sample_filename = "";
            int sample_length = 0;
            int sample_width = 0;
            int sample_rate = 0;
            int sample_is_mono = 0;
            int sample_root_key = 0;
            int sample_start_loop = 0;
            int sample_end_loop = 0;
            
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("instrument_number")) { //= 0
                    instrument_number = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("instrument_name")) { //= 000000
                    instrument_name = line.split("=")[1].trim();
                }
                if (line.startsWith("envelope_attack")) { //= 0.001
                    envelope_attack = Double.parseDouble(line.split("=")[1].trim());
                }
                if (line.startsWith("envelope_decay")) { //= 0.001
                    envelope_decay = Double.parseDouble(line.split("=")[1].trim());
                }
                if (line.startsWith("envelope_hold")) { //= 0.001
                    envelope_hold = Double.parseDouble(line.split("=")[1].trim());
                }
                if (line.startsWith("envelope_sustain_level")) { //= 0.000010000000000
                    envelope_sustain_level = Double.parseDouble(line.split("=")[1].trim());
                }
                if (line.startsWith("envelope_release")) { //= 0.9908006132652293
                    envelope_release = Double.parseDouble(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_filename")) { //= 000298.wav
                    sample_filename = line.split("=")[1].trim();
                }
                if (line.startsWith("sample_length")) { //= 4873
                    sample_length = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_width")) { //= 2
                    sample_width = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_rate")) { //= 22050
                    sample_rate = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_is_mono")) { //= 1
                    sample_is_mono = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_root_key")) { //= 103
                    sample_root_key = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_start_loop")) { //= 4686
                    sample_start_loop = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_end_loop")) { //= 4841
                    sample_end_loop = Integer.parseInt(line.split("=")[1].trim());
                } 
                if (line.startsWith("self.sf2parser=")) { //<sf2utils.sf2parse.Sf2File object at 0x000001CCDA240FE0>
                    int fineTuning = instrument_number == 68 ? 27 : 0;
                    Sample sample = new Sample("/res/samples/" + sample_filename, sample_root_key, sample_start_loop, sample_end_loop, fineTuning);
                    Instrument instrument = new Instrument(sample, envelope_attack, envelope_decay, 10.0, envelope_release, envelope_sustain_level);
                    synth.setBankInstrument(instrument_number, instrument);
                } 
            }
        }        

        // set all channel's default instrument to preset 0
        for (int i = 0; i < synth.getNumChannels(); i++) {
            synth.changeChannelInstrument(i, 0);
        }

        // load drums
        try (BufferedReader br = new BufferedReader(new InputStreamReader(App.class.getResourceAsStream("/res/drums.txt")))) {
            String line;

            int drum_note = 0;
            String sample_filename = "";
            int sample_start_loop = 0;
            int sample_end_loop = 0;
            
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("drum_note")) { //= 0
                    drum_note = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_filename")) { //= 000298.wav
                    sample_filename = line.split("=")[1].trim();
                }
                if (line.startsWith("sample_start_loop")) { //= 4686
                    sample_start_loop = Integer.parseInt(line.split("=")[1].trim());
                }
                if (line.startsWith("sample_end_loop")) { //= 4841
                    sample_end_loop = Integer.parseInt(line.split("=")[1].trim());
                } 
                if (line.startsWith("self.sf2parser=")) { //<sf2utils.sf2parse.Sf2File object at 0x000001CCDA240FE0>
                    int fineTuning = 0;
                    Sample sampleDrum = new Sample("/res/samples/drums/" + sample_filename, drum_note, 0, 0, fineTuning);
                    Instrument instrumentDrum = new Instrument(sampleDrum, 0.01, 0.01, 100.0, 0.01, 1.0);
                    synth.registerDrumInstrument(drum_note, instrumentDrum);
                } 
            }
        }        
        

        //Midi midiSequence = new Midi("/res/midi/star-stealing-girl-3-.mid"); // <-- ok
        Midi midiSequence = new Midi("/res/midi/TWINBEE.mid"); // <-- ok
        //Midi midiSequence = new Midi("/res/midi/ff7tifa.mid"); // <--ok
        //Midi midiSequence = new Midi("/res/midi/e1m1.mid"); // <-- ok

        //Midi midiSequence = new Midi("/res/midi/chrono_cross.mid");
        //Midi midiSequence = new Midi("/res/midi/ff7aerith.mid");
        //Midi midiSequence = new Midi("/res/midi/ff7barret.mid");
        //Midi midiSequence = new Midi("/res/midi/overworld.mid");

        
        List<midi.MidiEvent> allEvents = new ArrayList<>();
        for (MidiTrack track : midiSequence.getTracks()) {
            //for (midi.MidiEvent event : track.getEvents()) {
            //    System.out.println("time: " + event.time + " data: " + event.data1);
            //}
            allEvents.addAll(track.getEvents());
        }
        
        Collections.sort(allEvents);

        System.out.println();

        try {
            AudioFormat audioFormat = new AudioFormat(synth.getSampleRate(), 8, 1, false, false);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat);

            sourceDataLine.open(audioFormat);
            sourceDataLine.start();


            double bpm = 120.0;
            double resolution = midiSequence.getMidiHeader().resolution;
            double secondsPerTick = (1.0 /resolution) * (60.0 / bpm);
            int ticksPerSample = (int) (secondsPerTick * synth.getSampleRate());
            
            byte[] data = new byte[0xffff];
            
            int eventIndex = 0;
            for (int tick = 0; tick > -1; tick++) {
                midi.MidiEvent event = null;
                while ((event = allEvents.get(eventIndex)) != null) {
                    if (event != null && event.time == tick) {
                        //System.out.println("tick " + tick + " event " + event);
                        if (event.channel !=  555) {
                            switch (event.status) {
                                case midi.MidiEvent.MIDI_METAEVENT -> {
                                    // set tempo
                                    if (event.metaEventType == 0x51) {
                                        int microsecondsPerBeat = event.metaEventData;
                                        bpm = 60000000.0 / microsecondsPerBeat;                                        
                                        System.out.println("set tempo / bpm=" + bpm);

                                        resolution = midiSequence.getMidiHeader().resolution;
                                        //secondsPerTick = (1.0 / resolution) * (60.0 / bpm);
                                        //ticksPerSample = (int) (secondsPerTick * synth.getSampleRate());
                                        ticksPerSample = (int) ((microsecondsPerBeat * 0.000001 * synth.getSampleRate()) / resolution);
                                    }
                                }

                                case midi.MidiEvent.MIDI_EVENT_CHANGE_PROGRAM -> {
                                    synth.changeChannelInstrument(event.channel, event.data1);
                                    System.out.println("channel " + event.channel + " change program: " + event.data1);
                                }

                                case midi.MidiEvent.MIDI_EVENT_NOTE_ON -> {
                                    if (event.data2 > 0) {
                                        int data2 = event.data2;
                                        //if (event.channel == synth.getDrumChannelId()) data2 = 127;
                                        synth.noteOn(event.channel, event.data1, data2);
                                    } 
                                    else {
                                        synth.noteOff(event.channel, event.data1);
                                    }
                                }

                                case midi.MidiEvent.MIDI_EVENT_NOTE_OFF -> {
                                    synth.noteOff(event.channel, event.data1);
                                }

                                default -> {
                                    //System.out.println("ignoring message " + event.status + " " + event.data1 + " " + event.data2);
                                }
                            }
                        }
                        eventIndex++;
                        if (eventIndex > allEvents.size() - 1) {
                            eventIndex = 0;
                            tick = 0;
                        }
                    }
                    else {
                        break;
                    }
                }

                for (int i = 0; i < ticksPerSample; i++) {
                    data[i] = synth.getNextSample();
                }
                sourceDataLine.write(data, 0, ticksPerSample);
            }

            boolean playing = true;
            while (playing) {
                data[0] = synth.getNextSample();
                sourceDataLine.write(data, 0, 1);
            }
            
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
}
