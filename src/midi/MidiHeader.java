package midi;
import java.nio.ByteBuffer;

public class MidiHeader {
    
    public final int magic;
    public final int length;
    public final int format;
    public final int tracksCount;
    public final int resolution; // ticks per quarter note or SMPTE 

    public MidiHeader(ByteBuffer bb) {
        magic = bb.getInt();
        if (magic != 1297377380)
        {
            throw new RuntimeException("midi header " + magic + " magic number invalid!");
        }

        length = bb.getInt();
        format = bb.getShort();
        tracksCount = bb.getShort();
        resolution = bb.getShort();
    }

    @Override
    public String toString() {
        return "MidiHeader { length=" + length + ", format=" + format + ", tracksCount=" + tracksCount + ", resolution=" + resolution + " }";
    }

}
