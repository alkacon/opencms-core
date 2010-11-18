/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/CmsCoreProvider.java,v $
 * Date   : $Date: 2010/11/18 15:28:10 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Client side core data provider.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsGwtActionElement
 */
public final class CmsCoreProvider extends CmsCoreData {

    /** Path to the editor. */
    public static final String VFS_PATH_EDITOR = "/system/workplace/editors/editor.jsp";

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Internal instance. */
    private static CmsCoreProvider INSTANCE;

    /** The core service instance. */
    private static I_CmsCoreServiceAsync SERVICE;

    /**
     * Prevent instantiation.<p> 
     */
    protected CmsCoreProvider() {

        super((CmsCoreData)CmsRpcPrefetcher.getSerializedObject(getService(), DICT_NAME));
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsCoreProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsCoreProvider();
        }
        return INSTANCE;
    }

    /**
     * Returns the core service instance.<p>
     * 
     * @return the core service instance
     */
    public static I_CmsCoreServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsCoreService.class);
        }
        return SERVICE;
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

        if (resourcename.startsWith(VFS_PATH_SYSTEM)) {
            return "";
        } else {
            return siteRoot;
        }
    }

    /**
     * Returns an absolute link given a site path.<p>
     * 
     * @param sitePath the site path
     * 
     * @return the absolute link
     */
    public String link(String sitePath) {

        return getContext() + sitePath;
    }

    /**
     * Locks the current resource.<p>
     * 
     * @return <code>true</code> if succeeded
     * 
     * @see #lock(String)
     */
    public boolean lock() {

        return lock(getUri());
    }

    /**
     * Locks the given resource with a temporary lock, synchronously.<p>
     * 
     * @param uri the resource URI
     * 
     * @return <code>true</code> if succeeded, if not a a warning is already shown to the user
     */
    public boolean lock(final String uri) {

        // lock the sitemap
        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                setLoadingMessage(Messages.get().key(Messages.GUI_LOCKING_0));
                start(200, false);
                getService().lockTemp(uri, this);
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
                String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, uri, result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        return lockAction.executeSync() == null;
    }

    /**
     * Locks the given resource with a temporary lock, synchronously and additionally checking that 
     * the given resource has not been modified after the given timestamp.<p>
     * 
     * @param uri the resource URI
     * @param modification the timestamp to check
     * 
     * @return <code>true</code> if succeeded, if not a a warning is already shown to the user
     */
    public boolean lockAndCheckModification(final String uri, final long modification) {

        // lock the sitemap
        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                setLoadingMessage(Messages.get().key(Messages.GUI_LOCKING_0));
                start(200, false);
                getService().lockTempAndCheckModification(uri, modification, this);
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
                String text = Messages.get().key(Messages.GUI_LOCK_NOTIFICATION_2, uri, result);
                CmsNotification.get().send(CmsNotification.Type.ERROR, text);
            }
        };
        return lockAction.executeSync() == null;
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
        if ((siteRoot == getSiteRoot())
            && rootPath.startsWith(siteRoot)
            && ((rootPath.length() == siteRoot.length()) || (rootPath.charAt(siteRoot.length()) == '/'))) {
            rootPath = rootPath.substring(siteRoot.length());
        }
        return rootPath;
    }

    /**
     * Translates an url name to a form containing no illegal characters.<p>
     * 
     * @param urlname the url name which should be translated 
     * @param callback the callback which should be called with the translated url name 
     */
    public void translateUrlName(final String urlname, final AsyncCallback<String> callback) {

        // do not stop/start since we do not want to give any feedback to the user
        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().translateUrlName(urlname, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(String result) {

                callback.onSuccess(result);
            }
        };
        action.execute();
    }

    /**
     * Unlocks the current resource.<p>
     * 
     * @return <code>true</code> if succeeded
     * 
     * @see #unlock(String)
     */
    public boolean unlock() {

        return unlock(getUri());
    }

    /**
     * Unlocks the given resource, synchronously.<p>
     * 
     * @param uri the resource URI
     * 
     * @return <code>true</code> if succeeded, if not a a warning is already shown to the user
     */
    public boolean unlock(final String uri) {

        // lock the sitemap
        CmsRpcAction<String> unlockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                setLoadingMessage(Messages.get().key(Messages.GUI_UNLOCKING_0));
                start(200, false);
                getService().unlock(uri, this);
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
                String text = Messages.get().key(Messages.GUI_UNLOCK_NOTIFICATION_2, uri, result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        return unlockAction.executeSync() == null;
    }
}