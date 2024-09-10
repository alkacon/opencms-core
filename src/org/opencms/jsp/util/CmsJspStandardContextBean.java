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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.configuration.plugins.CmsTemplatePlugin;
import org.opencms.ade.configuration.plugins.CmsTemplatePluginFinder;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.ade.containerpage.CmsModelGroupHelper;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.i18n.CmsMessageToBundleIndex;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.i18n.CmsVfsResourceBundle;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.jsp.CmsJspTagEditable;
import org.opencms.jsp.Messages;
import org.opencms.jsp.jsonpart.CmsJsonPartFilter;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigParserUtils;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.CmsEditorConfiguration;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionParser;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsMetaMapping;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.templatemapper.CmsTemplateMapper;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;

/**
 * Allows convenient access to the most important OpenCms functions on a JSP page,
 * indented to be used from a JSP with the JSTL or EL.<p>
 *
 * This bean is available by default in the context of an OpenCms managed JSP.<p>
 *
 * @since 8.0
 */
public final class CmsJspStandardContextBean {

    /**
     * Container element wrapper to add some API methods.<p>
     */
    public class CmsContainerElementWrapper extends CmsContainerElementBean {

        /** Cache for the wrapped element parent. */
        private CmsContainerElementWrapper m_parent;

        /** Cache for the wrapped element type name. */
        private String m_resourceTypeName;

        /** The wrapped element instance. */
        private CmsContainerElementBean m_wrappedElement;

        /** Cache for the wrapped element settings. */
        private Map<String, CmsJspElementSettingValueWrapper> m_wrappedSettings;

        /** Cached formatter key - use array to distinguish between uncached and cached, but null. */
        private String[] m_formatterKey;

        /**
         * Constructor.<p>
         *
         * @param element the element to wrap
         */
        protected CmsContainerElementWrapper(CmsContainerElementBean element) {

            m_wrappedElement = element;

        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#clone()
         */
        @Override
        public CmsContainerElementBean clone() {

            return m_wrappedElement.clone();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#editorHash()
         */
        @Override
        public String editorHash() {

            return m_wrappedElement.editorHash();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            return m_wrappedElement.equals(obj);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getFormatterId()
         */
        @Override
        public CmsUUID getFormatterId() {

            return m_wrappedElement.getFormatterId();
        }

        /**
         * Returns the formatter key, if possible, otherwise the formatter configuration id, or null if nothing at all can be found.
         *
         * @return the formatter key
         */
        public String getFormatterKey() {

            if (m_formatterKey == null) {
                String key = null;
                I_CmsFormatterBean formatter = getElementFormatter(m_wrappedElement);
                if (formatter != null) {
                    key = formatter.getKeyOrId();
                }
                m_formatterKey = new String[] {key};
            }
            return m_formatterKey[0];
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getId()
         */
        @Override
        public CmsUUID getId() {

            return m_wrappedElement.getId();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getIndividualSettings()
         */
        @Override
        public Map<String, String> getIndividualSettings() {

            return m_wrappedElement.getIndividualSettings();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getInheritanceInfo()
         */
        @Override
        public CmsInheritanceInfo getInheritanceInfo() {

            return m_wrappedElement.getInheritanceInfo();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getInstanceId()
         */
        @Override
        public String getInstanceId() {

            return m_wrappedElement.getInstanceId();
        }

        /**
         * Returns the parent element if present.<p>
         *
         * @return the parent element or <code>null</code> if not available
         */
        public CmsContainerElementWrapper getParent() {

            if (m_parent == null) {
                CmsContainerElementBean parent = getParentElement(m_wrappedElement);
                m_parent = (parent != null) ? new CmsContainerElementWrapper(getParentElement(m_wrappedElement)) : null;
            }
            return m_parent;
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getResource()
         */
        @Override
        public CmsResource getResource() {

            return m_wrappedElement.getResource();
        }

        /**
         * Returns the resource type name of the element resource.<p>
         *
         * @return the resource type name
         */
        public String getResourceTypeName() {

            if (m_resourceTypeName == null) {
                m_resourceTypeName = "";
                try {
                    m_resourceTypeName = OpenCms.getResourceManager().getResourceType(
                        m_wrappedElement.getResource()).getTypeName();
                } catch (Exception e) {
                    CmsJspStandardContextBean.LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return m_resourceTypeName;
        }

        /**
         * Returns a lazy initialized setting map.<p>
         *
         * The values returned in the map are instances of {@link A_CmsJspValueWrapper}.
         *
         * @return the wrapped settings
         */
        public Map<String, CmsJspElementSettingValueWrapper> getSetting() {

            if (m_wrappedSettings == null) {
                m_wrappedSettings = CmsCollectionsGenericWrapper.createLazyMap(
                    new SettingsTransformer(m_wrappedElement));
            }
            return m_wrappedSettings;
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getSettings()
         */
        @Override
        public Map<String, String> getSettings() {

            return m_wrappedElement.getSettings();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#getSitePath()
         */
        @Override
        public String getSitePath() {

            return m_wrappedElement.getSitePath();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#hashCode()
         */
        @Override
        public int hashCode() {

            return m_wrappedElement.hashCode();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#initResource(org.opencms.file.CmsObject)
         */
        @Override
        public void initResource(CmsObject cms) throws CmsException {

            m_wrappedElement.initResource(cms);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#initSettings(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData, org.opencms.xml.containerpage.I_CmsFormatterBean, java.util.Locale, javax.servlet.ServletRequest, java.util.Map)
         */
        @Override
        public void initSettings(
            CmsObject cms,
            CmsADEConfigData config,
            I_CmsFormatterBean formatterBean,
            Locale locale,
            ServletRequest request,
            Map<String, String> settingPresets) {

            m_wrappedElement.initSettings(cms, config, formatterBean, locale, request, settingPresets);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isCreateNew()
         */
        @Override
        public boolean isCreateNew() {

            return m_wrappedElement.isCreateNew();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isGroupContainer(org.opencms.file.CmsObject)
         */
        @Override
        public boolean isGroupContainer(CmsObject cms) throws CmsException {

            return m_wrappedElement.isGroupContainer(cms);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isHistoryContent()
         */
        @Override
        public boolean isHistoryContent() {

            return m_wrappedElement.isHistoryContent();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isInheritedContainer(org.opencms.file.CmsObject)
         */
        @Override
        public boolean isInheritedContainer(CmsObject cms) throws CmsException {

            return m_wrappedElement.isInheritedContainer(cms);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isInMemoryOnly()
         */
        @Override
        public boolean isInMemoryOnly() {

            return m_wrappedElement.isInMemoryOnly();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isReleasedAndNotExpired()
         */
        @Override
        public boolean isReleasedAndNotExpired() {

            return m_wrappedElement.isReleasedAndNotExpired();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#isTemporaryContent()
         */
        @Override
        public boolean isTemporaryContent() {

            return m_wrappedElement.isTemporaryContent();
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#setFormatterId(org.opencms.util.CmsUUID)
         */
        @Override
        public void setFormatterId(CmsUUID formatterId) {

            m_wrappedElement.setFormatterId(formatterId);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#setHistoryFile(org.opencms.file.CmsFile)
         */
        @Override
        public void setHistoryFile(CmsFile file) {

            m_wrappedElement.setHistoryFile(file);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#setInheritanceInfo(org.opencms.ade.containerpage.shared.CmsInheritanceInfo)
         */
        @Override
        public void setInheritanceInfo(CmsInheritanceInfo inheritanceInfo) {

            m_wrappedElement.setInheritanceInfo(inheritanceInfo);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#setTemporaryFile(org.opencms.file.CmsFile)
         */
        @Override
        public void setTemporaryFile(CmsFile elementFile) {

            m_wrappedElement.setTemporaryFile(elementFile);
        }

        /**
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#toString()
         */
        @Override
        public String toString() {

            return m_wrappedElement.toString();
        }
    }

    /**
     * Provides a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function or resource type as a key.<p>
     */
    public class CmsDetailLookupTransformer implements Transformer {

        /** The selected prefix. */
        private String m_prefix;

        /**
         * Constructor with a prefix.<p>
         *
         * The prefix is used to distinguish between type detail pages and function detail pages.<p>
         *
         * @param prefix the prefix to use
         */
        public CmsDetailLookupTransformer(String prefix) {

            m_prefix = prefix;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object input) {

            String prefix = m_prefix;
            CmsObject cms = m_cms;
            String inputStr = String.valueOf(input);

            return getFunctionDetailLink(cms, prefix, inputStr, false);
        }

    }

    /**
     * The element setting transformer.<p>
     */
    public class SettingsTransformer implements Transformer {

        /** The element formatter config. */
        private I_CmsFormatterBean m_formatter;

        /** The configured formatter settings. */
        private Map<String, CmsXmlContentProperty> m_formatterSettingsConfig;

        /** The element. */
        private CmsContainerElementBean m_transformElement;

        /**
         * Constructor.<p>
         *
         * @param element the element
         */
        SettingsTransformer(CmsContainerElementBean element) {

            m_transformElement = element;
            m_formatter = getElementFormatter(element);
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        @Override
        public Object transform(Object settingName) {

            boolean exists;
            if (m_formatter != null) {
                if (m_formatterSettingsConfig == null) {
                    m_formatterSettingsConfig = OpenCms.getADEManager().getFormatterSettings(
                        m_cms,
                        m_config,
                        m_formatter,
                        m_transformElement.getResource(),
                        getLocale(),
                        m_request);
                }

                // the first condition is used to catch shared settings of nested formatters,
                // the second condition is used to catch settings with visibility parentShared
                // (just because you can't edit them on the child element doesn't mean they don't exist!)
                exists = (m_formatterSettingsConfig.get(settingName) != null)
                    || m_formatter.getSettings(m_config).containsKey(settingName);
            } else {
                exists = m_transformElement.getSettings().get(settingName) != null;
            }
            return new CmsJspElementSettingValueWrapper(
                CmsJspStandardContextBean.this,
                m_transformElement.getSettings().get(settingName),
                exists);
        }
    }

    /**
     * Bean containing a template name and URI.<p>
     */
    public static class TemplateBean {

        /** True if the template context was manually selected. */
        private boolean m_forced;

        /** The template name. */
        private String m_name;

        /** The template resource. */
        private CmsResource m_resource;

        /** The template uri, if no resource is set. */
        private String m_uri;

        /**
         * Creates a new instance.<p>
         *
         * @param name the template name
         * @param resource the template resource
         */
        public TemplateBean(String name, CmsResource resource) {

            m_resource = resource;
            m_name = name;
        }

        /**
         * Creates a new instance with an URI instead of a resoure.<p>
         *
         * @param name the template name
         * @param uri the template uri
         */
        public TemplateBean(String name, String uri) {

            m_name = name;
            m_uri = uri;
        }

        /**
         * Gets the template name.<p>
         *
         * @return the template name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the template resource.<p>
         *
         * @return the template resource
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Gets the template uri.<p>
         *
         * @return the template URI.
         */
        public String getUri() {

            if (m_resource != null) {
                return m_resource.getRootPath();
            } else {
                return m_uri;
            }
        }

        /**
         * Returns true if the template context was manually selected.<p>
         *
         * @return true if the template context was manually selected
         */
        public boolean isForced() {

            return m_forced;
        }

        /**
         * Sets the 'forced' flag to a new value.<p>
         *
         * @param forced the new value
         */
        public void setForced(boolean forced) {

            m_forced = forced;
        }

    }

    /**
     * The meta mappings transformer.<p>
     */
    class MetaLookupTranformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object arg0) {

            String result = null;
            if ((m_metaMappings != null) && m_metaMappings.containsKey(arg0)) {
                MetaMapping mapping = m_metaMappings.get(arg0);
                CmsGalleryNameMacroResolver resolver = null;
                try {
                    CmsResourceFilter filter = getIsEditMode()
                    ? CmsResourceFilter.IGNORE_EXPIRATION
                    : CmsResourceFilter.DEFAULT;
                    CmsResource res = m_cms.readResource(mapping.m_contentId, filter);
                    CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, res, m_request);
                    resolver = new CmsGalleryNameMacroResolver(m_cms, content, getLocale());
                    if (content.hasLocale(getLocale())) {
                        I_CmsXmlContentValue val = content.getValue(mapping.m_elementXPath, getLocale());
                        if (val != null) {
                            result = val.getStringValue(m_cms);
                        }
                    }

                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                if (result == null) {
                    result = mapping.m_defaultValue;
                }
                if ((resolver != null) && (result != null)) {
                    result = resolver.resolveMacros(result);
                }
            }
            return result;
        }

    }

    /** The meta mapping data. */
    class MetaMapping {

        /** The mapping content structure id. */
        CmsUUID m_contentId;

        /** The default value. */
        String m_defaultValue;

        /** The mapping value xpath. */
        String m_elementXPath;

        /** The mapping key. */
        String m_key;

        /** The mapping order. */
        int m_order;
    }

    /** The attribute name of the cms object.*/
    public static final String ATTRIBUTE_CMS_OBJECT = "__cmsObject";

    /** The attribute name of the standard JSP context bean. */
    public static final String ATTRIBUTE_NAME = "cms";

    /** The logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsJspStandardContextBean.class);

    /** OpenCms user context. */
    protected CmsObject m_cms;

    /** The sitemap configuration. */
    protected CmsADEConfigData m_config;

    /** The meta mapping configuration. */
    Map<String, MetaMapping> m_metaMappings;

    /** The current request. */
    ServletRequest m_request;

    /** Lazily initialized map from a category path to all sub-categories of that category. */
    private Map<String, CmsJspCategoryAccessBean> m_allSubCategories;

    /** Lazily initialized nested map for reading either attributes or properties (first key: file name, second key: attribute / property name). */
    private Map<String, Map<String, CmsJspObjectValueWrapper>> m_attributesOrProperties;

    /** Lazily initialized map from a category path to the path's category object. */
    private Map<String, CmsCategory> m_categories;

    /** The container the currently rendered element is part of. */
    private CmsContainerBean m_container;

    /** The current detail content resource if available. */
    private CmsResource m_detailContentResource;

    /** The detail function page. */
    private CmsResource m_detailFunctionPage;

    /** The detail only page references containers that are only displayed in detail view. */
    private CmsContainerPageBean m_detailOnlyPage;

    /** Flag to indicate if element was just edited. */
    private boolean m_edited;

    /** The currently rendered element. */
    private CmsContainerElementBean m_element;

    /** The elements of the current page. */
    private Map<String, CmsContainerElementBean> m_elementInstances;

    /** Flag to force edit mode to be disabled. */
    private boolean m_forceDisableEditMode;

    /** The lazy initialized map which allows access to the dynamic function beans. */
    private Map<String, CmsDynamicFunctionBeanWrapper> m_function;

    /** The lazy initialized map for the function detail pages. */
    private Map<String, String> m_functionDetailPage;

    /** The lazy initialized map for the function detail pages. */
    private Map<String, String> m_functionDetailPageExact;

    /** Indicates if in drag mode. */
    private boolean m_isDragMode;

    /** Stores the edit mode info. */
    private Boolean m_isEditMode;

    /** Lazily initialized map from the locale to the localized title property. */
    private Map<String, String> m_localeTitles;

    /** The currently displayed container page. */
    private CmsContainerPageBean m_page;

    /** The current container page resource, lazy initialized. */
    private CmsJspResourceWrapper m_pageResource;

    /** The parent containers to the given element instance ids. */
    private Map<String, CmsContainerBean> m_parentContainers;

    /** Lazily initialized map from a category path to all categories on that path. */
    private Map<String, List<CmsCategory>> m_pathCategories;

    /** Lazily initialized map from the root path of a resource to all categories assigned to the resource. */
    private Map<String, CmsJspCategoryAccessBean> m_resourceCategories;

    /** Map from root paths to site relative paths. */
    private Map<String, String> m_sitePaths;

    /** The template plugins. */
    private Map<String, List<CmsTemplatePluginWrapper>> m_templatePlugins;

    /** The lazy initialized map for the detail pages. */
    private Map<String, String> m_typeDetailPage;

    /** The VFS content access bean. */
    private CmsJspVfsAccessBean m_vfsBean;

    /**
     * Creates an empty instance.<p>
     */
    private CmsJspStandardContextBean() {

    }

    /**
     * Creates a new standard JSP context bean.
     *
     * @param req the current servlet request
     */
    private CmsJspStandardContextBean(ServletRequest req) {

        this();
        CmsFlexController controller = CmsFlexController.getController(req);
        m_request = req;
        CmsObject cms;
        if (controller != null) {
            cms = controller.getCmsObject();
        } else {
            cms = (CmsObject)req.getAttribute(ATTRIBUTE_CMS_OBJECT);
        }
        if (cms == null) {
            // cms object unavailable - this request was not initialized properly
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_MISSING_CMS_CONTROLLER_1, CmsJspBean.class.getName()));
        }
        updateCmsObject(cms);
        m_detailContentResource = CmsDetailPageResourceHandler.getDetailResource(req);
        m_detailFunctionPage = CmsDetailPageResourceHandler.getDetailFunctionPage(req);
    }

    /**
     * Gets the link to a function detail page.
     *
     * @param cms the CMS context
     * @param prefix the function detail prefix
     * @param functionName the function name
     * @param fullLink true if links should be generated with server prefix
     *
     * @return the link
     */
    public static String getFunctionDetailLink(CmsObject cms, String prefix, String functionName, boolean fullLink) {

        String type = prefix + functionName;

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.addSiteRoot(cms.getRequestContext().getUri()));
        List<CmsDetailPageInfo> detailPages = config.getDetailPagesForType(type);
        CmsDetailPageInfo detailPage = null;
        if ((detailPages == null) || (detailPages.size() == 0)) {
            detailPage = config.getDefaultDetailPage();
        } else {
            detailPage = detailPages.get(0);
        }
        if (detailPage == null) {
            return "[No detail page configured for type =" + type + "=]";
        }

        CmsUUID id = detailPage.getId();
        try {
            CmsResource r = cms.readResource(id);
            boolean originalForceAbsoluteLinks = cms.getRequestContext().isForceAbsoluteLinks();
            try {
                cms.getRequestContext().setForceAbsoluteLinks(fullLink || originalForceAbsoluteLinks);
                String link = OpenCms.getLinkManager().substituteLink(cms, r);
                return link;
            } finally {
                cms.getRequestContext().setForceAbsoluteLinks(originalForceAbsoluteLinks);
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return "[Error reading detail page for type =" + type + "=]";
        }
    }

    /**
     * Gets the link to a function detail page.
     *
     * <p>This just returns null if no function detail page is defined, it does not use the default detail page as a fallback.
     *
     * @param cms the CMS context
     * @param functionName the function name
     *
     * @return the link
     */
    public static String getFunctionDetailLinkExact(CmsObject cms, String functionName) {

        String type = CmsDetailPageInfo.FUNCTION_PREFIX + functionName;

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
            cms,
            cms.addSiteRoot(cms.getRequestContext().getUri()));
        List<CmsDetailPageInfo> detailPages = config.getDetailPagesForType(type);

        CmsDetailPageInfo detailPage = null;
        if ((detailPages == null) || (detailPages.size() == 0)) {
            return null;
        }
        detailPage = detailPages.get(0);
        if (detailPage.isDefaultDetailPage()) {
            return null;
        }

        CmsUUID id = detailPage.getId();
        try {
            CmsResource r = cms.readResource(id);
            String link = OpenCms.getLinkManager().substituteLink(cms, r);
            return link;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Creates a new instance of the standard JSP context bean.<p>
     *
     * To prevent multiple creations of the bean during a request, the OpenCms request context
     * attributes are used to cache the created VFS access utility bean.<p>
     *
     * @param req the current servlet request
     *
     * @return a new instance of the standard JSP context bean
     */
    public static CmsJspStandardContextBean getInstance(ServletRequest req) {

        Object attribute = req.getAttribute(ATTRIBUTE_NAME);
        CmsJspStandardContextBean result;
        if ((attribute != null) && (attribute instanceof CmsJspStandardContextBean)) {
            result = (CmsJspStandardContextBean)attribute;
        } else {
            result = new CmsJspStandardContextBean(req);
            req.setAttribute(ATTRIBUTE_NAME, result);
        }
        return result;
    }

    /**
     * Returns a copy of this JSP context bean.<p>
     *
     * @return a copy of this JSP context bean
     */
    public CmsJspStandardContextBean createCopy() {

        CmsJspStandardContextBean result = new CmsJspStandardContextBean();
        result.m_container = m_container;
        if (m_detailContentResource != null) {
            result.m_detailContentResource = m_detailContentResource.getCopy();
        }
        result.m_element = m_element;
        result.m_forceDisableEditMode = m_forceDisableEditMode;
        result.setPage(m_page);
        return result;
    }

    /**
     * Uses the default text encryption method to decrypt an encrypted string.
     *
     * @param text the encrypted stirng
     * @return the decrypted string
     */
    public String decrypt(String text) {

        try {
            return OpenCms.getTextEncryptions().get("default").decrypt(text);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Returns a caching hash specific to the element, it's properties and the current container width.<p>
     *
     * @return the caching hash
     */
    public String elementCachingHash() {

        String result = "";
        if (m_element != null) {
            result = m_element.editorHash();
            if (m_container != null) {
                result += "w:"
                    + m_container.getWidth()
                    + "cName:"
                    + m_container.getName()
                    + "cType:"
                    + m_container.getType();
            }
        }
        return result;
    }

    /**
     * Uses the default text encryption to encrypt an input text.
     *
     * @param text the input text
     * @return the encrypted text
     */
    public String encrypt(String text) {

        try {
            return OpenCms.getTextEncryptions().get("default").encrypt(text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the resource with the given path exists.
     *
     * @param path a path
     * @return true if the resource exists
     */
    public boolean exists(String path) {

        Boolean exists = getVfs().getExists().get(path);
        return exists != null ? exists.booleanValue() : false;

    }

    /**
     * Returns the locales available for the currently requested URI.
     *
     * @return the locales available for the currently requested URI.
     */
    public List<Locale> getAvailableLocales() {

        return OpenCms.getLocaleManager().getAvailableLocales(m_cms, getRequestContext().getUri());
    }

    /**
     * Helper for easy instantiation and initialization of custom context beans that returns
     * an instance of the class specified via <code>className</code>, with the current context already set.
     *
     * @param className name of the class to instantiate. Must be a subclass of {@link A_CmsJspCustomContextBean}.
     * @return an instance of the provided class with the current context already set.
     */
    public Object getBean(String className) {

        try {
            Class<?> clazz = Class.forName(className);
            if (A_CmsJspCustomContextBean.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor();
                Object instance = constructor.newInstance();
                Method setContextMethod = clazz.getMethod("setContext", CmsJspStandardContextBean.class);
                setContextMethod.invoke(instance, this);
                return instance;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            LOG.error(Messages.get().container(Messages.ERR_NO_CUSTOM_BEAN_1, className));
        }
        return null;

    }

    /**
     * Finds the folder to use for binary uploads, based on the list configuration given as an argument or
     * the current sitemap configuration.
     *
     * @param content the list configuration content
     *
     * @return the binary upload folder
     */
    public String getBinaryUploadFolder(CmsJspContentAccessBean content) {

        String keyToFind = CmsADEConfigData.ATTR_BINARY_UPLOAD_TARGET;
        String baseValue = null;
        if (content != null) {
            for (CmsJspContentAccessValueWrapper wrapper : content.getValueList().get(
                CmsConfigParserUtils.N_PARAMETER)) {
                String paramKey = wrapper.getValue().get(CmsConfigParserUtils.N_KEY).getToString();
                String paramValue = wrapper.getValue().get(CmsConfigParserUtils.N_VALUE).getToString();
                if (paramKey.equals(keyToFind)) {
                    LOG.debug("Found upload folder in configuration: " + paramValue);
                    baseValue = paramValue;
                    break;
                }
            }

            if (baseValue == null) {
                List<CmsJspContentAccessValueWrapper> folderEntries = content.getValueList().get(
                    CmsConfigParserUtils.N_SEARCH_FOLDER);
                if (folderEntries.size() == 1) {
                    CmsResource resource = folderEntries.get(0).getToResource();
                    List<String> galleryTypes = Arrays.asList(
                        CmsAjaxDownloadGallery.GALLERYTYPE_NAME,
                        CmsAjaxImageGallery.GALLERYTYPE_NAME);
                    if ((resource != null) && (null != findAncestor(m_cms, resource, (ancestor) -> {
                        return galleryTypes.stream().anyMatch(
                            type -> OpenCms.getResourceManager().matchResourceType(type, ancestor.getTypeId()));
                    }))) {
                        baseValue = m_cms.getSitePath(resource);
                        LOG.debug(
                            "Using single download gallery from search folder configuration as upload folder: "
                                + baseValue);

                    }
                }
            }
        }

        if (baseValue == null) {
            baseValue = m_config.getAttribute(keyToFind, null);
            if (baseValue != null) {
                LOG.debug("Found upload folder in sitemap configuration: " + baseValue);
            }
        }

        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setCmsObject(getCmsObject());
        resolver.addMacro("subsitepath", CmsFileUtil.removeTrailingSeparator(getSubSitePath()));
        resolver.addMacro("sitepath", "/");

        // if baseValue is still null, then resolveMacros will just return null
        String result = resolver.resolveMacros(baseValue);

        LOG.debug("Final value for upload folder : " + result);
        return result;
    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri.
     *
     * If the bundle for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    public String getBundleEditorLink(String messageKey) {

        return getBundleEditorLink(messageKey, null);
    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri with the provided backLinkAnchor added as anchor..
     *
     * If the bundle for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     * @param backLinkAnchor the anchor id to add to the backlink to the page. If <code>null</code> no anchor is added to the backlink.
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    public String getBundleEditorLink(String messageKey, String backLinkAnchor) {

        return getBundleEditorLink(messageKey, backLinkAnchor, null);
    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri with the provided backLinkAnchor added as anchor.
     *
     * If the bundle resource for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     * @param backLinkAnchor the anchor id to add to the backlink to the page. If <code>null</code> no anchor is added to the backlink.
     * @param backLinkParams request parameters to add to the backlink without leading '?', e.g. "param1=a&param2=b".
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    public String getBundleEditorLink(String messageKey, String backLinkAnchor, String backLinkParams) {

        return getBundleEditorLink(messageKey, backLinkAnchor, backLinkParams, null, null);
    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri with the provided backLinkAnchor added as anchor.
     *
     * If the bundle resource for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     * @param backLinkAnchor the anchor id to add to the backlink to the page. If <code>null</code> no anchor is added to the backlink.
     * @param backLinkParams request parameters to add to the backlink without leading '?', e.g. "param1=a&param2=b".
     * @param bundleFilters substrings of names of bundles to be preferred when multiple bundles contain the key.
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    public String getBundleEditorLink(
        String messageKey,
        String backLinkAnchor,
        String backLinkParams,
        List<String> bundleFilters) {

        return getBundleEditorLink(messageKey, backLinkAnchor, backLinkParams, null, bundleFilters);

    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri with the provided backLinkAnchor added as anchor.
     *
     * If the bundle resource for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     * @param backLinkAnchor the anchor id to add to the backlink to the page. If <code>null</code> no anchor is added to the backlink.
     * @param backLinkParams request parameters to add to the backlink without leading '?', e.g. "param1=a&param2=b".
     * @param bundleName the name of the bundle to search the key in. If <code>null</code> the bundle is detected automatically.
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    public String getBundleEditorLinkForBundle(
        String messageKey,
        String backLinkAnchor,
        String backLinkParams,
        String bundleName) {

        return getBundleEditorLink(messageKey, backLinkAnchor, backLinkParams, bundleName, null);

    }

    /**
     * Gets the root path for the VFS-based message bundle containing the given message key.
     *
     * <p>If no VFS-based message bundle contains the given key, null is returned. If multiple message bundles contain it,
     * the name filters are applied in the given order until at least one bundle matches a filter.
     * If multiple bundles match, one of them is arbitrarily chosen (but a warning is logged).
     * If no bundle matches, an arbitrary bundle is chosen (but also a warning is logged).
     *
     * <p>Note: This uses the online (published) state of message bundles, so if you have unpublished bundle changes, they will not be reflected in
     * the result.
     *
     * @param messageKey the message key
     * @param bundleFilters substrings of names of bundles to be preferred when multiple bundles contain the key.
     * @return the root path of the bundle containing the message key
     */
    public String getBundleRootPath(String messageKey, List<String> bundleFilters) {

        CmsObject cms = getCmsObject();
        try {
            CmsMessageToBundleIndex bundleIndex = null;
            OpenCmsServlet.RequestCache context = OpenCmsServlet.getRequestCache();
            if (context != null) {
                bundleIndex = (CmsMessageToBundleIndex)context.getAttribute(
                    CmsMessageToBundleIndex.class.getName() + "_" + cms.getRequestContext().getLocale(),
                    k -> {
                        try {
                            CmsMessageToBundleIndex result = CmsMessageToBundleIndex.read(getCmsObject());
                            return result;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            } else {
                bundleIndex = CmsMessageToBundleIndex.read(getCmsObject());
            }
            Collection<String> bundles = bundleIndex.getBundlesPathForKey(messageKey);
            switch (bundles.size()) {
                case 0:
                    return null;
                case 1:
                    return bundles.iterator().next();
                default:
                    if (!((null == bundleFilters) || bundleFilters.isEmpty())) {
                        for (String filter : bundleFilters) {
                            Set<String> matchingBundles = new HashSet<>(bundles.size());
                            for (String bundle : bundles) {
                                if (bundle.contains(filter)) {
                                    matchingBundles.add(bundle);
                                }
                            }
                            if (matchingBundles.size() > 0) {
                                if (matchingBundles.size() > 1) {
                                    LOG.warn(
                                        "Ambiguous message bundle for key "
                                            + messageKey
                                            + " and filter "
                                            + filter
                                            + ":"
                                            + matchingBundles);
                                }
                                return matchingBundles.iterator().next();
                            }
                        }
                    }
                    LOG.warn("Ambiguous message bundle for key " + messageKey + ":" + bundles);
                    return bundles.iterator().next();
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns the container the currently rendered element is part of.<p>
     *
     * @return the container the currently rendered element is part of
     */
    public CmsContainerBean getContainer() {

        return m_container;
    }

    /**
     * Gets information about a given container type.
     *
     * @param containerType the container type
     *
     * @return the bean with the information about the container type
     */
    public CmsContainerTypeInfoWrapper getContainerTypeInfo(String containerType) {

        return new CmsContainerTypeInfoWrapper(this, m_cms, m_config, containerType);
    }

    /**
     * Gets the CmsObject from the current Flex controller.
     *
     * @return the CmsObject from the current Flex controller
     */
    public CmsObject getControllerCms() {

        return CmsFlexController.getController(m_request).getCmsObject();
    }

    /**
     * Returns the current detail content, or <code>null</code> if no detail content is requested.<p>
     *
     * @return the current detail content, or <code>null</code> if no detail content is requested.<p>
     */
    public CmsJspResourceWrapper getDetailContent() {

        return CmsJspResourceWrapper.wrap(m_cms, m_detailContentResource);
    }

    /**
     * Returns the structure id of the current detail content, or <code>null</code> if no detail content is requested.<p>
     *
     * @return the structure id of the current detail content, or <code>null</code> if no detail content is requested.<p>
     */
    public CmsUUID getDetailContentId() {

        return m_detailContentResource == null ? null : m_detailContentResource.getStructureId();
    }

    /**
     * Returns the detail content site path, or <code>null</code> if not available.<p>
     *
     * @return the detail content site path
     */
    public String getDetailContentSitePath() {

        return ((m_cms == null) || (m_detailContentResource == null))
        ? null
        : m_cms.getSitePath(m_detailContentResource);
    }

    /**
     * Returns the detail function page.<p>
     *
     * @return the detail function page
     */
    public CmsJspResourceWrapper getDetailFunctionPage() {

        return CmsJspResourceWrapper.wrap(m_cms, m_detailFunctionPage);
    }

    /**
     * Returns the detail only page.<p>
     *
     * @return the detail only page
     */
    public CmsContainerPageBean getDetailOnlyPage() {

        if ((null == m_detailOnlyPage) && (null != m_detailContentResource)) {
            String pageRootPath = m_cms.getRequestContext().addSiteRoot(m_cms.getRequestContext().getUri());
            m_detailOnlyPage = CmsDetailOnlyContainerUtil.getDetailOnlyPage(m_cms, m_request, pageRootPath, false);
        }
        return m_detailOnlyPage;
    }

    /**
     * Returns the currently rendered element.<p>
     *
     * @return the currently rendered element
     */
    public CmsContainerElementWrapper getElement() {

        return m_element != null ? new CmsContainerElementWrapper(m_element) : null;
    }

    /**
     * Returns a lazy initialized map of wrapped container elements beans by container name suffix.<p>
     *
     * So in case there is more than one container where the name end with the given suffix,
     * a joined list of container elements beans is returned.<p>
     *
     * @return a lazy initialized map of wrapped container elements beans by container name suffix
     *
     * @see #getElementsInContainer()
     */
    public Map<String, List<CmsContainerElementWrapper>> getElementBeansInContainers() {

        return CmsCollectionsGenericWrapper.createLazyMap(obj -> {
            if (obj instanceof String) {
                List<CmsContainerElementBean> containerElements = new ArrayList<>();
                for (CmsContainerBean container : getPage().getContainers().values()) {
                    if (container.getName().endsWith("-" + obj)) {
                        for (CmsContainerElementBean element : container.getElements()) {
                            try {
                                element.initResource(m_cms);
                                containerElements.add(new CmsContainerElementWrapper(element));
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
                return containerElements;
            } else {
                return null;
            }
        });

    }

    /**
     * Returns a lazy initialized map of wrapped element resources by container name.<p>
     *
     * @return the lazy map of element resource wrappers
     */
    public Map<String, List<CmsJspResourceWrapper>> getElementsInContainer() {

        return CmsCollectionsGenericWrapper.createLazyMap(obj -> {
            if (obj instanceof String) {
                List<CmsJspResourceWrapper> elements = new ArrayList<>();
                CmsContainerBean container = getPage().getContainers().get(obj);
                if (container != null) {
                    for (CmsContainerElementBean element : container.getElements()) {
                        try {
                            element.initResource(m_cms);
                            elements.add(CmsJspResourceWrapper.wrap(m_cms, element.getResource()));
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
                return elements;
            } else {
                return null;
            }
        });

    }

    /**
     * Returns a lazy initialized map of wrapped element resources by container name suffix.<p>
     *
     * So in case there is more than one container where the name end with the given suffix,
     * a joined list of elements is returned.<p>
     *
     * @return the lazy map of element resource wrappers
     *
     * @see #getElementBeansInContainers()
     */
    public Map<String, List<CmsJspResourceWrapper>> getElementsInContainers() {

        return CmsCollectionsGenericWrapper.createLazyMap(obj -> {
            if (obj instanceof String) {
                List<CmsJspResourceWrapper> elements = new ArrayList<>();
                for (CmsContainerBean container : getPage().getContainers().values()) {
                    if (container.getName().endsWith("-" + obj)) {
                        for (CmsContainerElementBean element : container.getElements()) {
                            try {
                                element.initResource(m_cms);
                                elements.add(CmsJspResourceWrapper.wrap(m_cms, element.getResource()));
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
                return elements;
            } else {
                return null;
            }
        });

    }

    /**
     * Alternative method name for getReloadMarker().
     *
     * @see org.opencms.jsp.util.CmsJspStandardContextBean#getReloadMarker()
     *
     * @return the reload marker
     */
    public String getEnableReload() {

        return getReloadMarker();
    }

    /**
     * Gets the formatter info wrapper for the given formatter key.
     *
     * @param formatterKey a formatter key
     * @return the formatter information for the formatter key, or null if no formatter was found
     */
    public CmsFormatterInfoWrapper getFormatterInfo(String formatterKey) {

        CmsObject cms = m_cms;
        CmsADEConfigData config = m_config;
        I_CmsFormatterBean formatter = config.findFormatter(formatterKey);
        if (formatter == null) {
            return null;
        }
        return new CmsFormatterInfoWrapper(cms, config, formatter);

    }

    /**
     * Gets the formatter bean for active formatters with a given container type.
     *
     * @param containerType the container type
     * @return the wrapped formatters
     */
    public List<CmsFormatterInfoWrapper> getFormatterInfoForContainer(String containerType) {

        return wrapFormatters(m_config.getActiveFormattersWithContainerType(containerType));

    }

    /**
     * Gets the formatter beans for active formatters with a given display type.
     *
     * @param displayType the display type
     * @return the wrapped formatters
     */
    public List<CmsFormatterInfoWrapper> getFormatterInfoForDisplay(String displayType) {

        CmsADEConfigData config = m_config;
        return wrapFormatters(config.getActiveFormattersWithDisplayType(displayType));
    }

    /**
     * Gets a lazy map which can be used to access element setting defaults for a specific formatter key and setting name.
     *
     * @return the lazy map
     */
    public Map<String, Map<String, CmsJspObjectValueWrapper>> getFormatterSettingDefault() {

        return CmsCollectionsGenericWrapper.createLazyMap(input -> {
            String formatterKey = (String)input;
            I_CmsFormatterBean formatter = m_config.findFormatter(formatterKey);
            if (formatter == null) {
                return CmsCollectionsGenericWrapper.createLazyMap(input2 -> {
                    return CmsJspObjectValueWrapper.NULL_VALUE_WRAPPER;
                });
            } else {
                final Map<String, CmsXmlContentProperty> settingDefs = formatter.getSettings(m_config);
                return CmsCollectionsGenericWrapper.createLazyMap(input2 -> {
                    String settingName = (String)input2;
                    CmsXmlContentProperty settingDef = settingDefs.get(settingName);
                    if (settingDef == null) {
                        return CmsJspObjectValueWrapper.NULL_VALUE_WRAPPER;
                    } else {
                        String settingDefault = settingDef.getDefault();
                        return CmsJspObjectValueWrapper.createWrapper(m_cms, settingDefault);
                    }
                });
            }
        });
    }

    /**
     * Returns a lazy initialized Map which allows access to the dynamic function beans using the JSP EL.<p>
     *
     * When given a key, the returned map will look up the corresponding dynamic function bean in the module configuration.<p>
     *
     * @return a lazy initialized Map which allows access to the dynamic function beans using the JSP EL
     */
    public Map<String, CmsDynamicFunctionBeanWrapper> getFunction() {

        if (m_function == null) {

            Transformer transformer = new Transformer() {

                @Override
                public Object transform(Object input) {

                    try {
                        CmsDynamicFunctionBean dynamicFunction = readDynamicFunctionBean((String)input);
                        CmsDynamicFunctionBeanWrapper wrapper = new CmsDynamicFunctionBeanWrapper(
                            m_cms,
                            dynamicFunction);
                        return wrapper;

                    } catch (CmsException e) {
                        LOG.debug(e.getLocalizedMessage(), e);
                        return new CmsDynamicFunctionBeanWrapper(m_cms, null);
                    }
                }
            };
            m_function = CmsCollectionsGenericWrapper.createLazyMap(transformer);
        }
        return m_function;

    }

    /**
     * Deprecated method to access function detail pages using the EL.<p>
     *
     * @return a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function as a key
     *
     * @deprecated use {@link #getFunctionDetailPage()} instead
     */
    @Deprecated
    public Map<String, String> getFunctionDetail() {

        return getFunctionDetailPage();
    }

    /**
     * Returns a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function as a key.<p>
     *
     * The provided Map key is assumed to be a String that represents a named dynamic function.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;a href=${cms.functionDetailPage['search']} /&gt
     * </pre>
     *
     * @return a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function as a key
     *
     * @see #getTypeDetailPage()
     */
    public Map<String, String> getFunctionDetailPage() {

        if (m_functionDetailPage == null) {
            m_functionDetailPage = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsDetailLookupTransformer(CmsDetailPageInfo.FUNCTION_PREFIX));
        }
        return m_functionDetailPage;
    }

    /**
     * Returns a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function as a key.<p>
     *
     * The provided Map key is assumed to be a String that represents a named dynamic function.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;a href=${cms.functionDetailPage['search']} /&gt
     * </pre>
     *
     * @return a lazy initialized Map that provides the detail page link as a value when given the name of a
     * (named) dynamic function as a key
     *
     * @see #getTypeDetailPage()
     */
    public Map<String, String> getFunctionDetailPageExact() {

        if (m_functionDetailPageExact == null) {
            m_functionDetailPageExact = CmsCollectionsGenericWrapper.createLazyMap(
                name -> getFunctionDetailLinkExact(m_cms, (String)name));
        }
        return m_functionDetailPageExact;
    }

    /**
     * Returns a lazy map which creates a wrapper object for a dynamic function format when given an XML content
     * as a key.<p>
     *
     * @return a lazy map for accessing function formats for a content
     */
    public Map<CmsJspContentAccessBean, CmsDynamicFunctionFormatWrapper> getFunctionFormatFromContent() {

        Transformer transformer = new Transformer() {

            @Override
            public Object transform(Object contentAccess) {

                CmsXmlContent content = (CmsXmlContent)(((CmsJspContentAccessBean)contentAccess).getRawContent());
                CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
                CmsDynamicFunctionBean functionBean = null;
                try {
                    functionBean = parser.parseFunctionBean(m_cms, content);
                } catch (CmsException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                    return new CmsDynamicFunctionFormatWrapper(m_cms, null);
                }
                String type = getContainer().getType();
                String width = getContainer().getWidth();
                int widthNum = -1;
                try {
                    widthNum = Integer.parseInt(width);
                } catch (NumberFormatException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
                CmsDynamicFunctionBean.Format format = functionBean.getFormatForContainer(m_cms, type, widthNum);
                CmsDynamicFunctionFormatWrapper wrapper = new CmsDynamicFunctionFormatWrapper(m_cms, format);
                return wrapper;
            }
        };
        return CmsCollectionsGenericWrapper.createLazyMap(transformer);
    }

    /**
     * Returns <code>true</code> if the current page is a detail page.<p>
     *
     * @return <code>true</code> if the current page is a detail page
     */
    public boolean getIsDetailPage() {

        CmsJspResourceWrapper page = getPageResource();
        return OpenCms.getADEManager().isDetailPage(m_cms, page);
    }

    /**
     * Returns <code>true</code> if the current request is direct edit enabled.<p>
     *
     * Online-, history-requests, previews and temporary files will not be editable.<p>
     *
     * @return <code>true</code> if the current request is direct edit enabled
     */
    public boolean getIsEditMode() {

        if (m_isEditMode == null) {
            m_isEditMode = Boolean.valueOf(CmsJspTagEditable.isEditableRequest(m_request));
        }
        return m_isEditMode.booleanValue() && !m_forceDisableEditMode;
    }

    /**
     * Returns <code>true</code> if the current request is a JSON request.<p>
     *
     * @return <code>true</code> if we are in a JSON request
     */
    public boolean getIsJSONRequest() {

        return CmsJsonPartFilter.isJsonRequest(m_request);
    }

    /**
     * Returns <code>true</code> if the current project is the online project.<p>
     *
     * @return <code>true</code> if the current project is the online project
     */
    public boolean getIsOnlineProject() {

        return m_cms.getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Returns true if the current request is in direct edit preview mode.<p>
     *
     * This is the case if the request is not in edit mode and in the online project.<p>
     *
     * @return <code>true</code> if the current request is in direct edit preview mode
     */
    public boolean getIsPreviewMode() {

        return !getIsOnlineProject() && !getIsEditMode();
    }

    /**
     * Returns the current locale.<p>
     *
     * @return the current locale
     */
    public Locale getLocale() {

        return getRequestContext().getLocale();
    }

    /**
     * Gets a map providing access to the locale variants of the current page.<p>
     *
     * Note that all available locales for the site / subsite are used as keys, not just the ones for which a locale
     * variant actually exists.
     *
     * Usage in JSPs: ${cms.localeResource['de']]
     *
     * @return the map from locale strings to locale variant resources
     */
    public Map<String, CmsJspResourceWrapper> getLocaleResource() {

        Map<String, CmsJspResourceWrapper> result = getPageResource().getLocaleResource();
        List<Locale> locales = CmsLocaleGroupService.getPossibleLocales(m_cms, getPageResource());
        for (Locale locale : locales) {
            if (!result.containsKey(locale.toString())) {
                result.put(locale.toString(), null);
            }
        }
        return result;
    }

    /**
     * Gets the main locale for the current page's locale group.<p>
     *
     * @return the main locale for the current page's locale group
     */
    public Locale getMainLocale() {

        return getPageResource().getMainLocale();
    }

    /**
     * Returns the meta mappings map.<p>
     *
     * @return the meta mappings
     */
    public Map<String, String> getMeta() {

        initMetaMappings();
        return CmsCollectionsGenericWrapper.createLazyMap(new MetaLookupTranformer());
    }

    /**
     * Returns the currently displayed container page.<p>
     *
     * @return the currently displayed container page
     */
    public CmsContainerPageBean getPage() {

        if (null == m_page) {
            try {
                initPage();
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e, e);
                }
            }
        }
        return m_page;
    }

    /**
     * Returns the container page bean for the give page and locale.<p>
     *
     * @param page the container page resource as id, path or already as resource
     * @param locale the content locale as locale or string
     *
     * @return the container page bean
     */
    public CmsContainerPageBean getPage(Object page, Object locale) {

        CmsResource pageResource = null;
        CmsContainerPageBean result = null;
        if (m_cms != null) {
            try {
                pageResource = CmsJspElFunctions.convertRawResource(m_cms, page);
                Locale l = CmsJspElFunctions.convertLocale(locale);
                result = getPage(pageResource);
                if (result != null) {
                    CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(
                        m_cms,
                        pageResource.getRootPath());
                    for (CmsContainerBean container : result.getContainers().values()) {
                        for (CmsContainerElementBean element : container.getElements()) {
                            boolean isGroupContainer = element.isGroupContainer(m_cms);
                            boolean isInheritedContainer = element.isInheritedContainer(m_cms);
                            I_CmsFormatterBean formatterConfig = null;
                            if (!isGroupContainer && !isInheritedContainer) {
                                element.initResource(m_cms);
                                // ensure that the formatter configuration id is added to the element settings, so it will be persisted on save
                                formatterConfig = CmsJspTagContainer.getFormatterConfigurationForElement(
                                    m_cms,
                                    element,
                                    adeConfig,
                                    container.getName(),
                                    "",
                                    0);
                                if (formatterConfig != null) {
                                    element.initSettings(m_cms, adeConfig, formatterConfig, l, m_request, null);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

        }
        return result;
    }

    /**
     * Returns the current container page resource.<p>
     *
     * @return the current container page resource
     */
    public CmsJspResourceWrapper getPageResource() {

        try {
            if (m_pageResource == null) {
                // get the container page itself, checking the history first
                m_pageResource = CmsJspResourceWrapper.wrap(
                    m_cms,
                    (CmsResource)CmsHistoryResourceHandler.getHistoryResource(m_request));
                if (m_pageResource == null) {
                    m_pageResource = CmsJspResourceWrapper.wrap(
                        m_cms,
                        m_cms.readResource(
                            m_cms.getRequestContext().getUri(),
                            CmsResourceFilter.ignoreExpirationOffline(m_cms)));
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return m_pageResource;
    }

    /**
     * Returns the parent container to the current container if available.<p>
     *
     * @return the parent container
     */
    public CmsContainerBean getParentContainer() {

        CmsContainerBean result = null;
        if ((getContainer() != null) && (getContainer().getParentInstanceId() != null)) {
            result = m_parentContainers.get(getContainer().getParentInstanceId());
        }
        return result;
    }

    /**
     * Returns the instance id parent container mapping.<p>
     *
     * @return the instance id parent container mapping
     */
    public Map<String, CmsContainerBean> getParentContainers() {

        if (m_parentContainers == null) {
            initPageData();
        }
        return Collections.unmodifiableMap(m_parentContainers);
    }

    /**
     * Returns the parent element to the current element if available.<p>
     *
     * @return the parent element or null
     */
    public CmsContainerElementBean getParentElement() {

        return getParentElement(getElement());
    }

    /**
     * Gets the set of plugin group names.
     *
     * @return the set of plugin group names
     */
    public Set<String> getPluginGroups() {

        return getPlugins().keySet();
    }

    /**
     * Gets the map of plugins by group.
     *
     * @return the map of active plugins by group
     */
    public Map<String, List<CmsTemplatePluginWrapper>> getPlugins() {

        if (m_templatePlugins == null) {
            final Multimap<String, CmsTemplatePlugin> templatePluginsMultimap = new CmsTemplatePluginFinder(
                this).getTemplatePlugins();
            Map<String, List<CmsTemplatePluginWrapper>> templatePlugins = new HashMap<>();
            for (String key : templatePluginsMultimap.keySet()) {
                List<CmsTemplatePluginWrapper> wrappers = new ArrayList<>();
                for (CmsTemplatePlugin plugin : templatePluginsMultimap.get(key)) {
                    wrappers.add(new CmsTemplatePluginWrapper(m_cms, plugin));
                }
                templatePlugins.put(key, Collections.unmodifiableList(wrappers));
            }
            m_templatePlugins = templatePlugins;
        }
        return m_templatePlugins;
    }

    /**
     * JSP EL accessor method for retrieving the preview formatters.<p>
     *
     * @return a lazy map for accessing preview formatters
     */
    public Map<String, String> getPreviewFormatter() {

        Transformer transformer = new Transformer() {

            @Override
            public Object transform(Object uri) {

                try {
                    String rootPath = m_cms.getRequestContext().addSiteRoot((String)uri);
                    CmsResource resource = m_cms.readResource((String)uri);
                    CmsADEManager adeManager = OpenCms.getADEManager();
                    CmsADEConfigData configData = adeManager.lookupConfiguration(m_cms, rootPath);
                    CmsFormatterConfiguration formatterConfig = configData.getFormatters(m_cms, resource);
                    if (formatterConfig == null) {
                        return "";
                    }
                    I_CmsFormatterBean previewFormatter = formatterConfig.getPreviewFormatter();
                    if (previewFormatter == null) {
                        return "";
                    }
                    CmsUUID structureId = previewFormatter.getJspStructureId();
                    m_cms.readResource(structureId);
                    CmsResource formatterResource = m_cms.readResource(structureId);
                    String formatterSitePath = m_cms.getRequestContext().removeSiteRoot(
                        formatterResource.getRootPath());
                    return formatterSitePath;
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return "";
                }
            }
        };
        return CmsCollectionsGenericWrapper.createLazyMap(transformer);
    }

    /**
     * Reads all sub-categories below the provided category.
     * @return The map from the provided category to it's sub-categories in a {@link CmsJspCategoryAccessBean}.
     */
    public Map<String, CmsJspCategoryAccessBean> getReadAllSubCategories() {

        if (null == m_allSubCategories) {
            m_allSubCategories = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(Object categoryPath) {

                    try {
                        List<CmsCategory> categories = CmsCategoryService.getInstance().readCategories(
                            m_cms,
                            (String)categoryPath,
                            true,
                            m_cms.getRequestContext().getUri());
                        CmsJspCategoryAccessBean result = new CmsJspCategoryAccessBean(
                            m_cms,
                            categories,
                            (String)categoryPath);
                        return result;
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        return null;
                    }
                }

            });
        }
        return m_allSubCategories;
    }

    /**
     * Lazily reads the given attribute from the current sitemap or a property of the same name from the given resource.
     *
     * <p>Usage example: ${cms.readAttributeOrProperty['/index.html']['attr']}
     *
     * @return a lazy loading map for accessing attributes / properties
     */
    public Map<String, Map<String, CmsJspObjectValueWrapper>> getReadAttributeOrProperty() {

        if (m_attributesOrProperties == null) {
            m_attributesOrProperties = CmsCollectionsGenericWrapper.createLazyMap(pathObj -> {
                return CmsCollectionsGenericWrapper.createLazyMap(keyObj -> {

                    String path = (String)pathObj;
                    String key = (String)keyObj;

                    CmsObject cms = getCmsObject();
                    String result = m_config.getAttribute(key, null);
                    if (result == null) {
                        try {
                            CmsProperty prop = cms.readPropertyObject(path, key, /*search=*/true);
                            result = prop.getValue();
                        } catch (CmsVfsResourceNotFoundException e) {
                            LOG.info(e.getLocalizedMessage(), e);
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    return CmsJspObjectValueWrapper.createWrapper(cms, result);
                });
            });
        }
        return m_attributesOrProperties;
    }

    /**
     * Reads the categories assigned to the currently requested URI.
     * @return the categories assigned to the currently requested URI.
     */
    public CmsJspCategoryAccessBean getReadCategories() {

        return getReadResourceCategories().get(getRequestContext().getRootUri());
    }

    /**
     * Transforms the category path of a category to the category.
     * @return a map from root or site path to category.
     */
    public Map<String, CmsCategory> getReadCategory() {

        if (null == m_categories) {
            m_categories = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object categoryPath) {

                    try {
                        CmsCategoryService catService = CmsCategoryService.getInstance();
                        return catService.localizeCategory(
                            m_cms,
                            catService.readCategory(m_cms, (String)categoryPath, getRequestContext().getUri()),
                            m_cms.getRequestContext().getLocale());
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        return null;
                    }
                }

            });
        }
        return m_categories;
    }

    /**
     * Transforms the category path to the list of all categories on that path.<p>
     *
     * Example: For path <code>"location/europe/"</code>
     *          the list <code>[getReadCategory.get("location/"),getReadCategory.get("location/europe/")]</code>
     *          is returned.
     * @return a map from a category path to list of categories on that path.
     */
    public Map<String, List<CmsCategory>> getReadPathCategories() {

        if (null == m_pathCategories) {
            m_pathCategories = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object categoryPath) {

                    List<CmsCategory> result = new ArrayList<CmsCategory>();

                    String path = (String)categoryPath;

                    if ((null == path) || (path.length() <= 1)) {
                        return result;
                    }

                    //cut last slash
                    path = path.substring(0, path.length() - 1);

                    List<String> pathParts = Arrays.asList(path.split("/"));

                    String currentPath = "";
                    for (String part : pathParts) {
                        currentPath += part + "/";
                        CmsCategory category = getReadCategory().get(currentPath);
                        if (null != category) {
                            result.add(category);
                        }
                    }
                    return CmsCategoryService.getInstance().localizeCategories(
                        m_cms,
                        result,
                        m_cms.getRequestContext().getLocale());
                }

            });
        }
        return m_pathCategories;
    }

    /**
     * Reads the categories assigned to a resource.
     *
     * @return map from the resource path (root path) to the assigned categories
     */
    public Map<String, CmsJspCategoryAccessBean> getReadResourceCategories() {

        if (null == m_resourceCategories) {
            m_resourceCategories = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object resourceName) {

                    try {
                        CmsResource resource = m_cms.readResource(
                            getRequestContext().removeSiteRoot((String)resourceName));
                        return new CmsJspCategoryAccessBean(m_cms, resource);
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        return null;
                    }
                }
            });
        }
        return m_resourceCategories;
    }

    /**
     * Returns a HTML comment string that will cause the container page editor to reload the page if the element or its settings
     * were edited.<p>
     *
     * @return the reload marker
     */
    public String getReloadMarker() {

        if (m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return ""; // reload marker is not needed in Online mode
        } else {
            return CmsGwtConstants.FORMATTER_RELOAD_MARKER;
        }
    }

    /**
     * Gets the stored request.
     *
     * @return the stored request
     */
    public ServletRequest getRequest() {

        return m_request;
    }

    /**
     * Returns the request context.<p>
     *
     * @return the request context
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
    }

    /**
     * Gets information about a specific resource type for use in JSPs.
     *
     * <p>If no type with the given name exists, null is returned.
     *
     * @param typeName the type name
     * @return the bean representing the resource type
     */
    public CmsResourceTypeInfoWrapper getResourceTypeInfo(String typeName) {

        try {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeName);
            return new CmsResourceTypeInfoWrapper(this, m_cms, m_config, type);
        } catch (CmsLoaderException e) {
            LOG.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the schema information bean for the given type or XSD.
     *
     * @param typeOrXsd either the name of a resource type, or the VFS path to an XSD schema
     * @return the schema information bean
     *
     * @throws CmsException if something goes wrong
     */
    public CmsSchemaInfo getSchemaInfo(String typeOrXsd) throws CmsException {

        CmsXmlContentDefinition contentDef = null;
        if (OpenCms.getResourceManager().hasResourceType(typeOrXsd)) {
            contentDef = CmsXmlContentDefinition.getContentDefinitionForType(m_cms, typeOrXsd);
        } else if (typeOrXsd.startsWith("/")) {
            contentDef = CmsXmlContentDefinition.unmarshal(m_cms, typeOrXsd);
        } else {
            throw new IllegalArgumentException("Invalid getSchemaInfo argument: " + typeOrXsd);
        }
        CmsSchemaInfo info = new CmsSchemaInfo(m_cms, contentDef);
        return info;
    }

    /**
     * Returns the current site.<p>
     *
     * @return the current site
     */
    public CmsSite getSite() {

        return OpenCms.getSiteManager().getSiteForSiteRoot(m_cms.getRequestContext().getSiteRoot());
    }

    /**
     * Gets the wrapper for the sitemap configuration.
     *
     * @return the wrapper object for the sitemap configuration
     */
    public CmsJspSitemapConfigWrapper getSitemapConfig() {

        return new CmsJspSitemapConfigWrapper(this);
    }

    /**
     * Transforms root paths to site paths.
     *
     * @return lazy map from root paths to site paths.
     *
     * @see CmsRequestContext#removeSiteRoot(String)
     */
    public Map<String, String> getSitePath() {

        if (m_sitePaths == null) {
            m_sitePaths = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object rootPath) {

                    if (rootPath instanceof String) {
                        return getRequestContext().removeSiteRoot((String)rootPath);
                    }
                    return null;
                }
            });
        }
        return m_sitePaths;
    }

    /**
     * Returns the subsite path for the currently requested URI.<p>
     *
     * @return the subsite path
     */
    public String getSubSitePath() {

        return m_cms.getRequestContext().removeSiteRoot(
            OpenCms.getADEManager().getSubSiteRoot(m_cms, m_cms.getRequestContext().getRootUri()));
    }

    /**
     * Returns the system information.<p>
     *
     * @return the system information
     */
    public CmsSystemInfo getSystemInfo() {

        return OpenCms.getSystemInfo();
    }

    /**
     * Gets a bean containing information about the current template.<p>
     *
     * @return the template information bean
     */
    public TemplateBean getTemplate() {

        TemplateBean templateBean = getRequestAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN);
        if (templateBean == null) {
            templateBean = new TemplateBean("", "");
        }
        return templateBean;
    }

    /**
     * Returns the title of a page delivered from OpenCms, usually used for the <code>&lt;title&gt;</code> tag of
     * a HTML page.<p>
     *
     * If no title information has been found, the empty String "" is returned.<p>
     *
     * @return the title of the current page
     */
    public String getTitle() {

        return getLocaleSpecificTitle(null);

    }

    /**
     * Get the title and read the Title property according the provided locale.
     * @return The map from locales to the locale specific titles.
     */
    public Map<String, String> getTitleLocale() {

        if (m_localeTitles == null) {
            m_localeTitles = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object inputLocale) {

                    Locale locale = null;
                    if (null != inputLocale) {
                        if (inputLocale instanceof Locale) {
                            locale = (Locale)inputLocale;
                        } else if (inputLocale instanceof String) {
                            try {
                                locale = LocaleUtils.toLocale((String)inputLocale);
                            } catch (IllegalArgumentException | NullPointerException e) {
                                // do nothing, just go on without locale
                            }
                        }
                    }
                    return getLocaleSpecificTitle(locale);
                }

            });
        }
        return m_localeTitles;
    }

    /**
     * Returns a lazy initialized Map that provides the detail page link as a value when given the name of a
     * resource type as a key.<p>
     *
     * The provided Map key is assumed to be the name of a resource type that has a detail page configured.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;a href=${cms.typeDetailPage['bs-blog']} /&gt
     * </pre>
     *
     * @return a lazy initialized Map that provides the detail page link as a value when given the name of a
     * resource type as a key
     *
     * @see #getFunctionDetailPage()
     */
    public Map<String, String> getTypeDetailPage() {

        if (m_typeDetailPage == null) {
            m_typeDetailPage = CmsCollectionsGenericWrapper.createLazyMap(new CmsDetailLookupTransformer(""));
        }
        return m_typeDetailPage;
    }

    /**
     * Returns an initialized VFS access bean.<p>
     *
     * @return an initialized VFS access bean
     */
    public CmsJspVfsAccessBean getVfs() {

        if (m_vfsBean == null) {
            // create a new VVFS access bean
            m_vfsBean = CmsJspVfsAccessBean.create(m_cms);
        }
        return m_vfsBean;
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     *
     * @return returns the workplace locale from the current user's settings
     */
    public Locale getWorkplaceLocale() {

        return OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
    }

    /**
     * Returns an EL access wrapper map for the given object.<p>
     *
     * If the object is a {@link CmsResource}, then a {@link CmsJspResourceWrapper} is returned.
     * Otherwise the object is wrapped in a {@link CmsJspObjectValueWrapper}.<p>
     *
     * If the object is already is a wrapper, it is returned unchanged.<p>
     *
     * @return an EL access wrapper map for the given object
     */
    public Map<Object, Object> getWrap() {

        return CmsCollectionsGenericWrapper.createLazyMap(obj -> wrap(obj));
    }

    /**
     * Initializes the requested container page.<p>
     *
     * @throws CmsException in case reading the requested resource fails
     */
    public void initPage() throws CmsException {

        if ((m_page == null) && (m_cms != null)) {
            String requestUri = m_cms.getRequestContext().getUri();
            // get the container page itself, checking the history first
            CmsResource pageResource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(m_request);
            if (pageResource == null) {
                pageResource = m_cms.readResource(requestUri, CmsResourceFilter.ignoreExpirationOffline(m_cms));
            }
            m_config = OpenCms.getADEManager().lookupConfigurationWithCache(m_cms, pageResource.getRootPath());
            m_page = getPage(pageResource);
            m_page = CmsTemplateMapper.get(m_request).transformContainerpageBean(
                m_cms,
                m_page,
                pageResource.getRootPath());

        }
    }

    /**
     * Returns <code>true</code in case a detail page is available for the current element.<p>
     *
     * @return <code>true</code in case a detail page is available for the current element
     */
    public boolean isDetailPageAvailable() {

        boolean result = false;
        if ((m_cms != null)
            && (m_element != null)
            && !m_element.isInMemoryOnly()
            && (m_element.getResource() != null)) {
            try {
                String detailPage = OpenCms.getADEManager().getDetailPageHandler().getDetailPage(
                    m_cms,
                    m_element.getResource().getRootPath(),
                    m_cms.getRequestContext().getUri(),
                    null);
                result = detailPage != null;
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code> if this is a request to a detail resource, <code>false</code> otherwise.<p>
     *
     * Same as to check if {@link #getDetailContent()} is <code>null</code>.<p>
     *
     * @return <code>true</code> if this is a request to a detail resource, <code>false</code> otherwise
     */
    public boolean isDetailRequest() {

        return m_detailContentResource != null;
    }

    /**
     * Returns if the page is in drag mode.<p>
     *
     * @return if the page is in drag mode
     */
    public boolean isDragMode() {

        return m_isDragMode;
    }

    /**
     * Returns the flag to indicate if in drag and drop mode.<p>
     *
     * @return <code>true</code> if in drag and drop mode
     */
    public boolean isEdited() {

        return m_edited;
    }

    /**
     * Checks if the flag that forces edit mode to be disabled is set.
     *
     * @return true if the flag that disables edit mode is set
     */
    public boolean isForceDisableEditMode() {

        return m_forceDisableEditMode;
    }

    /**
     * Checks if the link is a link to a path in a different OpenCms site from the current one.
     *
     * @param link the link to check
     * @return true if the link is a link to different subsite
     */
    public boolean isLinkToDifferentSite(String link) {

        CmsObject cms = getControllerCms();
        try {
            URI uri = new URI(link);
            if (uri.getScheme() != null) {
                String sitePart = uri.getScheme() + "://" + uri.getAuthority();
                CmsSiteMatcher matcher = new CmsSiteMatcher(sitePart);
                CmsSite site = OpenCms.getSiteManager().matchSite(matcher);
                return ((site != null) && !site.getSiteRoot().equals(cms.getRequestContext().getSiteRoot()));
            } else {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Checks if the link is a link to a path in a different OpenCms subsite from the current one.
     *
     * <p>For detail links, this checks the subsite of the detail page, not the subsite of the detail content.
     *
     * @param link the link to check
     * @return true if the link is a link to different site
     */
    public boolean isLinkToDifferentSubSite(String link) {

        CmsObject cms = getControllerCms();
        String subSite = CmsLinkManager.getLinkSubsite(cms, link);
        String currentRootPath = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
        boolean result = (subSite != null)
            && !subSite.equals(OpenCms.getADEManager().getSubSiteRoot(cms, currentRootPath));
        return result;
    }

    /**
     * Returns if the current element is a model group.<p>
     *
     * @return <code>true</code> if the current element is a model group
     */
    public boolean isModelGroupElement() {

        return (m_element != null) && !m_element.isInMemoryOnly() && isModelGroupPage() && m_element.isModelGroup();
    }

    /**
     * Returns if the current page is used to manage model groups.<p>
     *
     * @return <code>true</code> if the current page is used to manage model groups
     */
    public boolean isModelGroupPage() {

        CmsResource page = getPageResource();
        return (page != null) && CmsContainerpageService.isEditingModelGroups(m_cms, page);

    }

    /**
     * Gets the link wrapper for the given path.
     *
     * @param path the path
     * @return the link wrapper
     */
    public CmsJspLinkWrapper link(String path) {

        return CmsJspObjectValueWrapper.createWrapper(m_cms, path).getToLink();

    }

    /**
     * Gets the resource wrapper for a given path or id.
     *
     * @param str a path or structure id
     * @return the wrapper for the resource with the given path or id
     */
    public CmsJspResourceWrapper readResource(String str) {

        return getVfs().getReadResource().get(str);

    }

    /**
     * Reads an XML content and returns it as a content access bean
     * @param str path or id
     * @return the content access bean for the content with the given path or id
     */
    public CmsJspContentAccessBean readXml(String str) {

        return getVfs().getReadXml().get(str);
    }

    /**
     * Renders the elements of container in a container page wrapper as HTML (without a surrounding element).
     *
     * @param page the page wrapper
     * @param name the name or name prefix of the container
     * @return the rendered HTML
     */
    public String renderContainer(CmsJspContainerPageWrapper page, String name) {

        String result = page.renderContainer(this, name);
        return result;
    }

    /**
     * Sets the container the currently rendered element is part of.<p>
     *
     * @param container the container the currently rendered element is part of
     */
    public void setContainer(CmsContainerBean container) {

        m_container = container;
    }

    /**
     * Sets the detail only page.<p>
     *
     * @param detailOnlyPage the detail only page to set
     */
    public void setDetailOnlyPage(CmsContainerPageBean detailOnlyPage) {

        m_detailOnlyPage = detailOnlyPage;
        clearPageData();
    }

    /**
     * Sets if the page is in drag mode.<p>
     *
     * @param isDragMode if the page is in drag mode
     */
    public void setDragMode(boolean isDragMode) {

        m_isDragMode = isDragMode;
    }

    /**
     * Sets the flag to indicate if in drag and drop mode.<p>
     *
     * @param edited <code>true</code> if in drag and drop mode
     */
    public void setEdited(boolean edited) {

        m_edited = edited;
    }

    /**
     * In edit mode, creates a meta tag that tells the form-based content editor to use the stylesheet with the given path as a default.
     *
     * <p>Does nothing outside of edit mode.
     *
     * @param path the site path of a style sheet
     * @return the meta tag
     */
    public String setEditorCssPath(String path) {

        if (getIsEditMode()) {
            return "\n<meta name=\""
                + CmsGwtConstants.META_EDITOR_STYLESHEET
                + "\" content=\""
                + CmsEncoder.escapeXml(path)
                + "\">\n";
        } else {
            return "";
        }
    }

    /**
     * Sets the currently rendered element.<p>
     *
     * @param element the currently rendered element to set
     */
    public void setElement(CmsContainerElementBean element) {

        m_element = element;
    }

    /**
     * Enables / disables the flag that forces edit mode to be disabled.
     *
     * @param forceDisableEditMode the new value for the flag
     */
    public void setForceDisableEditMode(boolean forceDisableEditMode) {

        m_forceDisableEditMode = forceDisableEditMode;
    }

    /**
     * Sets the currently displayed container page.<p>
     *
     * @param page the currently displayed container page to set
     */
    public void setPage(CmsContainerPageBean page) {

        m_page = page;
        clearPageData();
    }

    /**
     * Converts the given object to a resource wrapper and returns it, or returns null if the conversion fails
     * @param obj the object to convert
     * @return the resource wrapper
     */
    public CmsJspResourceWrapper toResource(Object obj) {

        Object wrapper = wrap(obj);
        try {
            if (obj instanceof A_CmsJspValueWrapper) {
                return ((A_CmsJspValueWrapper)obj).getToResource();
            } else if (obj instanceof CmsJspResourceWrapper) {
                return ((CmsJspResourceWrapper)obj).getToResource();
            } else {
                // in case we add another wrapper with a getToResource method that doesn't extend A_CmsJspValueWrapper
                return (CmsJspResourceWrapper)wrapper.getClass().getMethod("getToResource").invoke(wrapper);
            }
        } catch (Exception e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Updates the internally stored OpenCms user context.<p>
     *
     * @param cms the new OpenCms user context
     */
    public void updateCmsObject(CmsObject cms) {

        try {
            m_cms = OpenCms.initCmsObject(cms);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_cms = cms;
        }
        try {
            m_config = OpenCms.getADEManager().lookupConfigurationWithCache(cms, cms.getRequestContext().getRootUri());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the standard context bean from the request.<p>
     *
     * @param cmsFlexRequest the request from which to update the data
     */
    public void updateRequestData(CmsFlexRequest cmsFlexRequest) {

        CmsResource detailRes = CmsDetailPageResourceHandler.getDetailResource(cmsFlexRequest);
        m_detailContentResource = detailRes;
        m_request = cmsFlexRequest;
    }

    /**
     * Gets the path of either the detail content (if this is a detail request) or the current page if it's not a detail request.
     *
     * @return the URI of the page or detail content
     */
    public String uri() {

        return isDetailRequest() ? getDetailContent().getSitePath() : getRequestContext().getUri();
    }

    /**
     * Returns an EL access wrapper map for the given object.<p>
     *
     * If the object is a {@link CmsResource}, then a {@link CmsJspResourceWrapper} is returned.
     * Otherwise the object is wrapped in a {@link CmsJspObjectValueWrapper}.<p>
     *
     * If the object is already is a wrapper, it is returned unchanged.<p>
     *
     * @param obj the object to wrap
     * @return an EL access wrapper map for the given object
     */
    public Object wrap(Object obj) {

        if ((obj instanceof A_CmsJspValueWrapper) || (obj instanceof CmsJspResourceWrapper)) {
            return obj;
        } else if (obj instanceof CmsResource) {
            return CmsJspResourceWrapper.wrap(m_cms, (CmsResource)obj);
        } else {
            return CmsJspObjectValueWrapper.createWrapper(m_cms, obj);
        }
    }

    /**
     * Accessor for the CmsObject.
     *
     * @return the CmsObject
     */
    protected CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the formatter configuration to the given element.<p>
     *
     * @param element the element
     *
     * @return the formatter configuration
     */
    protected I_CmsFormatterBean getElementFormatter(CmsContainerElementBean element) {

        if (m_elementInstances == null) {
            initPageData();
        }
        I_CmsFormatterBean formatter = null;
        CmsContainerBean container = m_parentContainers.get(element.getInstanceId());
        if (container == null) {
            // use the current container
            container = getContainer();
        }
        if (container != null) {
            String containerName = container.getName();
            Map<String, String> settings = element.getSettings();
            if (settings != null) {
                String formatterConfigId = settings.get(CmsFormatterConfig.getSettingsKeyForContainer(containerName));
                I_CmsFormatterBean dynamicFmt = m_config.findFormatter(formatterConfigId);
                if (dynamicFmt != null) {
                    formatter = dynamicFmt;
                }
            }
            if (formatter == null) {
                try {
                    CmsResource resource = m_cms.readResource(m_cms.getRequestContext().getUri());

                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                        m_cms,
                        resource.getRootPath());
                    CmsFormatterConfiguration formatters = config.getFormatters(m_cms, resource);
                    int width = -2;
                    try {
                        width = Integer.parseInt(container.getWidth());
                    } catch (Exception e) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                    formatter = formatters.getDefaultSchemaFormatter(container.getType(), width);
                } catch (CmsException e1) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e1.getLocalizedMessage(), e1);
                    }
                }
            }
        }
        return formatter;
    }

    /**
     * Returns the title according to the given locale.
     * @param locale the locale for which the title should be read.
     * @return the title according to the given locale
     */
    protected String getLocaleSpecificTitle(Locale locale) {

        String result = null;

        try {

            if (isDetailRequest()) {
                // this is a request to a detail page
                CmsResource res = getDetailContent();
                CmsFile file = m_cms.readFile(res);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
                result = content.getHandler().getTitleMapping(m_cms, content, m_cms.getRequestContext().getLocale());
                if (result == null) {
                    // title not found, maybe no mapping OR not available in the current locale
                    // read the title of the detail resource as fall back (may contain mapping from another locale)
                    result = m_cms.readPropertyObject(
                        res,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false,
                        locale).getValue();
                }
            }
            if (result == null) {
                // read the title of the requested resource as fall back
                result = m_cms.readPropertyObject(
                    m_cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    true,
                    locale).getValue();
            }
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "";
        }

        return result;
    }

    /**
     * Returns the parent element if available.<p>
     *
     * @param element the element
     *
     * @return the parent element or null
     */
    protected CmsContainerElementBean getParentElement(CmsContainerElementBean element) {

        if (m_elementInstances == null) {
            initPageData();
        }
        CmsContainerElementBean parent = null;
        CmsContainerBean cont = m_parentContainers.get(element.getInstanceId());
        if ((cont != null) && cont.isNestedContainer()) {
            parent = m_elementInstances.get(cont.getParentInstanceId());
        }
        return parent;
    }

    /**
     * Accessor for the sitemap configuration.
     *
     * @return the sitemap configuration
     */
    protected CmsADEConfigData getSitemapConfigInternal() {

        return m_config;
    }

    /**
     * Reads a dynamic function bean, given its name in the module configuration.<p>
     *
     * @param configuredName the name of the dynamic function in the module configuration
     * @return the dynamic function bean for the dynamic function configured under that name
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsDynamicFunctionBean readDynamicFunctionBean(String configuredName) throws CmsException {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            m_cms,
            m_cms.addSiteRoot(m_cms.getRequestContext().getUri()));
        CmsFunctionReference functionRef = config.getFunctionReference(configuredName);
        if (functionRef == null) {
            return null;
        }
        CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
        CmsResource functionResource = m_cms.readResource(functionRef.getStructureId());
        CmsDynamicFunctionBean result = parser.parseFunctionBean(m_cms, functionResource);
        return result;
    }

    /**
     * Wraps a list of formatter beans for use in JSPs.
     *
     * @param formatters the formatters to wrap
     * @return the wrapped formatters
     */
    protected List<CmsFormatterInfoWrapper> wrapFormatters(Collection<? extends I_CmsFormatterBean> formatters) {

        List<I_CmsFormatterBean> formattersToSort = new ArrayList<>(formatters);
        List<CmsResourceTypeConfig> types = m_config.getResourceTypes();

        // we want to 'group' the returned formatters by resource type, which is slightly
        // complicated by the fact that formatters can support multiple resource types.

        // first build a map that records the positions of the resource types configured in the sitemap configuration
        Map<String, Integer> typePositionsByTypeName = new HashMap<>();
        for (int i = 0; i < types.size(); i++) {
            CmsResourceTypeConfig singleType = types.get(i);
            typePositionsByTypeName.put(singleType.getTypeName(), Integer.valueOf(i));
        }
        // for each formatter, save the lowest position of any resource type it supports
        Map<String, Integer> lowestResourceTypePositionsByFormatterId = new HashMap<>();
        for (I_CmsFormatterBean formatter : formatters) {
            int pos = Integer.MAX_VALUE;
            for (String typeName : formatter.getResourceTypeNames()) {
                Integer typeOrder = typePositionsByTypeName.get(typeName);
                if (typeOrder != null) {
                    pos = Math.min(pos, typeOrder.intValue());
                }
            }
            lowestResourceTypePositionsByFormatterId.put(formatter.getId(), Integer.valueOf(pos));
        }

        // now we can group the formatters by types using sorting, and inside the groups we sort by formatter rank
        Collections.sort(formattersToSort, new Comparator<I_CmsFormatterBean>() {

            public int compare(I_CmsFormatterBean o1, I_CmsFormatterBean o2) {

                return ComparisonChain.start().compare(getTypePosition(o1), getTypePosition(o2)).compare(
                    o2.getRank(),
                    o1.getRank()).result();
            }

            public int getTypePosition(I_CmsFormatterBean formatter) {

                return lowestResourceTypePositionsByFormatterId.computeIfAbsent(
                    formatter.getId(),
                    id -> Integer.valueOf(Integer.MAX_VALUE));
            }

        });
        return formattersToSort.stream().map(
            formatter -> new CmsFormatterInfoWrapper(m_cms, m_config, formatter)).collect(Collectors.toList());

    }

    /**
     * Adds the mappings of the given formatter configuration.<p>
     *
     * @param formatterBean the formatter bean
     * @param elementId the element content structure id
     * @param resolver The macro resolver used on key and default value of the mappings
     * @param isDetailContent in case of a detail content
     */
    private void addMappingsForFormatter(
        I_CmsFormatterBean formatterBean,
        CmsUUID elementId,
        CmsMacroResolver resolver,
        boolean isDetailContent) {

        if ((formatterBean != null) && (formatterBean.getMetaMappings() != null)) {
            for (CmsMetaMapping map : formatterBean.getMetaMappings()) {
                String key = map.getKey();
                key = resolver.resolveMacros(key);
                // the detail content mapping overrides other mappings
                if (isDetailContent
                    || !m_metaMappings.containsKey(key)
                    || (m_metaMappings.get(key).m_order <= map.getOrder())) {
                    MetaMapping mapping = new MetaMapping();
                    mapping.m_key = key;
                    mapping.m_elementXPath = map.getElement();
                    mapping.m_defaultValue = resolver.resolveMacros(map.getDefaultValue());
                    mapping.m_order = map.getOrder();
                    mapping.m_contentId = elementId;
                    m_metaMappings.put(key, mapping);
                }
            }
        }
    }

    /**
     * Clears the page element data.<p>
     */
    private void clearPageData() {

        m_elementInstances = null;
        m_parentContainers = null;
    }

    /**
     * Finds the first ancestor of a resource matching a given predicate.
     *
     * @param cms the CMS context
     * @param resource the resource
     * @param predicate the predicate to test
     *
     * @return the first ancestor matching the predicate (which may possibly be the given resource itself), or null if no matching ancestor is found
     * @throws CmsException
     */
    private CmsResource findAncestor(CmsObject cms, CmsResource resource, Predicate<CmsResource> predicate) {

        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource ancestor = resource;
            while (ancestor != null) {
                if (predicate.test(ancestor)) {
                    return ancestor;
                }
                String parentFolder = CmsResource.getParentFolder(ancestor.getRootPath());
                if (parentFolder == null) {
                    break;
                }
                try {
                    ancestor = rootCms.readResource(parentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (CmsException e) {
                    LOG.info(e.getLocalizedMessage(), e);
                    break;
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Generates a link to the bundle editor to edit the provided message key.
     * The back link for the editor is the current uri with the provided backLinkAnchor added as anchor.
     *
     * If the bundle resource for the key could not be found, <code>null</code> is returned.
     *
     * @param messageKey the message key to open the bundle editor for.
     * @param backLinkAnchor the anchor id to add to the backlink to the page. If <code>null</code> no anchor is added to the backlink.
     * @param backLinkParams request parameters to add to the backlink without leading '?', e.g. "param1=a&param2=b".
     * @param bundleName the name of the bundle to search the key in. If <code>null</code> the bundle is detected automatically.
     * @param nameFilters if more than one bundle is matched, bundles that match (substring matching) at least one of the provided strings are preferred.
     *  This option is only useful, if the bundleName is not provided.
     *
     * @return a link to the bundle editor for editing the provided key, or <code>null</code> if the bundle for the key could not be found.
     */
    private String getBundleEditorLink(
        String messageKey,
        String backLinkAnchor,
        String backLinkParams,
        String bundleName,
        List<String> nameFilters) {

        if (!m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            String filePath = null;
            if (null == bundleName) {
                filePath = getBundleRootPath(messageKey, nameFilters);
            } else {
                ResourceBundle bundle = CmsResourceBundleLoader.getBundle(
                    bundleName,
                    m_cms.getRequestContext().getLocale());
                if (bundle instanceof CmsVfsResourceBundle) {
                    CmsVfsResourceBundle vfsBundle = (CmsVfsResourceBundle)bundle;
                    filePath = vfsBundle.getParameters().getBasePath();
                }
            }
            try {
                if (null == filePath) {
                    throw new Exception("Could not determine the VFS root path of the bundle.");
                }
                CmsUUID structureId = m_cms.readResource(
                    m_cms.getRequestContext().removeSiteRoot(filePath)).getStructureId();
                String backLink = OpenCms.getLinkManager().getServerLink(m_cms, m_cms.getRequestContext().getUri());
                if (!((null == backLinkParams) || backLinkParams.isEmpty())) {
                    backLink = backLink + "?" + backLinkParams;
                }
                if (!((null == backLinkAnchor) || backLinkAnchor.isEmpty())) {
                    backLink = backLink + "#" + backLinkAnchor;
                }
                String appState = CmsEditor.getEditState(structureId, false, backLink);
                if (null != messageKey) {
                    appState = A_CmsWorkplaceApp.addParamToState(
                        appState,
                        CmsMessageBundleEditor.PARAM_KEYFILTER,
                        messageKey);
                }
                String link = CmsVaadinUtils.getWorkplaceLink(CmsEditorConfiguration.APP_ID, appState);
                return link;
            } catch (Throwable t) {
                if (LOG.isWarnEnabled()) {
                    String message = "Failed to open bundle editor for key '"
                        + messageKey
                        + "' and bundle with name '"
                        + bundleName
                        + "'.";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message, t);
                    } else {
                        LOG.warn(message);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the container page bean for the give resource.<p>
     *
     * @param pageResource the resource
     *
     * @return the container page bean
     *
     * @throws CmsException in case reading the page bean fails
     */
    private CmsContainerPageBean getPage(CmsResource pageResource) throws CmsException {

        CmsContainerPageBean result = null;
        if ((pageResource != null) && CmsResourceTypeXmlContainerPage.isContainerPage(pageResource)) {
            CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(m_cms, pageResource, m_request);
            result = xmlContainerPage.getContainerPage(m_cms);
            CmsModelGroupHelper modelHelper = new CmsModelGroupHelper(
                m_cms,
                OpenCms.getADEManager().lookupConfiguration(m_cms, pageResource.getRootPath()),
                CmsJspTagEditable.isEditableRequest(m_request) && (m_request instanceof HttpServletRequest)
                ? CmsADESessionCache.getCache((HttpServletRequest)m_request, m_cms)
                : null,
                CmsContainerpageService.isEditingModelGroups(m_cms, pageResource));
            result = modelHelper.readModelGroups(xmlContainerPage.getContainerPage(m_cms));
        }
        return result;
    }

    /**
     * Convenience method for getting a request attribute without an explicit cast.<p>
     *
     * @param name the attribute name
     * @return the request attribute
     */
    @SuppressWarnings("unchecked")
    private <A> A getRequestAttribute(String name) {

        Object attribute = m_request.getAttribute(name);

        return attribute != null ? (A)attribute : null;
    }

    /**
     * Initializes the mapping configuration.<p>
     */
    private void initMetaMappings() {

        if (m_metaMappings == null) {
            m_metaMappings = new HashMap<String, MetaMapping>();
            try {
                initPage();
                CmsMacroResolver resolver = new CmsMacroResolver();
                resolver.setKeepEmptyMacros(true);
                resolver.setCmsObject(m_cms);
                resolver.setMessages(OpenCms.getWorkplaceManager().getMessages(getLocale()));
                CmsResourceFilter filter = getIsEditMode()
                ? CmsResourceFilter.IGNORE_EXPIRATION
                : CmsResourceFilter.DEFAULT;
                if (m_page != null) {
                    for (CmsContainerBean container : m_page.getContainers().values()) {
                        for (CmsContainerElementBean element : container.getElements()) {
                            String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(container.getName());
                            String formatterConfigId = element.getSettings() != null
                            ? element.getSettings().get(settingsKey)
                            : null;
                            I_CmsFormatterBean formatterBean = null;
                            formatterBean = m_config.findFormatter(formatterConfigId);
                            if ((formatterBean != null)
                                && formatterBean.useMetaMappingsForNormalElements()
                                && m_cms.existsResource(element.getId(), filter)) {
                                addMappingsForFormatter(formatterBean, element.getId(), resolver, false);
                            }

                        }
                    }
                }
                if (getDetailContentId() != null) {
                    try {
                        CmsResource detailContent = m_cms.readResource(
                            getDetailContentId(),
                            CmsResourceFilter.ignoreExpirationOffline(m_cms));
                        CmsFormatterConfiguration config = OpenCms.getADEManager().lookupConfiguration(
                            m_cms,
                            m_cms.getRequestContext().getRootUri()).getFormatters(m_cms, detailContent);
                        for (I_CmsFormatterBean formatter : config.getDetailFormatters()) {
                            addMappingsForFormatter(formatter, getDetailContentId(), resolver, true);
                        }
                    } catch (CmsException e) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_READING_REQUIRED_RESOURCE_1,
                                getDetailContentId()),
                            e);
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Initializes the page element data.<p>
     */
    private void initPageData() {

        m_elementInstances = new HashMap<String, CmsContainerElementBean>();
        m_parentContainers = new HashMap<String, CmsContainerBean>();
        if (m_page != null) {
            for (CmsContainerBean container : m_page.getContainers().values()) {
                for (CmsContainerElementBean element : container.getElements()) {
                    m_elementInstances.put(element.getInstanceId(), element);
                    m_parentContainers.put(element.getInstanceId(), container);
                    try {
                        if (element.isGroupContainer(m_cms) || element.isInheritedContainer(m_cms)) {
                            List<CmsContainerElementBean> children;
                            if (element.isGroupContainer(m_cms)) {
                                children = CmsJspTagContainer.getGroupContainerElements(
                                    m_cms,
                                    element,
                                    m_request,
                                    container.getType());
                            } else {
                                children = CmsJspTagContainer.getInheritedContainerElements(m_cms, element);
                            }
                            for (CmsContainerElementBean childElement : children) {
                                m_elementInstances.put(childElement.getInstanceId(), childElement);
                                m_parentContainers.put(childElement.getInstanceId(), container);
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            // also add detail only data
            if (m_detailOnlyPage != null) {
                for (CmsContainerBean container : m_detailOnlyPage.getContainers().values()) {
                    for (CmsContainerElementBean element : container.getElements()) {
                        m_elementInstances.put(element.getInstanceId(), element);
                        m_parentContainers.put(element.getInstanceId(), container);
                    }
                }
            }
        }
    }

}