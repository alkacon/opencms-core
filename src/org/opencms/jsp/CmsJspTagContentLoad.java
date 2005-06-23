/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentLoad.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.22 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagContentLoad extends BodyTagSupport implements I_CmsJspTagContentContainer {

    /** The CmsObject for the current user. */
    private CmsObject m_cms;

    /** The name of the collector to use for list building. */
    private String m_collector;

    /** The list of collected content items. */
    private List m_collectorResult;

    /** Reference to the last loaded content element. */
    private A_CmsXmlDocument m_content;

    /** The bean to store information required to make the result list browsable. */
    private CmsContentInfoBean m_contentInfoBean;

    /** The FlexController for the current request. */
    private CmsFlexController m_controller;

    /** The link for creation of a new element, specified by the selected collector. */
    private String m_directEditCreateLink;

    /** The "direct edit" options to use for the 2nd to the last element. */
    private String m_directEditFollowOptions;

    /** Indicates if the last element was ediable (including user permissions etc.). */
    private String m_directEditPermissions;

    /** The editable flag. */
    private boolean m_editable;

    /** Refenence to the currently selected locale. */
    private Locale m_locale;

    /** The index of the current page that gets displayed. */
    private String m_pageIndex;

    /** The number of page links in the Google-like page navigation. */
    private String m_pageNavLength;

    /** The size of a page to be displayed. */
    private String m_pageSize;

    /** Paramter used for the collector. */
    private String m_param;

    /** The (optional) property to extend the parameter with. */
    private String m_property;

    /** The file name to load the current content value from. */
    private String m_resourceName;

    /**
     * Returns the resource name currently processed.<p> 
     * 
     * @param cms the current OpenCms user context
     * @param contentContainer the current content container
     * 
     * @return the resource name currently processed
     */
    protected static String getResourceName(CmsObject cms, I_CmsJspTagContentContainer contentContainer) {

        if (contentContainer != null && contentContainer.getResourceName() != null) {
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
     */
    private static List limitCollectorResult(CmsContentInfoBean contentInfoBean, List collectorResult) {

        List result = null;
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
    public int doAfterBody() throws JspException {

        if (m_directEditPermissions != null) {
            // last element was direct editable, close it
            CmsJspTagEditable.includeDirectEditElement(
                pageContext,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_END,
                m_resourceName,
                null,
                null,
                m_directEditPermissions,
                null);
            m_directEditPermissions = null;
        }

        // check if there are more files to iterate
        if (m_collectorResult.size() > 0) {

            // there are more files available...
            try {
                doLoadNextFile();
            } catch (CmsException e) {
                m_controller.setThrowable(e, m_resourceName);
                throw new JspException(e);
            }

            // check "direct edit" support
            if (m_editable) {

                m_directEditPermissions = CmsJspTagEditable.includeDirectEditElement(
                    pageContext,
                    I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_START,
                    m_resourceName,
                    null,
                    m_directEditFollowOptions,
                    null,
                    m_directEditCreateLink);
            }

            // another loop is required
            return EVAL_BODY_AGAIN;
        }

        // no more files are available, so skip the body and finish the loop
        return SKIP_BODY;
    }

    /**
     * Load the next file name fomr the initialized list of file names.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void doLoadNextFile() throws CmsException {

        // get the next resource from the collector
        CmsResource resource = getNextResource();
        if (resource == null) {
            m_resourceName = null;
            m_content = null;
            return;
        }

        // set the resource name
        m_resourceName = m_cms.getSitePath(resource);

        // upgrade the resource to a file
        // the static method CmsFile.upgrade(...) is not used for performance reasons
        CmsFile file = null;
        if (resource instanceof CmsFile) {
            // check the resource contents
            file = (CmsFile)resource;
            if ((file.getContents() == null) || (file.getContents().length <= 0)) {
                // file has no contents available, force re-read
                file = null;
            }
        }
        if (file == null) {
            // use ALL filter since the list itself should have filtered out all unwanted resources already 
            file = m_cms.readFile(m_resourceName, CmsResourceFilter.ALL);
        }

        // unmarshal the XML content from the resource        
        m_content = CmsXmlContentFactory.unmarshal(m_cms, file);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException, CmsIllegalArgumentException {

        // check if the tag contains a pageSize, pageIndex and pageNavLength attribute, or none of them
        int pageAttribCount = 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageSize) ? 1 : 0;
        pageAttribCount += CmsStringUtil.isNotEmpty(m_pageIndex) ? 1 : 0;
        //pageAttribCount += CmsStringUtil.isNotEmpty(m_pageNavLength) ? 1 : 0;

        if (pageAttribCount > 0 && pageAttribCount < 2) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_TAG_CONTENTLOAD_INDEX_SIZE_0));
        }

        // check if the tag contains a collector attribute
        if (CmsStringUtil.isEmpty(m_collector)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_TAG_CONTENTLOAD_MISSING_COLLECTOR_0));
        }

        // check if the tag contains a param attribute
        if (CmsStringUtil.isEmpty(m_param)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_TAG_CONTENTLOAD_MISSING_PARAM_0));
        }

        // initialize OpenCms access objects
        m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();

        // initialize a string mapper to resolve EL like strings in tag attributes
        String resourcename = getResourceName(m_cms, this);
        CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(m_cms).setJspPageContext(pageContext).setResourceName(
            resourcename).setKeepEmptyMacros(true);

        // resolve the collector name
        String collectorName = resolver.resolveMacros(getCollector());

        // store the current locale    
        m_locale = m_cms.getRequestContext().getLocale();

        // resolve the parameter
        String param = resolver.resolveMacros(getParam());

        // now collect the resources
        I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);

        try {
            if (collector == null) {
                throw new CmsException(Messages.get().container(Messages.ERR_COLLECTOR_NOT_FOUND_1, collectorName));
            }

            // execute the collector
            m_collectorResult = collector.getResults(m_cms, collectorName, param);

            m_contentInfoBean = new CmsContentInfoBean();
            m_contentInfoBean.setPageSizeAsString(resolver.resolveMacros(m_pageSize));
            m_contentInfoBean.setPageIndexAsString(resolver.resolveMacros(m_pageIndex));
            m_contentInfoBean.setPageNavLengthAsString(resolver.resolveMacros(m_pageNavLength));
            m_contentInfoBean.setResultSize(m_collectorResult.size());
            m_contentInfoBean.initResultIndex();

            m_collectorResult = CmsJspTagContentLoad.limitCollectorResult(m_contentInfoBean, m_collectorResult);
            m_contentInfoBean.initPageNavIndexes();

            String createParam = collector.getCreateParam(m_cms, collectorName, param);
            if (createParam != null) {
                // use "create link" only if collector supports it
                m_directEditCreateLink = CmsEncoder.encode(collectorName + "|" + createParam);
            }

            if (m_collectorResult != null && m_collectorResult.size() > 0) {
                doLoadNextFile();
            }
        } catch (CmsException e) {
            m_controller.setThrowable(e, m_cms.getRequestContext().getUri());
            throw new JspException(e);
        }

        // check "direct edit" support
        if (m_editable) {

            // check options for first element
            String directEditOptions;
            if (m_directEditCreateLink != null) {
                // if create link is not null, show "edit", "delete" and "new" button for first element
                directEditOptions = CmsJspTagEditable.createEditOptions(true, true, true);
                // show "edit" and "delete" button for 2nd to last element
                m_directEditFollowOptions = CmsJspTagEditable.createEditOptions(true, true, false);
            } else {
                // if create link is null, show only "edit" button for first element
                directEditOptions = CmsJspTagEditable.createEditOptions(true, false, false);
                // also show only the "edit" button for 2nd to last element
                m_directEditFollowOptions = directEditOptions;
            }

            m_directEditPermissions = CmsJspTagEditable.includeDirectEditElement(
                pageContext,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_START,
                m_resourceName,
                null,
                directEditOptions,
                null,
                m_directEditCreateLink);
        }

        return EVAL_BODY_INCLUDE;
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
     * Returns the editable flag.<p>
     * 
     * @return the editable flag
     */
    public String getEditable() {

        return String.valueOf(m_editable);
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
     * Returns the property.<p>
     *
     * @return the property
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getResourceName()
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getXmlDocument()
     */
    public A_CmsXmlDocument getXmlDocument() {

        return m_content;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getXmlDocumentElement()
     */
    public String getXmlDocumentElement() {

        // value must be set in "loop" or "show" class
        return null;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getXmlDocumentLocale()
     */
    public Locale getXmlDocumentLocale() {

        return m_locale;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_resourceName = null;
        m_collector = null;
        m_collectorResult = null;
        m_param = null;
        m_cms = null;
        m_controller = null;
        m_editable = false;
        m_directEditPermissions = null;
        m_directEditCreateLink = null;
        m_directEditFollowOptions = null;
        m_property = null;
    }

    /**
     * Sets the collector.<p>
     *
     * @param collector the collector to set
     */
    public void setCollector(String collector) {

        m_collector = collector;
    }

    /**
     * Sets the editable flag.<p>
     * 
     * @param editable the flag to set
     */
    public void setEditable(String editable) {

        m_editable = Boolean.valueOf(editable).booleanValue();
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
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        m_property = property;
    }

    /**
     * Returns the content info bean.<p>
     * 
     * @return the content info bean
     */
    CmsContentInfoBean getContentInfoBean() {

        return m_contentInfoBean;
    }

    /**
     * Returns the next resource from the collector.<p>
     * 
     * @return the next resource from the collector
     */
    private CmsResource getNextResource() {

        if ((m_collectorResult != null) && (m_collectorResult.size() > 0)) {

            m_contentInfoBean.incResultIndex();
            return (CmsResource)m_collectorResult.remove(0);
        }

        return null;
    }

}