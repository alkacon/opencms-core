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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client;

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.client.util.CmsMediaQuery;
import org.opencms.gwt.client.util.CmsUniqueActiveItemContainer;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsAutoBeanFactory;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import elemental2.dom.DomGlobal;
import elemental2.webstorage.WebStorageWindow;

/**
 * Client side core data provider.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.gwt.CmsGwtActionElement
 */
public final class CmsCoreProvider extends CmsCoreData {

    /** AutoBean factory instance. */
    public static final I_CmsAutoBeanFactory AUTO_BEAN_FACTORY = GWT.create(I_CmsAutoBeanFactory.class);

    /** The media query string to detect touch only devices. */
    public static final String TOUCH_ONLY_RULE = "(hover: none) or (pointer: coarse)";

    /** Media query do detect device with no hover capability. */
    public static final CmsMediaQuery TOUCH_ONLY = CmsMediaQuery.parse(TOUCH_ONLY_RULE);

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsCoreProvider INSTANCE;

    /** The core service instance. */
    private static I_CmsCoreServiceAsync SERVICE;

    /** The vfs-service instance. */
    private static I_CmsVfsServiceAsync VFS_SERVICE;

    /** The unique active item container for the flyout menu. */
    private CmsUniqueActiveItemContainer m_activeFlyoutMenu = new CmsUniqueActiveItemContainer();

    /** The client time when the data is loaded. */
    private long m_clientTime;

    /** Event bus for client side events. */
    private EventBus m_eventBus = new SimpleEventBus();

    /** Flag which indicates whether we are in Internet Explorer 7. */
    private boolean m_isIe7;

    /**
     * Prevent instantiation.<p>
     *
     * @throws SerializationException if deserialization failed
     */
    protected CmsCoreProvider()
    throws SerializationException {

        super((CmsCoreData)CmsRpcPrefetcher.getSerializedObjectFromDictionary(getService(), DICT_NAME));
        m_clientTime = System.currentTimeMillis();

        I_CmsUserAgentInfo userAgentInfo = GWT.create(I_CmsUserAgentInfo.class);
        m_isIe7 = userAgentInfo.isIE7();
    }

    /**
     * Returns the client message instance.<p>
     *
     * @return the client message instance
     */
    public static CmsCoreProvider get() {

        if (INSTANCE == null) {
            try {
                INSTANCE = new CmsCoreProvider();
            } catch (SerializationException e) {
                CmsErrorDialog.handleException(
                    new Exception(
                        "Deserialization of core data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                        e));
            }
        }
        return INSTANCE;
    }

    /**
     * Gets the content attribute of a meta tag with a given name.<p>
     *
     * @param nameToFind the name of the meta tag
     *
     * @return the content attribute value of the found meta tag, or null if no meta tag with the given name was found
     */
    public static String getMetaElementContent(String nameToFind) {

        NodeList<Element> metas = Document.get().getDocumentElement().getElementsByTagName("meta");
        for (int i = 0; i < metas.getLength(); i++) {
            Element meta = metas.getItem(i);
            String name = meta.getAttribute("name");
            if (nameToFind.equals(name)) {
                return meta.getAttribute("content");
            }
        }
        return null;
    }

    /**
     * Returns the core service instance.<p>
     *
     * @return the core service instance
     */
    public static I_CmsCoreServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsCoreService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.gwt.CmsCoreService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

    /**
     * Returns the vfs service instance.<p>
     *
     * @return the vfs service instance
     */
    public static I_CmsVfsServiceAsync getVfsService() {

        if (VFS_SERVICE == null) {
            VFS_SERVICE = GWT.create(I_CmsVfsService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.gwt.CmsVfsService.gwt");
            ((ServiceDefTarget)VFS_SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return VFS_SERVICE;
    }

    /**
     * Checks if the client is touch-only.
     *
     * <p>This uses media queries, but the touch-only status can also be forcibly turned on with the request parameter __touchOnly=1.
     *
     * @return true if the client is touch-only
     */
    public static boolean isTouchOnly() {

        return TOUCH_ONLY.matches()
            || "1".equals(Location.getParameter("__touchOnly"))
            || "1".equals(CmsJsUtil.getLocalStorage("__touchOnly"));
    }

    /**
     * Adds the current site root of this context to the given resource name.<p>
     *
     * @param sitePath the resource name
     *
     * @return the translated resource name including site root
     *
     * @see #removeSiteRoot(String)
     */
    public String addSiteRoot(String sitePath) {

        if (sitePath == null) {
            return null;
        }
        String siteRoot = getAdjustedSiteRoot(getSiteRoot(), sitePath);
        StringBuffer result = new StringBuffer(128);
        result.append(siteRoot);
        if (((siteRoot.length() == 0) || (siteRoot.charAt(siteRoot.length() - 1) != '/'))
            && ((sitePath.length() == 0) || (sitePath.charAt(0) != '/'))) {
            // add slash between site root and resource if required
            result.append('/');
        }
        result.append(sitePath);
        return result.toString();
    }

    /**
     * Creates a new CmsUUID.<p>
     *
     * @param callback the callback to execute
     */
    public void createUUID(final AsyncCallback<CmsUUID> callback) {

        // do not stop/start since we do not want to give any feedback to the user
        CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().createUUID(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsUUID result) {

                callback.onSuccess(result);
            }
        };
        action.execute();
    }

    /**
     * Fires a client side event.<p>
     *
     * @param event the event to fire
     */
    public void fireEvent(Event<?> event) {

        m_eventBus.fireEvent(event);
    }

    /**
     * Returns the adjusted site root for a resource using the provided site root as a base.<p>
     *
     * Usually, this would be the site root for the current site.
     * However, if a resource from the <code>/system/</code> folder is requested,
     * this will be the empty String.<p>
     *
     * @param siteRoot the site root of the current site
     * @param resourcename the resource name to get the adjusted site root for
     *
     * @return the adjusted site root for the resource
     */
    public String getAdjustedSiteRoot(String siteRoot, String resourcename) {

        if (resourcename.startsWith(VFS_PATH_SYSTEM) || resourcename.startsWith(getSharedFolder())) {
            return "";
        } else {
            return siteRoot;
        }
    }

    /**
     * Returns the approximate time on the server.<p>
     *
     * @return the approximate server time
     */
    public long getEstimatedServerTime() {

        return m_clientTime + (System.currentTimeMillis() - m_clientTime);
    }

    /**
     * Gets the core event bus.<p>
     *
     * @return the core event bus
     */
    public EventBus getEventBus() {

        return m_eventBus;
    }

    /**
     * Returns the link to view the given resource in the file explorer.<p>
     *
     * @param sitePath the resource site path
     *
     * @return the link
     */
    public String getExplorerLink(String sitePath) {

        return getFileExplorerLink() + sitePath;
    }

    /**
     * Gets the unique active item container which holds a reference to the currently active content element flyout menu.<p>
     *
     * @return the unique active item container for flyout menus
     */
    public CmsUniqueActiveItemContainer getFlyoutMenuContainer() {

        return m_activeFlyoutMenu;

    }

    /**
     * Gets the page id that was last stored in sessionStorage.
     *
     * @return the page id that was last stored in session storage
     */
    public CmsUUID getLastPageId() {

        WebStorageWindow window = WebStorageWindow.of(DomGlobal.window);
        String lastPageStr = window.sessionStorage.getItem(CmsGwtConstants.LAST_CONTAINER_PAGE_ID);
        if (lastPageStr != null) {
            return new CmsUUID(lastPageStr);
        } else {
            return null;
        }
    }

    /**
     * Fetches the state of a resource from the server.<p>
     *
     * @param structureId the structure id of the resource
     * @param callback the callback which should receive the result
     */
    public void getResourceState(final CmsUUID structureId, final AsyncCallback<CmsResourceState> callback) {

        CmsRpcAction<CmsResourceState> action = new CmsRpcAction<CmsResourceState>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, false);
                getService().getResourceState(structureId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsResourceState result) {

                stop(false);
                callback.onSuccess(result);
            }
        };
        action.execute();
    }

    /**
     * Returns the resource type name for a given filename.<p>
     *
     * @param file the file info
     *
     * @return the resource type name
     */
    public String getResourceType(CmsFileInfo file) {

        String typeName = null;
        typeName = getExtensionMapping().get(file.getFileSuffix().toLowerCase());
        if (typeName == null) {
            typeName = "plain";
        }
        return typeName;
    }

    /**
     * Returns the resource type name for a given filename.<p>
     *
     * @param file the file info
     *
     * @return the resource type name
     */
    public String getResourceTypeIcon(CmsFileInfo file) {

        String typeName = null;
        typeName = getIconMapping().get(file.getFileSuffix().toLowerCase());
        if (typeName == null) {
            typeName = getIconMapping().get("");
        }
        return typeName;
    }

    /**
     * Gets the resource type icon for the given path.
     *
     * @param path a path
     * @return the resource type icon
     */
    public String getResourceTypeIcon(String path) {

        String typeName = null;
        String name = null;
        int slashPos = path.lastIndexOf("/");
        if (slashPos >= 0) {
            name = path.substring(slashPos + 1);
        } else {
            name = path;
        }
        int dotPos = name.lastIndexOf(".");
        String ext = "";
        if (dotPos >= 0) {
            ext = name.substring(dotPos).toLowerCase();
        }
        typeName = getIconMapping().get(ext);
        if (typeName == null) {
            typeName = getIconMapping().get("");
        }
        return typeName;
    }

    /**
     * Returns if the current user agent is IE7.<p>
     *
     * @return <code>true</code> if the current user agent is IE7
     */
    public boolean isIe7() {

        return m_isIe7;
    }

    /**
     * Returns an absolute link given a site path.<p>
     *
     * @param sitePath the site path
     *
     * @return the absolute link
     */
    public String link(String sitePath) {

        return CmsStringUtil.joinPaths(getVfsPrefix(), sitePath);
    }

    /**
     * Locks the given resource with a temporary lock.<p>
     *
     * @param structureId the resource structure id
     * @param callback the callback to execute
     */
    public void lock(final CmsUUID structureId, final I_CmsSimpleCallback<Boolean> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockTemp(structureId, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, structureId, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result == null ? Boolean.TRUE : Boolean.FALSE);
            }
        };
        lockAction.execute();
    }

    /**
     * Locks the given resource with a temporary lock.<p>
     *
     * @param structureId the resource structure id
     * @param loadTime the time when the requested resource was loaded
     * @param callback the callback to execute
     */
    public void lock(final CmsUUID structureId, long loadTime, final I_CmsSimpleCallback<Boolean> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockTemp(structureId, loadTime, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, structureId, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result == null ? Boolean.TRUE : Boolean.FALSE);
            }
        };
        lockAction.execute();
    }

    /**
     * Locks the given resource with a temporary lock.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param loadTime the time when the requested resource was loaded
     * @param callback the callback to execute
     */
    public void lock(final String sitePath, long loadTime, final I_CmsSimpleCallback<Boolean> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockIfExists(sitePath, loadTime, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, sitePath, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result == null ? Boolean.TRUE : Boolean.FALSE);
            }
        };
        lockAction.execute();
    }

    /**
     * Tries to lock a resource with a given structure id and returns an error if the locking fails.<p>
     *
     * @param structureId the structure id of the resource to lock
     * @param callback the callback to execute
     */
    public void lockOrReturnError(final CmsUUID structureId, final I_CmsSimpleCallback<String> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockTemp(structureId, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    final String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, structureId, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result);
            }
        };
        lockAction.execute();
    }

    /**
     * Tries to lock a resource with a given structure id and returns an error if the locking fails.<p>
     *
     * @param structureId the structure id of the resource to lock
     * @param loadTime the time when the requested resource was loaded
     * @param callback the callback to execute
     */
    public void lockOrReturnError(
        final CmsUUID structureId,
        final long loadTime,
        final I_CmsSimpleCallback<String> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockTemp(structureId, loadTime, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    final String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, structureId, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result);
            }
        };
        lockAction.execute();
    }

    /**
     * Tries to lock a resource with a given site path and returns an error if the locking fails.<p>
     * If the resource does not exist yet, the next existing ancestor folder will be checked if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param callback the callback to execute
     */
    public void lockOrReturnError(final String sitePath, final I_CmsSimpleCallback<String> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockIfExists(sitePath, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    final String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, sitePath, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result);
            }
        };
        lockAction.execute();
    }

    /**
     * Tries to lock a resource with a given site path and returns an error if the locking fails.<p>
     * If the resource does not exist yet, the next existing ancestor folder will be checked if it is lockable.<p>
     *
     * @param sitePath the site path of the resource to lock
     * @param loadTime the time when the requested resource was loaded
     * @param callback the callback to execute
     */
    public void lockOrReturnError(
        final String sitePath,
        final long loadTime,
        final I_CmsSimpleCallback<String> callback) {

        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().lockIfExists(sitePath, loadTime, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result != null) {
                    // unable to lock
                    final String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, sitePath, result);
                    CmsNotification.get().sendDeferred(CmsNotification.Type.WARNING, text);
                }
                callback.execute(result);
            }
        };
        lockAction.execute();
    }

    /**
     * Removes the current site root prefix from the given root path,
     * that is adjusts the resource name for the current site root.<p>
     *
     * If the resource name does not start with the current site root,
     * it is left untouched.<p>
     *
     * @param rootPath the resource name
     *
     * @return the resource name adjusted for the current site root
     *
     * @see #addSiteRoot(String)
     */
    public String removeSiteRoot(String rootPath) {

        String siteRoot = getAdjustedSiteRoot(getSiteRoot(), rootPath);
        if ((siteRoot != null)
            && (siteRoot.equals(getSiteRoot()))
            && rootPath.startsWith(siteRoot)
            && ((rootPath.length() == siteRoot.length()) || (rootPath.charAt(siteRoot.length()) == '/'))) {
            rootPath = rootPath.substring(siteRoot.length());
        }
        return rootPath;
    }

    /**
     * @see org.opencms.gwt.shared.CmsCoreData#setShowEditorHelp(boolean)
     */
    @Override
    public void setShowEditorHelp(final boolean show) {

        super.setShowEditorHelp(show);
        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().setShowEditorHelp(show, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                //nothing to do
            }
        };
        action.execute();
    }

    /**
     * Returns the absolute link to the given root path.<p>
     *
     * @param rootPath the root path
     * @param callback the callback to execute
     */
    public void substituteLinkForRootPath(final String rootPath, final I_CmsSimpleCallback<String> callback) {

        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            @Override
            public void execute() {

                getVfsService().substituteLinkForRootPath(getSiteRoot(), rootPath, this);
            }

            @Override
            protected void onResponse(String result) {

                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Unlocks the given resource, synchronously.<p>
     *
     * @param structureId the resource structure id
     *
     * @return <code>true</code> if succeeded, if not a a warning is already shown to the user
     */
    public boolean unlock(final CmsUUID structureId) {

        // lock the sitemap
        CmsRpcAction<String> unlockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().unlock(structureId, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result == null) {
                    // ok
                    return;
                }
                // unable to lock
                String text = Messages.get().key(Messages.GUI_UNLOCK_NOTIFICATION_2, structureId.toString(), result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        return unlockAction.executeSync() == null;
    }

    /**
     * Unlocks the given resource, synchronously.<p>
     *
     * @param sitePath the resource site path
     *
     * @return <code>true</code> if succeeded, if not a a warning is already shown to the user
     */
    public boolean unlock(final String sitePath) {

        // lock the sitemap
        CmsRpcAction<String> unlockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(200, false);
                getService().unlock(sitePath, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop(false);
                if (result == null) {
                    // ok
                    return;
                }
                // unable to lock
                String text = Messages.get().key(Messages.GUI_UNLOCK_NOTIFICATION_2, sitePath, result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        return unlockAction.executeSync() == null;
    }

}