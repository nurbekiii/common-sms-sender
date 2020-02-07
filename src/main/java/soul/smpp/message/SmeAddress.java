package soul.smpp.message;

/**
 * The address of an SME.
 *
 * @deprecated Use {@link ie.omk.smpp.Address}.
 */
public class SmeAddress extends soul.smpp.Address {
    static final long serialVersionUID = -7812497043813226780L;

    public SmeAddress() {
    }

    public SmeAddress(int ton, int npi, String addr) {
        super(ton, npi, addr);
    }
}

