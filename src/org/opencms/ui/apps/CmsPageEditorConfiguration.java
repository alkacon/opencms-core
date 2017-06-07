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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspTagEnableAde;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * The page editor app configuration.<p>
 */
public class CmsPageEditorConfiguration extends A_CmsWorkplaceAppConfiguration implements I_CmsHasAppLaunchCommand {

    /** The app id. */
    public static final String APP_ID = "pageeditor";

    private static final Log LOG = CmsLog.getLog(CmsPageEditorConfiguration.class);

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    @Override
    public String getAppCategory() {

        return CmsWorkplaceAppManager.MAIN_CATEGORY_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        throw new IllegalStateException("The editor app should be launched through the app launch command only.");
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasAppLaunchCommand#getAppLaunchCommand()
     */
    public Runnable getAppLaunchCommand() {

        return new Runnable() {

            public void run() {

                openPageEditor();
            }
        };
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getButtonStyle()
     */
    @Override
    public String getButtonStyle() {

        return I_CmsAppButtonProvider.BUTTON_STYLE_TRANSPARENT;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    @Override
    public String getHelpText(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_PAGEEDITOR_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return new ExternalResource(OpenCmsTheme.getImageLink("apps/editor.png"));
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    public String getId() {

        return APP_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    @Override
    public String getName(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_PAGEEDITOR_TITLE_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    @Override
    public int getOrder() {

        return 1;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    @Override
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        boolean active = !cms.getRequestContext().getCurrentProject().isOnlineProject()
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot());
        HttpServletRequest req = CmsVaadinUtils.getRequest();
        String message = null;
        if (active) {
            if (req != null) {
                // this is a VAADIN UI request
                active = getPath(cms, req.getSession()) != null;
                if (!active) {
                    message = CmsVaadinUtils.getMessageText(Messages.GUI_PAGE_EDITOR_PLEASE_SELECT_PAGE_0);
                }
            }
        } else {
            message = CmsVaadinUtils.getMessageText(Messages.GUI_PAGE_EDITOR_NOT_AVAILABLE_0);
        }
        return new CmsAppVisibilityStatus(true, active, message);
    }

    /**
     * Opens the page editor for the current site.<p>
     */
    void openPageEditor() {

        CmsAppWorkplaceUi ui = CmsAppWorkplaceUi.get();
        if (ui.beforeViewChange(new ViewChangeEvent(ui.getNavigator(), ui.getCurrentView(), null, APP_ID, null))) {
            CmsObject cms = A_CmsUI.getCmsObject();
            HttpServletRequest req = CmsVaadinUtils.getRequest();
            if (req == null) {
                // called from outside the VAADIN UI, not allowed
                throw new RuntimeException("Wrong usage, this can not be called from outside a VAADIN UI.");
            }
            CmsJspTagEnableAde.removeDirectEditFlagFromSession(req.getSession());
            String page = getPath(cms, req.getSession());
            if (page != null) {
                A_CmsUI.get().getPage().setLocation(OpenCms.getLinkManager().substituteLink(cms, page));

            } else {
                String message = CmsVaadinUtils.getMessageText(Messages.GUI_PAGE_EDITOR_NOT_AVAILABLE_0);
                Notification.show(message, Type.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Returns the page editor path to open.<p>
     *
     * @param cms the cms context
     * @param session the user session
     *
     * @return the path or <code>null</code>
     */
    private String getPath(CmsObject cms, HttpSession session) {

        CmsQuickLaunchLocationCache locationCache = CmsQuickLaunchLocationCache.getLocationCache(session);
        String page = locationCache.getPageEditorLocation(cms.getRequestContext().getSiteRoot());
        if (page == null) {
            try {
                CmsResource mainDefaultFile = cms.readDefaultFile("/");
                if (mainDefaultFile != null) {
                    page = cms.getSitePath(mainDefaultFile);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return page;
    }

}
