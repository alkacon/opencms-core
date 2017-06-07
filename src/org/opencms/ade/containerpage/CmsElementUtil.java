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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationCache;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReference;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReferenceParser;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElement.ModelGroupState;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

import com.google.common.collect.Sets;

/**
 * Utility class to generate the element data objects used within the container-page editor.<p>
 *
 * @since 8.0.0
 */
public class CmsElementUtil {

    /** The maximum number of nested container levels. */
    public static final int MAX_NESTING_LEVEL = 7;

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

    /** The current container page. */
    private CmsResource m_page;

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
     * Use this constructor to set the current container page state.<p>
     *
     * @param cms the cms context
     * @param currentPageUri the current page uri
     * @param containerPage the container page bean with the current container state
     * @param detailContentId the detail content structure id
     * @param req the http request
     * @param res the http response
     * @param isDragMode if the page is in drag mode
     * @param locale the content locale
     *
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(
        CmsObject cms,
        String currentPageUri,
        CmsContainerPageBean containerPage,
        CmsUUID detailContentId,
        HttpServletRequest req,
        HttpServletResponse res,
        boolean isDragMode,
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
            CmsResource detailRes = m_cms.readResource(
                detailContentId,
                CmsResourceFilter.ignoreExpirationOffline(m_cms));
            req.setAttribute(CmsDetailPageResourceHandler.ATTR_DETAIL_CONTENT_RESOURCE, detailRes);
        }
        m_standardContext = CmsJspStandardContextBean.getInstance(req);
        m_page = m_cms.readResource(currentPageUri);
        m_standardContext.setPage(containerPage);
        m_standardContext.setDragMode(isDragMode);
    }

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
            CmsResource detailRes = m_cms.readResource(detailContentId, CmsResourceFilter.ignoreExpirationOffline(cms));
            req.setAttribute(CmsDetailPageResourceHandler.ATTR_DETAIL_CONTENT_RESOURCE, detailRes);
        }
        m_standardContext = CmsJspStandardContextBean.getInstance(req);
        m_page = m_cms.readResource(currentPageUri);
        CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, m_page, req);
        CmsContainerPageBean containerPage = xmlContainerPage.getContainerPage(cms);
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
     * Checks if a group element is allowed in a container with a given type.<p>
     *
     * @param containerType the container type spec (comma separated)
     * @param groupContainer the group
     *
     * @return true if the group is allowed in the container
     */
    public static boolean checkGroupAllowed(String containerType, CmsGroupContainerBean groupContainer) {

        return !Sets.intersection(CmsContainer.splitType(containerType), groupContainer.getTypes()).isEmpty();
    }

    /**
     * Returns the formatter bean for the given element and container.<p>
     *
     * @param cms the cms context
     * @param element the element to render
     * @param container the container
     * @param config the configuration data
     * @param allowNested if nested containers are allowed
     * @param cache the session cache
     *
     * @return the formatter bean
     */
    public static I_CmsFormatterBean getFormatterForContainer(
        CmsObject cms,
        CmsContainerElementBean element,
        CmsContainer container,
        CmsADEConfigData config,
        boolean allowNested,
        CmsADESessionCache cache) {

        I_CmsFormatterBean formatter = null;
        Map<String, I_CmsFormatterBean> formatters = config.getFormatters(
            cms,
            element.getResource()).getFormatterSelection(container.getType(), container.getWidth(), allowNested);
        String formatterId = element.getIndividualSettings().get(
            CmsFormatterConfig.getSettingsKeyForContainer(container.getName()));
        if (formatterId != null) {
            formatter = formatters.get(formatterId);
        }
        if (formatter == null) {
            formatterId = element.getIndividualSettings().get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY);
            if (formatterId != null) {
                formatter = formatters.get(formatterId);
            }
        }
        if ((formatter == null) && (element.getFormatterId() != null)) {
            for (I_CmsFormatterBean currentFormatter : formatters.values()) {
                if ((currentFormatter.getJspStructureId() != null)
                    && currentFormatter.getJspStructureId().equals(element.getFormatterId())) {
                    formatter = currentFormatter;
                    break;
                }
            }
        }
        if (formatter == null) {
            formatter = getStartFormatter(cms, container, config, element, allowNested, cache);
        }
        return formatter;
    }

    /**
     * Returns the start formatter for a newly dropped element.<p>
     * This will be either the least recently used matching formatter or the default formatter.<p>
     *
     * @param cms the cms context
     * @param cnt the container
     * @param configData the configuration data
     * @param element the container element
     * @param allowNested in case nested containers are allowed
     * @param cache the session cache
     *
     * @return the formatter bean
     */
    private static I_CmsFormatterBean getStartFormatter(
        CmsObject cms,
        CmsContainer cnt,
        CmsADEConfigData configData,
        CmsContainerElementBean element,
        boolean allowNested,
        CmsADESessionCache cache) {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(element.getResource());
        I_CmsFormatterBean formatter = cache.getRecentFormatter(type.getTypeName(), cnt, allowNested, configData);
        if (formatter == null) {
            formatter = configData.getFormatters(cms, element.getResource()).getDefaultFormatter(
                cnt.getType(),
                cnt.getWidth(),
                allowNested);
        }
        return formatter;
    }

    /**
     * Returns the HTML content for the given resource and container.<p>
     *
     * @param elementFile the element resource file
     * @param elementId the element id
     * @param container the container
     * @param allowNested if nested containers are allowed
     *
     * @return the HTML content
     */
    public String getContentByContainer(
        CmsFile elementFile,
        String elementId,
        CmsContainer container,
        boolean allowNested) {

        CmsContainerElementBean element = CmsADESessionCache.getCache(m_req, m_cms).getCacheContainerElement(elementId);
        element = element.clone();
        element.setTemporaryFile(elementFile);
        CmsFormatterConfiguration configs = getFormatterConfiguration(element.getResource());
        return getContentByContainer(element, container, configs, allowNested);
    }

    /**
     * Returns the data for an element.<p>
     *
     * @param page the current container page
     * @param element the resource
     * @param containers the containers on the current container page
     * @param allowNested if nested containers are allowed
     *
     * @return the data for an element
     *
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElementData getElementData(
        CmsResource page,
        CmsContainerElementBean element,
        Collection<CmsContainer> containers,
        boolean allowNested)
    throws CmsException {

        Locale requestLocale = m_cms.getRequestContext().getLocale();
        m_cms.getRequestContext().setLocale(m_locale);
        element.initResource(m_cms);
        if (element.getResource().isFolder()) {
            return null;
        }
        CmsContainerElementData elementData = getBaseElementData(page, element);
        CmsFormatterConfiguration formatterConfiguraton = getFormatterConfiguration(element.getResource());
        Map<String, String> contents = new HashMap<String, String>();
        if (element.isGroupContainer(m_cms)) {
            Map<String, CmsContainer> containersByName = new HashMap<String, CmsContainer>();
            for (CmsContainer container : containers) {
                containersByName.put(container.getName(), container);
            }
            CmsXmlGroupContainer xmlGroupContainer = CmsXmlGroupContainerFactory.unmarshal(
                m_cms,
                element.getResource(),
                m_req);
            CmsGroupContainerBean groupContainer = xmlGroupContainer.getGroupContainer(m_cms);
            // make sure to use the content title and not the property title
            elementData.setTitle(groupContainer.getTitle());
            elementData.setTypes(groupContainer.getTypes());
            elementData.setDescription(groupContainer.getDescription());
            if (groupContainer.getTypes().isEmpty()) {
                if (groupContainer.getElements().isEmpty()) {
                    String emptySub = "<div>NEW AND EMPTY</div>";
                    for (CmsContainer cont : containersByName.values()) {
                        if (formatterConfiguraton.hasFormatter(cont.getType(), cont.getWidth(), false)) {
                            contents.put(cont.getName(), emptySub);
                        }
                    }
                } else {
                    // TODO: throw appropriate exception
                    return null;
                }
            } else {

                // add formatter and content entries for the supported types
                for (CmsContainer cnt : containersByName.values()) {

                    String type = cnt.getType();
                    if (checkGroupAllowed(type, groupContainer)) {
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
                if (formatterConfiguraton.hasFormatter(container.getType(), container.getWidth(), false)) {
                    contents.put(container.getName(), "<div>should not be used</div>");
                }
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
                boolean missesFormatterSetting = !elementData.getSettings().containsKey(
                    CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()));
                if (missesFormatterSetting) {
                    boolean allowNestedCnt = allowNested && checkContainerTreeLevel(cnt, containers);
                    if (element.getFormatterId() == null) {
                        I_CmsFormatterBean formatter = getStartFormatter(
                            m_cms,
                            cnt,
                            m_adeConfig,
                            element,
                            allowNestedCnt,
                            CmsADESessionCache.getCache(m_req, m_cms));
                        if (formatter != null) {
                            elementData.getSettings().put(
                                CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()),
                                formatter.getId());
                            element.addFormatterSetting(cnt.getName(), formatter.getId());
                        }
                    } else {
                        Map<String, I_CmsFormatterBean> formatterSelection = formatterConfiguraton.getFormatterSelection(
                            cnt.getType(),
                            cnt.getWidth(),
                            allowNestedCnt);
                        for (Entry<String, I_CmsFormatterBean> formatterEntry : formatterSelection.entrySet()) {
                            I_CmsFormatterBean formatter = formatterEntry.getValue();
                            String id = formatterEntry.getKey();
                            if (element.getFormatterId().equals(formatter.getJspStructureId())) {
                                elementData.getSettings().put(
                                    CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()),
                                    id);
                                break;
                            }
                        }
                    }
                }
            }
            // get the formatter configuration
            Map<String, String> contentsByName = getContentsByContainerName(element, containers, allowNested);
            contents = contentsByName;
        }
        elementData.setContents(contents);
        m_cms.getRequestContext().setLocale(requestLocale);
        return elementData;
    }

    /**
     * Returns the formatter and settings config data for an element.<p>
     *
     * @param page the current container page
     * @param element the resource
     * @param containerId the parent container id
     * @param containers the containers on the current container page
     * @param allowNested if nested containers are allowed
     *
     * @return the data for an element
     *
     * @throws CmsException if something goes wrong
     */
    public CmsContainerElementData getElementSettingsConfig(
        CmsResource page,
        CmsContainerElementBean element,
        String containerId,
        Collection<CmsContainer> containers,
        boolean allowNested)
    throws CmsException {

        Locale requestLocale = m_cms.getRequestContext().getLocale();
        m_cms.getRequestContext().setLocale(m_locale);
        element.initResource(m_cms);
        if (element.getResource().isFolder()) {
            return null;
        }
        CmsContainerElementData elementData = getBaseElementData(page, element);
        if (!element.isGroupContainer(m_cms) && !element.isInheritedContainer(m_cms)) {
            CmsFormatterConfiguration formatterConfiguraton = getFormatterConfiguration(element.getResource());
            Map<String, Map<String, CmsFormatterConfig>> formatters = new HashMap<String, Map<String, CmsFormatterConfig>>();
            for (CmsContainer cnt : containers) {
                if (cnt.getName().equals(containerId)) {
                    Map<String, CmsFormatterConfig> containerFormatters = new LinkedHashMap<String, CmsFormatterConfig>();
                    boolean missesFormatterSetting = !elementData.getSettings().containsKey(
                        CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()));
                    boolean allowNestedCnt = allowNested && checkContainerTreeLevel(cnt, containers);
                    Map<String, I_CmsFormatterBean> formatterSelection = formatterConfiguraton.getFormatterSelection(
                        cnt.getType(),
                        cnt.getWidth(),
                        allowNestedCnt);
                    for (Entry<String, I_CmsFormatterBean> formatterEntry : formatterSelection.entrySet()) {
                        I_CmsFormatterBean formatter = formatterEntry.getValue();
                        String id = formatterEntry.getKey();
                        if (missesFormatterSetting
                            && ((element.getFormatterId() == null)
                                || element.getFormatterId().equals(formatter.getJspStructureId()))) {
                            elementData.getSettings().put(
                                CmsFormatterConfig.getSettingsKeyForContainer(cnt.getName()),
                                id);
                            missesFormatterSetting = false;
                        }
                        String label = formatter.getNiceName(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms));
                        if (formatterEntry.getKey().startsWith(CmsFormatterConfig.SCHEMA_FORMATTER_ID)) {
                            label = Messages.get().getBundle().key(Messages.GUI_SCHEMA_FORMATTER_LABEL_0)
                                + " ["
                                + CmsResource.getName(formatter.getJspRootPath())
                                + "]";
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
                            page,
                            element.getResource(),
                            settingsConfig);
                        config.setSettingConfig(settingsConfig);
                        config.setJspRootPath(formatter.getJspRootPath());
                        containerFormatters.put(id, config);
                    }
                    formatters.put(cnt.getName(), containerFormatters);
                }
            }
            elementData.setFormatters(formatters);
        }

        m_cms.getRequestContext().setLocale(requestLocale);
        return elementData;
    }

    /**
     * Gets the container page.<p>
     *
     * @return the container page resource
     */
    public CmsResource getPage() {

        return m_page;
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
        // reinitializing resource to avoid caching issues
        elementBean.initResource(m_cms);
        CmsResource resource = elementBean.getResource();
        boolean isModelGroup = elementBean.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_ID);
        if (isModelGroup) {
            CmsUUID groupId = new CmsUUID(elementBean.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_ID));
            resource = m_cms.readResource(groupId, CmsResourceFilter.IGNORE_EXPIRATION);
        }

        boolean newEditorDisabled = !CmsWorkplaceEditorManager.checkAcaciaEditorAvailable(
            m_cms,
            elementBean.getResource());
        result.setNewEditorDisabled(newEditorDisabled);
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        result.setResourceType(typeName);
        CmsPermissionInfo permissionInfo;
        String title;
        String subTitle;
        if (!elementBean.isInMemoryOnly()) {
            permissionInfo = OpenCms.getADEManager().getPermissionInfo(m_cms, resource, m_page.getRootPath());
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(permissionInfo.getNoEditReason())
                    && elementBean.isInheritedContainer(m_cms)) {
                    String requestUri = m_cms.getRequestContext().getUri();
                    String folderPath = CmsResource.getFolderPath(requestUri);
                    String configPath = CmsStringUtil.joinPaths(
                        folderPath,
                        CmsContainerConfigurationCache.INHERITANCE_CONFIG_FILE_NAME);
                    if (m_cms.existsResource(configPath)) {
                        permissionInfo.setNoEditReason(
                            new CmsResourceUtil(m_cms, m_cms.readResource(configPath)).getNoEditReason(wpLocale, true));
                    } else {
                        if (!m_cms.getLock(folderPath).isLockableBy(m_cms.getRequestContext().getCurrentUser())) {
                            permissionInfo.setNoEditReason(
                                org.opencms.workplace.explorer.Messages.get().getBundle(wpLocale).key(
                                    org.opencms.workplace.explorer.Messages.GUI_NO_EDIT_REASON_LOCK_1,
                                    new CmsResourceUtil(m_cms, m_cms.readResource(folderPath)).getLockedByName()));
                        }
                    }
                }
            } else {
                permissionInfo.setNoEditReason(
                    Messages.get().getBundle(wpLocale).key(Messages.GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0));
            }
            CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(
                m_cms,
                resource.getStructureId(),
                m_locale);
            title = searchResult.getTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                resource.getName();
            }
            subTitle = searchResult.getUserLastModified();
            Date lastModDate = searchResult.getDateLastModified();
            if (lastModDate != null) {
                subTitle += " / " + CmsDateUtil.getDateTime(lastModDate, DateFormat.MEDIUM, wpLocale);
            }
        } else {
            permissionInfo = new CmsPermissionInfo(true, true, "");
            title = CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName);
            subTitle = CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, typeName);
        }
        result.setTitle(title);
        result.setSubTitle(subTitle);
        result.setClientId(elementBean.editorHash());
        result.setSitePath(m_cms.getSitePath(resource));

        result.setCreateNew(elementBean.isCreateNew());
        CmsResourceTypeConfig typeConfig = getConfigData().getResourceType(typeName);
        if (typeConfig != null) {
            result.setCopyInModels(typeConfig.isCopyInModels());
        }
        Map<CmsUUID, CmsElementView> viewMap = OpenCms.getADEManager().getElementViews(m_cms);

        boolean isModelGroupEditing = CmsModelGroupHelper.isModelGroupResource(m_page);
        if (!isModelGroup
            && isModelGroupEditing
            && elementBean.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_STATE)
            && (ModelGroupState.isModelGroup == ModelGroupState.evaluate(
                elementBean.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_STATE)))) {
            isModelGroup = true;
        }
        if (!isModelGroupEditing && isModelGroup) {
            CmsResourceTypeConfig modelGroupConfig = getConfigData().getResourceType(
                CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME);
            if (modelGroupConfig != null) {
                CmsUUID elementView = modelGroupConfig.getElementView();
                CmsElementView viewObject = viewMap.get(elementView);
                if ((viewObject != null) && (viewObject.getParentViewId() != null)) {
                    elementView = viewObject.getParentViewId();
                }
                result.setElementView(elementView);
            }
            if (CmsResourceTypeXmlContainerPage.isModelReuseGroup(m_cms, resource)) {
                result.setResourceType(CmsGwtConstants.TYPE_MODELGROUP_REUSE);
            }
        } else if (typeConfig != null) {
            CmsUUID elementView = typeConfig.getElementView();
            CmsElementView viewObject = viewMap.get(elementView);
            if ((viewObject != null) && (viewObject.getParentViewId() != null)) {
                elementView = viewObject.getParentViewId();
            }
            result.setElementView(elementView);
        }
        if (elementBean.isCreateNew()
            && CmsStringUtil.isEmptyOrWhitespaceOnly(permissionInfo.getNoEditReason())
            && ((typeConfig == null)
                || !typeConfig.checkCreatable(m_cms, CmsResource.getParentFolder(m_page.getRootPath())))) {
            String niceName = CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName);
            permissionInfo.setNoEditReason(
                Messages.get().getBundle(wpLocale).key(Messages.GUI_CONTAINERPAGE_TYPE_NOT_CREATABLE_1, niceName));
        }
        result.setHasSettings(hasSettings(m_cms, elementBean.getResource()));
        result.setPermissionInfo(permissionInfo);
        result.setReleasedAndNotExpired(elementBean.isReleasedAndNotExpired());
        if (elementBean.isModelGroup()) {
            String modelId = elementBean.getIndividualSettings().get(CmsContainerElement.MODEL_GROUP_ID);
            result.setModelGroupId(modelId != null ? new CmsUUID(modelId) : CmsUUID.getNullUUID());
        }
        result.setWasModelGroup(
            elementBean.getIndividualSettings().containsKey(CmsContainerElement.MODEL_GROUP_STATE)
                && (ModelGroupState.evaluate(
                    elementBean.getIndividualSettings().get(
                        CmsContainerElement.MODEL_GROUP_STATE)) == ModelGroupState.wasModelGroup));
        return result;
    }

    /**
     * Returns the formatter configuration for the given element resource.<p>
     *
     * @param resource the element resource
     *
     * @return the formatter configuration
     */
    CmsFormatterConfiguration getFormatterConfiguration(CmsResource resource) {

        return getConfigData().getFormatters(m_cms, resource);
    }

    /**
     * Checks if the maximum nesting level is reached for the given container.<p>
     *
     * @param container the container
     * @param containers the other containers of the container page
     *
     * @return <code>true</code> if further nesting is allowed
     */
    private boolean checkContainerTreeLevel(CmsContainer container, Collection<CmsContainer> containers) {

        Map<String, CmsContainer> containersByName = new HashMap<String, CmsContainer>();
        for (CmsContainer cnt : containers) {
            containersByName.put(cnt.getName(), cnt);
        }
        int level = 0;
        String parentName = container.getParentContainerName();
        while (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parentName)) {
            level++;
            parentName = containersByName.containsKey(parentName)
            ? containersByName.get(parentName).getParentContainerName()
            : null;
        }
        boolean result = MAX_NESTING_LEVEL > level;
        if (!result && LOG.isWarnEnabled()) {
            LOG.warn("Max container nesting exceeded for " + container.getName() + ": " + level);
        }
        return result;
    }

    /**
     * Returns the base element data for the given element bean, without content or formatter info.<p>
     *
     * @param page the current container page
     * @param element the resource
     *
     * @return base element data
     *
     * @throws CmsException in case reading the data fails
     */
    private CmsContainerElementData getBaseElementData(CmsResource page, CmsContainerElementBean element)
    throws CmsException {

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, element.getResource());
        CmsContainerElementData elementData = new CmsContainerElementData();
        setElementInfo(element, elementData);

        elementData.setLastModifiedDate(element.getResource().getDateLastModified());
        elementData.setLastModifiedByUser(m_cms.readUser(element.getResource().getUserLastModified()).getName());
        elementData.setNavText(resUtil.getNavText());
        Map<String, CmsXmlContentProperty> settingConfig = CmsXmlContentPropertyHelper.getPropertyInfo(
            m_cms,
            page,
            element.getResource());
        elementData.setSettings(
            CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
                m_cms,
                element.getIndividualSettings(),
                settingConfig));
        return elementData;
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
     * Returns the HTML content of the given element and container.<p>
     *
     * @param element the element
     * @param container the container
     * @param configs the formatter configurations
     * @param allowNested if nested containers are allowed
     *
     * @return the HTML content
     */
    private String getContentByContainer(
        CmsContainerElementBean element,
        CmsContainer container,
        CmsFormatterConfiguration configs,
        boolean allowNested) {

        String content = null;
        I_CmsFormatterBean formatter = getFormatterForContainer(
            m_cms,
            element,
            container,
            m_adeConfig,
            allowNested,
            CmsADESessionCache.getCache(m_req, m_cms));
        if (formatter != null) {
            element.initSettings(m_cms, formatter);
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
     * Returns the rendered element content for all the given containers.
     *
     * @param element the element to render
     * @param containers the containers the element appears in
     * @param allowNested if nested containers are allowed
     *
     * @return a map from container names to rendered page contents
     */
    private Map<String, String> getContentsByContainerName(
        CmsContainerElementBean element,
        Collection<CmsContainer> containers,
        boolean allowNested) {

        CmsFormatterConfiguration configs = getFormatterConfiguration(element.getResource());
        Map<String, String> result = new HashMap<String, String>();
        for (CmsContainer container : containers) {
            String content = getContentByContainer(
                element,
                container,
                configs,
                allowNested && checkContainerTreeLevel(container, containers));
            if (content != null) {
                content = removeScriptTags(content);
            }
            result.put(container.getName(), content);
        }
        return result;
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
                    container.getParentInstanceId(),
                    container.isRootContainer(),
                    container.getMaxElements(),
                    Collections.<CmsContainerElementBean> emptyList());
            }
            if (containerBean.getWidth() == null) {
                containerBean.setWidth(String.valueOf(container.getWidth()));
            }
            containerBean.setDetailOnly(container.isDetailOnly());
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
            return (new String(
                OpenCms.getResourceManager().getLoader(formatter).dump(m_cms, formatter, null, m_locale, m_req, m_res),
                encoding)).trim();
        } finally {
            m_cms.getRequestContext().setUri(oldUri);
        }
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
        boolean result = (formatters.getAllFormatters().size() > 1)
            || !CmsXmlContentPropertyHelper.getPropertyInfo(m_cms, null, resource).isEmpty();
        if (!result && (formatters.getAllFormatters().size() == 1)) {
            result = (formatters.getAllFormatters().get(0).getSettings() != null)
                && (formatters.getAllFormatters().get(0).getSettings().size() > 0);
        }
        return result;
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
