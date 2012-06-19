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

public class CmsAliasEditValidationReply implements IsSerializable {

    private List<CmsAliasTableRow> m_changedRows;

    private String m_newEntryAliasError;

    private String m_newEntryPathError;

    private CmsAliasTableRow m_validatedNewEntry;

    public CmsAliasEditValidationReply() {

    }

    public List<CmsAliasTableRow> getChangedRows() {

        return m_changedRows;
    }

    public String getNewEntryAliasError() {

        return m_newEntryAliasError;
    }

    public String getNewEntryPathError() {

        return m_newEntryPathError;
    }

    public CmsAliasTableRow getValidatedNewEntry() {

        return m_validatedNewEntry;
    }

    public boolean hasErrors() {

        if ((m_validatedNewEntry != null) && m_validatedNewEntry.hasErrors()) {
            return true;
        }
        for (CmsAliasTableRow row : m_changedRows) {
            if (row.hasErrors()) {
                return true;
            }
        }
        return false;
    }

    public void setChangedRows(List<CmsAliasTableRow> changedRows) {

        m_changedRows = changedRows;
    }

    public void setNewEntryAliasError(String newEntryAliasError) {

        m_newEntryAliasError = newEntryAliasError;
    }

    public void setNewEntryPathError(String newEntryPathError) {

        m_newEntryPathError = newEntryPathError;
    }

    public void setValidatedNewEntry(CmsAliasTableRow validatedNewEntry) {

        m_validatedNewEntry = validatedNewEntry;
    }

}
