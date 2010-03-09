/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/shared/Attic/CmsClientSitemapEntry.java,v $
 * Date   : $Date: 2010/03/09 10:33:14 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GWT implementation of {@link org.opencms.xml.sitemap.CmsSitemapEntry}.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapEntry implements IsSerializable {

    /** The entry id. */
    private String m_id;

    /** The entry name. */
    private String m_name;

    /** The property map. */
    private Map<String, String> m_properties;

    /** The VFS resource id. */
    private String m_resourceId;

    /** The site path. */
    private String m_sitePath;

    /** The list of subentries. */
    private List<CmsClientSitemapEntry> m_subEntries;

    /** The title. */
    private String m_title;

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the inherited properties.<p>
     * 
     * @return the inherited properties
     */
    public Map<String, String> getInheritedProperties() {

        return m_properties;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
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
     * Returns the file's structure id.<p>
     *
     * @return the file's structure id
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the sub-entries.<p>
     *
     * @return the sub-entries
     */
    public List<CmsClientSitemapEntry> getSubEntries() {

        return m_subEntries;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the properties.<p>
     *
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {

        m_properties = properties;
    }

    /**
     * Sets the resoureId.<p>
     *
     * @param resoureId the resoureId to set
     */
    public void setResourceId(String resoureId) {

        m_resourceId = resoureId;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the subEntries.<p>
     *
     * @param subEntries the subEntries to set
     */
    public void setSubEntries(List<CmsClientSitemapEntry> subEntries) {

        m_subEntries = subEntries;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}