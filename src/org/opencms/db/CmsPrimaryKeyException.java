/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsPrimaryKeyException.java,v $
 * Date   : $Date: 2005/02/17 12:43:46 $
 * Version: $Revision: 1.7 $
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

import org.opencms.main.CmsException;

/**
 * Signals that a foreign key in the VFS STRUCTURE, RESOURCES or FILES database tables is either
 * invalid or empty.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.7 $ $Date: 2005/02/17 12:43:46 $
 * @since 5.1.4
 */
public class CmsPrimaryKeyException extends CmsException {
    
    // the allowed type range for this exception is >=100 and <200
    
    /** An entry in the STRUCTURE table has an invalid resource ID. */
    public static final int C_INVALID_RESOURCE_ID = 101;
    
    /** An entry in the RESOURCES table has an invalid file ID. */
    public static final int C_INVALID_FILE_ID = 102;
    
    /** An entry in the STRUCTURE table has no resource ID. */
    public static final int C_RESOURCE_ID_EMPTY = 104;
    
    /** An entry in the RESOURCES table has no file ID. */
    public static final int C_FILE_ID_EMPTY = 105;    
    
    /**
     * Constructs a CmsPrimaryKeyException with the specified detail message and type.<p>
     * 
     * @param message the detail message
     * @param type the type of the exception
     */
    public CmsPrimaryKeyException(String message, int type) {
        super(message, type, null);
    }    

}
