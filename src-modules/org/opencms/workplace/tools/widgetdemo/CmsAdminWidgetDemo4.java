/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/CmsAdminWidgetDemo4.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.13 $
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

package org.opencms.workplace.tools.widgetdemo;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsDownloadGalleryWidget;
import org.opencms.widgets.CmsHtmlGalleryWidget;
import org.opencms.widgets.CmsImageGalleryWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsLinkGalleryWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsTableGalleryWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A basic example and proof-of-concept on how to use OpenCms widgets within a custom build form
 * without XML contents.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13 $
 * @since 5.9.1
 */
public class CmsAdminWidgetDemo4 extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo4";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAdminWidgetDemo3.class);

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo4(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo4(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        // not implemented for this demo
    }

    /**
     * Builds the HTML for the demo4 form.<p>
     * 
     * @return the HTML for the demo4 form
     */
    public String buildDemo4Form() {

        StringBuffer result = new StringBuffer(1024);

        try {

            // create table
            result.append("<table class=\"xmlTable\">\n");

            Iterator i = getWidgets().iterator();
            // iterate the type sequence                    
            while (i.hasNext()) {

                // get the current widget base definition
                CmsWidgetDialogParameter base = (CmsWidgetDialogParameter)i.next();
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
                        result.append(key("editor.xmlcontent.optionalelement"));
                        result.append("</td>");
                    }

                    // append add and remove element buttons if required
                    result.append(dialogHorizontalSpacer(5));
                    result.append("<td>");
                    if (addValue || removeValue) {
                        result.append("<table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                        result.append(buildAddElement(base.getName(), p.getIndex(), addValue));
                        result.append(buildRemoveElement(base.getName(), p.getIndex(), removeValue));
                        result.append("</tr></table>");
                    }
                    result.append("</td>");
                    // close row
                    result.append("</tr>\n");

                }
            }
            // close table
            result.append("</table>\n");
        } catch (Throwable t) {
            LOG.error(org.opencms.workplace.editors.Messages.get().key(
                org.opencms.workplace.editors.Messages.ERR_XML_EDITOR_0), t);
        }
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        addWidget(new CmsWidgetDialogParameter("stringwidget", new CmsInputWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("textwidget", new CmsTextareaWidget(), 0, 5));
        // Please note: Boolean widget sequences are currently not supported 
        addWidget(new CmsWidgetDialogParameter("boolwidget", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter("vfsfilewidget", new CmsVfsFileWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("imagegalwidget", new CmsImageGalleryWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("downgalwidget", new CmsDownloadGalleryWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("htmlgalwidget", new CmsHtmlGalleryWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("tablegalwidget", new CmsTableGalleryWidget(), 0, 5));
        addWidget(new CmsWidgetDialogParameter("extgalwidget", new CmsLinkGalleryWidget(), 0, 5));

        String val1 = Messages.get().key(getLocale(), Messages.GUI_WIDGETDEMO_DEMOVALUE_1, new Object[] {"1"});
        List widgetOptions = new ArrayList();
        widgetOptions.add(new CmsSelectWidgetOption(val1, false, null, Messages.get().key(
            getLocale(),
            Messages.GUI_WIDGETDEMO_DEMOHELP_1,
            new Object[] {val1})));
        widgetOptions.add(new CmsSelectWidgetOption(Messages.get().key(
            getLocale(),
            Messages.GUI_WIDGETDEMO_DEMOVALUE_1,
            new Object[] {"2"}), true, null, Messages.get().key(
            getLocale(),
            Messages.GUI_WIDGETDEMO_DEMOHELP_VAL2_0,
            null)));
        widgetOptions.add(new CmsSelectWidgetOption(Messages.get().key(
            getLocale(),
            Messages.GUI_WIDGETDEMO_DEMOVALUE_1,
            new Object[] {"3"})));
        addWidget(new CmsWidgetDialogParameter("combowidget", new CmsComboWidget(widgetOptions), 0, 2));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return new String[] {"page1"};
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initMessages()
     */
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // fill the widget map
        defineWidgets();
        fillWidgetValues(request);

        // set the action for the JSP switch 
        if (DIALOG_SAVE.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_SAVE);
        } else if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_CANCEL);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else if (EDITOR_ACTION_ELEMENT_ADD.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_ADD);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_REMOVE);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }
    }
}
