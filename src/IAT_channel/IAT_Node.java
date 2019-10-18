package IAT_channel;

import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.USBtin;
import de.fischl.usbtin.USBtinException;
import error_correction.ErrorCorrectionCode;

public class IAT_Node extends USBtin {

    private long PERIOD;
    private long DELTA;
    private int WINDOW_LENGTH;
    private CANAuthMessage AUTH_MESSAGE;
    private byte[] AUTH = new byte[]{0,1,0,1};

    private int placeInWindow = 0;
    private int indexInAuthMessage = 0;
    private boolean running=true;
    private ErrorCorrectionCode corrector;

    public IAT_Node(long period, long delta, int windowLength) {
        PERIOD = period;
        DELTA = delta;
        WINDOW_LENGTH = windowLength;
    }

    public void start(CANMessage message) {
        this.AUTH_MESSAGE = new CANAuthMessage(AUTH);

        // error correction
        if (this.corrector != null) {
            this.AUTH_MESSAGE.setCorrectionCode(this.corrector);
        }

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
        byte[] auth_bytes = this.AUTH_MESSAGE.toByteArray();

        // wrap-arounds
        if (placeInWindow >= WINDOW_LENGTH) {
            indexInAuthMessage += 1;
            placeInWindow = 0;
        }

        if (indexInAuthMessage > auth_bytes.length+1) {
            indexInAuthMessage = 0;
        }

        placeInWindow += 1;

        // silence bits
        if (indexInAuthMessage == 0 || indexInAuthMessage == auth_bytes.length+1) {
            return PERIOD;
        }

        if (auth_bytes[indexInAuthMessage-1] == 0) {
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

    public void setCorrector(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }
}
