package midi;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MidiTrack {

    private int magic;
    private int length;
    private final List<MidiEvent> events = new ArrayList<>();
    
    private void log(String message)
    {
        //System.out.println(message);
    }

    private void logf(String message, Object ... args)
    {
        //System.out.printf(message, args);
    }

    private int extractVariableLength(ByteBuffer bb) {
        int nextByte = 0;
        int v = 0;
        do {
            nextByte = bb.get() & 0xff;
            v = (v << 7) + (nextByte & 0x7f);
        } while ((nextByte & 0x80) == 0x80);
        return v;
    }
    
    public MidiTrack() {
    }

    public MidiTrack(ByteBuffer bb) {
        log("==================== NEW TRACK ====================================");
        magic = bb.getInt();

        if (magic != 1297379947) {
            throw new RuntimeException("midi track magic number " + magic + " invalid!");
        }
        
        length = bb.getInt();
        logf("track length = %d \n", length);

        log("track start pos: " + bb.position());
        
        //ByteBuffer data = bb.slice(bb.position(), length);
        //data.order(ByteOrder.BIG_ENDIAN);
        //data.position(0);

        extractEvents(bb, length);
    }

    private void extractEvents(ByteBuffer data, int trackLength)
    {
        int currentTime = 0;
        int previousStatus = 0;
        boolean isEndOfTrack = false;

        while (!isEndOfTrack)
        {

            log("======> position: " + data.position());
            MidiEvent midiEvent = null;

            int delta = extractVariableLength(data);
            currentTime += delta;

            int status = data.get() & 0xff;
    
            logf("pos = %d, delta = %d, status = %d \n", data.position(), delta, status);
            boolean isEvent = (status & 0x80) == 0x80;

            if (!isEvent)
            {
                status = previousStatus;
                data.position(data.position() - 1);
            }
            
            switch (status) {
                // sys exclusive event
                case 0xf0:
                case 0xf7:
                {
                    int length = extractVariableLength(data);
                    data.position(data.position() + length);
                    break;
                }

                // meta event
                case 0xff:
                {
                    int type = data.get() & 0xff;
                    int length = data.get() & 0xff;
                    int dataPosition = data.position();

                    // end of track?
                    if (type == 0x2f && length == 0)
                    {
                        isEndOfTrack = true;
                        log("===> END OF TRACK <===");
                    }

                    // get meta event data
                    midiEvent = new MidiEvent(currentTime, delta, status, 0, 0, 0);
                    midiEvent.metaEventType = type;
                    midiEvent.metaEventData = 0;
                    for (int i = 0; i < length; i++) {
                        midiEvent.metaEventData = (midiEvent.metaEventData << 8) + (data.get() & 0xff);
                    }
                    events.add(midiEvent);

                    data.position(dataPosition + length);
                    break;
                }
            
                // midi event
                default:
                {
                    int event = status & 0xf0;
                    int channel = status & 0x0f;

                    switch (event) {

                        // program change
                        case 0xc0:
                        //Channel Pressure (After-touch)
                        case 0xd0:
                        {
                            int data1 = data.get() & 0xff;
                            midiEvent = new MidiEvent(currentTime,delta, event, channel, data1, 0);
                            events.add(midiEvent);
                            log("event 1 data " + midiEvent);
                            break;
                        }
                            
                        // note on
                        case 0x80:
                        // note off
                        case 0x90:
                        // others
                        default:
                            int data1 = data.get() & 0xff;
                            int data2 = data.get() & 0xff;
                            midiEvent = new MidiEvent(currentTime, delta, event, channel, data1, data2);
                            events.add(midiEvent);
                            log("event 2 data " + midiEvent);
                            break;
                    }


                }
                    
            }
            previousStatus = status;
        }
    }

    public int getMagic() {
        return magic;
    }

    public int getLength() {
        return length;
    }

    public List<MidiEvent> getEvents() {
        return events;
    }
    
}
