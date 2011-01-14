/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeDelete.java,v $
 * Date   : $Date: 2011/01/14 11:59:10 $
 * Version: $Revision: 1.4 $
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

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeDelete implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -6327424948083713380L;

    /** The deleted entry's site path . */
    private String m_sitePath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /**
     * Constructor.<p>
     * 
     * @param structureId the structure id
     * @param sitePath the deleted entry's site path
     */
    public CmsSitemapChangeDelete(CmsUUID structureId, String sitePath) {

        m_structureId = structureId;
        m_sitePath = sitePath;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeDelete() {

        // empty
    }

    /**
     * Returns the deleted entry's site path.<p>
     *
     * @return the deleted entry's site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getStructureId()
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.DELETE;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "DELETE " + m_sitePath;
    }
}