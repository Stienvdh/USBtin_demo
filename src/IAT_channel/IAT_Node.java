package IAT_channel;

import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.USBtin;
import de.fischl.usbtin.USBtinException;

public class IAT_Node extends USBtin {

    private long PERIOD;
    private long DELTA;
    private int WINDOW_LENGTH;
    private byte[] AUTH_MESSAGE = new byte[]{0,1,0,1};

    private int placeInWindow = 0;
    private int indexInAuthMessage = 0;
    private boolean running=true;

    public IAT_Node(long period, long delta, int windowLength) {
        PERIOD = period;
        DELTA = delta;
        WINDOW_LENGTH = windowLength;
    }

    public void start(CANMessage message) {
        while (running) {
            try {
                Thread.sleep(this.getTimeToSleep());
                this.send(message);
            }
            catch (InterruptedException | USBtinException ex) {
                System.err.println(ex);
            }
        }
    }

    public long getTimeToSleep() {
        // wrap-arounds
        if (placeInWindow >= WINDOW_LENGTH) {
            indexInAuthMessage += 1;
            placeInWindow = 0;
        }

        if (indexInAuthMessage > AUTH_MESSAGE.length+1) {
            indexInAuthMessage = 0;
        }

        placeInWindow += 1;

        // silence bits
        if (indexInAuthMessage == 0 || indexInAuthMessage == AUTH_MESSAGE.length+1) {
            return PERIOD;
        }

        if (AUTH_MESSAGE[indexInAuthMessage-1] == 0) {
            return PERIOD + DELTA;
        }

        return PERIOD - DELTA;
    }

    public void leave() {
        running = false;
        try {
            this.closeCANChannel();
            this.disconnect();
        }
        catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }
}
