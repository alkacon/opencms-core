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

package org.opencms.ui.apps.sitemanager;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog for deleting Sites.<p>
 */
public class CmsDeleteSiteDialog extends CmsBasicDialog {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsDeleteSiteDialog.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4861877088383896218L;

    /** The site manager instance.*/
    protected CmsSiteManager m_manager;

    /**cancel button.*/
    private Button m_cancelButton;

    /**check box: should resources be deleted?*/
    private CheckBox m_deleteResources;

    /**ok button.*/
    private Button m_okButton;

    /**sites to delete.*/
    protected final List<CmsSite> m_sitesToDelete = new ArrayList<CmsSite>();

    /**
     * Public constructor.<p>
     *
     * @param manager the site manager instance
     * @param data with values for siteroots to delete.
     */
    public CmsDeleteSiteDialog(CmsSiteManager manager, Set<String> data) {

        m_manager = manager;

        for (String site : data) {
            m_sitesToDelete.add(manager.getElement(site));
        }

        displayResourceInfoDirectly(getResourceInfos());

        setContent(getContent());
        m_okButton = new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        m_cancelButton = new Button(
            CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
        addButton(m_okButton);
        addButton(m_cancelButton);
        //Set Clicklistener
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -5769891739879269176L;

            public void buttonClick(ClickEvent event) {

                m_manager.closeDialogWindow(false);
            }
        });
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6932464669055039855L;

            public void buttonClick(ClickEvent event) {

                submit();
                m_manager.closeDialogWindow(true);
            }
        });
    }

    /**
     * Creates content of dialog containing CheckBox if resources should be deleted and a messages.<p>
     *
     * @return vertical layout component.
     */
    protected VerticalLayout getContent() {

        String message;

        if (m_sitesToDelete.size() == 1) {
            message = CmsVaadinUtils.getMessageText(
                Messages.GUI_SITE_CONFIRM_DELETE_SITE_1,
                CmsEncoder.escapeXml(m_sitesToDelete.get(0).getTitle()));
        } else {
            message = "";
            for (CmsSite site : m_sitesToDelete) {
                if (message.length() > 0) {
                    message += ", ";
                }
                message += CmsEncoder.escapeXml(site.getTitle());
            }
            message = CmsVaadinUtils.getMessageText(Messages.GUI_SITE_CONFIRM_DELETE_SITES_1, message);
        }

        VerticalLayout layout = new VerticalLayout();

        m_deleteResources = new CheckBox();
        m_deleteResources.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_DELETE_RESOURCES_0));
        m_deleteResources.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_DELETE_RESOURCES_HELP_0));

        layout.addComponent(m_deleteResources);

        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(message);

        layout.addComponent(label);
        return layout;
    }

    /**
     * delete sites.<p>
     */
    protected void submit() {

        List<String> siteRootsToDelete = new ArrayList<String>();
        for (CmsSite site : m_sitesToDelete) {

            String currentSite = A_CmsUI.getCmsObject().getRequestContext().getSiteRoot();
            if (currentSite.equals(site.getSiteRoot())) {
                A_CmsUI.getCmsObject().getRequestContext().setSiteRoot("");
            }
            siteRootsToDelete.add(site.getSiteRoot());
        }
        m_manager.deleteElements(siteRootsToDelete);
        if (m_deleteResources.getValue().booleanValue()) {
            for (CmsSite site : m_sitesToDelete) {
                try {
                    m_manager.getRootCmsObject().lockResource(site.getSiteRoot());
                } catch (CmsException e) {
                    LOG.error("unable to lock resource");
                }
                try {
                    m_manager.getRootCmsObject().deleteResource(
                        site.getSiteRoot(),
                        CmsResource.DELETE_PRESERVE_SIBLINGS);
                    try {
                        m_manager.getRootCmsObject().unlockResource(site.getSiteRoot());
                    } catch (CmsLockException e) {
                        LOG.info("Unlock failed.", e);
                    }
                } catch (CmsException e) {
                    //ok, resource was not published and can not be unlocked anymore..
                }
            }
        }
    }

    /**
     * Returns a list of CmsResourceInfo objects.<p>
     *
     * @return list of cmsresourceinfo.
     */
    private List<CmsResourceInfo> getResourceInfos() {

        List<CmsResourceInfo> infos = new ArrayList<CmsResourceInfo>();
        for (CmsSite site : m_sitesToDelete) {
            infos.add(
                new CmsResourceInfo(site.getTitle(), site.getSiteRoot(), m_manager.getFavIcon(site.getSiteRoot())));
        }
        return infos;
    }
}
