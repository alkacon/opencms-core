/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentLoad.java,v $
 * Date   : $Date: 2004/10/18 13:57:54 $
 * Version: $Revision: 1.2 $
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
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentFilter;

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public class CmsJspTagContentLoad extends BodyTagSupport implements I_CmsJspTagContentContainer {

    /** The link for creation of a new element, specified by the selected filter. */
    private String m_directEditCreateLink;
    
    /** The CmsObject for the current user. */
    private CmsObject m_cms;

    /** Reference to the last loaded content element. */
    private A_CmsXmlDocument m_content;

    /** The FlexController for the current request. */
    private CmsFlexController m_controller;

    /** The editable flag. */
    private boolean m_editable;
    
    /** Indicates if the last element was ediable (including user permissions etc.). */
    private String m_directEditPermissions;    

    /** The file name to load the current content value from. */
    private String m_file;

    /** The name of the filter to use for list building. */
    private String m_filter;

    /** The list of filtered content items. */
    private List m_filterContentList;

    /** Refenence to the currently selected locale. */
    private Locale m_locale;

    /** Paramter used for filters. */
    private String m_param;

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    public int doAfterBody() throws JspException {

        if (m_directEditPermissions != null) {
            // last element was direct editable, close it
            CmsJspTagEditable.includeDirectEditElement(
                pageContext,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_END, 
                m_file, 
                null, 
                null, 
                m_directEditPermissions,
                null);            
            m_directEditPermissions = null;
        }
        
        // check if there are more files to iterate
        if (m_filterContentList.size() > 0) {
            
            // there are more files available...
            try {
                doLoadNextFile();
            } catch (CmsException e) {
                m_controller.setThrowable(e, m_file);
                throw new JspException(e);
            }
            
            // check "direct edit" support
            if (m_editable) {
                m_directEditPermissions = CmsJspTagEditable.includeDirectEditElement(
                    pageContext, 
                    I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_START, 
                    m_file, 
                    null, 
                    CmsJspTagEditable.createEditOptions(true, true, false),  
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

        // ge the next file name from the current list
        m_file = getNextFilname();

        // try to read and initialize the XML content
        CmsFile file = m_cms.readFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(m_cms, file);        
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        if (getFilter() == null) {
            throw new IllegalArgumentException("'contentload' tag requires 'filter' attribute");
        }

        // initialize OpenCms access objects
        m_controller = (CmsFlexController)pageContext.getRequest().getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        m_cms = m_controller.getCmsObject();

        // store the current locale    
        m_locale = m_cms.getRequestContext().getLocale();

        // get the selected filter class
        String filterName = getFilter();
        String param = resolveMagicName(getParam());
        I_CmsXmlContentFilter filter = OpenCms.getXmlContentTypeManager().getContentFilter(filterName);

        try {
            // execute the filter
            m_filterContentList = filter.getFilterResults(m_cms, filterName, param);
            if (filter.getCreateLink(m_cms, filterName, param) != null) {
                // use "create link" only if filter supports it
                m_directEditCreateLink = CmsEncoder.encode(getFilter() + "|" + getParam());
            }
            doLoadNextFile();
        } catch (CmsException e) {
            m_controller.setThrowable(e, m_cms.getRequestContext().getUri());
            throw new JspException(e);
        }
        
        // check options for first element
        String directEditOptions;
        if (m_directEditCreateLink != null) {
            // if create link is not null, show "edit", "delete" and "new" button for first element
            directEditOptions = CmsJspTagEditable.createEditOptions(true, true, true);
        } else {
            // if create link is null, show onle "edit" button for first element
            directEditOptions = CmsJspTagEditable.createEditOptions(true, false, false);
        }
        
        // check "direct edit" support
        if (m_editable) {
            m_directEditPermissions = CmsJspTagEditable.includeDirectEditElement(
                pageContext, 
                I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_START, 
                m_file, 
                null, 
                directEditOptions,
                null,
                m_directEditCreateLink);
        }        

        return EVAL_BODY_INCLUDE;
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
     * Returns the file.<p>
     *
     * @return the file
     */
    public String getFile() {

        return m_file;
    }

    /**
     * Returns the filter.<p>
     *
     * @return the filter
     */
    public String getFilter() {

        return m_filter;
    }

    /**
     * Returns the filter parameter.<p>
     *
     * @return the filter paramete
     */
    public String getParam() {

        return m_param;
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getResourceName()
     */
    public String getResourceName() {

        return m_file;
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
     * @see org.opencms.jsp.I_CmsJspTagContentContainer#getXmlDocumentIndex()
     */
    public int getXmlDocumentIndex() {

        // index must be set in "loop" or "show" class
        return 0;
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
        m_file = null;
        m_filter = null;
        m_filterContentList = null;
        m_param = null;
        m_cms = null;
        m_controller = null;
        m_editable = false;    
        m_directEditPermissions = null;
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
                return m_file;
            default:
                // just return the name, unchanged
                return name;
        }
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
     * Sets the file.<p>
     *
     * @param file the file to set
     */
    public void setFile(String file) {

        m_file = file;
    }

    /**
     * Sets the filter.<p>
     *
     * @param filter the filter to set
     */
    public void setFilter(String filter) {

        m_filter = filter;
    }

    /**
     * Sets the filter parameter.<p>
     *
     * @param param the filter parameter to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Returns the next file name from the filter.<p>
     * 
     * @return the next file name from the filter
     */
    private String getNextFilname() {

        if ((m_filterContentList != null) && (m_filterContentList.size() > 0)) {
            return (String)m_filterContentList.remove(0);
        }
        return null;
    }
}