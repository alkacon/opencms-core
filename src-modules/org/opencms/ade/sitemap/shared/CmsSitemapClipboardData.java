/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapClipboardData.java,v $
 * Date   : $Date: 2011/01/21 11:09:42 $
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap clipboard data bean.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0
 */
public class CmsSitemapClipboardData implements IsSerializable {

    /** The session stored list of deleted sitemap entries. */
    private List<CmsClientSitemapEntry> m_deletions;

    /** The session stored list of modified sitemap entry paths. */
    private List<CmsClientSitemapEntry> m_modifications;

    /**
     * Constructor.<p>
     */
    public CmsSitemapClipboardData() {

        m_deletions = new ArrayList<CmsClientSitemapEntry>();
        m_modifications = new ArrayList<CmsClientSitemapEntry>();
    }

    /**
     * Constructor.<p>
     * 
     * @param deletions the session stored list of deleted sitemap entries
     * @param modifications the session stored list of modified sitemap entry paths
     */
    public CmsSitemapClipboardData(List<CmsClientSitemapEntry> deletions, List<CmsClientSitemapEntry> modifications) {

        m_deletions = deletions;
        m_modifications = modifications;
    }

    /**
     * Provides a copy of the clip-board data.<p>
     * 
     * @return the copied data 
     */
    public CmsSitemapClipboardData copy() {

        List<CmsClientSitemapEntry> deletions = new ArrayList<CmsClientSitemapEntry>();
        deletions.addAll(m_deletions);
        List<CmsClientSitemapEntry> modifications = new ArrayList<CmsClientSitemapEntry>();
        modifications.addAll(m_modifications);
        return new CmsSitemapClipboardData(deletions, modifications);
    }

    /**
     * Returns the session stored list of deleted sitemap entries.<p>
     *
     * @return the session stored list of deleted sitemap entries
     */
    public List<CmsClientSitemapEntry> getDeletions() {

        return m_deletions;
    }

    /**
     * Returns the session stored list of modified sitemap entry paths.<p>
     *
     * @return the session stored list of modified sitemap entry paths
     */
    public List<CmsClientSitemapEntry> getModifications() {

        return m_modifications;
    }
}
