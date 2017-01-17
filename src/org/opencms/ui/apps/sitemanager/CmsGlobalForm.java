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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

/**
 *Class for the Global configuration dialog.<p>
 *
 */

public class CmsGlobalForm extends VerticalLayout {

    /**generated id.*/
    private static final long serialVersionUID = -3553152729226102382L;

    /**Site manager instance. */
    private CmsSiteManager m_manager;

    /** Forbidden folder names.*/
    List<String> m_forbiddenFolder = new ArrayList<String>() {

        private static final long serialVersionUID = -52452684907673071L;

        {
            add("/sites/");
            add("/system/");
        }
    };

    /**Vaadin field.*/
    ComboBox m_fieldWorkplaceServer;

    /**Vaadin field.*/
    ComboBox m_fieldDefaultURI;

    /**Vaadin field.*/
    ComboBox m_fieldSharedFolder;

    /**Vaadin field.*/
    Button m_ok;

    /**Vaadin field.*/
    Button m_cancel;

    /**
     * Constructor.
     * @param manager CmsSiteManager instance
     */
    public CmsGlobalForm(CmsSiteManager manager) {
        m_manager = manager;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        A_CmsUI.getCmsObject().getRequestContext().setSiteRoot("/");

        List<CmsSite> allSites = OpenCms.getSiteManager().getAvailableSites(
            A_CmsUI.getCmsObject(),
            true,
            false,
            A_CmsUI.getCmsObject().getRequestContext().getOuFqn());

        for (CmsSite site : allSites) {
            if ((site.getSiteRoot() == null) || site.getSiteRoot().equals("") || site.getSiteRoot().equals("/")) {

                if (allSites.indexOf(site) == (allSites.size() - 1)) {
                    allSites.remove(site);
                    break;
                } else {
                    allSites.remove(site);
                }
            }
        }
        setUpWorkplaceComboBox(allSites);
        setUpDefaultUriComboBox(allSites);
        setUpSharedFolderComboBox();
        m_fieldSharedFolder.setValue(OpenCms.getSiteManager().getSharedFolder().replace("/", ""));

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -143413645462471704L;

            public void buttonClick(ClickEvent event) {

                submit();
                cancel();
                return;

            }

        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8880622123062328256L;

            public void buttonClick(ClickEvent event) {

                cancel();
                return;
            }
        });

    }

    /**
     * Cancels site edit.<p>
     */
    void cancel() {

        m_manager.openSubView("", true);
    }

    /**
     * Save results.
     */
    void submit() {

        try {
            OpenCms.getSiteManager().updateGeneralSettings(
                A_CmsUI.getCmsObject(),
                ((CmsSite)m_fieldDefaultURI.getValue()).getSiteRoot(),
                ((CmsSite)m_fieldWorkplaceServer.getValue()).getUrl(),
                "/" + (String)m_fieldSharedFolder.getValue() + "/");
            if (!CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.isEmpty()) {
                CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.set(1, (String)m_fieldSharedFolder.getValue());
            }
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
        } catch (@SuppressWarnings("unused") CmsException e) {
            //
        }

    }

    /**
     * Set up of combo box for default uri.
     * @param allSites alls available sites
     */
    private void setUpDefaultUriComboBox(List<CmsSite> allSites) {

        BeanItemContainer<CmsSite> objects = new BeanItemContainer<CmsSite>(CmsSite.class, allSites);
        m_fieldDefaultURI.setContainerDataSource(objects);
        m_fieldDefaultURI.setNullSelectionAllowed(false);
        m_fieldDefaultURI.setTextInputAllowed(false);
        m_fieldDefaultURI.setItemCaptionPropertyId("title");

        //set value

        String siteRoot = OpenCms.getSiteManager().getDefaultUri();
        if (siteRoot.endsWith("/")) {
            siteRoot = siteRoot.substring(0, siteRoot.length() - 1);
        }
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
        m_fieldDefaultURI.setValue(site);

    }

    /**
     * Set up of combo box for shared folder.
     */
    private void setUpSharedFolderComboBox() {

        m_fieldSharedFolder.setNullSelectionAllowed(false);
        m_fieldSharedFolder.setTextInputAllowed(false);
        try {
            List<CmsResource> folderUnderRoot = A_CmsUI.getCmsObject().readResources(
                "/",
                CmsResourceFilter.DEFAULT_FOLDERS,
                false);
            for (CmsResource folder : folderUnderRoot) {
                if (!m_forbiddenFolder.contains(folder.getRootPath())) {
                    m_fieldSharedFolder.addItem(folder.getRootPath().replace("/", ""));
                }
            }

        } catch (CmsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Sets the combo box for workplace.
     * @param allSites alls available sites
     */
    private void setUpWorkplaceComboBox(List<CmsSite> allSites) {

        final List<CmsSite> modSites = new ArrayList<CmsSite>();
        String defaultURL = OpenCms.getSiteManager().getWorkplaceServer();
        CmsSite siteWithDefaultURL = null;
        for (CmsSite site : allSites) {
            modSites.add(site);
            if (defaultURL.equals(site.getUrl())) {
                siteWithDefaultURL = site;
            }

        }
        if (siteWithDefaultURL == null) {
            siteWithDefaultURL = new CmsSite("dummy", defaultURL);
            modSites.add(0, siteWithDefaultURL);
        }
        final BeanItemContainer<CmsSite> objects = new BeanItemContainer<CmsSite>(CmsSite.class, modSites);
        m_fieldWorkplaceServer.setContainerDataSource(objects);
        m_fieldWorkplaceServer.setNullSelectionAllowed(false);
        m_fieldWorkplaceServer.setItemCaptionPropertyId("url");
        m_fieldWorkplaceServer.setValue(siteWithDefaultURL);
        m_fieldWorkplaceServer.setNewItemsAllowed(true);
        m_fieldWorkplaceServer.setImmediate(true);
        m_fieldWorkplaceServer.setNewItemHandler(new NewItemHandler() {

            private static final long serialVersionUID = -4760590374697520609L;

            public void addNewItem(String newItemCaption) {

                CmsSite newItem = new CmsSite("dummy", newItemCaption);
                objects.addBean(newItem);
                m_fieldWorkplaceServer.select(newItem);

            }
        });

    }

}
