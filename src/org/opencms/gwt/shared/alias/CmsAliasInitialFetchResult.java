/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared.alias;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The class used to transmit the original alias list when the alias editor dialog is first loaded.<p>
 */
public class CmsAliasInitialFetchResult implements IsSerializable {

    /** The list of aliases. */
    private List<CmsAliasTableRow> m_aliasRows;

    /** The alias download URL. */
    private String m_downloadUrl;

    /**
     * Gets the alias download URL.<p>
     * 
     * @return the alias download URL 
     */
    public String getDownloadUrl() {

        return m_downloadUrl;
    }

    /**
     * Gets the alias table rows.<p>
     * 
     * @return the alias table rows 
     */
    public List<CmsAliasTableRow> getRows() {

        return m_aliasRows;
    }

    /**
     * Sets the download URL for aliases.<p>
     * 
     * @param downloadUrl the download URL for aliases 
     */
    public void setDownloadUrl(String downloadUrl) {

        m_downloadUrl = downloadUrl;

    }

    /**
     * Sets the alias table rows.<p>
     * 
     * @param rows the alias table rows 
     */
    public void setRows(List<CmsAliasTableRow> rows) {

        m_aliasRows = rows;
    }

}
