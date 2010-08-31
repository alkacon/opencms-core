/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryField.java,v $
 * Date   : $Date: 2010/08/31 07:30:24 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.input.CmsFramePopup;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;

/**
 * A widget for selecting a resource from an ADE gallery dialog.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsGalleryField extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** The widget type. */
    public static final String WIDGET_TYPE = "gallery";

    /** The HTML id of the field. */
    private String m_id;

    /** The textbox containing the currently selected path. */
    private CmsTextBox m_textbox;

    /** 
     * Constructs a new gallery widget.<p>
     */
    public CmsGalleryField() {

        CmsTextBox box = new CmsTextBox();
        m_id = box.getId();
        m_textbox = box;
        box.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                openGalleryDialog();
            }
        });

        initWidget(box);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsGalleryField();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return m_textbox.getFormValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return m_textbox.getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#onOpenDialog(org.opencms.gwt.client.ui.input.form.CmsFormDialog)
     */
    public void onOpenDialog(CmsFormDialog formDialog) {

        // do nothing 
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textbox.reset();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textbox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        // do nothing 
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        m_textbox.setText(value);

    }

    /**
     * Creates the URL for the gallery dialog IFrame.<p>
     * 
     * @return the URL for the gallery dialog IFrame 
     */
    protected String buildGalleryUrl() {

        String basePath = "/system/modules/org.opencms.ade.galleries/gallery.jsp";
        return CmsCoreProvider.get().link(basePath + "?dialogmode=widget&fieldid=" + m_id);
    }

    /**
     * Internal method which opens the gallery dialog.<p>
     */
    protected void openGalleryDialog() {
        
        String title = Messages.get().key(Messages.GUI_GALLERY_SELECT_DIALOG_TITLE_0);
        CmsFramePopup popup = new CmsFramePopup(title, buildGalleryUrl());
        popup.setId(m_id);
        popup.getFrame().setSize("666px", "490px");
        popup.center();
    }

}
