import IAT_channel.IATThread;
import attestation.AttestationProtocol;
import attestation.HardCodedAttestation;
import de.fischl.usbtin.*;
import error_correction.ErrorCorrectionCode;
import error_correction.SimpleCRC;
import error_correction.SimpleParity;
import noise.NoiseThread;

import java.io.IOException;

public class USBtinLibDemo {

    private static final long PERIOD = 15;
    private static final long NOISE_PERIOD = 1; // NOISE_PERIOD=0 --> no noise
    private static final long DELTA = 10;
    private static final int WINDOW_LENGTH = 4;
    private static final int CHANNEL = 50000;

    private static final String SENDER_PORT = "/dev/tty.usbmodemA02183211";
    private static final String RECEIVER_PORT = "/dev/tty.usbmodemA02102821";
    private static final String NOISE_PORT = "/dev/tty.usbmodemA021CFBA1";
    private static final int WATCHID = 0x100;
    private static ErrorCorrectionCode AUTH_CORRECTOR = new SimpleCRC(2, "101"); // Set error correction instance here
    private static AttestationProtocol AUTH_PROTOCOL =
            new HardCodedAttestation(new byte[]{1,1,0,0,1}); // Set attestation protocol here

    public static void main(String[] args) {
        // Run a IAT thread
        IATThread IAT = new IATThread(PERIOD, DELTA, WINDOW_LENGTH, WATCHID, SENDER_PORT, RECEIVER_PORT, CHANNEL,
            new CANMessage(WATCHID, new byte[]{0x11, 0x22, 0x33}), NOISE_PERIOD);
        IAT.start();
        IAT.addAuthCorrectionCode(AUTH_CORRECTOR);
        IAT.addAttestationProtocol(AUTH_PROTOCOL);

        // Run a noise thread
        NoiseThread noise = new NoiseThread(NOISE_PERIOD, CHANNEL, NOISE_PORT,
                new CANMessage(0x200, new byte[]{0x11, 0x22, 0x33}));
        noise.start();

        // End the program
        try {
            System.in.read();
            IAT.end();
            noise.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
