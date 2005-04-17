/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsVfsException.java,v $
 * Date   : $Date: 2005/04/17 18:07:16 $
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
 
package org.opencms.file;

import org.opencms.db.CmsDataAccessException;

/**
 * Used to signal VFS related issues, for example during file access.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Moossen (a.kandzior@alkacon.com)
 * @version $Revision: 1.5 $
 * @since 5.1.4
 */
public class CmsVfsException extends CmsDataAccessException {

    /** Folders don't suport siblings. */
    public static final int C_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS = 300;

    /** List of property must not contain equal properties. */
    public static final int C_VFS_INVALID_PROPERTY_LIST = 301;
    
    /** Undo changes on a new resource is impossible. */
    public static final int C_VFS_UNDO_CHANGES_NOT_POSSIBLE_ON_NEW_RESOURCE = 302;
    
    // TODO: to change the constants in a more consistent way
    /** Resource not found. */
    public static final int C_VFS_RESOURCE_NOT_FOUND = 2; //C_NOT_FOUND;

    /** Resource already exists. */
    public static final int C_VFS_RESOURCE_ALREADY_EXISTS = 12; //C_FILE_EXISTS;
    
    /** Resource deleted. */
    public static final int C_VFS_RESOURCE_DELETED = 32; //C_RESOURCE_DELETED;

    /** Error code for not empty exception. */
    public static final int C_VFS_FOLDER_NOT_EMPTY = 5; //C_NOT_EMPTY;
    
    /**
     * Default constructor for a CmsSecurityException.<p>
     */
    public CmsVfsException() {
        super();
    }
    
    /**
     * Constructs a exception with the specified description message and type.<p>
     * 
     * @param type the type of the exception
     */
    public CmsVfsException(int type) {
        super(type);
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
     * Constructs a exception with the specified description message and root exception.<p>
     * 
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsVfsException(int type, Throwable rootCause) {
        super(type, rootCause);
    }        
    
    /**
     * Constructs a exception with the specified description message and root exception.<p>
     * 
     * @param message the description message
     * @param type the type of the exception
     * @param rootCause root cause exception
     */
    public CmsVfsException(String message, int type, Throwable rootCause) {
        super(message, type, rootCause);
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
            case C_VFS_FOLDERS_DONT_SUPPORT_SIBLINGS:
                return "Folders in the VFS don't support siblings!";
            case C_VFS_INVALID_PROPERTY_LIST:                
                return "Invalid multiple occurence of equal properties in property list!";
            case C_VFS_RESOURCE_ALREADY_EXISTS:
                return "Resource already exists!";
            case C_VFS_UNDO_CHANGES_NOT_POSSIBLE_ON_NEW_RESOURCE:
                return "Undo changes is not possible on a new resource!";
            case C_VFS_RESOURCE_DELETED:
                return "Resource has been deleted!";
            case C_VFS_FOLDER_NOT_EMPTY:
                return "Folder is not empty!";
            default:
                return super.getErrorDescription(type);
        }
    }
}
