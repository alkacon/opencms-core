/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeNew.java,v $
 * Date   : $Date: 2011/01/14 11:59:10 $
 * Version: $Revision: 1.8 $
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
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.Map;

/**
 * Stores one addition change to the sitemap.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeNew implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -3707815812421655807L;

    /** The entry url name. */
    private String m_name;

    /** The parent entry id. */
    private CmsUUID m_parentId;

    /** The entry's position. */
    private int m_position;

    /** The entry's properties. */
    private Map<String, CmsSimplePropertyValue> m_properties;

    /** The entry's site path. */
    private String m_sitePath;

    /** The id of the new sitemap entry (may be null). */
    private CmsUUID m_structureId;

    /** The entry's title. */
    private String m_title;

    /** The entry's VFS path. */
    private String m_vfsPath;

    /**
     * Constructor.<p>
     * 
     * @param structureId the UUID of the sitemap entry 
     * @param sitePath the entry's site path
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param properties the entry's properties
     * 
     */
    public CmsSitemapChangeNew(
        CmsUUID structureId,
        String sitePath,
        int position,
        String title,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> properties) {

        m_sitePath = sitePath;
        m_position = position;
        m_title = title;
        m_vfsPath = vfsPath;
        m_properties = properties;
        m_structureId = structureId;
    }

    /**
     * Constructor.<p>
     * 
     * @param structureId the UUID of the sitemap entry 
     * @param sitePath the entry's site path
     * @param name the entry url name
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param parentId the parent entry id
     * @param properties the entry's properties
     */
    public CmsSitemapChangeNew(
        CmsUUID structureId,
        String sitePath,
        String name,
        int position,
        String title,
        String vfsPath,
        CmsUUID parentId,
        Map<String, CmsSimplePropertyValue> properties) {

        m_sitePath = sitePath;
        m_name = name;
        m_position = position;
        m_title = title;
        m_vfsPath = vfsPath;
        m_properties = properties;
        m_parentId = parentId;
        m_structureId = structureId;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeNew() {

        // empty
    }

    /**
     * Returns the entry url name.<p>
     *
     * @return the entry url name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the parent entry id.<p>
     *
     * @return the parent entry id
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * Returns the entry's  position.<p>
     *
     * @return the entry's position
     */
    public int getPosition() {

        return m_position;
    }

    /**
     * Returns the entry's properties.<p>
     *
     * @return the entry's properties
     */
    public Map<String, CmsSimplePropertyValue> getProperties() {

        return m_properties;
    }

    /**
     * Returns the entry's site path.<p>
     *
     * @return the entry's site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Gets the id of the new sitemap entry.<p>
     * 
     * If this is null, the sitemap entry will receive a freshly generated id.<p>
     * 
     * @return the id of the new sitemap entry 
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the entry's title.<p>
     *
     * @return the entry's title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.NEW;
    }

    /**
     * Returns the entry's VFS path.<p>
     *
     * @return the entry's VFS path
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /**
     * Sets the entry url name.<p>
     *
     * @param name the entry url name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "NEW " + m_sitePath;
    }
}