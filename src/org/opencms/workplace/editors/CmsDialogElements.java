/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsDialogElements.java,v $
 * Date   : $Date: 2005/02/17 12:44:31 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the editor elements dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/dialogs/elements.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.3.0
 */
public class CmsDialogElements extends CmsDialog {
    
    /** Value for the action: delete the content of an element. */
    public static final int ACTION_DELETECONTENT = 200;

    /** Value for the action: update the elements of the page. */
    public static final int ACTION_UPDATE_ELEMENTS = 210;
    
    /** Request parameter value for the action: delete the content of an element. */
    public static final String DIALOG_DELETECONTENT = "deletecontent";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "elementselector";
    
    /** Request parameter value for the action: update the elements of the page. */
    public static final String DIALOG_UPDATE_ELEMENTS = "updateelements";
    
    /** Prefix for the html input field for the body. */
    public static final String PREFIX_PARAM_BODY = "element-";
    
    /** List used to store information of all possible elements of the page. */
    private List m_elementList;

    /** The element locale. */
    private Locale m_elementLocale;
    
    // Special parameters used by this dialog
    private String m_paramElementlanguage;
    private String m_paramElementname;
    private String m_paramDeleteElementContent;
    private String m_paramTempFile;
    
    /** Stores the element to change to after an element update operation. */
    private String m_changeElement;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDialogElements(CmsJspActionElement jsp) {
        super(jsp);
        m_changeElement = "";
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialogElements(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
        m_changeElement = "";
    } 
    
    /**
     * Creates a list of possible elements of a template from the template property "template-elements"
     * and the elements available in the provided xmlPage.<p>
     * 
     * @param cms the CmsObject
     * @param xmlPage the resource to read the elements from
     * @param xmlPageUri the URI of the resouirce to read the template property from
     * @param locale the current element locale
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public static List computeElements(CmsObject cms, CmsXmlPage xmlPage, String xmlPageUri, Locale locale) {
        List result = new ArrayList();
        
        if (xmlPage != null) {
            List elementNames = xmlPage.getNames(locale);
            
            Iterator i = elementNames.iterator();
            while (i.hasNext()) {
                String name = (String)i.next();
                CmsDialogElement element = new CmsDialogElement(name, null, false, false, true);
                result.add(element);
            }
        }
        
        String currentTemplate = null;
        try {
            currentTemplate = cms.readPropertyObject(xmlPageUri, I_CmsConstants.C_PROPERTY_TEMPLATE, true).getValue();
        } catch (CmsException e) {
            if (OpenCms.getLog(CmsDialogElements.class.getName()).isWarnEnabled()) {
                OpenCms.getLog(CmsDialogElements.class.getName()).warn("Error reading property '" + I_CmsConstants.C_PROPERTY_TEMPLATE + "' on resource " + xmlPageUri, e);
            }
        } 
        if (currentTemplate != null && currentTemplate.length() > 0) {
            // template found, check template-elements property
            String elements = null;
            try {
                // read the property from the template file
                elements = cms.readPropertyObject(currentTemplate, I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS, false).getValue(null);
            } catch (CmsException e) {
                if (OpenCms.getLog(CmsDialogElements.class.getName()).isWarnEnabled()) {
                    OpenCms.getLog(CmsDialogElements.class.getName()).warn("Error reading property '" + I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS + "' on resource " + currentTemplate, e);
                }
            }
            if (elements != null) {
                // elements are defined on template file, merge with available elements
                StringTokenizer T = new StringTokenizer(elements, ",");
                while (T.hasMoreTokens()) {
                    String currentElement = T.nextToken();
                    String niceName = null;
                    boolean mandatory = false;
                    int sepIndex = currentElement.indexOf("|");
                    if (sepIndex != -1) {
                        // nice name found for current element, extract it
                        niceName = currentElement.substring(sepIndex + 1);
                        currentElement = currentElement.substring(0, sepIndex);
                    }
                    if (currentElement.endsWith("*")) {
                        // element is mandatory
                        mandatory = true;
                        currentElement = currentElement.substring(0, currentElement.length() - 1);
                    }
                    
                    CmsDialogElement element = new CmsDialogElement(currentElement, niceName, mandatory, true, false);
                    if (result.contains(element)) {
                        element.setExisting(true);
                        result.remove(element);
                    }
                    result.add(element);
                   
                }
            }
        }
        
        Collections.sort(result);
        return result;
    }
    
    /**
     * Creates a list of possible elements of a template from the template property "template-elements"
     * and the elements available in the provided resource file.<p>
     * 
     * @param cms the CmsObject
     * @param xmlPageUri the resource to read the elements from
     * @param locale the current element locale
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public static List computeElements(CmsObject cms, String xmlPageUri, Locale locale) {           
     
        CmsXmlPage page = null;        
        try {
            // read the xmlpage file
            CmsFile pageFile = cms.readFile(xmlPageUri, CmsResourceFilter.IGNORE_EXPIRATION);
            page = CmsXmlPageFactory.unmarshal(cms, pageFile);
        } catch (CmsException e) {
            OpenCms.getLog(CmsDialogElements.class).warn("Could not read xmlPage from uri '" + xmlPageUri + "'", e);
            // xmlpage will be null, only "template-elements" property on template will be checked
        }        
        return computeElements(cms, page, xmlPageUri, locale);        
    }
    
    /**
     * Deletes the content of an element specified in the parameter "deleteelement".<p>
     */
    public void actionDeleteElementContent() {
        try {
            CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);
            // set the content of the element to an empty String
            page.setStringValue(getCms(), getParamDeleteElement(), getElementLocale(), "");
            // write the temporary file
            file.setContents(page.marshal());
            getCms().writeFile(file);
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
    }
    
    /**
     * Updates the enabled/diabled status of all elements of the current page.<p>
     */
    public void actionUpdateElements() {
        try {
            List elementList = computeElements();
            CmsFile file = getCms().readFile(getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);
            boolean foundMandatory = false;
            m_changeElement = "";
            Iterator i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                CmsDialogElement element = (CmsDialogElement)i.next();               
                if (element.isMandantory() 
                || "true".equals(getJsp().getRequest().getParameter(PREFIX_PARAM_BODY + element.getName()))) {
                    if (!element.isExisting()) {
                        // create element in order to enable it properly 
                        page.addValue(element.getName(), getElementLocale());
                    }
                    page.setEnabled(element.getName(), getElementLocale(), true);
                    if (element.isMandantory() && !foundMandatory) {
                        m_changeElement = element.getName();
                        foundMandatory = true;
                    }
                } else {
                    if (element.isExisting()) {
                        // must set enabled to true or check for contains always fails
                        page.setEnabled(element.getName(), getElementLocale(), true);
                        // disable element if it is already existing
                        if (page.getStringValue(getCms(), element.getName(), getElementLocale()) == null) {
                            // element is not defined in template, empty and disabled - remove it
                            page.removeValue(element.getName(), getElementLocale());
                        } else {
                            page.setEnabled(element.getName(), getElementLocale(), false);
                        }
                    }                    
                }
            }
            // write the temporary file
            file.setContents(page.marshal());
            getCms().writeFile(file);
            // set the javascript functions which should be executed
            if (page.isEnabled(getParamElementname(), getElementLocale())) {
                m_changeElement = getParamElementname();
            } else if (!foundMandatory) {
                if (elementList.size() > 0) {
                    m_changeElement = ((CmsDialogElement)elementList.get(0)).getName();                    
                }
            }                       
        } catch (CmsException e) {
            // show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.editor.elements"));
            String reason = key("error.reason.editor.elements") + "<br>\n" + key("error.suggestion.editor.elements") + "\n";
            setParamReasonSuggestion(reason);
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            try {
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR); 
            } catch (Exception exc) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(exc);
                }
            }
        }
    }
    
    /**
     * Builds the html String for a form list of all possible page elements.<p>
     * 
     * @return the html String for a form list
     */
    public String buildElementList() {
        StringBuffer retValue = new StringBuffer(512);
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("\t<td class=\"textbold\" unselectable=\"on\">"+key("editor.dialog.elements.pageelement")+"</td>\n");
        retValue.append("\t<td class=\"textbold\" unselectable=\"on\">&nbsp;&nbsp;"+key("editor.dialog.elements.enabled")+"&nbsp;&nbsp;</td>\n");
        retValue.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\" unselectable=\"on\">"+key("editor.dialog.elements.deletecontent")+"</td>\n");            
        retValue.append("</tr>\n");
        retValue.append("<tr><td><span style=\"height: 6px;\"></span></td></tr>\n");
        
        try {
            
            // get the list of all possible elements
            List elementList = computeElements();
            
            // get all present bodies from the temporary file
            CmsFile file = getCms().readFile(this.getParamTempfile(), CmsResourceFilter.IGNORE_EXPIRATION);
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);
            
            // show all possible elements
            Iterator i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                CmsDialogElement element = (CmsDialogElement)i.next();               
                // build an element row
                retValue.append("<tr>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">" + element.getNiceName());
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\"><input type=\"checkbox\" name=\"" + PREFIX_PARAM_BODY + element.getName() + "\" value=\"true\"");
                
                if ((!page.hasValue(element.getName(), getElementLocale()) && element.isMandantory())
                || page.isEnabled(element.getName(), getElementLocale())) {
                    retValue.append(" checked=\"checked\"");
                }
                if (element.isMandantory()) {
                    retValue.append(" disabled=\"disabled\"");
                }
                retValue.append(">");
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\">");
                retValue.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                if (page.getStringValue(getCms(), element.getName(), getElementLocale()) != null) {
                    // current element has content that can be deleted
                    retValue.append(button("javascript:confirmDelete('" + element.getName() + "');", null, "deletecontent", "button.delete", 0));
                } else {
                    // current element is empty
                    retValue.append(button(null, null, "deletecontent_in", "button.delete", 0));
                }
                retValue.append("</tr></table>");
                retValue.append("</td>\n");                
                retValue.append("</tr>\n");
            }
            
            
            
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        
        retValue.append("</table>\n");
        return retValue.toString();
    }
    
    /**
     * Creates a list of possible elements of a template from the template property "template-elements".<p>
     * 
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     */
    public List computeElements() {
        if (m_elementList == null) {
            m_elementList = computeElements(getCms(), getParamTempfile(), getElementLocale());
        }
        return m_elementList;
    }
    
    /**
     * Returns the element name that has to be changed.<p>
     * 
     * @return the element name that has to be changed
     */
    public String getChangeElement() {
        return m_changeElement;
    }
    
    /**
     * Returns the current element locale.<p>
     * 
     * @return the current element locale
     */
    public Locale getElementLocale() {
        if (m_elementLocale == null) {
            m_elementLocale = CmsLocaleManager.getLocale(getParamElementlanguage());
        } 
        return m_elementLocale;
    }    
    
    /**
     * Returns the current element language.<p>
     * 
     * @return the current element language
     */
    public String getParamElementlanguage() {
        return m_paramElementlanguage;
    }
    
    /**
     * Returns the current element name.<p>
     * 
     * @return the current element name
     */
    public String getParamElementname() {
        return m_paramElementname;
    }
    
    /**
     * Returns the element name to delete its content.<p>
     * 
     * @return the element name to delete its content
     */
    public String getParamDeleteElement() {
        return m_paramDeleteElementContent;
    }
    
    /**
     * Returns the name of the temporary file.<p>
     * 
     * @return the name of the temporary file
     */
    public String getParamTempfile() {
        return m_paramTempFile;
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_DELETECONTENT.equals(getParamAction())) {
            setAction(ACTION_DELETECONTENT);                            
        } else if (DIALOG_UPDATE_ELEMENTS.equals(getParamAction())) {
            setAction(ACTION_UPDATE_ELEMENTS);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for delete dialog     
            setParamTitle(key("editor.dialog.elements.title") + ": " + CmsResource.getName(getParamResource()));
        }      
    } 
    
    /**
     * Sets the current element language.<p>
     * 
     * @param elementLanguage the current element language
     */
    public void setParamElementlanguage(String elementLanguage) {
        m_paramElementlanguage = elementLanguage;
    }

    /**
     * Sets the current element name.<p>
     * 
     * @param elementName the current element name
     */
    public void setParamElementname(String elementName) {
        m_paramElementname = elementName;
    }
    
    /**
     * Sets the element name to delete its content.<p>
     * 
     * @param deleteElement the element name to delete its content
     */
    public void setParamDeleteElement(String deleteElement) {
        m_paramDeleteElementContent = deleteElement;
    }
    
    /**
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {
        m_paramTempFile = fileName;
    }
    
}
