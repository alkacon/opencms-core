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

package org.opencms.ade.galleries;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsImageScaler;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the gallery preview dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsPreviewService extends CmsGwtService implements I_CmsPreviewService {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPreviewService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = -8175522641937277445L;

    /**
     * Renders the preview content for the given resource and locale.<p>
     *
     * @param request the current servlet request
     * @param response the current servlet response
     * @param cms the cms context
     * @param resource the resource
     * @param locale the content locale
     *
     * @return the rendered HTML preview content
     */
    public static String getPreviewContent(
        HttpServletRequest request,
        HttpServletResponse response,
        CmsObject cms,
        CmsResource resource,
        Locale locale) {

        try {
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    cms.getRequestContext().getRootUri());

                CmsFormatterConfiguration formatters = adeConfig.getFormatters(cms, resource);
                I_CmsFormatterBean formatter = formatters.getPreviewFormatter();
                if (formatter != null) {
                    CmsObject tempCms = OpenCms.initCmsObject(cms);
                    tempCms.getRequestContext().setLocale(locale);
                    CmsResource formatterResource = tempCms.readResource(formatter.getJspStructureId());
                    request.setAttribute(CmsJspStandardContextBean.ATTRIBUTE_CMS_OBJECT, tempCms);
                    CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(request);
                    CmsContainerElementBean element = new CmsContainerElementBean(
                        resource.getStructureId(),
                        formatter.getJspStructureId(),
                        null,
                        false);
                    if ((resource instanceof I_CmsHistoryResource) && (resource instanceof CmsFile)) {
                        element.setHistoryFile((CmsFile)resource);
                    }
                    element.initResource(tempCms);
                    CmsContainerBean containerBean = new CmsContainerBean(
                        "PREVIEW",
                        CmsFormatterBean.PREVIEW_TYPE,
                        null,
                        true,
                        1,
                        Collections.<CmsContainerElementBean> emptyList());
                    containerBean.setWidth(String.valueOf(CmsFormatterBean.PREVIEW_WIDTH));

                    standardContext.setContainer(containerBean);
                    standardContext.setElement(element);
                    standardContext.setEdited(true);
                    standardContext.setPage(
                        new CmsContainerPageBean(Collections.<CmsContainerBean> singletonList(containerBean)));
                    String encoding = response.getCharacterEncoding();
                    return (new String(
                        OpenCms.getResourceManager().getLoader(formatterResource).dump(
                            tempCms,
                            formatterResource,
                            null,
                            locale,
                            request,
                            response),
                        encoding)).trim();
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
            //TODO: logging
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#getImageInfo(java.lang.String, java.lang.String)
     */
    public CmsImageInfoBean getImageInfo(String resourcePath, String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsImageInfoBean resInfo = new CmsImageInfoBean();
        try {
            int pos = resourcePath.indexOf("?");
            String resName = resourcePath;
            if (pos > -1) {
                resName = resourcePath.substring(0, pos);
            }
            CmsResource resource = readResourceFromCurrentOrRootSite(cms, resName);
            readResourceInfo(cms, resource, resInfo, locale);
            resInfo.setViewLink(
                CmsStringUtil.joinPaths(
                    OpenCms.getSystemInfo().getOpenCmsContext(),
                    CmsPermalinkResourceHandler.PERMALINK_HANDLER,
                    resource.getStructureId().toString()));
            resInfo.setHash(resource.getStructureId().hashCode());
            CmsImageScaler scaler = new CmsImageScaler(cms, resource);
            int height = -1;
            int width = -1;
            if (scaler.isValid()) {
                height = scaler.getHeight();
                width = scaler.getWidth();
            }
            resInfo.setHeight(height);
            resInfo.setWidth(width);
            CmsProperty property = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_COPYRIGHT, false);
            if (!property.isNullProperty()) {
                resInfo.setCopyright(property.getValue());
            }
        } catch (Exception e) {
            error(e);
        }
        return resInfo;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#getResourceInfo(java.lang.String, java.lang.String)
     */
    public CmsResourceInfoBean getResourceInfo(String resourcePath, String locale) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsResourceInfoBean resInfo = new CmsResourceInfoBean();
        try {
            int pos = resourcePath.indexOf("?");
            String resName = resourcePath;
            if (pos > -1) {
                resName = resourcePath.substring(0, pos);
            }
            CmsResource resource = readResourceFromCurrentOrRootSite(cms, resName);
            readResourceInfo(cms, resource, resInfo, locale);
        } catch (CmsException e) {
            error(e);
        }
        return resInfo;
    }

    /**
     * Retrieves the resource information and puts it into the provided resource info bean.<p>
     *
     * @param cms the initialized cms object
     * @param resource the resource
     * @param resInfo the resource info bean
     * @param locale the content locale
     *
     * @throws CmsException if something goes wrong
     */
    public void readResourceInfo(CmsObject cms, CmsResource resource, CmsResourceInfoBean resInfo, String locale)
    throws CmsException {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        resInfo.setTitle(resource.getName());
        resInfo.setStructureId(resource.getStructureId());
        resInfo.setDescription(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
        resInfo.setResourcePath(cms.getSitePath(resource));
        resInfo.setResourceType(type.getTypeName());
        // set the default file and detail type info
        String detailType = CmsResourceIcon.getDefaultFileOrDetailType(cms, resource);
        resInfo.setDetailResourceType(detailType);
        resInfo.setSize((resource.getLength() / 1024) + " kb");
        resInfo.setLastModified(new Date(resource.getDateLastModified()));
        resInfo.setNoEditReason(new CmsResourceUtil(cms, resource).getNoEditReason(wpLocale, true));
        // reading default explorer-type properties
        CmsExplorerTypeSettings setting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
        List<String> properties = setting.getProperties();
        String reference = setting.getReference();

        // looking up properties from referenced explorer types if properties list is empty

        while ((properties.size() == 0) && !CmsStringUtil.isEmptyOrWhitespaceOnly(reference)) {
            setting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(reference);
            properties = setting.getProperties();
            reference = setting.getReference();
        }
        Map<String, String> props = new LinkedHashMap<String, String>();
        Iterator<String> propIt = properties.iterator();
        while (propIt.hasNext()) {
            String propertyName = propIt.next();
            CmsProperty property = cms.readPropertyObject(resource, propertyName, false);
            if (!property.isNullProperty()) {
                props.put(property.getName(), property.getValue());
            } else {
                props.put(propertyName, null);
            }
        }
        resInfo.setProperties(props);
        resInfo.setPreviewContent(getPreviewContent(cms, resource, CmsLocaleManager.getLocale(locale)));
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#syncGetImageInfo(java.lang.String, java.lang.String)
     */
    public CmsImageInfoBean syncGetImageInfo(String resourcePath, String locale) throws CmsRpcException {

        return getImageInfo(resourcePath, locale);
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#updateImageProperties(java.lang.String, java.lang.String, java.util.Map)
     */
    public CmsImageInfoBean updateImageProperties(String resourcePath, String locale, Map<String, String> properties)
    throws CmsRpcException {

        try {
            saveProperties(resourcePath, properties);
        } catch (CmsException e) {
            error(e);
        }
        return getImageInfo(resourcePath, locale);
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#updateResourceProperties(java.lang.String, java.lang.String, java.util.Map)
     */
    public CmsResourceInfoBean updateResourceProperties(
        String resourcePath,
        String locale,
        Map<String, String> properties)
    throws CmsRpcException {

        try {
            saveProperties(resourcePath, properties);
        } catch (CmsException e) {
            error(e);
        }
        return getResourceInfo(resourcePath, locale);
    }

    /**
     * Renders the preview content for the given resource and locale.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param locale the content locale
     *
     * @return the rendered HTML preview content
     */
    private String getPreviewContent(CmsObject cms, CmsResource resource, Locale locale) {

        return getPreviewContent(getRequest(), getResponse(), cms, resource, locale);
    }

    /**
     * Tries to read a resource either from the current site or from the root site.<p>
     *
     * @param cms the CMS context to use
     * @param name the resource path
     *
     * @return the resource which was read
     * @throws CmsException if something goes wrong
     */
    private CmsResource readResourceFromCurrentOrRootSite(CmsObject cms, String name) throws CmsException {

        CmsResource resource = null;
        try {
            resource = cms.readResource(name, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsVfsResourceNotFoundException e) {
            String originalSiteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot("");
                resource = cms.readResource(name, CmsResourceFilter.IGNORE_EXPIRATION);
            } finally {
                cms.getRequestContext().setSiteRoot(originalSiteRoot);
            }

        }
        return resource;
    }

    /**
     * Saves the given properties to the resource.<p>
     *
     * @param resourcePath the resource path
     * @param properties the properties
     *
     * @throws CmsException if something goes wrong
     */
    private void saveProperties(String resourcePath, Map<String, String> properties) throws CmsException {

        CmsResource resource;
        CmsObject cms = getCmsObject();
        int pos = resourcePath.indexOf("?");
        String resName = resourcePath;
        if (pos > -1) {
            resName = resourcePath.substring(0, pos);
        }
        resource = cms.readResource(resName);

        if (properties != null) {
            for (Entry<String, String> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                String propertyValue = entry.getValue();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(propertyValue)) {
                    propertyValue = "";
                }
                try {
                    CmsProperty currentProperty = cms.readPropertyObject(resource, propertyName, false);
                    // detect if property is a null property or not
                    if (currentProperty.isNullProperty()) {
                        // create new property object and set key and value
                        currentProperty = new CmsProperty();
                        currentProperty.setName(propertyName);
                        if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                            // set structure value
                            currentProperty.setStructureValue(propertyValue);
                            currentProperty.setResourceValue(null);
                        } else {
                            // set resource value
                            currentProperty.setStructureValue(null);
                            currentProperty.setResourceValue(propertyValue);
                        }
                    } else if (currentProperty.getStructureValue() != null) {
                        // structure value has to be updated
                        currentProperty.setStructureValue(propertyValue);
                        currentProperty.setResourceValue(null);
                    } else {
                        // resource value has to be updated
                        currentProperty.setStructureValue(null);
                        currentProperty.setResourceValue(propertyValue);
                    }
                    CmsLock lock = cms.getLock(resource);
                    if (lock.isUnlocked()) {
                        // lock resource before operation
                        cms.lockResource(resName);
                    }
                    // write the property to the resource
                    cms.writePropertyObject(resName, currentProperty);
                    // unlock the resource
                    cms.unlockResource(resName);
                } catch (CmsException e) {
                    // writing the property failed, log error
                    log(e.getLocalizedMessage());
                }
            }
        }
    }

}
