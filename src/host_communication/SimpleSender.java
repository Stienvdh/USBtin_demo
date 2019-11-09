package host_communication;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;

public class SimpleSender extends CANSender {

    private CANMessage message;

    public SimpleSender(CANMessage mess, String port, int channel) {
        this.message = mess;

        try {
            this.connect(port);
            this.openCANChannel(channel, USBtin.OpenMode.ACTIVE);
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CANMessage getMessageToSend() {
        return this.message;
    }

    @Override
    public void sendMessage(CANMessage message) {
        try {
            this.send(message);
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }

}
