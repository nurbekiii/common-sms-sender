package soul.smpp;

/**
 * NotBoundException
 *
 * @author Oran Kelly
 * @version 1.0
 */
public class NotBoundException extends soul.smpp.SMPPRuntimeException {
    static final long serialVersionUID = -3977230990049048741L;

    public NotBoundException() {
    }

    /**
     * Construct a new NotBoundException with specified message.
     */
    public NotBoundException(String s) {
        super(s);
    }
}

