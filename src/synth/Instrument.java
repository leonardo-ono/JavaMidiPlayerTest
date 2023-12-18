package synth;
public class Instrument {
    
    private final Sample sample;
    private final double attack;
    private final double decay;
    private final double sustain;
    private final double release;
    private final double sustainLevel; // 0.0~1.0

    public Instrument(Sample sample, double attack, double decay, double sustain, double release, double sustainLevel) {
        this.sample = sample;
        this.attack = attack;
        this.decay = decay;
        this.sustain = sustain;
        this.release = release;
        this.sustainLevel = sustainLevel;
    }

    public Sample getSample() {
        return sample;
    }

    public double getAttack() {
        return attack;
    }

    public double getDecay() {
        return decay;
    }

    public double getSustain() {
        return sustain;
    }

    public double getRelease() {
        return release;
    }

    public double getSustainLevel() {
        return sustainLevel;
    }

}
