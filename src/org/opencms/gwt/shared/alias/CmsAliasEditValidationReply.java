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
 * The class used to transfer validation results from the server to the client.<p>
 */
public class CmsAliasEditValidationReply implements IsSerializable {

    /** The rows which need to be changed. */
    private List<CmsAliasTableRow> m_changedRows;

    /** The error for the new entry alias path text box. */
    private String m_newEntryAliasError;

    /** The error for the new entry resource path text box. */
    private String m_newEntryPathError;

    /** The validated new entry. */
    private CmsAliasTableRow m_validatedNewEntry;

    /**
     * Default constructor.<p>
     */
    public CmsAliasEditValidationReply() {

    }

    /**
     * Gets the changed rows.<p>
     *
     * @return the changed row list
     */
    public List<CmsAliasTableRow> getChangedRows() {

        return m_changedRows;
    }

    /**
     * Gets the error message for the new entry alias path text box.<p>
     *
     * @return an error message
     */
    public String getNewEntryAliasError() {

        return m_newEntryAliasError;
    }

    /**
     * Gets the error message for the new entry resource path text box.<p>
     *
     * @return an error message
     */
    public String getNewEntryPathError() {

        return m_newEntryPathError;
    }

    /**
     * Gets the validated new entry.<p>
     *
     * @return the validated new entry
     */
    public CmsAliasTableRow getValidatedNewEntry() {

        return m_validatedNewEntry;
    }

    /**
     * Check if this validation result has any errors.<p>
     *
     * @return true if the validation result has errors
     */
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

    /**
     * Sets the changed rows.<p>
     *
     * @param changedRows the changed rows
     */
    public void setChangedRows(List<CmsAliasTableRow> changedRows) {

        m_changedRows = changedRows;
    }

    /**
     * Sets the error message for the new entry alias path text box.<p>
     *
     * @param newEntryAliasError the error message
     */
    public void setNewEntryAliasError(String newEntryAliasError) {

        m_newEntryAliasError = newEntryAliasError;
    }

    /**
     * Sets the error message for the new entry resource path text box.<p>
     *
     * @param newEntryPathError the error message
     */
    public void setNewEntryPathError(String newEntryPathError) {

        m_newEntryPathError = newEntryPathError;
    }

    /**
     * Sets the validated new entry.<p>
     *
     * @param validatedNewEntry the validated new entry
     */
    public void setValidatedNewEntry(CmsAliasTableRow validatedNewEntry) {

        m_validatedNewEntry = validatedNewEntry;
    }

}
