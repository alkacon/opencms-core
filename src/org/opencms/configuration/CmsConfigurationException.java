/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsConfigurationException.java,v $
 * Date   : $Date: 2004/06/13 23:31:17 $
 * Version: $Revision: 1.2 $
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

package org.opencms.configuration;

import org.opencms.main.CmsException;

/**
 * Exceptions that occur during the XML configuration process.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsConfigurationException extends CmsException {
    
    /** Administrator privileges required */
    public static final int C_CONFIGURATION_ERROR = 300;
    
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
     * Constructs a CmsConfigurationException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsConfigurationException(String message, int type) {
        super(message, type);
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
     * Returns the exception description message.<p>
     *
     * @return the exception description message
     */
    public String getMessage() {
        return getClass().getName() + ": " + getErrorDescription(getType());
    }
    
    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */    
    protected String getErrorDescription(int type) {
        switch (type) {
            case C_CONFIGURATION_ERROR:
                return "Error in the OpenCms configuration.";              
            default:
                return super.getErrorDescription(type);
        }
    }
}
