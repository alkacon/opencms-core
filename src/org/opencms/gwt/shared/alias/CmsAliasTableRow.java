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

public class CmsAliasTableRow implements IsSerializable {

    private String m_aliasError;

    private String m_aliasPath;

    private boolean m_isChanged;

    private boolean m_isEdited;

    private String m_key;

    private CmsAliasMode m_mode;

    private CmsUUID m_originalStructureId;

    private String m_pathError;

    private String m_resourcePath;

    private boolean m_selected;

    private CmsUUID m_structureId;

    public CmsAliasTableRow() {

    }

    public void clearErrors() {

        if ((getAliasError() != null) || (getPathError() != null)) {
            // ensure rows with errors are updated correctly 
            m_isChanged = true;
        }
        m_pathError = null;
        m_aliasError = null;
    }

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
        result.setSelected(m_selected);
        result.setEdited(m_isEdited);
        result.setOriginalStructureId(m_originalStructureId);

        return result;
    }

    public void editAliasPath(String newPath) {

        m_aliasPath = newPath;
        m_isChanged = true;
    }

    public void editResourcePath(String newPath) {

        m_resourcePath = newPath;
        m_structureId = null;
        m_isChanged = true;
    }

    @Override
    public boolean equals(Object other) {

        return (other != null)
            && (other instanceof CmsAliasTableRow)
            && ((CmsAliasTableRow)other).getKey().equals(getKey());
    }

    public String getAliasError() {

        return m_aliasError;
    }

    public String getAliasPath() {

        return m_aliasPath;
    }

    public String getKey() {

        return m_key;
    }

    public CmsAliasMode getMode() {

        return m_mode;
    }

    public CmsUUID getOriginalStructureId() {

        return m_originalStructureId;
    }

    public String getPathError() {

        return m_pathError;
    }

    public String getResourcePath() {

        return m_resourcePath;
    }

    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Checks whether any validation errors have been set.<p>
     * @return
     */
    public boolean hasErrors() {

        return (m_pathError != null) || (m_aliasError != null);
    }

    @Override
    public int hashCode() {

        return m_key.hashCode();
    }

    public boolean isChanged() {

        return m_isChanged;
    }

    public boolean isEdited() {

        return m_isEdited;
    }

    public boolean isSelected() {

        return m_selected;
    }

    public void setAliasError(String aliasError) {

        m_isChanged = true;
        m_aliasError = aliasError;
    }

    public void setAliasPath(String aliasPath) {

        m_aliasPath = aliasPath;
    }

    public void setChanged(boolean isChanged) {

        m_isChanged = isChanged;
    }

    public void setEdited(boolean isEdited) {

        m_isEdited = isEdited;
    }

    public void setKey(String key) {

        m_key = key;
    }

    public void setMode(CmsAliasMode mode) {

        m_mode = mode;
    }

    public void setOriginalStructureId(CmsUUID originalStructureId) {

        m_originalStructureId = originalStructureId;
    }

    public void setPathError(String pathError) {

        m_isChanged = true;
        m_pathError = pathError;
    }

    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    public void setSelected(boolean selected) {

        m_selected = selected;
    }

    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    public void update(CmsAliasTableRow updateRow) {

        m_aliasError = updateRow.m_aliasError;
        m_aliasPath = updateRow.m_aliasPath;
        m_mode = updateRow.m_mode;
        m_pathError = updateRow.m_pathError;
        m_resourcePath = updateRow.m_resourcePath;
        m_structureId = updateRow.m_structureId;
        m_selected = updateRow.m_selected;
        m_isEdited = updateRow.m_isEdited;
        m_originalStructureId = updateRow.m_originalStructureId;
    }

}
