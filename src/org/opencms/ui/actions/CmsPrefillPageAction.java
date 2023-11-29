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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.prefillpage.I_CmsPrefillPageHandler;
import org.opencms.ui.actions.prefillpage.CmsStaticPrefillPageHandler;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Action class for filling in container page content from a 'prefill template' configured via
 * the sitemap configuration.
 */
public class CmsPrefillPageAction extends A_CmsWorkplaceAction implements I_CmsADEAction {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPrefillPageAction.class);

    /** Message key for the context menu - this is not in a message bundle class because it is not defined by default. */
    public static final String GUI_EXPLORER_CONTEXT_PREFILL_PAGE_0 = "GUI_EXPLORER_CONTEXT_PREFILL_PAGE_0";

    /** The sitemap attribute used to configure the prefill handler. */
    private static final String ATTR_PREFILL_HANDLER = "template.prefill.handler";

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        CmsObject cms = context.getCms();
        CmsResource resource = context.getResources().get(0);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
        String prefillHandler = config.getAttribute(ATTR_PREFILL_HANDLER, null);
        I_CmsPrefillPageHandler handler = null;
        if (null == prefillHandler) {
            handler = new CmsStaticPrefillPageHandler();
        } else {
            Class<?> handlerClass;
            try {
                handlerClass = Class.forName(prefillHandler);
                if (I_CmsPrefillPageHandler.class.isAssignableFrom(handlerClass)) {
                    handler = (I_CmsPrefillPageHandler)handlerClass.newInstance();
                }
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e, e);
                } else {
                    LOG.error(e);
                }
            }
        }
        if (handler != null) {
            handler.execute(context);
        } else {
            LOG.error(
                "Failed to execute prefill action with handler "
                    + prefillHandler
                    + ". The handler could not be initialized.");
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsADEAction#getCommandClassName()
     */
    public String getCommandClassName() {

        return "org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction";
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return "template_prefill";
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

        Map<String, String> params = new HashMap<String, String>();
        params.put(CmsGwtConstants.ACTION_PARAM_DIALOG_ID, this.getClass().getName());
        params.put(CmsGwtConstants.PREFILL_MENU_PLACEHOLDER, "true");
        return params;
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getTitle(java.util.Locale)
     */
    @Override
    public String getTitle(Locale locale) {

        // we make the context menu entry label customizable by trying a message key first that isn't
        // defined by default, and using the message key defined in the OpenCms core only as a fallback if that doesn't work.
        CmsWorkplaceMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
        String result = messages.key(GUI_EXPLORER_CONTEXT_PREFILL_PAGE_0, /*allowNull=*/true);
        if (result == null) {
            result = messages.key(org.opencms.workplace.explorer.Messages.GUI_EXPLORER_CONTEXT_PREFILL_PAGE_DEFAULT_0);
        }
        return result;

    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return null;
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

        // check permissions
        if (!CmsStandardVisibilityCheck.DEFAULT.getVisibility(cms, Arrays.asList(resource)).isActive()) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());
        String prefillHandler = config.getAttribute(ATTR_PREFILL_HANDLER, null);
        I_CmsPrefillPageHandler handler = null;
        if (null == prefillHandler) {
            handler = new CmsStaticPrefillPageHandler();
        } else {
            Class<?> handlerClass;
            try {
                handlerClass = Class.forName(prefillHandler);
                if (I_CmsPrefillPageHandler.class.isAssignableFrom(handlerClass)) {
                    handler = (I_CmsPrefillPageHandler)handlerClass.newInstance();
                }
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e, e);
                } else {
                    LOG.error(e);
                }
            }
        }
        if (handler != null) {
            if (handler.isExecutable(context)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
        } else {
            LOG.error(
                "Failed to execute prefill action with handler "
                    + prefillHandler
                    + ". The handler could not be initialized.");
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;

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

        // not used - getTitle is implemented directly
        return null;
    }

}
