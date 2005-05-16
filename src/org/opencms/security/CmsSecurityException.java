/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsSecurityException.java,v $
 * Date   : $Date: 2005/05/16 13:46:55 $
 * Version: $Revision: 1.15 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Used to signal security related issues, for example example during file access and login.<p> 
 * 
 * A security released issue impies that the operation attempted can be executed in general,
 * but that the current user who attemted it does not have the required permissions at the current time.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.15 $
 * @since 5.1.4
 */
public class CmsSecurityException extends CmsException {
    
    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsSecurityException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsSecurityException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }    
    
    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsSecurityException(container, cause);
    }
    
    /** No permissions to perform operation. */
    public static final int C_SECURITY_NO_PERMISSIONS = 303;
       
    /** Invalid password (only for password change and validation of password). */    
    public static final int C_SECURITY_INVALID_PASSWORD = 305;
    
    /** Login failed. */
    public static final int C_SECURITY_LOGIN_FAILED = 306;

    /**
     * Default constructor for a CmsSecurityException.<p>
     */
    public CmsSecurityException() {
        super();
    }
    
    /**
     * Constructs a CmsSecurityException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the message
     */
    public CmsSecurityException(String message, int type) {
        super(message, type);
    }
    
    /**
     * Constructs a CmsSecurityException with the specified description message and root exception.<p>
     * 
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsSecurityException(int type, Throwable rootCause) {
        super(type, rootCause);
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
            case C_SECURITY_LOGIN_FAILED:
                return "OpenCms login validation failed";                
            default:
                return super.getErrorDescription(type);
        }
    }
}
