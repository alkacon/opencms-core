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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
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
 * The sitemap editor app configuration.<p>
 */
public class CmsSitemapEditorConfiguration extends A_CmsWorkplaceAppConfiguration implements I_CmsHasAppLaunchCommand {

    /** The app id. */
    public static final String APP_ID = "sitemapeditor";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapEditorConfiguration.class);

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

        throw new IllegalStateException("The sitemap app should be launched through the app launch command only.");
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasAppLaunchCommand#getAppLaunchCommand()
     */
    public Runnable getAppLaunchCommand() {

        return new Runnable() {

            public void run() {

                openSitemapEditor();
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

        return Messages.get().getBundle(locale).key(Messages.GUI_SITEMAP_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return new ExternalResource(OpenCmsTheme.getImageLink("apps/sitemap.png"));
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

        return Messages.get().getBundle(locale).key(Messages.GUI_SITEMAP_TITLE_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    @Override
    public int getOrder() {

        return 2;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getPriority()
     */
    @Override
    public int getPriority() {

        return I_CmsWorkplaceAppConfiguration.DEFAULT_PRIORIY;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    @Override
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        if (OpenCms.getRoleManager().hasRole(cms, CmsRole.EDITOR)) {
            String siteRoot = cms.getRequestContext().getSiteRoot();
            boolean active = CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot);
            HttpServletRequest req = CmsVaadinUtils.getRequest();
            String message = null;
            if (active) {
                if (req != null) {
                    // this is a VAADIN UI request
                    active = getPath(cms, req.getSession()) != null;
                    if (!active) {
                        message = CmsVaadinUtils.getMessageText(Messages.GUI_SITEMAP_COULD_NOT_BE_DETERMINED_0);
                    }
                }
            } else {
                message = CmsVaadinUtils.getMessageText(Messages.GUI_SITEMAP_NOT_AVAILABLE_0);
            }
            return new CmsAppVisibilityStatus(true, active, message);
        } else {
            return CmsAppVisibilityStatus.INVISIBLE;
        }
    }

    /**
     * Opens the sitemap editor for the current site.<p>
     */
    void openSitemapEditor() {

        CmsObject cms = A_CmsUI.getCmsObject();
        String siteRoot = cms.getRequestContext().getSiteRoot();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)) {
            String path = getPath(cms, A_CmsUI.get().getHttpSession());
            if (path != null) {
                try {
                    CmsAppWorkplaceUi ui = CmsAppWorkplaceUi.get();
                    if (ui.beforeViewChange(
                        new ViewChangeEvent(ui.getNavigator(), ui.getCurrentView(), null, APP_ID, null))) {
                        CmsResource res = cms.readResource(CmsADEManager.PATH_SITEMAP_EDITOR_JSP);
                        String link = OpenCms.getLinkManager().substituteLink(cms, res);
                        A_CmsUI.get().getPage().setLocation(link + "?path=" + path);
                    }
                    return;
                } catch (CmsException e) {
                    LOG.debug("Unable to open sitemap editor.", e);
                }
            }
        }
        Notification.show(CmsVaadinUtils.getMessageText(Messages.GUI_SITEMAP_NOT_AVAILABLE_0), Type.WARNING_MESSAGE);
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
        String page = locationCache.getFileExplorerLocation(cms.getRequestContext().getSiteRoot());
        if (page != null) {
            CmsADEConfigData conf = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.getRequestContext().addSiteRoot(page));
            if ((conf == null) || (conf.getBasePath() == null)) {
                page = null;
            } else {
                page = cms.getRequestContext().removeSiteRoot(conf.getBasePath());
            }
        }
        if (page == null) {
            page = locationCache.getSitemapEditorLocation(cms.getRequestContext().getSiteRoot());
        }
        return page;
    }

}
