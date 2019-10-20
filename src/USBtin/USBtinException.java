package USBtin;

/**
 * Exception regarding USBtin.USBtin
 */
public class USBtinException extends Exception {

    /**
     * Standard constructor
     */
    public USBtinException() {
        super();
    }

    /**
     * Construct exception
     *
     * @param message Message string
     */
    public USBtinException(String message) {
        super(message);
    }

    /**
     * Construct exception
     *
     * @param message Message string
     * @param cause Cause of exception
     */
    public USBtinException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct exception
     *
     * @param cause Cause of exception
     */
    public USBtinException(Throwable cause) {
        super(cause);
    }
}
