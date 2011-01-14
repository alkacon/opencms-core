/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapChangeNewSubSitemapEntry.java,v $
 * Date   : $Date: 2011/01/14 11:59:10 $
 * Version: $Revision: 1.5 $
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
 * Sitemap change class for adding new entries to a sub-sitemap.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.5 $
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
     * @param structureId the id of the new sub-sitemap entry 
     * @param sitePath the entry's site path
     * @param position the entry's position
     * @param title the entry's title
     * @param vfsPath the entry's VFS path
     * @param properties the entry's properties
     * @param entryPoint the entry point of the sitemap in which the entry should be inserted
     */
    public CmsSitemapChangeNewSubSitemapEntry(
        CmsUUID structureId,
        String sitePath,
        int position,
        String title,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> properties,
        String entryPoint) {

        super(structureId, sitePath, position, title, vfsPath, properties);
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
