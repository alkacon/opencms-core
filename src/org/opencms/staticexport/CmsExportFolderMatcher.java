/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsExportFolderMatcher.java,v $
 * Date   : $Date: 2004/07/08 13:52:47 $
 * Version: $Revision: 1.4 $
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
package org.opencms.staticexport;

import org.opencms.util.CmsStringSubstitution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class provides a file name matcher to find out those resources which must be part of 
 * a static export.<p> 
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsExportFolderMatcher {

    /** Internal array containing the vfs folders that should be exported. */
    private List m_vfsFolders;
    
    /**
     * Creates a new CmsExportFolderMatcher.<p>
     * 
     * @param vfsFolders array of vfsFolder used for static export 
     * @param checkResource additional resource name to be added to the static export
     */
    public CmsExportFolderMatcher(List vfsFolders, String checkResource) {
        
        m_vfsFolders = new ArrayList();
        
        Iterator i = vfsFolders.iterator();
        while (i.hasNext()) {
            m_vfsFolders.add(Pattern.compile((String)i.next()));
        }
                    
        m_vfsFolders.add(Pattern.compile(CmsStringSubstitution.escapePattern(checkResource)));     
        
    }    
    
    /**
     * Checks if a vfsName matches the given static export folders.<p>
     * 
     * @param vfsName the vfs name of a resource to check
     * @return true if the name matches one of the given static export folders
     */
    public boolean match(String vfsName) {
        boolean match = false;
       
        for (int j = 0; j < m_vfsFolders.size(); j++) {
            Pattern pattern = (Pattern)m_vfsFolders.get(j);
             match = pattern.matcher(vfsName).matches();
             if (match) {
                break;
             }
        }        
        return match;        
    }
}
