/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsNotImplementedException.java,v $
 * Date   : $Date: 2005/05/11 08:32:42 $
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
 
package org.opencms.db;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals that an attempt to call a method has failed since it is not implemented.
 * This exception may be thrown by various driver implementation classes. 
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.5 $ $Date: 2005/05/11 08:32:42 $
 * @since 5.1.2
 */
public class CmsNotImplementedException extends CmsException {

    // the allowed type range for this exception is >=400 and <500    
    
    /** Administrator privileges required. */
    public static final int C_NOT_IMPLEMENTED_EXCEPTION = 400;   
        
    /**
     * Default constructor for a CmsNotImplementedException.<p>
     */
    public CmsNotImplementedException() {
        super();
    }
    
    /**
     * Constructs a CmsNotImplementedException with the specified type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsNotImplementedException(int type) {
        super(type);
    }

    /**
     * Constructs a default CmsNotImplementedException with the specified description message.<p>
     * 
     * @param message the description message
     */
    public CmsNotImplementedException(String message) {
        super(message, C_NOT_IMPLEMENTED_EXCEPTION);
    }
            
    /**
     * Constructs a CmsNotImplementedException with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsNotImplementedException(String message, int type) {
        super(message, type);
    }
    
    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer)
     */
    public CmsNotImplementedException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * @see org.opencms.main.CmsException#CmsException(CmsMessageContainer, Throwable)
     */
    public CmsNotImplementedException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }    
    
    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsNotImplementedException(container, cause);
    }
    
    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */    
    protected String getErrorDescription(int type) {
        switch (type) {
            case C_NOT_IMPLEMENTED_EXCEPTION:
                return "Method is not implemented";
            default:
                return super.getErrorDescription(type);
        }
    }
}
