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
import org.opencms.gwt.client.I_HasResizeOnShow;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Basic text area widget for forms.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsTextArea extends Composite
implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String>, HasResizeHandlers, HasFocusHandlers,
I_HasResizeOnShow {

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "textarea";

    /** The default rows set. */
    int m_defaultRows;

    /** The fade panel. */
    Panel m_fadePanel = new SimplePanel();

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The internal text area widget used by this widget. */
    TextArea m_textArea = new TextArea();

    /** The container for the text area. */
    CmsScrollPanel m_textAreaContainer = GWT.create(CmsScrollPanel.class);

    /** Overlay to disable the text area. */
    private Element m_disabledOverlay;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The previous value. */
    private String m_previousValue;

    /** A timer to schedule the widget size recalculation. */
    private Timer m_updateSizeTimer;

    /**
     * Text area widgets for ADE forms.<p>
     */
    public CmsTextArea() {

        super();
        initWidget(m_panel);
        m_panel.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().textArea());
        m_textAreaContainer.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().textAreaBoxPanel());
        m_textArea.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().textAreaBox());
        m_fadePanel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().fader());
        m_panel.add(m_textAreaContainer);
        m_textAreaContainer.setResizable(true);
        m_textAreaContainer.getElement().getStyle().setHeight(m_textArea.getOffsetHeight(), Unit.PX);
        m_textAreaContainer.add(m_textArea);
        m_fadePanel.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_textArea.setFocus(true);
            }
        }, ClickEvent.getType());

        m_textArea.addKeyUpHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent event) {

                scheduleResize();
                fireValueChangedEvent(false);
            }

        });

        m_textArea.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireValueChangedEvent(false);
            }
        });

        m_panel.add(m_error);
        m_textAreaContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        m_textArea.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                m_panel.remove(m_fadePanel);
                CmsDomUtil.fireFocusEvent(CmsTextArea.this);
            }
        });
        m_textArea.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                showFadePanelIfNeeded();
                m_textAreaContainer.scrollToTop();

            }
        });
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

                return new CmsTextArea();
            }
        });
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return m_textAreaContainer.addResizeHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
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

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_textArea.getText() == null) {
            return "";
        }
        return m_textArea.getText();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the text contained in the text area.<p>
     * 
     * @return the text in the text area
     */
    public String getText() {

        return m_textArea.getText();
    }

    /**
     * Returns the textarea of this widget.<p>
     * 
     * @return the textarea
     */
    public TextArea getTextArea() {

        return m_textArea;
    }

    /**
     * Returns the text area container of this widget.<p>
     * 
     * @return the text area container
     */
    public CmsScrollPanel getTextAreaContainer() {

        return m_textAreaContainer;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textArea.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textArea.setText("");
    }

    /**
     * @see org.opencms.gwt.client.I_HasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        m_textAreaContainer.onResizeDescendant();
        updateContentSize();
        showFadePanelIfNeeded();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textArea.setEnabled(enabled);
        // hide / show resize handle
        m_textAreaContainer.setResizable(enabled);
        if (enabled) {
            if (m_disabledOverlay != null) {
                m_disabledOverlay.removeFromParent();
                m_disabledOverlay = null;
            }
        } else {
            if (m_disabledOverlay == null) {
                m_disabledOverlay = DOM.createDiv();
                m_disabledOverlay.setClassName(I_CmsInputLayoutBundle.INSTANCE.inputCss().disableTextArea());
                m_panel.getElement().appendChild(m_disabledOverlay);
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the value of the widget.<p>
     * 
     * @param value the new value 
     */
    public void setFormValue(Object value) {

        if (value == null) {
            value = "";
        }
        if (value instanceof String) {
            m_previousValue = (String)value;
            m_textArea.setText(m_previousValue);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String newValue) {

        setFormValue(newValue);
    }

    /**
     * Sets the name of the input field.<p>
     * 
     * @param name of the input field
     * 
     * */
    public void setName(String name) {

        m_textArea.setName(name);

    }

    /**
     * Sets the height of this textarea.<p>
     * 
     * @param rows the value of rows should be shown
     */
    public void setRows(int rows) {

        m_defaultRows = rows;
        double height_scroll = (rows * 17.95) + 8;
        m_textArea.setVisibleLines(rows);
        m_textAreaContainer.setHeight(height_scroll + "px");
        m_textAreaContainer.setDefaultHeight(height_scroll);
        m_textAreaContainer.onResizeDescendant();
    }

    /**
     * Sets the height of this textarea. Especial for the image Gallery.<p>
     * 
     * @param rows the value of rows should be shown
     */
    public void setRowsGallery(int rows) {

        m_defaultRows = rows;
        double height_scroll = (rows * 17.95) + 8 + 5;
        m_textArea.setVisibleLines(rows);
        m_textAreaContainer.setHeight(height_scroll + "px");
        m_textAreaContainer.setDefaultHeight(height_scroll);
        m_textAreaContainer.onResizeDescendant();
    }

    /**
     * Sets the text in the text area.<p>
     * 
     * @param text the new text
     */
    public void setText(String text) {

        m_textArea.setText(text);
    }

    /** 
     * Helper method for firing a 'value changed' event.<p>
     * 
     * @param force if <true, some additional information will be added to the event to ask event handlers to not perform any validation directly
     */
    protected void fireValueChangedEvent(boolean force) {

        if (force || !getFormValueAsString().equals(m_previousValue)) {
            m_previousValue = getFormValueAsString();
            ValueChangeEvent.fire(this, m_previousValue);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                resizeOnShow();
            }
        });
    }

    /**
     * Schedules resizing the widget.<p>
     */
    protected void scheduleResize() {

        if (m_updateSizeTimer != null) {
            m_updateSizeTimer.cancel();
        }
        m_updateSizeTimer = new Timer() {

            @Override
            public void run() {

                updateContentSize();
            }
        };
        m_updateSizeTimer.schedule(300);
    }

    /**
     * Shows the fade panel if the text area content exceeds the visible area.<p> 
     */
    protected void showFadePanelIfNeeded() {

        if (m_defaultRows < m_textArea.getVisibleLines()) {
            m_panel.add(m_fadePanel);
        }
    }

    /**
     * Updates the text area height according to the current text content.<p>
     */
    protected void updateContentSize() {

        int offsetHeight = m_textArea.getOffsetHeight();
        // sanity check: don't do anything, if the measured height doesn't make any sense
        if (offsetHeight > 5) {
            int visibleRows = m_textArea.getVisibleLines();
            double lineHeight = (1.00 * offsetHeight) / visibleRows;
            // store the current scroll position
            int scrollPosition = m_textAreaContainer.getVerticalScrollPosition()
                + m_textArea.getElement().getScrollTop();
            if (visibleRows != m_defaultRows) {
                m_textArea.setVisibleLines(m_defaultRows);
            }
            int rows = (int)Math.ceil(m_textArea.getElement().getScrollHeight() / lineHeight) + 1;
            if (rows < m_defaultRows) {
                rows = m_defaultRows;
            }
            m_textArea.setVisibleLines(rows);
            // restore the scroll position
            m_textAreaContainer.setVerticalScrollPosition(scrollPosition);
            if (visibleRows != rows) {
                m_textAreaContainer.onResizeDescendant();
            }
        }
    }
}
