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

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
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
implements I_CmsFormWidget, HasValueChangeHandlers<String>, HasFocusHandlers, I_CmsTruncable {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsSelectBoxUiBinder extends UiBinder<Panel, A_CmsSelectBox<?>> {
        // binder interface
    }

    /** The layout bundle. */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The UiBinder instance used for this widget. */
    private static I_CmsSelectBoxUiBinder uiBinder = GWT.create(I_CmsSelectBoxUiBinder.class);

    /** Error widget. */
    @UiField
    protected CmsErrorWidget m_error;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** Handler registration for mouse wheel handlers. */
    protected HandlerRegistration m_mousewheelRegistration;

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

    /** The maximum cell width. */
    private int m_maxCellWidth;

    /** The value to test the popup resize behaviour.*/
    private boolean m_resizePopup = true;

    /** The text metrics prefix. */
    private String m_textMetricsPrefix;

    /** The widget width for truncation. */
    private int m_widgetWidth;

    /** Handler registration for the window resize handler. */
    private HandlerRegistration m_windowResizeHandlerReg;

    /**
     * Creates a new select box.<p>
     */
    public A_CmsSelectBox() {

        m_eventBus = new SimpleEventBus();
        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
        m_selectBoxState = new CmsStyleVariable(m_opener);
        m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        m_selectorState = new CmsStyleVariable(m_popup);
        m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());

        m_opener.addStyleName(CSS.selectBoxSelected());
        addHoverHandlers(m_opener);
        addMainPanelHoverHandlers(m_panel);
        m_openClose = new CmsPushButton(I_CmsButton.TRIANGLE_RIGHT, I_CmsButton.TRIANGLE_DOWN);
        m_openClose.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_openClose.setSize(Size.small);
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

        m_popup.addStyleName(CSS.selectBoxSelector());
        m_popup.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
        m_popup.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
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
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
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
     * Adds a widget.<p>
     *
     * @param widget the widget to add
     */
    public void addWidget(Widget widget) {

        m_panel.add(widget);
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

        if (m_selectedValue == null) {
            return "";
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
     * Returns the selector of this widget.<p>
     *
     * @return the selector of this widget
     */
    public Panel getSelectorPopup() {

        return m_popup;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // Should not act on button if disabled.
        if (!isEnabled()) {
            return;
        }
        super.onBrowserEvent(event);
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
        getElement().setPropertyBoolean("disabled", !enabled);
        m_openClose.setEnabled(enabled);
        if (enabled) {
            removeStyleName(CSS.selectBoxDisabled());
        } else {
            addStyleName(CSS.selectBoxDisabled());
        }
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

        setFormValue(value, false);
    }

    /**
     * Sets the form value of this select box.<p>
     *
     * @param value the new value
     * @param fireEvents true if change events should be fired
     */
    public void setFormValue(Object value, boolean fireEvents) {

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
            onValueSelect(strValue, fireEvents);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String formValue) {

        setFormValue(formValue);
    }

    /**
     * Sets the behavior of the popup if the input is bigger than the selectbox itself.
     *
     * @param resize <code>true</code> to resize
     */
    public void setPopupResize(boolean resize) {

        m_resizePopup = resize;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        m_textMetricsPrefix = textMetricsPrefix;
        m_widgetWidth = widgetWidth;
        truncateOpener(textMetricsPrefix, widgetWidth);
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
     * Initializes the selector width.<p>
     */
    protected void initMaxCellWidth() {

        m_maxCellWidth = m_opener.getOffsetWidth() - 2 /*border*/;
        for (Widget widget : m_selector) {
            if (widget instanceof A_CmsSelectCell) {
                int cellWidth = ((A_CmsSelectCell)widget).getRequiredWidth();
                CmsDebugLog.getInstance().printLine(
                    "Measure for " + ((A_CmsSelectCell)widget).getElement().getInnerText() + ": " + cellWidth);
                if (cellWidth > m_maxCellWidth) {
                    m_maxCellWidth = cellWidth;
                }
            }
        }
    }

    /**
     * The implementation of this method should initialize the opener of the select box.<p>
     */
    protected abstract void initOpener();

    /**
     * @see com.google.gwt.user.client.ui.Composite#onDetach()
     */
    @Override
    protected void onDetach() {

        super.onDetach();
        removeWindowResizeHandler();
    }

    /**
     * Handles the focus event on the opener.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_opener")
    protected void onFocus(FocusEvent event) {

        CmsDomUtil.fireFocusEvent(this);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        removeWindowResizeHandler();
        m_windowResizeHandlerReg = Window.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                close();
            }
        });

        m_mousewheelRegistration = RootPanel.get().addDomHandler(new MouseWheelHandler() {

            public void onMouseWheel(MouseWheelEvent event) {

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        positionPopup();
                    }
                });

            }
        }, MouseWheelEvent.getType());

    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onUnload()
     */
    @Override
    protected void onUnload() {

        super.onUnload();
        if (m_mousewheelRegistration != null) {
            m_mousewheelRegistration.removeHandler();
        }
    }

    /**
     * This method is called when a value is selected.<p>
     *
     * @param value the selected value
     */
    protected void onValueSelect(String value) {

        onValueSelect(value, true);
    }

    /**
     * Internal handler method which is called when a new value is selected.<p>
     *
     * @param value the new value
     * @param fireEvents true if change events should be fired
     */
    protected void onValueSelect(String value, boolean fireEvents) {

        String oldValue = m_selectedValue;
        selectValue(value);
        if (fireEvents) {
            if ((oldValue == null) || !oldValue.equals(value)) {
                // fire value change only if the the value really changed
                ValueChangeEvent.<String> fire(this, value);
            }
        }
    }

    /**
     * Internal method which is called when the selector is opened.<p>
     */
    protected void open() {

        if (!m_enabled) {
            return;
        }
        m_openClose.setDown(true);
        if (m_maxCellWidth == 0) {
            initMaxCellWidth();
        }
        int selectorWidth = m_maxCellWidth;
        // should not be any wider than the actual window
        int windowWidth = Window.getClientWidth();
        if (m_maxCellWidth > windowWidth) {
            selectorWidth = windowWidth - 10;
        }
        // if the resize option is deactivated the popup should not be wider than the selectbox.
        // Default its true.
        if (!m_resizePopup) {
            selectorWidth = m_opener.getOffsetWidth() - 2;
        }
        m_popup.setWidth(selectorWidth + "px");
        m_popup.setWidget(m_selector);
        m_popup.show();

        positionPopup();
    }

    /**
     * Deinstalls the window resize handler.<p>
     */
    protected void removeWindowResizeHandler() {

        if (m_windowResizeHandlerReg != null) {
            m_windowResizeHandlerReg.removeHandler();
            m_windowResizeHandlerReg = null;
        }
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
     * Positions the select popup.<p>
     */
    void positionPopup() {

        if (m_popup.isShowing()) {
            int width = m_popup.getOffsetWidth();

            int openerHeight = CmsDomUtil.getCurrentStyleInt(m_opener.getElement(), CmsDomUtil.Style.height);
            int popupHeight = m_popup.getOffsetHeight();
            int dx = 0;
            if (width > (m_opener.getOffsetWidth())) {
                int spaceOnTheRight = (Window.getClientWidth() + Window.getScrollLeft())
                    - m_opener.getAbsoluteLeft()
                    - width;
                dx = spaceOnTheRight < 0 ? spaceOnTheRight : 0;
            }
            // Calculate top position for the popup
            int top = m_opener.getAbsoluteTop();

            // Make sure scrolling is taken into account, since
            // box.getAbsoluteTop() takes scrolling into account.
            int windowTop = Window.getScrollTop();
            int windowBottom = Window.getScrollTop() + Window.getClientHeight();

            // Distance from the top edge of the window to the top edge of the
            // text box
            int distanceFromWindowTop = top - windowTop;

            // Distance from the bottom edge of the window to the bottom edge of
            // the text box
            int distanceToWindowBottom = windowBottom - (top + m_opener.getOffsetHeight());

            // If there is not enough space for the popup's height below the text
            // box and there IS enough space for the popup's height above the text
            // box, then then position the popup above the text box. However, if there
            // is not enough space on either side, then stick with displaying the
            // popup below the text box.
            boolean displayAbove = (distanceFromWindowTop > distanceToWindowBottom)
                && (distanceToWindowBottom < popupHeight);

            // in case there is not enough space, add a scroll panel to the selector popup
            if ((displayAbove && (distanceFromWindowTop < popupHeight))
                || (!displayAbove && (distanceToWindowBottom < popupHeight))) {
                setScrollingSelector((displayAbove ? distanceFromWindowTop : distanceToWindowBottom) - 10);
                popupHeight = m_popup.getOffsetHeight();
            }

            if (displayAbove) {
                // Position above the text box
                CmsDomUtil.positionElement(m_popup.getElement(), m_panel.getElement(), dx, -(popupHeight - 2));
                m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
                m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
            } else {
                CmsDomUtil.positionElement(m_popup.getElement(), m_panel.getElement(), dx, openerHeight - 1);
                m_selectBoxState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
                m_selectorState.setValue(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
            }
        }
    }

    /**
     * Helper method for adding event handlers for a 'hover' effect to the opener.<p>
     *
     * @param panel the opener
     */
    private void addHoverHandlers(FocusPanel panel) {

        final CmsStyleVariable hoverVar = new CmsStyleVariable(panel);
        hoverVar.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerNoHover());
        panel.addMouseOverHandler(new MouseOverHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
             */
            public void onMouseOver(MouseOverEvent event) {

                hoverVar.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerHover());
            }
        });

        panel.addMouseOutHandler(new MouseOutHandler() {

            /**
             * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
             */
            public void onMouseOut(MouseOutEvent event) {

                hoverVar.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerNoHover());
            }
        });

    }

    /**
     * Helper method for adding event handlers for a 'hover' effect to the main panel.<p>
     *
     * @param panel the main panel
     */
    private void addMainPanelHoverHandlers(Panel panel) {

        final CmsStyleVariable hoverPanel = new CmsStyleVariable(panel);
        hoverPanel.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerNoHover());
        panel.addDomHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent event) {

                hoverPanel.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerHover());

            }
        }, MouseOverEvent.getType());
        panel.addDomHandler(new MouseOutHandler() {

            public void onMouseOut(MouseOutEvent event) {

                hoverPanel.setValue(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().openerNoHover());

            }
        }, MouseOutEvent.getType());
    }

    /**
     * Initializes the event handlers of a select cell.<p>
     *
     * @param cell the select cell whose event handlers should be initialized
     */
    private void initSelectCell(final A_CmsSelectCell cell) {

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
     * Adds a scroll panel to the selector popup.<p>
     *
     * @param availableHeight the available popup height
     */
    private void setScrollingSelector(int availableHeight) {

        CmsScrollPanel panel = GWT.create(CmsScrollPanel.class);
        panel.getElement().getStyle().setHeight(availableHeight, Unit.PX);
        panel.setWidget(m_selector);
        m_popup.setWidget(panel);
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
