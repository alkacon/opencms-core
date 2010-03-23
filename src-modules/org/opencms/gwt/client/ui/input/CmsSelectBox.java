/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsSelectBox.java,v $
 * Date   : $Date: 2010/03/23 10:34:32 $
 * Version: $Revision: 1.7 $
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for selecting one of multiple items from a drop-down list which opens
 * after the user clicks on the widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsSelectBox extends Composite implements I_CmsFormWidget, HasValueChangeHandlers<String> {

    /**
     * Mode constants for the select box options. 
     */
    public enum Mode {
        /** HTML mode - the text of the option will be literally used as its HTML. */
        HTML,

        /** text mode - all HTML will be escaped. */
        TEXT
    }

    /**
     * The UI Binder interface for this widget.<p>
     */
    interface I_CmsSelectBoxUiBinder extends UiBinder<Widget, CmsSelectBox> {
        // binder interface
    }

    /**
     * This class represents a single select option in the selector of the select box.
     * 
     */
    private class CmsSelectCell extends HTML {

        String m_text;

        String m_value;

        /**
         * Creates a new select cell.<p>
         * 
         * @param value the value of the select option
         * @param text the text to display for the select option
         */
        public CmsSelectCell(String value, String text) {

            final CmsSelectCell self = this;
            m_value = value;
            m_text = text;
            if (m_mode == Mode.HTML) {

                setHTML(m_text);
            } else {
                setText(m_text);
            }

            addStyleName(CSS.selectBoxCell());

            addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent e) {

                    onValueSelect(m_value, m_text);
                    self.removeStyleName(CSS.selectHover());
                }
            });

            addMouseOverHandler(new MouseOverHandler() {

                public void onMouseOver(MouseOverEvent e) {

                    self.addStyleName(CSS.selectHover());

                }
            });

            addMouseOutHandler(new MouseOutHandler() {

                public void onMouseOut(MouseOutEvent e) {

                    self.removeStyleName(CSS.selectHover());

                }
            });
        }

    }

    /**
     * The layout bundle.<p>
     */
    static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    private static I_CmsSelectBoxUiBinder uiBinder = GWT.create(I_CmsSelectBoxUiBinder.class);

    /** The arrow shown in the opener. */
    @UiField
    protected HTML m_arrow;

    /** Error widget. */
    @UiField
    protected CmsErrorWidget m_error;

    /** Handler manager for this widget's events. */
    protected final HandlerManager m_handlerManager = new HandlerManager(this);

    /** Flag indicating whether the next click on the opener should be ignored. */
    protected boolean m_ignoreNextToggle;

    /** The mode of the select box (text or HTML). */
    protected Mode m_mode;

    /** The opener widget. */
    @UiField
    protected FocusPanel m_opener;

    /** The field in the opener which contains the currently selected option. */
    @UiField
    protected HTML m_openerHtml;

    /**
     * Flag indicating whether the mouse is over the opener.<p>
     * 
     *  This is used to prevent the auto-close feature of the PopupPanel with the other event handlers for the opener.
     */
    protected boolean m_overOpener;

    /**  Container for the opener and error widget. */
    @UiField
    protected Panel m_panel = new FlowPanel();

    /** The popup panel inside which the selector will be shown.<p> */
    protected PopupPanel m_popup = new PopupPanel(true);

    /** Style of the select box widget. */
    protected final CmsStyleVariable m_selectBoxState;

    /** Flag indicating whether this widget is enabled. */
    private boolean m_enabled = true;

    private boolean m_first = true;

    /** The text of the first select option. */
    private String m_firstText;

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
     * 
     * @param mode the mode of the select box (Mode.TEXT or Mode.HTML)
     */
    public CmsSelectBox(Mode mode) {

        m_mode = mode;

        initWidget(uiBinder.createAndBindUi(this));
        m_selectBoxState = new CmsStyleVariable(m_opener, m_selector);
        m_selectBoxState.setValue(CSS.selectBoxClosed());

        m_opener.addStyleName(CSS.selectBoxSelected());
        m_arrow.setStyleName(CSS.selectClosedIcon());
        m_popup.setWidget(m_selector);
        m_popup.addStyleName(CSS.selectorPopup());

        m_selector.setStyleName(CSS.selectBoxSelector());
        m_popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> e) {

                m_arrow.setStyleName(CSS.selectClosedIcon());
                m_selectBoxState.setValue(CSS.selectBoxClosed());

                if (e.isAutoClosed() && m_overOpener) {
                    // user clicked on the opener, so we have to ignore the click event for the opener which will
                    // be fired next.
                    m_ignoreNextToggle = true;
                }

            }
        });

        // attach event handlers so we can track whether the mouse is over the opener
        m_opener.addMouseOverHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent e) {

                m_overOpener = true;
            }
        });
        m_opener.addMouseOutHandler(new MouseOutHandler() {

            public void onMouseOut(MouseOutEvent e) {

                m_overOpener = false;

            }
        });
    }

    /**
     * Constructs a new select box from a list of key-value pairs.
     * 
     * The first component of each pair is the option value, the second is the text to be displayed for the option value. 
     * @param items
     * @param mode the mode of the select box (Mode.TEXT or Mode.HTML)
     */
    public CmsSelectBox(Mode mode, List<CmsPair<String, String>> items) {

        this(mode);
        for (CmsPair<String, String> item : items) {
            addOption(item.getFirst(), item.getSecond());
        }
    }

    /**
     * Constructs a new select box from a map.
     * 
     * The keys of the  map are the select option values, the values are the texts to be displayed for the options.
     *
     * @param mode the mode
     * @param items the map of options 
     */
    public CmsSelectBox(Mode mode, Map<String, String> items) {

        this(mode);
        for (Map.Entry<String, String> item : items.entrySet()) {
            addOption(item.getKey(), item.getValue());
        }
    }

    static {
        CSS.ensureInjected();
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
        m_valueLabels.put(value, text);
        m_selector.add(cell);
        if (m_first) {
            selectValueInternal(value, text);
            m_firstValue = value;
            m_firstText = text;
        }
        m_first = false;

    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        m_handlerManager.addHandler(ValueChangeEvent.getType(), handler);
        return new HandlerRegistration() {

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
        onValueSelect(m_firstValue, m_firstText);
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
            this.onValueSelect(strValue, m_valueLabels.get(strValue));
        }
    }

    /**
     * Internal helper method to set the current selected option.<p>
     * 
     * @param value the new value
     * @param label the text corresponding to a new label
     */
    protected void selectValueInternal(String value, String label) {

        if (m_mode == Mode.HTML) {
            m_openerHtml.setHTML(label);
        } else {
            m_openerHtml.setText(label);
        }
        m_selectedValue = value;
        close();
    }

    /**
     * Handle clicks on the opener.<p>
     * @param e the click event
     */
    @UiHandler("m_opener")
    void doClickOpener(ClickEvent e) {

        toggleOpen();
    }

    /**
     * Internal handler method which is called when a new value is selected.<p>
     * 
     * @param value the new value
     * @param label the text corresponding to a new label
     */
    void onValueSelect(String value, String label) {

        selectValueInternal(value, label);
        ValueChangeEvent.<String> fire(this, value);
    }

    /**
     * Internal method which is called when the selector is closed.<p> 
     */
    private void close() {

        if (!m_popup.isShowing()) {
            return;
        }
        m_popup.hide();
        m_arrow.setStyleName(CSS.selectClosedIcon());
        m_selectBoxState.setValue(CSS.selectBoxClosed());
    }

    /**
     * Internal method which is called when the selector is opened.<p>
     */
    private void open() {

        if (m_popup.isShowing()) {
            return;
        }
        m_popup.setWidth(2 /* left/right border */+ m_opener.getElement().getClientWidth() + "px");
        m_popup.show();
        positionElement(m_popup.getElement(), m_panel.getElement(), 0, m_opener.getElement().getClientHeight());
        m_arrow.setStyleName(CSS.selectOpenIcon());
        m_selectBoxState.setValue(CSS.selectBoxOpen());
    }

    /**
     * Toggles the state of the selector popup between 'open' and 'closed'.<p>
     */
    private void toggleOpen() {

        if (m_ignoreNextToggle) {
            m_ignoreNextToggle = false;
            return;
        }
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
