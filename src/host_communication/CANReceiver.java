package host_communication;

import USBtin.USBtin;
import USBtin.USBtinException;

public abstract class CANReceiver extends USBtin {

    public void closedCC() {
        try {
            this.closeCANChannel();
            this.disconnect();
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }

}
