/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsDefaultPasswordHandler.java,v $
 * Date   : $Date: 2005/02/17 12:44:41 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.security;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ExtendedProperties;

/**
 * Default implementation for OpenCms password validation,
 * just checks if a password is at last 4 characters long.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 *
 * @version $Revision: 1.6 $
 * @since 5.1.11 
 */
public class CmsDefaultPasswordHandler implements I_CmsPasswordHandler {

    /** The encoding the encoding used for translating the input string to bytes. */
    private String m_inputEncoding = "UTF-8";
    
    /** The digest type used. */
    private String m_digestType = "MD5";
    
    /**  The minimum length of a password. */
    public static final int C_PASSWORD_MINLENGTH = 4;

    /*
     * The secure random number generator.
     */
    private static SecureRandom prng = null;
    
    /*
     * The configuration of the password handler.
     */
    private static ExtendedProperties m_configuration;
    
    /**
     * The constructor does not perform any operation.<p>
     */
    public CmsDefaultPasswordHandler() {
        
        m_configuration = new ExtendedProperties();
    }
    
    /**
     * Returns the digestType.<p>
     *
     * @return the digestType
     */
    public String getDigestType() {

        return m_digestType;
    }
    
    /**
     * Sets the digestType.<p>
     *
     * @param digestType the digestType to set
     */
    public void setDigestType(String digestType) {

        m_digestType = digestType;
    }
    
    /**
     * Returns the input encoding.<p>
     *
     * @return the input encoding
     */
    public String getInputEncoding() {

        return m_inputEncoding;
    }
    
    /**
     * Sets the input encoding.<p>
     *
     * @param inputEncoding the input encoding to set
     */
    public void setInputEncoding(String inputEncoding) {

        m_inputEncoding = inputEncoding;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.addProperty(paramName, paramValue);
    }
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        // TODO: should be immutable
        return m_configuration;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        // simple default configuration does not need to be initialized
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("initConfiguration() called on " + this);
            // supress compiler warning, this is never true
            if (this == null) {
                throw new CmsConfigurationException();
            }
        }
    } 
    
    /**
     * @see org.opencms.security.I_CmsPasswordHandler#validatePassword(java.lang.String)
     */
    public void validatePassword(String password) throws CmsSecurityException {
        if (password.length() < C_PASSWORD_MINLENGTH) {
            throw new CmsSecurityException("Minimum password length " + C_PASSWORD_MINLENGTH, CmsSecurityException.C_SECURITY_INVALID_PASSWORD);
        }
    }
    
    /**
     * @see org.opencms.security.I_CmsPasswordHandler#digest(java.lang.String)
     */
    public String digest(String password) throws CmsException {
        
        return digest(password, m_digestType, m_inputEncoding);
    }
    
    /**
     * @see org.opencms.security.I_CmsPasswordHandler#digest(java.lang.String, java.lang.String, java.lang.String)
     */
    public String digest(String password, String digestType, String inputEncoding) throws CmsException {
        
        MessageDigest md;
        String result;
                
        try {
            if (C_DIGEST_TYPE_PLAIN.equals(digestType.toLowerCase())) {
                
                result = password;
                
            } else if (C_DIGEST_TYPE_SSHA.equals(digestType.toLowerCase())) {
                
                byte[] salt = new byte[4];
                byte[] digest;
                byte[] total;
                
                if (prng == null) {
                    prng = SecureRandom.getInstance ("SHA1PRNG");
                }
                prng.nextBytes(salt);
                    
                md = MessageDigest.getInstance(C_DIGEST_TYPE_SHA);
                md.reset();
                md.update(password.getBytes(inputEncoding));
                md.update(salt);
                
                digest = md.digest();
                total = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, total, 0, digest.length);
                System.arraycopy(salt, 0, total, digest.length, salt.length);
                
                result = new String(Base64.encodeBase64(total));
                
            } else {
                
                md = MessageDigest.getInstance(digestType);
                md.reset();
                md.update(password.getBytes(inputEncoding));
                result = new String(Base64.encodeBase64(md.digest()));
                
            }
        } catch (NoSuchAlgorithmException exc) {
            throw new CmsException("Digest algorithm " + digestType + " not supported.");
        } catch (UnsupportedEncodingException exc) {
            throw new CmsException("Password encoding " + inputEncoding + " not supported.");
        }
        
        return result;
    }
}