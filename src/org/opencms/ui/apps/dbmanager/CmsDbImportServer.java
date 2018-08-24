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

package org.opencms.ui.apps.dbmanager;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.workplace.threads.CmsDatabaseImportThread;

import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;

/**
 * Class for the import from server option.<p>
 */
public class CmsDbImportServer extends A_CmsServerImportForm {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = -1489144623755018754L;

    /**vaadin component.*/
    private CheckBox m_keepPermissions;

    /**vaadin component.*/
    private ComboBox m_moduleSelect;

    /**vaadin component.*/
    private Button m_ok;

    /**vaadin component.*/
    private ComboBox m_siteSelect;

    /**vaadin component.*/
    private ComboBox m_projectSelect;

    /**
     * Public constructor.<p>
     *
     * @param app which calls the form
     */
    public CmsDbImportServer(I_CmsReportApp app) {

        super(app, "packages", false);

    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsServerImportForm#getImportSelect()
     */
    @Override
    public ComboBox getImportSelect() {

        return m_moduleSelect;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getThread()
     */
    @Override
    public A_CmsReportThread getThread() {

        return new CmsDatabaseImportThread(
            getCmsObject(),
            m_importFile.getPath(),
            m_keepPermissions.getValue().booleanValue());
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getCancelButton()
     */
    @Override
    protected Button getCancelButton() {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getOkButton()
     */
    @Override
    protected Button getOkButton() {

        return m_ok;
    }

    @Override
    protected ComboBox getProjectSelector() {

        return m_projectSelect;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getReportPath()
     */
    @Override
    protected String getReportPath() {

        return CmsDbImportApp.PATH_REPORT_SERVER;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getSiteSelector()
     */
    @Override
    protected ComboBox getSiteSelector() {

        return m_siteSelect;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.A_CmsImportForm#getTitle()
     */
    @Override
    protected String getTitle() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTSERVER_ADMIN_TOOL_NAME_0);
    }
}
