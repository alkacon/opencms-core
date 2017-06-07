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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.actions.I_CmsWorkplaceAction;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;

/**
 * Separate UI for VAADIN based dialog embedded into a GWT module.<p>
 */
@Theme("opencms")
public class CmsEmbeddedDialogsUI extends A_CmsUI {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEmbeddedDialogsUI.class);

    /** The dialogs path fragment. */
    public static final String DIALOGS_PATH = "dialogs/";

    /** The serial version id. */
    private static final long serialVersionUID = 1201184887611215370L;

    /**
     * The dialog context of the currently opened dialog.<p>
     */
    CmsEmbeddedDialogContext m_currentContext;

    /**
     * Returns the context path for embedded dialogs.<p>
     *
     * @return the context path for embedded dialogs
     */
    public static String getEmbeddedDialogsContextPath() {

        return CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getWorkplaceContext(), DIALOGS_PATH);
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getLocale()
     */
    @Override
    public Locale getLocale() {

        CmsObject cms = getCmsObject();
        return OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
    }

    /**
     * @see org.opencms.ui.A_CmsUI#reload()
     */
    @Override
    public void reload() {

        if (m_currentContext != null) {
            m_currentContext.reload();
        }
    }

    /**
     * @see org.opencms.ui.A_CmsUI#init(com.vaadin.server.VaadinRequest)
     */
    @Override
    protected void init(VaadinRequest request) {

        super.init(request);
        Throwable t = null;
        String errorMessage = null;
        try {
            OpenCms.getRoleManager().checkRole(getCmsObject(), CmsRole.ELEMENT_AUTHOR);
            try {
                String resources = request.getParameter("resources");
                List<CmsResource> resourceList;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resources)) {
                    resourceList = new ArrayList<CmsResource>();
                    String[] resIds = resources.split(";");
                    for (int i = 0; i < resIds.length; i++) {
                        if (CmsUUID.isValidUUID(resIds[i])) {
                            resourceList.add(
                                getCmsObject().readResource(
                                    new CmsUUID(resIds[i]),
                                    CmsResourceFilter.IGNORE_EXPIRATION));
                        }

                    }
                } else {
                    resourceList = Collections.<CmsResource> emptyList();
                }
                String typeParam = request.getParameter("contextType");

                ContextType type;
                String appId = "";
                try {
                    type = ContextType.valueOf(typeParam);
                    if (ContextType.containerpageToolbar.equals(type)) {
                        appId = CmsPageEditorConfiguration.APP_ID;
                    } else if (ContextType.sitemapToolbar.equals(type)) {
                        appId = CmsSitemapEditorConfiguration.APP_ID;
                    }
                } catch (Exception e) {
                    type = ContextType.appToolbar;
                    LOG.error("Could not parse context type parameter " + typeParam);
                }

                m_currentContext = new CmsEmbeddedDialogContext(appId, type, resourceList);
                I_CmsWorkplaceAction action = getAction(request);
                if (action.isActive(m_currentContext)) {
                    action.executeAction(m_currentContext);
                } else {
                    errorMessage = CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0);
                }
            } catch (Throwable e) {
                t = e;
                errorMessage = CmsVaadinUtils.getMessageText(
                    org.opencms.ui.dialogs.Messages.ERR_DAILOG_INSTANTIATION_FAILED_1,
                    request.getPathInfo());
            }
        } catch (CmsRoleViolationException ex) {
            t = ex;
            errorMessage = CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0);
        }
        if (errorMessage != null) {
            CmsErrorDialog.showErrorDialog(errorMessage, t, new Runnable() {

                public void run() {

                    m_currentContext = new CmsEmbeddedDialogContext("", null, Collections.<CmsResource> emptyList());
                    m_currentContext.finish(null);
                }
            });
        }
    }

    /**
     * Returns the dialog action matching the given request.<p>
     *
     * @param request the request
     *
     * @return the dialog action
     *
     * @throws Exception in case instantiating the action fails
     */
    private I_CmsWorkplaceAction getAction(VaadinRequest request) throws Exception {

        String dialogId = getDialogId(request);
        @SuppressWarnings("unchecked")
        Class<I_CmsWorkplaceAction> actionClass = (Class<I_CmsWorkplaceAction>)getClass().getClassLoader().loadClass(
            dialogId);
        return actionClass.newInstance();
    }

    /**
     * Returns the dialog id extracted from the requested path.<p>
     *
     * @param request the request
     *
     * @return the id
     */
    private String getDialogId(VaadinRequest request) {

        String path = request.getPathInfo();
        // remove the leading slash
        return path != null ? path.substring(1) : null;
    }
}
