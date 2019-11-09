package transmission_channel.DLC_channel;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;
import host_communication.CANReceiver;
import host_communication.CANSender;
import transmission_channel.TransmissionThread;

public class DLC_Thread extends TransmissionThread {

    private long PERIOD;
    private int WATCHID;
    private DLC_Node DLC_sender;
    private DLC_Monitor DLC_monitor;
    private CANSender host_sender;
    private CANReceiver host_receiver;
    private boolean running = true;

    public DLC_Thread(long period, int watchid, CANReceiver receiver, CANSender sender) {
        this.PERIOD = period;
        this.WATCHID = watchid;
        this.host_sender = sender;
        this.host_receiver = receiver;
    }

    public void run() {
        // create the instances
        DLC_Node sender = new DLC_Node(this.PERIOD, this.host_sender);
        DLC_Monitor monitor = new DLC_Monitor(this.WATCHID);

        this.DLC_sender = sender;
        this.DLC_monitor = monitor;

        // add error detection
        if (this.getCorrector() != null) {
            this.DLC_sender.setCorrector(this.getCorrector());
            this.DLC_monitor.setCorrector(this.getCorrector());
        }

        // add attestation protocol
        if (this.getAttestationProtocol() != null) {
            this.DLC_sender.setAttestation(this.getAttestationProtocol());
            this.DLC_monitor.setAttestation(this.getAttestationProtocol());
        }

        // connect to USBtin.USBtin and open CAN channel in Active-Mode
        host_receiver.addMessageListener(monitor);

        DLC_sender.start();
    }

    public void end() {
        this.host_receiver.closedCC();
        this.host_sender.closedCC();
        DLC_sender.leave();
    }
}
