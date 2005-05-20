/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsLegacySecurityException.java,v $
 * Date   : $Date: 2005/05/20 14:32:31 $
 * Version: $Revision: 1.3 $
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
 
package com.opencms.legacy;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.security.CmsSecurityException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * Used to signal security related issues, for example example during file access and login.<p> 
 * 
 * A security released issue impies that the operation attempted can be executed in general,
 * but that the current user who attemted it does not have the required permissions at the current time.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 * @since 5.1.4
 */
public class CmsLegacySecurityException extends CmsSecurityException {
    
    /** No permissions to perform operation. */
    public static final int C_SECURITY_NO_PERMISSIONS = 303;
       
    /** Invalid password (only for password change and validation of password). */    
    public static final int C_SECURITY_INVALID_PASSWORD = 305;
    
    private String m_errorMessage;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param message the localized message container to use
     */
    public CmsLegacySecurityException(CmsMessageContainer message) {

        super(message);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param message the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsLegacySecurityException(CmsMessageContainer message, Throwable cause) {

        super(message);
        initCause(cause);
    }
    
    /**
     * Creates a copied instance of this localized exception.<p>
     * 
     * @param container the message container
     * @param cause the root cause
     * 
     * @return a copied instance of this localized exception
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsLegacySecurityException(container, cause);
    }    

    /**
     * Creates a CmsLegacySecurityException with the provided description message.<p>
     *
     * @param message the description message
     */
    public CmsLegacySecurityException(String message) {

        super(null);
        m_errorMessage = message;
    }

    /**
     * Creates a CmsLegacySecurityException with the provided description message and error code.<p>
     * 
     * @param message the description message
     * @param type exception error code
     */
    public CmsLegacySecurityException(String message, int type) {

        super(null);
        m_errorMessage = message;
        m_type = type;
    }

    /**
     * Creates a CmsLegacySecurityException with the provided description message, error code and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param type exception error code
     * @param cause root cause exception
     */
    public CmsLegacySecurityException(String message, int type, Throwable cause) {

        super(null);
        m_errorMessage = message;
        initCause(cause);
        m_type = type;
    }

    /**
     * Construtcs a CmsLegacySecurityException with the provided description message and 
     * a given root cause.<p>
     *
     * @param message the description message
     * @param cause root cause exception
     */
    public CmsLegacySecurityException(String message, Throwable cause) {

        super(null);
        m_errorMessage = message;
        initCause(cause);
    }

    /**
     * Returns the stack trace (including the message) of an exception as a String.<p>
     * 
     * If the exception is a CmsLegacySecurityException, 
     * also writes the root cause to the String.<p>
     * 
     * @param e the exception to get the stack trace from
     * @return the stack trace of an exception as a String
     */
    public static String getStackTraceAsString(Throwable e) {

        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {

        if (m_message == null) {
            return super.getLocalizedMessage();
        }
        return m_message.key();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage(Locale)
     */
    public String getLocalizedMessage(Locale locale) {

        return m_message.key(locale);
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage() {

        if (m_message != null) {
            // localized message is available
            return getLocalizedMessage();
        }

        StringBuffer result = new StringBuffer(256);
        result.append(super.getMessage());

        if (getType() > 0) {
            result.append(' ');
            result.append(getErrorDescription(getType()));
        }

        return result.toString();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getMessageContainer()
     */
    public CmsMessageContainer getMessageContainer() {

        return m_message;
    }

    /**
     * Returns the type of the CmsLegacySecurityException.<p>
     *
     * @return the type of the CmsLegacySecurityException
     */
    public int getType() {

        return m_type;
    }
    
    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */    
    protected String getErrorDescription(int type) {
        switch (type) {
            case C_SECURITY_NO_PERMISSIONS:
                return "No permissions to perform this operation";
            case C_SECURITY_INVALID_PASSWORD:
                return "Invalid password";                              
            default:
                return this.getClass().getName();
        }
    }
}
