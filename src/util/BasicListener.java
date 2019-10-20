package util;

import USBtin.CANMessage;
import USBtin.CANMessageListener;

public class BasicListener implements CANMessageListener {

    @Override
    public void receiveCANMessage(CANMessage canmsg) {
        System.out.println("Watched message: " + canmsg.toString());
    }
}
