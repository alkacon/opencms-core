/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWidgetDialog.java,v $
 * Date   : $Date: 2005/06/19 10:57:05 $
 * Version: $Revision: 1.47 $
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
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Base class for dialogs that use the OpenCms widgets without XML content.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.47 $
 * @since 5.9.1
 */
public abstract class CmsWidgetDialog extends CmsDialog implements I_CmsWidgetDialog {

    /** Action for optional element creation. */
    public static final int ACTION_ELEMENT_ADD = 152;

    /** Action for optional element removal. */
    public static final int ACTION_ELEMENT_REMOVE = 153;

    /** Value for the action: error in the form validation. */
    public static final int ACTION_ERROR = 303;

    /** Value for the action: save the dialog. */
    public static final int ACTION_SAVE = 300;

    /** Request parameter value for the action: save the dialog. */
    public static final String DIALOG_SAVE = "save";

    /** Indicates an optional element should be created. */
    public static final String EDITOR_ACTION_ELEMENT_ADD = "addelement";

    /** Indicates an optional element should be removed. */
    public static final String EDITOR_ACTION_ELEMENT_REMOVE = "removeelement";

    /** Prefix for "hidden" parameters, required since these must be unescaped later. */
    public static final String HIDDEN_PARAM_PREFIX = "hidden.";

    /** The errors thrown by commit actions. */
    protected List m_commitErrors;

    /** The object edited with this widget dialog. */
    protected Object m_dialogObject;

    /** The allowed pages for this dialog in a List. */
    protected List m_pages;

    /** Controls which page is currently displayed in the dialog. */
    protected String m_paramPage;

    /** The validation errors for the input form. */
    protected List m_validationErrorList;

    /** Contains all parameter value of this dialog. */
    protected Map m_widgetParamValues;

    /** The list of widgets used on the dialog. */
    protected List m_widgets;

    /** 
     * Parameter stores the index of the element to add or remove.<p>
     * 
     * This must not be <code>null</code>, because it must be available 
     * when calling <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.<p>
     */
    private String m_paramElementIndex = "0";

    /** 
     * Parameter stores the name of the element to add or remove.<p>
     * 
     * This must not be <code>null</code>, because it must be available
     * when calling <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.<p>
     */
    private String m_paramElementName = "undefined";

    /** Optional localized key prefix identificator. */
    private String m_prefix;

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
     * Deletes the edited dialog object from the session.<p>
     */
    public void actionCancel() {

        clearDialogObject();
    }

    /**
     * Commits the edited object after pressing the "OK" button.<p>
     * 
     * @throws IOException in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public abstract void actionCommit() throws IOException, ServletException;

    /**
     * Adds or removes an optional element.<p>
     * 
     * Depends on the value stored in the <code>{@link CmsDialog#getAction()}</code> method.<p>
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
        CmsWidgetDialogParameter base = getParameterDefinition(name);
        if (base != null) {
            // the requested parameter is valid for this dialog
            List params = (List)getParameters().get(name);
            if (getAction() == ACTION_ELEMENT_REMOVE) {
                // remove the value
                params.remove(index);
            } else {
                List sequence = (List)getParameters().get(base.getName());
                if (sequence.size() > 0) {
                    // add the new value after the clicked element
                    index = index + 1;
                }
                CmsWidgetDialogParameter newParam = new CmsWidgetDialogParameter(base, index);
                params.add(index, newParam);
            }
            // reset all index value in the parameter list
            for (int i = 0; i < params.size(); i++) {
                CmsWidgetDialogParameter param = (CmsWidgetDialogParameter)params.get(i);
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
            return button(href.toString(), null, "new.png", Messages.GUI_DIALOG_BUTTON_ADDNEW_0, 0);
        } else {
            return "";
        }
    }

    /**
     * Builds the HTML for the dialog form.<p>
     * 
     * @return the HTML for the dialog form
     */
    public String buildDialogForm() {

        // create the dialog HTML
        return createDialogHtml(getParamPage());
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
            return button(href.toString(), null, "deletecontent.png", Messages.GUI_DIALOG_BUTTON_DELETE_0, 0);
        } else {
            return "";
        }
    }

    /**
     * Clears the "dialog object" for this widget dialog by removing it from the current users session.<p>
     */
    public void clearDialogObject() {

        setDialogObject(null);
    }

    /**
     * Builds the end HTML for a block with 3D border in the dialog content area.<p>
     * 
     * @return 3D block start / end segment
     */
    public String dialogBlockEnd() {

        StringBuffer result = new StringBuffer(8);
        result.append(super.dialogBlockEnd());
        result.append(dialogSpacer());
        result.append("</td></tr>\n");
        return result.toString();
    }

    /**
     * Builds the start HTML for a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param headline the headline String for the block
     * @return 3D block start / end segment
     */
    public String dialogBlockStart(String headline) {

        StringBuffer result = new StringBuffer(8);
        result.append("<tr><td colspan=\"5\">\n");
        result.append(super.dialogBlockStart(headline));
        return result.toString();
    }

    /**
     * Creates the HTML for the buttons on the dialog.<p>
     * 
     * @return the HTML for the buttons on the dialog.<p>
     */
    public String dialogButtonsCustom() {

        if (getPages().size() > 1) {
            // this is a multi page dialog, create buttons according to current page
            int pageIndex = getPages().indexOf(getParamPage());
            if (pageIndex == getPages().size() - 1) {
                // this is the last dialog page
                return dialogButtons(new int[] {BUTTON_OK, BUTTON_BACK, BUTTON_CANCEL}, new String[3]);
            } else if (pageIndex > 0) {
                // this is a dialog page between first and last page
                return dialogButtons(new int[] {BUTTON_BACK, BUTTON_CONTINUE, BUTTON_CANCEL}, new String[3]);
            } else {
                // this is the first dialog page
                return dialogButtons(new int[] {BUTTON_CONTINUE, BUTTON_CANCEL}, new String[2]);
            }
        }
        boolean onlyDisplay = true;
        Iterator it = m_widgets.iterator();
        while (it.hasNext()) {
            CmsWidgetDialogParameter wdp = (CmsWidgetDialogParameter)it.next();
            if (!(wdp.getWidget() instanceof CmsDisplayWidget)) {
                onlyDisplay = false;
                break;
            }
        }
        if (!onlyDisplay) {
            // this is a single page dialog, create common buttons
            return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[2]);
        }
        // this is a display only dialog
        return "";
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public void displayDialog() throws JspException, IOException, ServletException {

        switch (getAction()) {

            case ACTION_CANCEL:
                // ACTION: cancel button pressed
                actionCancel();
                actionCloseDialog();
                break;

            case ACTION_ERROR:
                // ACTION: an error occured (display nothing)
                break;

            case ACTION_SAVE:
                // ACTION: save edited values
                setParamAction(DIALOG_OK);
                actionCommit();
                if (closeDialogOnCommit()) {
                    actionCloseDialog();
                    break;
                }

            case ACTION_DEFAULT:
            default:
                // ACTION: show dialog (default)
                setParamAction(DIALOG_SAVE);
                JspWriter out = getJsp().getJspContext().getOut();
                out.print(defaultActionHtml());
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetDialog#getButtonStyle()
     */
    public int getButtonStyle() {

        return getSettings().getUserSettings().getEditorButtonStyle();
    }

    /**
     * Returns the errors that are thrown by save actions or form generation.<p>
     * 
     * @return the errors that are thrown by save actions or form generation
     */
    public List getCommitErrors() {

        return m_commitErrors;
    }

    /**
     * Returns the dialog object for this widget dialog, or <code>null</code>
     * if no dialog object has been set.<p>
     * 
     * @return the dialog object for this widget dialog, or <code>null</code>
     */
    public Object getDialogObject() {

        if (m_dialogObject == null) {
            m_dialogObject = getDialogObjectMap().get(getClass().getName());
        }
        return m_dialogObject;
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
     * Returns the page parameter.<p>
     *
     * @return the page parameter
     */
    public String getParamPage() {

        return m_paramPage;
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

        List params = (List)m_widgetParamValues.get(name);
        if (params != null) {
            if ((index >= 0) && (index < params.size())) {
                CmsWidgetDialogParameter param = (CmsWidgetDialogParameter)params.get(index);
                if (param.getId().equals(CmsWidgetDialogParameter.createId(name, index))) {
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
     */
    public String getWidgetHtmlEnd() {

        StringBuffer result = new StringBuffer(32);
        // iterate over unique widgets from collector
        Iterator i = getWidgets().iterator();
        while (i.hasNext()) {
            CmsWidgetDialogParameter param = (CmsWidgetDialogParameter)i.next();
            result.append(param.getWidget().getDialogHtmlEnd(getCms(), this, param));
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
                I_CmsWidget widget = ((CmsWidgetDialogParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogIncludes(getCms(), this));
                    result.append('\n');
                    set.add(widget);
                }
            }
        } catch (Throwable e) {

            includeErrorpage(this, e);
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
                I_CmsWidget widget = ((CmsWidgetDialogParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogInitCall(getCms(), this));
                    set.add(widget);
                }
            }
        } catch (Throwable e) {
            includeErrorpage(this, e);
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
                I_CmsWidget widget = ((CmsWidgetDialogParameter)i.next()).getWidget();
                if (!set.contains(widget)) {
                    result.append(widget.getDialogInitMethod(getCms(), this));
                    set.add(widget);
                }
            }
        } catch (Throwable e) {
            includeErrorpage(this, e);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    public String paramsAsHidden() {

        if (getAction() != ACTION_ERROR) {
            return super.paramsAsHidden();
        }
        // on an error page, also output the widget parameters
        StringBuffer result = new StringBuffer();
        result.append(super.paramsAsHidden());
        result.append('\n');
        result.append(widgetParamsAsHidden());
        return result.toString();
    }

    /**
     * Stores the given object as "dialog object" for this widget dialog in the current users session.<p> 
     * 
     * @param dialogObject the object to store
     */
    public void setDialogObject(Object dialogObject) {

        m_dialogObject = dialogObject;
        if (dialogObject == null) {
            // null object: remove the entry from the map
            getDialogObjectMap().remove(getClass().getName());
        } else {
            getDialogObjectMap().put(getClass().getName(), dialogObject);
        }
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
     * Sets the page parameter.<p>
     *
     * @param paramPage the page parameter to set
     */
    public void setParamPage(String paramPage) {

        m_paramPage = paramPage;
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
        Iterator i = m_widgetParamValues.keySet().iterator();
        while (i.hasNext()) {
            List params = (List)m_widgetParamValues.get(i.next());
            Iterator j = params.iterator();
            while (j.hasNext()) {
                CmsWidgetDialogParameter param = (CmsWidgetDialogParameter)j.next();
                String value = param.getStringValue(getCms());
                if (CmsStringUtil.isNotEmpty(value)
                    && ((excludeDialogPage == null) || (!param.getDialogPage().equals(excludeDialogPage)))) {
                    result.append("<input type=\"hidden\" name=\"");
                    result.append(HIDDEN_PARAM_PREFIX);
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
     * Adds the given error to the list of errors that are thrown by save actions or form generation.<p>
     * 
     * If the error list has not been initilaized yet, this is done automatically.<p>
     * 
     * @param error the errors to add
     */
    protected void addCommitError(Exception error) {

        if (m_commitErrors == null) {
            m_commitErrors = new ArrayList();
        }
        m_commitErrors.add(error);
    }

    /**
     * Adds a new widget parameter definition to the list of all widgets of this dialog.<p>
     * 
     * @param param the widget parameter definition to add
     */
    protected void addWidget(CmsWidgetDialogParameter param) {

        if (m_widgets == null) {
            m_widgets = new ArrayList();
        }
        param.setKeyPrefix(m_prefix);
        m_widgets.add(param);
    }

    /**
     * Returns <code>true</code> if the dialog should be closed after the values have been committed.<p>
     * 
     * The default implementation returns <code>true</code> in case there are no 
     * commit errors.<p>
     * 
     * @return <code>true</code> if the dialog should be closed after the values have been committed
     */
    protected boolean closeDialogOnCommit() {

        return !hasCommitErrors();
    }

    /**
     * Commits all values on the dialog.<p> 
     * 
     * @return a List of all Exceptions that occured when comitting the dialog.<p>
     */
    protected List commitWidgetValues() {

        return commitWidgetValues(null);
    }

    /**
     * Commits all values on the given dialog page.<p> 
     * 
     * @param dialogPage the dialog (page) to commit
     * 
     * @return a List of all Exceptions that occured when comitting the dialog page.<p>
     */
    protected List commitWidgetValues(String dialogPage) {

        List result = new ArrayList();
        Iterator i = getWidgets().iterator();
        while (i.hasNext()) {
            // check for all widget parameters            
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();
            if ((dialogPage == null) || (base.getDialogPage() == null) || dialogPage.equals(base.getDialogPage())) {
                // the parameter is located on the requested dialog
                base.prepareCommit();
                List params = (List)m_widgetParamValues.get(base.getName());
                Iterator j = params.iterator();
                while (j.hasNext()) {
                    CmsWidgetDialogParameter param = (CmsWidgetDialogParameter)j.next();
                    try {
                        param.commitValue(this);
                    } catch (Exception e) {
                        result.add(e);
                    }
                }
            }
        }
        setValidationErrorList(result);
        return result;
    }

    /**
     * Creates the dialog HTML for all defined widgets of this dialog.<p>  
     * 
     * @return the dialog HTML for all defined widgets of this dialog
     */
    protected String createDialogHtml() {

        return createDialogHtml(null);
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * To get a more complex layout variation, you have to overwrite this method in your dialog class.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        Iterator i = getWidgets().iterator();
        // iterate the type sequence                    
        while (i.hasNext()) {
            // get the current widget base definition
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();
            // check if the element is on the requested dialog page
            if ((dialog == null) || dialog.equals(base.getDialogPage())) {
                // add the HTML for the dialog element
                result.append(createDialogRowHtml(base));
            }
        }
        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the dialog HTML for all occurences of one widget parameter.<p>  
     * 
     * @param base the widget parameter base
     * @return the dialog HTML for one widget parameter
     */
    protected String createDialogRowHtml(CmsWidgetDialogParameter base) {

        StringBuffer result = new StringBuffer(256);

        List sequence = (List)getParameters().get(base.getName());
        int count = sequence.size();

        // check if value is optional or multiple
        boolean addValue = false;
        if (count < base.getMaxOccurs()) {
            addValue = true;
        }
        boolean removeValue = false;
        if (count > base.getMinOccurs()) {
            removeValue = true;
        }

        // check if value is present
        boolean disabledElement = false;
        if (count < 1) {
            // no parameter with the value present, but also not optional: use base as parameter
            sequence = new ArrayList();
            sequence.add(base);
            count = 1;
            if (base.getMinOccurs() == 0) {
                disabledElement = true;
            }
        }

        // loop through multiple elements
        for (int j = 0; j < count; j++) {

            // get the parameter and the widget
            CmsWidgetDialogParameter p = (CmsWidgetDialogParameter)sequence.get(j);
            I_CmsWidget widget = p.getWidget();

            // check for an error in this row
            if (p.hasError()) {
                // show error message
                result.append("<tr><td></td><td><img src=\"");
                result.append(getSkinUri()).append("editors/xmlcontent/");
                result.append("error.png");
                result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdError\">");
                Throwable t = p.getError();
                while (t != null) {
                    result.append(t.getLocalizedMessage());
                    t = t.getCause();
                    if (t != null) {
                        result.append("<br>");
                    }
                }
                result.append("</td><td colspan=\"2\"></td></tr>\n");
            }

            // create label and help bubble cells
            result.append("<tr>");
            result.append("<td class=\"xmlLabel");
            if (disabledElement) {
                // element is disabled, mark it with css
                result.append("Disabled");
            }

            result.append("\">");
            result.append(key(A_CmsWidget.getLabelKey(p), p.getName()));
            if (count > 1) {
                result.append(" [").append(p.getIndex() + 1).append("]");
            }
            result.append(": </td>");
            if (p.getIndex() == 0) {
                // show help bubble only on first element of each content definition 
                result.append(p.getWidget().getHelpBubble(getCms(), this, p));
            } else {
                // create empty cell for all following elements 
                result.append(dialogHorizontalSpacer(16));
            }

            // append individual widget html cell if element is enabled
            if (!disabledElement) {
                // this is a simple type, display widget
                result.append(widget.getDialogWidget(getCms(), this, p));
            } else {
                // disabled element, show message for optional element
                result.append("<td class=\"xmlTdDisabled maxwidth\">");
                result.append(key(Messages.GUI_EDITOR_WIDGET_OPTIONALELEMENT_0));
                result.append("</td>");
            }

            // append add and remove element buttons if required
            result.append(dialogHorizontalSpacer(5));
            result.append("<td>");
            if (addValue || removeValue) {
                result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

                if (!addValue) {
                    result.append(dialogHorizontalSpacer(25));
                } else {
                    result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                    result.append(buildAddElement(base.getName(), p.getIndex(), addValue));
                }

                if (removeValue) {
                    if (!addValue) {
                        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                    }
                    result.append(buildRemoveElement(base.getName(), p.getIndex(), removeValue));
                }

                result.append("</tr></table></td>");
                result.append("</tr></table>");
            }
            result.append("</td>");
            // close row
            result.append("</tr>\n");
        }

        return result.toString();
    }

    /**
     * Creates the dialog widget rows HTML for the specified widget indices.<p>
     * 
     * @param startIndex the widget index to start with
     * @param endIndex the widget index to stop at
     * @return the dialog widget rows HTML for the specified widget indices
     */
    protected String createDialogRowsHtml(int startIndex, int endIndex) {

        StringBuffer result = new StringBuffer((endIndex - startIndex) * 8);
        for (int i = startIndex; i <= endIndex; i++) {
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)getWidgets().get(i);
            result.append(createDialogRowHtml(base));
        }
        return result.toString();
    }

    /**
     * Creates the complete widget dialog end block HTML that finishes a widget block.<p>
     * 
     * @return the complete widget dialog end block HTML that finishes a widget block
     */
    protected String createWidgetBlockEnd() {

        StringBuffer result = new StringBuffer(8);
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        return result.toString();
    }

    /**
     * Create the complete widget dialog start block HTML that begins a widget block with optional headline.<p>
     * @param headline the headline String for the block
     * @return the complete widget dialog start block HTML that begins a widget block with optional headline
     */
    protected String createWidgetBlockStart(String headline) {

        StringBuffer result = new StringBuffer(16);
        result.append(dialogBlockStart(headline));
        result.append(createWidgetTableStart());
        return result.toString();
    }

    /**
     * Creates the HTML for the error message if validation errors were found.<p>
     * 
     * @return the HTML for the error message if validation errors were found
     */
    protected String createWidgetErrorHeader() {

        StringBuffer result = new StringBuffer(8);
        if (hasValidationErrors() || hasCommitErrors()) {
            result.append("<tr><td colspan=\"5\">&nbsp;</td></tr>\n");
            result.append("<tr><td colspan=\"2\">&nbsp;</td>");
            result.append("<td class=\"xmlTdErrorHeader\">");
            result.append(key(Messages.GUI_EDITOR_WIDGET_VALIDATION_ERROR_TITLE_0));
            result.append("</td><td colspan=\"2\">&nbsp;");
            result.append("</td></tr>\n");
            result.append("<tr><td colspan=\"5\">&nbsp;</td></tr>\n");
            if (hasCommitErrors()) {
                result.append(dialogBlockStart(""));
                result.append(createWidgetTableStart());
                Iterator i = getCommitErrors().iterator();
                while (i.hasNext()) {
                    Throwable t = (Throwable)i.next();
                    result.append("<tr><td><img src=\"");
                    result.append(getSkinUri()).append("editors/xmlcontent/");
                    result.append("error.png");
                    result.append("\" border=\"0\" alt=\"\"></td><td class=\"xmlTdError maxwidth\">");
                    while (t != null) {
                        result.append(CmsStringUtil.escapeHtml(t.getLocalizedMessage()));
                        t = t.getCause();
                        if (t != null) {
                            result.append("<br>");
                        }
                    }
                    result.append("</td></tr>\n");
                }
                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
            }
        }
        return result.toString();
    }

    /**
     * Creates the HTML for the table around the dialog widgets.<p>
     * 
     * @return the HTML for the table around the dialog widgets
     */
    protected String createWidgetTableEnd() {

        return "</table>\n";
    }

    /**
     * Creates the HTML to close the table around the dialog widgets.<p>
     * 
     * @return the HTML to close the table around the dialog widgets
     */
    protected String createWidgetTableStart() {

        return "<table cellspacing='0' cellpadding='0' class='xmlTable'>\n";
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     * 
     * @throws JspException if something goes wrong
     */
    protected String defaultActionHtml() throws JspException {

        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(defaultActionHtmlContent());
        result.append(dialogEnd());
        result.append(bodyEnd());
        result.append(htmlEnd());
        return result.toString();
    }

    /**
     * Returns the html code for the default action content.<p>
     * 
     * @return html code
     */
    protected String defaultActionHtmlContent() {

        StringBuffer result = new StringBuffer(2048);
        result.append("<form name=\"EDITOR\" id=\"EDITOR\" method=\"post\" action=\"").append(getDialogUri());
        result.append("\" class=\"nomargin\" onsubmit=\"return submitAction('").append(DIALOG_OK).append(
            "', null, 'EDITOR');\">\n");
        result.append(dialogContentStart(null));
        result.append(buildDialogForm());
        result.append(dialogContentEnd());
        result.append(dialogButtonsCustom());
        result.append(paramsAsHidden());
        if (getParamFramename() == null) {
            result.append("\n<input type=\"hidden\" name=\"").append(PARAM_FRAMENAME).append("\" value=\"\">\n");
        }
        result.append("</form>\n");
        result.append(getWidgetHtmlEnd());
        return result.toString();
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     * 
     * @throws JspException if something goes wrong
     */
    protected String defaultActionHtmlStart() throws JspException {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart("administration/index.html"));
        result.append("<script type=\"text/javascript\" src=\"").append(getResourceUri()).append(
            "editors/xmlcontent/edit.js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"").append(getResourceUri()).append(
            "editors/xmlcontent/help.js\"></script>\n");
        result.append(getWidgetIncludes());
        result.append("<script type=\"text/javascript\">\n<!--\n");
        result.append("// flag indicating if form initialization is finished\n");
        result.append("var initialized = false;\n");
        result.append("// the OpenCms context path\n");
        result.append("var contextPath = \"").append(OpenCms.getSystemInfo().getOpenCmsContext()).append("\";\n\n");
        result.append("// action parameters of the form\n");
        result.append("var actionAddElement = \"").append(EDITOR_ACTION_ELEMENT_ADD).append("\";\n");
        result.append("var actionRemoveElement = \"").append(EDITOR_ACTION_ELEMENT_REMOVE).append("\";\n");
        result.append("function init() {\n");
        result.append(getWidgetInitCalls());
        result.append("\tsetTimeout(\"scrollForm();\", 200);\n");
        result.append("\tinitialized = true;\n");
        result.append("}\n\n");
        result.append("function exitEditor() {\n");
        result.append("\ttry {\n");
        result.append("\t\t// close file selector popup if present\n");
        result.append("\t\tcloseTreeWin();\n");
        result.append("\t} catch (e) {}\n");
        result.append("}\n");
        result.append(getWidgetInitMethods());
        result.append("\n// -->\n</script>\n");
        result.append(bodyStart(null, "onload='init();' onunload='exitEditor();'"));
        result.append(dialogStart());
        return result.toString();
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

        Map parameters = request.getParameterMap();
        Map processedParamters = new HashMap();
        Iterator p = parameters.keySet().iterator();
        // make sure all "hidden" widget parameters are decoded
        while (p.hasNext()) {
            String key = (String)p.next();
            String[] values = (String[])parameters.get(key);
            if (key.startsWith(HIDDEN_PARAM_PREFIX)) {
                // this is an encoded hidden parameter
                key = key.substring(HIDDEN_PARAM_PREFIX.length());
                String[] newValues = new String[values.length];
                for (int l = 0; l < values.length; l++) {
                    newValues[l] = CmsEncoder.decode(values[l], getCms().getRequestContext().getEncoding());
                }
                values = newValues;
            }
            processedParamters.put(key, values);
        }

        // now process the parameters
        m_widgetParamValues = new HashMap();
        Iterator i = getWidgets().iterator();

        while (i.hasNext()) {
            // check for all widget base parameters            
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();

            List params = new ArrayList();
            int maxOccurs = base.getMaxOccurs();

            boolean onPage = false;
            if (base.isCollectionBase()) {
                // for a collection base, check if we are on the page where the collection base is shown
                if (CmsStringUtil.isNotEmpty(getParamAction()) && !DIALOG_INITIAL.equals(getParamAction())) {
                    // if no action set (usually for first display of dialog) make sure all values are shown
                    // DIALOG_INITIAL is a special value for the first display and must be handled the same way
                    String page = getParamPage();
                    // keep in mind that since the paramPage will be set AFTER the widget values are filled,
                    // so the first time this page is called from another page the following will result to "false",
                    // but for every "submit" on the page this will be "true"
                    onPage = CmsStringUtil.isEmpty(page)
                        || CmsStringUtil.isEmpty(base.getDialogPage())
                        || base.getDialogPage().equals(page);
                }
            }

            for (int j = 0; j < maxOccurs; j++) {
                // check for all possible values in the request parameters
                String id = CmsWidgetDialogParameter.createId(base.getName(), j);

                boolean required = (params.size() < base.getMinOccurs())
                    || (processedParamters.get(id) != null)
                    || (!onPage && base.hasValue(j));

                if (required) {
                    CmsWidgetDialogParameter param = new CmsWidgetDialogParameter(base, params.size(), j);
                    param.setKeyPrefix(m_prefix);
                    base.getWidget().setEditorValue(getCms(), processedParamters, this, param);
                    params.add(param);
                }
            }
            m_widgetParamValues.put(base.getName(), params);
        }
    }

    /**
     * Returns the allowed pages for this dialog.<p>
     * 
     * @return the allowed pages for this dialog
     */
    protected abstract String[] getPageArray();

    /**
     * Returns the allowed pages for this dialog.<p>
     * 
     * @return the allowed pages for this dialog
     */
    protected List getPages() {

        if (m_pages == null) {
            m_pages = Arrays.asList(getPageArray());
        }
        return m_pages;
    }

    /**
     * Returns the parameter widget definition for the given parameter name.<p>
     * 
     * @param name the parameter name to get the definition for 
     * 
     * @return the parameter widget definition for the given parameter name
     */
    protected CmsWidgetDialogParameter getParameterDefinition(String name) {

        Iterator i = getWidgets().iterator();
        while (i.hasNext()) {
            // check for all widget parameters            
            CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();
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

        return m_widgetParamValues;
    }

    /**
     * Returns the validation errors for the dialog.<p>
     * 
     * The method (@link CmsWidgetDialog#commitWidgetValues(String)) has to set this list.<p>
     * 
     * @return the validation errors for the dialog
     */
    protected List getValidationErrorList() {

        return m_validationErrorList;
    }

    /**
     * Returns the widget HTML code for the given parameter.<p> 
     * 
     * @param param the name (id) of the parameter to get the widget HTML for
     * 
     * @return the widget HTML code for the given parameter
     */
    protected String getWidget(CmsWidgetDialogParameter param) {

        if (param != null) {
            return param.getWidget().getDialogWidget(getCms(), this, param);
        }
        return null;
    }

    /**
     * Returns the list of all widgets used on this widget dialog, the 
     * List must contain Objects of type <code>{@link CmsWidgetDialogParameter}</code>.<p>
     * 
     * @return the list of all widgets used on this widget dialog
     */
    protected List getWidgets() {

        if (m_widgets == null) {
            m_widgets = new ArrayList();
        }
        return m_widgets;
    }

    /**
     * Returns <code>true</code> if the current dialog (page) has commit errors.<p>
     * 
     * @return <code>true</code> if the current dialog (page) has commit errors
     */
    protected boolean hasCommitErrors() {

        return (m_commitErrors != null) && (m_commitErrors.size() > 0);
    }

    /**
     * Returns <code>true</code> if the current dialog (page) has validation errors.<p>
     * 
     * @return <code>true</code> if the current dialog (page) has validation errors
     */
    protected boolean hasValidationErrors() {

        return (m_validationErrorList != null) && (m_validationErrorList.size() > 0);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.Messages.get().getBundleName());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(getClass().getName());

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamPage()) || !getPages().contains(getParamPage())) {
            // ensure a valid page is set
            setParamPage((String)getPages().get(0));
        }

        // fill the widget map
        defineWidgets();
        fillWidgetValues(request);

        // set the action for the JSP switch 
        if (DIALOG_SAVE.equals(getParamAction())) {
            // ok button pressed, save    
            List errors = commitWidgetValues();
            if (errors.size() > 0) {
                setAction(ACTION_DEFAULT);
                // found validation errors, redisplay page
                return;
            }
            setAction(ACTION_SAVE);
        } else if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_CANCEL);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else if (EDITOR_ACTION_ELEMENT_ADD.equals(getParamAction())) {
            // add optional input element
            setAction(ACTION_ELEMENT_ADD);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            // remove optional input element
            setAction(ACTION_ELEMENT_REMOVE);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (DIALOG_BACK.equals(getParamAction())) {
            // go back one page           
            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(getParamPage());
            if (errors.size() > 0) {
                // found validation errors, redisplay page
                return;
            }
            int pageIndex = getPages().indexOf(getParamPage()) - 1;
            setParamPage((String)getPages().get(pageIndex));

        } else if (DIALOG_CONTINUE.equals(getParamAction())) {
            // go to next page
            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(getParamPage());
            if (errors.size() > 0) {
                // found validation errors, redisplay page
                return;
            }
            int pageIndex = getPages().indexOf(getParamPage()) + 1;
            setParamPage((String)getPages().get(pageIndex));

        } else {
            // first dialog call, set the default action               
            setAction(ACTION_DEFAULT);
        }
    }

    /**
     * Sets the errors that are thrown by save actions or form generation.<p>
     * 
     * @param errors the errors that are thrown by save actions or form generation
     */
    protected void setCommitErrors(List errors) {

        m_commitErrors = errors;
    }

    /**
     * Sets an optional localized key prefix identificator for all widgets.<p>  
     * 
     * @param prefix the optional localized key prefix identificator for all widgets
     * 
     * @see org.opencms.widgets.I_CmsWidgetParameter#setKeyPrefix(java.lang.String)
     */
    protected void setKeyPrefix(String prefix) {

        m_prefix = prefix;
    }

    /**
     * Sets the allowed pages for this dialog.<p>
     * 
     * @param pages the allowed pages for this dialog
     */
    protected void setPages(List pages) {

        m_pages = pages;
    }

    /**
     * Sets the validation errors for the dialog.<p>
     * 
     * Use this in the method (@link CmsWidgetDialog#commitWidgetValues(String)) to set the list.<p>
     * 
     * @param errors the validation errors
     */
    protected void setValidationErrorList(List errors) {

        m_validationErrorList = errors;
    }

    /**
     * Returns the (internal use only) map of dialog objects.<p>
     * 
     * @return the (internal use only) map of dialog objects 
     */
    private Map getDialogObjectMap() {

        Map objects = (Map)getSettings().getDialogObject();
        if (objects == null) {
            // using hashtable as most efficient version of a synchronized map
            objects = new Hashtable();
            getSettings().setDialogObject(objects);
        }
        return objects;
    }
}