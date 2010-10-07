/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeEdit.java,v $
 * Date   : $Date: 2010/10/07 13:49:12 $
 * Version: $Revision: 1.3 $
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

import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.Map;

/**
 * Stores one edit change to the sitemap.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeEdit implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -2505651588029718797L;

    /** The entry's new properties. */
    private Map<String, CmsSimplePropertyValue> m_properties;

    /** The entry's site path. */
    private String m_sitePath;

    /** The entry's new title. */
    private String m_title;

    /** The entry's new VFS path. */
    private String m_vfsPath;

    /**
     * Constructor.<p>
     * 
     * @param sitePath the entry's site path
     * @param title the entry's new title
     * @param vfsPath the entry's new VFS path
     * @param properties the entry's new properties
     */
    public CmsSitemapChangeEdit(
        String sitePath,
        String title,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> properties) {

        m_sitePath = sitePath;
        m_title = title;
        m_vfsPath = vfsPath;
        m_properties = properties;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeEdit() {

        // empty
    }

    /**
     * Returns the entry's new properties.<p>
     *
     * @return the entry's new properties
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
     * Returns the entry's new title.<p>
     *
     * @return the entry's new title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.EDIT;
    }

    /**
     * Returns the entry's new VFS path.<p>
     *
     * @return the entry's new VFS path
     */
    public String getVfsPath() {

        return m_vfsPath;
    }
}