/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsHtmlConverterException.java,v $
 * Date   : $Date: 2004/10/08 13:57:03 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.util;

import org.opencms.main.CmsException;

/**
 * Used to signal problems during tidy html parsing.<p> 
 */
public class CmsHtmlConverterException extends CmsException {

    // the allowed type range for this exception is >=500 and <600    
    
    /** Administrator privileges required. */
    public static final int C_HTML_PARSING_ERROR = 500;
    
    
    /**
     * Default constructor for a CmsHtmlConverterException.<p>
     */
    public CmsHtmlConverterException() {
        super();        
    }
    
    /**
     * Constructs a CmsHtmlConverterException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsHtmlConverterException(String message, int type) {
        super(message, type);  
    }
    
}
