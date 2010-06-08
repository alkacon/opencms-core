/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeNewSubSitemapEntry.java,v $
 * Date   : $Date: 2010/06/08 14:42:16 $
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

import java.util.Map;

/**
 * Sitemap change class for adding new entries to a sub-sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeNewSubSitemapEntry extends CmsSitemapChangeNew {

    /** serial version id. */
    private static final long serialVersionUID = -6543250703279289164L;

    /** The entry point of the sitemap into which the new entry should be inserted. */
    private String m_entryPoint;

    /**
     * Constructor.<p>
     * 
     * @param sitePath the entry's site path
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param properties the entry's properties
     * @param entryPoint the entry point of the sitemap in which the entry should be inserted 
     */
    public CmsSitemapChangeNewSubSitemapEntry(
        String sitePath,
        int position,
        String title,
        String vfsPath,
        Map<String, String> properties,
        String entryPoint) {

        super(sitePath, position, title, vfsPath, properties);
        m_entryPoint = entryPoint;
    }

    /**
     * Returns the entry point for the sitemap into which the entry should be inserted.<p>
     * 
     * @return the entry point 
     */
    public String getEntryPoint() {

        return m_entryPoint;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsSitemapChange#getType()
     */
    @Override
    public Type getType() {

        return Type.SUBSITEMAP_NEW;
    }

}
