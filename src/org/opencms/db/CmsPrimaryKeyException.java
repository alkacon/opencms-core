/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsPrimaryKeyException.java,v $
 * Date   : $Date: 2003/07/16 13:45:49 $
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
 
package org.opencms.db;

import com.opencms.core.CmsException;

/**
 * Signals that a foreign key in the VFS STRUCTURE, RESOURCES or FILES database tables is either
 * invalid or empty.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/07/16 13:45:49 $
 * @since 5.1.4
 */
public class CmsPrimaryKeyException extends CmsException {
    
    /** An entry in the STRUCTURE table has an invalid parent ID */
    public static final int C_INVALID_PARENT_ID = 1;
    
    /** An entry in the STRUCTURE table has an invalid resource ID */
    public static final int C_INVALID_RESOURCE_ID = 2;
    
    /** An entry in the RESOURCES table has an invalid file ID */
    public static final int C_INVALID_FILE_ID = 3;
    
    /** An entry in the STRUCTURE table has no parent ID */
    public static final int C_PARENT_ID_EMPTY = 4;
    
    /** An entry in the STRUCTURE table has no resource ID */
    public static final int C_RESOURCE_ID_EMPTY = 16;
    
    /** An entry in the RESOURCES table has no file ID */
    public static final int C_FILE_ID_EMPTY = 32;    
    
    /**
     * Constructs a CmsPrimaryKeyException with the specified detail message and type.
     * 
     * @param message the detail message
     * @param type the type of the exception
     */
    public CmsPrimaryKeyException(String message, int type) {
        super(message, type, null);
    }    

}
