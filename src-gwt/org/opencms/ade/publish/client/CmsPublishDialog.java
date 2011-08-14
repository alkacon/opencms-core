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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * 
 * Main class for the publish dialog.<p>
 * 
 * This class is mostly responsible for the control flow and RPC calls of the publish dialog.
 * It delegates most of the actual GUI work to the {@link CmsPublishSelectPanel} and {@link CmsBrokenLinksPanel} classes.
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishDialog extends CmsPopup {

    /**
     * The action for publishing and/or removing resources from the publish list.<p>
     */
    private class CmsPublishAction extends CmsRpcAction<List<CmsPublishResource>> {

        /** If true, try to ignore broken links when publishing. */
        private boolean m_force;

        /** Creates a new instance of this action. 
         * 
         * @param force if true, try to ignore broken links when publishing
         */
        public CmsPublishAction(boolean force) {

            m_force = force;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            setLoadingMessage(Messages.get().key(Messages.GUI_PUBLISHING_0));
            start(0, true);
            List<CmsUUID> resourcesToPublish = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToPublish());
            List<CmsUUID> resourcesToRemove = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToRemove());
            getService().publishResources(resourcesToPublish, resourcesToRemove, m_force, this);
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(List<CmsPublishResource> result) {

            onReceiveStatus(result);
            stop(true);
        }
    }

    /**
     * The action for loading the publish list.<p>
     */
    private class CmsPublishListAction extends CmsRpcAction<List<CmsPublishGroup>> {

        /** The publish list options which should be used. */
        private CmsPublishOptions m_options;

        /**
         * Creates a new publish list action.<p>
         * 
         * @param options the publish list options which should be used 
         */
        public CmsPublishListAction(CmsPublishOptions options) {

            m_options = options;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            start(0, true);
            getService().getPublishGroups(m_options, this);
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(List<CmsPublishGroup> result) {

            onReceivePublishList(result);
            stop(false);
        }
    }

    /** The dialog width in pixels. */
    public static final int DIALOG_WIDTH = 766;

    /** The project map used by showPublishDialog. */
    public static Map<String, String> m_staticProjects;

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** The index of the "broken links" panel. */
    private static final int PANEL_BROKEN_LINKS = 1;

    /** The index of the publish selection panel. */
    private static final int PANEL_SELECT = 0;

    /** The publish service instance. */
    private static I_CmsPublishServiceAsync SERVICE;

    /** The panel for selecting the resources to publish or remove from the publish list. */
    protected CmsPublishSelectPanel m_publishSelectPanel;

    /** The panel for showing the links that would be broken by publishing. */
    private CmsBrokenLinksPanel m_brokenLinksPanel;

    /** The root panel of this dialog which contains both the selection panel and the panel for displaying broken links. */
    private DeckPanel m_panel = new DeckPanel();

    /**
     * Constructs a new publish dialog.<p>
     * 
     * @param initData the initial data 
     */
    public CmsPublishDialog(CmsPublishData initData) {

        super(Messages.get().key(Messages.GUI_PUBLISH_DIALOG_TITLE_0), 800);
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(true);
        addStyleName(CSS.publishDialog());

        m_publishSelectPanel = new CmsPublishSelectPanel(this, initData.getProjects(), initData.getOptions());
        m_brokenLinksPanel = new CmsBrokenLinksPanel(this, initData.isCanPublishBrokenRelations());

        addDialogClose(null);

        m_panel = new DeckPanel();
        m_panel.add(m_publishSelectPanel);
        m_panel.add(m_brokenLinksPanel);
        setMainContent(m_panel);

        setPanel(PANEL_SELECT);
        onReceivePublishList(initData.getGroups());
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     */
    public static void showPublishDialog() {

        showPublishDialog(null);
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     * 
     * @param handler the close handler
     */
    public static void showPublishDialog(final CloseHandler<PopupPanel> handler) {

        (new CmsRpcAction<CmsPublishData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().getInitData(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(final CmsPublishData result) {

                CmsPublishDialog publishDialog = new CmsPublishDialog(result);
                if (handler != null) {
                    publishDialog.addCloseHandler(handler);
                }
                stop(false);
                publishDialog.centerHorizontally(100);
                // replace current notification widget by overlay
                publishDialog.catchNotifications();
            }
        }).execute();
    }

    /**
     * Returns the publish service instance.<p>
     * 
     * @return the publish service instance
     */
    protected static I_CmsPublishServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsPublishService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.publish.CmsPublishService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public void initCss() {

        I_CmsPublishLayoutBundle.INSTANCE.publishCss().ensureInjected();
    }

    /**
     * Method which is called when the cancel button is pressed.<p>
     */
    public void onCancel() {

        hide();
    }

    /**
     * Method which is called when the publish options are changed.<p>
     */
    public void onChangeOptions() {

        CmsPublishOptions options = m_publishSelectPanel.getPublishOptions();
        (new CmsPublishListAction(options)).execute();
    }

    /**
     * Method which is  called when the back button is pressed.<p>
     */
    public void onGoBack() {

        setPanel(PANEL_SELECT);
    }

    /**
     * Method which is called after the publish list has been received from the server.<p>
     * 
     * @param groups the groups of the publish list
     */
    public void onReceivePublishList(List<CmsPublishGroup> groups) {

        m_publishSelectPanel.setGroups(groups, true);
        setPanel(PANEL_SELECT);
        if (!isVisible()) {
            center();
        }
    }

    /**
     * Method which is called after the status from a publish action has arrived.<p>
     * 
     * @param brokenResources the list of broken resources
     */
    public void onReceiveStatus(List<CmsPublishResource> brokenResources) {

        if (brokenResources.isEmpty()) {
            hide();
        } else {
            m_brokenLinksPanel.setEntries(brokenResources);
            setPanel(PANEL_BROKEN_LINKS);
        }
    }

    /**
     * Method which is called when the "force publish" button is pressed.<p>    
     */
    public void onRequestForcePublish() {

        (new CmsPublishAction(true)).execute();
    }

    /**
     * Method which is called when the publish button is pressed.<p>     
     */
    public void onRequestPublish() {

        (new CmsPublishAction(false)).execute();
    }

    /**
     * Changes the currently active panel.<p>
     * 
     * @param panelId the number of the panel to show
     */
    public void setPanel(int panelId) {

        m_panel.showWidget(panelId);
        removeAllButtons();
        if (panelId == PANEL_SELECT) {
            for (CmsPushButton button : m_publishSelectPanel.getButtons()) {
                addButton(button);
            }
        } else if (panelId == PANEL_BROKEN_LINKS) {
            for (CmsPushButton button : m_brokenLinksPanel.getButtons()) {
                addButton(button);
            }
        }
    }
}
