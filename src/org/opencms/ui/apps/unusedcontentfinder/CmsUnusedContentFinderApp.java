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

package org.opencms.ui.apps.unusedcontentfinder;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsUnusedContentFinderComponent;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

/**
 * Vaadin app to find unused contents.<p>
 */
public class CmsUnusedContentFinderApp extends A_CmsWorkplaceApp {

    /**
     * App state.
     */
    public static class StateBean {

        /** The site. */
        String m_site;

        /** The folder. */
        String m_folder;

        /** The resource type. */
        I_CmsResourceType m_resourceType;

        /**
         * Returns the folder.
         * @return the folder
         */
        public String getFolder() {

            return m_folder;
        }

        /**
         * Returns the resource type.
         * @return the resource type
         */
        public I_CmsResourceType getResourceType() {

            return m_resourceType;
        }

        /**
         * Returns the site.
         * @return the site
         */
        public String getSite() {

            return m_site;
        }

        /**
         * Sets the folder.
         * @param folder the folder
         */
        public void setFolder(String folder) {

            m_folder = folder;
        }

        /**
         * Sets the resource type.
         * @param resourceType the resource type
         */
        public void setResourceType(I_CmsResourceType resourceType) {

            m_resourceType = resourceType;
        }

        /**
         * Sets the site.
         * @param site the site
         */
        public void setSite(String site) {

            m_site = site;
        }
    }

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUnusedContentFinderApp.class);

    /** Hash parameter. */
    private static final String SITE = "s";

    /** Hash parameter. */
    private static final String FOLDER = "f";

    /** Hash parameter. */
    private static final String RESOURCE_TYPE = "t";

    /** The unused content finder component. */
    private CmsUnusedContentFinderComponent m_unusedContentFinderComponent;

    /**
     * Generates a state bean from a given state string.
     * @param state the state string
     * @return the state bean
     */
    public static StateBean generateStateBean(String state) {

        StateBean stateBean = new StateBean();
        stateBean.setSite(A_CmsWorkplaceApp.getParamFromState(state, SITE).replace("%2F", "/"));
        stateBean.setFolder(A_CmsWorkplaceApp.getParamFromState(state, FOLDER).replace("%2F", "/"));
        try {
            String typeName = A_CmsWorkplaceApp.getParamFromState(state, RESOURCE_TYPE);
            if (typeName != null) {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
                stateBean.setResourceType(type);
            }
        } catch (CmsLoaderException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return stateBean;
    }

    /**
     * Generates a state string for a given state bean.
     * @param stateBean the state bean
     * @return the state string
     */
    public static String generateStateString(StateBean stateBean) {

        String state = "";
        state = A_CmsWorkplaceApp.addParamToState(state, SITE, stateBean.getSite());
        state = A_CmsWorkplaceApp.addParamToState(state, FOLDER, stateBean.getFolder());
        if (stateBean.getResourceType() != null) {
            state = A_CmsWorkplaceApp.addParamToState(state, RESOURCE_TYPE, stateBean.getResourceType().getTypeName());
        }
        return state;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changed -> {
            if (m_unusedContentFinderComponent != null) {
                m_unusedContentFinderComponent.search(false);
            }
        });
        super.initUI(context);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<>();
        crumbs.put(
            "",
            Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_UNUSED_CONTENT_FINDER_TITLE_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSizeFull();
        m_unusedContentFinderComponent = new CmsUnusedContentFinderComponent();
        splitPanel.setFirstComponent(m_unusedContentFinderComponent.getFormComponent());
        splitPanel.setSecondComponent(m_unusedContentFinderComponent.getResultComponent());
        splitPanel.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        m_infoLayout.addComponent(m_unusedContentFinderComponent.getResultFilterComponent());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state)) {
            CmsUnusedContentFinderApp.StateBean stateBean = generateStateBean(state);
            m_unusedContentFinderComponent.setState(stateBean);
            m_unusedContentFinderComponent.search(false);
        }
        return splitPanel;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;

    }
}
