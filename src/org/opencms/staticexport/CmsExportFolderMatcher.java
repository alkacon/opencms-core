/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsExportFolderMatcher.java,v $
 * Date   : $Date: 2004/03/29 09:56:41 $
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
package org.opencms.staticexport;

import org.opencms.main.OpenCms;

import org.apache.oro.text.PatternCache;
import org.apache.oro.text.PatternCacheFIFO;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * This class provides a file name matcher to find out those resources which must be part of 
 * a static export.<p> 
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsExportFolderMatcher {

    /** Internal array containing the vfs folders that should be exported*/
    private String[] m_vfsFolders = null;
    
    /** Perl5 utility class */
    private Perl5Util m_perlUtil = null;
    
    /** Perl5 patter cache to avoid unecessary re-parsing of properties */
    private PatternCache m_perlPatternCache = null;   
    
    
    /**
     * Creates a new CmsExportFolderMatcher.<p>
     * 
     * @param vfsFolders array of vfsFolder used for static export 
     */
    public CmsExportFolderMatcher(String[] vfsFolders) {
        m_vfsFolders = vfsFolders;
        // Pre-cache the patterns 
        m_perlPatternCache = new PatternCacheFIFO(m_vfsFolders.length+1);
        for (int i=0; i<m_vfsFolders.length; i++) {
            try {
                m_perlPatternCache.addPattern(m_vfsFolders[i]);
            } catch (MalformedPatternException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Malformed resource translation rule: \"" + m_vfsFolders[i] + "\"");
                }
            }
        }        
        // Initialize the Perl5Util
        m_perlUtil = new Perl5Util(m_perlPatternCache);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(". Static Export folder matches : " + vfsFolders.length + " rules initialized");
        }          
    }    
    
    /**
     * Checks if a vfsName matches the given static export folders.<p>
     * 
     * @param vfsName the vfs name of a resource to check
     * @return true if the name matches one of the given static export folders
     */
    public boolean match(String vfsName) {
        boolean match = false;
        for (int i=0; i<m_vfsFolders.length; i++) {
            if (m_perlUtil.match(m_vfsFolders[i], vfsName)) {
                match = true;
            }
        }        
        return match;        
    }
}
