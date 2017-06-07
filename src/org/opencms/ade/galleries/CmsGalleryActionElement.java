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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWorkplace;

import java.util.Arrays;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Gallery action used to generate the gallery dialog.<p>
 *
 * see jsp file <tt>/system/modules/org.opencms.ade.galleries/testVfs.jsp</tt>.<p>
 *
 * @since 8.0.0
 */
public class CmsGalleryActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.galleries";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.galleries.name();

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryActionElement.class);

    /** The gallery mode. */
    private GalleryMode m_galleryMode;

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGalleryActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

        try {
            m_galleryMode = GalleryMode.valueOf(
                getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE).trim());
        } catch (Exception e) {
            m_galleryMode = GalleryMode.view;
            LOG.debug("Could not parse gallery mode parameter.", e);
        }
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        return "";
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(export(m_galleryMode));
        sb.append(exportCloseLink());
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        return sb.toString();
    }

    /**
     * Returns the serialized initial data for gallery dialog within the container-page editor.<p>
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    public String exportForContainerpage() throws Exception {

        return ClientMessages.get().export(getRequest());
    }

    /**
     * Exports the gallery messages for widget use.<p>
     *
     * @return the gallery messages
     */
    public String exportWidget() {

        return ClientMessages.get().export(getRequest());
    }

    /**
     * Returns the editor title.<p>
     *
     * @return the editor title
     */
    public String getTitle() {

        return Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_GALLERIES_TITLE_0);
    }

    /**
     * Returns if the current gallery mode is the editor mode (used inside a rich text editor).<p>
     *
     * @return <code>true</code> if the gallery was opened from the editor
     */
    public boolean isEditorMode() {

        return m_galleryMode == GalleryMode.editor;
    }

    /**
     * Returns true if the gallery mode is set to 'view'.
     *
     * @return true if the gallery mode is 'view'
     */
    public boolean isViewMode() {

        return m_galleryMode == GalleryMode.view;
    }

    /**
     * Returns if the current gallery mode is the widget mode (used within xml-content editor etc.).<p>
     *
     * @return <code>true</code> if the gallery was opened as a widget
     */
    public boolean isWidgetMode() {

        return m_galleryMode == GalleryMode.widget;
    }

    /**
     * Uses the request parameters of the current request to create a gallery configuration object.<p>
     *
     * @param galleryMode the gallery mode
     *
     * @return the gallery configuration
     */
    private CmsGalleryConfiguration createGalleryConfigurationFromRequest(GalleryMode galleryMode) {

        CmsGalleryConfiguration conf = new CmsGalleryConfiguration();
        conf.setGalleryMode(galleryMode);
        conf.setReferencePath(getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH));
        conf.setGalleryPath(getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_GALLERY_PATH));
        conf.setCurrentElement(getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT));
        String resourceTypes = getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_RESOURCE_TYPES);
        String useLinkDefaultTypes = getRequest().getParameter(
            I_CmsGalleryProviderConstants.PARAM_USE_LINK_DEFAULT_TYPES);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourceTypes)) {
            conf.setResourceTypes(Arrays.asList(resourceTypes.split(",")));
        }
        if (Boolean.parseBoolean(useLinkDefaultTypes)) {
            try {
                CmsObject cms = getCmsObject();
                CmsResource referenceResource = cms.readResource(
                    conf.getReferencePath(),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                String searchTypes = CmsVfsFileWidget.getDefaultSearchTypes(cms, referenceResource);
                conf.setSearchTypes(CmsStringUtil.splitAsList(searchTypes, ","));
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        String galleryTypes = getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_GALLERY_TYPES);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(galleryTypes)) {
            conf.setGalleryTypes(galleryTypes.split(","));
        }
        String tabs = getRequest().getParameter(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(tabs)) {
            conf.setTabConfiguration(CmsGalleryTabConfiguration.resolve(tabs));
        } else {
            conf.setTabConfiguration(CmsGalleryTabConfiguration.getDefault());
        }
        String galleryStoragePrefix = getRequest().getParameter(
            I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(galleryStoragePrefix)) {
            galleryStoragePrefix = "";
        }
        conf.setGalleryStoragePrefix(galleryStoragePrefix);
        return conf;
    }

    /**
     * Returns the serialized initial data for gallery dialog depending on the given mode.<p>
     *
     * @param galleryMode the gallery mode
     *
     * @return the data
     *
     * @throws Exception if something goes wrong
     */
    private String export(GalleryMode galleryMode) throws Exception {

        CmsGalleryConfiguration conf = createGalleryConfigurationFromRequest(galleryMode);
        CmsGalleryDataBean data = CmsGalleryService.getInitialSettings(getRequest(), conf);
        CmsGallerySearchBean search = null;
        if (GalleryTabId.cms_tab_results.equals(data.getStartTab())) {
            search = CmsGalleryService.getSearch(getRequest(), data);
        }
        Set<String> folderFilter = data.getStartFolderFilter();
        if ((folderFilter != null) && !folderFilter.isEmpty()) {
            data.setVfsPreloadData(CmsGalleryService.generateVfsPreloadData(getCmsObject(), null, folderFilter));
        }
        if ((search != null) && (search.getScope() != null) && (search.getScope() != data.getScope())) {
            // default selected scope option should be the one for which the search has been actually performed
            data.setScope(search.getScope());
        } else if ((search != null) && (search.getScope() == null)) {
            data.setScope(OpenCms.getWorkplaceManager().getGalleryDefaultScope());
        }

        StringBuffer sb = new StringBuffer();
        sb.append(
            exportDictionary(
                CmsGalleryDataBean.DICT_NAME,
                I_CmsGalleryService.class.getMethod("getInitialSettings", CmsGalleryConfiguration.class),
                data));
        sb.append(
            exportDictionary(
                CmsGallerySearchBean.DICT_NAME,
                I_CmsGalleryService.class.getMethod("getSearch", CmsGalleryDataBean.class),
                search));
        return sb.toString();
    }

    /**
     * Returns a javascript tag that contains a variable deceleration that has the close link as value.<p>
     *
     * @return a javascript tag that contains a variable deceleration that has the close link as value
     */
    private String exportCloseLink() {

        String closeLink = null;
        if (getRequest().getAttribute(I_CmsGalleryProviderConstants.ATTR_CLOSE_LINK) != null) {
            closeLink = (String)getRequest().getAttribute(I_CmsGalleryProviderConstants.ATTR_CLOSE_LINK);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(closeLink)) {
            closeLink = CmsWorkplace.FILE_EXPLORER_FILELIST;
        }

        StringBuffer sb = new StringBuffer();
        // var closeLink = '/system/workplace/views/explorer/explorer_files.jsp';
        sb.append(wrapScript("var ", I_CmsGalleryProviderConstants.ATTR_CLOSE_LINK, " = \'", link(closeLink), "\';"));
        return sb.toString();
    }
}
