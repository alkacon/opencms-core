/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/synchronize/CmsSynchronizeException.java,v $
 * Date   : $Date: 2003/07/21 08:17:42 $
 * Version: $Revision: 1.1 $
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
package org.opencms.synchronize;

import com.opencms.core.CmsException;

/**
 * Thrown by a class which implements com.opencms.file.I_CmsSyncModifications.<p>
 * 
 * When this exeption is thrown, 
 * all other implementations of I_CmsSyncModifications will not be executed.<p>
 * 
 * @author  Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsSynchronizeException extends CmsException {

    /**
     * Constructs a simple CmsSyncModificationException.<p>
     */
    public CmsSynchronizeException() {
        super();
    }

    /**
     * Constructs a CmsSyncModificationException with an additional error message.<p>
     *
     * @param message the exception message
     */
    public CmsSynchronizeException(String message) {
        super(message, 0, null, false);
    }
}
