/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.components;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.Messages;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;

import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

/**
 * Displays the resource icon and state and lock info.<p>
 * Important: To avoid issues with click event propagation within tables, we are required to extent the Label component.
 */
public class CmsResourceIcon extends Label {

    /** Enum used to control icon display style. */
    public enum IconMode {
        /** locale compare mode. */
        localeCompare,

        /** sitemap selection mode. */
        sitemapSelect;
    }

    /** The changed icon class. */
    public static final String ICON_CLASS_CHANGED = "oc-icon-16-overlay-changed";

    /** The other user lock icon class. */
    public static final String ICON_CLASS_LOCK_OTHER = "oc-icon-16-lock-other";

    /** The own user lock icon class. */
    public static final String ICON_CLASS_LOCK_OWN = "oc-icon-16-lock-own";

    /** The publish lock icon class. */
    public static final String ICON_CLASS_LOCK_PUBLISH = "oc-icon-16-lock-publish";

    /** The shared lock icon class. */
    public static final String ICON_CLASS_LOCK_SHARED = "oc-icon-16-lock-shared";

    /** The sibling icon class. */
    public static final String ICON_CLASS_SIBLING = "oc-icon-16-overlay-sibling";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceIcon.class);

    /** The serial version id. */
    private static final long serialVersionUID = 5031544534869165777L;

    /**
     * Constuctor.<p>
     * To be used in declarative layouts. Make sure to call initContent later on.<p>
     */
    public CmsResourceIcon() {

        setPrimaryStyleName(OpenCmsTheme.RESOURCE_ICON);
        setContentMode(ContentMode.HTML);
    }

    /**
     * Constructor.<p>
     *
     * @param resUtil the resource util
     * @param state the resource state
     * @param showLocks <code>true</code> to show the resource locks
     */
    public CmsResourceIcon(CmsResourceUtil resUtil, CmsResourceState state, boolean showLocks) {

        this();
        initContent(resUtil, state, showLocks, true);
    }

    /**
     * Returns the default file type or detail type for the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the container page resource
     *
     * @return the detail content type
     */
    public static String getDefaultFileOrDetailType(CmsObject cms, CmsResource resource) {

        String type = null;

        if (resource.isFolder()
            && !(OpenCms.getResourceManager().getResourceType(resource) instanceof CmsResourceTypeFolderExtended)
            && !CmsJspNavBuilder.isNavLevelFolder(cms, resource)) {
            try {
                CmsResource defaultFile = cms.readDefaultFile(resource, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                if (defaultFile != null) {
                    type = getDetailType(cms, defaultFile, resource);
                    if (type == null) {
                        type = OpenCms.getResourceManager().getResourceType(defaultFile).getTypeName();
                    }

                }
            } catch (CmsSecurityException e) {
                // ignore
            }
        } else if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            type = getDetailType(cms, resource, null);

        }
        return type;

    }

    /**
     * Returns the detail content type for container pages that may be detail pages.<p>
     *
     * @param cms the cms context
     * @param detailPage the container page resource
     * @param parentFolder the parent folder or <code>null</code>
     *
     * @return the detail content type
     */
    public static String getDetailType(CmsObject cms, CmsResource detailPage, CmsResource parentFolder) {

        String type = null;
        try {
            if (OpenCms.getADEManager().isDetailPage(cms, detailPage)) {
                List<CmsDetailPageInfo> detailPages = OpenCms.getADEManager().getRawDetailPages(cms);
                if (parentFolder == null) {
                    parentFolder = cms.readParentFolder(detailPage.getStructureId());
                }
                for (CmsDetailPageInfo info : detailPages) {
                    if (info.getId().equals(detailPage.getStructureId())
                        || info.getId().equals(parentFolder.getStructureId())) {
                        type = info.getType();
                        if (type.startsWith(CmsDetailPageInfo.FUNCTION_PREFIX)) {
                            type = CmsXmlDynamicFunctionHandler.TYPE_FUNCTION;
                        }
                        break;
                    }
                }
            }
        } catch (CmsException e) {
            // parent folder can't be read, ignore
        }
        return type;
    }

    /**
     * Returns the icon HTML.<p>
     *
     * @param resUtil the resource util for the resource
     * @param state the resource state
     * @param showLocks <code>true</code> to show lock state overlay
     *
     * @return the icon HTML
     */
    public static String getIconHTML(CmsResourceUtil resUtil, CmsResourceState state, boolean showLocks) {

        return "<span class=\""
            + OpenCmsTheme.RESOURCE_ICON
            + "\">"
            + getIconInnerHTML(resUtil, state, showLocks, true)
            + "</span>";
    }

    /**
     * Gets the resource icon for a resource for use in a CmsResourceInfo widget when used in a sitemap context.<p>
     *
     * @param cms the CMS context
     * @param resource a resource
     * @param iconMode the icon mode
     * @return the path for the resource icon
     */
    public static Resource getSitemapResourceIcon(CmsObject cms, CmsResource resource, IconMode iconMode) {

        CmsResource defaultFile = null;
        List<CmsResource> resourcesForType = Lists.newArrayList();
        resourcesForType.add(resource);
        boolean skipDefaultFile = (iconMode == IconMode.sitemapSelect)
            && OpenCms.getResourceManager().matchResourceType(
                CmsResourceTypeFolderSubSitemap.TYPE_SUBSITEMAP,
                resource.getTypeId());
        if (resource.isFolder() && !skipDefaultFile) {

            try {
                defaultFile = cms.readDefaultFile(resource, CmsResourceFilter.IGNORE_EXPIRATION);
                if (defaultFile != null) {
                    resourcesForType.add(0, defaultFile);
                }
            } catch (Exception e) {
                // Shouldn't normally happen - readDefaultFile returns null instead of throwing an exception when it doesn't find a default file
            }
        }
        if (CmsJspNavBuilder.isNavLevelFolder(cms, resource)) {
            return new CmsCssIcon(CmsExplorerTypeSettings.ICON_STYLE_NAV_LEVEL_BIG);
        }
        CmsResource maybePage = resourcesForType.get(0);
        if (CmsResourceTypeXmlContainerPage.isContainerPage(maybePage)) {
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, maybePage.getRootPath());
            for (CmsDetailPageInfo realInfo : config.getAllDetailPages(true)) {
                if (realInfo.getUri().equals(maybePage.getRootPath())
                    || realInfo.getUri().equals(CmsResource.getParentFolder(maybePage.getRootPath()))) {
                    CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                        realInfo.getIconType());
                    if (settings != null) {
                        return CmsResourceUtil.getBigIconResource(settings, resource.getName());
                    }
                }
            }
        }

        Resource result = null;
        for (CmsResource res : resourcesForType) {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res);
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            if (settings != null) {
                result = CmsResourceUtil.getBigIconResource(settings, res.getName());
                break;
            }
        }
        return result;
    }

    /**
     * Returns the tree caption HTML including the resource icon.<p>
     *
     * @param resourceName the resource name to display
     * @param resUtil the resource util for the resource
     * @param state the resource state
     * @param showLocks <code>true</code> to show lock state overlay
     *
     * @return the icon HTML
     */
    public static String getTreeCaptionHTML(
        String resourceName,
        CmsResourceUtil resUtil,
        CmsResourceState state,
        boolean showLocks) {

        return CmsResourceIcon.getIconHTML(resUtil, null, false)
            + "<span class=\"o-tree-caption\">"
            + resourceName
            + "</span>";
    }

    /**
     * Returns the icon inner HTML.<p>
     *
     * @param resUtil the resource util for the resource
     * @param state the resource state
     * @param showLocks <code>true</code> to show lock state overlay
     * @param showDetailIcon <code>true</code> to show the detail icon overlay
     *
     * @return the icon inner HTML
     */
    private static String getIconInnerHTML(
        CmsResourceUtil resUtil,
        CmsResourceState state,
        boolean showLocks,
        boolean showDetailIcon) {

        Resource iconResource = resUtil.getBigIconResource();

        return getIconInnerHTML(resUtil, iconResource, state, showLocks, showDetailIcon);
    }

    /**
     * Returns the icon inner HTML.<p>
     *
     * @param resUtil the resource util for the resource
     * @param iconResource the icon path
     * @param state the resource state
     * @param showLocks <code>true</code> to show lock state overlay
     * @param showDetailIcon <code>true</code> to show the detail icon overlay
     *
     * @return the icon inner HTML
     */
    private static String getIconInnerHTML(
        CmsResourceUtil resUtil,
        Resource iconResource,
        CmsResourceState state,
        boolean showLocks,
        boolean showDetailIcon) {

        String content;
        if (iconResource instanceof FontIcon) {
            content = ((FontIcon)iconResource).getHtml();
        } else if (iconResource instanceof ExternalResource) {
            content = "<img src=\"" + ((ExternalResource)iconResource).getURL() + "\" />";
        } else {
            content = "";
        }

        boolean isNavLevel = false;

        if (resUtil != null) {
            if (showDetailIcon && !isNavLevel) {

                if (resUtil.getResource().isFolder()) {
                    String detailType = getDefaultFileOrDetailType(resUtil.getCms(), resUtil.getResource());
                    if (detailType != null) {
                        String smallIconUri = getSmallTypeIconHTML(detailType, false);
                        if (smallIconUri != null) {
                            content += smallIconUri;
                        }
                    }
                } else if (CmsResourceTypeXmlContainerPage.isContainerPage(resUtil.getResource())) {
                    String detailType = getDefaultFileOrDetailType(resUtil.getCms(), resUtil.getResource());
                    if (detailType != null) {
                        String smallIconUri = getSmallTypeIconHTML(detailType, true);
                        if (smallIconUri != null) {
                            content += smallIconUri;
                        }
                    }

                }
            }
            if (showLocks) {
                String lockIcon;
                String message = null;
                if (resUtil.getLock().getSystemLock().isPublish()) {
                    lockIcon = OpenCmsTheme.LOCK_PUBLISH + " " + ICON_CLASS_LOCK_PUBLISH;
                    message = CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.explorer.Messages.GUI_PUBLISH_TOOLTIP_0);
                } else {
                    switch (resUtil.getLockState()) {
                        case 1:
                            lockIcon = OpenCmsTheme.LOCK_OTHER + " " + ICON_CLASS_LOCK_OTHER;
                            break;

                        case 2:
                            lockIcon = OpenCmsTheme.LOCK_SHARED + " " + ICON_CLASS_LOCK_SHARED;
                            break;
                        case 3:
                            lockIcon = OpenCmsTheme.LOCK_USER + " " + ICON_CLASS_LOCK_OWN;
                            break;
                        default:
                            lockIcon = null;
                    }
                    if (lockIcon != null) {
                        message = CmsVaadinUtils.getMessageText(
                            Messages.GUI_EXPLORER_LIST_ACTION_LOCK_NAME_2,
                            resUtil.getLockedByName(),
                            resUtil.getLockedInProjectName());
                    }
                }
                if (lockIcon != null) {
                    content += getOverlaySpan(lockIcon, message);
                }
            }
        }
        if (state != null) {
            String title = resUtil != null
            ? CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_USER_LAST_MODIFIED_0)
                + " "
                + resUtil.getUserLastModified()
            : null;
            if (state.isChanged() || state.isDeleted()) {
                content += getOverlaySpan(OpenCmsTheme.STATE_CHANGED + " " + ICON_CLASS_CHANGED, title);
            } else if (state.isNew()) {
                content += getOverlaySpan(OpenCmsTheme.STATE_NEW + " " + ICON_CLASS_CHANGED, title);
            }
        }
        if ((resUtil != null) && (resUtil.getLinkType() == 1)) {
            content += getOverlaySpan(OpenCmsTheme.SIBLING + " " + ICON_CLASS_SIBLING, null);
        }
        return content;
    }

    /**
     * Generates an overlay icon span.<p>
     *
     * @param title the span title
     * @param cssClass the CSS class
     *
     * @return the span element string
     */
    private static String getOverlaySpan(String cssClass, String title) {

        StringBuffer result = new StringBuffer();
        result.append("<span class=\"").append(cssClass).append("\"");
        if (title != null) {
            result.append(" title=\"").append(title).append("\"");
        }
        result.append("></span>");
        return result.toString();
    }

    /**
     * Returns the URI of the small resource type icon for the given type.<p>
     *
     * @param type the resource type name
     * @param isPageOverlay <code>true</code> in case this is a page overlay and not a folder overlay
     *
     * @return the icon URI
     */
    private static String getSmallTypeIconHTML(String type, boolean isPageOverlay) {

        CmsExplorerTypeSettings typeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);
        if ((typeSettings == null) && LOG.isWarnEnabled()) {
            LOG.warn("Could not read explorer type settings for " + type);
        }
        String result = null;
        String overlayClass = isPageOverlay ? "o-page-icon-overlay" : "o-icon-overlay";
        if (typeSettings != null) {
            if (typeSettings.getSmallIconStyle() != null) {
                result = "<span class=\"v-icon "
                    + overlayClass
                    + " "
                    + typeSettings.getSmallIconStyle()
                    + "\">&nbsp;</span>";
            } else if (typeSettings.getIcon() != null) {
                result = "<img src=\""
                    + CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + typeSettings.getIcon())
                    + "\" class=\""
                    + overlayClass
                    + "\" />";
            } else {
                result = "<span class=\"v-icon "
                    + overlayClass
                    + " "
                    + CmsExplorerTypeSettings.ICON_STYLE_DEFAULT_SMALL
                    + "\">&nbsp;</span>";
            }
        }
        return result;
    }

    /**
     * Initializes the content.<p>
     *
     * @param resUtil the resource util
     * @param state the resource state
     * @param showLocks <code>true</code> to show the resource locks
     * @param showDetailIcon <code>true</code> to show the detail icon overlay
     */
    public void initContent(
        CmsResourceUtil resUtil,
        CmsResourceState state,
        boolean showLocks,
        boolean showDetailIcon) {

        setValue(getIconInnerHTML(resUtil, state, showLocks, showDetailIcon));
    }

    /**
     * Initializes the content.<p>
     *
     * @param resUtil the resource util
     * @param iconResource the icon path
     * @param state the resource state
     * @param showLocks <code>true</code> to show the resource locks
     * @param showDetailIcon <code>true</code> to show the detail icon overlay
     */
    public void initContent(
        CmsResourceUtil resUtil,
        Resource iconResource,
        CmsResourceState state,
        boolean showLocks,
        boolean showDetailIcon) {

        setValue(getIconInnerHTML(resUtil, iconResource, state, showLocks, showDetailIcon));
    }
}
