/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsPageException.java,v $
 * Date   : $Date: 2004/01/06 16:41:01 $
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
 
package org.opencms.page;

import com.opencms.core.CmsException;

/**
 * Signals that a particular action was invoked on resource with an insufficient lock state.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.1.4
 */
public class CmsPageException extends CmsException {
    
    // the allowed type range for this exception is >=400 and <500    
    
    /** Generic init error */
    public static final int C_PAGE_UNSPECIFIED_ERROR = 400;
    
    /** Wizard still enabled error */
    public static final int C_PAGE_DOCUMENT_ERROR = 401; 
        
    /**
     * Default constructor for a CmsInitException.<p>
     */
    public CmsPageException() {
        super();
    }
    
    /**
     * Constructs a CmsInitException with the specified description message and type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsPageException(int type) {
        super(type);
    }
        
    /**
     * Constructs a CmsInitException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsPageException(String message, int type) {
        super(message, type);
    }
    
    /**
     * Constructs a CmsInitException with the specified description message.<p>
     * 
     * @param message the description message
     */
    public CmsPageException(String message) {
        super(message, C_PAGE_UNSPECIFIED_ERROR);
    }    
    
    /**
     * Constructs a CmsInitException with the specified description message and root cause.<p>
     * 
     * @param message the description message
     * @param rootCause the root cause
     */
    public CmsPageException(String message, Throwable rootCause) {
        super(message, C_PAGE_UNSPECIFIED_ERROR, rootCause);
    }        
    
    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */    
    protected String getErrorDescription(int type) {
        switch (type) {
            case C_PAGE_UNSPECIFIED_ERROR:
                return "OpenCms page error";
            default:
                return super.getErrorDescription(type);
        }
    }
}
