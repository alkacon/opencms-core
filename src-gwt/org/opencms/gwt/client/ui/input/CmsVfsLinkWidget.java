/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

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
    //protected CmsVfsSelector m_vfsSelector;

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

                openSelector(getSelectorUrl());
            }
        });

        /*m_textbox.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent arg0) {

                openSelector(getSelectorUrl());
            }
        });*/
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
     * Adds a value change handler.<p>
     *
     * @param handler the handler to add
     */
    public void addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_textbox.addValueChangeHandler(handler);
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

        m_textbox.setFormValueAsString(value);
        m_textbox.fireValueChangedEvent();

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
     * Returns the URL to the link selector popup.<p>
     *
     * @return the URL to the link selector popup
     */
    protected String getSelectorUrl() {

        StringBuffer result = new StringBuffer(128);
        result.append(CmsCoreProvider.get().link("/system/workplace/views/explorer/tree_fs.jsp"));
        result.append("?type=vfswidget&includefiles=true&showsiteselector=true&projectaware=false&treesite=");
        result.append(CmsCoreProvider.get().getSiteRoot());
        return result.toString();
    }

    /**
     * Opens the vfs-selector.<p>
     *
     * @param selectorUrl the URL to the link selector popup
     */
    protected native void openSelector(String selectorUrl)/*-{
        var newwin = $wnd
                .open(
                        selectorUrl,
                        "file_selector",
                        "toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=300,height=450");
        if (newwin != null) {
            if (newwin.opener == null) {
                newwin.opener = $wnd.self;
            }
        } else {
            @org.opencms.gwt.client.util.CmsDomUtil::showPopupBlockerMessage()();
            return;
        }
        newwin.focus();
        var self = this;
        $wnd.setFormValue = function(fileName) {
            self.@org.opencms.gwt.client.ui.input.CmsVfsLinkWidget::setFormValueAsString(Ljava/lang/String;)(fileName);
        }
    }-*/;

}
