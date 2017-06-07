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

package org.opencms.ui.contextmenu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.I_CmsFilePropertyEditHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;

import org.apache.commons.logging.Log;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.AbstractErrorMessage.ContentMode;
import com.vaadin.server.UserError;
import com.vaadin.ui.TextField;

/**
 * Handles inline editing within the file table.<p>
 */
public class CmsContextMenuEditHandler implements I_CmsFilePropertyEditHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContextMenuEditHandler.class);

    /** The serial version id. */
    private static final long serialVersionUID = -9160838301862765592L;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The edited content structure id. */
    private CmsUUID m_editId;

    /** The edited property. */
    private CmsResourceTableProperty m_editProperty;

    /** The file table. */
    private CmsFileTable m_fileTable;

    /** The lock action record. */
    private CmsLockActionRecord m_lockActionRecord;

    /**
     * Constructor.<p>
     *
     * @param editId the content structure id
     * @param editProperty the property to edit
     * @param fileTable the file table
     * @param context the dialog context
     */
    public CmsContextMenuEditHandler(
        CmsUUID editId,
        CmsResourceTableProperty editProperty,
        CmsFileTable fileTable,
        I_CmsDialogContext context) {
        m_editId = editId;
        m_editProperty = editProperty;
        m_fileTable = fileTable;
        m_context = context;
    }

    /**
     * Cancels the edit process. Unlocks the resource if required.<p>
     *
     * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#cancel()
     */
    public void cancel() {

        if (m_lockActionRecord.getChange() == LockChange.locked) {
            CmsObject cms = A_CmsUI.getCmsObject();
            try {
                CmsResource res = cms.readResource(m_editId);
                cms.unlockResource(res);
            } catch (CmsException e) {
                LOG.warn("Failed to unlock resource " + m_editId.toString(), e);
            }
        }
        CmsAppWorkplaceUi.get().enableGlobalShortcuts();
    }

    /**
     * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#save(java.lang.String)
     */
    public void save(String value) {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            CmsResource res = cms.readResource(m_editId);
            try {
                if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.equals(m_editProperty)) {
                    String sourcePath = cms.getSitePath(res);
                    cms.renameResource(
                        sourcePath,
                        CmsStringUtil.joinPaths(CmsResource.getParentFolder(sourcePath), value));
                } else if (m_editProperty.isEditProperty()) {

                    CmsProperty prop = new CmsProperty(m_editProperty.getEditPropertyId(), value, null);
                    cms.writePropertyObject(cms.getSitePath(res), prop);
                }
            } finally {
                if (m_lockActionRecord.getChange() == LockChange.locked) {
                    CmsResource updatedRes = cms.readResource(res.getStructureId(), CmsResourceFilter.ALL);
                    try {
                        cms.unlockResource(updatedRes);
                    } catch (CmsLockException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
                CmsAppWorkplaceUi.get().enableGlobalShortcuts();
                m_fileTable.clearSelection();
            }
            m_context.finish(Collections.singletonList(m_editId));
        } catch (CmsException e) {
            LOG.error("Exception while saving changed " + m_editProperty + " to resource " + m_editId, e);
            m_context.error(e);
        }

    }

    /**
     * @see org.opencms.ui.components.I_CmsFilePropertyEditHandler#start()
     */
    public void start() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource res = cms.readResource(m_editId);
            m_lockActionRecord = CmsLockUtil.ensureLock(cms, res);
            CmsAppWorkplaceUi.get().disableGlobalShortcuts();
            m_fileTable.startEdit(m_editId, m_editProperty, this);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.debug(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see com.vaadin.event.FieldEvents.TextChangeListener#textChange(com.vaadin.event.FieldEvents.TextChangeEvent)
     */
    public void textChange(TextChangeEvent event) {

        TextField tf = (TextField)event.getSource();
        try {
            validate(event.getText());
            tf.setComponentError(null);
        } catch (InvalidValueException e) {
            tf.setComponentError(new UserError(e.getHtmlMessage(), ContentMode.HTML, null));
        }
    }

    /**
     * @see com.vaadin.data.Validator#validate(java.lang.Object)
     */
    public void validate(Object value) throws InvalidValueException {

        if ((m_editProperty == CmsResourceTableProperty.PROPERTY_RESOURCE_NAME) && (value instanceof String)) {
            try {
                String newName = (String)value;
                CmsResource.checkResourceName(newName);
                CmsObject cms = A_CmsUI.getCmsObject();
                CmsResource res = cms.readResource(m_editId);
                if (!res.getName().equals(newName)) {
                    String sourcePath = cms.getSitePath(res);
                    if (cms.existsResource(CmsStringUtil.joinPaths(CmsResource.getParentFolder(sourcePath), newName))) {
                        throw new InvalidValueException("The selected filename already exists.");
                    }
                }
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            } catch (CmsException e) {
                LOG.warn("Error while validating new filename", e);
                throw new InvalidValueException(e.getLocalizedMessage(A_CmsUI.get().getLocale()));
            }
        }
    }
}
