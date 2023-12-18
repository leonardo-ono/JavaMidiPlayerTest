package synth;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Sample {

    private final int sampleRate;
    private final int baseMidiNote;
    private final int loopStart;
    private final int loopLength;
    private final int loopEnd;
    private final boolean useLoop;

    public byte[] data;

    public Sample(String resource, int baseMidiNote, int loopStart, int loopEnd) throws Exception {
        int sampleRateTmp = 0;

        try (
            InputStream is = getClass().getResourceAsStream(resource);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
        ) {

            AudioFormat format = audioInputStream.getFormat();

            System.out.println("format: " + format + " encoding: " + format.getEncoding());
            if (format.getChannels() != 1 || format.getSampleSizeInBits() != 8 || format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
                throw new Exception("invalid audio format " + format + " !");
            }
            sampleRateTmp = (int) format.getSampleRate();

            int frameSize = format.getFrameSize();
            data = new byte[(int) (frameSize * audioInputStream.getFrameLength())];
            audioInputStream.read(data);
        } 

        this.sampleRate = sampleRateTmp;
        this.baseMidiNote = baseMidiNote;
        this.loopStart = loopStart;

        if (loopEnd > data.length - 1) {
            loopEnd = data.length - 1;
        }

        this.loopEnd = loopEnd;
        this.loopLength = loopEnd - loopStart + 1;
        this.useLoop = loopLength > 2;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBaseMidiNote() {
        return baseMidiNote;
    }

    public int getLoopStart() {
        return loopStart;
    }

    public int getLoopLength() {
        return loopLength;
    }

    public int getLoopEnd() {
        return loopEnd;
    }

    public boolean isUseLoop() {
        return useLoop;
    }

    public byte[] getData() {
        return data;
    }

    public byte getNextSample(double sampleIndex) {
        int nextSample = 0;

        int sampleIndexIntA = (int) sampleIndex;
        int sampleIndexIntB = sampleIndexIntA + 1;

        int nextSampleA = 0;
        int nextSampleB = 0;

        if (useLoop) {
            if (sampleIndexIntA <= loopEnd) { // before loop
                nextSampleA = data[sampleIndexIntA] & 0xff;
            }
            else { // after loop
                nextSampleA = data[loopStart + ((sampleIndexIntA - loopEnd) % loopLength)] & 0xff;
            }

            if (sampleIndexIntB <= loopEnd) { // before loop
                nextSampleB = data[sampleIndexIntB] & 0xff;
            }
            else { // after loop
                nextSampleB = data[loopStart + ((sampleIndexIntB - loopEnd) % loopLength)] & 0xff;
            }
        }
        else {
            if (sampleIndexIntA < data.length) {
                nextSampleA = data[sampleIndexIntA] & 0xff;
            }
            if (sampleIndexIntB < data.length) {
                nextSampleB = data[sampleIndexIntB] & 0xff;
            }
        }

        nextSampleA = nextSampleA - 128;
        nextSampleB = nextSampleB - 128;

        double lerp = sampleIndex - sampleIndexIntA;
        nextSample = (int) (nextSampleA + lerp * (nextSampleB - nextSampleA));

        return (byte) (nextSample + 128);
    }
    
}
