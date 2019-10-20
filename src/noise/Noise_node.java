package noise;

import USBtin.CANMessage;
import USBtin.USBtin;
import USBtin.USBtinException;

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
