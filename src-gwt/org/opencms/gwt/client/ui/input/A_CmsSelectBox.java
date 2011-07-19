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

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract superclass for select box widgets.<p>
 * 
 * @param <OPTION> the widget type of the select options 
 * 
 * @since 8.0.0
 * 
 */
public abstract class A_CmsSelectBox<OPTION extends A_CmsSelectCell> extends Composite
implements I_CmsFormWidget, HasValueChangeHandlers<String>, I_CmsTruncable {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsSelectBoxUiBinder extends UiBinder<Panel, A_CmsSelectBox<?>> {
        // binder interface
    }

    /** The layout bundle. */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** Text metrics key. */
    private static final String TM_OPTION = "Option";

    /** The UiBinder instance used for this widget. */
    private static I_CmsSelectBoxUiBinder uiBinder = GWT.create(I_CmsSelectBoxUiBinder.class);

    /** Error widget. */
    @UiField
    protected CmsErrorWidget m_error;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The open-close button. */
    protected CmsPushButton m_openClose;

    /** The opener widget. */
    @UiField
    protected FocusPanel m_opener;

    /**  Container for the opener and error widget. */
    @UiField
    protected Panel m_panel;

    /** The popup panel inside which the selector will be shown.<p> */
    protected PopupPanel m_popup = new PopupPanel(true);

    /** Style of the select box widget. */
    protected final CmsStyleVariable m_selectBoxState;

    /** The map of select options. */
    protected Map<String, OPTION> m_selectCells = new HashMap<String, OPTION>();

    /** The value of the currently selected option. */
    protected String m_selectedValue;

    /** The selector which contains the select options. */
    protected Panel m_selector = new FlowPanel();

    /** Style of the select box widget. */
    protected final CmsStyleVariable m_selectorState;

    /** Flag indicating whether this widget is enabled. */
    private boolean m_enabled = true;

    /** The value of the first select option. */
    private String m_firstValue;

    /** The text metrics prefix. */
    private String m_textMetricsPrefix;

    /** The widget width for truncation. */
    private int m_widgetWidth;

    /**
     * Creates a new select box.<p>
     */
    public A_CmsSelectBox() {

        m_eventBus = new SimpleEventBus();
        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
        m_selectBoxState = new CmsStyleVariable(m_opener);
        m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        m_selectorState = new CmsStyleVariable(m_selector);
        m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());

        m_opener.addStyleName(CSS.selectBoxSelected());
        addHoverHandlers(m_opener);

        m_openClose = new CmsPushButton(
            I_CmsImageBundle.INSTANCE.style().triangleRight(),
            I_CmsImageBundle.INSTANCE.style().triangleDown());
        m_openClose.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_openClose.addStyleName(CSS.selectIcon());
        m_panel.add(m_openClose);
        m_openClose.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (m_popup.isShowing()) {
                    close();
                } else {
                    open();
                }
            }
        });

        m_popup.setWidget(m_selector);
        m_popup.addStyleName(CSS.selectorPopup());
        m_popup.addAutoHidePartner(m_panel.getElement());

        m_selector.setStyleName(CSS.selectBoxSelector());
        m_selector.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
        m_selector.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
        m_popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            /**
             * @see CloseHandler#onClose(CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> e) {

                close();
            }
        });
        initOpener();
    }

    /**
     * Adds a new select option to the select box.<p>
     * 
     * @param cell the widget representing the select option 
     */
    public void addOption(OPTION cell) {

        String value = cell.getValue();
        boolean first = m_selectCells.isEmpty();
        m_selectCells.put(value, cell);

        m_selector.add(cell);
        if (first) {
            selectValue(value);
            m_firstValue = value;
        }
        initSelectCell(cell);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        return super.addHandler(handler, ValueChangeEvent.getType());
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

        if (m_selectedValue.equals("")) {
            return null;
        }
        return m_selectedValue;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        close();
        onValueSelect(m_firstValue);
    }

    /**
     * Helper method to set the current selected option.<p>
     * 
     * This method does not trigger the "value changed" event.<p>
     * 
     * @param value the new value
     */
    public void selectValue(String value) {

        if (m_selectCells.get(value) == null) {
            return;
        }

        updateOpener(value);
        if (m_textMetricsPrefix != null) {
            truncate(m_textMetricsPrefix, m_widgetWidth);
        }
        m_selectedValue = value;
        close();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        close();
        m_enabled = enabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the form value of this select box.<p>
     * 
     * @param value the new value 
     */
    public void setFormValue(Object value) {

        if (value == null) {
            value = "";
        }
        if (!"".equals(value) && !m_selectCells.containsKey(value)) {
            OPTION option = createUnknownOption((String)value);
            if (option != null) {
                addOption(option);
            }
        }
        if (value instanceof String) {
            String strValue = (String)value;
            this.onValueSelect(strValue);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String formValue) {

        setFormValue(formValue);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        m_textMetricsPrefix = textMetricsPrefix;
        m_widgetWidth = widgetWidth;
        truncateOpener(textMetricsPrefix, widgetWidth);
        int labelWidth = widgetWidth - 2 - 5; // 2px border left/right + 5px left margin
        for (Widget widget : m_selector) {
            if (widget instanceof I_CmsTruncable) {
                ((I_CmsTruncable)widget).truncate(textMetricsPrefix + TM_OPTION, labelWidth);
            }
        }
    }

    /**
     * Internal helper method for clearing the select options.<p>
     */
    protected void clearItems() {

        m_selectCells.clear();
        m_selector.clear();
        m_selectedValue = null;
    }

    /**
     * Internal method which is called when the selector is closed.<p> 
     */
    protected void close() {

        if (!m_enabled) {
            return;
        }
        m_openClose.setDown(false);
        m_popup.hide();
        m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
    }

    /**
     * Internal method to create a select option for an unknown value.<p>
     * 
     * @param value the value for which to create the option 
     * 
     * @return the new option
     */
    protected abstract OPTION createUnknownOption(String value);

    /**
     * Handle clicks on the opener.<p>
     * 
     * @param e the click event
     */
    @UiHandler("m_opener")
    protected void doClickOpener(ClickEvent e) {

        toggleOpen();
    }

    /** 
     * The implementation of this method should initialize the opener of the select box.<p>
     */
    protected abstract void initOpener();

    /**
     * Internal handler method which is called when a new value is selected.<p>
     * 
     * @param value the new value
     */
    protected void onValueSelect(String value) {

        selectValue(value);
        ValueChangeEvent.<String> fire(this, value);
    }

    /**
     * Internal method which is called when the selector is opened.<p>
     */
    protected void open() {

        if (!m_enabled) {
            return;
        }

        m_openClose.setDown(true);
        int newWidth = m_opener.getOffsetWidth() - 2 /*border*/;
        m_popup.setWidth(newWidth + "px");
        m_popup.show();
        int panelTop = m_panel.getElement().getAbsoluteTop();
        int openerHeight = CmsDomUtil.getCurrentStyleInt(m_opener.getElement(), CmsDomUtil.Style.height);
        int popupHeight = m_popup.getOffsetHeight();
        if (((Window.getClientHeight() - (panelTop + openerHeight)) < popupHeight) && (panelTop > popupHeight)) {
            CmsDomUtil.positionElement(m_popup.getElement(), m_panel.getElement(), 0, -(popupHeight - 2));
            m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
            m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
        } else {
            CmsDomUtil.positionElement(m_popup.getElement(), m_panel.getElement(), 0, openerHeight);
            m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
            m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
        }
        // m_selectBoxState.setValue(CSS.selectBoxOpen());
    }

    /**
     * Abstract method whose implementation should truncate the opener widget(s).<p>
     * 
     * @param prefix the text metrics prefix
     * @param width the widget width 
     */
    protected abstract void truncateOpener(String prefix, int width);

    /** 
     * The implementation of this method should update the opener when a new value is selected by the user.<p>
     * 
     * @param newValue the value selected by the user
     */
    protected abstract void updateOpener(String newValue);

    /**
     * Helper method for adding event handlers for a 'hover' effect to the opener.<p>
     * 
     * @param panel the opener
     */
    private void addHoverHandlers(FocusPanel panel) {

        final CmsStyleVariable hoverVar = new CmsStyleVariable(panel);
        hoverVar.setValue(CSS.openerNoHover());
        panel.addMouseOverHandler(new MouseOverHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
             */
            public void onMouseOver(MouseOverEvent event) {

                hoverVar.setValue(CSS.openerHover());

            }
        });

        panel.addMouseOutHandler(new MouseOutHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
             */
            public void onMouseOut(MouseOutEvent event) {

                hoverVar.setValue(CSS.openerNoHover());
            }
        });

    }

    /**
     * Initializes the event handlers of a select cell.<p>
     * 
     * @param cell the select cell whose event handlers should be initialized 
     */
    private void initSelectCell(final A_CmsSelectCell cell) {

        cell.addStyleName(CSS.selectBoxCell());

        cell.registerDomHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                onValueSelect(cell.getValue());
                cell.removeStyleName(CSS.selectHover());
            }
        }, ClickEvent.getType());

        cell.registerDomHandler(new MouseOverHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
             */
            public void onMouseOver(MouseOverEvent e) {

                cell.addStyleName(CSS.selectHover());

            }
        }, MouseOverEvent.getType());

        cell.registerDomHandler(new MouseOutHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
             */
            public void onMouseOut(MouseOutEvent e) {

                cell.removeStyleName(CSS.selectHover());
            }
        }, MouseOutEvent.getType());

    }

    /**
     * Toggles the state of the selector popup between 'open' and 'closed'.<p>
     */
    private void toggleOpen() {

        if (!m_enabled) {
            return;
        }
        if (m_popup.isShowing()) {
            close();
        } else {
            open();
        }
    }
}
