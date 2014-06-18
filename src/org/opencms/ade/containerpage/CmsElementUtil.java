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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationCache;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReference;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReferenceParser;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Utility class to generate the element data objects used within the container-page editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsElementUtil {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(org.opencms.ade.containerpage.CmsElementUtil.class);

    /** The ADE configuration data for the current page URI. */
    private CmsADEConfigData m_adeConfig;

    /** The cms context. */
    private CmsObject m_cms;

    /** The current page uri. */
    private String m_currentPageUri;

    /** The content locale. */
    private Locale m_locale;

    /** The request parameters to use while rendering the elements. */
    @SuppressWarnings("unused")
    private Map<String, Object> m_parameterMap;

    /** The http request. */
    private HttpServletRequest m_req;

    /** The http response. */
    private HttpServletResponse m_res;

    /** The standard context bean. */
    private CmsJspStandardContextBean m_standardContext;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param currentPageUri the current page uri
     * @param detailContentId the detail content structure id
     * @param req the http request
     * @param res the http response
     * @param locale the content locale
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(
        CmsObject cms,
        String currentPageUri,
        CmsUUID detailContentId,
        HttpServletRequest req,
        HttpServletResponse res,
        Locale locale)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_req = req;

        m_res = res;
        m_currentPageUri = currentPageUri;
        m_locale = locale;
        // initializing request for standard context bean
        req.setAttribute(CmsJspStandardContextBean.ATTRIBUTE_CMS_OBJECT, m_cms);
        if (detailContentId != null) {
            CmsResource detailRes = m_cms.readResource(detailContentId);
            req.setAttribute(CmsDetailPageResourceHandler.ATTR_DETAIL_CONTENT_RESOURCE, detailRes);
        }

        m_standardContext = CmsJspStandardContextBean.getInstance(req);

        CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(
            cms,
            m_cms.readResource(currentPageUri),
            req);
        CmsContainerPageBean containerPage = xmlContainerPage.getContainerPage(cms, m_locale);
        m_standardContext.setPage(containerPage);
    }

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param currentPageUri the current page uri
     * @param detailContentId the detail content structure id
     * @param requestParameters the request parameters to use while rendering the elements
     * @param req the http request
     * @param res the http response
     * @param locale the content locale
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(
        CmsObject cms,
        String currentPageUri,
        CmsUUID detailContentId,
        String requestParameters,
        HttpServletRequest req,
        HttpServletResponse res,
        Locale locale)
    throws CmsException {

        this(cms, currentPageUri, detailContentId, req, res, locale);
        m_parameterMap = parseRequestParameters(requestParameters);
    }

    /**
     * Returns the HTML content for the given resource and container.<p>
     * 
     * @param elementFile the element resource file
     * @param elementId the element id
     * @param container the container
     * 
     * @return the HTML content
     */
    public String getContentByContainer(CmsFile elementFile, String elementId, CmsContainer container) {

        CmsContainerElementBean element = CmsADESessionCache.getCache(m_req, m_cms).getCacheContainerElement(elementId);
        element = element.clone();
        element.setTemporaryFile(elementFile);
        CmsFormatterConfiguration configs = getFormatterConfiguration(element.getResource());
        return getContentByContainer(element, container, configs);
    }

    /**
     * Returns the rendered element content for all the given containers.
     *  
     * @param element the element to render
     * @param containers the containers the element appears in
     *  
     * @return a map from container names to rendered page contents
     */
    public Map<String, String> getContentsByContainerName(
        CmsContainerElementBean element,
        Collection<CmsContainer> containers) {

        CmsFormatterConfiguration configs = getFormatterConfiguration(element.getResource());
        Map<String, String> result = new HashMap<String, String>();
        for (CmsContainer container : containers) {
            String content = getContentByContainer(element, container, configs);
            if (content != null) {
                content = removeScriptTags(content);
                result.put(container.getName(), content);
            }
        }
        return result;
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param element the resource
     * @param containers the containers on the current container page 
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElementData getElementData(CmsContainerElementBean element, Collection<CmsContainer> containers)
    throws CmsException {

        Locale requestLocale = m_cms.getRequestContext().getLocale();
        m_cms.getRequestContext().setLocale(m_locale);
        element.initResource(m_cms);

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, element.getResource());
        CmsUUID structureId = resUtil.getResource().getStructureId();
        CmsContainerElementData elementData = new CmsContainerElementData();
        setElementInfo(element, elementData);

        elementData.setLastModifiedDate(element.getResource().getDateLastModified());
        elementData.setLastModifiedByUser(m_cms.readUser(element.getResource().getUserLastModified()).getName());
        elementData.setNavText(resUtil.getNavText());
        String title = resUtil.getTitle();
        if (!structureId.isNullUUID()) {
            CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(m_cms, structureId, requestLocale);
            title = searchResult.getTitle();
        }
        elementData.setTitle(title);

        Map<String, CmsXmlContentProperty> settingConfig = CmsXmlContentPropertyHelper.getPropertyInfo(
            m_cms,
            element.getResource());
        elementData.setSettings(CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
            m_cms,
            element.getIndividualSettings(),
            settingConfig));
        CmsFormatterConfiguration formatterConfiguraton = getFormatterConfiguration(element.getResource());
        Map<String, Map<String, CmsFormatterConfig>> formatters = new HashMap<String, Map<String, CmsFormatterConfig>>();

        //   elementData.setSettingConfig(new LinkedHashMap<String, CmsXmlContentProperty>(settingConfig));
        Map<String, String> contents = new HashMap<String, String>();
        if (element.isGroupContainer(m_cms)) {
            Set<String> types = new HashSet<String>();
            Map<String, CmsContainer> containersByName = new HashMap<String, CmsContainer>();
            for (CmsContainer container : containers) {
                types.add(container.getType());
                containersByName.put(container.getName(), container);
            }
            CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                m_cms,
                element.getResource(),
                m_req);
            CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(m_cms, m_locale);
            // make sure to use the content title and not the property title
            elementData.setTitle(groupContainer.getTitle());
            elementData.setTypes(groupContainer.getTypes());
            elementData.setDescription(groupContainer.getDescription());
            if (groupContainer.getTypes().isEmpty()) {
                if (groupContainer.getElements().isEmpty()) {
                    String emptySub = "<div>NEW AND EMPTY</div>";
                    for (String name : containersByName.keySet()) {
                        contents.put(name, emptySub);
                    }
                } else {
                    // TODO: throw appropriate exception
                    return null;
                }
            } else {

                // add formatter and content entries for the supported types
                for (CmsContainer cnt : containersByName.values()) {

                    String type = cnt.getType();
                    if (groupContainer.getTypes().contains(type)) {
                        contents.put(cnt.getName(), "<div>should not be used</div>");
                    }
                }
            }
            // add subitems
            List<String> subItems = new ArrayList<String>();

            for (CmsContainerElementBean subElement : groupContainer.getElements()) {
                // collect ids
                subItems.add(subElement.editorHash());
            }
            elementData.setSubItems(subItems);
        } else if (element.isInheritedContainer(m_cms)) {
            CmsInheritanceReferenceParser parser = new CmsInheritanceReferenceParser(m_cms);
            parser.parse(element.getResource());
            CmsInheritanceReference ref = parser.getReference(m_locale);
            String name = null;
            // check for new inheritance reference
            if (ref != null) {
                name = ref.getName();
                elementData.setDescription(ref.getDescription());
                elementData.setTitle(ref.getTitle());
            }
            for (CmsContainer container : containers) {
                contents.put(container.getName(), "<div>should not be used</div>");
            }
            List<CmsInheritanceInfo> inheritanceInfos = new ArrayList<CmsInheritanceInfo>();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                CmsInheritedContainerState result = OpenCms.getADEManager().getInheritedContainerState(
                    m_cms,
                    m_cms.addSiteRoot(m_currentPageUri),
                    name);
                for (CmsContainerElementBean subElement : result.getElements(true)) {
                    CmsInheritanceInfo inheritanceInfo = subElement.getInheritanceInfo();
                    inheritanceInfo.setClientId(subElement.editorHash());
                    inheritanceInfos.add(inheritanceInfo);
                }
            } else {
                // setting a new id for name, will be persisted once the inheritance reference is edited and saved.
                // use the structure id so it will always be the same for the resource
                name = element.getResource().getStructureId().toString();
            }
            elementData.setInheritanceInfos(inheritanceInfos);
            elementData.setInheritanceName(name);
        } else {
            for (CmsContainer cnt : containers) {
                Map<String, CmsFormatterConfig> containerFormatters = new LinkedHashMap<String, CmsFormatterConfig>();
                boolean missesFormatterSetting = !elementData.getSettings().containsKey(
                    CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()));
                for (Entry<String, I_CmsFormatterBean> formatterEntry : formatterConfiguraton.getFormatterSelection(
                    cnt.getType(),
                    cnt.getWidth()).entrySet()) {
                    I_CmsFormatterBean formatter = formatterEntry.getValue();
                    String id = formatterEntry.getKey();
                    if (missesFormatterSetting
                        && ((element.getFormatterId() == null) || element.getFormatterId().equals(
                            formatter.getJspStructureId()))) {
                        elementData.getSettings().put(CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()), id);
                        missesFormatterSetting = false;
                    }
                    String label = formatter.getNiceName();
                    if (formatterEntry.getKey().equals(CmsFormatterConfig.SCHEMA_FORMATTER_ID)) {
                        label = Messages.get().getBundle().key(Messages.GUI_SCHEMA_FORMATTER_LABEL_0);
                    }
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(label)) {
                        label = id;
                    }
                    CmsFormatterConfig config = new CmsFormatterConfig(id);
                    Set<String> cssResources = new LinkedHashSet<String>();
                    for (String cssSitePath : formatter.getCssHeadIncludes()) {
                        cssResources.add(OpenCms.getLinkManager().getOnlineLink(m_cms, cssSitePath));
                    }
                    config.setCssResources(cssResources);
                    config.setInlineCss(formatter.getInlineCss());
                    config.setLabel(label);
                    Map<String, CmsXmlContentProperty> settingsConfig = new LinkedHashMap<String, CmsXmlContentProperty>(
                        formatter.getSettings());
                    settingsConfig = CmsXmlContentPropertyHelper.resolveMacrosForPropertyInfo(
                        m_cms,
                        element.getResource(),
                        settingsConfig);
                    config.setSettingConfig(settingsConfig);
                    config.setJspRootPath(formatter.getJspRootPath());
                    containerFormatters.put(id, config);
                }
                formatters.put(cnt.getName(), containerFormatters);
            }
            // get the formatter configuration
            Map<String, String> contentsByName = getContentsByContainerName(element, containers);
            contents = contentsByName;
        }
        elementData.setContents(contents);
        elementData.setFormatters(formatters);
        m_cms.getRequestContext().setLocale(requestLocale);
        return elementData;
    }

    /**
     * Sets the data to the given container element.<p>
     * 
     * @param elementBean the element bean
     * @param result the container element to set the data to
     * 
     * @return the container element
     *  
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElement setElementInfo(CmsContainerElementBean elementBean, CmsContainerElement result)
    throws CmsException {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
        String noEditReason = "";
        // reinitializing resource to avoid caching issues
        elementBean.initResource(m_cms);
        boolean newEditorDisabled = !CmsWorkplaceEditorManager.checkAcaciaEditorAvailable(
            m_cms,
            elementBean.getResource());
        result.setNewEditorDisabled(newEditorDisabled);
        if (!elementBean.isInMemoryOnly()) {
            if (CmsResourceTypeXmlContent.isXmlContent(elementBean.getResource())) {
                result.setWritePermission(m_cms.hasPermissions(
                    elementBean.getResource(),
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION));
                noEditReason = new CmsResourceUtil(m_cms, elementBean.getResource()).getNoEditReason(wpLocale, true);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(noEditReason) && elementBean.isInheritedContainer(m_cms)) {
                    String requestUri = m_cms.getRequestContext().getUri();
                    String folderPath = CmsResource.getFolderPath(requestUri);
                    String configPath = CmsStringUtil.joinPaths(
                        folderPath,
                        CmsContainerConfigurationCache.INHERITANCE_CONFIG_FILE_NAME);
                    if (m_cms.existsResource(configPath)) {
                        noEditReason = new CmsResourceUtil(m_cms, m_cms.readResource(configPath)).getNoEditReason(
                            wpLocale,
                            true);
                    } else {
                        if (!m_cms.getLock(folderPath).isLockableBy(m_cms.getRequestContext().getCurrentUser())) {
                            noEditReason = org.opencms.workplace.explorer.Messages.get().getBundle(wpLocale).key(
                                org.opencms.workplace.explorer.Messages.GUI_NO_EDIT_REASON_LOCK_1,
                                new CmsResourceUtil(m_cms, m_cms.readResource(folderPath)).getLockedByName());
                        }
                    }
                } else {
                    noEditReason = new CmsResourceUtil(m_cms, elementBean.getResource()).getNoEditReason(wpLocale, true);
                }
            } else {
                noEditReason = Messages.get().getBundle().key(Messages.GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0);
            }
        }
        result.setClientId(elementBean.editorHash());
        result.setSitePath(elementBean.getSitePath());
        String typeName = OpenCms.getResourceManager().getResourceType(elementBean.getResource().getTypeId()).getTypeName();
        result.setResourceType(typeName);
        result.setNew(elementBean.isCreateNew());
        if (elementBean.isCreateNew()) {
            CmsResourceTypeConfig typeConfig = getConfigData().getResourceType(typeName);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(noEditReason)
                && ((typeConfig == null) || !typeConfig.checkCreatable(m_cms))) {
                String niceName = CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName);
                noEditReason = Messages.get().getBundle().key(Messages.GUI_CONTAINERPAGE_TYPE_NOT_CREATABLE_1, niceName);
            }
        }
        result.setHasSettings(hasSettings(m_cms, elementBean.getResource()));
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);

        // although the addRequireVisible seems redundant, it is actually needed because of a weird bug
        // in the default permission handler.
        result.setViewPermission(elementBean.isInMemoryOnly()
            || (m_cms.hasPermissions(
                elementBean.getResource(),
                CmsPermissionSet.ACCESS_VIEW,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible()) && settings.getAccess().getPermissions(
                m_cms,
                elementBean.getResource()).requiresViewPermission()));

        result.setReleasedAndNotExpired(elementBean.isReleasedAndNotExpired());
        result.setNoEditReason(noEditReason);
        return result;
    }

    /**
     * Returns the HTML content of the given element and container.<p>
     *  
     * @param element the element
     * @param container the container
     * @param configs the formatter configurations
     * 
     * @return the HTML content
     */
    private String getContentByContainer(
        CmsContainerElementBean element,
        CmsContainer container,
        CmsFormatterConfiguration configs) {

        String content = null;
        I_CmsFormatterBean formatter;

        String formatterId = element.getSettings().get(
            CmsFormatterConfig.getSettingsKeyForContainer(container.getName()));
        if (formatterId != null) {
            Map<String, I_CmsFormatterBean> formatters = configs.getFormatterSelection(
                container.getType(),
                container.getWidth());
            formatter = formatters.get(formatterId);
        } else {
            formatter = configs.getDefaultFormatter(container.getType(), container.getWidth());
        }
        if (formatter != null) {
            try {
                content = getElementContent(element, m_cms.readResource(formatter.getJspStructureId()), container);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            if (content != null) {
                content = removeScriptTags(content);
            }
        }
        return content;
    }

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param element the element bean
     * @param formatter the formatter uri
     * @param container the container for which the element content should be retrieved 
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * 
     * @throws IOException if a jsp related error occurs
     */
    private String getElementContent(CmsContainerElementBean element, CmsResource formatter, CmsContainer container)
    throws CmsException, ServletException, IOException {

        element.initResource(m_cms);
        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            formatter), element.getResource(), formatter);
        CmsResource loaderRes = loaderFacade.getLoaderStartResource();
        TemplateBean templateBean = CmsADESessionCache.getCache(m_req, m_cms).getTemplateBean(
            m_cms.addSiteRoot(m_currentPageUri),
            true);
        String oldUri = m_cms.getRequestContext().getUri();
        try {
            m_cms.getRequestContext().setUri(m_currentPageUri);
            CmsContainerBean containerBean = null;
            if ((m_standardContext.getPage() != null)
                && m_standardContext.getPage().getContainers().containsKey(container.getName())) {
                containerBean = m_standardContext.getPage().getContainers().get(container.getName());
            } else {
                containerBean = new CmsContainerBean(
                    container.getName(),
                    container.getType(),
                    container.getMaxElements(),
                    Collections.<CmsContainerElementBean> emptyList());
            }
            if (containerBean.getWidth() == null) {
                containerBean.setWidth(String.valueOf(container.getWidth()));
            }
            m_standardContext.setContainer(containerBean);
            m_standardContext.setElement(element);
            m_standardContext.setEdited(true);
            // to enable 'old' direct edit features for content-collector-elements, 
            // set the direct-edit-provider-attribute in the request
            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(m_cms, CmsDirectEditMode.TRUE, element.getSitePath());
            m_req.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);
            m_req.setAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN, templateBean);
            String encoding = m_res.getCharacterEncoding();
            return (new String(loaderFacade.getLoader().dump(m_cms, loaderRes, null, m_locale, m_req, m_res), encoding)).trim();
        } finally {
            m_cms.getRequestContext().setUri(oldUri);
        }
    }

    /**
     * Returns the ADE configuration data for the current URI.<p>
     * 
     * @return the ADE configuration data
     */
    private CmsADEConfigData getConfigData() {

        if (m_adeConfig == null) {
            m_adeConfig = OpenCms.getADEManager().lookupConfiguration(m_cms, m_cms.addSiteRoot(m_currentPageUri));
        }
        return m_adeConfig;
    }

    /**
     * Returns the formatter configuration for the given element resource.<p>
     *  
     * @param resource the element resource
     * 
     * @return the formatter configuration
     */
    private CmsFormatterConfiguration getFormatterConfiguration(CmsResource resource) {

        return getConfigData().getFormatters(m_cms, resource);
    }

    /**
     * Helper method for checking whether there are properties defined for a given content element.<p>
     * 
     * @param cms the CmsObject to use for VFS operations 
     * @param resource the resource for which it should be checked whether it has properties 
     * 
     * @return true if the resource has properties defined 
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean hasSettings(CmsObject cms, CmsResource resource) throws CmsException {

        if (!CmsResourceTypeXmlContent.isXmlContent(resource)) {
            return false;
        }

        CmsFormatterConfiguration formatters = getConfigData().getFormatters(m_cms, resource);
        return (formatters.getAllFormatters().size() > 1)
            || !CmsXmlContentPropertyHelper.getPropertyInfo(m_cms, resource).isEmpty();
    }

    /**
     * Parses the given request parameters string into a parameter map.<p>
     * 
     * @param requestParameters the request parameters to parse
     * 
     * @return the parameter map
     */
    private Map<String, Object> parseRequestParameters(String requestParameters) {

        Map<String, Object> parameterMap;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(requestParameters)) {
            parameterMap = new HashMap<String, Object>();
            String[] params = requestParameters.split("&");
            for (int i = 0; i < params.length; i++) {
                int position = params[i].indexOf("=");
                if (position >= 0) {
                    String key = params[i].substring(0, position);
                    String value = params[i].substring(position + 1);
                    if (value.contains(",")) {
                        parameterMap.put(key, value.split(","));
                    } else {
                        parameterMap.put(key, value);
                    }
                }
            }
        } else {
            parameterMap = Collections.<String, Object> emptyMap();
        }
        return parameterMap;
    }

    /** 
     * Removes all script tags from given input.<p>
     * 
     * @param input the input to remove script tags from
     * 
     * @return the cleaned input
     */
    private String removeScriptTags(String input) {

        Pattern removePattern = Pattern.compile("<script[^>]*?>[\\s\\S]*?<\\/script>");
        Matcher match = removePattern.matcher(input);
        return match.replaceAll("");
    }
}
