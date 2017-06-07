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

package org.opencms.ui.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExternalLayout;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;
import org.opencms.ui.shared.rpc.I_CmsSitemapClientRpc;
import org.opencms.ui.shared.rpc.I_CmsSitemapServerRpc;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Extension used for the Vaadin dialogs in the sitemap editor.<p>
 */
public class CmsSitemapExtension extends AbstractExtension implements I_CmsSitemapServerRpc {

    /**
     * Dialog context for Vaadin dialogs in the sitemap editor.<p>
     */
    abstract class DialogContext implements I_CmsDialogContext {

        /** The context type. */
        private ContextType m_contextType;

        /** The list of resources. */
        private List<CmsResource> m_resources;

        /** The window used to display the dialog. */
        private Window m_window;

        /**
         * Constructor.<p>
         *
         * @param contextType the context type
         * @param resources the resources
         */
        public DialogContext(ContextType contextType, List<CmsResource> resources) {
            m_contextType = contextType;
            m_resources = resources != null ? resources : Collections.<CmsResource> emptyList();
        }

        /**
         * Closes the dialog window.<p>
         */
        public void closeWindow() {

            if (m_window != null) {
                m_window.close();
                m_window = null;
            }
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
         */
        @SuppressWarnings("synthetic-access")
        public void error(Throwable error) {

            closeWindow();
            LOG.error(error.getLocalizedMessage(), error);
            CmsErrorDialog.showErrorDialog(error, new Runnable() {

                public void run() {
                    // empty
                }
            });
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
         */
        public void finish(CmsProject project, String siteRoot) {

            throw new RuntimeException("NOT INPLEMENTED");

        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#focus(org.opencms.util.CmsUUID)
         */
        public void focus(CmsUUID structureId) {

            // does not apply
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getAllStructureIdsInView()
         */
        public List<CmsUUID> getAllStructureIdsInView() {

            return Collections.emptyList();
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getAppId()
         */
        public String getAppId() {

            return CmsSitemapEditorConfiguration.APP_ID;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getCms()
         */
        public CmsObject getCms() {

            return A_CmsUI.getCmsObject();
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getContextType()
         */
        public ContextType getContextType() {

            return m_contextType;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#getResources()
         */
        public List<CmsResource> getResources() {

            return m_resources;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#navigateTo(java.lang.String)
         */
        public void navigateTo(String appId) {

            throw new RuntimeException("NOT IMPLEMENTED");
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#onViewChange()
         */
        public void onViewChange() {

            throw new RuntimeException("NOT IMPLEMENTED");
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#reload()
         */
        public void reload() {

            throw new RuntimeException("NOT IMPLEMENTED");
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#setWindow(com.vaadin.ui.Window)
         */
        public void setWindow(Window window) {

            m_window = window;
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
         */
        public void start(String title, Component dialog) {

            start(title, dialog, DialogWidth.narrow);
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component, org.opencms.ui.components.CmsBasicDialog.DialogWidth)
         */
        public void start(String title, Component dialog, DialogWidth width) {

            if (dialog != null) {
                m_window = CmsBasicDialog.prepareWindow(width);
                m_window.setCaption(title);
                m_window.setContent(dialog);
                UI.getCurrent().addWindow(m_window);
                m_window.addCloseListener(new CloseListener() {

                    private static final long serialVersionUID = 1L;

                    public void windowClose(CloseEvent e) {

                        handleWindowClose();
                    }
                });
                if (dialog instanceof CmsBasicDialog) {
                    ((CmsBasicDialog)dialog).initActionHandler(m_window);
                }
            }
        }

        /**
         * @see org.opencms.ui.I_CmsDialogContext#updateUserInfo()
         */
        public void updateUserInfo() {

            // not supported
        }

        /**
         * Returns the client RPC.<p>
         *
         * @return the client RPC
         */
        @SuppressWarnings("synthetic-access")
        protected I_CmsEmbeddedDialogClientRPC getClientRPC() {

            return getRpcProxy(I_CmsEmbeddedDialogClientRPC.class);
        }

        /**
         * Handles the window close event.<p>
         */
        void handleWindowClose() {
            // empty
        }

    }

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapExtension.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The container for the locale comparison view. */
    private VerticalLayout m_localeCompareContainer;

    /** The currently active sitemap tree controller. */
    private CmsSitemapTreeController m_sitemapTreeController;

    /** The UI instance. */
    private CmsSitemapUI m_ui;

    /**
     * Creates a new instance.<p>
     *
     * @param ui the component to attach to
     */
    public CmsSitemapExtension(CmsSitemapUI ui) {
        extend(ui);
        m_ui = ui;
        registerRpc(this, I_CmsSitemapServerRpc.class);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapServerRpc#handleChangedProperties(java.lang.String)
     */
    public void handleChangedProperties(String id) {

        if (m_sitemapTreeController != null) {
            m_sitemapTreeController.updateNodeForId(new CmsUUID(id));
        }
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapServerRpc#openPageCopyDialog(java.lang.String, java.lang.String)
     */
    public void openPageCopyDialog(final String callId, final String structureId) {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResource resource = cms.readResource(new CmsUUID(structureId), CmsResourceFilter.IGNORE_EXPIRATION);
            DialogContext context = new DialogContext(null, Arrays.asList(resource)) {

                @SuppressWarnings("synthetic-access")
                public void finish(java.util.Collection<CmsUUID> result) {

                    closeWindow();
                    String response = result.isEmpty()
                    ? ""
                    : CmsStringUtil.listAsString(new ArrayList<Object>(result), "|");
                    getRpcProxy(I_CmsSitemapClientRpc.class).finishPageCopyDialog(callId, response);
                }
            };
            CmsCopyPageDialog dialog = new CmsCopyPageDialog(context);
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_DIALOG_TITLE_0);
            context.start(title, dialog);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Opens the property dialog for the locale comparison view.<p>
     *
     * @param sitemapEntryId the structure id for the sitemap entry to edit
     * @param rootId the structure id of the current tree's root
     */
    public void openPropertyDialog(CmsUUID sitemapEntryId, CmsUUID rootId) {

        getRpcProxy(I_CmsSitemapClientRpc.class).openPropertyDialog("" + sitemapEntryId, "" + rootId);
    }

    /**
     * Sets the currently active sitemap tree controller.<p>
     *
     * @param controller the controller to set
     */
    public void setSitemapTreeController(CmsSitemapTreeController controller) {

        m_sitemapTreeController = controller;
    }

    /**
     * Shows an info header in the locale-header-container element.<p>
     *
     * @param title the title
     * @param description the description
     * @param path the path
     * @param locale the locale
     * @param iconClass the icon class
     */
    public void showInfoHeader(String title, String description, String path, String locale, String iconClass) {

        getRpcProxy(I_CmsSitemapClientRpc.class).showInfoHeader(title, description, path, locale, iconClass);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapServerRpc#showLocaleComparison(java.lang.String)
     */
    public void showLocaleComparison(String id) {

        if (m_localeCompareContainer == null) {
            m_localeCompareContainer = new VerticalLayout();
            CmsExternalLayout layout = new CmsExternalLayout(
                CmsGwtConstants.ID_LOCALE_COMPARISON,
                m_localeCompareContainer);
            m_ui.getContent().addComponent(layout);
        }
        m_localeCompareContainer.removeAllComponents();
        m_localeCompareContainer.addComponent(new CmsLocaleComparePanel(id));

    }

}
