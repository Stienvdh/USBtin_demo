package noise;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;

public class NoiseThread extends Thread {

    private long PERIOD;
    private int CHANNEL;
    private String port;
    private CANMessage message;
    private Noise_node noise;

    public NoiseThread(long period, int channel, String port, CANMessage mess) {
        this.PERIOD = period;
        this.CHANNEL = channel;
        this.port = port;
        this.message = mess;
    }

    public void run() {
        Noise_node node = new Noise_node(this.PERIOD);
        this.noise = node;

        try {
            node.connect(this.port);
            node.openCANChannel(this.CHANNEL, USBtin.OpenMode.ACTIVE);
        } catch (USBtinException e) {
            e.printStackTrace();
        }

        node.start(this.message);
    }

    public void end() {
        this.noise.stop();
        try {
            this.noise.closeCANChannel();
            this.noise.disconnect();
        } catch (USBtinException e) {
            e.printStackTrace();
        }
    }
}
