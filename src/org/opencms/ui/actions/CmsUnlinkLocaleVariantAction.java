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
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.ui.sitemap.CmsUnlinkDialog;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Workplace action for the 'Link locale variant' dialog.<p>
 */
public class CmsUnlinkLocaleVariantAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "unlinklocale";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.DEFAULT_DEFAULTFILE);

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUnlinkLocaleVariantAction.class);

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(final I_CmsDialogContext context) {

        try {
            final CmsResource resource = context.getResources().get(0);
            CmsObject cms = context.getCms();
            List<CmsRelation> relations = readOutgoingRelations(cms, resource);
            for (CmsRelation relation : relations) {
                try {
                    CmsResource target = relation.getTarget(cms, CmsResourceFilter.IGNORE_EXPIRATION);
                    CmsUnlinkDialog unlinkDialog = new CmsUnlinkDialog(context, target);
                    context.start(getTitle(), unlinkDialog);
                    break;
                } catch (CmsException e) {
                    LOG.info("No target found for: " + relation, e);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            context.error(e);
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    public String getTitle() {

        return "Unlink locale";
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        CmsMenuItemVisibilityMode visibility = VISIBILITY.getVisibility(cms, resources);
        if (visibility.isInVisible() || visibility.isInActive()) {
            return visibility;
        }
        try {
            List<CmsRelation> relations = readOutgoingRelations(cms, resources.get(0));
            boolean hasRelations = false;
            for (CmsRelation relation : relations) {
                if (!relation.getTargetId().isNullUUID()) {
                    hasRelations = true;
                    break;
                }
            }
            return hasRelations
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Reads the outgoing LOCALE_VARIANT relations for a given resource.<p>
     *
     * @param cms the CMS context
     * @param resource the resource
     * @return the list of relations
     * @throws CmsException if something goes wrong
     */
    List<CmsRelation> readOutgoingRelations(CmsObject cms, CmsResource resource) throws CmsException {

        List<CmsRelation> results = cms.readRelations(
            CmsRelationFilter.relationsFromStructureId(resource.getStructureId()).filterType(
                CmsRelationType.LOCALE_VARIANT));
        return results;
    }

}
