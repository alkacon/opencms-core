/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client.css;

import com.alkacon.acacia.client.css.I_LayoutBundle.I_Widgets;

import org.opencms.gwt.client.ui.css.I_CmsFloatDecoratedPanelCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;

/**
 * Content editor CSS resources bundle.<p>
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** The gallery widget CSS. */
    interface I_CmsGalleryWidgetsCss
    extends I_Widgets, I_CmsLocationPickerBase,
    org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.I_CmsGalleryFieldBaseCss {
        // no additional CSS classes
    }

    /** The XML content widget CSS. */
    interface I_CmsWidgetCss extends I_Widgets, I_CmsFloatDecoratedPanelCss, I_CmsGlobalWidgetCss {

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String calendarStyle();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String categoryPanel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String checkboxlabel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String checkBoxStyle();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String colorPicker();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String colorpickerpopup();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String colorPickerValue();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String comboBoxInput();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String displayTextBox();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String displayTextBoxPanel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String inputField();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String passwordTextBox();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String radioButtonlabel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String radioButtonPanel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String selectBoxPanel();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String textBox();

        /**
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String vfsInputBox();
    }

    /** The XML content editor CSS. */
    interface I_CmsXmlEditorCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String basePanel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String inlineEditable();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String integratedEditor();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String standAloneEditor();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the XML content editor CSS
     */
    @Source("editor.css")
    I_CmsXmlEditorCss editorCss();

    /**
     * Access method.<p>
     * 
     * @return the gallery widgets CSS
     */
    @Source("galleryWidgets.css")
    I_CmsGalleryWidgetsCss galleryWidgetsCss();

    /**
     * Access method.<p>
     * 
     * @return the XML content widget CSS
     */
    @Source("widget.css")
    I_CmsWidgetCss widgetCss();
}
