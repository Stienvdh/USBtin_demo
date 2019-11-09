package host_communication;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;

public abstract class CANSender extends USBtin {

    public abstract CANMessage getMessageToSend();

    public abstract void sendMessage(CANMessage message);

    public void closedCC() {
        try {
            this.closeCANChannel();
            this.disconnect();
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }
}
