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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.dialogs.CmsDeleteDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Multimap;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * App to check relations of resources in folder to other folder.<p>
 */
public class CmsLinkInFolderValidationApp extends A_CmsWorkplaceApp implements I_CmsUpdatableComponent {

    /**
     * Validator.<p>
     */
    public class InFolderValidator extends A_CmsLinkValidator {

        /**Resource which would be broken, if considered resoures would be deleted. */
        Multimap<CmsResource, CmsResource> brokenResources;

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#failedResources(java.util.List)
         */
        @Override
        public List<CmsResource> failedResources(List<String> resources) {

            try {
                CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                List<CmsResource> res = new ArrayList<CmsResource>();
                for (String resource : resources) {
                    if (cms.existsResource(resource)) {
                        res.add(cms.readResource(resource));
                    }
                }

                brokenResources = CmsDeleteDialog.getBrokenLinks(
                    cms,
                    res,
                    false,
                    CmsVaadinUtils.isButtonPressed(m_revertButton));
                return new ArrayList<CmsResource>(brokenResources.keySet());
            } catch (CmsException e) {
                return new ArrayList<CmsResource>();
            }
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#failMessage(org.opencms.file.CmsResource)
         */
        @Override
        public String failMessage(CmsResource resource) {

            if (brokenResources.size() == 0) {
                return "";
            }
            return brokenResources.get(resource).iterator().next().getRootPath();
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#getClickListener()
         */
        @Override
        public ItemClickListener getClickListener() {

            return new ItemClickListener() {

                private static final long serialVersionUID = -7729459896374968941L;

                public void itemClick(ItemClickEvent event) {

                    if (event.getButton().equals(MouseButton.RIGHT)) {
                        return;
                    }
                    if (!property.equals(event.getPropertyId())) {
                        return;
                    }
                    try {
                        CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                        cms.getRequestContext().setSiteRoot("");
                        CmsResource resource = cms.readResource(new CmsUUID((String)event.getItemId()));
                        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                        window.setCaption(CmsVaadinUtils.getMessageText(getCaptionKey()));
                        window.setContent(
                            new CmsResourceListDialog(new ArrayList<CmsResource>(brokenResources.get(resource))));
                        A_CmsUI.get().addWindow(window);
                    } catch (CmsException e) {
                        LOG.error("Unable to show detail resources", e);
                    }
                }

            };
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#getPropertyName()
         */
        @Override
        public String getPropertyName() {

            return "Relations";
        }

        /**
         * @see org.opencms.ui.apps.linkvalidation.A_CmsLinkValidator#getTableProperties()
         */
        @Override
        public Map<CmsResourceTableProperty, Integer> getTableProperties() {

            property = new CmsResourceTableProperty(getPropertyName(), String.class, "", getCaptionKey(), true, 0, 200);
            Map<CmsResourceTableProperty, Integer> res = new LinkedHashMap<CmsResourceTableProperty, Integer>(
                CmsFileTable.DEFAULT_TABLE_PROPERTIES);
            res.put(property, Integer.valueOf(0));
            return res;
        }

        /**
         * Returns the caption key.<p>
         *
         * @return key for caption
         */
        String getCaptionKey() {

            return org.opencms.ui.apps.Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_COLUMN_HEADER_0;
        }

    }

    /**
     * Bean for the state of the app.<p>
     */
    static class CmsStateBean {

        /**State seperator. */
        protected static String STATE_SEPERATOR = A_CmsWorkplaceApp.PARAM_SEPARATOR;

        /**Seperator for resources in state. */
        private static String RESOURCE_SEPERATOR = ";";

        /**List of resources. */
        private List<String> m_resources;

        /**Reverse the output? */
        private boolean m_reverse;

        /**CmsObject */
        private CmsObject m_cms = null;

        /**
         * public constructor.<p>
         *
         * @param resources List of resources (Root-paths)
         * @param reverse boolean
         *
         */
        public CmsStateBean(List<String> resources, boolean reverse) {

            setCmsObject();

            m_resources = resources;
            m_reverse = reverse;
        }

        /**
         * public constructor.<p>
         *
         * @param resources Resources as state string
         * @param reverse boolean
         */
        public CmsStateBean(String resources, boolean reverse) {

            setCmsObject();
            m_resources = getResourcesFromState(resources);
            m_reverse = reverse;
        }

        /**
         * Parses a given state string to state bean.<p>
         *
         * @param state to be read
         * @return CmsStateBean
         */
        public static CmsStateBean parseState(String state) {

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
                return new CmsStateBean(Collections.emptyList(), false);
            }
            String[] parameter = state.split(STATE_SEPERATOR);
            boolean reverse = false;
            if (parameter.length > 1) {
                reverse = Boolean.parseBoolean(parameter[1]);
            }
            return new CmsStateBean(parameter[0], reverse);
        }

        /**
         * Get the resource list.<p>
         *
         * @return List of resource root paths
         */
        public List<String> getResources() {

            return m_resources;
        }

        /**
         * Gets the state string of the current bean.<p>
         *
         * @return state string
         */
        public String getState() {

            return getResourceString() + STATE_SEPERATOR + Boolean.valueOf(m_reverse).toString();

        }

        /**
         * Is reverse mode selected?
         *
         * @return boolean
         */
        public boolean isReverse() {

            return m_reverse;
        }

        /**
         * Gets resources from state string.<p>
         *
         * @param resources state string
         * @return List of resource paths
         */
        private List<String> getResourcesFromState(String resources) {

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(resources)) {
                return Collections.EMPTY_LIST;
            }
            List<String> res = new ArrayList<String>();

            for (String uuidString : resources.split(RESOURCE_SEPERATOR)) {
                try {
                    res.add(m_cms.readResource(new CmsUUID(uuidString)).getRootPath());
                } catch (CmsException e) {
                    LOG.error("Can not read resource from state", e);
                }
            }
            return res;
        }

        /**
         * Get the state string of the resources.<p>
         *
         * @return resources as string to use in state
         */
        private String getResourceString() {

            String res = "";
            try {

                for (String resource : m_resources) {
                    res += m_cms.readResource(resource).getStructureId().getStringValue() + RESOURCE_SEPERATOR;
                }

            } catch (CmsException e) {
                LOG.error("Can't read resource", e);
            }
            return res.length() > 1 ? res.substring(0, res.length() - 1) : res;
        }

        /**
         * Prepares the CmsObject.<p>
         */
        private void setCmsObject() {

            if (m_cms == null) {
                try {
                    m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    m_cms.getRequestContext().setSiteRoot("");
                } catch (CmsException e) {
                    m_cms = A_CmsUI.getCmsObject();
                }
            }
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsLinkInFolderValidationApp.class);

    /**Bean for the state status. */
    private CmsStateBean m_stateBean;

    /**Vaadin component. */
    Button m_revertButton;

    /**Table */
    private CmsLinkValidationInternalTable m_table;

    /**Resource Selector. */
    private CmsInternalResources m_resourceSelector;

    /**
     * @see org.opencms.ui.apps.linkvalidation.I_CmsUpdatableComponent#update(java.util.List)
     */
    public void update(List<String> resources) {

        openSubView(new CmsStateBean(resources, CmsVaadinUtils.isButtonPressed(m_revertButton)).getState(), true);

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_stateBean = CmsStateBean.parseState(state);
        if (m_revertButton == null) {
            addToolbars();
        }

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel panel = new HorizontalSplitPanel();
        VerticalLayout result = new VerticalLayout();
        result.setSizeFull();
        VerticalLayout intro = CmsVaadinUtils.getInfoLayout(Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_INTRO_0);
        VerticalLayout nullResult = CmsVaadinUtils.getInfoLayout(
            Messages.GUI_LINKVALIDATION_CHECK_FOLDER_RELATIONS_NO_RESULT_0);

        nullResult.setVisible(false);
        m_table = new CmsLinkValidationInternalTable(intro, nullResult, new InFolderValidator());
        m_table.setVisible(false);
        m_table.setSizeFull();
        m_table.setWidth("100%");

        result.addComponent(m_table);
        result.addComponent(intro);
        result.addComponent(nullResult);

        m_table.setVisible(false);
        m_table.setSizeFull();
        m_table.setWidth("100%");
        m_resourceSelector = new CmsInternalResources(this);
        panel.setFirstComponent(m_resourceSelector);
        panel.setSecondComponent(result);

        panel.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);

        if (!m_stateBean.getResources().isEmpty()) {
            m_table.update(m_stateBean.getResources());
            m_resourceSelector.clearResources();
            for (String resource : m_stateBean.getResources()) {
                m_resourceSelector.addResource(resource);
            }
        }

        return panel;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Toggle table.<p>
     */
    protected void toggleTable() {

        CmsVaadinUtils.toggleButton(m_revertButton);
        openSubView(
            new CmsStateBean(m_stateBean.getResources(), CmsVaadinUtils.isButtonPressed(m_revertButton)).getState(),
            true);
    }

    /**
     * Add toolbar icons.<p>
     */
    private void addToolbars() {

        m_revertButton = CmsToolBar.createButton(
            FontOpenCms.REDO,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUPS_TOGGLE_0));
        m_revertButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8265075332953321274L;

            public void buttonClick(ClickEvent event) {

                toggleTable();

            }

        });
        if (m_stateBean.isReverse()) {
            CmsVaadinUtils.toggleButton(m_revertButton);
        }
        m_uiContext.addToolbarButton(m_revertButton);
    }
}
