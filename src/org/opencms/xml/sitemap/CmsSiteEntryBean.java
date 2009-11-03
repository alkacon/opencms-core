/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSiteEntryBean.java,v $
 * Date   : $Date: 2009/11/03 13:30:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One entry in a sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6 
 */
public class CmsSiteEntryBean {

    /** The file's structure id. */
    private final CmsUUID m_elementId;

    /** The configured properties. */
    private final Map<String, String> m_properties;

    /** The list of sub-entries. */
    private final List<CmsSiteEntryBean> m_subEntries;

    /**
     * Creates a new sitemap entry bean.<p> 
     *  
     * @param elementId the file's structure id
     * @param properties the properties as a map of name/value pairs
     * @param subEntries the list of sub-entries
     **/
    public CmsSiteEntryBean(CmsUUID elementId, Map<String, String> properties, List<CmsSiteEntryBean> subEntries) {

        m_elementId = elementId;
        Map<String, String> props = (properties == null ? new HashMap<String, String>() : properties);
        m_properties = Collections.unmodifiableMap(props);
        m_subEntries = Collections.unmodifiableList(subEntries);
    }

    /**
     * Returns the file's structure id.<p>
     *
     * @return the file's structure id
     */
    public CmsUUID getElementId() {

        return m_elementId;
    }

    /**
     * Returns the configured properties.<p>
     * 
     * @return the configured properties
     */
    public Map<String, String> getProperties() {

        return m_properties;
    }

    /**
     * Returns the sub-entries.<p>
     *
     * @return the sub-entries
     */
    public List<CmsSiteEntryBean> getSubEntries() {

        return m_subEntries;
    }
}
