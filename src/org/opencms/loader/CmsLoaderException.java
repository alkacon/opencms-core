/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsLoaderException.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.loader;

import org.opencms.main.CmsException;

/**
 * Signals exceptions occuring during the resource loading process.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.5 $
 * @since 5.3
 */
public class CmsLoaderException extends CmsException {

    /** Generic error code for loader errors. */
    public static final int C_LOADER_GENERIC_ERROR = 28; 
    
    /** Non-template loader called through template loader facade. */
    public static final int C_LOADER_NOT_TEMPLATE_ENABLED = 300;
    
    /** Unknown resource type. */
    public static final int C_LOADER_UNKNOWN_RESOURCE_TYPE = 301;
        
    /**
     * Default constructor for a CmsSecurityException.<p>
     */
    public CmsLoaderException() {
        super();
    }
    
    /**
     * Constructs a CmsLoaderException with the specified description message and type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsLoaderException(int type) {
        super(type);
    }
     
    /**
     * Constructs a CmsLoaderException with the specified description message and type.<p>
     * 
     * @param message the description message
     */
    public CmsLoaderException(String message) {
        super(message, C_LOADER_GENERIC_ERROR);
    }
    
    /**
     * Constructs a CmsLoaderException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsLoaderException(String message, int type) {
        super(message, type);
    }
    
    /**
     * Constructs a CmsLoaderException with the specified description message, type and root exception.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsLoaderException(String message, int type, Throwable rootCause) {
        super(message, type, rootCause);
    }
    
    /**
     * Constructs a CmsLoaderException with the specified description message and root exception.<p>
     * 
     * @param message the description message
     * @param rootCause root cause exception
     */
    public CmsLoaderException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
    
    /**
     * Returns the exception description message.<p>
     *
     * @return the exception description message
     */
    public String getMessage() {
        if (m_message != null) {
            return getClass().getName() + ": " + m_message;
        } else {
            return getClass().getName() + ": " + getErrorDescription(getType());
        }
    }    
    
    /**
     * Returns the description String for the provided CmsLoaderException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsLoaderException type
     */    
    protected String getErrorDescription(int type) {
        switch (type) {
            case C_LOADER_GENERIC_ERROR:
                return "Error while loading invoking resource loader";       
            case C_LOADER_NOT_TEMPLATE_ENABLED:
                return "Resource loader not template enabled";
            case C_LOADER_UNKNOWN_RESOURCE_TYPE:
                return "Unknown resource type requested!";                
            default:
                return super.getErrorDescription(type);
        }
    }
}
