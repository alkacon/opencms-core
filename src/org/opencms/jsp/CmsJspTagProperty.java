/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagProperty.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
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

import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;


import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Provides access to the properties of a resource in the OpenCms VFS .<p>
 * 
 * Of particular importance is the setting of the <code>file</code> attribute,
 * which can take the following values.<p>
 * 
 * This attribute allows you to specify where to search for the property.<BR>            
 * The following values are supported: 
 * </P>
 * <DL>
 *   <DT><b>uri</b> (default) 
 *   <DD>  Look up  the property on the file with the 
 *   uri requested by the user. 
 *   <DT><b>search.uri</b> or <b>search</b> 
 *   <DD>Look up the property by also checking all parent folders for the property, 
 *   starting with the file with uri requested by the user and 
 *   going "upward" if the property was not found there. 
 *   <DT><b>element.uri</b> 
 *   <DD>Look up the property on the currently 
 *   processed sub - element. This is useful in templates or other pages that 
 *   consist of many elements.   
 *   <DT><b>search.element.uri</b> 
 *   <DD>Look up the property by also checking all parent folders for the 
 *   property, starting with the file with the currently processed sub - 
 *   element and going "upward" if the property was not found there.
 *   <DT><B>{some-file-uri}</B> 
 *   <DD>Look up the property on that exact file 
 *   uri in the OpenCms VFS,<EM> fallback if no other valid option is 
 *   selected for the file attribute.</EM>           
 *   </DD>
 * </DL>
 *   
 * <P>There are also some deprecated options for the "file" value that are 
 * still supported but should not longer be used:</P>
 * <DL>
 *   <DT>parent 
 *   <DD>same as <STRONG>uri</STRONG> 
 *   <DT>search-parent 
 *   <DD>same as <STRONG>search.uri</STRONG> 
 *   <DT>this
 *   <DD>same as <STRONG>element.uri</STRONG> 
 *   <DT>search-this 
 *   <DD>same as <STRONG>search.element.uri</STRONG></DD>
 * </DL>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class CmsJspTagProperty extends javax.servlet.jsp.tagext.TagSupport {
    
    // internal member variables
    private String m_propertyName = null;    
    private String m_propertyFile = null;    
    private String m_defaultValue = null;
    private boolean m_escapeHtml = false;
    
    /** Accessor constant: Use uri */
    public static final String USE_URI = "uri";
    
    /** Accessor constant: Use parent (same as USE_URI) */
    public static final String USE_PARENT = "parent";
    
    /** Accessor constant: Use search (same as USE_SEARCH_URI) */
    public static final String USE_SEARCH = "search";
    
    /** Accessor constant: Search uri */
    public static final String USE_SEARCH_URI = "search.uri";
    
    /** Accessor constant: Search parent (same as USE_SEARCH_URI) */
    public static final String USE_SEARCH_PARENT = "search-parent";
    
    /** Accessor constant: Use element uri */
    public static final String USE_ELEMENT_URI = "element.uri";
    
    /** Accessor constant: Use this (same as USE_ELEMENT_URI)*/
    public static final String USE_THIS = "this";
    
    /** Accessor constant: Use search element uri */
    public static final String USE_SEARCH_ELEMENT_URI = "search.element.uri";
    
    /** Accessor contant: Use seach this (same as USE_SEARCH_ELEMENT_URI)*/
    public static final String USE_SEARCH_THIS = "search-this";
    
    // DEBUG flag
    private static final int DEBUG = 0;
    
    /** static array of the possible "file" properties */
    public static final String[] m_actionValues = {
            USE_URI,
            USE_PARENT,
            USE_SEARCH,
            USE_SEARCH_URI,
            USE_SEARCH_PARENT,
            USE_ELEMENT_URI,
            USE_THIS,
            USE_SEARCH_ELEMENT_URI,
            USE_SEARCH_THIS
        };

    /** array list for fast lookup */
    public static final java.util.List m_actionValue =
        java.util.Arrays.asList(m_actionValues);    
    
    /**
     * Sets the property name.<p>
     * 
     * @param name the property name to set
     */
    public void setName(String name) {
        if (name != null) {
            m_propertyName = name;
        }
    }
    
    /**
     * Returns the property name.<p>
     * 
     * @return String the property name
     */
    public String getName() {
        return m_propertyName!=null?m_propertyName:"";
    }

    /**
     * Sets the default value.<p>
     * 
     * This is used if a selected property is not found.<p>
     * 
     * @param def the default value
     */
    public void setDefault(String def) {
        if (def != null) {
            m_defaultValue = def;
        }
    }
    
    /**
     * Returns the default value.<p>
     * 
     * @return the default value
     */
    public String getDefault() {
        return m_defaultValue!=null?m_defaultValue:"";
    }
    
    /**
     * Sets the file name.<p>
     * 
     * @param file the file name
     */
    public void setFile(String file) {
        if (file != null) {
            m_propertyFile = file.toLowerCase();
        }
    }
    
    /**
     * Returns the file name.<p>
     * 
     * @return the file name
     */
    public String getFile() {
        return m_propertyFile!=null?m_propertyFile:"parent";
    }

    /**
     * Set the escape html flag.<p>
     * 
     * @param value should be "true" or "false" (all values other then "true" are
     * considered to be false)
     */
    public void setEscapeHtml(String value) {
        if (value != null) {
            m_escapeHtml = "true".equalsIgnoreCase(value.trim());
        }
    }

    /**
     * The value of the escape html flag.<p>
     * 
     * @return the value of the escape html flag
     */
    public String getEscapeHtml() {
        return "" + m_escapeHtml;
    }
        
    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        m_propertyFile = null;
        m_propertyName = null;
        m_defaultValue = null;
        m_escapeHtml = false;
    }    
    
    /**
     * @return SKIP_BODY
     * @throws JspException in case somethins goes wrong
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        
        ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            
            try {       
                String prop = propertyTagAction(getName(), getFile(), m_defaultValue, m_escapeHtml, req);
                // Make sure that no null String is returned
                if (prop == null) {
                    prop = "";
                }
                pageContext.getOut().print(prop);
                
            } catch (Exception ex) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error in Jsp 'property' tag processing", ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Internal action method.<p>
     * 
     * @param property the property to look up
     * @param action the search action
     * @param defaultValue the default value
     * @param escape if the result html should be escaped or not
     * @param req the current request
     * @return String the value of the property or <code>null</code> if not found (and no
     *      defaultValue provided)
     * @throws CmsException if something goes wrong
     */
    public static String propertyTagAction(
        String property, 
        String action, 
        String defaultValue, 
        boolean escape, 
        ServletRequest req
    ) throws CmsException {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        if (DEBUG > 0) {      
            System.err.println("propertyTagAction() called!\nproperty=" + property 
                + "\naction=" + action 
                + "\ndefaultValue=" + defaultValue 
                + "\nescape=" + escape);
            System.err.println("propertyTagAction() request URI=" + controller.getCmsObject().getRequestContext().getUri());
        }
        String value;
        
        // if action is not set use default
        if (action == null) {
            action = m_actionValues[0];
        }

        switch (m_actionValue.indexOf(action)) {      
            case 0: // USE_URI
            case 1: // USE_PARENT
                // Read properties of parent (i.e. top requested) file
                value = controller.getCmsObject().readProperty(controller.getCmsObject().getRequestContext().getUri(), property, false, defaultValue); 
                break;
            case 2: // USE_SEARCH
            case 3: // USE_SEARCH_URI
            case 4: // USE_SEARCH_PARENT 
                // Try to find property on parent file and all parent folders
                value = controller.getCmsObject().readProperty(controller.getCmsObject().getRequestContext().getUri(), property, true, defaultValue);
                break;                
            case 5: // USE_ELEMENT_URI
            case 6: // USE_THIS
                // Read properties of this file            
                value = controller.getCmsObject().readProperty(controller.getCurrentRequest().getElementUri(), property, false, defaultValue);
                break;
            case 7: // USE_SEARCH_ELEMENT_URI
            case 8: // USE_SEARCH_THIS
                // Try to find property on this file and all parent folders
                value = controller.getCmsObject().readProperty(controller.getCurrentRequest().getElementUri(), property, true, defaultValue);
                break;
            default:
                // Read properties of the file named in the attribute            
                value = controller.getCmsObject().readProperty(CmsLinkManager.getAbsoluteUri(action, controller.getCurrentRequest().getElementUri()), property, false, defaultValue);
        }           
        if (escape) {
            value = CmsEncoder.escapeHtml(value);
        }
        if (DEBUG > 0) {
            System.err.println("propertyTagAction(): result=" + value);
        }
        return value;
    }

}
