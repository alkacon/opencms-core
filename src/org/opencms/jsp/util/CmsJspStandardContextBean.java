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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.Messages;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionParser;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

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

    /** Cached object for the EL 'function' accessor. */
    private Object m_function;

    /** The currently displayed container page. */
    private CmsContainerPageBean m_page;

    /** The current request. */
    private CmsFlexRequest m_request;

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
        CmsObject cms;
        if (controller != null) {
            cms = controller.getCmsObject();
        } else {
            cms = (CmsObject)req.getAttribute(ATTRIBUTE_CMS_OBJECT);
        }
        if (cms == null) {
            // cms object unavailable - this request was not initialized properly
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_MISSING_CMS_CONTROLLER_1,
                CmsJspBean.class.getName()));
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
        result.m_container = getContainer();
        if (getDetailContent() != null) {
            result.m_detailContentResource = getDetailContent().getCopy();
        }
        result.m_element = getElement();
        result.m_page = getPage();
        return result;
    }

    /**
     * Returns if the current project is the online project.<p>
     * 
     * @return <code>true</code> if the current project is the online project
     */
    public boolean isOnline() {

        return m_cms.getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Returns a caching hash specific to the element, it's properties and the current container width.<p>
     * 
     * @return the caching hash
     */
    public String elementCachingHash() {

        if ((m_element != null) && (m_container != null)) {
            return m_element.editorHash()
                + "w:"
                + m_container.getWidth()
                + "cName:"
                + m_container.getName()
                + "cType:"
                + m_container.getType();
        }
        return "";
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
     * Returns the detail content site path. Returns <code>null</code> if not available.<p>
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
    public CmsContainerElementBean getElement() {

        return m_element;
    }

    /**
     * Returns a map which allows access to dynamic function beans using the JSP EL.<p>
     * 
     * When given a key, the returned map will look up the corresponding dynamic function in the module configuration.<p>
     * 
     * @return  a map which allows access to dynamic function beans
     */
    public Object getFunction() {

        if (m_function != null) {
            return m_function;
        }
        MapMaker mm = new MapMaker();
        m_function = mm.makeComputingMap(new Function<String, Object>() {

            public Object apply(String key) {

                try {
                    CmsDynamicFunctionBean dynamicFunction = readDynamicFunctionBean(key);
                    CmsDynamicFunctionBeanWrapper wrapper = new CmsDynamicFunctionBeanWrapper(m_cms, dynamicFunction);
                    return wrapper;

                } catch (CmsException e) {
                    return new CmsDynamicFunctionBeanWrapper(m_cms, null);
                }
            }
        });
        return m_function;

    }

    /**
     * Returns a lazy map which computes the detail page link as a value when given the name of a (named) dynamic function
     * as a key.<p>
     * 
     * @return a lazy map for computing function detail page links  
     */
    public Map<String, String> getFunctionDetail() {

        MapMaker mm = new MapMaker();
        return mm.makeComputingMap(new Function<String, String>() {

            public String apply(String key) {

                String detailType = CmsDetailPageInfo.FUNCTION_PREFIX + key;
                CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                    m_cms,
                    m_cms.addSiteRoot(m_cms.getRequestContext().getUri()));
                List<CmsDetailPageInfo> detailPages = config.getDetailPagesForType(detailType);
                if ((detailPages == null) || (detailPages.size() == 0)) {
                    return "";
                }
                CmsDetailPageInfo mainDetailPage = detailPages.get(0);
                CmsUUID id = mainDetailPage.getId();
                CmsResource detailRes;
                try {
                    detailRes = m_cms.readResource(id);
                    return OpenCms.getLinkManager().substituteLink(m_cms, detailRes);
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return "";
                }
            }
        });
    }

    /**
     * Returns a lazy map which creates a wrapper object for a dynamic function format when given an XML content
     * as a key.<p>
     * 
     * @return a lazy map for accessing function formats for a content 
     */
    public Map<CmsJspContentAccessBean, CmsDynamicFunctionFormatWrapper> getFunctionFormatFromContent() {

        MapMaker mm = new MapMaker();
        return mm.makeComputingMap(new Function<CmsJspContentAccessBean, CmsDynamicFunctionFormatWrapper>() {

            public CmsDynamicFunctionFormatWrapper apply(CmsJspContentAccessBean contentAccess) {

                CmsXmlContent content = (CmsXmlContent)(contentAccess.getRawContent());
                CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
                CmsDynamicFunctionBean functionBean = null;
                try {
                    functionBean = parser.parseFunctionBean(m_cms, content);
                } catch (CmsException e) {
                    return new CmsDynamicFunctionFormatWrapper(m_cms, null);
                }
                String type = getContainer().getType();
                String width = getContainer().getWidth();
                int widthNum = -1;
                try {
                    widthNum = Integer.parseInt(width);
                } catch (NumberFormatException e) {
                    // NOOP 
                }
                CmsDynamicFunctionBean.Format format = functionBean.getFormatForContainer(m_cms, type, widthNum);
                CmsDynamicFunctionFormatWrapper wrapper = new CmsDynamicFunctionFormatWrapper(m_cms, format);
                return wrapper;
            }
        });

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
     * Returns the currently displayed container page.<p>
     * 
     * @return the currently displayed container page
     */
    public CmsContainerPageBean getPage() {

        return m_page;
    }

    /**
     * JSP EL accessor method for retrieving the preview formatters.<p>
     * 
     * @return a lazy map for accessing preview formatters 
     */
    public Map<String, String> getPreviewFormatter() {

        MapMaker mm = new MapMaker();
        return mm.makeComputingMap(new Function<String, String>() {

            public String apply(String uri) {

                try {
                    String rootPath = m_cms.getRequestContext().addSiteRoot(uri);
                    CmsResource resource = m_cms.readResource(uri);
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
                    String formatterSitePath = m_cms.getRequestContext().removeSiteRoot(formatterResource.getRootPath());
                    return formatterSitePath;
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return "";
                }
            }
        });
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
     * Checks whether a detail page is available for the container element.<p>
     * 
     * @return true if there is a detail page for the container element
     */
    public boolean isDetailPageAvailable() {

        if (m_cms == null) {
            return false;
        }
        CmsContainerElementBean element = getElement();
        if (element == null) {
            return false;
        }
        if (element.isInMemoryOnly()) {
            return false;
        }
        CmsResource res = element.getResource();
        if (res == null) {
            return false;
        }
        try {
            String detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
                m_cms,
                res.getRootPath(),
                m_cms.getRequestContext().getUri());
            return detailPage != null;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            return false;
        }
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
     * Returns the flag to indicate if in drag and drop mode.<p>
     *
     * @return <code>true</code> if in drag and drop mode
     */
    public boolean isEdited() {

        return m_edited;
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
            // should not happen
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

}