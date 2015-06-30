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

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing a row of the alias table.<p>
 */
public class CmsAliasTableRow implements IsSerializable {

    /** The alias path error. */
    private String m_aliasError;

    /** The alias path. */
    private String m_aliasPath;

    /** Flag which indicates whether this row is changed. */
    private boolean m_isChanged;

    /** Flag which indicates whether this row is edited. */
    private boolean m_isEdited;

    /** The internal key of the alias table row. */
    private String m_key;

    /** The alias mode. */
    private CmsAliasMode m_mode;

    /** The original structure id of the alias. */
    private CmsUUID m_originalStructureId;

    /** The path error message. */
    private String m_pathError;

    /** The resource path. */
    private String m_resourcePath;

    /** The structure id of the alias target. */
    private CmsUUID m_structureId;

    /**
     * Default constructor.<p>
     */
    public CmsAliasTableRow() {

        // do nothing
    }

    /**
     * Clears validation errors.<p>
     */
    public void clearErrors() {

        if ((getAliasError() != null) || (getPathError() != null)) {
            // ensure rows with errors are updated correctly
            m_isChanged = true;
        }
        m_pathError = null;
        m_aliasError = null;
    }

    /**
     * Copies this object.<p>
     *
     * @return a copy of the alias row
     */
    public CmsAliasTableRow copy() {

        CmsAliasTableRow result = new CmsAliasTableRow();
        result.setKey(m_key);
        result.setResourcePath(m_resourcePath);
        result.setStructureId(m_structureId);
        result.setChanged(m_isChanged);
        result.setMode(m_mode);
        result.setPathError(m_pathError);
        result.setAliasError(m_aliasError);
        result.setAliasPath(m_aliasPath);
        result.setEdited(m_isEdited);
        result.setOriginalStructureId(m_originalStructureId);

        return result;
    }

    /**
     * Changes the alias path.<p>
     *
     * @param newPath the new alias path
     */
    public void editAliasPath(String newPath) {

        m_aliasPath = newPath;
        m_isChanged = true;
    }

    /**
     * Changes the resource path.<p>
     *
     * @param newPath the new resource path
     */
    public void editResourcePath(String newPath) {

        m_resourcePath = newPath;
        m_structureId = null;
        m_isChanged = true;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        return (other != null)
            && (other instanceof CmsAliasTableRow)
            && ((CmsAliasTableRow)other).getKey().equals(getKey());
    }

    /**
     * Gets the alias path  error message.<p>
     *
     * @return the alias path error message
     */
    public String getAliasError() {

        return m_aliasError;
    }

    /**
     * Gets the resource path error message.<p>
     *
     * @return the resource path error message
     */
    public String getAliasPath() {

        return m_aliasPath;
    }

    /**
     * Gets the internal key for the row.<p>
     *
     * This key is artificially generated, it has no significance for the alias itself but is only used
     * during editing to keep track of rows.<p>
     *
     * @return the internal key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Gets the alias mode.<p>
     *
     * @return the alias mode
     */
    public CmsAliasMode getMode() {

        return m_mode;
    }

    /**
     * Gets the original structure id.<p>
     *
     * @return the original structure id
     */
    public CmsUUID getOriginalStructureId() {

        return m_originalStructureId;
    }

    /**
     * Gets the resource path error message.<p>
     *
     * @return the resource path error message
     */
    public String getPathError() {

        return m_pathError;
    }

    /**
     * Gets the resource path.<p>
     *
     * @return the resource path
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * Gets the structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Checks whether any validation errors have been set.<p>
     *
     * @return true if any validation errors have been set
     */
    public boolean hasErrors() {

        return (m_pathError != null) || (m_aliasError != null);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_key.hashCode();
    }

    /**
     * Checks whether this row is changed.<p>
     *
     * @return true if this row is changed
     */
    public boolean isChanged() {

        return m_isChanged;
    }

    /**
     * Checks whether this row is edited.<p>
     *
     * @return true if this row is edited
     */
    public boolean isEdited() {

        return m_isEdited;
    }

    /**
     * Sets the alias error message.<p>
     *
     * @param aliasError the alias error message
     */
    public void setAliasError(String aliasError) {

        m_isChanged = true;
        m_aliasError = aliasError;
    }

    /**
     * Sets the alias path.<p>
     *
     * @param aliasPath the new alias path
     */
    public void setAliasPath(String aliasPath) {

        m_aliasPath = aliasPath;
    }

    /**
     * Sets the 'changed' flag.<p>
     *
     * @param isChanged the new value of the 'changed' flag
     */
    public void setChanged(boolean isChanged) {

        m_isChanged = isChanged;
    }

    /**
     * Sets the 'edited' flag.<p>
     *
     * @param isEdited the new value of the 'edited' flag
     */
    public void setEdited(boolean isEdited) {

        m_isEdited = isEdited;
    }

    /**
     * Sets the internal key.<p>
     *
     * @param key the internal key
     */
    public void setKey(String key) {

        m_key = key;
    }

    /**
     * Sets the alias mode.<p>
     *
     * @param mode the new alias mode
     */
    public void setMode(CmsAliasMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the original structure id.<p>
     *
     * @param originalStructureId the original structure id value
     */
    public void setOriginalStructureId(CmsUUID originalStructureId) {

        m_originalStructureId = originalStructureId;
    }

    /**
     * Sets the resource path error message.<p>
     *
     * @param pathError the resource path error message
     */
    public void setPathError(String pathError) {

        m_isChanged = true;
        m_pathError = pathError;
    }

    /**
     * Sets the resource path.<p>
     *
     * @param resourcePath the resource path
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Updates this bean with data from another instance.<p>
     *
     * @param updateRow the bean which the data should be updated from
     */
    public void update(CmsAliasTableRow updateRow) {

        m_aliasError = updateRow.m_aliasError;
        m_aliasPath = updateRow.m_aliasPath;
        m_mode = updateRow.m_mode;
        m_pathError = updateRow.m_pathError;
        m_resourcePath = updateRow.m_resourcePath;
        m_structureId = updateRow.m_structureId;
        m_isEdited = updateRow.m_isEdited;
        m_originalStructureId = updateRow.m_originalStructureId;
    }

}
