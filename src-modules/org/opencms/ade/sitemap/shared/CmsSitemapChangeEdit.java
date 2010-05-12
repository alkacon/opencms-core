/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapChangeEdit.java,v $
 * Date   : $Date: 2010/05/12 10:14:06 $
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


/**
 * Stores one edition change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapChangeEdit implements I_CmsSitemapChange {

    /** The new entry without children. */
    private CmsClientSitemapEntry m_newEntry;

    /** The old entry without children. */
    private CmsClientSitemapEntry m_oldEntry;

    /**
     * Constructor.<p>
     * 
     * @param oldEntry the old entry
     * @param newEntry the new entry
     */
    public CmsSitemapChangeEdit(CmsClientSitemapEntry oldEntry, CmsClientSitemapEntry newEntry) {

        m_oldEntry = oldEntry.cloneEntry();
        m_newEntry = newEntry;
    }

    /**
     * Returns the new entry.<p>
     *
     * @return the new entry
     */
    public CmsClientSitemapEntry getNewEntry() {

        return m_newEntry;
    }

    /**
     * Returns the old entry.<p>
     *
     * @return the old entry
     */
    public CmsClientSitemapEntry getOldEntry() {

        return m_oldEntry;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapChange#getType()
     */
    public Type getType() {

        return Type.EDIT;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapChange#revert()
     */
    public I_CmsSitemapChange revert() {

        return new CmsSitemapChangeEdit(getNewEntry(), getOldEntry());
    }
}