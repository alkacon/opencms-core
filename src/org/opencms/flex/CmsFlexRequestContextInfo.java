/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexRequestContextInfo.java,v $
 * Date   : $Date: 2004/03/25 11:45:05 $
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
package org.opencms.flex;

/**
 * Contains information about the OpenCms request context required by the 
 * Flex implementation.<p>
 * 
 * An instance of this class is attached to every <code>CmsRequestContext</code> as 
 * an attribute as soon as the request context is wrapped in a flex response.
 * Information about the "last modified" and "expire" times of VFS resources are 
 * stored in this Object.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class CmsFlexRequestContextInfo {
     
    /** The currently calculated "expires" date for this request context */
    private long m_dateExpires;
    
    /** The currently calculated "last modified" date for this request context  */
    private long m_dateLastModified;   
    
    /**
     * Public constructor.<p>
     */
    public CmsFlexRequestContextInfo() {
        // noop
    }
    
    /**
     * Updates the "last modified" date for this context with the given value.<p>
     * 
     * The currently stored value is only updated with the new value if
     * the new value is either larger (i.e. newer) then the stored value,
     * or if the new value is less then zero, which indicates that the "last modified"
     * optimization can not be used because the element is dynamic.<p>
     * 
     * @param dateLastModified the value to update the "last modified" date with
     */
    public void updateDateLastModified(long dateLastModified) {
        if ((m_dateLastModified > -1) && ((dateLastModified > m_dateLastModified) || (dateLastModified < 0))) {
            m_dateLastModified = dateLastModified;
        }         
    }
    
    /**
     * Updates the "expires" date for this context with the given value.<p>
     * 
     * @param dateExpires the value to update the "last modified" date with
     */
    public void updateDateExpires(long dateExpires) {
        if ((m_dateExpires > -1) && (dateExpires < m_dateExpires)) {
            m_dateExpires = dateExpires;
        }         
    }
    
    /**
     * Returns the "last modified" date for this context.<p>
     * 
     * @return the "last modified" date for this context
     */
    public long getDateLastModified() {
        return m_dateLastModified;
    }
    
    /**
     * Returns the "expires" date for this context.<p>
     * 
     * @return the "expires" date for this context
     */    
    public long getDateExpires() {
        return m_dateExpires;
    }
    
    /**
     * Merges this context info with the values from the other context info.<p>
     *  
     * @param other the context info to merge with
     */
    public void merge(CmsFlexRequestContextInfo other) {
        updateDateLastModified(other.getDateLastModified());
        updateDateExpires(other.getDateExpires());
    }
}
