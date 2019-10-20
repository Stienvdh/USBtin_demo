package USBtin;

/**
 * Represents the CAN filter registers.
 */
public class FilterValue extends FilterMask {

    /**
     * Create filter for extended CAN messages.
     *
     * @param extid Filter for extended identifier
     */
    public FilterValue(int extid) {
        super(extid);
        registers[1] |= 0x08;
    }

    /**
     * Create filter for standard CAN message.
     * @param sid Filter for standard identifier
     * @param d0 Filter for first data byte
     * @param d1 Filter for second data byte
     */
    public FilterValue(int sid, byte d0, byte d1) {
        super(sid, d0, d1);
    }

}
