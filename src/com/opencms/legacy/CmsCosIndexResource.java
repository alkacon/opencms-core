/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsCosIndexResource.java,v $
 * Date   : $Date: 2004/11/19 15:05:36 $
 * Version: $Revision: 1.6 $
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
package com.opencms.legacy;

import org.opencms.search.A_CmsIndexResource;

import com.opencms.defaults.master.*;


/**
 * Wrapper class to hide the concrete type of a data object.<p>
 * The type is either <code>CmsResource</code> while indexing vfs data,
 * or <code>CmsMasterDataSet</code> while indexing cos data.
 * 
 * @version $Revision: 1.6 $ $Date: 2004/11/19 15:05:36 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsCosIndexResource extends A_CmsIndexResource {
    
    /**
     * Creates a new instance to wrap the given <code>CmsMasterDataSet</code>.<p>
     * 
     * @param ds the data object
     * @param path access path of the data object
     * @param channel channel of the data object
     * @param contentDefinition content definition of the data object
     */
    public CmsCosIndexResource(CmsMasterDataSet ds, String path, String channel, String contentDefinition) {
        m_data = ds;
        m_id = ds.m_masterId;
        m_name = ds.m_title;
        m_type = ds.m_subId;
        m_mimeType = null;
        m_path = path;
        m_channel = channel;
        m_contentDefinition = contentDefinition;
    }

    /**
     * @see org.opencms.search.A_CmsIndexResource#getDocumentKey(boolean)
     */
    public String getDocumentKey(boolean withMimeType) {
        return CmsCosDocument.C_DOCUMENT_KEY_PREFIX + getType();
    }

    /**
     * Channel of the object, <code>null</code> if the object is a <code>CmsResource</code>. 
     */
    protected String m_channel;
    /**
     * Content definition of the object, <code>null</code> if the object is a <code>CmsResource</code>. 
     */
    protected String m_contentDefinition;

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
}
