/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexRequestContextInfo.java,v $
 * Date   : $Date: 2004/06/14 14:25:57 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsResource;

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
 * @version $Revision: 1.3 $
 */
public class CmsFlexRequestContextInfo {
     
    /** The currently calculated "expires" date for this request context .*/
    private long m_dateExpires;
    
    /** The currently calculated "last modified" date for this request context.  */
    private long m_dateLastModified;   
    
    /**
     * Public constructor.<p>
     */
    public CmsFlexRequestContextInfo() {
        // by default the expiration date is the max long value
        m_dateExpires = CmsResource.DATE_EXPIRED_DEFAULT;
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
     * Returns the "last modified" date for this context.<p>
     * 
     * @return the "last modified" date for this context
     */
    public long getDateLastModified() {
        return m_dateLastModified;
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
    
    /**
     * Updates the "expires" date for this context with the given value.<p>
     * 
     * @param dateExpires the value to update the "expires" date with
     */
    public void updateDateExpires(long dateExpires) {
        if (dateExpires > System.currentTimeMillis()) {
            if (dateExpires < m_dateExpires) {
                m_dateExpires = dateExpires;
            }
        } else {
            updateDateLastModified(dateExpires);
        }
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
     * Updates both the "last modified" and the "expires" date
     * for this context with the given values.<p>
     * 
     * @param dateLastModified the value to update the "last modified" date with
     * @param dateExpires the value to update the "expires" date with
     */
    public void updateDates(long dateLastModified, long dateExpires) {
        updateDateLastModified(dateLastModified);
        updateDateExpires(dateExpires);
    }    
    
    /**
     * Updates the "last modified" date for this context as well as the
     * "expires" date with the values from a given resource.<p>
     * 
     * The "expires" date is the calculated from the given date values 
     * of resource release and expiration and also the current time.<p>
     * 
     * @param resource the resource to use for updating the context values
     */
    public void updateFromResource(CmsResource resource) {
        // first set the last modification date
        updateDateLastModified(resource.getDateLastModified());
        // now use both release and expiration date from the resource to update the expires info
        updateDateExpires(resource.getDateReleased());
        updateDateExpires(resource.getDateExpired());
    }
}
