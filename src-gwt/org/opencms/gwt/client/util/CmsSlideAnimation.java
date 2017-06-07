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

package org.opencms.gwt.client.util;

import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;

/**
 * Slide animation. Sliding the element into view or sliding it out.<p>
 * Uses the in-line CSS display property, clear after completion if appropriate.<p>
 *
 * @since 8.0.0
 */
public class CmsSlideAnimation extends A_CmsAnimation {

    /** The animated element. */
    private Element m_element;

    /** The element style. */
    private Style m_elementStyle;

    /** The height of the fully visible element. */
    private int m_height;

    /** Show or hide flag. */
    private boolean m_show;

    /** Flag to indicate if the animation has already started. */
    private boolean m_started;

    /**
     * Constructor.<p>
     *
     * @param element the element to animate
     * @param show <code>true</code> to show the element, <code>false</code> to hide it away
     * @param callback the callback executed after the animation is completed
     */
    public CmsSlideAnimation(Element element, boolean show, Command callback) {

        super(callback);
        m_show = show;
        m_element = element;
        m_elementStyle = m_element.getStyle();
    }

    /**
     * Slides the given element into view executing the callback afterwards.<p>
     *
     * @param element the element to slide in
     * @param callback the callback
     * @param duration the animation duration
     *
     * @return the running animation object
     */
    public static CmsSlideAnimation slideIn(Element element, Command callback, int duration) {

        CmsSlideAnimation animation = new CmsSlideAnimation(element, true, callback);
        animation.run(duration);
        return animation;
    }

    /**
     * Slides the given element out of view executing the callback afterwards.<p>
     *
     * @param element the element to slide out
     * @param callback the callback
     * @param duration the animation duration
     *
     * @return the running animation object
     */
    public static CmsSlideAnimation slideOut(Element element, Command callback, int duration) {

        CmsSlideAnimation animation = new CmsSlideAnimation(element, false, callback);
        animation.run(duration);
        return animation;
    }

    /**
     * @see com.google.gwt.animation.client.Animation#run(int, double)
     */
    @Override
    public void run(int duration, double startTime) {

        if (m_show) {
            String heightProperty = m_elementStyle.getHeight();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(heightProperty) && heightProperty.contains("px")) {
                m_height = CmsClientStringUtil.parseInt(heightProperty);
            }
            if (m_height == 0) {
                String display = m_elementStyle.getDisplay();
                m_elementStyle.setDisplay(Display.BLOCK);
                m_height = m_element.getOffsetHeight();
                m_elementStyle.setProperty("display", display);
            }
        } else {
            m_height = CmsDomUtil.getCurrentStyleInt(m_element, org.opencms.gwt.client.util.CmsDomUtil.Style.height);
        }
        super.run(duration, startTime);
    }

    /**
     * @see com.google.gwt.animation.client.Animation#onComplete()
     */
    @Override
    protected void onComplete() {

        onUpdate(1.0);
        if (!m_show) {
            m_elementStyle.setDisplay(Display.NONE);
        }
        m_elementStyle.clearHeight();
        m_elementStyle.clearOverflow();
        m_height = 0;
        m_started = false;
        if (m_callback != null) {
            m_callback.execute();
        }
    }

    /**
     * @see com.google.gwt.animation.client.Animation#onUpdate(double)
     */
    @Override
    protected void onUpdate(double progress) {

        if (!m_started) {
            m_started = true;
            m_elementStyle.setOverflow(Overflow.HIDDEN);
            m_elementStyle.setDisplay(Display.BLOCK);
        }
        progress = progress * progress;
        if (m_show) {
            m_elementStyle.setHeight(progress * m_height, Unit.PX);
        } else {
            m_elementStyle.setHeight((-progress + 1) * m_height, Unit.PX);
        }
    }
}
