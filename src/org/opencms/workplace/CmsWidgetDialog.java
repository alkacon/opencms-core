/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWidgetDialog.java,v $
 * Date   : $Date: 2005/05/12 13:16:13 $
 * Version: $Revision: 1.8 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.xmlwidgets.A_CmsXmlWidget;
import org.opencms.workplace.xmlwidgets.CmsWidgetParameter;
import org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog;
import org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter;
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

import org.apache.commons.logging.Log;

/**
 * Base class for dialogs that use the OpenCms widgets without XML content.
 * <p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.9.1
 */
public abstract class CmsWidgetDialog extends CmsDialog implements
		I_CmsWidgetDialog {

	/** Action for optional element creation. */
	public static final int ACTION_ELEMENT_ADD = 152;

	/** Action for optional element removal. */
	public static final int ACTION_ELEMENT_REMOVE = 153;

	/** Indicates an optional element should be created. */
	public static final String EDITOR_ACTION_ELEMENT_ADD = "addelement";

	/** Indicates an optional element should be removed. */
	public static final String EDITOR_ACTION_ELEMENT_REMOVE = "removeelement";

	/**
	 * Prefix for "hidden" parameters, required since these must be unescaped
	 * later.
	 */
	public static final String HIDDEN_PARAM_PREFIX = "hidden.";

	/** The log object for this class. */
	private static final Log LOG = CmsLog.getLog(CmsWidgetDialog.class);

	/**
	 * Parameter stores the index of the element to add or remove.
	 * <p>
	 * 
	 * This must not be <code>null</code>, because it must appear when
	 * calling
	 * <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.
	 * <p>
	 */
	private String m_paramElementIndex = "0";

	/**
	 * Parameter stores the name of the element to add or remove.
	 * <p>
	 * 
	 * This must not be <code>null</code>, because it must appear when
	 * calling
	 * <code>{@link org.opencms.workplace.CmsWorkplace#paramsAsHidden()}</code>.
	 * <p>
	 */
	private String m_paramElementName = "undefined";

	/** Contains all parameter value of this dialog. */
	protected Map m_paramValues;

	/** The list of widgets used on the dialog. */
	protected List m_widgets;

	/**
	 * Public constructor with JSP action element.
	 * <p>
	 * 
	 * @param jsp
	 *            an initialized JSP action element
	 */
	public CmsWidgetDialog(CmsJspActionElement jsp) {

		super(jsp);
	}

	/**
	 * Public constructor with JSP variables.
	 * <p>
	 * 
	 * @param context
	 *            the JSP page context
	 * @param req
	 *            the JSP request
	 * @param res
	 *            the JSP response
	 */
	public CmsWidgetDialog(PageContext context, HttpServletRequest req,
			HttpServletResponse res) {

		this(new CmsJspActionElement(context, req, res));
	}

	/**
	 * Adds an optional element to the xml content or removes an optional
	 * element from the xml content.
	 * <p>
	 * 
	 * Depends on the given action value.
	 * <p>
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
			List params = (List) getParameters().get(name);
			if (getAction() == ACTION_ELEMENT_REMOVE) {
				// remove the value
				params.remove(index);
			} else {
				// add the new value after the clicked element
				index = index + 1;
				CmsWidgetParameter newParam = new CmsWidgetParameter(base,
						index);
				params.add(index, newParam);
			}
			// reset all index value in the parameter list
			for (int i = 0; i < params.size(); i++) {
				CmsWidgetParameter param = (CmsWidgetParameter) params.get(i);
				param.setindex(i);
			}
		}
	}

	/**
	 * Adds a new widget parameter definition to the list of all widgets of this
	 * dialog.
	 * <p>
	 * 
	 * @param param
	 *            the widget parameter definition to add
	 */
	protected void addWidget(CmsWidgetParameter param) {

		if (m_widgets == null) {
			m_widgets = new ArrayList();
		}

		m_widgets.add(param);
	}

	/**
	 * Returns the html for a button to add an optional element.
	 * <p>
	 * 
	 * @param elementName
	 *            name of the element
	 * @param insertAfter
	 *            the index of the element after which the new element should be
	 *            created
	 * @param enabled
	 *            if true, the button to add an element is shown, otherwise a
	 *            spacer is returned
	 * @return the html for a button to add an optional element
	 */
	public String buildAddElement(String elementName, int insertAfter,
			boolean enabled) {

		if (enabled) {
			StringBuffer href = new StringBuffer(4);
			href.append("javascript:addElement('");
			href.append(elementName);
			href.append("', ");
			href.append(insertAfter);
			href.append(");");
			return button(href.toString(), null, "new", "button.addnew", 0);
		} else {
			return "";
		}
	}

	/**
	 * Returns the html for a button to remove an optional element.
	 * <p>
	 * 
	 * @param elementName
	 *            name of the element
	 * @param index
	 *            the element index of the element to remove
	 * @param enabled
	 *            if true, the button to remove an element is shown, otherwise a
	 *            spacer is returned
	 * @return the html for a button to remove an optional element
	 */
	public String buildRemoveElement(String elementName, int index,
			boolean enabled) {

		if (enabled) {
			StringBuffer href = new StringBuffer(4);
			href.append("javascript:removeElement('");
			href.append(elementName);
			href.append("', ");
			href.append(index);
			href.append(");");
			return button(href.toString(), null, "deletecontent",
					"button.delete", 0);
		} else {
			return "";
		}
	}

	/**
	 * Commits all values on the dialog.
	 * <p>
	 * 
	 * @return a List of all Exceptions that occured when comitting the dialog.
	 *         <p>
	 */
	protected List commitWidgetValues() {

		return commitWidgetValues(null);
	}

	/**
	 * Commits all values on the given dialog page.
	 * <p>
	 * 
	 * @param dialog
	 *            the dialog page to commit
	 * 
	 * @return a List of all Exceptions that occured when comitting the dialog
	 *         page.
	 *         <p>
	 */
	protected List commitWidgetValues(String dialog) {

		List result = new ArrayList();
		Iterator i = getWidgets().iterator();
		while (i.hasNext()) {
			// check for all widget parameters
			CmsWidgetParameter base = (CmsWidgetParameter) i.next();
			if ((dialog == null) || (base.getDialog() == null)
					|| dialog.equals(base.getDialog())) {
				// the parameter is located on the requested dialog
				List params = (List) m_paramValues.get(base.getName());
				Iterator j = params.iterator();
				while (j.hasNext()) {
					CmsWidgetParameter param = (CmsWidgetParameter) j.next();
					try {
						param.commitValue();
					} catch (Exception e) {
						result.add(e);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Creates the dialog HTML for all defined widgets of this dialog.
	 * <p>
	 * 
	 * @return the dialog HTML for all defined widgets of this dialog
	 * 
	 * @throws CmsXmlException
	 *             in case the HTML for the dialog can't be generated
	 */
	protected String createDialogHtml() throws CmsXmlException {

		return createDialogHtml(null);
	}

	/**
	 * Creates the dialog HTML for all defined widgets of the named dialog
	 * (page).
	 * <p>
	 * 
	 * @param dialog
	 *            the dialog (page) to get the HTML for
	 * @return the dialog HTML for all defined widgets of the named dialog
	 *         (page)
	 * 
	 * @throws CmsXmlException
	 *             in case the HTML for the dialog can't be generated
	 */
	protected String createDialogHtml(String dialog) throws CmsXmlException {

		StringBuffer result = new StringBuffer(1024);

		// create table
		result.append("<table class=\"xmlTable\">\n");

		Iterator i = getWidgets().iterator();
		// iterate the type sequence
		while (i.hasNext()) {
			// get the current widget base definition
			CmsWidgetParameter base = (CmsWidgetParameter) i.next();
			// check if the element is on the requested dialog page
			if ((dialog == null) || dialog.equals(base.getDialog())) {
				// add the HTML for the dialog element
				result.append(createDialogRowHtml(base));
			}
		}
		// close table
		result.append("</table>\n");

		return result.toString();
	}

	/**
	 * Creates the dialog HTML for all occurences of one widget parameter.
	 * <p>
	 * 
	 * @param base
	 *            the widget parameter base
	 * @return the dialog HTML for one widget parameter
	 * 
	 * @throws CmsXmlException
	 *             in case the HTML for the dialog widget can't be generated
	 */
	protected String createDialogRowHtml(CmsWidgetParameter base)
			throws CmsXmlException {

		StringBuffer result = new StringBuffer(256);

		List sequence = (List) getParameters().get(base.getName());
		int count = sequence.size();

		if ((count < 1) && (base.getMinOccurs() > 0)) {
			// no parameter with the value present, but also not optional: use
			// base as parameter
			sequence = new ArrayList();
			sequence.add(base);
			count = 1;
		}

		// check if value is optional or multiple
		boolean addValue = false;
		if (count < base.getMaxOccurs()) {
			addValue = true;
		}
		boolean removeValue = false;
		if (count > base.getMinOccurs()) {
			removeValue = true;
		}

		boolean disabledElement = false;

		// loop through multiple elements
		for (int j = 0; j < count; j++) {

			// get the parameter and the widget
			CmsWidgetParameter p = (CmsWidgetParameter) sequence.get(j);
			I_CmsXmlWidget widget = p.getWidget();

			// create label and help bubble cells
			result.append("<tr>");
			result.append("<td class=\"xmlLabel");
			if (disabledElement) {
				// element is disabled, mark it with css
				result.append("Disabled");
			}
			result.append("\">");
			result.append(key(A_CmsXmlWidget.getLabelKey(p), p.getName()));
			if (count > 1) {
				result.append(" [").append(p.getIndex() + 1).append("]");
			}
			result.append(": </td>");
			if (p.getIndex() == 0) {
				// show help bubble only on first element of each content
				// definition
				//result.append(widget.getHelpBubble(getCms(), this, p));
				result.append(widgetHelpBubble(p));
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
				result.append(key("editor.xmlcontent.optionalelement"));
				result.append("</td>");
			}

			// append add and remove element buttons if required
			result.append(dialogHorizontalSpacer(5));
			result.append("<td>");
			if (addValue || removeValue) {
				result
						.append("<table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
				result.append(buildAddElement(base.getName(), p.getIndex(),
						addValue));
				result.append(buildRemoveElement(base.getName(), p.getIndex(),
						removeValue));
				result.append("</tr></table>");
			}
			result.append("</td>");
			// close row
			result.append("</tr>\n");
		}

		return result.toString();
	}

	/**
	 * Defines the list of parameters for this dialog.
	 * <p>
	 */
	protected abstract void defineWidgets();

	/**
	 * Fills all widgets of this widget dialog with the values from the request
	 * parameters.
	 * <p>
	 * 
	 * @param request
	 *            the current HTTP servlet request
	 */
	protected void fillWidgetValues(HttpServletRequest request) {

		Map parameters = request.getParameterMap();
		Map processedParamters = new HashMap();
		Iterator p = parameters.keySet().iterator();
		// make sure all "hidden" widget parameters are decoded
		while (p.hasNext()) {
			String key = (String) p.next();
			String[] values = (String[]) parameters.get(key);
			if (key.startsWith(HIDDEN_PARAM_PREFIX)) {
				// this is an encoded hidden parameter
				key = key.substring(HIDDEN_PARAM_PREFIX.length());
				String[] newValues = new String[values.length];
				for (int l = 0; l < values.length; l++) {
					newValues[l] = CmsEncoder.decode(values[l], getCms()
							.getRequestContext().getEncoding());
				}
				values = newValues;
			}
			processedParamters.put(key, values);
		}

		// now process the parameters
		m_paramValues = new HashMap();
		Iterator i = getWidgets().iterator();
		while (i.hasNext()) {
			// check for all widget parameters
			CmsWidgetParameter base = (CmsWidgetParameter) i.next();
			List params = new ArrayList();
			// "unbounded" dialog lists are not required
			int maxOccurs = base.getMaxOccurs() < CmsWidgetParameter.MAX_OCCURENCES ? base
					.getMaxOccurs()
					: CmsWidgetParameter.MAX_OCCURENCES;
			for (int j = 0; j < maxOccurs; j++) {
				// check for all possible values in the request parameters
				String id = CmsWidgetParameter.createId(base.getName(), j);
				boolean required = (params.size() < base.getMinOccurs())
						|| (processedParamters.get(id) != null)
						|| base.hasValue(j);
				if (required) {
					CmsWidgetParameter param = new CmsWidgetParameter(base,
							params.size(), j);
					try {
						base.getWidget().setEditorValue(getCms(),
								processedParamters, this, param);
					} catch (CmsXmlException e) {
						int todo = 0;
						// TODO: error handling
					}
					params.add(param);
				}
			}
			m_paramValues.put(base.getName(), params);
		}
	}

	/**
	 * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog#getButtonStyle()
	 */
	public int getButtonStyle() {

		return getSettings().getUserSettings().getEditorButtonStyle();
	}

	/**
	 * Returns the index of the element to add or remove.
	 * <p>
	 * 
	 * @return the index of the element to add or remove
	 */
	public String getParamElementIndex() {

		return m_paramElementIndex;
	}

	/**
	 * Returns the name of the element to add or remove.
	 * <p>
	 * 
	 * @return the name of the element to add or remove
	 */
	public String getParamElementName() {

		return m_paramElementName;
	}

	/**
	 * Returns the parameter widget definition for the given parameter name.
	 * <p>
	 * 
	 * @param name
	 *            the parameter name to get the definition for
	 * 
	 * @return the parameter widget definition for the given parameter name
	 */
	protected CmsWidgetParameter getParameterDefinition(String name) {

		Iterator i = getWidgets().iterator();
		while (i.hasNext()) {
			// check for all widget parameters
			CmsWidgetParameter base = (CmsWidgetParameter) i.next();
			if (base.getName().equals(name)) {
				return base;
			}
		}
		return null;
	}

	/**
	 * Returns the map with the widget parameter values.
	 * <p>
	 * 
	 * @return the map with the widget parameter values
	 */
	protected Map getParameters() {

		return m_paramValues;
	}

	/**
	 * Returns the value of the widget parameter with the given name, or
	 * <code>null</code> if no such widget parameter is available.
	 * <p>
	 * 
	 * @param name
	 *            the widget parameter name to get the value for
	 * 
	 * @return the value of the widget parameter with the given name
	 */
	public String getParamValue(String name) {

		return getParamValue(name, 0);
	}

	/**
	 * Returns the value of the widget parameter with the given name and index,
	 * or <code>null</code> if no such widget parameter is available.
	 * <p>
	 * 
	 * @param name
	 *            the widget parameter name to get the value for
	 * @param index
	 *            the widget parameter index
	 * 
	 * @return the value of the widget parameter with the given name and index
	 */
	public String getParamValue(String name, int index) {

		List params = (List) m_paramValues.get(name);
		if (params != null) {
			if ((index >= 0) && (index < params.size())) {
				CmsWidgetParameter param = (CmsWidgetParameter) params
						.get(index);
				if (param.getId().equals(
						CmsWidgetParameter.createId(name, index))) {
					return param.getStringValue(getCms());
				}
			}
		}

		return null;
	}

	/**
	 * Returns the widget HTML code for the given parameter.
	 * <p>
	 * 
	 * @param param
	 *            the name (id) of the parameter to get the widget HTML for
	 * 
	 * @return the widget HTML code for the given parameter
	 * 
	 * @throws CmsXmlException
	 *             if the widget HTML could not be generated
	 */
	protected String getWidget(CmsWidgetParameter param) throws CmsXmlException {

		if (param != null) {
			return param.getWidget().getDialogWidget(getCms(), this, param);
		}
		return null;
	}

	/**
	 * Generates the HTML for the end of the widget dialog.
	 * <p>
	 * 
	 * This HTML includes additional components, for example the &lt;div&gt;
	 * tags containing the help texts.
	 * <p>
	 * 
	 * @return the HTML for the end of the widget dialog
	 */
	public String getWidgetHtmlEnd() {

		StringBuffer result = new StringBuffer(32);
		// iterate over unique widgets from collector
		Iterator i = getWidgets().iterator();
		while (i.hasNext()) {
			CmsWidgetParameter param = (CmsWidgetParameter) i.next();
			//result.append(widget.getDialogHtmlEnd(getCms(), this, param));
			result.append(widgetHelpText(param));
		}
		return result.toString();
	}

	/**
	 * Generates the HTML include tags for external JavaScripts files of the
	 * used widgets.
	 * <p>
	 * 
	 * @return the HTML include tags for external JavaScripts files of the used
	 *         widgets
	 * 
	 * @throws JspException
	 *             if an error occurs during JavaScript generation
	 */
	public String getWidgetIncludes() throws JspException {

		StringBuffer result = new StringBuffer(32);
		try {
			// iterate over unique widgets from collector
			Iterator i = getWidgets().iterator();
			Set set = new HashSet();
			while (i.hasNext()) {
				I_CmsXmlWidget widget = ((CmsWidgetParameter) i.next())
						.getWidget();
				if (!set.contains(widget)) {
					result.append(widget.getDialogIncludes(getCms(), this));
					result.append('\n');
					set.add(widget);
				}
			}
		} catch (CmsXmlException e) {

			LOG.error(e.getLocalizedMessage());
			getJsp().getRequest().setAttribute(ATTRIBUTE_THROWABLE, e);
			getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
		}
		if (!useNewStyle()) {
			result.append("<script type='text/javascript' src='");
			result.append(getResourceUri());
			result.append("editors/xmlcontent/help.js'></script>\n");		
		}
		return result.toString();
	}

	/**
	 * Generates the JavaScript init calls for the used widgets.
	 * <p>
	 * 
	 * @return the JavaScript init calls for the used widgets
	 * 
	 * @throws JspException
	 *             the JavaScript init calls for the used widgets
	 */
	public String getWidgetInitCalls() throws JspException {

		StringBuffer result = new StringBuffer(32);
		try {
			// iterate over unique widgets from collector
			Iterator i = getWidgets().iterator();
			Set set = new HashSet();
			while (i.hasNext()) {
				I_CmsXmlWidget widget = ((CmsWidgetParameter) i.next())
						.getWidget();
				if (!set.contains(widget)) {
					result.append(widget.getDialogInitCall(getCms(), this));
					set.add(widget);
				}
			}
		} catch (CmsXmlException e) {

			LOG.error(e.getLocalizedMessage());
			getJsp().getRequest().setAttribute(ATTRIBUTE_THROWABLE, e);
			getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
		}
		return result.toString();
	}

	/**
	 * Generates the JavaScript initialization methods for the used widgets.
	 * <p>
	 * 
	 * @return the JavaScript initialization methods for the used widgets
	 * 
	 * @throws JspException
	 *             if an error occurs during JavaScript generation
	 */
	public String getWidgetInitMethods() throws JspException {

		StringBuffer result = new StringBuffer(32);
		try {
			// iterate over unique widgets from collector
			Iterator i = getWidgets().iterator();
			Set set = new HashSet();
			while (i.hasNext()) {
				I_CmsXmlWidget widget = ((CmsWidgetParameter) i.next())
						.getWidget();
				if (!set.contains(widget)) {
					result.append(widget.getDialogInitMethod(getCms(), this));
					set.add(widget);
				}
			}
		} catch (CmsXmlException e) {

			LOG.error(e.getLocalizedMessage());
			getJsp().getRequest().setAttribute(ATTRIBUTE_THROWABLE, e);
			getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
		}
		return result.toString();
	}

	/**
	 * Returns the list of all widgets used on this widget dialog, the List must
	 * contain Objects of type <code>{@link CmsWidgetParameter}</code>.
	 * <p>
	 * 
	 * @return the list of all widgets used on this widget dialog
	 */
	protected List getWidgets() {

		return m_widgets;
	}

	/**
	 * Sets the index of the element to add or remove.
	 * <p>
	 * 
	 * @param elementIndex
	 *            the index of the element to add or remove
	 */
	public void setParamElementIndex(String elementIndex) {

		m_paramElementIndex = elementIndex;
	}

	/**
	 * Sets the name of the element to add or remove.
	 * <p>
	 * 
	 * @param elementName
	 *            the name of the element to add or remove
	 */
	public void setParamElementName(String elementName) {

		m_paramElementName = elementName;
	}

	/**
	 * Implementation for the Administration framework.
	 * <p>
	 * 
	 * @param param
	 *            the widget parameter
	 * 
	 * @return html code
	 * 
	 * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getHelpBubble(org.opencms.file.CmsObject,
	 *      I_CmsWidgetDialog, I_CmsWidgetParameter)
	 */
	protected String widgetHelpBubble(CmsWidgetParameter param) {

		if (!useNewStyle()) {
			return param.getWidget().getHelpBubble(getCms(), this, param);
		}
		String locKey = A_CmsXmlWidget.getHelpKey(param);
		String locValue = ((I_CmsWidgetDialog) this).key(locKey, null);
		if (locValue == null) {
			// there was no help message found for this key, so return a spacer
			// cell
			return this.dialogHorizontalSpacer(16);
		} else {
			StringBuffer result = new StringBuffer(256);
			result.append("<td>");
			result.append("<img name=\"img");
			result.append(locKey);
			result.append("\" id=\"img");
			result.append(locKey);
			result.append("\" src=\"");
			result.append(OpenCms.getLinkManager().substituteLink(getCms(),
					"/system/workplace/resources/commons/help.gif"));
			result.append("\" border=\"0\" onmouseout=\"hideMenuHelp('");
			result.append(locKey);
			result.append("');\" onmouseover=\"showMenuHelp('");
			result.append(locKey);
			result.append("');\">");
			result.append("</td>");
			return result.toString();
		}
	}

	/**
	 * Implementation for the Administration framework.
	 * <p>
	 * 
	 * @param param
	 *            the widget parameter
	 * 
	 * @return html code
	 * 
	 * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getHelpText(I_CmsWidgetDialog,
	 *      I_CmsWidgetParameter)
	 */
	protected String widgetHelpText(CmsWidgetParameter param) {

		if (!useNewStyle()) {
			return param.getWidget().getHelpText(this, param);
		}
		StringBuffer result = new StringBuffer(128);
		// calculate the key
		String locKey = A_CmsXmlWidget.getHelpKey(param);
		String locValue = ((I_CmsWidgetDialog) this).key(locKey, null);
		if (locValue == null) {
			// there was no help message found for this key, so return an empty
			// string
			return "";
		} else {
			result.append("<div class=\"help\" name=\"help");
			result.append(locKey);
			result.append("\" id=\"help");
			result.append(locKey);
			result.append("\" onmouseout=\"hideMenuHelp('");
			result.append(locKey);
			result.append("');\" onmouseover=\"showMenuHelp('");
			result.append(locKey);
			result.append("');\">");
			result.append(locValue);
			result.append("</div>");
			return result.toString();
		}
	}

	/**
	 * Returns the values of all widget parameters of this dialog as HTML hidden
	 * fields.
	 * <p>
	 * 
	 * @return the values of all widget parameters of this dialog as HTML hidden
	 *         fields
	 * 
	 * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
	 */
	public String widgetParamsAsHidden() {

		return widgetParamsAsHidden(null);
	}

	/**
	 * Returns the values of all widget parameters of this dialog as HTML hidden
	 * fields, excluding the widget values that are on the given dialog page.
	 * <p>
	 * 
	 * This can be used to create multi-page dialogs where the values are passed
	 * from one page to another before everyting is submitted. If a widget A is
	 * used on page X, there should be no "hidden" HTML field for A since
	 * otherwise A would have 2 values when submitting the dialog page: The one
	 * from the widget itself and the one from the hidden field. This may lead
	 * to undefined results when processing the submitted values.
	 * <p>
	 * 
	 * @param excludeDialogPage
	 *            the dialog page to exclude the values for
	 * 
	 * @return the values of all widget parameters of this dialog as HTML hidden
	 *         fields, excluding the widget values that are on the given dialog
	 *         page
	 * 
	 * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
	 */
	public String widgetParamsAsHidden(String excludeDialogPage) {

		StringBuffer result = new StringBuffer();
		Iterator i = m_paramValues.keySet().iterator();
		while (i.hasNext()) {
			List params = (List) m_paramValues.get(i.next());
			Iterator j = params.iterator();
			while (j.hasNext()) {
				CmsWidgetParameter param = (CmsWidgetParameter) j.next();
				String value = param.getStringValue(getCms());
				if (CmsStringUtil.isNotEmpty(value)
						&& ((excludeDialogPage == null) || (!param.getDialog()
								.equals(excludeDialogPage)))) {
					result.append("<input type=\"hidden\" name=\"");
					result.append(HIDDEN_PARAM_PREFIX);
					result.append(param.getId());
					result.append("\" value=\"");
					String encoded = CmsEncoder.encode(value, getCms()
							.getRequestContext().getEncoding());
					result.append(encoded);
					result.append("\">\n");
				}
			}
		}
		return result.toString();
	}
}
