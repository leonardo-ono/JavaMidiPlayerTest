package midi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Midi {
    
    private final MidiHeader midiHeader;
    private final List<MidiTrack> tracks = new ArrayList<>();

    public Midi(String resource) throws FileNotFoundException, IOException {
        try (InputStream dis = getClass().getResourceAsStream(resource))
        {
            ByteBuffer bb = ByteBuffer.wrap(dis.readAllBytes());
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.position(0);

            // extract header
            midiHeader = new MidiHeader(bb);
            
            // extract all midi tracks
            for (int i = 0; i < midiHeader.tracksCount; i++) {
                MidiTrack midiTrack = new MidiTrack(bb);
                tracks.add(midiTrack);
            }
        }        
    }

    public MidiHeader getMidiHeader() {
        return midiHeader;
    }

    public List<MidiTrack> getTracks() {
        return tracks;
    }

}
