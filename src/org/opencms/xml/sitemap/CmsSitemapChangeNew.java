/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeNew.java,v $
 * Date   : $Date: 2010/10/07 13:49:12 $
 * Version: $Revision: 1.6 $
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
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeNew implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -3407815812421655807L;

    /** The id of the new sitemap entry (may be null). */
    private CmsUUID m_id;

    /** The entry's position. */
    private int m_position;

    /** The entry's properties. */
    private Map<String, CmsSimplePropertyValue> m_properties;

    /** The entry's site path. */
    private String m_sitePath;

    /** The entry's title. */
    private String m_title;

    /** The entry's VFS path. */
    private String m_vfsPath;

    /**
     * Constructor.<p>
     * 
     * @param sitePath the entry's site path
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param properties the entry's properties
     */
    public CmsSitemapChangeNew(
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
    }

    /**
     * Constructor.<p>
     * 
     * @param sitePath the entry's site path
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param properties the entry's properties
     * @param id the UUID of the sitemap entry 
     */
    public CmsSitemapChangeNew(
        String sitePath,
        int position,
        String title,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> properties,
        CmsUUID id) {

        m_sitePath = sitePath;
        m_position = position;
        m_title = title;
        m_vfsPath = vfsPath;
        m_properties = properties;
        m_id = id;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeNew() {

        // empty
    }

    /**
     * Gets the id of the new sitemap entry.<p>
     * 
     * If this is null, the sitemap entry will receive a freshly generated id.<p>
     * 
     * @return the id of the new sitemap entry 
     */
    public CmsUUID getId() {

        return m_id;
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

}