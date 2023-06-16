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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsTemplateContextProvider;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Template context selection action.
 *
 * <p>This is handled specially by the client side code.
 */
public class CmsTemplateContextsAction extends A_CmsWorkplaceAction implements I_CmsADEAction {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateContextsAction.class);

    /** Special integer that indicates one of several positions in the context menu. Currently can only be 0 or 1. */
    private int m_menuPosition;

    /**
     * Creates a new instance.
     *
     * @param menuPosition the position in the context menu
     */
    public CmsTemplateContextsAction(int menuPosition) {

        if ((menuPosition != 0) && (menuPosition != 1)) {
            throw new IllegalArgumentException("Menu position must be 0 or 1");
        }
        m_menuPosition = menuPosition;

    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        // not supported
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return CmsGwtConstants.TEMPLATECONTEXT_MENU_PLACEHOLDER;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return CmsGwtConstants.ACTION_TEMPLATECONTEXTS + "_" + m_menuPosition;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getJspPath()
     */
    public String getJspPath() {

        return null;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getParams()
     */
    public Map<String, String> getParams() {

        return null;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        if (!AdeContext.pageeditor.name().equals(context.getAppId())) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        List<CmsResource> resources = context.getResources();
        if (resources.size() != 1) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        CmsObject cms = context.getCms();
        CmsResource resource = resources.get(0);

        try {
            List<CmsProperty> properties = cms.readPropertyObjects(resource, true);
            // this menu entry is only available in the container page editor, so we know we have to use the template property,
            // not template-elements
            CmsProperty templateProp = CmsProperty.get(CmsPropertyDefinition.PROPERTY_TEMPLATE, properties);
            if ((templateProp != null) && !templateProp.isNullProperty()) {
                String propertyValue = templateProp.getValue();
                if (CmsTemplateContextManager.hasPropertyPrefix(propertyValue)) {
                    I_CmsTemplateContextProvider provider = OpenCms.getTemplateContextManager().getTemplateContextProvider(
                        CmsTemplateContextManager.removePropertyPrefix(propertyValue));
                    if (provider != null) {
                        if (provider.getMenuPosition() != m_menuPosition) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                        }
                        if (!provider.shouldShowContextMenuOption(cms)) {
                            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);

        }
        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;

    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#isAdeSupported()
     */
    public boolean isAdeSupported() {

        return true;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitleKey()
     */
    @Override
    protected String getTitleKey() {

        return "TEMPLATECONTEXTS";
    }
}
