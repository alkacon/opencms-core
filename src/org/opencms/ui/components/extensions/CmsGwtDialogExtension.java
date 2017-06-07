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

package org.opencms.ui.components.extensions;

import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.publish.CmsPublishService;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsPrefetchSerializationPolicy;
import org.opencms.gwt.shared.CmsHistoryVersion.OfflineOnline;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc;
import org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.gwt.user.server.rpc.RPC;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

/**
 * Extension used to open existing GWT based dialogs (from ADE, etc.) from the server side, for use in context menu actions.<p>
 */
public class CmsGwtDialogExtension extends AbstractExtension implements I_CmsGwtDialogServerRpc {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGwtDialogExtension.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The update listener. */
    private I_CmsUpdateListener<String> m_updateListener;

    /**
     * Creates a new instance and binds it to a UI instance.<p>
     *
     * @param ui the UI to bind this extension to
     * @param updateListener the update listener
     */
    public CmsGwtDialogExtension(UI ui, I_CmsUpdateListener<String> updateListener) {
        extend(ui);
        m_updateListener = updateListener;
        registerRpc(this, I_CmsGwtDialogServerRpc.class);
    }

    /**
     * Opens the dialog for editing pointer resources.<p>
     *
     * @param resource the pointer resource
     */
    public void editPointer(CmsResource resource) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).editPointer("" + resource.getStructureId());
    }

    /**
     * Open property editor for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     * @param editName controls whether the file name should be editable
     */
    public void editProperties(CmsUUID structureId, boolean editName) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).editProperties("" + structureId, editName);
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc#onClose(boolean)
     */
    public void onClose(boolean reinitUI) {

        remove();
        if (reinitUI) {
            A_CmsUI.get().reload();
        }
        if (m_updateListener != null) {
            m_updateListener.onUpdate(new ArrayList<String>());
        }
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc#onClose(java.util.List, long)
     */
    public void onClose(List<String> changedStructureIds, long delayMillis) {

        remove();
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (m_updateListener != null) {
            m_updateListener.onUpdate(changedStructureIds);
        }

    }

    /**
     * Opens the categories dialog for the given resource.<p>
     *
     * @param resource the resource
     */
    public void openCategories(CmsResource resource) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).openCategoriesDialog(resource.getStructureId().toString());
    }

    /**
     * Opens the gallery dialog for the given gallery folder.<p>
     *
     * @param resource the gallery folder resource
     */
    public void openGalleryDialog(CmsResource resource) {

        try {
            CmsObject cms = A_CmsUI.getCmsObject();
            JSONObject conf = new JSONObject();
            conf.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE, GalleryMode.view.name());
            conf.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_PATH, cms.getSitePath(resource));
            conf.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX, "");
            conf.put(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG, CmsGalleryTabConfiguration.TC_SELECT_ALL);
            getRpcProxy(I_CmsGwtDialogClientRpc.class).openGalleryDialog(conf.toString());
        } catch (JSONException e) {
            CmsErrorDialog.showErrorDialog(e);
        }
    }

    /**
     * Opens the resource info dialog.<p>
     *
     * @param resource the resource
     */
    public void openInfoDialog(CmsResource resource) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).openInfoDialog(resource.getStructureId().toString());
    }

    /**
     * Opens the lock report for the given resource.<p>
     *
     * @param resource the resource for which to display the lock report
     */
    public void openLockReport(CmsResource resource) {

        String dialogTitle = CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_DIALOGTITLE_LOCKREPORT_0);
        getRpcProxy(I_CmsGwtDialogClientRpc.class).openLockReport(dialogTitle, resource.getStructureId().toString());
    }

    /**
     * Opens the publish dialog for the given project.<p>
     *
     * @param project the project for which to open the dialog
     */
    public void openPublishDialog(CmsProject project) {

        CmsPublishData publishData = getPublishData(project);
        String data = getSerializedPublishData(publishData);
        getRpcProxy(I_CmsGwtDialogClientRpc.class).openPublishDialog(data);
    }

    /**
     * Tells the client to open the publish dialog for the given resources.<p>
     *
     * @param resources the resources for which to open the publish dialog.
     */
    public void openPublishDialog(List<CmsResource> resources) {

        String data = getSerializedPublishData(getPublishData(resources));
        getRpcProxy(I_CmsGwtDialogClientRpc.class).openPublishDialog(data);
    }

    /**
     * Opens the 'Replace' dialog for the resource with the given structure id.<p>
     *
     * @param structureId the structure id
     */
    public void openReplaceDialog(CmsUUID structureId) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).openReplaceDialog("" + structureId);
    }

    /**
     * Shows the OpenCms about dialog.<p>
     */
    public void showAbout() {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).showAbout();
    }

    /**
     * Shows the prewview dialog for a given resource and version.<p>
     *
     * @param id the structure id of the resource
     * @param version the version
     * @param offlineOnline indicates whether we want the offlne or online version
     */
    public void showPreview(CmsUUID id, Integer version, OfflineOnline offlineOnline) {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).showPreview("" + id, version + ":" + offlineOnline);
    }

    /**
     * Shows the user preferences.<p>
     */
    public void showUserPreferences() {

        getRpcProxy(I_CmsGwtDialogClientRpc.class).showUserPreferences();
    }

    /**
     * Gets the publish data for the given project.<p>
     *
     * @param project the project to open publish dialog for
     *
     * @return the publish data
     */
    protected CmsPublishData getPublishData(CmsProject project) {

        CmsPublishService publishService = new CmsPublishService();
        CmsObject cms = A_CmsUI.getCmsObject();
        publishService.setCms(cms);
        publishService.setRequest((HttpServletRequest)(VaadinService.getCurrentRequest()));
        try {
            return publishService.getPublishData(
                cms,
                new HashMap<String, String>()/*params*/,
                null/*workflowId*/,
                "" + project.getUuid()/*projectParam*/,
                new ArrayList<String>(),
                null/*closelink*/,
                false/*confirmation*/);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the publish data for the given resources.<p>
     *
     * @param directPublishResources the resources to publish
     *
     * @return the publish data for the resources
     */
    protected CmsPublishData getPublishData(List<CmsResource> directPublishResources) {

        CmsPublishService publishService = new CmsPublishService();
        CmsObject cms = A_CmsUI.getCmsObject();
        publishService.setCms(cms);
        List<String> pathList = Lists.newArrayList();
        for (CmsResource resource : directPublishResources) {
            pathList.add(cms.getSitePath(resource));
        }
        publishService.setRequest((HttpServletRequest)(VaadinService.getCurrentRequest()));
        try {
            return publishService.getPublishData(
                cms,
                new HashMap<String, String>()/*params*/,
                null/*workflowId*/,
                null/*projectParam*/,
                pathList,
                null/*closelink*/,
                false/*confirmation*/);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Serializes a CmsPublishData object into string form using the GWT serialization.<p>
     *
     * @param data the publish data
     *
     * @return the serialized publish data
     */
    protected String getSerializedPublishData(CmsPublishData data) {

        try {
            String prefetchedData = RPC.encodeResponseForSuccess(
                I_CmsPublishService.class.getMethod("getInitData", java.util.HashMap.class),
                data,
                CmsPrefetchSerializationPolicy.instance());
            return prefetchedData;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
