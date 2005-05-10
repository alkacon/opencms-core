/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsVfsResourceNotFoundException.java,v $
 * Date   : $Date: 2005/05/10 15:46:21 $
 * Version: $Revision: 1.4 $
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
 
package org.opencms.file;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals that an attempt to read a resource in the VFS denoted by a specified 
 * pathname has failed.<p> 
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $ $Date: 2005/05/10 15:46:21 $
 * @since 5.1.2
 */
public class CmsVfsResourceNotFoundException extends CmsVfsException {

    /**
     * Constructs a CmsVfsResourceNotFoundException with the specified detail message.<p>
     * 
     * @param message the detail message
     */
    public CmsVfsResourceNotFoundException(String message) {
        super(message, C_VFS_RESOURCE_NOT_FOUND);
    }

    /**
     * Constructs a CmsVfsResourceNotFoundException with the specified detail message
     * and adds the original exception as a delegated root cause.<p>
     * 
     * @param message the detail message
     * @param rootCause the delegated exception
     */
    public CmsVfsResourceNotFoundException(String message, Throwable rootCause) {
        super(message, C_VFS_RESOURCE_NOT_FOUND, rootCause);
    }
    
    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsVfsResourceNotFoundException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsVfsResourceNotFoundException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }   
    
    
    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsVfsResourceNotFoundException(container, cause);
    }
}
