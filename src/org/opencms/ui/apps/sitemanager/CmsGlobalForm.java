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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSSLMode;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 *Class for the Global configuration dialog.<p>
 */

public class CmsGlobalForm extends CmsBasicDialog {

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsGlobalForm.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3553152729226102382L;

    /** Forbidden folder names.*/
    List<String> m_forbiddenFolder = new ArrayList<String>() {

        private static final long serialVersionUID = -52452684907673071L;

        {
            add("/sites/");
            add("/system/");
        }
    };

    /**Vaadin field.*/
    private Button m_cancel;

    /**Vaadin field.*/
    private ComboBox m_fieldDefaultURI;

    /**Vaadin field.*/
    private ComboBox m_fieldSharedFolder;

    /**Site manager instance. */
    private CmsSiteManager m_manager;

    /**Vaadin field.*/
    private Button m_ok;

    /**Vaadin layout for server.*/
    private VerticalLayout m_serverLayout;

    /**Edit group for workplace servers.*/
    private CmsEditableGroup m_workplaceServerGroup;

    /**CmsObject.*/
    private CmsObject m_cms;

    /**
     * Constructor.<p>
     *
     * @param manager CmsSiteManager instance
     */
    public CmsGlobalForm(CmsSiteManager manager) {

        m_manager = manager;

        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error("Error on cloning CmsObject", e);
        }
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_cms.getRequestContext().setSiteRoot("/");

        List<CmsSite> allSites_temp = OpenCms.getSiteManager().getAvailableSites(
            m_cms,
            true,
            false,
            m_cms.getRequestContext().getOuFqn());

        List<CmsSite> allSites = new ArrayList<CmsSite>(allSites_temp);

        for (CmsSite site : allSites_temp) {
            if ((site.getSiteRoot() == null) || site.getSiteRoot().equals("") || site.getSiteRoot().equals("/")) {

                if (allSites_temp.indexOf(site) == (allSites_temp.size() - 1)) {
                    allSites.remove(site);
                    break;
                } else {
                    allSites.remove(site);
                }
            }
        }
        allSites_temp.clear();
        setUpDefaultUriComboBox(allSites);
        setUpSharedFolderComboBox();

        setServerLayout(allSites);
        m_fieldSharedFolder.setValue(OpenCms.getSiteManager().getSharedFolder().replace("/", ""));

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -143413645462471704L;

            public void buttonClick(ClickEvent event) {

                submit();
                closeDialog();
            }

        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8880622123062328256L;

            public void buttonClick(ClickEvent event) {

                closeDialog();
                return;
            }
        });
    }

    /**
     * Selects given item of ComboBox.<p>
     *
     * @param site to be selected
     * @param combo Combobox
     */
    static void selectNewWorkplaceServer(CmsSite site, ComboBox combo) {

        combo.select(site);
    }

    /**
     * Cancels site edit.<p>
     */
    void closeDialog() {

        m_manager.closeDialogWindow(false);
    }

    /**
     * Returns a list with all entered workplace servers.<p>
     *
     * @return a string list
     */
    Map<String, CmsSSLMode> getWebserverList() {

        Map<String, CmsSSLMode> ret = new LinkedHashMap<String, CmsSSLMode>();
        for (I_CmsEditableGroupRow row : m_workplaceServerGroup.getRows()) {
            CmsWorkplaceServerWidget widget = (CmsWorkplaceServerWidget)row.getComponent();
            ret.put(widget.getServer(), widget.getSSLMode());
        }
        return ret;
    }

    /**
     * Save results.<p>
     */
    void submit() {

        try {
            m_manager.updateGeneralSettings(
                m_cms,
                ((CmsSite)m_fieldDefaultURI.getValue()).getSiteRoot(),
                getWebserverList(),
                "/" + (String)m_fieldSharedFolder.getValue() + "/");
            if (!CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.isEmpty()) {
                CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.set(1, (String)m_fieldSharedFolder.getValue());
            }
            // write the system configuration
            // OpenCms.writeConfiguration(CmsSitesConfiguration.class);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
            //
        }

    }

    /**
     * Fills the layout with combo boxes for all setted workplace servers + one combo box for adding further urls.<p>
     *
     * @param sites from sitemanager
     */
    private void setServerLayout(final List<CmsSite> sites) {

        m_workplaceServerGroup = new CmsEditableGroup(m_serverLayout, new Supplier<Component>() {

            public Component get() {

                CmsWorkplaceServerWidget row = new CmsWorkplaceServerWidget(sites, null);
                return row;
            }
        }, "Add");

        for (String server : OpenCms.getSiteManager().getWorkplaceServers()) {
            CmsWorkplaceServerWidget row = new CmsWorkplaceServerWidget(sites, server);
            m_workplaceServerGroup.addRow(row);
        }
    }

    /**
     * Set up of combo box for default uri.<p>
     *
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
     * Set up of combo box for shared folder.<p>
     */
    private void setUpSharedFolderComboBox() {

        m_fieldSharedFolder.setNullSelectionAllowed(false);
        m_fieldSharedFolder.setTextInputAllowed(false);
        try {
            List<CmsResource> folderUnderRoot = m_cms.readResources("/", CmsResourceFilter.DEFAULT_FOLDERS, false);
            for (CmsResource folder : folderUnderRoot) {
                if (!m_forbiddenFolder.contains(folder.getRootPath())) {
                    m_fieldSharedFolder.addItem(folder.getRootPath().replace("/", ""));
                }
            }
        } catch (CmsException e) {
            LOG.error("Error reading resource.", e);
        }
    }

}
