/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsLegacySecurityException.java,v $
 * Date   : $Date: 2005/06/23 14:01:14 $
 * Version: $Revision: 1.9 $
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

import java.util.Locale;

/**
 * Exception to keep the legacy packages com.opencms.* and com.opencms.*
 * with the old exception handling mechanism running.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.9 $
 * @since 5.1.4
 */
public class CmsLegacySecurityException extends CmsSecurityException {
    
    /** 
     * 
     * MessageContainer with string-constructor, to be used only in this class.
     * CmsLegacyMessageContainers are not localized. <p>
     */
    private static class CmsLegacyMessageContainer extends CmsMessageContainer {
        
        /**
         * Creates a new message container with the specified message.<p>
         * 
         * @param message the message to use
         */
        public CmsLegacyMessageContainer(String message) {

            super(Messages.get(), message);
        }
        
        /**
         * Returns the message described by this container.<p>
         * 
         * @return the message described by this container
         */
        public String key() {

            return getKey();
        }

        /**
         * Returns the message described by this container. The message is not localized and the locale argument will
         * not be taken into account.<p>
         * 
         * @param locale the locale to use
         * @return the message described by this container
         */
        public String key(Locale locale) {

            return getKey();
        }

    }
    
    /** Stores the error code of the CmsLegacyException. */
    protected int m_type;
    
    /** No permissions to perform operation. */
    public static final int C_SECURITY_NO_PERMISSIONS = 303;
       
    /** Invalid password (only for password change and validation of password). */    
    public static final int C_SECURITY_INVALID_PASSWORD = 305;

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
     * Creates a CmsLegacySecurityException with the provided description message and error code.<p>
     * 
     * @param message the description message
     * @param type exception error code
     */
    public CmsLegacySecurityException(String message, int type) {

        super(new CmsLegacyMessageContainer(message));
        m_type = type;
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
