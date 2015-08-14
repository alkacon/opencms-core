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

import org.opencms.ui.contextmenu.CmsDialogContextMenuItem;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.ui.dialogs.CmsSecureExportDialog;
import org.opencms.ui.dialogs.CmsTouchDialog;
import org.opencms.ui.dialogs.CmsUndeleteDialog;
import org.opencms.ui.dialogs.CmsUndoDialog;
import org.opencms.ui.dialogs.availability.CmsAvailabilityDialog;

import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of menu item provider.<p>
 */
public class CmsDefaultMenuItemProvider implements I_CmsContextMenuItemProvider {

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider#getMenuItems()
     */
    public List<I_CmsContextMenuItem> getMenuItems() {

        return Arrays.<I_CmsContextMenuItem> asList(
            new CmsDialogContextMenuItem(
                "availability",
                null,
                CmsAvailabilityDialog.class,
                "%(key.GUI_EXPLORER_CONTEXT_AVAILABILITY_0)",
                1,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDialogContextMenuItem(
                "undo",
                null,
                CmsUndoDialog.class,
                "%(key.GUI_EXPLORER_CONTEXT_UNDOCHANGES_0)",
                3,
                0,
                CmsStandardVisibilityCheck.UNDO),
            new CmsDialogContextMenuItem(
                "secureexport",
                null,
                CmsSecureExportDialog.class,
                "%(key.GUI_EXPLORER_CONTEXT_SECURE_0)",
                2,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDialogContextMenuItem(
                "touch",
                null,
                CmsTouchDialog.class,
                "%(key.GUI_EXPLORER_CONTEXT_TOUCH_0)",
                0,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDialogContextMenuItem(
                "undelete",
                null,
                CmsUndeleteDialog.class,
                "%(key.GUI_EXPLORER_CONTEXT_UNDELETE_0)",
                5,
                0,
                CmsStandardVisibilityCheck.UNDELETE));
    }
}
