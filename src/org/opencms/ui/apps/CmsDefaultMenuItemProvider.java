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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.CmsAboutDialogAction;
import org.opencms.ui.actions.CmsAvailabilityDialogAction;
import org.opencms.ui.actions.CmsChangeTypeDialogAction;
import org.opencms.ui.actions.CmsContextMenuActionItem;
import org.opencms.ui.actions.CmsCopyMoveDialogAction;
import org.opencms.ui.actions.CmsDeleteDialogAction;
import org.opencms.ui.actions.CmsDirectPublishDialogAction;
import org.opencms.ui.actions.CmsEditCodeDialogAction;
import org.opencms.ui.actions.CmsEditDialogAction;
import org.opencms.ui.actions.CmsPreferencesDialogAction;
import org.opencms.ui.actions.CmsProjectDialogAction;
import org.opencms.ui.actions.CmsPropertiesDialogAction;
import org.opencms.ui.actions.CmsPublishQueueDialogAction;
import org.opencms.ui.actions.CmsSecureExportDialogAction;
import org.opencms.ui.actions.CmsTouchDialogAction;
import org.opencms.ui.actions.CmsUndeleteDialogAction;
import org.opencms.ui.actions.CmsUndoDialogAction;
import org.opencms.ui.contextmenu.CmsDefaultContextMenuItem;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.CmsSubmenu;
import org.opencms.ui.contextmenu.I_CmsContextMenuAction;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.ui.dialogs.CmsLockAction;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Default implementation of menu item provider.<p>
 */
public class CmsDefaultMenuItemProvider implements I_CmsContextMenuItemProvider {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsDefaultMenuItemProvider.class);

    /**
     * Creates a new instance.<p>
     */
    public CmsDefaultMenuItemProvider() {
        // default constructor, do nothing
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider#getMenuItems()
     */
    public List<I_CmsContextMenuItem> getMenuItems() {

        CmsSubmenu advanced = new CmsSubmenu("advanced", null, "%(key.GUI_EXPLORER_CONTEXT_ADVANCED_0)", 6, 0);

        return Arrays.<I_CmsContextMenuItem> asList(
            advanced,
            new CmsContextMenuActionItem(new CmsAvailabilityDialogAction(), advanced.getId(), 3, 0),
            new CmsContextMenuActionItem(new CmsUndoDialogAction(), null, 1, 0),
            new CmsContextMenuActionItem(new CmsCopyMoveDialogAction(), null, 2, 0),
            new CmsContextMenuActionItem(new CmsDeleteDialogAction(), null, 3, 0),
            new CmsContextMenuActionItem(new CmsSecureExportDialogAction(), advanced.getId(), 2, 0),
            new CmsContextMenuActionItem(new CmsTouchDialogAction(), advanced.getId(), 0, 0),
            new CmsContextMenuActionItem(new CmsUndeleteDialogAction(), null, 5, 0),
            new CmsContextMenuActionItem(new CmsPropertiesDialogAction(), null, 7, 0),
            new CmsContextMenuActionItem(new CmsDirectPublishDialogAction(), null, 1, 0),
            new CmsContextMenuActionItem(new CmsEditDialogAction(), null, 0, 0),
            new CmsContextMenuActionItem(new CmsChangeTypeDialogAction(), advanced.getId(), 0, 0),
            new CmsContextMenuActionItem(new CmsEditCodeDialogAction(), advanced.getId(), 0, 0),
            new CmsContextMenuActionItem(new CmsAboutDialogAction(), null, 7, 0),
            new CmsContextMenuActionItem(new CmsPreferencesDialogAction(), null, 6, 0),
            new CmsContextMenuActionItem(new CmsPublishQueueDialogAction(), null, 4, 0),
            new CmsContextMenuActionItem(new CmsProjectDialogAction(), null, 5, 0),

            new CmsDefaultContextMenuItem(
                "lock",
                null,
                new CmsLockAction(),
                "%(key.GUI_EXPLORER_CONTEXT_LOCK_0)",
                0,
                0,
                CmsStandardVisibilityCheck.LOCK),

            new CmsDefaultContextMenuItem("unlock", null, new I_CmsContextMenuAction() {

                public void executeAction(I_CmsDialogContext context) {

                    CmsObject cms = A_CmsUI.getCmsObject();
                    List<CmsUUID> changedIds = Lists.newArrayList();
                    for (CmsResource res : context.getResources()) {
                        try {
                            cms.unlockResource(res);
                            changedIds.add(res.getStructureId());
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                        context.finish(changedIds);
                    }
                }
            }, "%(key.GUI_EXPLORER_CONTEXT_UNLOCK_0)", 0, 0, CmsStandardVisibilityCheck.UNLOCK),

            new CmsDefaultContextMenuItem("steallock", null, new I_CmsContextMenuAction() {

                public void executeAction(I_CmsDialogContext context) {

                    CmsObject cms = A_CmsUI.getCmsObject();
                    List<CmsUUID> changedIds = Lists.newArrayList();

                    for (CmsResource res : context.getResources()) {
                        try {
                            cms.changeLock(res);
                            changedIds.add(res.getStructureId());
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                    context.finish(changedIds);
                }

            }, "%(key.GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0)", 0, 0, CmsStandardVisibilityCheck.STEAL_LOCK));
    }

}
