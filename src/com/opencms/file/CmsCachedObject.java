/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsCachedObject.java,v $
 * Date   : $Date: 2000/02/19 10:15:27 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

/**
 * This class defines one individual object stored in the MhtDBCacheFile and method to access it
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/19 10:15:27 $
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
	
	/**
	 * Gets the contents of this CmsCacheObject.
	 * The last access time of the Cache object is set tu the current system time.
	 * 
	 * @return Contents of the CmsCacheObject.
	 */
	public Object getContents()	{
		m_timestamp=System.currentTimeMillis();
		return m_contents;
	}
	
     /**
	 * Clones the MhtCachedObject. 
	 * This is needed to return only clones of the objects stored in the cache
	 * @param content Flag for cloning the file content, too.
	 */
	 public  Object clone() {
		try { 
		    CmsCachedObject clonedObj = (CmsCachedObject)super.clone();
		    return clonedObj;
		  }			catch (CloneNotSupportedException e) 		{ 
		    // this shouldn't happen, since we are Cloneable
		    throw new InternalError();
		}
	
    }

}

