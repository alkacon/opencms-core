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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspResourceLoadBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Implementation of the <code>&lt;cms:resourceload/&gt;</code> tag, 
 * used to access and display resource information from the VFS.<p>
 * 
 * @since 8.0 
 */
public class CmsJspTagResourceLoad extends CmsJspScopedVarBodyTagSuport implements I_CmsResourceContainer {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3753361821868919139L;

    /** The CmsObject for the current user. */
    protected transient CmsObject m_cms;

    /** The name of the collector to use for list building. */
    protected String m_collector;

    /** The name of the resource collector used. */
    protected String m_collectorName;

    /** The parameters of the resource collector uses. */
    protected String m_collectorParam;

    /** The list of collected resource items. */
    protected List<CmsResource> m_collectorResult;

    /** The bean to store information required to make the result list browsable. */
    protected CmsContentInfoBean m_contentInfoBean;

    /** The FlexController for the current request. */
    protected CmsFlexController m_controller;

    /** The index of the current page that gets displayed. */
    protected String m_pageIndex;

    /** The number of page links in the Google-like page navigation. */
    protected String m_pageNavLength;

    /** The size of a page to be displayed. */
    protected String m_pageSize;

    /** Parameter used for the collector. */
    protected String m_param;

    /** Indicates if the collector results should be preloaded. */
    protected boolean m_preload;

    /** The (optional) property to extend the parameter with. */
    protected String m_property;

    /** Reference to the last loaded resource element. */
    protected transient CmsResource m_resource;

    /** The file name to load the current content value from. */
    protected String m_resourceName;

    /**
     * Empty constructor, required for JSP tags.<p> 
     */
    public CmsJspTagResourceLoad() {

        super();
    }

    /**
     * Constructor used when using <code>resourceload</code> from scriptlet code.<p> 
     * 
     * @param container the parent resource container (could be a preloader)
     * @param context the JSP page context
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * 
     * @throws JspException in case something goes wrong
     */
    public CmsJspTagResourceLoad(
        I_CmsResourceContainer container,
        PageContext context,
        String collectorName,
        String collectorParam)
    throws JspException {

        this(container, context, collectorName, collectorParam, null, null);
    }

    /**
     * Constructor used when using <code>resourceload</code> from scriptlet code.<p> 
     * 
     * @param container the parent resource container (could be a preloader)
     * @param context the JSP page context
     * @param collectorName the collector name to use
     * @param collectorParam the collector param to use
     * @param pageIndex the display page index (may contain macros)
     * @param pageSize the display page size (may contain macros)
     * 
     * @throws JspException in case something goes wrong
     */
    public CmsJspTagResourceLoad(
        I_CmsResourceContainer container,
        PageContext context,
        String collectorName,
        String collectorParam,
        String pageIndex,
        String pageSize)
    throws JspException {

        setCollector(collectorName);
        setParam(collectorParam);
        setPageIndex(pageIndex);
        setPageSize(pageSize);
        m_preload = false;

        setPageContext(context);
        init(container);
    }

    /**
     * Returns the resource name currently processed.<p> 
     * 
     * @param cms the current OpenCms user context
     * @param contentContainer the current resource container
     * 
     * @return the resource name currently processed
     */
    protected static String getResourceName(CmsObject cms, I_CmsResourceContainer contentContainer) {

        if ((contentContainer != null) && (contentContainer.getResourceName() != null)) {
            return contentContainer.getResourceName();
        } else if (cms != null) {
            return cms.getRequestContext().getUri();
        } else {
            return null;
        }
    }

    /**
     * Limits the collector's result list to the size of a page to be displayed in a JSP.<p>
     * 
     * @param contentInfoBean the info bean of the collector
     * @param collectorResult the result list of the collector
     * 
     * @return a limited collector's result list
     */
    protected static List<CmsResource> limitCollectorResult(
        CmsContentInfoBean contentInfoBean,
        List<CmsResource> collectorResult) {

        List<CmsResource> result = null;
        int pageCount = -1;

        if (contentInfoBean.getPageSize() > 0) {

            pageCount = collectorResult.size() / contentInfoBean.getPageSize();
            if ((collectorResult.size() % contentInfoBean.getPageSize()) != 0) {
                pageCount++;
            }

            contentInfoBean.setPageCount(pageCount);

            int startIndex = (contentInfoBean.getPageIndex() - 1) * contentInfoBean.getPageSize();
            int endIndex = contentInfoBean.getPageIndex() * contentInfoBean.getPageSize();
            if (endIndex > collectorResult.size()) {
                endIndex = collectorResult.size();
            }

            result = collectorResult.subList(startIndex, endIndex);
        } else {

            result = collectorResult;
            if (collectorResult.size() > 0) {
                contentInfoBean.setPageCount(1);
            }
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    @Override
    public int doAfterBody() throws JspException {

        // close open direct edit first
        if (hasMoreResources()) {
            // another loop is required
            return EVAL_BODY_AGAIN;
        }
        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        // no more files are available, so skip the body and finish the loop
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {

        release();
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException, CmsIllegalArgumentException {

        // get a reference to the parent "content container" class (if available)
        Tag ancestor = findAncestorWithClass(this, I_CmsResourceContainer.class);
        I_CmsResourceContainer container = null;
        if (ancestor != null) {
            // parent content container available, use preloaded values from this container
            container = (I_CmsResourceContainer)ancestor;
            // check if container really is a preloader
            if (!container.isPreloader()) {
                // don't use ancestor if not a preloader
                container = null;
            }
        }

        // initialize the content load tag
        init(container);

        hasMoreResources();

        return isScopeVarSet() ? SKIP_BODY : EVAL_BODY_INCLUDE;
    }

    /**
     * Returns the collector.<p>
     *
     * @return the collector
     */
    public String getCollector() {

        return m_collector;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#getCollectorName()
     */
    public String getCollectorName() {

        return m_collectorName;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#getCollectorParam()
     */
    public String getCollectorParam() {

        return m_collectorParam;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#getCollectorResult()
     */
    public List<CmsResource> getCollectorResult() {

        return m_collectorResult;
    }

    /**
     * Returns the index of the page to be displayed.<p>
     * 
     * @return the index of the page to be displayed
     */
    public String getPageIndex() {

        return m_pageIndex;
    }

    /**
     * Returns the number of page links in the Google-like page navigation.<p>
     * 
     * @return the number of page links in the Google-like page navigation
     */
    public String getPageNavLength() {

        return m_pageNavLength;
    }

    /**
     * Returns the size of a single page to be displayed.<p>
     * 
     * @return the size of a single page to be displayed
     */
    public String getPageSize() {

        return m_pageSize;
    }

    /**
     * Returns the collector parameter.<p>
     *
     * @return the collector parameter
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns <code>"true"</code> if this content load tag should only preload the values from the collector.<p>
     * 
     * @return <code>"true"</code> if this content load tag should only preload the values from the collector
     */
    public String getPreload() {

        return String.valueOf(isPreloader());
    }

    /**
     * Returns the property.<p>
     *
     * @return the property
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#getResource()
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#getResourceName()
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#hasMoreContent()
     */
    @Deprecated
    public boolean hasMoreContent() throws JspException {

        return hasMoreResources();
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#hasMoreResources()
     */
    @SuppressWarnings("unused")
    public boolean hasMoreResources() throws JspException {

        if (isPreloader()) {
            // if in preload mode, no result is required            
            return false;
        }

        // check if there are more files to iterate
        boolean hasMoreResources = m_collectorResult.size() > 0;
        if (hasMoreResources) {
            // there are more results available...
            doLoadNextResource();
        }

        return hasMoreResources;
    }

    /**
     * @see org.opencms.jsp.I_CmsResourceContainer#isPreloader()
     */
    public boolean isPreloader() {

        return isScopeVarSet() ? true : m_preload;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_cms = null;
        m_collector = null;
        m_collectorName = null;
        m_collectorParam = null;
        m_collectorResult = null;
        m_resource = null;
        m_contentInfoBean = null;
        m_controller = null;
        m_pageIndex = null;
        m_pageNavLength = null;
        m_pageSize = null;
        m_param = null;
        m_preload = false;
        m_property = null;
        m_resourceName = null;
        super.release();
    }

    /**
     * Sets the collector name.<p>
     *
     * @param collector the collector name to set
     */
    public void setCollector(String collector) {

        m_collector = collector;
    }

    /**
     * Sets the index of the page to be displayed.<p>
     * 
     * @param pageIndex the index of the page to be displayed
     */
    public void setPageIndex(String pageIndex) {

        m_pageIndex = pageIndex;
    }

    /**
     * Sets the number of page links in the Google-like page navigation.<p>
     * 
     * @param pageNavLength the number of page links in the Google-like page navigation
     */
    public void setPageNavLength(String pageNavLength) {

        m_pageNavLength = pageNavLength;
    }

    /**
     * Sets the size of a single page to be displayed.<p>
     * 
     * @param pageSize the size of a single page to be displayed
     */
    public void setPageSize(String pageSize) {

        m_pageSize = pageSize;
    }

    /**
     * Sets the collector parameter.<p>
     *
     * @param param the collector parameter to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Sets the preload flag for this resource load tag.<p> 
     * 
     * If this is set to <code>true</code>, then the collector result will only 
     * be preloaded, but not iterated.<p> 
     * 
     * @param preload the preload flag to set
     */
    public void setPreload(String preload) {

        m_preload = Boolean.valueOf(preload).booleanValue();
    }

    /**
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        m_property = property;
    }

    /**
     * Load the next resource from the initialized list of resources.<p>
     */
    protected void doLoadNextResource() {

        // get the next resource from the collector
        CmsResource resource = getNextResource();
        if (resource == null) {
            m_resourceName = null;
            m_resource = null;
            return;
        }

        // set the resource name
        m_resourceName = m_cms.getSitePath(resource);

        // set the resource
        m_resource = resource;
    }

    /**
     * Returns the content info bean.<p>
     * 
     * @return the content info bean
     */
    protected CmsContentInfoBean getContentInfoBean() {

        return m_contentInfoBean;
    }

    /**
     * Returns the next resource from the collector.<p>
     * 
     * @return the next resource from the collector
     */
    protected CmsResource getNextResource() {

        if ((m_collectorResult != null) && (m_collectorResult.size() > 0)) {

            m_contentInfoBean.incResultIndex();
            return m_collectorResult.remove(0);
        }

        return null;
    }

    /**
     * Initializes this content load tag.<p> 
     * 
     * @param container the parent container (could be a preloader)
     * 
     * @throws JspException in case something goes wrong
     */
    protected void init(I_CmsResourceContainer container) throws JspException {

        // check if the tag contains a pageSize, pageIndex and pageNavLength attribute, or none of them
        int pageAttribCount = 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageSize) ? 1 : 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageIndex) ? 1 : 0;

        if ((pageAttribCount > 0) && (pageAttribCount < 2)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_TAG_RESOURCELOAD_INDEX_SIZE_0));
        }

        I_CmsResourceContainer usedContainer;
        if (container == null) {
            // no preloading ancestor has been found
            usedContainer = this;
            if (CmsStringUtil.isEmpty(m_collector)) {
                // check if the tag contains a collector attribute
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_TAG_RESOURCELOAD_MISSING_COLLECTOR_0));
            }
            if (CmsStringUtil.isEmpty(m_param)) {
                // check if the tag contains a param attribute
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_TAG_RESOURCELOAD_MISSING_PARAM_0));
            }
        } else {
            // use provided container (preloading ancestor)
            usedContainer = container;
        }

        // initialize OpenCms access objects
        m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();

        // get the resource name from the selected container
        String resourcename = getResourceName(m_cms, usedContainer);

        // initialize a string mapper to resolve EL like strings in tag attributes
        CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(m_cms).setJspPageContext(pageContext).setResourceName(
            resourcename).setKeepEmptyMacros(true);

        // resolve the collector name
        if (container == null) {
            // no preload parent container, initialize new values
            m_collectorName = resolver.resolveMacros(getCollector());
            // resolve the parameter
            m_collectorParam = resolver.resolveMacros(getParam());
            m_collectorResult = null;
        } else {
            // preload parent content container available, use values from this container
            m_collectorName = usedContainer.getCollectorName();
            m_collectorParam = usedContainer.getCollectorParam();
            m_collectorResult = usedContainer.getCollectorResult();
        }

        try {
            // now collect the resources
            I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(m_collectorName);
            if (collector == null) {
                throw new CmsException(Messages.get().container(Messages.ERR_COLLECTOR_NOT_FOUND_1, m_collectorName));
            }
            // execute the collector if not already done in parent tag
            if (m_collectorResult == null) {
                m_collectorResult = collector.getResults(m_cms, m_collectorName, m_collectorParam);
            }

            m_contentInfoBean = new CmsContentInfoBean();
            m_contentInfoBean.setPageSizeAsString(resolver.resolveMacros(m_pageSize));
            m_contentInfoBean.setPageIndexAsString(resolver.resolveMacros(m_pageIndex));
            m_contentInfoBean.setPageNavLengthAsString(resolver.resolveMacros(m_pageNavLength));
            m_contentInfoBean.setResultSize(m_collectorResult.size());
            m_contentInfoBean.initResultIndex();

            if (!isPreloader()) {
                // not required when only preloading 
                m_collectorResult = CmsJspTagResourceLoad.limitCollectorResult(m_contentInfoBean, m_collectorResult);
                m_contentInfoBean.initPageNavIndexes();
            } else if (isScopeVarSet()) {
                // scope variable is set, store resource load bean in JSP context
                CmsJspResourceLoadBean bean = new CmsJspResourceLoadBean(m_cms, m_collectorResult);
                storeAttribute(bean);
            }

        } catch (CmsException e) {
            m_controller.setThrowable(e, m_cms.getRequestContext().getUri());
            throw new JspException(e);
        }
    }
}