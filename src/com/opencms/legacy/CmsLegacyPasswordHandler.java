package com.opencms.legacy;

import org.opencms.main.CmsException;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.I_CmsPasswordHandler;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Utility class to hide the implementation details for password encryption.<p>
 */
public class CmsLegacyPasswordHandler extends CmsDefaultPasswordHandler {
    
    /**
     * @see org.opencms.security.I_CmsPasswordHandler#digest(java.lang.String, java.lang.String, java.lang.String)
     */
    public String digest (String password, String digestType, String inputEncoding) throws CmsException {
               
        MessageDigest md;
        byte[] result;
                
        try {     
            if (I_CmsPasswordHandler.C_DIGEST_TYPE_PLAIN.equals(digestType.toLowerCase())) {
                result = password.getBytes(inputEncoding);
            } else {
                md = MessageDigest.getInstance(digestType);
        
                md.reset();
                md.update(password.getBytes(inputEncoding));
                result = md.digest();
            } 
        } catch (NoSuchAlgorithmException exc) {
            throw new CmsException("Digest algorithm " + digestType + " not supported.");
        } catch (UnsupportedEncodingException exc) {
            throw new CmsException("Password encoding " + inputEncoding + " not supported.");
        }
        
        StringBuffer buf = new StringBuffer();
        String addZerro;
        for (int i = 0; i < result.length; i++) {
            addZerro = Integer.toHexString(128 + result[i]);
            if (addZerro.length() < 2) {
                addZerro = '0' + addZerro;
            }
            buf.append(addZerro);
        }
        return buf.toString();
    }
}
