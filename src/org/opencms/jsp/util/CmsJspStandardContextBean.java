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
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsLocaleGroupService;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.jsp.CmsJspTagEditable;
import org.opencms.jsp.Messages;
import org.opencms.jsp.jsonpart.CmsJsonPartFilter;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionParser;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;

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

        /** The wrapped element instance. */
        private CmsContainerElementBean m_wrappedElement;

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

            CmsContainerElementBean parent = getParentElement(m_wrappedElement);
            return parent != null ? new CmsContainerElementWrapper(getParentElement(m_wrappedElement)) : null;
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

            String result = "";
            try {
                result = OpenCms.getResourceManager().getResourceType(m_wrappedElement.getResource()).getTypeName();
            } catch (Exception e) {
                CmsJspStandardContextBean.LOG.error(e.getLocalizedMessage(), e);
            }
            return result;
        }

        /**
         * Returns a lazy initialized setting map.<p>
         *
         * @return the settings
         */
        public Map<String, ElementSettingWrapper> getSetting() {

            return CmsCollectionsGenericWrapper.createLazyMap(new SettingsTransformer(m_wrappedElement));
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
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#initSettings(org.opencms.file.CmsObject, org.opencms.xml.containerpage.I_CmsFormatterBean)
         */
        @Override
        public void initSettings(CmsObject cms, I_CmsFormatterBean formatterBean) {

            m_wrappedElement.initSettings(cms, formatterBean);
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
         * @see org.opencms.xml.containerpage.CmsContainerElementBean#removeInstanceId()
         */
        @Override
        public void removeInstanceId() {

            m_wrappedElement.removeInstanceId();
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

            String type = m_prefix + String.valueOf(input);
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                m_cms,
                m_cms.addSiteRoot(m_cms.getRequestContext().getUri()));
            List<CmsDetailPageInfo> detailPages = config.getDetailPagesForType(type);
            if ((detailPages == null) || (detailPages.size() == 0)) {
                return "[No detail page configured for type =" + type + "=]";
            }
            CmsDetailPageInfo mainDetailPage = detailPages.get(0);
            CmsUUID id = mainDetailPage.getId();
            try {
                CmsResource r = m_cms.readResource(id);
                return OpenCms.getLinkManager().substituteLink(m_cms, r);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                return "[Error reading detail page for type =" + type + "=]";
            }
        }
    }

    /**
     * Element setting value wrapper.<p>
     */
    public class ElementSettingWrapper extends A_CmsJspValueWrapper {

        /** Flag indicating the setting has been configured. */
        private boolean m_exists;

        /** The wrapped value. */
        private String m_value;

        /**
         * Constructor.<p>
         *
         * @param value the wrapped value
         * @param exists flag indicating the setting has been configured
         */
        ElementSettingWrapper(String value, boolean exists) {

            m_value = value;
            m_exists = exists;
        }

        /**
         * Returns if the setting has been configured.<p>
         *
         * @return <code>true</code> if the setting has been configured
         */
        @Override
        public boolean getExists() {

            return m_exists;
        }

        /**
         * Returns if the setting value is null or empty.<p>
         *
         * @return <code>true</code> if the setting value is null or empty
         */
        @Override
        public boolean getIsEmpty() {

            return CmsStringUtil.isEmpty(m_value);
        }

        /**
         * Returns if the setting value is null or white space only.<p>
         *
         * @return <code>true</code> if the setting value is null or white space only
         */
        @Override
        public boolean getIsEmptyOrWhitespaceOnly() {

            return CmsStringUtil.isEmptyOrWhitespaceOnly(m_value);
        }

        /**
         * @see org.opencms.jsp.util.A_CmsJspValueWrapper#getIsSet()
         */
        @Override
        public boolean getIsSet() {

            return getExists() && !getIsEmpty();
        }

        /**
         * Returns the value.<p>
         *
         * @return the value
         */
        public String getValue() {

            return m_value;
        }

        /**
         * Returns the string value.<p>
         *
         * @return the string value
         */
        @Override
        public String toString() {

            return m_value != null ? m_value : "";
        }
    }

    /**
     * The element setting transformer.<p>
     */
    public class SettingsTransformer implements Transformer {

        /** The element formatter config. */
        private I_CmsFormatterBean m_formatter;

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
        public Object transform(Object arg0) {

            return new ElementSettingWrapper(
                m_transformElement.getSettings().get(arg0),
                m_formatter != null
                ? m_formatter.getSettings().get(arg0) != null
                : m_transformElement.getSettings().get(arg0) != null);
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

    /** The attribute name of the cms object.*/
    public static final String ATTRIBUTE_CMS_OBJECT = "__cmsObject";

    /** The attribute name of the standard JSP context bean. */
    public static final String ATTRIBUTE_NAME = "cms";

    /** The logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsJspStandardContextBean.class);

    /** OpenCms user context. */
    protected CmsObject m_cms;

    /** Lazily initialized map from a category path to the path's category object. */
    private Map<String, CmsCategory> m_categories;

    /** Lazily initialized map from a category path to all sub-categories of that category */
    private Map<String, CmsJspCategoryAccessBean> m_allSubCategories;

    /** The container the currently rendered element is part of. */
    private CmsContainerBean m_container;

    /** The current detail content resource if available. */
    private CmsResource m_detailContentResource;

    /** The detail only page references containers that are only displayed in detail view. */
    private CmsContainerPageBean m_detailOnlyPage;

    /** Flag to indicate if element was just edited. */
    private boolean m_edited;

    /** The currently rendered element. */
    private CmsContainerElementBean m_element;

    /** The elements of the current page. */
    private Map<String, CmsContainerElementBean> m_elementInstances;

    /** The lazy initialized map which allows access to the dynamic function beans. */
    private Map<String, CmsDynamicFunctionBeanWrapper> m_function;

    /** The lazy initialized map for the function detail pages. */
    private Map<String, String> m_functionDetailPage;

    /** Indicates if in drag mode. */
    private boolean m_isDragMode;

    /** Stores the edit mode info. */
    private Boolean m_isEditMode;

    /** Lazily initialized map from the locale to the localized title property. */
    private Map<String, String> m_localeTitles;

    /** The currently displayed container page. */
    private CmsContainerPageBean m_page;

    /** The current container page resource, lazy initialized. */
    private CmsResource m_pageResource;

    /** The parent containers to the given element instance ids. */
    private Map<String, CmsContainerBean> m_parentContainers;

    /** Lazily initialized map from a category path to all categories on that path. */
    private Map<String, List<CmsCategory>> m_pathCategories;

    /** The current request. */
    private ServletRequest m_request;

    /** Lazily initialized map from the root path of a resource to all categories assigned to the resource. */
    private Map<String, CmsJspCategoryAccessBean> m_resourceCategories;

    /** The resource wrapper for the current page. */
    private CmsJspResourceWrapper m_resourceWrapper;

    /** The lazy initialized map for the detail pages. */
    private Map<String, String> m_typeDetailPage;

    /** The VFS content access bean. */
    private CmsJspVfsAccessBean m_vfsBean;

    /**
     * Creates an empty instance.<p>
     */
    private CmsJspStandardContextBean() {

        // NOOP
    }

    /**
     * Creates a new standard JSP context bean.
     *
     * @param req the current servlet request
     */
    private CmsJspStandardContextBean(ServletRequest req) {

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
        result.setPage(m_page);
        return result;
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
     * Returns the locales available for the currently requested URI.
     *
     * @return the locales available for the currently requested URI.
     */
    public List<Locale> getAvailableLocales() {

        return OpenCms.getLocaleManager().getAvailableLocales(m_cms, getRequestContext().getUri());
    }

    /**
     * Returns the container the currently rendered element is part of.<p>
     *
     * @return the currently the currently rendered element is part of
     */
    public CmsContainerBean getContainer() {

        return m_container;
    }

    /**
     * Returns the current detail content, or <code>null</code> if no detail content is requested.<p>
     *
     * @return the current detail content, or <code>null</code> if no detail content is requested.<p>
     */
    public CmsResource getDetailContent() {

        return m_detailContentResource;
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
     * Returns the detail only page.<p>
     *
     * @return the detail only page
     */
    public CmsContainerPageBean getDetailOnlyPage() {

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
     * Checks if the current request should be direct edit enabled.
     * Online-, history-requests, previews and temporary files will not be editable.<p>
     *
     * @return <code>true</code> if the current request should be direct edit enabled
     */
    public boolean getIsEditMode() {

        if (m_isEditMode == null) {
            m_isEditMode = Boolean.valueOf(CmsJspTagEditable.isEditableRequest(m_request));
        }
        return m_isEditMode.booleanValue();
    }

    /**
     * Returns true if the current request is a JSON request.<p>
     *
     * @return true if we are in a JSON request
     */
    public boolean getIsJSONRequest() {

        return CmsJsonPartFilter.isJsonRequest(m_request);
    }

    /**
     * Returns if the current project is the online project.<p>
     *
     * @return <code>true</code> if the current project is the online project
     */
    public boolean getIsOnlineProject() {

        return m_cms.getRequestContext().getCurrentProject().isOnlineProject();
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

        Map<String, CmsJspResourceWrapper> result = getResourceWrapperForPage().getLocaleResource();
        List<Locale> locales = CmsLocaleGroupService.getPossibleLocales(m_cms, getContainerPage());
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

        return getResourceWrapperForPage().getMainLocale();
    }

    /**
     * Returns the currently displayed container page.<p>
     *
     * @return the currently displayed container page
     */
    public CmsContainerPageBean getPage() {

        return m_page;
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
     * Reads the categories assigned to the currently requested URI.
     * @return the categories assigned to the currently requested URI.
     */
    public CmsJspCategoryAccessBean getReadCategories() {

        return m_resourceCategories.get(getRequestContext().getUri());
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
                        return CmsCategoryService.getInstance().readCategory(
                            m_cms,
                            (String)categoryPath,
                            getRequestContext().getUri());
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
                    return result;
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
     * Returns the request context.<p>
     *
     * @return the request context
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
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
                String detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
                    m_cms,
                    m_element.getResource().getRootPath(),
                    m_cms.getRequestContext().getUri(),
                    null);
                result = detailPage != null;
            } catch (CmsException e) {
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

        CmsResource page = getContainerPage();
        return (page != null) && CmsContainerpageService.isEditingModelGroups(m_cms, page);

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
     * Sets the currently rendered element.<p>
     *
     * @param element the currently rendered element to set
     */
    public void setElement(CmsContainerElementBean element) {

        m_element = element;
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
        String containerName = container.getName();
        Map<String, String> settings = element.getSettings();
        if (settings != null) {
            String formatterConfigId = settings.get(CmsFormatterConfig.getSettingsKeyForContainer(containerName));
            if (CmsUUID.isValidUUID(formatterConfigId)) {
                formatter = OpenCms.getADEManager().getCachedFormatters(false).getFormatters().get(
                    new CmsUUID(formatterConfigId));
            }
        }
        if (formatter == null) {
            try {
                CmsResource resource = m_cms.readResource(m_cms.getRequestContext().getUri());

                CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(m_cms, resource.getRootPath());
                CmsFormatterConfiguration formatters = config.getFormatters(m_cms, resource);
                int width = -2;
                try {
                    width = Integer.parseInt(container.getWidth());
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
                formatter = formatters.getDefaultSchemaFormatter(container.getType(), width);
            } catch (CmsException e1) {
                LOG.error(e1.getLocalizedMessage(), e1);
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
     * Clears the page element data.<p>
     */
    private void clearPageData() {

        m_elementInstances = null;
        m_parentContainers = null;
    }

    /**
     * Returns the current container page resource.<p>
     *
     * @return the current container page resource
     */
    private CmsResource getContainerPage() {

        try {
            if (m_pageResource == null) {
                // get the container page itself, checking the history first
                m_pageResource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(m_request);
                if (m_pageResource == null) {
                    m_pageResource = m_cms.readResource(m_cms.getRequestContext().getUri());
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return m_pageResource;
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
     * Gets the resource wrapper for the current page, initializing it if necessary.<p>
     *
     * @return the resource wrapper for the current page
     */
    private CmsJspResourceWrapper getResourceWrapperForPage() {

        if (m_resourceWrapper != null) {
            return m_resourceWrapper;
        }
        m_resourceWrapper = new CmsJspResourceWrapper(m_cms, getContainerPage());
        return m_resourceWrapper;
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