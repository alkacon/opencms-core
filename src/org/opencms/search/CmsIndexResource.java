/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsIndexResource.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.2 $
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
package org.opencms.search;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.CmsMasterDataSet;
import org.opencms.file.CmsResource;

/**
 * Wrapper class to hide the concrete type of a data object to index.<p>
 * The type is either <code>CmsResource</code> while indexing vfs data,
 * or <code>CmsMasterDataSet</code> while indexing cos data.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/13 13:41:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsIndexResource {

    /**
     * Concrete data object
     */
    private Object m_obj;

    /**
     * Id of the object
     */
    private CmsUUID m_id;
    
    /**
     * Name of the object
     */
    private String m_name;
    
    /**
     * Path to access the object
     */
    private String m_path;
    
    /**
     * Type of the object
     */
    private int m_type;
    
    /**
     * Mimetype of the object, <code>null</code> if the object is a <code>CmsMasterDataSet</code>
     */
    private String m_mimeType;

    /**
     * Channel of the object, <code>null</code> if the object is a <code>CmsResource</code>
     */
    private String m_channel;
    
    /**
     * Content definition of the object, <code>null</code> if the object is a <code>CmsResource</code>
     */
    private String m_contentDefinition;
    
    /**
     * Creates a new instance to wrap the given data object.<p>
     * 
     * @param res the data object
     */
    public CmsIndexResource(CmsResource res) {
        m_obj = res;
        m_id = res.getResourceId();
        m_name = res.getName();
        m_type = res.getType();
        m_mimeType = OpenCms.getMimeType(res.getName(), null);
        m_path = res.getRootPath();
        m_channel = null;
        m_contentDefinition = null;
    }

    /**
     * Creates a new instance to wrap the given data object.<p>
     * 
     * @param ds the data object
     * @param path access path of the data object
     * @param channel channel of the data object
     * @param contentDefinition content definition of the data object
     */
    public CmsIndexResource(CmsMasterDataSet ds, String path, String channel, String contentDefinition) {
        m_obj = ds;
        m_id = ds.m_masterId;
        m_name = ds.m_title;
        m_type = ds.m_subId;
        m_mimeType = null;
        m_path = path;
        m_channel = channel;
        m_contentDefinition = contentDefinition;
    }
    
    /**
     * Returns the wrapped object.<p>
     * 
     * @return the wrapped object
     */
    public Object getObject() {
        return m_obj;
    }
    
    /**
     * Returns the type of the wrapped object.<p>
     * 
     * @return the type of the wrapped object
     */
    public int getType() {
        return m_type;
    }
    
    /**
     * Returns the mimetype of the wrapped object.<p>
     * 
     * @return the mimetype of the wrapped object or <code>null</code>
     */
    public String getMimetype() {
        return m_mimeType;
    }
    
    /**
     * Returns the name of the wrapped object.<p>
     * 
     * @return the name of the wrapped object
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Returns the access path of the wrapped object.<p>
     * 
     * @return the access path of the wrapped object
     */
    public String getRootPath() {
        return m_path;
    }
    
    /**
     * Returns the channel of the wrapped object.<p>
     * 
     * @return the channel of the wrapped object or <code>null</code>
     */
    public String getChannel() {
        return m_channel;
    }
    
    /**
     * Returns the content definition of the wrapped object.<p>
     * 
     * @return the content definition or <code>null</code>
     */
    public String getContentDefinition() {
        return m_contentDefinition;
    }
    
    /**
     * Returns the id of the wrapped object.<p>
     * 
     * @return the id
     */
    public CmsUUID getId() {
        return m_id;
    }
}
