/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapSaveData.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
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

import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.I_CmsSitemapChange;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data that is saved when the user clicks "save" in the sitemap editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapSaveData implements IsSerializable {

    /** The list of changes to the sitemap itself. */
    private List<I_CmsSitemapChange> m_changes;

    /** The clipboard changes. */
    private CmsSitemapClipboardData m_clipboardData;

    /** The detail page information. */
    private List<CmsDetailPageInfo> m_detailPageInfo;

    /**
     * Instantiates a new sitemap save data object.
     *
     * @param changes the changes
     * @param clipboardData the clipboard data
     * @param detailPageInfo the detail page info
     */
    public CmsSitemapSaveData(
        List<I_CmsSitemapChange> changes,
        CmsSitemapClipboardData clipboardData,
        List<CmsDetailPageInfo> detailPageInfo) {

        m_changes = changes;
        m_clipboardData = clipboardData;
        m_detailPageInfo = detailPageInfo;
    }

    /** 
     * Empty constructor for serialization. 
     **/
    protected CmsSitemapSaveData() {

        // do nothing 
    }

    /**
     * Gets the changes.
     *
     * @return the changes
     */
    public List<I_CmsSitemapChange> getChanges() {

        return m_changes;
    }

    /**
     * Gets the clipboard data.
     *
     * @return the clipboard data
     */
    public CmsSitemapClipboardData getClipboardData() {

        return m_clipboardData;
    }

    /**
     * Gets the detail page info.
     *
     * @return the detail page info
     */
    public List<CmsDetailPageInfo> getDetailPageInfo() {

        return m_detailPageInfo;
    }

    /**
     * Sets the changes.
     *
     * @param changes the new changes
     */
    public void setChanges(List<I_CmsSitemapChange> changes) {

        m_changes = changes;
    }

    /**
     * Sets the clipboard data.
     *
     * @param data the new clipboard data
     */
    public void setClipboardData(CmsSitemapClipboardData data) {

        m_clipboardData = data;
    }

    /**
     * Sets the detail page info.
     *
     * @param detailPageInfo the new detail page info
     */
    public void setDetailPageInfo(List<CmsDetailPageInfo> detailPageInfo) {

        m_detailPageInfo = detailPageInfo;
    }

}
