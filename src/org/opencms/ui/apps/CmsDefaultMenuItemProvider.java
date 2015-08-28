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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.contextmenu.CmsBlockingLockCheck;
import org.opencms.ui.contextmenu.CmsDefaultContextMenuItem;
import org.opencms.ui.contextmenu.CmsDialogAction;
import org.opencms.ui.contextmenu.CmsDirectPublishDialogAction;
import org.opencms.ui.contextmenu.CmsEditPropertiesAction;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.CmsSubmenu;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.ui.dialogs.CmsDeleteDialog;
import org.opencms.ui.dialogs.CmsSecureExportDialog;
import org.opencms.ui.dialogs.CmsTouchDialog;
import org.opencms.ui.dialogs.CmsUndeleteDialog;
import org.opencms.ui.dialogs.CmsUndoDialog;
import org.opencms.ui.dialogs.availability.CmsAvailabilityDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.UI;

/**
 * Default implementation of menu item provider.<p>
 */
public class CmsDefaultMenuItemProvider implements I_CmsContextMenuItemProvider {

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsDefaultMenuItemProvider.class);

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider#getMenuItems()
     */
    public List<I_CmsContextMenuItem> getMenuItems() {

        return Arrays.<I_CmsContextMenuItem> asList(
            new CmsDefaultContextMenuItem(
                "availability",
                "advanced",
                new CmsBlockingLockCheck(new CmsDialogAction(CmsAvailabilityDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_AVAILABILITY_0)",
                1,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDefaultContextMenuItem(
                "undo",
                null,
                new CmsBlockingLockCheck(new CmsDialogAction(CmsUndoDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_UNDOCHANGES_0)",
                3,
                0,
                CmsStandardVisibilityCheck.UNDO),
            new CmsDefaultContextMenuItem(
                "secureexport",
                "advanced",
                new CmsBlockingLockCheck(new CmsDialogAction(CmsSecureExportDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_SECURE_0)",
                2,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDefaultContextMenuItem(
                "touch",
                "advanced",
                new CmsBlockingLockCheck(new CmsDialogAction(CmsTouchDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_TOUCH_0)",
                0,
                0,
                CmsStandardVisibilityCheck.DEFAULT),
            new CmsDefaultContextMenuItem(
                "undelete",
                null,
                new CmsBlockingLockCheck(new CmsDialogAction(CmsUndeleteDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_UNDELETE_0)",
                5,
                0,
                CmsStandardVisibilityCheck.UNDELETE),

            new CmsDefaultContextMenuItem(
                "properties",
                null,
                new CmsBlockingLockCheck(new CmsEditPropertiesAction()),
                "%(key.GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0)",
                7,
                0,
                new CmsMenuItemVisibilitySingleOnly(CmsStandardVisibilityCheck.DEFAULT)),

            new CmsDefaultContextMenuItem(
                "directpublish",
                null,
                new CmsDirectPublishDialogAction(),
                "%(key.GUI_EXPLORER_CONTEXT_PUBLISH_0)",
                1,
                0,
                CmsStandardVisibilityCheck.PUBLISH),

            new CmsSubmenu("advanced", null, "%(key.GUI_EXPLORER_CONTEXT_ADVANCED_0)", 6, 0),

            new CmsDefaultContextMenuItem(
                "edit",
                null,
                null,
                "%(key.GUI_EXPLORER_CONTEXT_EDIT_0)",
                0,
                0,
                new CmsMenuItemVisibilitySingleOnly(CmsStandardVisibilityCheck.EDIT)) {

                @Override
                public void executeAction(org.opencms.ui.I_CmsDialogContext context) {

                    CmsObject cms = A_CmsUI.getCmsObject();
                    String url = OpenCms.getLinkManager().substituteLink(cms, "/system/workplace/editors/editor.jsp");
                    url += "?resource=";
                    url += cms.getSitePath(context.getResources().get(0));
                    url += "&backlink=";
                    try {
                        url += URLEncoder.encode(UI.getCurrent().getPage().getLocation().toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        url += UI.getCurrent().getPage().getLocation().toString();
                    }
                    UI.getCurrent().getPage().open(url, "_self");
                }
            },

            new CmsDefaultContextMenuItem(
                "delete",
                null,
                new CmsBlockingLockCheck(new CmsDialogAction(CmsDeleteDialog.class)),
                "%(key.GUI_EXPLORER_CONTEXT_DELETE_0)",
                3,
                0,
                CmsStandardVisibilityCheck.DEFAULT));
    }
}
