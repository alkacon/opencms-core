/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/I_CmsFileListUsers.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.11 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001 - 2005 The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * Interface for all classes using the workplace file list.
 * <P>
 * Any class called by CmsXmlTemplateFile for handling the special workplace tag
 * <code>&lt;FILELIST&gt;</code> has to implement this interface.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.11 $ $Date: 2005/02/18 14:23:15 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * @see com.opencms.workplace.CmsFileList
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public interface I_CmsFileListUsers {
    
    /**
     * Fill all customized columns with the appropriate settings for the given file 
     * list entry. Any column filled by this method may be used in the customized template
     * for the file list.
     * @param cms Cms object for accessing system resources.
     * @param filelist Template file containing the definitions for the file list together with
     * the included customized defintions.
     * @param res CmsResource Object of the current file list entry.
     * @param lang Current language file.
     * @throws CmsException if access to system resources failed.
     */
    
    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, 
            CmsResource res, CmsXmlLanguageFile lang) throws CmsException;
    
    /** 
     * Collect all folders and files that are displayed in the file list.
     * @param cms The CmsObject.
     * @return A vector of folder and file objects.
     * @throws Throws CmsException if something goes wrong.
     */
    
    public List getFiles(CmsObject cms) throws CmsException;
    
    /**
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * @param cms Cms object for accessing system resources.
     * @param prefs Old bit pattern.
     * @return New modified bit pattern.
     */
    
    public int modifyDisplayedColumns(CmsObject cms, int prefs);
}
