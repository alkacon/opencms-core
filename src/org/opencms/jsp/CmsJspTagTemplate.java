/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagTemplate.java,v $
 * Date   : $Date: 2004/03/29 10:39:54 $
 * Version: $Revision: 1.11 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceTypeXmlPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.util.CmsStringSubstitution;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to select various template elements form a JSP template that
 * is included in another file.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.11 $
 */
public class CmsJspTagTemplate extends BodyTagSupport { 
    
    /** Name of element */
    private String m_element;
    
    /** List of elements for element check */
    private String m_elementlist;
    
    /** Condition for element check */
    private boolean m_checkall;
    
    /** Condition for negative element check */
    private boolean m_checknone;
    
    /**
     * Sets the element target.<p>
     * 
     * @param element the target to set
     */
    public void setElement(String element) {
        if (element != null) {
            m_element = element.toLowerCase();
        }
    }
    
    /**
     * Returns the selected element.<p>
     * 
     * @return the selected element
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
            m_checknone = false;
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
            m_checknone = false;
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
            m_checknone = false;
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
     * Sets the list of elements to check.<p>
     * 
     * @param elements the list of elements
     */
    public void setIfexistsnone(String elements) {
        if (elements != null) {
            m_elementlist = elements;
            m_checkall = false;
            m_checknone = true;
        }           
    }

    /**
     * Returns the list of elements to check.<p>
     * 
     * @return the list of elements
     */
    public String getIfexistsnone() {
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
        if (templateTagAction(m_element, m_elementlist, m_checkall, m_checknone, pageContext.getRequest())) {
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
     * @param checknone flag to indicate that the check is done for nonexisting elements
     * @param req the current request 
     * @return boolean <code>true</code> if this element should be inclued, <code>false</code>
     * otherwise
     */    
    public static boolean templateTagAction(String element, String elementlist, boolean checkall, boolean checknone, ServletRequest req) {

        if (elementlist != null) {
            
            CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
            String filename = controller.getCmsObject().getRequestContext().getUri();
            
            CmsXmlPage page = (CmsXmlPage)req.getAttribute(filename);                    
            if (page == null) {
                CmsResource resource = controller.getCmsResource();
                if (resource.getType() == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID) {
                    try {
                        // make sure a page is only read once (not every time for each element)
                        page = CmsXmlPage.read(controller.getCmsObject(), CmsFile.upgrade(resource, controller.getCmsObject()));
                        req.setAttribute(filename, page);                
                    } catch (CmsException e) {
                        OpenCms.getLog(CmsJspTagTemplate.class).error("Error checking for XML page", e);
                    }
                }    
            }    
            
            if (page != null) {
                String absolutePath = controller.getCmsObject().readAbsolutePath(page.getFile());
                Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(controller.getCmsObject().getRequestContext().getLocale(), OpenCms.getLocaleManager().getDefaultLocales(controller.getCmsObject(), absolutePath), page.getLocales());
                
                // check the elements in the elementlist, if the check fails don't render the body
                String elements[] = CmsStringSubstitution.split(elementlist, ",");
                boolean found = false;
                for (int i = 0; i < elements.length; i++) {
                    String el = elements[i].trim();
                    if (page.hasElement(el, locale) && page.isEnabled(el, locale)) {
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
                
                if (!found && !checknone) {
                    // no element found while checking for existing elements
                    return false;
                } else if (found && checknone) {
                    // element found while checking for nonexisting elements
                    return false;
                }
            } 
        }
        
        // otherwise, check if an element was defined and if its equal to the desired element
        String param = req.getParameter(I_CmsConstants.C_PARAMETER_ELEMENT);        
        return ((element ==  null) || (param == null) || (param.equals(element)));
    }
 }