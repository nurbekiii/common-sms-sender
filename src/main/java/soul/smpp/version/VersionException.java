package soul.smpp.version;

/**
 * Exception thrown when there is a problem with SMPP versions.
 *
 * @author Oran Kelly
 * @since 1.0
 */
public class VersionException extends soul.smpp.SMPPRuntimeException {
    static final long serialVersionUID = -6347880117047656707L;

    public VersionException() {
    }

    public VersionException(String msg) {
        super(msg);
    }

    public VersionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

