/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsVfsException.java,v $
 * Date   : $Date: 2005/05/16 13:46:56 $
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

package org.opencms.file;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Used to signal VFS related issues, for example during file access.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Moossen (a.kandzior@alkacon.com)
 * @version $Revision: 1.9 $
 * @since 5.1.4
 */
public class CmsVfsException extends CmsDataAccessException {

    /** Error code for not empty exception. */
    public static final int C_VFS_FOLDER_NOT_EMPTY = 5; //C_NOT_EMPTY;    

    /** Resource already exists. */
    public static final int C_VFS_RESOURCE_ALREADY_EXISTS = 12; //C_FILE_EXISTS;

    /** Resource deleted. */
    public static final int C_VFS_RESOURCE_DELETED = 32; //C_RESOURCE_DELETED;

    // TODO: to change the constants in a more consistent way
    /** Resource not found. */
    public static final int C_VFS_RESOURCE_NOT_FOUND = 2; //C_NOT_FOUND;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsVfsException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsVfsException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }

    /**
     * Constructs a exception with the specified description message and type.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     */
    public CmsVfsException(String message, int type) {

        super(message, type);
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsVfsException(container, cause);
    }

    /**
     * Returns the description String for the provided CmsException type.<p>
     * 
     * @param type exception error code 
     * @return the description String for the provided CmsException type
     */
    protected String getErrorDescription(int type) {

        switch (type) {
            case C_VFS_RESOURCE_NOT_FOUND:
                return "Resource not found!";
            case C_VFS_RESOURCE_ALREADY_EXISTS:
                return "Resource already exists!";
            case C_VFS_RESOURCE_DELETED:
                return "Resource has been deleted!";
            case C_VFS_FOLDER_NOT_EMPTY:
                return "Folder is not empty!";
            default:
                return super.getErrorDescription(type);
        }
    }
}
