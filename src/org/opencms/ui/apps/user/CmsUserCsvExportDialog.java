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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsUser;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.Label;

/**
 * Only export dialog.<p>
 */
public class CmsUserCsvExportDialog extends A_CmsImportExportUserDialog {

    /**List of all user to export. */
    private List<CmsUser> m_user;

    /**vaadin component. */
    private Button m_cancel;

    /**vaadin component. */
    private Button m_download;

    /**vaadin component. */
    private Label m_elementToExportOU;

    /**vaadin component. */
    private Label m_elementToExportGroup;

    /**vaadin component. */
    private Label m_elementToExportRole;

    /**Include technical fields flag. */
    private CheckBox m_includeTechnicalFields;

    /**vaadin component. */
    private Label m_elementExtendedDataUser;

    /**vaadin component. */
    private Label m_elementExtendedDataRole;

    /**vaadin component. */
    private Label m_elementToExportCount;

    /**
     * constructor.<p>
     *
     * @param userToExport user list
     * @param ou ouname
     * @param type state type
     * @param elementName name of element
     * @param extendedData extended Data
     * @param window window
     */
    CmsUserCsvExportDialog(
        List<CmsUser> userToExport,
        String ou,
        I_CmsOuTreeType type,
        String elementName,
        boolean extendedData,
        Window window,
        boolean includeTechnicalFields) {

        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_EXPORT_0));

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_includeTechnicalFields.addValueChangeListener(new ValueChangeListener<Boolean>() {

            public void valueChange(ValueChangeEvent event) {

                initDownloadButton();

            }

        });
        m_includeTechnicalFields.setVisible(includeTechnicalFields);
        m_user = userToExport;
        super.init(ou, window);
        m_elementToExportOU.setVisible(type.isUser());
        m_elementToExportGroup.setVisible(type.isGroup());
        m_elementToExportRole.setVisible(type.isRole());

        m_elementExtendedDataUser.setVisible(extendedData && type.isUser());
        m_elementExtendedDataRole.setVisible(extendedData && type.isRole());

        m_elementToExportOU.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EXPORT_OU_1, elementName));
        m_elementToExportGroup.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EXPORT_GROUP_1, elementName));
        m_elementToExportRole.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EXPORT_ROLE_1, elementName));

        m_elementToExportCount.setValue(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EXPORT_COUNT_1, userToExport.size()));

    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getCloseButton()
     */
    @Override
    Button getCloseButton() {

        return m_cancel;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getDownloadButton()
     */
    @Override
    Button getDownloadButton() {

        return m_download;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getUserToExport()
     */
    @Override
    Map<CmsUUID, CmsUser> getUserToExport() {

        Map<CmsUUID, CmsUser> userMap = new HashMap<CmsUUID, CmsUser>();
        for (CmsUser user : m_user) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#isExportWithTechnicalFields()
     */
    @Override
    boolean isExportWithTechnicalFields() {

        return m_includeTechnicalFields.getValue().booleanValue();
    }

}
