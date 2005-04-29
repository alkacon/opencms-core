/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationException.java,v $
 * Date   : $Date: 2005/04/29 15:00:35 $
 * Version: $Revision: 1.10 $
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

package org.opencms.configuration;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Exceptions that occur during the XML configuration process.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsConfigurationException extends CmsException {
    
    /** General configuration error. */
    public static final int C_CONFIGURATION_ERROR = 300;
    
    /** Required module dependencies not fulfilled. */
    public static final int C_CONFIGURATION_MODULE_DEPENDENCIES = 301;

    /**
     * Default constructor for a CmsConfigurationException.<p>
     */
    public CmsConfigurationException() {
        this(C_CONFIGURATION_ERROR);
    }
    
    /**
     * Constructs a CmsConfigurationException with the specified description message and type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsConfigurationException(int type) {
        super(type);
    }
    
    /**
     * Constructs a CmsConfigurationException with the specified description message and root exception.<p>
     * 
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsConfigurationException(int type, Throwable rootCause) {
        super(type, rootCause);
    }        
    
    /**
     * Constructs a CmsConfigurationException with the specified description 
     * and a default type.<p>
     * 
     * @param message the description message
     */
    public CmsConfigurationException(String message) {
        super(message, C_CONFIGURATION_ERROR);
    }    
        
    /**
     * Constructs a CmsConfigurationException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsConfigurationException(String message, int type) {
        super(message, type);
    }
    
    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsConfigurationException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsConfigurationException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }     
    
    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */
    protected String getErrorDescription(int type) {

        if (m_message != null) {
            // return the message of the CmsMessageContainer object, if it is set
            return m_message.key();
        } else {

            switch (type) {
                case C_CONFIGURATION_ERROR:
                    return "Error in the OpenCms configuration.";
                case C_CONFIGURATION_MODULE_DEPENDENCIES:
                    return "Module dependencies not fulfilled.";
                default:
                    return super.getErrorDescription(type);
            }
        }
    }        
}
