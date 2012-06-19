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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CmsAliasEditValidationRequest implements IsSerializable {

    private List<CmsAliasTableRow> m_editedData;

    private CmsAliasTableRow m_newEntry;

    private List<CmsAliasTableRow> m_originalData;

    public CmsAliasEditValidationRequest() {

    }

    public CmsAliasEditValidationRequest(
        List<CmsAliasTableRow> originalData,
        List<CmsAliasTableRow> editedData,
        CmsAliasTableRow newEntry) {

        m_originalData = originalData;
        m_editedData = editedData;
        m_newEntry = newEntry;
    }

    public List<CmsAliasTableRow> getEditedData() {

        return m_editedData;
    }

    public CmsAliasTableRow getNewEntry() {

        return m_newEntry;
    }

    public List<CmsAliasTableRow> getOriginalData() {

        return m_originalData;
    }

    public void setEditedData(List<CmsAliasTableRow> data) {

        m_editedData = new ArrayList<CmsAliasTableRow>();
        m_editedData.addAll(data);
    }

    public void setNewEntry(CmsAliasTableRow newEntry) {

        m_newEntry = newEntry;
    }

    public void setOriginalData(List<CmsAliasTableRow> originalData) {

        m_originalData = originalData;
    }

}
