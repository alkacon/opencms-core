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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsLockedResourcesList;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Abstract workplace actions class providing helper methods.<p>
 */
public abstract class A_CmsWorkplaceAction implements I_CmsWorkplaceAction {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsWorkplaceAction.class);

    /**
     * Gets the title to use for the dialog.<p>
     *
     * @return the title to use for the dialog
     */
    public String getDialogTitle() {

        return OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()).key(getDialogTitleKey());
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle(java.util.Locale)
     */
    public String getTitle(Locale locale) {

        return OpenCms.getWorkplaceManager().getMessages(locale).key(getTitleKey());
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        return getVisibility(context.getCms(), context.getResources());
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#isActive(org.opencms.ui.I_CmsDialogContext)
     */
    public boolean isActive(I_CmsDialogContext context) {

        return getVisibility(context).isActive();
    }

    /**
     * Returns the workplace message bundle key of the action dialog title.<p>
     *
     * @return the dialog title message bundle key
     */
    protected String getDialogTitleKey() {

        return getTitleKey();
    }

    /**
     * Returns the workplace message bundle key of the action title.<p>
     *
     * @return the title message bundle key
     */
    protected abstract String getTitleKey();

    /**
     * Returns if there are any blocking locks within the context resources.<p>
     * Will open the blocking locks dialog if required.<p>
     *
     * @param context the dialog context
     *
     * @return <code>true</code> in case of blocking locks
     */
    protected boolean hasBlockingLocks(final I_CmsDialogContext context) {

        return hasBlockingLocks(context, true);
    }

    /**
     * Returns if there are any blocking locks within the context resources.<p>
     * Will open the blocking locks dialog if required.<p>
     *
     * @param context the dialog context
     * @param showDialog flag, indicating if the dialog should be displayed, hinting to the locked resources.
     *
     * @return <code>true</code> in case of blocking locks
     */
    protected boolean hasBlockingLocks(final I_CmsDialogContext context, boolean showDialog) {

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
            return false;
        } else if (showDialog) {

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
            return true;
        }

    /**
     * Opens the given dialog in a new overlay window.<p>
     *
     * @param dialog the dialog
     * @param context the dialog context
     */
    protected void openDialog(CmsBasicDialog dialog, I_CmsDialogContext context) {

        context.start(getDialogTitle(), dialog);
    }

    /**
     * Opens the given dialog in a new overlay window.<p>
     *
     * @param dialog the dialog
     * @param context the dialog context
     * @param dialogWidth The dialog width
     */
    protected void openDialog(
        CmsBasicDialog dialog,
        I_CmsDialogContext context,
        CmsBasicDialog.DialogWidth dialogWidth) {

        context.start(getDialogTitle(), dialog, dialogWidth);
    }
}
