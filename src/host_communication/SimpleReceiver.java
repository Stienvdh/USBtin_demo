package host_communication;

import USBtin.USBtinException;
import USBtin.USBtin;

public class SimpleReceiver extends CANReceiver {

    public SimpleReceiver(String port, int channel) {
        super();
        try {
            this.connect(port);
            this.openCANChannel(channel, USBtin.OpenMode.ACTIVE);
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }

}
