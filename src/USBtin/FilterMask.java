package USBtin;

/**
 * Represents the CAN filter mask registers.
 */
public class FilterMask {

    /** Registers in MCP2515 style */
    protected byte[] registers = new byte[4];

    /**
     * Create filer mask for extended CAN messages.
     *
     * Bitmask:
     * 0 - accept (accept regardless of filter)
     * 1 - check (accept only if filter matches)
     *
     * Examples:
     * fm1 = new USBtin.FilterMask(0x1fffffff); // Check whole extended id
     * fm2 = new USBtin.FilterMask(0x1fffff00); // Check extended id except last 8 bits
     *
     * @param extid Filter mask for CAN identifier
     */
    public FilterMask(int extid) {

        registers[0] = (byte)((extid >> 21) & 0xff);
        registers[1] = (byte)(((extid >> 16) & 0x03) | ((extid >> 13) & 0xe0));
        registers[2] = (byte)((extid >> 8) & 0xff);
        registers[3] = (byte)(extid & 0xff);
    }

    /**
     * Create filter mask for standard CAN messages.
     *
     * Bitmask:
     * 0 - accept (accept regardless of filter)
     * 1 - check (accept only if filter matches)
     *
     * Examples:
     * fm1 = new USBtin.FilterMask(0x7ff, (byte)0x00, (byte)0x00); // check whole id, data bytes are irrelevant
     * fm2 = new USBtin.FilterMask(0x7f0, (byte)0x00, (byte)0x00); // check whole id except last 4 bits, data bytes are irrelevant
     * fm2 = new USBtin.FilterMask(0x7f0, (byte)0xff, (byte)0x00); // check whole id except last 4 bits, check first data byte, second is irrelevant
     *
     * @param sid Filter mask for CAN identifier
     * @param d0 Filter mask for first data byte
     * @param d1 Filter mask for second data byte
     */
    public FilterMask(int sid, byte d0, byte d1) {

        registers[0] = (byte) ((sid >> 3) & 0xff);
        registers[1] = (byte) ((sid & 0x7) << 5);
        registers[2] = d0;
        registers[3] = d1;
    }

    /**
     * Get register values in MCP2515 style
     *
     * @return Register values
     */
    public byte[] getRegisters() {
        return registers;
    }

}
