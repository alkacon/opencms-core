/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsDefaultPasswordHandler.java,v $
 * Date   : $Date: 2004/10/15 15:09:28 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ExtendedProperties;

/**
 * Default implementation for OpenCms password validation,
 * just checks if a password is at last 4 characters long.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 *
 * @version $Revision: 1.3 $
 * @since 5.1.11 
 */
public class CmsDefaultPasswordHandler implements I_CmsPasswordHandler {

    /** The encoding the encoding used for translating the input string to bytes. */
    private String m_inputEncoding = "UTF-8";
    
    /** The digest type used. */
    private String m_digestType = "MD5";
    
    /**  The minimum length of a password. */
    public static final int C_PASSWORD_MINLENGTH = 4;
    
    /**
     * The constructor does not perform any operation.<p>
     */
    public CmsDefaultPasswordHandler() {
        // empty
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

        // this configuration does not support parameters 
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "addConfigurationParameter(" + paramName + ", " + paramValue + ") called on " + this);
        }
    }
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        // this configuration does not support parameters
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getConfiguration() called on " + this);
        }
        return null;
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
        
        return new String(Base64.encodeBase64(result));
    }
}