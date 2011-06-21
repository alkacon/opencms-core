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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The vfs-link widget.<p>
 * 
 * @since 8.0.0
 */
public class CmsVfsLinkWidget extends Composite implements I_CmsFormWidget, I_CmsHasInit, I_CmsHasGhostValue {

    /** The widget type. */
    public static final String WIDGET_TYPE = "vfslink";

    /** The browse button. */
    protected CmsPushButton m_browseButton;

    /** The textbox containing the currently selected path. */
    protected CmsTextBox m_textbox;

    /** The vfs-selector popup. */
    protected CmsVfsSelector m_vfsSelector;

    /** The widget panel. */
    private FlowPanel m_main;

    /**
     * Constructor.<p>
     */
    public CmsVfsLinkWidget() {

        m_main = new FlowPanel();
        initWidget(m_main);

        m_textbox = new CmsTextBox();
        m_main.add(m_textbox);
        m_browseButton = new CmsPushButton();
        m_browseButton.setText(Messages.get().key(Messages.GUI_BROWSE_0));
        m_browseButton.setTitle(Messages.get().key(Messages.GUI_BROWSE_0));
        m_browseButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                openSelector();
            }
        });
        m_main.add(m_browseButton);
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

                return new CmsVfsLinkWidget();
            }
        });
    }

    /**
     * Adds a style-name to the browse button.<p>
     * 
     * @param styleName the style name
     */
    public void addButtonStyle(String styleName) {

        m_browseButton.addStyleName(styleName);
    }

    /**
     * Adds a style-name to the input text-box.<p>
     * 
     * @param styleName the style name
     */
    public void addInputStyleName(String styleName) {

        m_textbox.addStyleName(styleName);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textbox.isEnabled();
    }

    /**
     * Removes a style-name from the browse button.<p>
     * 
     * @param styleName the style name
     */
    public void removeButtonStyle(String styleName) {

        m_browseButton.removeStyleName(styleName);
    }

    /**
     * Removes a style-name from the input text-box.<p>
     * 
     * @param styleName the style name
     */
    public void removeInputStyle(String styleName) {

        m_textbox.removeStyleName(styleName);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textbox.reset();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // do nothing 
    }

    /**
     * Set the browse button size.<p>
     * 
     * @param size the button size
     */
    public void setButtonSize(Size size) {

        m_browseButton.setSize(size);
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
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostMode(boolean)
     */
    public void setGhostMode(boolean ghostMode) {

        m_textbox.setGhostMode(ghostMode);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsHasGhostValue#setGhostValue(java.lang.String, boolean)
     */
    public void setGhostValue(String value, boolean isGhostMode) {

        m_textbox.setGhostValue(value, isGhostMode);
    }

    /**
     * Opens the vfs-selector.<p>
     */
    protected void openSelector() {

        m_browseButton.disable(Messages.get().key(Messages.GUI_BROWSING_0));
        if (m_vfsSelector == null) {
            m_vfsSelector = new CmsVfsSelector();
            m_vfsSelector.setSelectCallback(new I_CmsSimpleCallback<String>() {

                public void execute(String path) {

                    m_textbox.setFormValueAsString(path);
                    m_vfsSelector.hide();
                    m_browseButton.enable();

                }
            });
            m_vfsSelector.setAutoHideEnabled(true);
            m_vfsSelector.addCloseHandler(new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    m_browseButton.enable();
                }
            });
        }
        m_vfsSelector.center();
    }

}
