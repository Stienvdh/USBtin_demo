package transmission_channel.DLC_channel;

import USBtin.CANMessage;
import USBtin.CANMessageListener;
import attestation.AttestationProtocol;
import error_detection.ErrorCorrectionCode;
import util.CANAuthMessage;

import java.util.LinkedList;
import java.util.List;

public class DLC_Monitor implements CANMessageListener {

    private boolean detecting = false;
    private List<Byte> authMessage = new LinkedList<>();
    private ErrorCorrectionCode corrector;
    private AttestationProtocol protocol;
    private int WATCHID;

    public DLC_Monitor(int watchid) {
        this.WATCHID = watchid;
    }

    @Override
    public void receiveCANMessage(CANMessage message) {
        String result = detectBit(message);
    }

    private String detectBit(CANMessage message) {
        int DLC = message.getDLC();

        if (detecting) {
            if (DLC > 8) {
                switch (DLC) {
                    case (DLC_Node.DLC_0):
                        authMessage.add( (byte) 0);
                        return "0";
                    case (DLC_Node.DLC_1):
                        authMessage.add( (byte) 1);
                        return "1";
                    case (DLC_Node.DLC_00):
                        authMessage.add( (byte) 0);
                        authMessage.add( (byte) 0);
                        return "00";
                    case (DLC_Node.DLC_01):
                        authMessage.add( (byte) 0);
                        authMessage.add( (byte) 1);
                        return "01";
                    case (DLC_Node.DLC_10):
                        authMessage.add( (byte) 1);
                        authMessage.add( (byte) 0);
                        return "10";
                    case (DLC_Node.DLC_11):
                        authMessage.add( (byte) 1);
                        authMessage.add( (byte) 1);
                        return "11";
                }
            }
        }

        if (DLC == DLC_Node.SILENCE_BIT_DLC) {
            if (detecting) {
                // check error detection
                if (this.corrector == null) {
                    System.out.println("DETECTED MESSAGE: " + authMessage);
                }
                else if (this.corrector.checkCodeForAuthMessage(authMessage)) {
                    if (authMessage.size() - this.corrector.getNrCorrectingBits() < 0) {
                        System.out.println("Error in transmission detected!");
                    }
                    else {
                        List<Byte> mess = authMessage.subList(0, authMessage.size() - this.corrector.getNrCorrectingBits());
                        System.out.println("DETECTED MESSAGE: " + mess);
                    }
                }
                else {
                    System.out.println("Error in transmission detected!");
                }

                // check attestation
                int size = authMessage.size() - this.corrector.getNrCorrectingBits() > 0 ?
                        authMessage.size() - this.corrector.getNrCorrectingBits() :
                        0;
                CANAuthMessage canAuthMessage = this.corrector==null ?
                        new CANAuthMessage(authMessage) :
                        new CANAuthMessage(authMessage.subList(0, size));

                if (this.protocol.checkAttestationMessage(canAuthMessage)) {
                    System.out.println("Attestation OK");
                }
                else { System.out.println("Attestation NOK"); }
            }
            authMessage = new LinkedList<>();
            detecting = !detecting;
            return "Silence bit";
        }

        return "No bit detected";
    }

    public void setCorrector(ErrorCorrectionCode corrector) {
        this.corrector = corrector;
    }

    public void setAttestation(AttestationProtocol protocol) {
        this.protocol = protocol;
    }

}
