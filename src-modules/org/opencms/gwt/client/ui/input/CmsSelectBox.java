/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsSelectBox.java,v $
 * Date   : $Date: 2010/04/14 14:16:20 $
 * Version: $Revision: 1.14 $
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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.CmsImageButton;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Widget for selecting one of multiple items from a drop-down list which opens
 * after the user clicks on the widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsSelectBox extends Composite implements I_CmsFormWidget, HasValueChangeHandlers<String> {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsSelectBoxUiBinder extends UiBinder<Panel, CmsSelectBox> {
        // binder interface
    }

    /**
     * This class represents a single select option in the selector of the select box.
     */
    private class CmsSelectCell extends CmsLabel {

        /** The text of the select option. */
        String m_text;

        /** The value of the select option. */
        String m_value;

        /**
         * Creates a new select cell.<p>
         * 
         * @param value the value of the select option
         * @param text the text to display for the select option
         */
        public CmsSelectCell(String value, String text) {

            super();
            final CmsSelectCell self = this;
            m_value = value;
            m_text = text;
            setText(m_text);

            addStyleName(CSS.selectBoxCell());
            addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent e) {

                    onValueSelect(m_value);
                    self.removeStyleName(CSS.selectHover());
                }
            });

            addMouseOverHandler(new MouseOverHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
                 */
                public void onMouseOver(MouseOverEvent e) {

                    self.addStyleName(CSS.selectHover());

                }
            });

            addMouseOutHandler(new MouseOutHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
                 */
                public void onMouseOut(MouseOutEvent e) {

                    self.removeStyleName(CSS.selectHover());
                }
            });
        }
    }

    /** The layout bundle. */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The UiBinder instance used for this widget. */
    private static I_CmsSelectBoxUiBinder uiBinder = GWT.create(I_CmsSelectBoxUiBinder.class);

    /** Error widget. */
    @UiField
    protected CmsErrorWidget m_error;

    /** Handler manager for this widget's events. */
    protected final HandlerManager m_handlerManager = new HandlerManager(this);

    /** The open-close button. */
    protected CmsImageButton m_openClose;

    /** The opener widget. */
    @UiField
    protected FocusPanel m_opener;

    /** The field in the opener which contains the currently selected option. */
    @UiField
    protected CmsLabel m_openerLabel;

    /**  Container for the opener and error widget. */
    @UiField
    protected Panel m_panel;

    /** The popup panel inside which the selector will be shown.<p> */
    protected PopupPanel m_popup = new PopupPanel(true);

    /** Style of the select box widget. */
    protected final CmsStyleVariable m_selectBoxState;

    /** Flag indicating whether this widget is enabled. */
    private boolean m_enabled = true;

    /** The value of the first select option. */
    private String m_firstValue;

    /** The value of the currently selected option. */
    private String m_selectedValue;

    /** The selector which contains the select options. */
    private Panel m_selector = new VerticalPanel();

    /** The labels for each of the select options. */
    private final Map<String, String> m_valueLabels = new HashMap<String, String>();

    /**
     * Creates a new select box.<p>
     */
    public CmsSelectBox() {

        m_panel = uiBinder.createAndBindUi(this);
        initWidget(m_panel);
        m_openerLabel.addStyleName(CSS.selectBoxOpener());
        m_selectBoxState = new CmsStyleVariable(m_opener, m_selector);
        m_selectBoxState.setValue(CSS.selectBoxClosed());

        m_opener.addStyleName(CSS.selectBoxSelected());
        m_openClose = new CmsImageButton(CmsImageButton.Icon.triangle_1_e, CmsImageButton.Icon.triangle_1_s, false);
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

        m_selector.setStyleName(CSS.selectBoxSelector());
        m_popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            /**
             * @see CloseHandler#onClose(CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> e) {

                close();
            }
        });
    }

    /**
     * Constructs a new select box from a list of key-value pairs.<p>
     * 
     * The first component of each pair is the option value, the second is the text to be displayed for the option value.<p>
     * 
     * @param items the items
     */
    public CmsSelectBox(List<CmsPair<String, String>> items) {

        this();
        setItems(items);
    }

    /**
     * Positions an element in the DOM relative to another element.<p>
     * 
     * @param elem the element to position
     * @param referenceElement the element relative to which the first element should be positioned
     * @param dx the x offset relative to the reference element
     * @param dy the y offset relative to the reference element 
     */
    private static void positionElement(Element elem, Element referenceElement, int dx, int dy) {

        Style style = elem.getStyle();
        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        int myX = elem.getAbsoluteLeft();
        int myY = elem.getAbsoluteTop();
        int refX = referenceElement.getAbsoluteLeft();
        int refY = referenceElement.getAbsoluteTop();
        int newX = refX - myX + dx;
        int newY = refY - myY + dy;
        style.setLeft(newX, Unit.PX);
        style.setTop(newY, Unit.PX);
    }

    /**
     * Adds a new selection cell.<p>
     * 
     * @param value the value of the select option 
     * @param text the text to be displayed for the select option 
     */
    public void addOption(String value, String text) {

        CmsSelectCell cell = new CmsSelectCell(value, text);
        boolean first = m_valueLabels.isEmpty();
        m_valueLabels.put(value, text);
        m_selector.add(cell);
        if (first) {
            selectValue(value);
            m_firstValue = value;
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        m_handlerManager.addHandler(ValueChangeEvent.getType(), handler);
        return new HandlerRegistration() {

            /**
             * @see com.google.gwt.event.shared.HandlerRegistration#removeHandler()
             */
            public void removeHandler() {

                m_handlerManager.removeHandler(ValueChangeEvent.getType(), handler);
            }
        };
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    @Override
    public void fireEvent(GwtEvent<?> event) {

        if (m_handlerManager != null) {
            m_handlerManager.fireEvent(event);
        }
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

        return m_selectedValue;

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

        String label = m_valueLabels.get(value);

        m_openerLabel.setText(label);
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValue(java.lang.Object)
     */
    public void setFormValue(Object value) {

        if (value instanceof String) {
            String strValue = (String)value;
            this.onValueSelect(strValue);
        }
    }

    /**
     * Sets the items as key-value pairs.<p>
     * 
     * The first component of each pair is the option value, the second is the text to be displayed for the option value.<p>
     * 
     * @param items the items
     */
    public void setItems(List<CmsPair<String, String>> items) {

        m_valueLabels.clear();
        m_selector.clear();
        m_selectedValue = null;
        for (CmsPair<String, String> item : items) {
            addOption(item.getFirst(), item.getSecond());
        }
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
        m_selectBoxState.setValue(CSS.selectBoxClosed());
    }

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
        m_popup.setWidth(2 /* left/right border */+ m_opener.getElement().getClientWidth() + "px");
        m_popup.show();
        positionElement(m_popup.getElement(), m_panel.getElement(), 0, m_opener.getElement().getClientHeight());
        m_selectBoxState.setValue(CSS.selectBoxOpen());
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
