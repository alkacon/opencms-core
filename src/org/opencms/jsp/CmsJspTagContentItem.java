/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspTagContentItem.java,v $
 * Date   : $Date: 2004/08/03 07:19:04 $
 * Version: $Revision: 1.1 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editor.I_CmsEditorActionHandler;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlDefaultContentFilter;
import org.opencms.xml.content.I_CmsXmlContentFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.0
 */
public class CmsJspTagContentItem extends BodyTagSupport { 
    
    /** Reqnest attribute where the XML content base filename is stored. */
    public static final String ATTRIBUTE_FILENAME = "__xmlContentFilename";
    
    /** Request attribute where the XML content object is stored. */
    public static final String ATTRIBUTE_XMLCONTENT = "__xmlContentObject";
    
    /** The link for creation of a new element, specified by the selected filter. */
    private String m_createLink;
    
    /** The editable flag. */
    private boolean m_editable;
    
    /** The file to load the content value from. */
    private String m_file;
    
    /** The name of the filter to use for list building. */
    private String m_filter;
    
    /** The list of filtered content items. */
    private List m_filterContentList;
    
    /** Paramter used for filters. */
    private String m_param;
    
    /** Name of an individual accessed content value. */
    private String m_value;

    /** Indocates if the last element was ediable (could be page attribute). */
    private boolean m_wasEditable;
    
    /**
     * Adds parameters to a parameter Map that can be used for a http request.<p>
     * 
     * @param parameters the Map to add the parameters to
     * @param name the name to add
     * @param value the value to add
     * @param overwrite if <code>true</code>, a parameter in the map will be overwritten by
     *      a parameter with the same name, otherwise the request will have multiple parameters 
     *      with the same name (which is possible in http requests)
     */
    public static void addParameter(Map parameters, String name, String value, boolean overwrite) {
        // No null values allowed in parameters
        if ((parameters == null) || (name == null) || (value == null)) {
            return;
        }
        
        // Check if the parameter name (key) exists
        if (parameters.containsKey(name) && (! overwrite)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = (String[]) parameters.get(name);
            String[] newValues = new String[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            parameters.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] {value};
            parameters.put(name, values);
        } 
    }    

    /**
     * Internal action method to load an XML content file.<p>
     * 
     * @param context the current JSP page context
     * @param filename the XML content item file to load
     * @param editable if true, the element should be direct editable
     * @param req the current request 
     * @param editOptions the edit options to use
     * @param createLink  the link for creating new contents
     * 
     * @throws JspException in case somthing goes wrong
     * 
     * @return true if the element was editable
     */    
    public static boolean contentItemTagFileAction(PageContext context, String filename, boolean editable, String editOptions, String createLink, ServletRequest req) throws JspException {

        int todo = 0;
        // TODO: this methods must be re-written
        
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        CmsObject cms = controller.getCmsObject();
        
        CmsFile contentFile;
        CmsXmlContent content;
        try {
            // try to read and initialize the XML content
            contentFile = cms.readFile(filename);
            content = CmsXmlContentFactory.unmarshal(cms, contentFile);
        } catch (CmsException e) {
            controller.setThrowable(e, filename);
            throw new JspException(e);
        }
                
        // store the XML file name and the content object as a request attribute value
        req.setAttribute(ATTRIBUTE_FILENAME, filename);
        req.setAttribute(ATTRIBUTE_XMLCONTENT, content);   
        
        // check the "direct edit" mode
        String directEditPermissions = null;
        String directEditIncludeFile = null;          
        
        if (editable) {
            // get the include file where the direct edit HTML is stored in
            directEditIncludeFile = (String)context.getRequest().getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI);
            if (directEditIncludeFile != null) {
                // check the direct edit permissions of the current user                    
                directEditPermissions = OpenCms.getWorkplaceManager().getEditorActionHandler().getEditMode(controller.getCmsObject(), filename, null, req);
            }
            if (directEditPermissions == null) {
                // "editable" is true only if both direct edit include file and direct edit permissions are available
                editable = false;
            }            
        }        
        
        // include direct edit "start" element (if enabled)
        if (editable) {                
            includeDirectEditElement(context, directEditIncludeFile, I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_START + "_" + directEditPermissions, filename, editOptions, createLink, req, context.getResponse());
        }
        
        return editable;
    }
    
    /**
     * Internal action method to access an XML content item value.<p>
     * 
     * @param value the name of the value to access
     * @param req the current request 
     * @return the value of the accessed content item
     */     
    public static String contentItemTagValueAction(String value, ServletRequest req) {
        
        CmsXmlContent content = (CmsXmlContent)req.getAttribute(ATTRIBUTE_XMLCONTENT);
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        CmsObject cms = controller.getCmsObject();
        
        if (content == null) {
            return null;
        }        
        if ("opencms:filename".equals(value)) {
            return (String)req.getAttribute(ATTRIBUTE_FILENAME);
        }
        Locale locale = cms.getRequestContext().getLocale();
        try {
            return content.getStringValue(cms, value, locale);
        } catch (CmsXmlException e) {
            int todo = 0;
            // TODO: Improve exception handling
            throw new RuntimeException("Error setting value", e);
        }        
    }

    /**
     * Returns an option String for the direct editor generated from the provided values.<p>
     * 
     * @param showEdit indicates that the edit button should be shown 
     * @param showDelete indicates that the delete button should be shown 
     * @param showNew indicates that the new button should be shown 
     * @return an option String for the direct editor generated from the provided values
     */
    public static String createEditOptions(boolean showEdit, boolean showDelete, boolean showNew) {
        StringBuffer result = new StringBuffer(32);
        if (showEdit) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_EDIT);
        }
        result.append('|');            
        if (showDelete) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_DELETE);
        }            
        result.append('|');            
        if (showNew) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_NEW);
        }
        return result.toString();
    }
    
    /**
     * Includes the "direct edit" element that add HTML for the editable area to 
     * the output page.<p>
     * 
     * @param context the current JSP page context
     * @param target the source of the element
     * @param element the editor element to include       
     * @param req the current request
     * @param res the current response
     * @throws JspException in case something goes wrong         
     */
    private static void includeDirectEditElement(PageContext context, String target, String element, String editTarget, String editOptions, String createLink, ServletRequest req, ServletResponse res) 
    throws JspException {

        CmsFlexController controller = (CmsFlexController) req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        
        Map parameterMap = new HashMap();
        addParameter(parameterMap, I_CmsConstants.C_PARAMETER_ELEMENT, element, true);
        // set additional request parameters required by the included direct edit JSP 
        addParameter(parameterMap, I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_TARGET, editTarget, true);        
        addParameter(parameterMap, I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_LOCALE, controller.getCmsObject().getRequestContext().getLocale().toString(), true);        
        CmsUserSettings settings = new CmsUserSettings(controller.getCmsObject().getRequestContext().currentUser());        
        addParameter(parameterMap, I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_BUTTONSTYLE, String.valueOf(settings.getDirectEditButtonStyle()), true);
        if (editOptions != null) {
            addParameter(parameterMap, I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_OPTIONS, editOptions, true);
        }
        if (createLink != null) {
            addParameter(parameterMap, I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_NEWLINK, createLink, true);
        }
        
        try {
            
            controller.getCurrentRequest().addParameterMap(parameterMap); 
            context.getOut().print(CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
            controller.getCurrentResponse().addToIncludeList(target, parameterMap);
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, res);
            
        } catch (ServletException e) {
            Throwable t;
            if (e.getRootCause() != null) {
                t = e.getRootCause();
            } else {
                t = e;
            }
            t = controller.setThrowable(t, target);
            throw new JspException(t); 
        } catch (IOException e) {
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    }    
    
    
    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    public int doAfterBody() throws JspException {
        
        doEndTag();
        m_file = getNextFilname(); 
        if (m_file != null) {
            m_wasEditable = contentItemTagFileAction(pageContext, m_file, m_editable, createEditOptions(true, true, false), null, pageContext.getRequest());            
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        
        if (getFile() != null) {            
            // this is the end of a tag where a content was loaded, remove the attributes 
            pageContext.getRequest().removeAttribute(ATTRIBUTE_FILENAME);
            pageContext.getRequest().removeAttribute(ATTRIBUTE_XMLCONTENT);
            
            // include direct edit "end" element (if enabled)
            if (m_wasEditable) {
                
                CmsFlexController controller = (CmsFlexController)pageContext.getRequest().getAttribute(CmsFlexController.ATTRIBUTE_NAME);
                
                // check the "direct edit" mode
                String directEditPermissions = null;
                String directEditIncludeFile = null;          
                
                // get the include file where the direct edit HTML is stored in
                directEditIncludeFile = (String)pageContext.getRequest().getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI);
                if (directEditIncludeFile != null) {
                    // check the direct edit permissions of the current user                    
                    directEditPermissions = OpenCms.getWorkplaceManager().getEditorActionHandler().getEditMode(controller.getCmsObject(), getFile(), null,  pageContext.getRequest());
                }    
                includeDirectEditElement(pageContext, directEditIncludeFile, I_CmsEditorActionHandler.C_DIRECT_EDIT_AREA_END + "_" + directEditPermissions,  getFile(), null, null, pageContext.getRequest(), pageContext.getResponse());
            }
        }  
        
        return EVAL_PAGE;
    }
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        
        if (! CmsStringUtil.isEmpty(getValue())) {
            String value = contentItemTagValueAction(getValue(), pageContext.getRequest());
            // make sure that no null String is returned
            if (value == null) {
                value = "";
            }
            try {
                pageContext.getOut().print(value);
            } catch (IOException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error in Jsp 'contentitem' value tag processing", e);
                }
                throw new javax.servlet.jsp.JspException(e);
            }            
            return EVAL_BODY_INCLUDE;
        } 
        
        if (getFilter() != null) {
            
            // check if this is already initialized
            m_file = getNextFilname();
            
            if (m_file == null) {            
                CmsFlexController controller = (CmsFlexController)pageContext.getRequest().getAttribute(CmsFlexController.ATTRIBUTE_NAME);
                I_CmsXmlContentFilter filter;
    
                // HACK: Need to improve this
                filter = new CmsXmlDefaultContentFilter();       
                try {
                    m_filterContentList = filter.getFilterResults(controller.getCmsObject(), getFilter(), getParam());
                    m_createLink = CmsEncoder.encode(getFilter() + "|" + getParam());
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error in Jsp 'contentitem' filter tag processing", e);
                    }                
                    throw new javax.servlet.jsp.JspException(e);
                }
            }

            m_file = getNextFilname(); 
            m_wasEditable = contentItemTagFileAction(pageContext, getFile(), m_editable, createEditOptions(true, true, true), m_createLink, pageContext.getRequest());            
            return EVAL_BODY_INCLUDE;
        } 
        
        if (getFile() != null) {
            m_wasEditable = contentItemTagFileAction(pageContext, getFile(), m_editable, createEditOptions(true, false, false), null, pageContext.getRequest());
       
            return EVAL_BODY_INCLUDE;
        }
        
        return SKIP_BODY;
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
     * Returns the name of the individual content value was accessed.<p>
     * 
     * @return the selected value 
     */
    public String getValue() {
        
        return (m_value != null)?m_value:"";
    }
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */ 
    public void release() {
        
        super.release();        
        m_value = null;
        m_file = null;
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
     * Sets the value of an individual content filed that is to be accessed.<p>
     * 
     * @param value the value to set
     */
    public void setValue(String value) {
        
        m_value = value;
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