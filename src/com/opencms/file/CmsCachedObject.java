/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCachedObject.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.12 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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

package com.opencms.file;

/**
 * Defines one individual object stored in the DBCacheFile and method to access it
 * 
 * @author Michael Emmerich
 * @author Andreas Schoutem
 * @version $Revision: 1.12 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsCachedObject implements Cloneable
{
    private Object m_contents;
    private long m_timestamp;
    
    /**
     * Creates a new CmsCachedObject.
     * The current system time is used as the timestamp.
     * 
     * @param contents The object to be stored in the CmsCache.
     */

    public CmsCachedObject(Object contents) {
        m_contents=contents;
        m_timestamp = System.currentTimeMillis();
    }
    /**
     * Creates a new CmsCachedObject.
     *  
     * @param contents The object to be stored in the CmsCache.
     * @param timestamp The timestamp for the new Object.
     */

    public CmsCachedObject(Object contents,long timestamp) {
        m_contents=contents;
        m_timestamp = timestamp;
    }
    /**
     * Clones the CachedObject. 
     * This is needed to return only clones of the objects stored in the cache
     * @param content Flag for cloning the file content, too.
     */

    public  Object clone() {
        // spceial clone-method for each content
        // if there is an easy way to do this - it may be replaced
        if(m_contents == null) {
            return new CmsCachedObject(null);
        } else if(m_contents instanceof Boolean) {
            return new CmsCachedObject(new Boolean(((Boolean)m_contents).booleanValue()));
        } else if(m_contents instanceof CmsResource) {
            return new CmsCachedObject(((CmsResource)m_contents).clone());
        } else if(m_contents instanceof CmsFile) {
            return new CmsCachedObject(((CmsFile)m_contents).clone());
        } else if(m_contents instanceof CmsFolder) {
            return new CmsCachedObject(((CmsFolder)m_contents).clone());
        } else if(m_contents instanceof CmsUser) {
            return new CmsCachedObject(((CmsUser)m_contents).clone());
        } else if(m_contents instanceof CmsGroup) {
            return new CmsCachedObject(((CmsGroup)m_contents).clone());
        } else if(m_contents instanceof CmsProject) {
            return new CmsCachedObject(((CmsProject)m_contents).clone());
        } else if(m_contents instanceof CmsPropertydefinition) {
            return new CmsCachedObject(((CmsPropertydefinition)m_contents).clone());
        } else if(m_contents instanceof java.util.Vector) {
            return new CmsCachedObject(((java.util.Vector)m_contents).clone());
        } else if(m_contents instanceof java.util.Hashtable) {
            return new CmsCachedObject(((java.util.Hashtable)m_contents).clone());
        } else if(m_contents instanceof String) {
            return new CmsCachedObject( new String((String)m_contents));
        } else if (m_contents instanceof byte[]) {
            return new CmsCachedObject(((byte[])m_contents).clone());
        } else {
            throw new InternalError();
        }
    }
    /**
     * Gets the contents of this CmsCacheObject.
     * The last access time of the Cache object is set tu the current system time.
     * 
     * @return Contents of the CmsCacheObject.
     */
    public Object getContents() {
        m_timestamp=System.currentTimeMillis();
        return m_contents;
    }
    /**
     * Gets the last access time for this CmsCacheObject.
     *
     *  @return The last access time.
     */
    public long getTimestamp() {
        return m_timestamp;
    }
    /**
     * Sets the last access time for this CmsCacheObject.
     */
    public void setTimestamp() {
        m_timestamp=System.currentTimeMillis();
    }
}
