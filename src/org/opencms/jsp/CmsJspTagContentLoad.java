/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentLoad.java,v $
 * Date   : $Date: 2004/11/17 12:16:59 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.I_CmsResourceCollector;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
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
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * @since 5.5.0
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

    /** Paramter used for the collector. */
    private String m_param;

    /** The file name to load the current content value from. */
    private String m_resourceName;

    /** The (optional) property to extend the parameter with. */
    private String m_property;
    
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
     * @throws CmsException if something goes wring
     */
    public void doLoadNextFile() throws CmsException {

        // get the next resource from the collector
        CmsResource resource = getNextResource();
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
    public int doStartTag() throws JspException {

        // get the selected collector
        String collectorName = getCollector();        
        if (CmsStringUtil.isEmpty(collectorName)) {
            throw new IllegalArgumentException("'contentload' tag requires 'collector' attribute");
        }

        // initialize OpenCms access objects
        m_controller = (CmsFlexController)pageContext.getRequest().getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        m_cms = m_controller.getCmsObject();

        // store the current locale    
        m_locale = m_cms.getRequestContext().getLocale();

        // construct the parameters from the "property" and the "param" tag
        String param = getProperty();
        String createParam;
        if (CmsStringUtil.isNotEmpty(param)) {
            
            // read the selected property value
            CmsProperty property;
            try {
                property = m_cms.readPropertyObject(m_cms.getRequestContext().getUri(), param, true);
            } catch (CmsException e) {
                OpenCms.getLog(this).error(
                    "Error reading property '" + param + "' on resource " + m_cms.getRequestContext().getUri(), e);
                property = CmsProperty.getNullProperty();
            }
            param = property.getValue("");
                        
            if (CmsStringUtil.isNotEmpty(getParam())) {
                // property and param not empty, concat "property" and "param" tag
                param = param.concat(getParam());
            }
            createParam = param;
        } else {
            // resolve magic parameter name
            param = resolveMagicName(getParam());
            createParam = getParam();
        }
        
        // now collect the resources
        I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);

        try {
            // execute the collector
            m_collectorResult = collector.getResults(m_cms, collectorName, param);
            if ((m_collectorResult == null) || (m_collectorResult.size() == 0)) {
                // the collector returned an empty list, there's no content to iterate
                return SKIP_BODY;
            }
            if (collector.getCreateLink(m_cms, collectorName, param) != null) {
                // use "create link" only if collector supports it
                m_directEditCreateLink = CmsEncoder.encode(collectorName + "|" + createParam);
            }
            doLoadNextFile();
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
     * Returns the collector parameter.<p>
     *
     * @return the collector parameter
     */
    public String getParam() {

        return m_param;
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
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#resolveMagicName(java.lang.String)
     */
    public String resolveMagicName(String name) {

        if (name == null) {
            return name;
        }

        if (!name.startsWith(I_CmsJspTagContentContainer.C_MAGIC_PREFIX)) {
            return name;
        }

        String command = name.substring(I_CmsJspTagContentContainer.C_MAGIC_PREFIX.length());
        int index = I_CmsJspTagContentContainer.C_MAGIC_LIST.indexOf(command);

        switch (index) {
            case 0:
                // "uri"
                return m_cms.getRequestContext().getUri();
            case 1:
                // "filename"
                return m_resourceName;
            default:
                // just return the name, unchanged
                return name;
        }
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
     * Sets the collector parameter.<p>
     *
     * @param param the collector parameter to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Returns the next resource from the collector.<p>
     * 
     * @return the next resource from the collector
     */
    private CmsResource getNextResource() {

        if ((m_collectorResult != null) && (m_collectorResult.size() > 0)) {
            return (CmsResource)m_collectorResult.remove(0);
        }
        return null;
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
     * Sets the property.<p>
     *
     * @param property the property to set
     */
    public void setProperty(String property) {

        m_property = property;
    }
}