package transmission_channel.IAT_channel;

import java.util.LinkedList;
import java.util.List;

public class OneBitConverter extends IATBitConverter {
    public OneBitConverter(long period, long delta, int bits) {
        super(period, delta, bits);
    }

    @Override
    List<Byte> convertFromIntervals(int intervals) {
        List<Byte> result = new LinkedList<>();
        if (intervals == -1) { result.add( (byte) 0 ); }
        if (intervals == 1) { result.add( (byte) 1 ); }
        return result;
    }

    @Override
    int convertToIntervals(List<Byte> bytes) {
        if (bytes.get(0) == ( (byte) 0) ) { return -1; }
        return 1;
    }
}
