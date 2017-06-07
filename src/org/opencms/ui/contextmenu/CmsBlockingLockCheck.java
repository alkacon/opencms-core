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

package org.opencms.ui.contextmenu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsLockedResourcesList;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * A wrapper context menu action which first checks whether the resources for which the action is executed have any children
 * locked by different users.<p>
 *
 * If so, a dialog showing these resources will be displayed; otherwise the wrapped context menu action
 * will be executed.<p>
 */
public class CmsBlockingLockCheck implements I_CmsContextMenuAction {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsBlockingLockCheck.class);

    /** Context menu to execute if we don't have blocking locked resources. */
    private I_CmsContextMenuAction m_nextAction;

    /**
     * Creates a new instance.<p>
     *
     * @param nextAction the action to execute if we don't have blocking locked resources
     *
     */
    public CmsBlockingLockCheck(I_CmsContextMenuAction nextAction) {
        m_nextAction = nextAction;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        List<CmsResource> resources = context.getResources();
        List<CmsResource> blocked = Lists.newArrayList();
        for (CmsResource resource : resources) {
            try {
                blocked.addAll(cms.getBlockingLockedResources(resource));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (blocked.isEmpty()) {
            m_nextAction.executeAction(context);
        } else {

            CmsLockedResourcesList dialog = new CmsLockedResourcesList(
                cms,
                blocked,
                CmsVaadinUtils.getMessageText(Messages.GUI_CANT_PERFORM_OPERATION_BECAUSE_OF_LOCKED_RESOURCES_0),
                new Runnable() {

                    public void run() {

                        List<CmsUUID> noStructureIds = Collections.emptyList();
                        context.finish(noStructureIds);
                    }

                },
                null);

            context.start(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_CONTEXT_LOCKS_0),
                dialog);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "CmsBlockingLockCheck[" + m_nextAction.toString() + "]";
    }
}
