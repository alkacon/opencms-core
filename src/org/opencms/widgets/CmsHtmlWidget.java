/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsHtmlWidget.java,v $
 * Date   : $Date: 2005/10/01 20:50:06 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 * 
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 *
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.0.1 
 */
public class CmsHtmlWidget extends A_CmsHtmlWidget {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlWidget.class);

    /** The editor widget to use depending on the current users settings, current browser and installed editors. */
    private I_CmsWidget m_editorWidget;

    /**
     * Creates a new html editing widget.<p>
     */
    public CmsHtmlWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlWidget(CmsHtmlWidgetOption configuration) {

        super(configuration);
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsHtmlWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogIncludes(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitCall(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getEditorWidget(cms, widgetDialog).getDialogInitMethod(cms, widgetDialog);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return getEditorWidget(cms, widgetDialog).getDialogWidget(cms, widgetDialog, param);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsHtmlWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

    /**
     * Returns the editor widget to use depending on the current users settings, current browser and installed editors.<p>
     * 
     * @param cms the current CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @return the editor widget to use depending on the current users settings, current browser and installed editors
     */
    private I_CmsWidget getEditorWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        if (m_editorWidget == null) {
            // get HTML widget to use from editor manager
            String widgetClassName = OpenCms.getWorkplaceManager().getWorkplaceEditorManager().getWidgetEditor(
                cms.getRequestContext(),
                widgetDialog.getUserAgent());
            boolean foundWidget = true;
            if (CmsStringUtil.isEmpty(widgetClassName)) {
                // no installed widget found, use default text area to edit HTML value
                widgetClassName = CmsTextareaWidget.class.getName();
                foundWidget = false;
            }
            try {
                if (foundWidget) {
                    // get widget instance and set the widget configuration
                    Class widgetClass = Class.forName(widgetClassName);
                    A_CmsHtmlWidget editorWidget = (A_CmsHtmlWidget)widgetClass.newInstance();
                    editorWidget.setHtmlWidgetOption(getHtmlWidgetOption());
                    m_editorWidget = editorWidget;
                } else {
                    // set the text area to display 15 rows for editing
                    Class widgetClass = Class.forName(widgetClassName);
                    I_CmsWidget editorWidget = (I_CmsWidget)widgetClass.newInstance();
                    editorWidget.setConfiguration("15");
                    m_editorWidget = editorWidget;
                }
            } catch (Exception e) {
                // failed to create widget instance
                LOG.error(Messages.get().container(Messages.LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1, widgetClassName).key());
            }

        }
        return m_editorWidget;
    }
}