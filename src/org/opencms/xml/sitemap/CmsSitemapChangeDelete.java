/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeDelete.java,v $
 * Date   : $Date: 2010/05/18 12:58:17 $
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

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeDelete implements I_CmsSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -6327424948083713380L;

    /** The deleted entry's site path . */
    private String m_sitePath;

    /**
     * Serialization constructor.<p>
     */
    protected CmsSitemapChangeDelete() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param sitePath the deleted entry's site path
     */
    public CmsSitemapChangeDelete(String sitePath) {

        m_sitePath = sitePath;
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
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.DELETE;
    }
}