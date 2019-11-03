package transmission_channel.IAT_channel;

import java.util.LinkedList;
import java.util.List;

public class TwoBitConverter extends IATBitConverter {

    private final int INT_0 = 3;
    private final int INT_00 = 2;
    private final int INT_01 = 1;
    private final int INT_10 = -1;
    private final int INT_11 = -2;
    private final int INT_1 = -3;

    public TwoBitConverter(long period, long delta, int bits) {
        super(period, delta, bits);
    }

    @Override
    List<Byte> convertFromIntervals(int intervals) {
        List<Byte> result = new LinkedList<>();
        switch (intervals) {
            case (INT_0):
                result.add( (byte) 0 );
                break;
            case (INT_00):
                result.add( (byte) 0 );
                result.add( (byte) 0 );
                break;
            case (INT_01):
                result.add( (byte) 0 );
                result.add( (byte) 1 );
                break;
            case (INT_10):
                result.add( (byte) 1 );
                result.add( (byte) 0 );
                break;
            case (INT_11):
                result.add( (byte) 1 );
                result.add( (byte) 1 );
                break;
            case (INT_1):
                result.add( (byte) 1 );
                break;
        }
        return result;
    }

    @Override
    int convertToIntervals(List<Byte> bytes) {
        if (bytes.get(0).equals( (byte) 0 )) {
            if (bytes.size() == 1) { return INT_0; }
            if (bytes.get(1).equals( (byte) 0 )) { return INT_00; }
            return INT_01;
        }
        if (bytes.size() == 1) { return INT_1; }
        if (bytes.get(1).equals( (byte) 0 )) { return INT_10; }
        if (bytes.get(1).equals( (byte) 1 )) { return INT_11; }
        return 0;
    }

}
