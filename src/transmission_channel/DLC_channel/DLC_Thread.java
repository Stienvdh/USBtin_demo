package transmission_channel.DLC_channel;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import transmission_channel.TransmissionThread;

public class DLC_Thread extends TransmissionThread {

    private long PERIOD;
    private int WATCHID;
    private int CHANNEL;
    private String sender_port;
    private String receiver_port;
    private DLC_Node sender;
    private DLC_Monitor monitor;
    private USBtin receiver;
    private CANMessage message;
    private boolean running = true;

    public DLC_Thread(long period, int watchid, String sender, String receiver, int channel, CANMessage mess) {
        this.PERIOD = period;
        this.WATCHID = watchid;
        this.sender_port = sender;
        this.receiver_port = receiver;
        this.CHANNEL = channel;
        this.message = mess;
    }

    public void run() {
        try {
            // create the instances
            DLC_Node sender = new DLC_Node(this.PERIOD);
            USBtin listener = new USBtin();
            DLC_Monitor monitor = new DLC_Monitor(this.WATCHID);

            this.sender = sender;
            this.receiver = listener;
            this.monitor = monitor;

            // add error detection
            if (this.getCorrector() != null) {
                this.sender.setCorrector(this.getCorrector());
                this.monitor.setCorrector(this.getCorrector());
            }

            // add attestation protocol
            if (this.getAttestationProtocol() != null) {
                this.sender.setAttestation(this.getAttestationProtocol());
                this.monitor.setAttestation(this.getAttestationProtocol());
            }

            // connect to USBtin.USBtin and open CAN channel in Active-Mode
            sender.connect(this.sender_port);
            listener.connect(this.receiver_port);
            listener.addMessageListener(monitor);

            sender.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);
            listener.openCANChannel(CHANNEL, USBtin.OpenMode.ACTIVE);

            sender.start(message);
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }

    public void end() {
        try {
            sender.leave();
            receiver.closeCANChannel();
            receiver.disconnect();
        } catch (USBtinException ex) {
            ex.printStackTrace();
        }
    }
}
