/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDialogElements.java,v $
 * Date   : $Date: 2004/02/16 12:05:58 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editor;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
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
 * @version $Revision: 1.13 $
 * 
 * @since 5.3.0
 */
public class CmsDialogElements extends CmsDialog {
    
    /** Value for the action: delete the content of an element */
    public static final int ACTION_DELETECONTENT = 200;

    /** Value for the action: update the elements of the page */
    public static final int ACTION_UPDATE_ELEMENTS = 210;
    
    /** Request parameter value for the action: delete the content of an element */
    public static final String DIALOG_DELETECONTENT = "deletecontent";

    /** The dialog type */
    public static final String DIALOG_TYPE = "elementselector";
    
    /** Request parameter value for the action: update the elements of the page */
    public static final String DIALOG_UPDATE_ELEMENTS = "updateelements";
    
    /** Prefix for the html input field for the body */
    public static final String PREFIX_PARAM_BODY = "element-";
    
    /** List used to store information of all possible elements of the page */
    private List m_elementList = null;

    /** The element locale */
    private Locale m_elementLocale;
    
    // Special parameters used by this dialog
    private String m_paramElementlanguage;
    private String m_paramElementname;
    private String m_paramDeleteElementContent;
    private String m_paramTempFile;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDialogElements(CmsJspActionElement jsp) {
        super(jsp);
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
    } 
    
    /**
     * Creates a list of possible elements of a template from the template property "template-elements".<p>
     * 
     * @param cms the CmsObject
     * @param resource the resource to read from
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     * @throws CmsException if reading the property fails
     */
    public static List computeElements(CmsObject cms, String resource) throws CmsException {   
        List elementList = new ArrayList();
        String currentTemplate = cms.readProperty(resource, I_CmsConstants.C_PROPERTY_TEMPLATE, true);
        if (currentTemplate == null || currentTemplate.length() == 0) {
            // no template found, return empty list
            return elementList;
        }
        String elements = null;
        
        try {
            // read the property from the template file
            elements = cms.readProperty(currentTemplate, I_CmsConstants.C_PROPERTY_TEMPLATE_ELEMENTS, false, null);
        } catch (CmsException e) {
            // ignore this exception
        }
        if (elements == null) {
            // no elements defined on template file , return empty list
            return elementList;
        }
        StringTokenizer T = new StringTokenizer(elements, ",");
        while (T.hasMoreTokens()) {
            String currentElement = T.nextToken();
            String niceName = "";
            String mandatory = "0";
            int sepIndex = currentElement.indexOf("|");
            if (sepIndex != -1) {
                // nice name found for current element, extract it
                niceName = currentElement.substring(sepIndex + 1);
                currentElement = currentElement.substring(0, sepIndex);
            }
            if (currentElement.endsWith("*")) {
                // element is mandatory
                mandatory = "1";
                currentElement = currentElement.substring(0, currentElement.length() - 1);
            }
            if ("".equals(niceName)) {
                // no nice name found, use element name as nice name
                niceName = currentElement;
            }
            elementList.add(new String[] {currentElement, niceName, mandatory});
        }
        return elementList;
    }
    
    /**
     * Deletes the content of an element specified in the parameter "deleteelement".<p>
     */
    public void actionDeleteElementContent() {
        try {
            CmsFile file = getCms().readFile(this.getParamTempfile());
            CmsXmlPage page = CmsXmlPage.read(getCms(), file);
            // set the content of the element to an empty String
            page.setContent(getCms(), getParamDeleteElement(), getElementLocale(), "");
            // write the temporary file
            getCms().writeFile(page.write(file));
        } catch (CmsException e) {
            // ignore this exception
        }
    }
    
    /**
     * Updates the enabled/diabled status of all elements of the current page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionUpdateElements() throws JspException {
        try {
            List elementList = computeElements();
            if (elementList == null) {
                throw new CmsException("Elements not specified on template file!");
            }
            CmsFile file = getCms().readFile(this.getParamTempfile());
            CmsXmlPage page = CmsXmlPage.read(getCms(), file);
            boolean foundMandatory = false;
            String changeElement = "";
            Iterator i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                String[] currentElement = (String[])i.next();               
                String elementName = currentElement[0];
                boolean isExisting = page.hasElement(elementName, getElementLocale());
                boolean isMandatory = "1".equals(currentElement[2]);
                if (isMandatory || "true".equals(getJsp().getRequest().getParameter(PREFIX_PARAM_BODY + elementName))) {
                    if (!isExisting) {
                        // create element in order to enable it properly 
                        page.addElement(elementName, getElementLocale());
                    }
                    page.setEnabled(elementName, getElementLocale(), true);
                    if (isMandatory && !foundMandatory) {
                        changeElement = elementName;
                        foundMandatory = true;
                    }
                } else {
                    if (isExisting) {
                        // disable element if it is already existing
                        page.setEnabled(elementName, getElementLocale(), false);
                    }
                }
            }
            // write the temporary file
            getCms().writeFile(page.write(file));
            // set the javascript functions which should be executed
            if (page.isEnabled(getParamElementname(), getElementLocale())) {
                changeElement = getParamElementname();
            } else if (!foundMandatory) {
                changeElement = ((String[])elementList.get(0))[0];
            }
            setParamOkFunctions("window.opener.changeElement(\"" + changeElement + "\", \"" + getElementLocale() + "\");window.close();");
                       
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            // if no exception is caused update operation was successful
            closeDialog();
        } catch (CmsException e) {
            // show error dialog
            setParamOkFunctions("window.close();");
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.editor.elements"));
            String reason = key("error.reason.editor.elements") + "<br>\n" + key("error.suggestion.editor.elements") + "\n";
            setParamReasonSuggestion(reason);
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            try {
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR); 
            } catch (Exception exc) {
                // ignore this exception
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
            if (elementList == null) {
                throw new CmsException("Elements not specified on template file!");
            }
            
            // get all present bodies from the temporary file
            CmsFile file = getCms().readFile(this.getParamTempfile());
            CmsXmlPage page = CmsXmlPage.read(getCms(), file);
            
            // show all possible elements
            Iterator i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                String[] currentElement = (String[])i.next();               
                String elementName = currentElement[0];
                String elementNice = currentElement[1];
                boolean isMandatory = "1".equals(currentElement[2]);
                // build an element row
                retValue.append("<tr>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">" + elementNice);
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\"><input type=\"checkbox\" name=\"" + PREFIX_PARAM_BODY + elementName + "\" value=\"true\"");
                if (!page.hasElement(elementName, getElementLocale()) || page.isEnabled(elementName, getElementLocale())) {
                    retValue.append(" checked=\"checked\"");
                }
                if (isMandatory) {
                    retValue.append(" disabled=\"disabled\"");
                }
                retValue.append(">");
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\" unselectable=\"on\">");
                retValue.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                if (!"".equals(page.getContent(getCms(), elementName, getElementLocale()))) {
                    // current element has content that can be deleted
                    retValue.append(button("javascript:confirmDelete('" + elementName + "');", null, "deletecontent", "button.delete", 0));
                } else {
                    // current element is empty
                    retValue.append(button(null, null, "deletecontent_in", "button.delete", 0));
                }
                retValue.append("</tr></table>");
                retValue.append("</td>\n");                
                retValue.append("</tr>\n");
            }
            
            
            
        } catch (CmsException e) {
            // ignore this exception
        }
        
        retValue.append("</table>\n");
        return retValue.toString();
    }
    
    /**
     * Creates a list of possible elements of a template from the template property "template-elements".<p>
     * 
     * @return the list of elements in a String array with element name, nice name (if present) and mandatory flag
     * @throws CmsException if reading the property fails
     */
    public List computeElements() throws CmsException {
        if (m_elementList == null) {
            m_elementList = computeElements(getCms(), getParamTempfile());
        }
        return m_elementList;
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
