package USBtin;

/**
 * Listener for CAN messages.
 */
public interface CANMessageListener {

    /**
     * This method is called on incoming CAN messages
     *
     * @param canmsg Received CAN message
     */
    public void receiveCANMessage(CANMessage canmsg);
}
