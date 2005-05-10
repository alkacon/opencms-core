/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWidgetDialog.java,v $
 * Date   : $Date: 2005/05/10 09:24:02 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.xml.CmsXmlException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Base class for dialogs that use the OpenCms widgets without XML content.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.9.1
 */
public abstract class CmsWidgetDialog extends CmsDialog implements I_CmsWidgetDialog {

    /** Action for optional element creation. */
    public static final int ACTION_ELEMENT_ADD = 152;

    /** Action for optional element removal. */
    public static final int ACTION_ELEMENT_REMOVE = 153;

    /** Indicates an optional element should be created. */
    public static final String EDITOR_ACTION_ELEMENT_ADD = "addelement";

    /** Indicates an optional element should be removed. */
    public static final String EDITOR_ACTION_ELEMENT_REMOVE = "removeelement";

    /** Contains all parameter value of this dialog. */
    protected Map m_paramValues;

    /** The list of widgets used on the dialog. */
    protected List m_widgets;

    /** 
     * Parameter stores the index of the element to add or remove.<p>
     * 
     * This must not be <code>null</code>, because it must appear 
     * when calling <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.<p>
     */
    private String m_paramElementIndex = "0";
    /** 
     * Parameter stores the name of the element to add or remove.<p>
     * 
     * This must not be <code>null</code>, because it must appear 
     * when calling <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.<p>
     */
    private String m_paramElementName = "undefined";
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsWidgetDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsWidgetDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Adds an optional element to the xml content or removes an optional element from the xml content.<p>
     * 
     * Depends on the given action value.<p>
     */
    public void actionToggleElement() {

        // get the necessary parameters to add/remove the element
        int index = 0;
        try {
            index = Integer.parseInt(getParamElementIndex());
        } catch (Exception e) {
            // ignore, should not happen
        }
        String name = getParamElementName();
        // get the base parameter definition
        CmsWidgetParameter base = getParameterDefinition(name);
        if (base != null) {
            // the requested parameter is valid for this dialog
            List params = (List)getParameters().get(name);
            if (getAction() == ACTION_ELEMENT_REMOVE) {
                // remove the value
                params.remove(index);
            } else {
                // add the new value after the clicked element
                index = index + 1;
                CmsWidgetParameter newParam = new CmsWidgetParameter(base, base.getDefault(getCms()), index);
                params.add(index, newParam);
            }
            // reset all index value in the parameter list
            for (int i = 0; i < params.size(); i++) {
                CmsWidgetParameter param = (CmsWidgetParameter)params.get(i);
                param.setindex(i);
            }
        }
    }

    /**
     * Returns the html for a button to add an optional element.<p>
     * 
     * @param elementName name of the element
     * @param insertAfter the index of the element after which the new element should be created
     * @param enabled if true, the button to add an element is shown, otherwise a spacer is returned
     * @return the html for a button to add an optional element
     */
    public String buildAddElement(String elementName, int insertAfter, boolean enabled) {

        if (enabled) {
            StringBuffer href = new StringBuffer(4);
            href.append("javascript:addElement('");
            href.append(elementName);
            href.append("', ");
            href.append(insertAfter);
            href.append(");");
            return button(href.toString(), null, "new", "button.addnew", 0);
        } else {
            return dialogHorizontalSpacer(22);
        }
    }

    /**
     * Returns the html for a button to remove an optional element.<p>
     * 
     * @param elementName name of the element
     * @param index the element index of the element to remove
     * @param enabled if true, the button to remove an element is shown, otherwise a spacer is returned
     * @return the html for a button to remove an optional element
     */
    public String buildRemoveElement(String elementName, int index, boolean enabled) {

        if (enabled) {
            StringBuffer href = new StringBuffer(4);
            href.append("javascript:removeElement('");
            href.append(elementName);
            href.append("', ");
            href.append(index);
            href.append(");");
            return button(href.toString(), null, "deletecontent", "button.delete", 0);
        } else {
            return dialogHorizontalSpacer(22);
        }
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog#getButtonStyle()
     */
    public int getButtonStyle() {

        return getSettings().getUserSettings().getEditorButtonStyle();
    }

    /**
     * Returns the index of the element to add or remove.<p>
     *
     * @return the index of the element to add or remove
     */
    public String getParamElementIndex() {

        return m_paramElementIndex;
    }

    /**
     * Returns the name of the element to add or remove.<p>
     *
     * @return the name of the element to add or remove
     */
    public String getParamElementName() {

        return m_paramElementName;
    }

    /**
     * Returns the value of the widget parameter with the given name, or <code>null</code>
     * if no such widget parameter is available.<p>
     * 
     * @param name the widget parameter name to get the value for
     * 
     * @return the value of the widget parameter with the given name
     */
    public String getParamValue(String name) {

        return getParamValue(name, 0);
    }

    /**
     * Returns the value of the widget parameter with the given name and index, or <code>null</code>
     * if no such widget parameter is available.<p>
     * 
     * @param name the widget parameter name to get the value for
     * @param index the widget parameter index
     * 
     * @return the value of the widget parameter with the given name and index
     */
    public String getParamValue(String name, int index) {

        List params = (List)m_paramValues.get(name);
        if (params != null) {
            if ((index >= 0) && (index < params.size())) {
                CmsWidgetParameter param = (CmsWidgetParameter)params.get(index);
                if (param.getId().equals(CmsWidgetParameter.createId(name, index))) {
                    return param.getStringValue(getCms());
                }
            }
        }

        return null;
    }

    /**
     * Generates the HTML for the end of the widget dialog.<p>
     * 
     * This HTML includes additional components, for example the &lt;div&gt;
     * tags containing the help texts.<p>
     * 
     * @return the HTML for the end of the widget dialog
     * 
     * @throws JspException if an error occurs during HTML generation
     */
    public String getWidgetHtmlEnd() throws JspException {

        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgets().iterator();
            while (i.hasNext()) {
                CmsWidgetParameter param = (CmsWidgetParameter)i.next();
                I_CmsXmlWidget widget = param.getWidget();
                result.append(widget.getDialogHtmlEnd(getCms(), this, param));
            }
        } catch (CmsXmlException e) {

            // TODO: Exception handling!
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Generates the HTML include tags for external JavaScripts files of the used widgets.<p>
     * 
     * @return the HTML include tags for external JavaScripts files of the used widgets
     * 
     * @throws JspException if an error occurs during JavaScript generation
     */
    public String getWidgetIncludes() throws JspException {

        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgets().iterator();
            Set set = new HashSet();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = ((CmsWidgetParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogIncludes(getCms(), this));
                    result.append('\n');
                    set.add(widget);
                }
            }
        } catch (CmsXmlException e) {

            // TODO: Exception handling!            
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Generates the JavaScript init calls for the used widgets.<p>
     * 
     * @return the JavaScript init calls for the used widgets
     * 
     * @throws JspException the JavaScript init calls for the used widgets
     */
    public String getWidgetInitCalls() throws JspException {

        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgets().iterator();
            Set set = new HashSet();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = ((CmsWidgetParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogInitCall(getCms(), this));
                    set.add(widget);
                }
            }
        } catch (CmsXmlException e) {

            // TODO: Exception handling!            
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Generates the JavaScript initialization methods for the used widgets.<p>
     * 
     * @return the JavaScript initialization methods for the used widgets
     * 
     * @throws JspException if an error occurs during JavaScript generation
     */
    public String getWidgetInitMethods() throws JspException {

        StringBuffer result = new StringBuffer(32);
        try {
            // iterate over unique widgets from collector
            Iterator i = getWidgets().iterator();
            Set set = new HashSet();
            while (i.hasNext()) {
                I_CmsXmlWidget widget = ((CmsWidgetParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogInitMethod(getCms(), this));
                    set.add(widget);
                }
            }
        } catch (CmsXmlException e) {

            // TODO: Exception handling!            
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Sets the index of the element to add or remove.<p>
     *
     * @param elementIndex the index of the element to add or remove
     */
    public void setParamElementIndex(String elementIndex) {

        m_paramElementIndex = elementIndex;
    }

    /**
     * Sets the name of the element to add or remove.<p>
     *
     * @param elementName the name of the element to add or remove
     */
    public void setParamElementName(String elementName) {

        m_paramElementName = elementName;
    }

    /**
     * Returns the values of all widget parameters of this dialog as HTML hidden fields.<p>
     * 
     * @return the values of all widget parameters of this dialog as HTML hidden fields
     * 
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    public String widgetParamsAsHidden() {

        return widgetParamsAsHidden(null);
    }

    /**
     * Returns the values of all widget parameters of this dialog as HTML hidden fields,
     * excluding the widget values that are on the given dialog page.<p>
     * 
     * This can be used to create multi-page dialogs where the values are passed from
     * one page to another before everyting is submitted. If a widget A is used on page X,
     * there should be no "hidden" HTML field for A since otherwise A would have 2 values when 
     * submitting the dialog page: The one from the widget itself and the one from the hidden 
     * field. This may lead to undefined results when processing the submitted values.<p>
     * 
     * @param excludeDialogPage the dialog page to exclude the values for
     * 
     * @return the values of all widget parameters of this dialog as HTML hidden fields,
     *      excluding the widget values that are on the given dialog page
     * 
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    public String widgetParamsAsHidden(String excludeDialogPage) {

        StringBuffer result = new StringBuffer();
        Iterator i = m_paramValues.keySet().iterator();
        while (i.hasNext()) {
            List params = (List)m_paramValues.get(i.next());
            Iterator j = params.iterator();
            while (j.hasNext()) {
                CmsWidgetParameter param = (CmsWidgetParameter)j.next();
                String value = param.getStringValue(getCms());
                if (CmsStringUtil.isNotEmpty(value)
                    && ((excludeDialogPage == null) || (!param.getDialog().equals(excludeDialogPage)))) {
                    result.append("<input type=\"hidden\" name=\"");
                    result.append(param.getId());
                    result.append("\" value=\"");
                    String encoded = CmsEncoder.encode(value, getCms().getRequestContext().getEncoding());
                    result.append(encoded);
                    result.append("\">\n");
                }
            }
        }
        return result.toString();
    }

    /**
     * Adds a new widget parameter definition to the list of all widgets of this dialog.<p>
     * 
     * @param param the widget parameter definition to add
     */
    protected void addWidget(CmsWidgetParameter param) {

        if (m_widgets == null) {
            m_widgets = new ArrayList();
        }

        m_widgets.add(param);
    }

    /**
     * Defines the list of parameters for this dialog.<p>
     */
    protected abstract void defineWidgets();

    /**
     * Fills all widgets of this widget dialog with the values from the request parameters.<p>
     * 
     * @param request the current HTTP servlet request
     */
    protected void fillWidgetValues(HttpServletRequest request) {

        Iterator i = getWidgets().iterator();
        Map parameters = request.getParameterMap();
        m_paramValues = new HashMap();

        while (i.hasNext()) {
            // check for all widget parameters            
            CmsWidgetParameter base = (CmsWidgetParameter)i.next();
            List params = new ArrayList();
            int count = 0;
            for (int j = 0; j < base.getMaxOccurs(); j++) {
                String id = CmsWidgetParameter.createId(base.getName(), j);
                String[] values = (String[])parameters.get(id);
                String value = null;
                if ((values != null) && (values.length > 0)) {
                    // found a value for this parameter
                    value = decodeParamValue(base.getName(), values[0]);
                } else {
                    if (count < base.getMinOccurs()) {
                        // no value found but still required - use default
                        value = base.getDefault(getCms());
                    }
                }
                if (value != null) {
                    CmsWidgetParameter param = new CmsWidgetParameter(base, value, count);
                    params.add(param);
                    count++;
                }
            }
            m_paramValues.put(base.getName(), params);
        }
    }

    /**
     * Returns the parameter widget definition for the given parameter name.<p>
     * 
     * @param name the parameter name to get the definition for 
     * 
     * @return the parameter widget definition for the given parameter name
     */
    protected CmsWidgetParameter getParameterDefinition(String name) {

        Iterator i = getWidgets().iterator();
        while (i.hasNext()) {
            // check for all widget parameters            
            CmsWidgetParameter base = (CmsWidgetParameter)i.next();
            if (base.getName().equals(name)) {
                return base;
            }
        }
        return null;
    }

    /**
     * Returns the map with the widget parameter values.<p> 
     * 
     * @return the map with the widget parameter values
     */
    protected Map getParameters() {

        return m_paramValues;
    }

    /**
     * Returns the widget HTML code for the given parameter.<p> 
     * 
     * @param param the name (id) of the parameter to get the widget HTML for
     * 
     * @return the widget HTML code for the given parameter
     * 
     * @throws CmsXmlException if the widget HTML could not be generated
     */
    protected String getWidget(CmsWidgetParameter param) throws CmsXmlException {

        if (param != null) {
            return param.getWidget().getDialogWidget(getCms(), this, param);
        }
        return null;
    }

    /**
     * Returns the list of all widgets used on this widget dialog, the 
     * List must contain Objects of type <code>{@link CmsWidgetParameter}</code>.<p>
     * 
     * @return the list of all widgets used on this widget dialog
     */
    protected List getWidgets() {

        return m_widgets;
    }
}
