/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at our option) any later version.
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

package org.opencms.ade.postupload.client.ui;

import org.opencms.ade.postupload.shared.CmsPostUploadDialogBean;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogPanelBean;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsSimplePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Panel for the property dialog.<p>
 */
public class CmsUploadPropertyPanel extends FlowPanel implements I_CmsFormHandler, I_CmsTruncable {

    /** The upload property dialog containing this panel. */
    CmsUploadPropertyDialog m_dialog;

    /** The property editor handler instance. */
    I_CmsPropertyEditorHandler m_propertyEditorHandler;

    /** The property editor instance. */
    private CmsSimplePropertyEditor m_propertyEditor;

    /** The path relative resource path. */
    private String m_resourcePath;

    /** The values. */
    private CmsPostUploadDialogPanelBean m_values;

    /**
     * Public constructor.<p>
     *
     * @param dialog the dialog which this panel is added to
     * @param options the data to fill UI component options
     * @param values the bean with the current values
     */
    public CmsUploadPropertyPanel(
        CmsUploadPropertyDialog dialog,
        CmsPostUploadDialogBean options,
        CmsPostUploadDialogPanelBean values) {

        m_values = values;
        m_dialog = dialog;
        m_resourcePath = values.getInfoBean().getSubTitle();
        initializePropertyEditor();
        // height may change on click
        addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_dialog.updateHeight();
            }
        }, ClickEvent.getType());
    }

    /**
     * Gets the property editor instance.<p>
     *
     * @return the property editor instance
     */
    public CmsSimplePropertyEditor getPropertyEditor() {

        return m_propertyEditor;
    }

    /**
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * Returns the content bean (values) of the current dialog.<p>
     *
     * @return the content bean (values) of the current dialog
     */
    public CmsPostUploadDialogPanelBean getUpdatedValues() {

        CmsPostUploadDialogPanelBean bean = new CmsPostUploadDialogPanelBean(
            m_values.getStructureId(),
            m_values.getInfoBean());

        if (!m_values.equals(bean)) {
            m_values = bean;
        }
        return m_values;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#isSubmitting()
     */
    public boolean isSubmitting() {

        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onSubmitValidationResult(CmsForm form, boolean ok) {

        if (ok) {
            form.handleSubmit(new CmsPropertySubmitHandler(m_propertyEditorHandler));
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onValidationResult(CmsForm form, boolean ok) {

        // do nothing for now
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_propertyEditor.getForm().getWidget().truncate(textMetricsKey, clientWidth);
    }

    /**
     * Sets up the property editor.<p>
     */
    protected void initializePropertyEditor() {

        Map<String, CmsXmlContentProperty> propertyConfig = m_values.getPropertyDefinitions();
        m_propertyEditorHandler = new CmsUploadPropertyEditorHandler(m_dialog, m_values);
        CmsSimplePropertyEditor propertyEditor = new CmsUploadPropertyEditor(propertyConfig, m_propertyEditorHandler);
        propertyEditor.getForm().setFormHandler(this);
        m_propertyEditor = propertyEditor;
        m_propertyEditor.initializeWidgets(null);
        A_CmsFormFieldPanel propertiesPanel = m_propertyEditor.getForm().getWidget();
        add(propertiesPanel);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                if (m_dialog != null) {
                    m_dialog.updateHeight();
                }
            }
        });
    }
}
