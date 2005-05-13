/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/CmsAdminWidgetDemo3.java,v $
 * Date   : $Date: 2005/05/13 15:16:31 $
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

package org.opencms.workplace.tools.widgetdemo;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsImageGalleryWidget;
import org.opencms.widgets.CmsInputWidget;
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

/**
 * A basic example and proof-of-concept on how to use OpenCms widgets within a custom build form
 * without XML contents.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.9.1
 */
public class CmsAdminWidgetDemo3 extends CmsWidgetDialog {

    /** Value for the action: save the settings. */
    public static final int ACTION_SAVE = 300;

    /** Request parameter value for the action: save the dialog. */
    public static final String DIALOG_SAVE = "save";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo3";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo3(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo3(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the HTML for the demo3 form.<p>
     * 
     * @return the HTML for the demo3 form
     */
    public String buildDemo3Form() {

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

                if ((count < 1) && (base.getMinOccurs() > 0)) {
                    // no parameter with the value present, but also not optional: use base as parameter
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
                        result.append(widgetHelpBubble(p));
                    } else {
                        // create empty cell for all following elements 
                        result.append(buttonBarSpacer(16));
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
                    result.append("<td style=\"vertical-align: top;\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
                    result.append(buildAddElement(base.getName(), p.getIndex(), addValue));
                    result.append(buildRemoveElement(base.getName(), p.getIndex(), removeValue));
                    result.append("</tr></table></td>");
                    // close row
                    result.append("</tr>\n");

                }
            }
            // close table
            result.append("</table>\n");
        } catch (Throwable t) {
            OpenCms.getLog(this).error("Error in XML editor", t);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        addWidget(new CmsWidgetDialogParameter("stringwidget", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter("textwidget", new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter("boolwidget", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter("vfsfilewidget", new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter("imagegalwidget", new CmsImageGalleryWidget()));
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
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }
    }
}
