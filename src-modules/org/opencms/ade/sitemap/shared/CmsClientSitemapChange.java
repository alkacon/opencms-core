/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsClientSitemapChange.java,v $
 * Date   : $Date: 2010/04/06 12:22:32 $
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

package org.opencms.ade.sitemap.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores one change of a sitemap entry.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChange implements IsSerializable {

    /**
     * Sitemap entry change type.<p>
     */
    public static enum ChangeType {

        /** Delete entry. */
        DELETE,

        /** Name, VFS, Properties change. */
        EDIT,

        /** Path change. */
        MOVE,

        /** New entry. */
        NEW;
    }

    /** After change entry. */
    private CmsClientSitemapEntry m_new;

    /** Before change entry. */
    private CmsClientSitemapEntry m_old;

    /** Change type. */
    private ChangeType m_type;

    /**
     * Constructor.<p>
     * 
     * @param oldEntry the old sitemap entry, can be <code>null</code> for new entries
     * @param newEntry the new sitemap entry, can be <code>null</code> for deletion
     * @param changeType the change type
     */
    public CmsClientSitemapChange(CmsClientSitemapEntry oldEntry, CmsClientSitemapEntry newEntry, ChangeType changeType) {

        m_old = oldEntry;
        m_new = newEntry;
        m_type = changeType;
    }

    /**
     * Constructor for serialization.<p>
     */
    protected CmsClientSitemapChange() {

        // for serialization
    }

    /**
     * Returns the new sitemap entry.<p>
     *
     * @return the new sitemap entry
     */
    public CmsClientSitemapEntry getNew() {

        return m_new;
    }

    /**
     * Returns the old sitemap entry.<p>
     *
     * @return the old sitemap entry
     */
    public CmsClientSitemapEntry getOld() {

        return m_old;
    }

    /**
     * Returns the change type.<p>
     *
     * @return the change type
     */
    public ChangeType getType() {

        return m_type;
    }
}