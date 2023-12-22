package synth;
public class ChannelNote {
    
    public int channelId;

    private final SampleSynth synth;

    private int midiNote; // 0~127
    private Instrument instrument;
    private double volumeFactor;

    public double sampleIndex;
    private double sampleIndexInc;
    
    private double envelopeVolume = 0; // 0.0~1.0
    private double attackRate;
    private double decayRate;
    private double sustainRate;
    private double releaseRate;

    public static enum STAGE {
        ATTACK, DECAY, SUSTAIN, RELEASE, NONE
    }

    private STAGE stage = STAGE.NONE;

    public ChannelNote(int channelId, SampleSynth synth) {
        this.channelId = channelId;
        this.synth = synth;
    }

    public int getMidiNote() {
        return midiNote;
    }

    public STAGE getStage() {
        return stage;
    }

    // use the last instrument
    public void noteOn(int midiNote, int velocity) {
        noteOn(instrument, midiNote, velocity);
    }

    public void noteOn(Instrument instrument, int midiNote, int velocity) {
        this.instrument = instrument;
        this.midiNote = midiNote;
        this.volumeFactor = velocity / 127.0;

        if (instrument == null) {
            return;
        }
        
        int midiBaseNote = instrument.getSample().getBaseMidiNote();
        int fineTuning = instrument.getSample().getFineTuning();

        double noteFrequency = instrument.getSample().getSampleRate() * Math.pow(2.0, (midiNote - midiBaseNote + (fineTuning / 100.0)) / 12.0);
        double pitchFactor = noteFrequency / synth.getSampleRate();
        
        sampleIndex = 0;
        sampleIndexInc = pitchFactor;
        
        envelopeVolume = 0.0;

        stage = STAGE.ATTACK;
        attackRate = (instrument.getAttack() * synth.getSampleRate());
        decayRate = (instrument.getDecay() * synth.getSampleRate());
        sustainRate = (instrument.getSustain() * synth.getSampleRate());
        releaseRate = (instrument.getRelease() * synth.getSampleRate());

        attackRate = (attackRate != 0) ? 1.0 / attackRate : 1.0;
        decayRate = (decayRate != 0) ? 1.0 / decayRate : 1.0;
        sustainRate = (sustainRate != 0) ? 1.0 / sustainRate : 1.0;
        releaseRate = (releaseRate != 0) ? 1.0 / releaseRate : 1.0;
    }

    public void noteOff() {
        stage = STAGE.RELEASE;
    }

    public byte getNextSample() {
        byte sample = 0;

        if (instrument != null) {
            sample = instrument.getSample().getNextSample(sampleIndex);
        }

        sampleIndex += sampleIndexInc;

        switch (stage) {
            case ATTACK -> {
                envelopeVolume += attackRate;
                if (envelopeVolume >= 1.0) {
                    envelopeVolume = 1.0;
                    stage = STAGE.DECAY;
                }
            }

            case DECAY -> {
                envelopeVolume -= decayRate;
                if (envelopeVolume <= instrument.getSustainLevel()) {
                    envelopeVolume = instrument.getSustainLevel();
                    stage = STAGE.SUSTAIN;
                }
            }
            
            case SUSTAIN -> {
                envelopeVolume -= sustainRate;
                if (envelopeVolume < 0.0) envelopeVolume = 0.0;
                if (envelopeVolume > 1.0) envelopeVolume = 1.0;
            }

            case RELEASE -> {
                envelopeVolume -= releaseRate;
                if (envelopeVolume <= 0) {
                    envelopeVolume = 0;
                    stage = STAGE.NONE;
                }
            }

            default -> { }
        }

        return (byte) ((volumeFactor * envelopeVolume * ((sample & 0xff) - 128)) + 128);
    }
}
