package transmission_channel.IAT_channel;

import java.util.List;

public abstract class IATBitConverter {

    private long period;
    private long delta;
    private int bits;

    IATBitConverter(long period, long delta, int bits) {
        this.period = period;
        this.delta = delta;
        this.bits = bits;
    }

    public int getIntervals(long IAT) {
        return Math.round(((float)(IAT-this.period))/(float)this.delta);
    }

    public int getBitsEncoded() {
        return this.bits;
    }

    public List<Byte> convertFromIAT(long IAT) {
        return convertFromIntervals(getIntervals(IAT));
    }

    public long convertToIAT(List<Byte> bytes) {
        return this.period + this.delta*convertToIntervals(bytes);
    }

    abstract List<Byte> convertFromIntervals(int intervals);

    abstract int convertToIntervals(List<Byte> bytes);
}
