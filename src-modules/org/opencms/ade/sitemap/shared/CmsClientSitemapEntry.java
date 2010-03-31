/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsClientSitemapEntry.java,v $
 * Date   : $Date: 2010/03/31 12:19:02 $
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

package org.opencms.ade.sitemap.shared;

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

    /** The sitemap path. */
    private String m_sitePath;

    /** The title. */
    private String m_title;

    /** The VFS path. */
    private String m_vfsPath;

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
     * Returns the sitemap path.<p>
     *
     * @return the sitemap path
     */
    public String getSitePath() {

        return m_sitePath;
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
     * Returns the vfs path.<p>
     *
     * @return the vfs path
     */
    public String getVfsPath() {

        return m_vfsPath;
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
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the VFS path.<p>
     *
     * @param path the path to set
     */
    public void setVfsPath(String path) {

        m_vfsPath = path;
    }
}