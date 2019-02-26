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

package org.opencms.xml.templatemapper;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.actions.A_CmsWorkplaceAction;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.components.CmsErrorDialog;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Action for replacing formatters in pages according to a template mapper configuration.<p>
 */
public class CmsTemplateMapperAction extends A_CmsWorkplaceAction {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMapperAction.class);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        try {
            CmsTemplateMapperDialog dialog = new CmsTemplateMapperDialog(context);
            context.start(
                CmsVaadinUtils.getMessageText(Messages.GUI_TEMPLATEMAPPER_DIALOG_TITLE_0),
                dialog,
                DialogWidth.wide);
            dialog.setReportThread(
                new CmsTemplateMappingContentRewriter(context.getCms(), context.getResources().get(0)));
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return "template-mapper";
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        CmsMenuItemVisibilityMode invisible = CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        if (!CmsTemplateMappingContentRewriter.checkConfiguredInModules()) {
            return invisible;
        }

        if (resources.size() != 1) {
            return invisible;
        }
        if (!resources.get(0).isFolder()) {
            return invisible;
        }

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return invisible;
        }

        if (!OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)) {
            return invisible;
        }

        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return Messages.GUI_TEMPLATEMAPPER_MENU_TITLE_0;
    }

}
