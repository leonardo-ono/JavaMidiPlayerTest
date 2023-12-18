package midi;
public class MidiEvent implements Comparable<MidiEvent> {
    
    public static final int MIDI_EVENT_NOTE_OFF = 0x80;
    public static final int MIDI_EVENT_NOTE_ON = 0x90;
    public static final int MIDI_EVENT_CONTROL_CHANGE = 0xB0;
    public static final int MIDI_EVENT_CHANGE_PROGRAM = 0xC0;
    public static final int MIDI_EVENT_CHANNEL_PRESSURE = 0xD0;
    public static final int MIDI_METAEVENT = 0xFF;

    public int time;
    public final int delta;
    public final int status;
    public final int channel;
    public final int data1;
    public final int data2;
    public int spuChannel;

    public int metaEventType;
    public int metaEventData;

    public MidiEvent(int time, int delta, int status, int channel, int data1, int data2) {
        this.time = time;
        this.delta = delta;
        this.status = status;
        this.channel = channel;
        this.data1 = data1;
        this.data2 = data2;
    }
    
    public String getStatusType()
    {
        switch (status) {
            case MIDI_EVENT_NOTE_ON: return "NOTE_ON";
            case MIDI_EVENT_NOTE_OFF: return "NOTE_OFF";
            case MIDI_EVENT_CONTROL_CHANGE: return "CONTROL_CHANGE";
            case MIDI_EVENT_CHANGE_PROGRAM: return "CHANGE_PROGRAM";
            case MIDI_EVENT_CHANNEL_PRESSURE: return "CHANNEL_PRESSURE";
            default:
                return "NOT_RECOGNIZED";
        }
    }

    @Override
    public String toString() {
        return "MidiEvent = { time=" + time + ", delta=" + delta + ", status=" + status +", status_description=" + getStatusType() + ", channel=" + channel + ", spuChannel=" + spuChannel + ", data1=" + data1 + ", data2=" + data2 + " }";
    }

    @Override
    public int compareTo(MidiEvent o) {
        return time - o.time;
    }

}

