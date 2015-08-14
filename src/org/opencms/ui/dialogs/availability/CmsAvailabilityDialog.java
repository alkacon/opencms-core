/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.dialogs.availability;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Date;
import java.util.List;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.VerticalLayout;

public class CmsAvailabilityDialog extends CmsBasicDialog {

    private Button m_cancelButton;
    private I_CmsDialogContext m_dialogContext;

    private DateField m_expiredField;
    private Button m_okButton;
    private DateField m_releasedField;
    private CheckBox m_resetExpired;

    private CheckBox m_resetReleased;

    private CheckBox m_subresourceModificationField;

    public CmsAvailabilityDialog(I_CmsDialogContext dialogContext) {

        super();
        m_dialogContext = dialogContext;
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);

        List<CmsResource> resources = dialogContext.getResources();
        if (resources.size() == 1) {
            CmsResource onlyResource = resources.get(0);
            if (onlyResource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT) {
                m_releasedField.setValue(new Date(onlyResource.getDateReleased()));
            }
            if (onlyResource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT) {
                m_expiredField.setValue(new Date(onlyResource.getDateExpired()));
            }
        }
        boolean hasFolders = false;
        for (CmsResource resource : resources) {
            if (resource.isFolder()) {
                hasFolders = true;
            }
        }
        m_subresourceModificationField.setVisible(hasFolders);
        initResetCheckbox(m_resetReleased, m_releasedField);
        initResetCheckbox(m_resetExpired, m_expiredField);

        AbstractComponentContainer cont = new VerticalLayout();

        m_okButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                if (validate()) {
                    try {

                        changeAvailability();
                        m_dialogContext.finish(null);
                    } catch (Throwable t) {
                        m_dialogContext.error(t);
                    }
                }
            }

        });

        m_cancelButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_dialogContext.finish(null);
            }
        });
        displayResourceInfo(m_dialogContext.getResources());
    }

    protected void changeAvailability() throws CmsException {

        Date released = m_releasedField.getValue();
        Date expired = m_expiredField.getValue();
        boolean resetReleased = m_resetReleased.getValue().booleanValue();
        boolean resetExpired = m_resetExpired.getValue().booleanValue();
        boolean modifySubresources = m_subresourceModificationField.getValue().booleanValue();
        for (CmsResource resource : m_dialogContext.getResources()) {
            changeAvailability(resource, released, resetReleased, expired, resetExpired, modifySubresources);
        }

    }

    private void changeAvailability(
        CmsResource resource,
        Date released,
        boolean resetReleased,
        Date expired,
        boolean resetExpired,
        boolean modifySubresources) throws CmsException {

        CmsObject cms = m_dialogContext.getCms();
        CmsLockActionRecord lockActionRecord = CmsLockUtil.ensureLock(cms, resource);
        try {
            long newDateReleased;
            if (resetReleased || (released != null)) {
                newDateReleased = released != null ? released.getTime() : CmsResource.DATE_RELEASED_DEFAULT;
                cms.setDateReleased(resource, newDateReleased, modifySubresources);
            }
            long newDateExpired;
            if (resetExpired || (expired != null)) {
                newDateExpired = expired != null ? expired.getTime() : CmsResource.DATE_EXPIRED_DEFAULT;
                cms.setDateExpired(resource, newDateExpired, modifySubresources);
            }
        } finally {
            if (lockActionRecord.getChange() == LockChange.locked) {
                cms.unlockResource(resource);
            }

        }
    }

    private void initResetCheckbox(CheckBox box, final DateField field) {

        box.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Boolean value = (Boolean)(event.getProperty().getValue());
                if (value.booleanValue()) {
                    field.clear();
                    field.setEnabled(false);
                } else {
                    field.setEnabled(true);
                }
            }
        });
    }

    private boolean validate() {

        return m_releasedField.isValid() && m_expiredField.isValid();
    }
}
