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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsLockedResourcesList;
import org.opencms.ui.contextmenu.I_CmsContextMenuAction;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Action to lock a folder if it isn't already locked, with a dialog asking to confirm your choice
 * if the folder contains resources locked by other users.
 */
public class CmsLockAction implements I_CmsContextMenuAction {

    /**
     * Cancel action.<p>
     */
    class ActionCancel implements Runnable {

        /**
         * @see java.lang.Runnable#run()
         */
        @SuppressWarnings("synthetic-access")
        public void run() {

            m_context.finish(new ArrayList<CmsUUID>());
        }
    }

    /**
     * OK button action.<p>
     */
    class ActionOk implements Runnable {

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {

            doLock();
        }

    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLockAction.class);

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        CmsObject cms = A_CmsUI.getCmsObject();
        m_context = context;
        List<CmsResource> blockingLocked = Lists.newArrayList();
        for (CmsResource res : context.getResources()) {
            try {
                List<CmsResource> blockingLockedForCurrentResource = cms.getBlockingLockedResources(res);
                blockingLocked.addAll(blockingLockedForCurrentResource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        if (blockingLocked.isEmpty()) {
            doLock();
        } else {
            String messageKey = context.getResources().size() > 1
            ? org.opencms.workplace.commons.Messages.GUI_LOCK_MULTI_INFO_LOCKEDSUBRESOURCES_0
            : org.opencms.workplace.commons.Messages.GUI_LOCK_INFO_LOCKEDSUBRESOURCES_0;
            CmsLockedResourcesList widget = new CmsLockedResourcesList(
                cms,
                blockingLocked,
                CmsVaadinUtils.getMessageText(messageKey),
                new ActionOk(),
                new ActionCancel());
            widget.displayResourceInfo(context.getResources());
            context.start(CmsVaadinUtils.getMessageText(Messages.GUI_LOCK_DIALOG_TITLE_0), widget);
        }
    }

    /**
     * Actually locks the resources.<p>
     */
    protected void doLock() {

        CmsObject cms = A_CmsUI.getCmsObject();
        CmsException storedException = null;
        List<CmsUUID> changedIds = Lists.newArrayList();
        for (CmsResource res : m_context.getResources()) {
            try {
                cms.lockResource(res);
                changedIds.add(res.getStructureId());
            } catch (CmsException e) {
                if (storedException == null) {
                    storedException = e;
                }
                LOG.warn(e.getLocalizedMessage(), e);
            }

        }
        if (storedException != null) {
            m_context.finish(changedIds);
            m_context.error(storedException);
        } else {
            m_context.finish(changedIds);
        }
    }

}
