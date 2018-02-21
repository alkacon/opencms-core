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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.report.CmsReportWidget;
import org.opencms.workplace.Messages;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Dialog to delete property definitions.<p>
 */
public class CmsPropertyDeleteDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3397853426667893254L;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsPropertyDeleteDialog.class.getName());

    /**List of resources having the property to be deleted.*/
    protected List<CmsResource> m_resources;

    /**Vaadin component.*/
    private VerticalLayout m_forceDeleteLayout;

    /**Vaadin component.*/
    protected VerticalLayout m_start;

    /**Vaadin component.*/
    private Label m_icon;

    /**Vaadin component.*/
    protected Button m_cancelButton;

    /**Vaadin component.*/
    protected Button m_okButton;

    /**Vaadin component.*/
    protected CheckBox m_forceDelete;

    /**Vaadin component.*/
    protected FormLayout m_threadReport;

    /**Vaadin component.*/
    protected Panel m_report;

    /**CmsObject.*/
    private CmsObject m_cms;

    /**
     * public constructor.<p>
     *
     * @param propName name of property
     * @param window to be closed
     * @param runnable to update table
     */
    public CmsPropertyDeleteDialog(final String propName, final Window window, final Runnable runnable) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        displayResourceInfoDirectly(
            Collections.singletonList(new CmsResourceInfo(propName, "", new CmsCssIcon(OpenCmsTheme.ICON_DATABASE))));

        //Setup icon
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        m_report.setVisible(false);

        try {
            m_resources = getCms().readResourcesWithProperty(propName);
            m_forceDeleteLayout.setVisible(!m_resources.isEmpty());
            m_okButton.setEnabled(m_resources.isEmpty());
        } catch (CmsException e) {
            //
        }
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 4788359189538313935L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }
        });

        m_forceDelete.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 7482690600008082762L;

            public void valueChange(ValueChangeEvent event) {

                m_okButton.setEnabled(m_forceDelete.getValue().booleanValue());

            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7861406021237202016L;

            public void buttonClick(ClickEvent event) {

                if (!m_resources.isEmpty()) {
                    m_start.setVisible(false);
                    m_report.setVisible(true);
                    m_okButton.setEnabled(false);

                    CmsRemovePropertyFromResourcesThread thread = new CmsRemovePropertyFromResourcesThread(
                        getCms(),
                        propName);
                    m_threadReport.addComponent(new CmsReportWidget(thread));

                    thread.start();
                    m_cancelButton.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_DIALOG_BUTTON_CLOSE_0));
                    m_cancelButton.addClickListener(new ClickListener() {

                        public void buttonClick(ClickEvent event) {

                            runnable.run();

                        }

                    });
                    return;
                }
                try {
                    getCms().deletePropertyDefinition(propName);
                    runnable.run();
                } catch (CmsException e) {
                    LOG.error("Unable to delete property definition", e);
                }
                window.close();
            }

        });
    }

    /**
     * Gets a copy of the cms object set to root site.<p>
     *
     * @return CmsObject
     */
    protected CmsObject getCms() {

        if (m_cms == null) {
            try {
                m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                m_cms.getRequestContext().setSiteRoot("");
            } catch (CmsException e) {
                return null;
            }
        }
        return m_cms;
    }
}
