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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsRemovableFormRow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.VerticalLayout;

/**
 *Class for the Global configuration dialog.<p>
 */

public class CmsGlobalForm extends VerticalLayout {

    /**
     * Validator for the worlplace server combo boxes.<p>
     */
    class WorkplaceServerValidator implements Validator {

        /**vaadin seril id.*/
        private static final long serialVersionUID = -2592817529797310651L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                return;
            }
            String workplaceServer = ((CmsSite)value).getUrl();
            List<String> allServer = getWebserverList();
            if (allServer.indexOf(workplaceServer) != allServer.lastIndexOf(workplaceServer)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_WORKPLACE_DOUBLE_1, workplaceServer));
            }

        }

    }

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

    /**All currently configured sites in opencms.*/
    private List<CmsSite> m_sites;

    /**Vaadin field.*/
    private Button m_ok;

    /**Vaadin layout for server.*/
    private FormLayout m_serverLayout;

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
        m_sites = allSites;
        setUpDefaultUriComboBox(allSites);
        setUpSharedFolderComboBox();

        setServerLayout(allSites);
        m_fieldSharedFolder.setValue(OpenCms.getSiteManager().getSharedFolder().replace("/", ""));

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -143413645462471704L;

            public void buttonClick(ClickEvent event) {

                if (isValidWorkplaceServer()) {
                    submit();
                    cancel();
                }
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

        validateWorkplaceServerBoxes();

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
     * Adds a new removable combo box row to the layout.<p>
     */
    void addWorkplaceServerBox() {

        final ComboBox serverCombo = new ComboBox();
        final CmsRemovableFormRow<ComboBox> server = new CmsRemovableFormRow<ComboBox>(serverCombo, "remove hard");
        setUpWorkplaceComboBox(m_sites, serverCombo, true, null);
        serverCombo.setValue(null);
        serverCombo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 2526672395159579782L;

            public void valueChange(ValueChangeEvent event) {

                server.setEnabledRemoveOption(true);
                serverCombo.removeValueChangeListener(this);
                addWorkplaceServerBox();

            }
        });

        server.setWidth("100%");
        server.setEnabledRemoveOption(false);
        serverCombo.addValidator(new WorkplaceServerValidator());
        server.setRemoveRunnable(new Runnable() {

            public void run() {

                validateWorkplaceServerBoxes();

            }
        });
        m_serverLayout.addComponent(server);
        validateWorkplaceServerBoxes();
    }

    /**
     * Cancels site edit.<p>
     */
    void cancel() {

        m_manager.openSubView("", true);
    }

    /**
     * Returns a list with all entered workplace servers.<p>
     *
     * @return a string list
     */
    List<String> getWebserverList() {

        List<String> ret = new ArrayList<String>();
        for (Component item : m_serverLayout) {
            @SuppressWarnings("unchecked")
            CmsRemovableFormRow<ComboBox> row = (CmsRemovableFormRow<ComboBox>)item;
            if (((ComboBox)row.getComponent(0)).getValue() != null) {
                ret.add(((CmsSite)((ComboBox)row.getComponent(0)).getValue()).getUrl());
            }
        }

        return ret;
    }

    /**
     * Checks if all workplace server comboboxes are valid.<p>
     *
     * @return true if entered values are valid
     */
    boolean isValidWorkplaceServer() {

        for (Component item : m_serverLayout) {
            @SuppressWarnings("unchecked")
            CmsRemovableFormRow<ComboBox> row = (CmsRemovableFormRow<ComboBox>)item;
            if (!((ComboBox)row.getComponent(0)).isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Save results.<p>
     */
    void submit() {

        try {
            OpenCms.getSiteManager().updateGeneralSettings(
                m_cms,
                ((CmsSite)m_fieldDefaultURI.getValue()).getSiteRoot(),
                getWebserverList(),
                "/" + (String)m_fieldSharedFolder.getValue() + "/");
            if (!CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.isEmpty()) {
                CmsEditSiteForm.FORBIDDEN_FOLDER_NAMES.set(1, (String)m_fieldSharedFolder.getValue());
            }
            // write the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
        } catch (CmsException e) {
            //
        }

    }

    /**
     * Sets the caption and description for first workplace server row and enables or disables the remove options.<p>
     */
    @SuppressWarnings("unchecked")
    void validateWorkplaceServerBoxes() {

        m_serverLayout.getComponent(0).setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_WORKPLACE_0));
        ((CmsRemovableFormRow<ComboBox>)m_serverLayout.getComponent(0)).setDescription(
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_WORKPLACE_HELP_0));
        int count = m_serverLayout.getComponentCount();
        if (count > 2) {
            for (int i = 0; i < (count - 1); i++) {
                ((CmsRemovableFormRow<ComboBox>)m_serverLayout.getComponent(i)).setEnabledRemoveOption(true);
            }
        } else {
            ((CmsRemovableFormRow<ComboBox>)m_serverLayout.getComponent(0)).setEnabledRemoveOption(false);
        }

    }

    /**
     * Fills the layout with combo boxes for all setted workplace servers + one combo box for adding further urls.<p>
     *
     * @param sites from sitemanager
     */
    private void setServerLayout(List<CmsSite> sites) {

        for (String site : OpenCms.getSiteManager().getWorkplaceServers()) {
            ComboBox serverCombo = new ComboBox();
            setUpWorkplaceComboBox(sites, serverCombo, false, site);
            serverCombo.addValidator(new WorkplaceServerValidator());

            CmsRemovableFormRow<ComboBox> server = new CmsRemovableFormRow<ComboBox>(serverCombo, "remove hard");
            server.setRemoveRunnable(new Runnable() {

                public void run() {

                    validateWorkplaceServerBoxes();

                }
            });
            server.setWidth("100%");

            m_serverLayout.addComponent(server);
        }

        addWorkplaceServerBox();
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

    /**
     * Sets the combo box for workplace.<p>
     *
     * @param allSites alls available sites
     * @param combo combo box to fill
     * @param nullselect if true, nothing is selected
     * @param defaultValue if set, this value gets chosen
     */
    private void setUpWorkplaceComboBox(
        List<CmsSite> allSites,
        final ComboBox combo,
        boolean nullselect,
        String defaultValue) {

        final List<CmsSite> modSites = new ArrayList<CmsSite>();
        CmsSite siteWithDefaultURL = null;

        String defaultURL = defaultValue;

        for (CmsSite site : allSites) {
            modSites.add(site);
            if (defaultValue != null) {
                if (defaultURL.equals(site.getUrl())) {
                    siteWithDefaultURL = site;
                }
            }
        }
        if (defaultValue != null) {
            if (siteWithDefaultURL == null) {
                siteWithDefaultURL = new CmsSite("dummy", defaultURL);
                modSites.add(0, siteWithDefaultURL);
            }
        }

        final BeanItemContainer<CmsSite> objects = new BeanItemContainer<CmsSite>(CmsSite.class, modSites);
        combo.setContainerDataSource(objects);
        combo.setNullSelectionAllowed(nullselect);
        combo.setItemCaptionPropertyId("url");
        combo.setValue(siteWithDefaultURL);
        combo.setNewItemsAllowed(true);
        combo.setImmediate(true);
        combo.setNewItemHandler(new NewItemHandler() {

            private static final long serialVersionUID = -4760590374697520609L;

            public void addNewItem(String newItemCaption) {

                CmsSite newItem = new CmsSite("dummy", newItemCaption);
                objects.addBean(newItem);
                combo.select(newItem);
            }
        });

    }

}
