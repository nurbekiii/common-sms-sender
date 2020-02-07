/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soul.smpp.util;

/**
 * @author daliev
 */

import java.io.UnsupportedEncodingException;

public class SolEncoding extends soul.smpp.util.AlphabetEncoding {

    private static final String ENCODING = "ISO-8859-5";
    private static final int DCS = 6;

    /**
     * Construct a new UCS2 encoding.
     * @throws java.io.UnsupportedEncodingException if the ISO-10646-UCS-2
     * charset is not supported by the JVM.
     */
    public SolEncoding() throws UnsupportedEncodingException {
        super(DCS);
        setCharset(ENCODING);
    }
}
