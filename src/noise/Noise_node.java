package noise;

import de.fischl.usbtin.CANMessage;
import de.fischl.usbtin.USBtin;
import de.fischl.usbtin.USBtinException;

public class Noise_node extends USBtin {

    private long PERIOD;
    private boolean running = true;

    public Noise_node(long period) {
        PERIOD = period;
        if (PERIOD == 0) {
            running = false;
        }
    }

    public void start(CANMessage mess) {
        while (running) {
            try {
                Thread.sleep((long) (Math.random()*PERIOD));
                this.send(mess);
            } catch (InterruptedException | USBtinException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.running = false;
    }
}
