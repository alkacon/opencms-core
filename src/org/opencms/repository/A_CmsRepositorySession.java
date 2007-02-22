/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/A_CmsRepositorySession.java,v $
 * Date   : $Date: 2007/02/22 12:35:51 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.repository;



/**
 * Abstract super class to use for a repository session to make the functionality
 * of filters available.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.6
 */
public abstract class A_CmsRepositorySession implements I_CmsRepositorySession {
    
    /** The filter to use for this session. */
    private CmsRepositoryFilter m_filter;
    
    /**
     * Sets the filter.<p>
     *
     * @param filter the filter to set
     */
    public void setFilter(CmsRepositoryFilter filter) {
    
        m_filter = filter;
    }
    
    /**
     * Checks if a path is filtered out of the filter or not.<p>
     * 
     * @param path the path of a resource to check
     * @return true if the name matches one of the given filter patterns
     */
    protected boolean isFiltered(String path) {
        
        if (m_filter == null) {
            return false;
        }
        
        return m_filter.isFiltered(path);
    }
    
}
