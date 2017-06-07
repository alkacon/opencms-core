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
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

/**
 * Tri-state checkbox.<p>
 */
public class CmsTriStateCheckBox extends Composite implements HasValueChangeHandlers<CmsTriStateCheckBox.State> {

    /**
     * The possible check box states.<p>
     */
    public enum State {
        /** neither on nor off. */
        middle, /** off. **/
        off, /** on. **/
        on;
    }

    /** The CSS bundle for this class. */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The button which provides the actual widget layout. */
    private CmsPushButton m_button;

    /** The state to use as a next state if the user clicks on the checkbox while it is in its third state. */
    private State m_nextStateAfterMiddle = State.off;

    /** The current state. */
    private State m_state;

    /** The check box text. */
    private String m_text;

    /**
     * Creates a new instance.<p>
     *
     * @param labelText the label text
     */
    public CmsTriStateCheckBox(String labelText) {

        m_button = new CmsPushButton();
        m_text = labelText;
        initWidget(m_button);
        m_button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        addStyleName(CSS.triState());
        m_state = State.middle;
        updateStyle();
        m_button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsTriStateCheckBox.this.onClick();
            }
        });

    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<State> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Sets the state which the check box should transition to if the user clicks on it while it is neither on nor off.<p>
     *
     * @param state the target state
     */
    public void setNextStateAfterIntermediateState(State state) {

        m_nextStateAfterMiddle = state;
    }

    /**
     * Sets the state of the check box and optionally fires an event.<p>
     *
     * @param state the new state
     * @param fireEvent true if a ValueChangeEvent should be fired
     */
    public void setState(State state, boolean fireEvent) {

        boolean changed = m_state != state;
        m_state = state;
        if (changed) {
            updateStyle();
        }
        if (fireEvent) {
            ValueChangeEvent.fire(this, state);
        }
    }

    /**
     * Sets the check box label text.<p>
     *
     * @param text the new label text
     */
    public void setText(String text) {

        m_text = text;
        updateStyle();
    }

    /**
     * Handles clicks on the check box.<p>
     */
    protected void onClick() {

        State newState = getNextState(m_state);
        setState(newState, true);
    }

    /**
     * Gets the image class to use for the check box.<p>
     *
     * @return the image class
     */
    private String getImageClass() {

        switch (m_state) {
            case on:
                return CSS.triStateOn();
            case off:
                return CSS.triStateOff();
            case middle:
            default:
                return CSS.triStateMedium();
        }

    }

    /**
     * Gets the state which the check box would change to if cilcked on in a given state.<p>
     *
     * @param state the original state
     * @return the next state
     */
    private State getNextState(State state) {

        State nextState = null;
        switch (state) {
            case off:
                nextState = State.on;
                break;
            case on:
                nextState = State.off;
                break;
            case middle:
            default:
                nextState = m_nextStateAfterMiddle;
        }
        return nextState;
    }

    /**
     * Updates the UI state according to the logical state.<p>
     */
    private void updateStyle() {

        m_button.setUpFace(m_text, getImageClass());
    }
}
