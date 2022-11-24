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

package org.opencms.ui.dialogs;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsQuickLaunchLocationCache;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsExtendedSiteSelector.SiteSelectorOption;
import org.opencms.ui.components.extensions.CmsEmbeddedDialogExtension;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Context for dialogs embedded into plain GWT modules.<p>
 */
public class CmsEmbeddedDialogContext implements I_CmsDialogContext {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEmbeddedDialogContext.class);

    /** Pattern to check if a given server link starts with with a protocol string. */
    private static Pattern PROTOCOL_PATTERN = Pattern.compile("^http.?://.*");

    /** The app id. */
    private String m_appId;

    /** The context type. */
    private ContextType m_contextType;

    /** Keeps the dialog frame on window close. */
    private boolean m_keepFrameOnClose;

    /** The list of resources. */
    private List<CmsResource> m_resources;

    /** The window used to display the dialog. */
    private Window m_window;

    /** The extension used for communicating with the client. */
    private CmsEmbeddedDialogExtension m_extension;

    /** The parameters. */
    private Map<String, String> m_parameters;

    /**
     * Constructor.<p>
     *
     * @param appId the app id
     * @param extension the extension used to communicate with the client
     * @param contextType the context type
     * @param resources the resources
     * @param parameters the additional parameters
     */
    public CmsEmbeddedDialogContext(
        String appId,
        CmsEmbeddedDialogExtension extension,
        ContextType contextType,
        List<CmsResource> resources,
        Map<String, String> parameters) {

        m_extension = extension;
        m_appId = appId;
        m_contextType = contextType;
        m_resources = resources != null ? resources : Collections.<CmsResource> emptyList();
        m_parameters = parameters;
    }

    /**
     * Closes the dialog window.<p>
     *
     * @param keepFrame <code>true</code> to keep the embedded iFrame.<p>
     */
    public void closeWindow(boolean keepFrame) {

        if (m_window != null) {
            m_keepFrameOnClose = keepFrame;
            m_window.close();
            m_window = null;
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
     */
    public void error(Throwable error) {

        closeWindow(true);
        CmsErrorDialog.showErrorDialog(error, new Runnable() {

            public void run() {

                removeDialogFrame();
            }
        });
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(org.opencms.file.CmsProject, java.lang.String)
     */
    public void finish(CmsProject project, String siteRoot) {

        if ((project != null) || (siteRoot != null)) {
            String sitePath = null;
            if (siteRoot != null) {
                CmsQuickLaunchLocationCache locationCache = CmsQuickLaunchLocationCache.getLocationCache(
                    A_CmsUI.get().getHttpSession());
                sitePath = locationCache.getPageEditorLocation(A_CmsUI.getCmsObject(), siteRoot);
                if (sitePath == null) {
                    sitePath = locationCache.getFileExplorerLocation(siteRoot);
                    if (sitePath != null) {
                        int index = sitePath.indexOf("/" + CmsADEManager.CONTENT_FOLDER_NAME);
                        if (index >= 0) {
                            sitePath = sitePath.substring(0, index);
                        }
                    }
                }
            } else if ((m_resources != null) && !m_resources.isEmpty()) {
                sitePath = A_CmsUI.getCmsObject().getSitePath(m_resources.get(0));
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(sitePath)) {
                sitePath = "/";
            }
            String serverLink = OpenCms.getLinkManager().getServerLink(getCms(), sitePath);
            if (!PROTOCOL_PATTERN.matcher(serverLink).matches()) {
                serverLink = "http://" + serverLink;
            }
            getClientRPC().finishForProjectOrSiteChange(sitePath, serverLink);
        } else {
            finish((Collection<CmsUUID>)null);
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.util.Collection)
     */
    public void finish(Collection<CmsUUID> result) {

        closeWindow(true);
        String resources = "";
        if (result != null) {
            for (CmsUUID id : result) {
                resources += id.toString() + ";";
            }
        }
        getClientRPC().finish(resources);
    }

    /**
     * Opens the location from the site selector option.
     *
     * @param option the site selector option
     */
    public void finish(SiteSelectorOption option) {

        String path = option.getPath();
        CmsObject cms = A_CmsUI.getCmsObject();
        if (path == null) {
            finish(null, option.getSite());
        } else {
            String sitePath = option.getPath();
            boolean foundPage = false;
            try {
                CmsResource res = cms.readDefaultFile(sitePath);
                if (res != null) {
                    foundPage = true;
                }
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            if (foundPage) {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(sitePath)) {
                    sitePath = "/";
                }
                String serverLink = OpenCms.getLinkManager().getServerLink(getCms(), sitePath);
                if (!PROTOCOL_PATTERN.matcher(serverLink).matches()) {
                    serverLink = "http://" + serverLink;
                }
                getClientRPC().finishForProjectOrSiteChange(sitePath, serverLink);
            } else {
                Page.getCurrent().open(CmsCoreService.getFileExplorerLink(cms, option.getSite()) + sitePath, "_top");
            }
        }
    }

    /**
     * Finishes the dialog and returns an arbitrary string to the opener.
     *
     * @param str the string to pass to the opener
     */
    public void finishWithString(String str) {

        closeWindow(true);
        getClientRPC().selectString(str);
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

        return m_appId;
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
     * @see org.opencms.ui.I_CmsDialogContext#getParameters()
     */
    public Map<String, String> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getResources()
     */
    public List<CmsResource> getResources() {

        return m_resources;
    }

    /**
     * Leaves page by navigating to given URI.
     *
     * @param uri the URI to navigate to
     */
    public void leavePage(String uri) {

        getClientRPC().leavePage(uri);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#navigateTo(java.lang.String)
     */
    public void navigateTo(String appId) {

        String targetUri = CmsVaadinUtils.getWorkplaceLink(appId);
        getClientRPC().leavePage(targetUri);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#onViewChange()
     */
    public void onViewChange() {

        if (m_window != null) {
            m_window.center();
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#reload()
     */
    public void reload() {

        closeWindow(true);
        reloadParent();
    }

    /**
     * Sets the principal.<p>
     *
     * @param principalName the principal name
     */
    public void selectString(String principalName) {

        getClientRPC().selectString(principalName);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#setWindow(com.vaadin.ui.Window)
     */
    public void setWindow(Window window) {

        m_window = window;
        m_window.addCloseListener(new CloseListener() {

            private static final long serialVersionUID = 1L;

            public void windowClose(CloseEvent e) {

                handleWindowClose();
            }
        });
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
            m_keepFrameOnClose = false;
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

        getClientRPC().reloadParent();
    }

    /**
     * Returns the client RPC.<p>
     *
     * @return the client RPC
     */
    protected I_CmsEmbeddedDialogClientRPC getClientRPC() {

        return m_extension.getClientRPC();
    }

    /**
     * Handles the window close event.<p>
     */
    void handleWindowClose() {

        if (!m_keepFrameOnClose) {
            removeDialogFrame();
        }
    }

    /**
     * Removes the dialog iFrame.<p>
     */
    void removeDialogFrame() {

        getClientRPC().finish(null);
    }

    /**
     * Reloads the parent window.<p>
     */
    private void reloadParent() {

        getClientRPC().reloadParent();
    }
}
