/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagTemplate.java,v $
 * Date   : $Date: 2004/01/20 17:09:43 $
 * Version: $Revision: 1.4 $
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
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;

import com.opencms.util.Utils;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to select various template elements form a JSP template that
 * is included in another file.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsJspTagTemplate extends BodyTagSupport { 
    
    // Attribute member variables
    
    /** Name of element */
    private String m_element = null;
    
    /** List of elements for element check */
    private String m_elementlist = null;
    
    /** Condition for element check */
    private boolean m_checkall = false;

    /** Template part identifier */
    public static final String C_TEMPLATE_ELEMENT = "__element";
    
    /**
     * Sets the include page/file target.
     * @param element the target to set
     */
    public void setElement(String element) {
        if (element != null) {
            m_element = element.toLowerCase();
        }
    }
    
    /**
     * Returns the include page/file target.
     * @return String
     */
    public String getElement() {
        return m_element!=null?m_element:"";
    }

    /**
     * Sets the list of elements to check.<p>
     * 
     * @param elements the list of elements
     */
    public void setIfexists(String elements) {
        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
        }
    }
    
    /**
     * Returns the list of elements to check.<p>
     * 
     * @return the list of elements
     */
    public String getIfexists() {
        return m_elementlist!=null?m_elementlist:"";
    }

    /**
     * Sets the list of elements to check.<p>
     * 
     * @param elements the list of elements
     */
    public void setIfexistsone(String elements) {
        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
        }        
    }

    /**
     * Returns the list of elements to check.<p>
     * 
     * @return the list of elements
     */
    public String getIfexistsone() {
        return m_elementlist!=null?m_elementlist:"";
    }

    /**
     * Sets the list of elements to check.<p>
     * 
     * @param elements the list of elements
     */
    public void setIfexistsall(String elements) {
        if (elements != null) {
            m_elementlist = elements;
            m_checkall = true;
        }           
    }

    /**
     * Returns the list of elements to check.<p>
     * 
     * @return the list of elements
     */
    public String getIfexistsall() {
        return m_elementlist!=null?m_elementlist:"";
    }
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */ 
    public void release() {
        super.release();
        m_element = null;
    }    

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        if (templateTagAction(m_element, m_elementlist, m_checkall, pageContext.getRequest())) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Internal action method.<p>
     * 
     * @param element the selected element
     * @param elementlist list the list of elements to check
     * @param checkall flag to indicate that all elements should be checked
     * @param req the current request 
     * @return boolean <code>true</code> if this element should be inclued, <code>false</code>
     * otherwise
     */    
    public static boolean templateTagAction(String element, String elementlist, boolean checkall, ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        CmsXmlPage page = (CmsXmlPage)req.getAttribute(org.opencms.loader.CmsXmlPageLoader.C_XMLPAGE_OBJECT);
        
        if (page != null && elementlist != null) {
            String absolutePath = controller.getCmsObject().readAbsolutePath(page.getFile());
            Locale l = OpenCms.getLocaleManager().getLocale(controller.getCmsObject(), absolutePath, page.getLanguages()); 
            String localeProp = l.toString();
            // check the elements in the elementlist, if the check fails don't render the body
            String elements[] = Utils.split(elementlist, ",");
            boolean found = false;
            for (int i = 0; i < elements.length; i++) {
                String el = elements[i].trim();
                if (page.hasElement(el, localeProp) && page.isEnabled(el, localeProp)) {
                    found = true;
                    if (!checkall) {
                        // found at least an element that is available
                        break;
                    }
                } else {
                    if (checkall) {
                        // found at least an element that is not available
                        return false;
                    }
                }
            }
            
            // no element found
            if (!found) {
                return false;
            }
        } 
        
        // otherwise, check if an element was defined and if its equal to the desired element
        String param =  req.getParameter(C_TEMPLATE_ELEMENT);        
        return ((element ==  null) || (param == null) || (param.equals(element)));
    }
 }