/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsTabDialog.java,v $
 * Date   : $Date: 2004/06/28 11:18:10 $
 * Version: $Revision: 1.12 $
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
package org.opencms.workplace;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for tab styled dialogs.<p> 
 * 
 * Extend this class in order to create a tab styled dialog and provide a method
 * getTabs() in the new dialog class which should return a list of localized Strings
 * which represent the tabs of the dialog.<p> 
 * 
 * This class is used for the following dialogs:
 * <ul>
 * <li>User preferences (CmsPreferences.java)
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.12 $
 * 
 * @since 5.1.12
 */
public abstract class CmsTabDialog extends CmsDialog {

    /** Name of the request parameter for the set button pressed flag. */
    public static final String PARAM_SETPRESSED = "setpressed";
    /** Name of the request parameter for the current tab. */
    public static final String PARAM_TAB = "tab";
    
    /** Value for the action: switch the tab. */
    public static final int ACTION_SWITCHTAB = 100; 
    
    /** Request parameter value for the action: switch the tab. */
    public static final String DIALOG_SWITCHTAB = "switchtab"; 
    
    /** Stores the current tab. */
    private String m_paramTab;
    /** Determines if the "set" button was pressed. */
    private String m_paramSetPressed;
    
    /** Stores the currently active tab. */
    private int m_activeTab = -1;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTabDialog(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTabDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }   
    
    /**
     * Returns the value of the setpressed parameter.<p>
     * 
     * @return the value of the setpressed parameter
     */    
    public String getParamSetPressed() {
        return m_paramSetPressed;
    }

    /**
     * Sets the value of the setpressed parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamSetPressed(String value) {
        m_paramSetPressed = value;
    }
    
    
    /**
     * Returns the value of the tab parameter.<p>
     * 
     * @return the value of the tab parameter
     */    
    public String getParamTab() {
        return m_paramTab;
    }

    /**
     * Sets the value of the tab parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamTab(String value) {
        m_paramTab = value;
    }
    
    /**
     * Returns a list with localized Strings representing the names of the tabs.<p>
     * 
     * @return list with localized String for the tabs
     */
    public abstract List getTabs();
    
    /**
     * Returns the order of the parameter prefixes for each tab.<p>
     * 
     * For example, all parameters stored in tab 1 have the prefix "Tab1", i.e.
     * the getter and setter methods must be getParam<b>Tab1</b>MyParameterName().<p>
     * 
     * To change the tab order, simply change the order in the String array 
     * and in the generated tab list.<p> 
     * 
     * @return the ordered parameter prefix List
     * @see org.opencms.workplace.CmsTabDialog#getTabs()
     */
    public abstract List getTabParameterOrder();
    
    /**
     * Returns the number of the currently active tab depending on the request parameter.<p>
     * 
     * This method has to be called once in initWorkplaceRequestValues after filling the request parameters.<p>
     * 
     * @return the number of the currently active tab
     */
    public int getActiveTab() {
        if (m_activeTab < 0) {
            String paramTab = getParamTab();
            int tab = 1;
            if (paramTab != null && !"".equals(paramTab)) {
                try {
                    tab = Integer.parseInt(paramTab);
                } catch (NumberFormatException e) {
                    // do nothing, the first tab is returned
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info(e);
                    }                    
                }
            }
            setParamTab("" + tab);
            m_activeTab = tab;
            return tab;
        } else {
            return m_activeTab;
        }
    }
    
    /**
     * Returns the localized name of the currently active tab.<p>
     * 
     * @return the localized name of the currently active tab or null if no tab name was found
     */
    public String getActiveTabName() {
        if (m_activeTab < 0) {
            getActiveTab();    
        }
        List tabNames = getTabs();
        try {
            return (String)tabNames.get(m_activeTab -1);
        } catch (IndexOutOfBoundsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
            return null;
        }
    }
    
    /**
     * Returns the start html for the tab content area of the dialog window.<p>
     * 
     * @param title the title for the dialog
     * @return the start html for the tab content area of the dialog window
     */                
    public String dialogTabContentStart(String title) {
        return dialogTabContent(HTML_START, title, null);
    }
    
    /**
     * Returns the start html for the tab content area of the dialog window.<p>
     * 
     * @param title the title for the dialog
     * @param attributes additional attributes for the content &lt;div&gt; area of the tab dialog
     * @return the start html for the tab content area of the dialog window
     */                
    public String dialogTabContentStart(String title, String attributes) {
        return dialogTabContent(HTML_START, title, attributes);
    }

    /**
     * Returns the end html for the tab content area of the dialog window.<p>
     * 
     * @return the end html for the tab content area of the dialog window
     */                
    public String dialogTabContentEnd() {
        return dialogTabContent(HTML_END, null, null);
    }
    
    /**
     * Builds the tab content area of the dialog window.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param title the title String for the dialog window
     * @param attributes additional attributes for the content &lt;div&gt; area of the tab dialog
     * @return a tab content area start / end segment
     */
    public String dialogTabContent(int segment, String title, String attributes) {
        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            // null title is ok, we always want the title headline
            result.append(dialogHead(title));
            result.append("<div class=\"dialogtabstart\" unselectable=\"on\">\n");
            result.append("<!-- dialogtabs start -->\n");
            result.append(dialogTabRow());
            result.append("<div class=\"dialogtabcontent\"");
            if (attributes != null) {
                result.append(" " + attributes);
            }
            result.append(">\n");
            result.append("<!-- dialogcontent start -->\n");
            return result.toString();
        } else {
            return "\n<!-- dialogcontent end --></div>\n<!-- dialogtabs end --></div>";
        }
    }
    
    /**
     * Builds the html for the tab row of the tab dialog.<p>
     * 
     * @return the html for the tab row
     */
    public String dialogTabRow() {
        StringBuffer result = new StringBuffer(512);
        StringBuffer lineRow = new StringBuffer(256);
        List tabNames = getTabs();
        if (tabNames.size() < 2) {
            // less than 2 tabs present, do not show them and create a border line
            result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\" style=\"empty-cells: show;\">\n");
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogtabrow\"></td>\n");
            result.append("</tr>\n");
            result.append("</table>\n");
            return result.toString();    
        }
        Iterator i = tabNames.iterator();
        int counter = 1;
        int activeTab = getActiveTab();
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\" style=\"empty-cells: show;\">\n");
        result.append("<tr>\n");
        while (i.hasNext()) {
            // build a tab entry
            String curTab = (String)i.next();
            String curTabLink = "javascript:openTab('" + counter + "');";
            if (counter == activeTab) {
                // create the currently active tab
                int addDelta = 0;
                result.append("\t<td class=\"dialogtabactive\"");
                if (counter == 1) {
                    // for first tab, add special html for correct layout
                    result.append(" style=\"border-left-width: 1px;\"");
                    addDelta = 1;
                }
                result.append(">");
                result.append("<span class=\"tabactive\" unselectable=\"on\"");
                result.append(" style=\"width: " + (curTab.length() * 8 + addDelta) + "px;\"");
                result.append(">");
                result.append(curTab);
                result.append("</span></td>\n");
                lineRow.append("\t<td></td>\n");
            } else {
                // create an inactive tab
                result.append("\t<td class=\"dialogtab\" unselectable=\"on\">");
                result.append("<a class=\"tab\" href=\"" + curTabLink + "\"");
                result.append(" style=\"width: " + (curTab.length() * 8) + "px;\"");
                result.append(">");
                result.append(curTab);
                result.append("</a></td>\n");
                lineRow.append("\t<td class=\"dialogtabrow\"></td>\n");
            }       
            
            counter++;
        }
        result.append("\t<td class=\"maxwidth\"></td>\n");
        result.append("</tr>\n");
        result.append("<tr>\n");
        result.append(lineRow);
        result.append("\t<td class=\"dialogtabrow\"></td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
        return result.toString();
    }
    
    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param helpUrl the key for the online help to include on the page
     * @return the start html of the page
     */
    public String htmlStart(String helpUrl) { 
        String stylesheet = null;
        if ("true".equals(getParamIsPopup())) {
            stylesheet = "files/css_popup.css";
        }
        StringBuffer result = new StringBuffer(super.pageHtmlStyle(HTML_START, null, stylesheet));
        if (getSettings().isViewExplorer()) {
            result.append("<script type=\"text/javascript\" src=\"");
            result.append(getSkinUri());
            result.append("files/explorer.js\"></script>\n");
        }
        result.append("<script type=\"text/javascript\">\n");
        if (helpUrl != null) {          
            result.append("top.head.helpUrl=\"");
            result.append(helpUrl + "\";\n");
            
        }
        // js to switch the dialog tabs
        result.append("function openTab(tabValue) {\n");
        result.append("\tdocument.forms[0]." + PARAM_TAB + ".value = tabValue;\n");
        result.append("\tdocument.forms[0]." + PARAM_ACTION + ".value = \"" + DIALOG_SWITCHTAB + "\";\n");
        result.append("\tdocument.forms[0].submit();\n");
        result.append("}\n");
        // js for the button actions, overwrites CmsDialog.dialogScriptSubmit() js method
        result.append("function submitAction(actionValue, theForm, formName) {\n");
        result.append("\tif (theForm == null) {\n");
        result.append("\t\ttheForm = document.forms[formName];\n");
        result.append("\t}\n");        
        result.append("\ttheForm." + PARAM_FRAMENAME + ".value = window.name;\n");
        result.append("\tif (actionValue == \"" + DIALOG_SET + "\") {\n");
        result.append("\t\ttheForm." + PARAM_ACTION + ".value = \"" + DIALOG_SET + "\";\n");
        result.append("\t} else if (actionValue == \"" + DIALOG_CANCEL + "\") {\n");
        result.append("\t\ttheForm." + PARAM_ACTION + ".value = \"" + DIALOG_CANCEL + "\";\n");
        result.append("\t}\n");
        result.append("\ttheForm.submit();\n");
        result.append("\treturn false;\n");
        result.append("}\n");
        result.append("//-->\n</script>\n");
        return result.toString();
    }

    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @return the start html of the page
     */
    public String htmlStart() {
        return htmlStart(null);
    }
    
    /**
     * Returns all initialized parameters of the current workplace class 
     * as hidden field tags that can be inserted in a form.<p>
     * 
     * This overwrites the method in CmsWorkplace because for each tab, 
     * only the hidden parameters of the non displayed tabs are added.<p> 
     * 
     * @return all initialized parameters of the current workplace class
     * as hidden field tags that can be inserted in a html form
     */
    public String paramsAsHidden() {
        StringBuffer result = new StringBuffer(512);
        String activeTab = (String)getTabParameterOrder().get(getActiveTab() - 1);
        Map params = paramValues();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            if (!param.startsWith(activeTab)) {
                // add only parameters which are not displayed in currently active tab
                Object value = params.get(param);
                result.append("<input type=\"hidden\" name=\"");
                result.append(param);
                result.append("\" value=\"");
                result.append(CmsEncoder.encode(value.toString(), getCms().getRequestContext().getEncoding()));
                result.append("\">\n");
            }
        }        
        return result.toString();
    }
    
}
