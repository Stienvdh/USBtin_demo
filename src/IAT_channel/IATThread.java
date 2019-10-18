package IAT_channel;

import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.USBtin;
import de.fischl.usbtin.USBtinException;
import error_correction.ErrorCorrectionCode;

public class IATThread extends Thread {

    private long PERIOD;
    private long DELTA;
    private int WATCH_ID;
    private int WINDOW_LENGTH;
    private int CHANNEL;
    private String SENDER_PORT;
    private String RECEIVER_PORT;
    private long NOISE_PERIOD;

    private CANMessage message;
    private USBtin receiver;
    private IAT_Node sender;
    private IAT_Monitor monitor;
    private ErrorCorrectionCode corrector;

    public IATThread(long period, long delta, int window_length, int watchid, String sender, String receiver, int channel,
                     CANMessage mess, long nperiod) {
        this.PERIOD = period;
        this.DELTA = delta;
        this.WINDOW_LENGTH = window_length;
        this.WATCH_ID = watchid;
        this.NOISE_PERIOD = nperiod;

        this.SENDER_PORT = sender;
        this.RECEIVER_PORT = receiver;
        this.CHANNEL = channel;
        this.message = mess;
    }

    public void addAuthCorrectionCode(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void run() {
        try {
            // create the instances
            IAT_Node sender = new IAT_Node(PERIOD, DELTA, WINDOW_LENGTH);
            USBtin listener = new USBtin();
            IAT_Monitor monitor = new IAT_Monitor(PERIOD, DELTA, WINDOW_LENGTH, WATCH_ID, CHANNEL, NOISE_PERIOD);

            this.sender = sender;
            this.receiver = listener;
            this.monitor = monitor;

            // add authentication error correction
            if (this.corrector != null) {
                this.sender.setCorrector(this.corrector);
                this.monitor.setCorrector(this.corrector);
            }

            // connect to USBtin and open CAN channel in Active-Mode
            sender.connect(SENDER_PORT);
            listener.connect(RECEIVER_PORT);
            listener.addMessageListener(monitor);

            sender.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);
            listener.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);

            sender.start(message);
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }

    public void end() {
        this.sender.leave();
        this.monitor.leave();

        try {
            this.receiver.closeCANChannel();
            this.receiver.disconnect();
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }
}