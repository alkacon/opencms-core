/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDialogElements.java,v $
 * Date   : $Date: 2004/01/14 10:00:04 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.0
 */
public class CmsDialogElements extends CmsDialog {

    /** The dialog type */
    public static final String DIALOG_TYPE = "elementselector";
    
    /** Prefix for the html input field for the body */
    public static final String PREFIX_PARAM_BODY = "element-";
    
    /** Value for the action: delete the content of an element */
    public static final int ACTION_DELETECONTENT = 200;
    /** Value for the action: update the elements of the page */
    public static final int ACTION_UPDATE_ELEMENTS = 210;
    
    /** Request parameter value for the action: delete the content of an element */
    public static final String DIALOG_DELETECONTENT = "deletecontent";
    /** Request parameter value for the action: update the elements of the page */
    public static final String DIALOG_UPDATE_ELEMENTS = "updateelements";
    
    /** List used to store information of all possible elements of the page */
    private List m_elementList = null;
    
    /** special parameters used by this dialog */
    private String m_paramBodyname;
    private String m_paramBodylanguage;
    private String m_paramDeleteElementContent;
    private String m_paramPageTemplate;
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
     * Returns the current body element name.<p>
     * 
     * @return the current body element name
     */
    public final String getParamBodyname() {
        return m_paramBodyname;
    }

    /**
     * Sets the current body element name.<p>
     * 
     * @param bodyname the current body element name
     */
    public final void setParamBodyname(String bodyname) {
        m_paramBodyname = bodyname;
    }
    
    /**
     * Returns the current body element language.<p>
     * 
     * @return the current body element language
     */
    public final String getParamBodylanguage() {
        return m_paramBodylanguage;
    }

    /**
     * Sets the current body element language.<p>
     * 
     * @param bodyLanguage the current body element language
     */
    public final void setParamBodylanguage(String bodyLanguage) {
        m_paramBodylanguage = bodyLanguage;
    }
    
    /**
     * Returns the element name to delete its content.<p>
     * 
     * @return the element name to delete its content
     */
    public final String getParamDeleteElement() {
        return m_paramDeleteElementContent;
    }
    
    /**
     * Sets the element name to delete its content.<p>
     * 
     * @param deleteElement the element name to delete its content
     */
    public final void setParamDeleteElement(String deleteElement) {
        m_paramDeleteElementContent = deleteElement;
    }
    
    /**
     * Returns the page template.<p>
     * 
     * @return the page template
     */
    public final String getParamPagetemplate() {
        return m_paramPageTemplate;
    }
    
    /**
     * Sets the page template.<p>
     * 
     * @param pageTemplate the page template
     */
    public final void setParamPagetemplate(String pageTemplate) {
        m_paramPageTemplate = pageTemplate;
    }
    
    /**
     * Returns the name of the temporary file.<p>
     * 
     * @return the name of the temporary file
     */
    public final String getParamTempfile() {
        return m_paramTempFile;
    }
    
    /**
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public final void setParamTempfile(String fileName) {
        m_paramTempFile = fileName;
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
     * Deletes the content of an element specified in the parameter "deleteelement".<p>
     */
    public void actionDeleteElementContent() {
        try {
            CmsFile file = getCms().readFile(this.getParamTempfile());
            CmsXmlPage page = CmsXmlPage.read(getCms(), file);
            // set the content of the element to an empty String
            page.setContent(getCms(), getParamDeleteElement(), getParamBodylanguage(), "");
            // write the temporary file
            getCms().writeFile(page.write(file));
            // set the parameter value back to null
            setParamDeleteElement(null);
        } catch (CmsException e) {
            // TODO: show exception
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
            String changeBody = "";
            Iterator i = elementList.iterator();
            while (i.hasNext()) {
                // get the current list element
                String[] currentElement = (String[])i.next();               
                String elementName = currentElement[0];
                boolean isMandatory = "1".equals(currentElement[2]);
                if (isMandatory || "true".equals(getJsp().getRequest().getParameter(PREFIX_PARAM_BODY + elementName))) {
                    page.setEnabled(elementName, getParamBodylanguage(), true);
                    if (isMandatory && !foundMandatory) {
                        changeBody = elementName;
                        foundMandatory = true;
                    }
                } else {
                    page.setEnabled(elementName, getParamBodylanguage(), false);
                }
            }
            // write the temporary file
            getCms().writeFile(page.write(file));
            // set the javascript functions which should be executed
            if (page.isEnabled(getParamBodyname(), getParamBodylanguage())) {
                changeBody = getParamBodyname();
            } else if (!foundMandatory) {
                changeBody = ((String[])elementList.get(0))[0];
            }
            setParamOkFunctions("window.opener.changeBody(\"" + changeBody + "\", \"" + getParamBodylanguage() + "\");window.close();");
                       
            // save initialized instance of this class in request attribute for included sub-elements
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            // if no exception is caused update operation was successful
            closeDialog();
        } catch (CmsException e) {
            // TODO: show exception
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
        retValue.append("\t<td class=\"textbold\">"+key("editor.dialog.elements.pageelement")+"</td>\n");
        retValue.append("\t<td class=\"textbold\">"+key("editor.dialog.elements.enabled")+"</td>\n");
        retValue.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">"+key("editor.dialog.elements.deletecontent")+"</td>\n");            
        retValue.append("</tr>\n");
        retValue.append("<tr>\n\t<td>"+dialogSpacer()+"</td>\n</tr>\n");
        
        try {
            
            String buttonFolder = getSkinUri() + "editors/buttons/";
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
                retValue.append("\t<td style=\"white-space: nowrap;\">" + elementNice);
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\"><input type=\"checkbox\" name=\"" + PREFIX_PARAM_BODY + elementName + "\" value=\"true\"");
                if (page.isEnabled(elementName, getParamBodylanguage())) {
                    retValue.append(" checked=\"checked\"");
                }
                if (isMandatory) {
                    retValue.append(" disabled=\"disabled\"");
                }
                retValue.append(">");
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\">");
                retValue.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                if (!"".equals(page.getContent(getCms(), elementName, getParamBodylanguage()))) {
                    // current element has content that can be deleted
                    //retValue.append("<a href=\"javascript:confirmDelete('" + elementName + "');\">");
                    //retValue.append("<img src=\"" + buttonFolder + ".gif\" border=\"0\" title=\"" + key("button.delete") + "\">");
                    retValue.append(button("javascript:confirmDelete('" + elementName + "');", null, "deletecontent", "button.delete", 0, buttonFolder));
                    //retValue.append("</a>");
                } else {
                    // current element is empty
                    //retValue.append("<img src=\"" + buttonFolder + ".gif\" border=\"0\" title=\"" + key("button.delete") + "\">");
                    retValue.append(button(null, null, "deletecontent_in", "button.delete", 0, buttonFolder));
                }
                retValue.append("</tr></table>");
                retValue.append("</td>\n");                
                retValue.append("</tr>\n");
            }
            
            
            
        } catch (CmsException e) {
            // TODO: show exception
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
            m_elementList = new ArrayList();
            // read the property from the template file
            String elements = getCms().readProperty(getParamPagetemplate(), "template-elements", false, null);
            if (elements == null) {
                // no elements defined on template file, don't create list
                return null;
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
                m_elementList.add(new String[] {currentElement, niceName, mandatory});
            }
        }
        return m_elementList;
    }
    
    public void showException(CmsException exc) throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        // show error dialog
        setParamErrorstack(exc.getStackTraceAsString());
        setParamMessage(key("error.message." + getParamDialogtype()));
        String reason = key("error.reason.") + "<br>\n" + key("error.suggestion." ) + "\n";
        setParamReasonSuggestion(reason);
        getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
    }

}
