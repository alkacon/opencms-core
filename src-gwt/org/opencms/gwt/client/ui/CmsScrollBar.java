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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.VerticalScrollbar;

/**
 * A custom scroll bar to be used with {@link org.opencms.gwt.client.ui.CmsScrollPanel}.<p>
 */
public class CmsScrollBar extends FocusPanel
implements I_CmsDescendantResizeHandler, HasValue<Integer>, VerticalScrollbar {

    /**
     * The timer used to continue to shift the knob as the user holds down one of
     * the left/right arrow keys. Only IE auto-repeats, so we just keep catching
     * the events.
     */
    private class KeyTimer extends Timer {

        /** A bit indicating that this is the first run. */
        private boolean m_firstRun = true;

        /** The number of steps to shift with each press. */
        private int m_multiplier = 1;

        /** The delay between shifts, which shortens as the user holds down the button. */
        private final int m_repeatDelay = 30;

        /** A bit indicating whether we are shifting to a higher or lower value. */
        private boolean m_shiftUp;

        /**
         * Constructor.<p>
         */
        public KeyTimer() {

            // nothing to do
        }

        /**
         * This method will be called when a timer fires. Override it to implement
         * the timer's logic.
         */
        @Override
        public void run() {

            int newPos = 0;
            boolean stop = false;
            // Slide the slider bar
            if (m_shiftUp) {
                newPos = getVerticalScrollPosition() - (m_multiplier * m_stepSize);
            } else {
                newPos = getVerticalScrollPosition() + (m_multiplier * m_stepSize);
            }

            // Check if we are paging while holding mouse down
            // and make sure will not overshoot original mouse down position.
            if (m_pagingMouse && m_shiftUp && (m_mouseDownPos > newPos)) {
                stop = true;
                setValue(Integer.valueOf(m_mouseDownPos));
            } else if (m_pagingMouse && !m_shiftUp && (m_mouseDownPos < newPos)) {
                stop = true;
                setValue(Integer.valueOf(m_mouseDownPos));
            }
            if (!stop) {
                if (m_firstRun) {
                    m_firstRun = false;
                }

                // Slide the slider bar
                setValue(Integer.valueOf(newPos));

                // Repeat this timer until cancelled by keyup event
                schedule(m_repeatDelay);
            }
        }

        /**
         * Schedules a timer to elapse in the future.
         *
         * @param delayMillis how long to wait before the timer elapses, in
         *          milliseconds
         * @param shiftUp2 whether to shift up or not
         * @param multiplier2 the number of steps to shift
         */
        public void schedule(final int delayMillis, final boolean shiftUp2, final int multiplier2) {

            m_firstRun = true;
            m_shiftUp = shiftUp2;
            m_multiplier = multiplier2;
            super.schedule(delayMillis);
        }
    }

    /** The initial delay. */
    private static final int INITIALDELAY = 400;

    /** The scroll knob minimum height. */
    private static final int SCROLL_KNOB_MIN_HEIGHT = 10;

    /** The scroll knob top and bottom offset. */
    private static final int SCROLL_KNOB_OFFSET = 2;

    /** The size of the increments between knob positions. */
    protected int m_stepSize = 5;

    /** The position of the first mouse down. Used for paging. */
    int m_mouseDownPos;

    /** A flag for the when the mouse button is held down when away from the slider. */
    boolean m_pagingMouse;

    /** The scroll content element. */
    private Element m_containerElement;

    /** The current scroll position. */
    private int m_currentValue;

    /**
     * The timer used to continue to shift the knob if the user holds down a key.
     */
    private final KeyTimer m_keyTimer = new KeyTimer();

    /** The scroll knob. */
    private Element m_knob;

    /** The current scroll knob height. */
    private int m_knobHeight;

    /** Last mouse Y position. */
    private int m_lastMouseY;

    /** Mous sliding start value. */
    private int m_mouseSlidingStartValue;

    /** Mouse sliding start Y position. */
    private int m_mouseSlidingStartY;

    /** The page size value. */
    private int m_pageSize = 20;

    /** The value knob position ratio. */
    private double m_positionValueRatio;

    /** The scrollable element. */
    private Element m_scrollableElement;

    /** A bit indicating whether or not we are currently sliding the slider bar due to keyboard events. */
    private boolean m_slidingKeyboard;

    /**
     * A bit indicating whether or not we are currently sliding the slider bar due
     * to mouse events.
     */
    private boolean m_slidingMouse;

    /**
     * Constructor.<p>
     *
     * @param scrollableElement the scrollable element
     * @param containerElement the scroll content
     */
    public CmsScrollBar(Element scrollableElement, Element containerElement) {

        I_CmsLayoutBundle.INSTANCE.scrollBarCss().ensureInjected();
        setStyleName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollBar());
        m_scrollableElement = scrollableElement;
        m_containerElement = containerElement;
        m_knob = DOM.createDiv();
        m_knob.addClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollKnob());
        getElement().appendChild(m_knob);
        sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEWHEEL | Event.KEYEVENTS | Event.FOCUSEVENTS);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasScrollHandlers#addScrollHandler(com.google.gwt.event.dom.client.ScrollHandler)
     */
    public HandlerRegistration addScrollHandler(ScrollHandler handler) {

        // Sink the event on the scrollable element, not the root element.
        Event.sinkEvents(getScrollableElement(), Event.ONSCROLL);
        return addHandler(handler, ScrollEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasVerticalScrolling#getMaximumVerticalScrollPosition()
     */
    public int getMaximumVerticalScrollPosition() {

        return m_containerElement.getOffsetHeight() - getScrollableElement().getOffsetHeight();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasVerticalScrolling#getMinimumVerticalScrollPosition()
     */
    public int getMinimumVerticalScrollPosition() {

        return 0;
    }

    /**
     * @see com.google.gwt.user.client.ui.VerticalScrollbar#getScrollHeight()
     */
    public int getScrollHeight() {

        return m_containerElement.getOffsetHeight();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public Integer getValue() {

        return Integer.valueOf(m_currentValue);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasVerticalScrolling#getVerticalScrollPosition()
     */
    public int getVerticalScrollPosition() {

        return getValue().intValue();
    }

    /**
     * @param reziseable true if the panel is resizeable
     *
     */
    public void isResizeable(boolean reziseable) {

        if (reziseable) {
            getElement().getStyle().setMarginBottom(7, Unit.PX);
        } else {
            getElement().getStyle().setMarginBottom(0, Unit.PX);
        }
    }

    /**
     * Listen for events that will move the knob.
     *
     * @param event the event that occurred
     */
    @Override
    public final void onBrowserEvent(final Event event) {

        super.onBrowserEvent(event);
        switch (DOM.eventGetType(event)) {
            // Unhighlight and cancel keyboard events
            case Event.ONBLUR:
                m_keyTimer.cancel();
                if (m_slidingMouse) {
                    stopMouseSliding(event);

                } else if (m_slidingKeyboard) {
                    m_slidingKeyboard = false;

                }

                break;

            // Mousewheel events
            case Event.ONMOUSEWHEEL:
                int velocityY = event.getMouseWheelVelocityY() * m_stepSize;
                event.preventDefault();
                CmsDebugLog.getInstance().printLine("Whell velocity: " + velocityY);
                if (velocityY > 0) {
                    shiftDown(velocityY);
                } else {
                    shiftUp(-velocityY);
                }
                break;

            // Shift left or right on key press
            case Event.ONKEYDOWN:
                if (!m_slidingKeyboard) {
                    int multiplier = 1;
                    if (event.getCtrlKey()) {
                        multiplier = m_stepSize;
                    }

                    switch (event.getKeyCode()) {
                        case KeyCodes.KEY_HOME:
                            event.preventDefault();
                            setValue(Integer.valueOf(0));
                            break;
                        case KeyCodes.KEY_END:
                            event.preventDefault();
                            setValue(Integer.valueOf(getMaximumVerticalScrollPosition()));
                            break;
                        case KeyCodes.KEY_PAGEUP:
                            event.preventDefault();
                            m_slidingKeyboard = true;
                            shiftUp(m_pageSize);
                            m_keyTimer.schedule(INITIALDELAY, true, m_pageSize);
                            break;
                        case KeyCodes.KEY_PAGEDOWN:
                            event.preventDefault();
                            m_slidingKeyboard = true;

                            shiftDown(m_pageSize);
                            m_keyTimer.schedule(INITIALDELAY, false, m_pageSize);
                            break;
                        case KeyCodes.KEY_UP:
                            event.preventDefault();
                            m_slidingKeyboard = true;

                            shiftUp(multiplier);
                            m_keyTimer.schedule(INITIALDELAY, true, multiplier);
                            break;
                        case KeyCodes.KEY_DOWN:
                            event.preventDefault();
                            m_slidingKeyboard = true;

                            shiftDown(multiplier);
                            m_keyTimer.schedule(INITIALDELAY, false, multiplier);
                            break;

                        default:
                    }
                }
                break;
            // Stop shifting on key up
            case Event.ONKEYUP:
                m_keyTimer.cancel();
                if (m_slidingKeyboard) {
                    m_slidingKeyboard = false;
                }
                break;

            // Mouse Events
            case Event.ONMOUSEDOWN:
                if (sliderClicked(event)) {
                    startMouseSliding(event);
                    event.preventDefault();
                }
                break;
            case Event.ONMOUSEUP:
                stopMouseSliding(event);
                break;
            case Event.ONMOUSEMOVE:
                slideKnob(event);
                break;
            default:
        }

    }

    /**
     * @see org.opencms.gwt.client.I_CmsDescendantResizeHandler#onResizeDescendant()
     */
    public void onResizeDescendant() {

        redraw();
    }

    /**
     * @see com.google.gwt.user.client.ui.VerticalScrollbar#setScrollHeight(int)
     */
    public void setScrollHeight(int height) {

        redraw();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(Integer value) {

        setValue(value, true);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(Integer value, boolean fireEvents) {

        if (value != null) {
            m_currentValue = value.intValue();
        } else {
            m_currentValue = 0;
        }
        setKnobPosition(m_currentValue);
        // Fire the ValueChangeEvent
        if (fireEvents) {
            ValueChangeEvent.fire(this, Integer.valueOf(m_currentValue));
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasVerticalScrolling#setVerticalScrollPosition(int)
     */
    public void setVerticalScrollPosition(int position) {

        setValue(Integer.valueOf(position));
    }

    /**
     * Returns the associated scrollable element.<p>
     *
     * @return the associated scrollable element
     */
    protected Element getScrollableElement() {

        return m_scrollableElement;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();

        /*
         * Attach the event listener in onAttach instead of onLoad so users cannot
         * accidentally override it.
         */
        Event.setEventListener(getScrollableElement(), this);
        redraw();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    protected void onDetach() {

        /*
         * Detach the event listener in onDetach instead of onUnload so users cannot
         * accidentally override it.
         */
        Event.setEventListener(getScrollableElement(), null);

        super.onDetach();
    }

    /**
     * Redraws the scroll bar.<p>
     */
    protected void redraw() {

        if (isAttached()) {
            int outerHeight = getElement().getOffsetHeight();
            int innerHeight = m_containerElement.getOffsetHeight();
            if (outerHeight >= innerHeight) {
                setScrollbarVisible(false);
            } else {
                setScrollbarVisible(true);
                adjustKnobHeight(outerHeight, innerHeight);
                setKnobPosition(m_currentValue);
            }
        }
        if (m_slidingMouse) {
            m_mouseSlidingStartY = m_lastMouseY;
            m_mouseSlidingStartValue = m_currentValue;
        }
    }

    /**
     * Shifts the scroll position down.<p>
     *
     * @param shift the shift size
     */
    protected void shiftDown(int shift) {

        int max = getMaximumVerticalScrollPosition();
        if ((m_currentValue + shift) < max) {
            setVerticalScrollPosition(m_currentValue + shift);
        } else {
            setVerticalScrollPosition(max);
        }
    }

    /**
     * Shifts the scroll position up.<p>
     *
     * @param shift the shift size
     */
    protected void shiftUp(int shift) {

        int min = getMinimumVerticalScrollPosition();
        if ((m_currentValue - shift) > min) {
            setVerticalScrollPosition(m_currentValue - shift);
        } else {
            setVerticalScrollPosition(min);
        }
    }

    /**
     * Calculates the scroll knob height.<p>
     *
     * @param outerHeight the height of the scrollable element
     * @param innerHeight the height of the scroll content
     */
    private void adjustKnobHeight(int outerHeight, int innerHeight) {

        int result = (int)((1.0 * outerHeight * outerHeight) / innerHeight);
        result = result > (outerHeight - 5) ? 5 : (result < 8 ? 8 : result);
        m_positionValueRatio = (1.0 * (outerHeight - result)) / (innerHeight - outerHeight);
        m_knobHeight = result - (2 * SCROLL_KNOB_OFFSET);
        m_knobHeight = m_knobHeight < SCROLL_KNOB_MIN_HEIGHT ? SCROLL_KNOB_MIN_HEIGHT : m_knobHeight;
        m_knob.getStyle().setHeight(m_knobHeight, Unit.PX);
    }

    /**
     * Sets the scroll knob position according to the given value.<p>
     *
     * @param value the value
     */
    private void setKnobPosition(int value) {

        int top = (int)(SCROLL_KNOB_OFFSET + (m_positionValueRatio * value));
        int maxPosition = getElement().getOffsetHeight() - m_knobHeight - SCROLL_KNOB_OFFSET;
        top = top < SCROLL_KNOB_OFFSET ? SCROLL_KNOB_OFFSET : (top > maxPosition ? maxPosition : top);
        m_knob.getStyle().setTop(top, Unit.PX);
    }

    /**
     * Sets the scroll bar visibility.<p>
     *
     * @param visible <code>true</code> to set the scroll bar visible
     */
    private void setScrollbarVisible(boolean visible) {

        if (visible) {
            getElement().getStyle().clearWidth();
            m_knob.getStyle().clearDisplay();
        } else {
            getElement().getStyle().setWidth(0, Unit.PX);
            m_knob.getStyle().setDisplay(Display.NONE);
        }
    }

    /**
     * Sides the scroll knob according to the mouse event.<p>
     *
     * @param event the mouse event
     */
    private void slideKnob(Event event) {

        if (m_slidingMouse) {
            m_lastMouseY = event.getClientY();
            int shift = (int)((m_lastMouseY - m_mouseSlidingStartY) / m_positionValueRatio);
            int nextValue = m_mouseSlidingStartValue + shift;
            CmsDebugLog.getInstance().printLine("Mouse sliding should set value to: " + nextValue);
            int max = getMaximumVerticalScrollPosition();
            int min = getMinimumVerticalScrollPosition();
            if (nextValue < min) {
                nextValue = min;
            } else if (nextValue > max) {
                nextValue = max;
            }
            setValue(Integer.valueOf(nextValue));
        }
    }

    /**
     * Returns <code>true</code> if the events mouse position is above the scroll bar knob.<p>
     *
     * @param event the mouse event
     *
     * @return <code>true</code> if the events mouse position is above the scroll bar knob
     */
    private boolean sliderClicked(Event event) {

        boolean result = CmsPositionBean.generatePositionInfo(m_knob).isOverElement(
            event.getClientX() + Window.getScrollLeft(),
            event.getClientY() + Window.getScrollTop());
        CmsDebugLog.getInstance().printLine("Slider was clicked: " + result);
        return result;
    }

    /**
     * Starts the mouse sliding.<p>
     *
     * @param event the mouse event
     */
    private void startMouseSliding(Event event) {

        if (!m_slidingMouse) {
            m_slidingMouse = true;
            DOM.setCapture(getElement());
            m_mouseSlidingStartY = event.getClientY();
            m_mouseSlidingStartValue = m_currentValue;
            CmsDebugLog.getInstance().printLine(
                "Mouse sliding started with clientY: "
                    + m_mouseSlidingStartY
                    + " and start value: "
                    + m_mouseSlidingStartValue
                    + " and a max value of "
                    + getMaximumVerticalScrollPosition());
        }
    }

    /**
     * Stops the mouse sliding.<p>
     *
     * @param event the mouse event
     */
    private void stopMouseSliding(Event event) {

        slideKnob(event);
        m_slidingMouse = false;
        DOM.releaseCapture(getElement());
    }
}
