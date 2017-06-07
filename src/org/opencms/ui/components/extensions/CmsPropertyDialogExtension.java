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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsPrefetchSerializationPolicy;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsUpdateListener;
import org.opencms.ui.actions.CmsPropertiesDialogAction;
import org.opencms.ui.shared.rpc.I_CmsPropertyClientRpc;
import org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc;
import org.opencms.ui.util.CmsNewResourceBuilder;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Extension used for the GWT-based property dialog called from the workplace.
 *
 * This keeps track of the list of resources which were visible when the property dialog was opened, allowing
 * the user to navigate through the list with prev/next buttons.
 */
public class CmsPropertyDialogExtension extends AbstractExtension implements I_CmsPropertyServerRpc {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyDialogExtension.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The list of structure ids. */
    List<CmsUUID> m_ids = Lists.newArrayList();

    /** Current position in the ID list. */
    int m_position;

    /** Helper used to create a new resource after entering its properties. */
    private CmsNewResourceBuilder m_newResourceBuilder;

    /** The structure ids of possibly updated resources. */
    private HashSet<CmsUUID> m_updatedIds = Sets.newHashSet();

    /** The update listener. */
    private I_CmsUpdateListener<String> m_updateListener;

    /**
     * Creates a new instance and binds it to a UI instance.<p>
     *
     * @param ui the UI to bind this extension to
     * @param updateListener the update listener
     */
    public CmsPropertyDialogExtension(UI ui, I_CmsUpdateListener<String> updateListener) {
        extend(ui);
        m_updateListener = updateListener;
        registerRpc(this, I_CmsPropertyServerRpc.class);
    }

    /**
     * Open property editor for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     * @param allIds structure ids of resources for the prev/next navigation
     * @param editName controls whether the file name should be editable
     */
    public void editProperties(CmsUUID structureId, List<CmsUUID> allIds, boolean editName) {

        m_position = allIds.indexOf(structureId);

        m_ids = allIds;
        m_updatedIds.add(structureId);
        boolean online = A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().isOnlineProject();
        getRpcProxy(I_CmsPropertyClientRpc.class).editProperties(
            "" + structureId,
            editName,
            online || (allIds.size() < 2));
    }

    /**
     * Opens the property dialog for a resource to be created with the 'New' dialog.<p>
     *
     * @param builder the resource builder used by the 'New' dialog to create the resource
     */
    public void editPropertiesForNewResource(CmsNewResourceBuilder builder) {

        try {
            CmsPropertiesBean propData = builder.getPropertyData();
            String serializedPropData = RPC.encodeResponseForSuccess(
                I_CmsVfsService.class.getMethod("loadPropertyData", CmsUUID.class),
                propData,
                CmsPrefetchSerializationPolicy.instance());
            getRpcProxy(I_CmsPropertyClientRpc.class).editPropertiesForNewResource(serializedPropData);
            m_newResourceBuilder = builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc#onClose(long)
     */
    public void onClose(long delayMillis) {

        remove();
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        List<String> updates = Lists.newArrayList();
        for (CmsUUID id : m_updatedIds) {
            updates.add("" + id);
        }
        m_updateListener.onUpdate(updates);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc#removeExtension()
     */
    public void removeExtension() {

        remove();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc#requestNextFile(int)
     */
    public void requestNextFile(int offset) {

        int newPos = m_position;
        int count = 0;
        do {
            newPos = nextIndex(newPos, offset);
            count += 1;
            if (count > m_ids.size()) {
                // prevent infinite loop in case user suddenly can't edit any properties anymore (e.g. through a permission change)
                newPos = m_position;
                break;
            }
        } while (!canEdit(m_ids.get(newPos)));
        m_position = newPos;
        CmsUUID nextId = m_ids.get(m_position);
        m_updatedIds.add(nextId);
        getRpcProxy(I_CmsPropertyClientRpc.class).sendNextId("" + nextId);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc#savePropertiesForNewResource(java.lang.String)
     */
    public void savePropertiesForNewResource(String data) {

        try {
            getRpcProxy(I_CmsPropertyClientRpc.class).confirmSaveForNew();
            ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(
                Thread.currentThread().getContextClassLoader(),
                null);
            // Filling stream reader with data
            streamReader.prepareToRead(data);
            // Reading deserialized object from the stream
            CmsPropertyChangeSet changes = (CmsPropertyChangeSet)(streamReader.readObject());
            m_newResourceBuilder.setPropertyChanges(changes);
            m_newResourceBuilder.safeCreateResource();
            remove();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the user can edit the resource with the given id.<p>
     *
     * @param id a structure id
     * @return true if the user can edit the file
     */
    protected boolean canEdit(CmsUUID id) {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsResource res = null;
        try {
            res = cms.readResource(id, CmsResourceFilter.ALL);
            boolean result = CmsPropertiesDialogAction.VISIBILITY.getVisibility(
                A_CmsUI.getCmsObject(),
                Lists.newArrayList(res)).isActive();
            return result;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Computes the next index.<p>
     *
     * This method by called more than once per press of next/prev, since uneditable resources need to be skipped.
     *
     * @param pos the current position
     * @param offset the offset (+1 or -1)
     * @return the next index
     */
    int nextIndex(int pos, int offset) {

        return (pos + offset + m_ids.size()) % m_ids.size();
    }

}
