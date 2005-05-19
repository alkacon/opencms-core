/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsIndexException.java,v $
 * Date   : $Date: 2005/05/19 15:24:34 $
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

package org.opencms.search;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Signals an error during an indexing operation.<p> 
 * 
 * This exception is thrown by various classes 
 * in the <code>org.opencms.search</code> package.
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.9 $ $Date: 2005/05/19 15:24:34 $
 * @since 5.3.1
 */
public class CmsIndexException extends CmsException {

    /**
     * Constructs a CmsIndexException with the specified detail message
     * and adds the original exception as a delegated root cause.<p>
     * 
     * @param message the detail message
     * @param rootCause the delegated exception
     */
    public CmsIndexException(String message, Throwable rootCause) {

        super(message, 0, rootCause);
    }
    
    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsIndexException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsIndexException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }    
    
    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {
        
        return new CmsIndexException(container, cause);
    } 
}