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

package org.opencms.gwt;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.gwt.shared.CmsQuickLaunchParams;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsLegacyAppConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsQuickLaunchLocationCache;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;

/**
 * Provides the data for the buttons in the quick launch menu.<p>
 */
public final class CmsQuickLaunchProvider {

    /** The font icon HTML format String. */
    private static final String FONT_ICON_PREFIX = "fonticon:";

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsQuickLaunchProvider.class);

    /**
     * Hiding default constructor.<p>
     */
    private CmsQuickLaunchProvider() {

        // nothing to do
    }

    /**
     * Gets the quick launch data for the current user and context.<p>
     *
     * The context is a string which identifies where the quick launch menu is located
     * @param cms the cms context
     * @param session  the user session
     *
     * @param params the quick launch parameters
     *
     * @return the list of available quick launch items
     */
    public static List<CmsQuickLaunchData> getQuickLaunchData(
        CmsObject cms,
        HttpSession session,
        CmsQuickLaunchParams params) {

        List<CmsQuickLaunchData> result = Lists.newArrayList();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        List<I_CmsWorkplaceAppConfiguration> appConfigs = new ArrayList<I_CmsWorkplaceAppConfiguration>(
            OpenCms.getWorkplaceAppManager().getQuickLaunchConfigurations(cms));

        CmsResource currentPage = null;
        if (params.getPageId() != null) {
            try {
                currentPage = cms.readResource(params.getPageId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        CmsQuickLaunchLocationCache locationCache = CmsQuickLaunchLocationCache.getLocationCache(session);
        for (I_CmsWorkplaceAppConfiguration config : appConfigs) {
            try {
                boolean reload = false;
                String link = null;
                String errorTitle = null;
                String errorMessage = null;
                boolean useLegacyButtonStyle = config instanceof CmsLegacyAppConfiguration;
                if (CmsFileExplorerConfiguration.APP_ID.equals(config.getId())) {
                    String page = locationCache.getFileExplorerLocation(cms.getRequestContext().getSiteRoot());
                    if (page != null) {
                        link = CmsCoreService.getVaadinWorkplaceLink(cms, cms.getRequestContext().addSiteRoot(page));
                    } else {
                        if (cms.existsResource("/", CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                            link = CmsCoreService.getVaadinWorkplaceLink(cms, cms.getRequestContext().getSiteRoot());
                        } else if (currentPage != null) {
                            link = CmsCoreService.getVaadinWorkplaceLink(cms, params.getPageId());
                        } else {
                            errorTitle = config.getName(locale);
                            errorMessage = Messages.get().getBundle(locale).key(
                                Messages.GUI_QUICKLAUNCH_EXPLORER_NOT_ALLOWED_0);
                        }
                    }
                } else if (CmsPageEditorConfiguration.APP_ID.equals(config.getId())) {
                    if (params.isPageContext()) {
                        if ((currentPage != null)
                            && CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME.equals(
                                OpenCms.getResourceManager().getResourceType(currentPage).getTypeName())) {
                            if (params.getSessionPageId() != null) {
                                try {
                                    CmsResource page = cms.readResource(
                                        params.getSessionPageId(),
                                        CmsResourceFilter.IGNORE_EXPIRATION);
                                    // only use the page id from session storage if it's in the current site.
                                    if (CmsStringUtil.isPrefixPath(
                                        cms.getRequestContext().getSiteRoot(),
                                        page.getRootPath())) {
                                        link = OpenCms.getLinkManager().substituteLink(cms, page);
                                    }
                                } catch (CmsVfsResourceNotFoundException | CmsSecurityException e) {
                                    LOG.info(e.getLocalizedMessage(), e);
                                } catch (Exception e) {
                                    LOG.error(e.getLocalizedMessage(), e);
                                }
                            }
                            if (link == null) {
                                String page = locationCache.getPageEditorLocation(
                                    cms,
                                    cms.getRequestContext().getSiteRoot());
                                if (page != null) {
                                    link = OpenCms.getLinkManager().substituteLink(cms, page);
                                } else {
                                    reload = true;
                                }
                            }
                        } else {
                            reload = true;
                        }
                    } else if (params.isSitemapContext()) {
                        String page = locationCache.getPageEditorLocation(cms, cms.getRequestContext().getSiteRoot());
                        if (page == null) {
                            page = locationCache.getSitemapEditorLocation(cms.getRequestContext().getSiteRoot());
                        }
                        if (page != null) {
                            link = OpenCms.getLinkManager().substituteLink(cms, page);
                        }
                    }
                } else if (CmsSitemapEditorConfiguration.APP_ID.equals(config.getId())) {
                    if (params.isSitemapContext()) {
                        reload = true;
                    } else if (params.isPageContext()) {
                        String sitemapLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                            cms,
                            CmsADEManager.PATH_SITEMAP_EDITOR_JSP);
                        String page = locationCache.getPageEditorLocation(cms, cms.getRequestContext().getSiteRoot());
                        if ((page != null) && page.contains(CmsADEManager.CONTENT_FOLDER_NAME)) {
                            page = cms.getRequestContext().getUri();
                        }
                        link = sitemapLink + "?path=" + page;
                    }
                } else {
                    link = CmsVaadinUtils.getWorkplaceLink(config.getId());
                }
                Resource icon = config.getIcon();
                String imageLink = "";
                if (icon instanceof ExternalResource) {
                    imageLink = ((ExternalResource)icon).getURL();
                } else if (icon instanceof FontIcon) {
                    imageLink = FONT_ICON_PREFIX + ((FontIcon)icon).getHtml();
                }

                String name = config.getName(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                CmsAppVisibilityStatus visibility = config.getVisibility(cms);
                if (!visibility.isActive()) {
                    errorTitle = name;
                    errorMessage = visibility.getHelpText();
                }
                CmsQuickLaunchData data = new CmsQuickLaunchData(
                    link,
                    name,
                    imageLink,
                    config.getButtonStyle(),
                    errorTitle,
                    errorMessage,
                    useLegacyButtonStyle,
                    reload);
                result.add(data);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        }
        return result;
    }

}
