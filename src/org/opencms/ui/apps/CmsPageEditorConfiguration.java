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
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.shared.CmsReturnLinkInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsADESessionCache.LastPageBean;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

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

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPageEditorConfiguration.class);

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    @Override
    public String getAppCategory() {

        return "Main";
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

        return new CmsAppVisibilityStatus(true, !cms.getRequestContext().getCurrentProject().isOnlineProject(), null);
    }

    /**
     * Opens the page editor for the current site.<p>
     */
    void openPageEditor() {

        CmsObject cms = A_CmsUI.getCmsObject();
        String siteRoot = cms.getRequestContext().getSiteRoot();
        HttpServletRequest req = CmsVaadinUtils.getRequest();
        CmsADESessionCache cache = CmsADESessionCache.getCache(req, cms);
        LastPageBean lastPage = cache.getLastPage();
        if (lastPage != null) {
            if (cms.getRequestContext().getSiteRoot().equals(lastPage.getSiteRoot())) {
                String returncode = lastPage.getDetailId() != null
                ? lastPage.getPageId() + ":" + lastPage.getDetailId()
                : "" + lastPage.getPageId();
                try {
                    CmsReturnLinkInfo linkInfo = CmsCoreService.internalGetLinkForReturnCode(cms, returncode);
                    A_CmsUI.get().getPage().setLocation(linkInfo.getLink());
                    return;
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    cache.clearLastPage();
                }
            } else {
                // Switching sites clears the last page
                cache.clearLastPage();
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)
            && !OpenCms.getSiteManager().getSharedFolder().equals(siteRoot)) {
            try {
                CmsResource res = cms.readDefaultFile("/");
                if (res != null) {
                    String link = OpenCms.getLinkManager().substituteLink(cms, res);
                    A_CmsUI.get().getPage().setLocation(link);
                    return;
                }
            } catch (CmsException e) {
                LOG.debug("Unable to open page editor for site " + siteRoot + ".", e);
            }

        }
        Notification.show("The page editor is not available for the current site.", Type.WARNING_MESSAGE);
    }

}
