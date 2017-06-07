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
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.shared.CmsLinkBean;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Widget for selecting an internal or external link.<p>
 *
 * @since 8.0.0
 */
public class CmsLinkSelector extends Composite implements I_CmsFormWidget, I_CmsHasGhostValue {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsLinkSelectorUiBinder extends UiBinder<Panel, CmsLinkSelector> {
        // binder interface
    }

    /** The widget type. */
    public static final String WIDGET_TYPE = "link";

    /** The ui binder for this widget. */
    private static I_CmsLinkSelectorUiBinder uibinder = GWT.create(I_CmsLinkSelectorUiBinder.class);

    /** The button for editing the link. */
    @UiField
    protected CmsPushButton m_editButton;

    /** The check box for setting the link to external or internal. */
    @UiField
    protected CmsCheckBox m_externalCheckbox;

    /** The row containing the input field and the gallery edit button. */
    @UiField
    protected HorizontalPanel m_inputRow;

    /** The text box containing the current link. */
    @UiField
    protected CmsTextBox m_textbox;

    /** The HTML id of the field. */
    private String m_id;

    /** True if the widget is in "internal" mode. */
    private boolean m_internal;

    /**
     * Constructs a new gallery widget.<p>
     */
    public CmsLinkSelector() {

        initWidget(uibinder.createAndBindUi(this));
        m_id = m_textbox.getId();
        String label = Messages.get().key(Messages.GUI_LINK_CHECKBOX_EXTERNAL_0);
        m_externalCheckbox.setText(label);
        m_inputRow.setCellWidth(m_textbox.getTextBox(), "330px");
        m_editButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_editButton.setImageClass(I_CmsButton.PEN_SMALL);
        m_editButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                openGalleryDialog();
            }
        });
        m_externalCheckbox.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                userChangedExternal(m_externalCheckbox.isChecked());
            }
        });
        setInternal(false);
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

                return new CmsLinkSelector();
            }
        });
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

        String link = m_textbox.getFormValueAsString();
        return m_internal + "|" + link;
    }

    /**
     * Returns the selected link target string.<p>
     *
     * @return the link target string
     */
    public String getLink() {

        return m_textbox.getFormValueAsString();
    }

    /**
     * Returns the selected link as a bean.<p>
     *
     * @return the selected link as a bean
     */
    public CmsLinkBean getLinkBean() {

        String link = m_textbox.getText();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
            return null;
        }
        return new CmsLinkBean(m_textbox.getText(), m_internal);
    }

    /**
     * Returns the text box of this widget.<p>
     *
     * @return the CmsTextBox.
     */
    public CmsTextBox getTextBox() {

        return m_textbox;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return !m_textbox.isReadOnly();
    }

    /**
     * Returns true if the widget is in internal mode.
     *
     * @return true if the widget is in internal mode
     */
    public boolean isInternal() {

        return m_internal;
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textbox.setReadOnly(!enabled);
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

        boolean internal = true;
        String link = "";

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            List<String> tokens = CmsStringUtil.splitAsList(value, "|");
            internal = Boolean.parseBoolean(tokens.get(0));
            link = tokens.get(1);
        }
        setInternal(internal);
        m_textbox.setFormValueAsString(link);

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
     * Sets the widget's mode to internal or external.<p>
     *
     * @param internal if true, sets the mode to internal, else to external
     */
    public void setInternal(boolean internal) {

        m_internal = internal;
        m_textbox.setReadOnly(internal);
        m_editButton.setEnabled(internal);
        m_editButton.getElement().getStyle().setVisibility(internal ? Visibility.VISIBLE : Visibility.HIDDEN);
        //m_editButton.setVisible(internal);
        m_externalCheckbox.setChecked(!internal);
    }

    /**
     * Sets the link target.<p>
     *
     * @param link the link target
     */
    public void setLink(String link) {

        if (link == null) {
            link = "";
        }
        m_textbox.setFormValueAsString(link);
    }

    /**
     * Sets the link from a bean.<p>
     *
     * @param link the link bean
     */
    public void setLinkBean(CmsLinkBean link) {

        if (link == null) {
            link = new CmsLinkBean("", true);
        }
        m_textbox.setFormValueAsString(link.getLink());
        setInternal(link.isInternal());
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    public void setName(String name) {

        m_textbox.setName(name);

    }

    /**
     * Creates the URL for the gallery dialog IFrame.<p>
     *
     * @return the URL for the gallery dialog IFrame
     */
    protected String buildGalleryUrl() {

        String basePath = "/system/workplace/commons/gallery.jsp";
        return CmsCoreProvider.get().link(basePath + "?dialogmode=widget&fieldid=" + m_id);
    }

    /**
     * Internal method which opens the gallery dialog.<p>
     */
    protected void openGalleryDialog() {

        String title = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_GALLERY_SELECT_DIALOG_TITLE_0);
        final CmsFramePopup popup = new CmsFramePopup(title, buildGalleryUrl());
        popup.setCloseHandler(new Runnable() {

            public void run() {

                m_textbox.setGhostMode(false);

            }

        });
        popup.setId(m_id);
        popup.getFrame().setSize("700px", "490px");
        popup.center();

        CmsPushButton button = new CmsPushButton(I_CmsButton.DELETE_SMALL);
        button.setButtonStyle(ButtonStyle.FONT_ICON, null);
        button.setSize(Size.small);
        Style style = button.getElement().getStyle();
        style.setRight(4, Unit.PX);
        style.setTop(0, Unit.PX);
        style.setPosition(Position.ABSOLUTE);
        style.setCursor(Cursor.POINTER);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                popup.hide();
            }
        });

        popup.insertFront(button);
    }

    /**
     * Called if the user changed the state to internal or external.<p>
     *
     * @param external if the new state is "external"
     */
    protected void userChangedExternal(boolean external) {

        if (!external) {
            m_textbox.setFormValueAsString("");
        }
        setInternal(!external);
    }

}
