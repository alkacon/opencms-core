/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsIndexResource.java,v $
 * Date   : $Date: 2004/02/20 13:35:45 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsUUID;


/**
 * Wrapper class to hide the concrete type of a data object.<p>
 * The type is either <code>CmsResource</code> while indexing vfs data,
 * or <code>CmsMasterDataSet</code> while indexing cos data.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/02/20 13:35:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public abstract class CmsIndexResource {
    
    /** Concrete data object */
    protected Object m_data;

    /** Id of the object */
    protected CmsUUID m_id;
    
    /** Name of the object */
    protected String m_name;
    
    /** Path to access the object */
    protected String m_path;
    
    /** Type of the object */
    protected int m_type;
    
    /** Mimetype of the object, <code>null</code> if the object is a <code>CmsMasterDataSet</code> */
    protected String m_mimeType;

    /** Channel of the object, <code>null</code> if the object is a <code>CmsResource</code> */
    protected String m_channel;
    
    /** Content definition of the object, <code>null</code> if the object is a <code>CmsResource</code> */
    protected String m_contentDefinition;
    
    /**
     * Returns the wrapped data object.<p>
     * 
     * The concrete type of the data object is either <code>CmsResource</code>
     * or <code>CmsMasterDataSet</code>
     * 
     * @return the wrapped data object
     */
    public Object getData() {
        return m_data;
    }
    
    /**
     * Returns the type of the wrapped object.<p>
     * 
     * The type is either the type of the wrapped <code>CmsResource</code> 
     * or the SubId of the <code>CmsMasterDataSet</code>.
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
    
    /**
     * Returns the document key for the search manager.<p> 
     * 
     * @return the document key for the search manager
     */
    public abstract String getDocumentKey();     
}
