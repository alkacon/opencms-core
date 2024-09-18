/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsDialogContext;
import org.opencms.ui.I_CmsEditPropertyContext;
import org.opencms.ui.contextmenu.CmsContextMenuEditHandler;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * The file table dialog context.<p>
 */
public class CmsFileTableDialogContext extends A_CmsDialogContext implements I_CmsEditPropertyContext {

    /** The file table instance. */
    private CmsFileTable m_fileTable;

    /** The in line editable properties. */
    private Collection<CmsResourceTableProperty> m_editableProperties;

    /**
     * Creates a new instance.<p>
     *
     * @param appId the app id
     * @param contextType the context type
     * @param fileTable the file table instance
     * @param resources the list of selected resources
     */
    public CmsFileTableDialogContext(
        String appId,
        ContextType contextType,
        CmsFileTable fileTable,
        List<CmsResource> resources) {

        super(appId, contextType, resources);
        m_fileTable = fileTable;
    }

    /**
     * @see org.opencms.ui.I_CmsEditPropertyContext#editProperty(java.lang.Object)
     */
    public void editProperty(Object propertyId) {

        new CmsContextMenuEditHandler(
            getResources().get(0).getStructureId(),
            (CmsResourceTableProperty)propertyId,
            m_fileTable,
            this).start();
    }

    /**
     * @see org.opencms.ui.A_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
     */
    @Override
    public void finish(CmsProject project, String siteRoot) {

        super.finish(null);
        super.reload();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
     */
    @Override
    public void finish(Collection<CmsUUID> ids) {

        super.finish(ids);
        m_fileTable.clearSelection();
        if (ids != null) {
            if (ids.stream().anyMatch(CmsUUID::isNullUUID)) {
                m_fileTable.update(m_fileTable.getAllIds(), false);
            } else {
                ids = ids.stream().filter(id -> m_fileTable.containsId(id)).collect(Collectors.toList());
                m_fileTable.update(ids, false);
            }
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
     */
    public void focus(CmsUUID cmsUUID) {

        // nothing to do
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getAllStructureIdsInView()
     */
    public List<CmsUUID> getAllStructureIdsInView() {

        return Lists.newArrayList(m_fileTable.getAllIds());
    }

    /**
     * @see org.opencms.ui.I_CmsEditPropertyContext#isPropertyEditable(java.lang.Object)
     */
    public boolean isPropertyEditable(Object propertyId) {

        return (getResources().size() == 1)
            && (m_editableProperties != null)
            && m_editableProperties.contains(propertyId)
            && m_fileTable.isColumnVisible((CmsResourceTableProperty)propertyId);
    }

    /**
     * Sets the in line editable properties.<p>
     *
     * @param editableProperties the in line editable properties
     */
    public void setEditableProperties(Collection<CmsResourceTableProperty> editableProperties) {

        m_editableProperties = editableProperties;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#updateUserInfo()
     */
    public void updateUserInfo() {

        // not supported
    }
}
