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
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagHeadIncludes;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;
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
     * @param req the http request
     * @param res the http response
     * @param locale the content locale
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(
        CmsObject cms,
        String currentPageUri,
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
        String requestParameters,
        HttpServletRequest req,
        HttpServletResponse res,
        Locale locale)
    throws CmsException {

        this(cms, currentPageUri, req, res, locale);
        m_parameterMap = parseRequestParameters(requestParameters);
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

        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(
            m_cms,
            m_cms.getRequestContext().addSiteRoot(m_currentPageUri));
        CmsFormatterConfiguration configs = adeConfig.getFormatters(m_cms, element.getResource());
        Map<String, String> result = new HashMap<String, String>();
        for (CmsContainer container : containers) {
            String name = container.getName();
            CmsFormatterBean formatter = configs.getFormatter(container.getType(), container.getWidth());
            if (formatter != null) {
                String content = null;
                try {
                    content = getElementContent(element, m_cms.readResource(formatter.getJspStructureId()), container);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                if (content != null) {
                    content = removeScriptTags(content);
                    result.put(name, content);
                }
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
        String title = resUtil.getTitle();
        if (!structureId.isNullUUID()) {
            CmsGallerySearchResult searchResult = CmsGallerySearch.searchById(m_cms, structureId, requestLocale);
            title = searchResult.getTitle();
        }

        CmsContainerElementData elementBean = new CmsContainerElementData();
        elementBean.setReleasedAndNotExpired(element.isReleasedAndNotExpired());
        elementBean.setClientId(element.editorHash());
        elementBean.setSitePath(resUtil.getFullPath());
        elementBean.setLastModifiedDate(element.getResource().getDateLastModified());
        elementBean.setLastModifiedByUser(m_cms.readUser(element.getResource().getUserLastModified()).getName());
        elementBean.setNavText(resUtil.getNavText());
        elementBean.setTitle(title);
        elementBean.setResourceType(OpenCms.getResourceManager().getResourceType(element.getResource().getTypeId()).getTypeName());
        Set<String> cssResources = new LinkedHashSet<String>();
        for (String cssSitePath : CmsJspTagHeadIncludes.getCSSHeadIncludes(m_cms, element.getResource())) {
            cssResources.add(OpenCms.getLinkManager().substituteLinkForUnknownTarget(m_cms, cssSitePath));
        }
        elementBean.setCssResources(cssResources);
        Map<String, CmsXmlContentProperty> settingConfig = CmsXmlContentPropertyHelper.getPropertyInfo(
            m_cms,
            element.getResource());
        elementBean.setSettings(CmsXmlContentPropertyHelper.convertPropertiesToClientFormat(
            m_cms,
            element.getIndividualSettings(),
            settingConfig));
        elementBean.setSettingConfig(new LinkedHashMap<String, CmsXmlContentProperty>(settingConfig));
        // no need to check permissions for in memory resources
        elementBean.setViewPermission(element.isInMemoryOnly()
            || m_cms.hasPermissions(
                element.getResource(),
                CmsPermissionSet.ACCESS_VIEW,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION));
        String noEditReason = "";
        if (CmsResourceTypeXmlContent.isXmlContent(element.getResource())) {
            if (!element.isInMemoryOnly()) {
                noEditReason = CmsEncoder.escapeHtml(resUtil.getNoEditReason(
                    OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms),
                    true));
            }
        } else {
            noEditReason = org.opencms.jsp.Messages.get().getBundle().key(
                org.opencms.jsp.Messages.GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0);
        }
        elementBean.setNoEditReason(noEditReason);
        elementBean.setStatus(resUtil.getStateAbbreviation());

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
            elementBean.setGroupContainer(true);

            // make sure to use the content title and not the property title
            elementBean.setTitle(groupContainer.getTitle());

            elementBean.setTypes(groupContainer.getTypes());
            elementBean.setDescription(groupContainer.getDescription());
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
            elementBean.setSubItems(subItems);
        } else {
            // get the formatter configuration
            Map<String, String> contentsByName = getContentsByContainerName(element, containers);
            contents = contentsByName;
        }
        elementBean.setContents(contents);
        m_cms.getRequestContext().setLocale(requestLocale);
        return elementBean;
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
            String encoding = m_res.getCharacterEncoding();
            return (new String(loaderFacade.getLoader().dump(m_cms, loaderRes, null, m_locale, m_req, m_res), encoding)).trim();
        } finally {
            m_cms.getRequestContext().setUri(oldUri);
        }
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
