/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * The string edit widget.<p>
 */
public class CmsStringWidget extends A_CmsEditWidget {

    /** The value to know if the user want to paste something. */
    protected boolean m_paste;

    /** Indicating if the widget is active. */
    private boolean m_active;

    /** The value changed handler initialized flag. */
    private boolean m_valueChangeHandlerInitialized;

    /**
     * Constructor.<p>
     */
    public CmsStringWidget() {

        this(DOM.createDiv());
    }

    /**
     * Constructor wrapping a specific DOM element.<p>
     * 
     * @param element the element to wrap
     */
    public CmsStringWidget(Element element) {

        super(element);
        init();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        // Initialization code
        if (!m_valueChangeHandlerInitialized) {
            m_valueChangeHandlerInitialized = true;
            addDomHandler(new KeyDownHandler() {

                /** The text selection range. */
                protected JavaScriptObject m_range;

                /** The Element of this widget. */
                protected com.google.gwt.dom.client.Element m_element;

                /** Helper text area to store the text that should be pasted. */
                protected TextArea m_helpfield;

                public void onKeyDown(KeyDownEvent event) {

                    // check if something was pasted to the field
                    if (event.isShiftKeyDown() || event.isControlKeyDown()) {
                        int charCode = event.getNativeEvent().getCharCode();
                        if ((charCode == 'v') || (charCode == 45)) {
                            m_helpfield = new TextArea();
                            m_helpfield.getElement().getStyle().setPosition(Position.FIXED);
                            m_range = getSelection();
                            m_element = event.getRelativeElement();
                            m_element.setAttribute("contentEditable", "false");
                            RootPanel.get().add(m_helpfield);
                            m_helpfield.setFocus(true);
                        }
                    }
                    // prevent adding line breaks
                    if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }

                    // schedule the change event, so the key press can take effect
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            if (m_range != null) {
                                String pasteValue = m_helpfield.getText();
                                m_helpfield.removeFromParent();
                                m_element.setAttribute("contentEditable", "true");
                                setFocus(true);
                                setSelection(m_range, pasteValue);
                                m_range = null;

                            }
                            fireValueChange(false);
                        }
                    });
                }

            }, KeyDownEvent.getType());

            addDomHandler(new ChangeHandler() {

                public void onChange(ChangeEvent event) {

                    fireValueChange(false);

                }
            }, ChangeEvent.getType());
            addDomHandler(new BlurHandler() {

                public void onBlur(BlurEvent event) {

                    fireValueChange(false);
                }
            }, BlurEvent.getType());
        }
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {

        return getElement().getInnerText();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (m_active == active) {
            return;
        }
        m_active = active;
        if (m_active) {
            getElement().setAttribute("contentEditable", "true");
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
            getElement().focus();
            fireValueChange(true);
        } else {
            getElement().setAttribute("contentEditable", "false");
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // nothing to do

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, true);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        getElement().setInnerText(value);
        if (fireEvents) {
            fireValueChange(false);
        }
    }

    /**
     * Returns the actual range of the courser.<p>
     * 
     * @return the actual range of the courser
     */
    protected native JavaScriptObject getSelection()
    /*-{
        var range, sel;
        sel = $wnd.rangy.getSelection();
        range = null;
        if (sel.rangeCount > 0) {
            range = sel.getRangeAt(0);
        } else {
            range = rangy.createRange();
        }
        return range;
    }-*/;

    /**
     * Includes the new text into the text block.<p>
     * @param range the range where the text should be included
     * @param text the text that should be included 
     */
    protected native void setSelection(JavaScriptObject range, String text)
    /*-{
        var sel;
        range.deleteContents();
        var textNode = $wnd.document.createTextNode(text)
        range.insertNode(textNode);
        sel = $wnd.rangy.getSelection();
        range.setStart(textNode, textNode.length);
        range.setEnd(textNode, textNode.length);
        sel.removeAllRanges();
        sel.setSingleRange(range);
    }-*/;

    /**
     * Initializes the widget.<p>
     */
    private void init() {

        getElement().setAttribute("contentEditable", "true");
        addStyleName(I_CmsLayoutBundle.INSTANCE.form().input());
        m_active = true;
    }
}
