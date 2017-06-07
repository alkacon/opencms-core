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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;

/**
 * Changes the elements height until the target height is reached.<p>
 *
 * @since 8.0.0
 */
public class CmsChangeHeightAnimation extends A_CmsAnimation {

    /** The element style. */
    private Style m_elementStyle;

    /** The height of the fully visible element. */
    private int m_height;

    /** The difference between the target height and the original height. */
    private int m_heightDiff;

    /** The height when the animation should stop. */
    private int m_targetHeight;

    /**
     * Constructor.<p>
     *
     * @param element the element to animate
     * @param targetHeight the height when the animation should stop
     * @param callback the callback executed after the animation is completed
     */
    public CmsChangeHeightAnimation(Element element, int targetHeight, Command callback) {

        super(callback);

        m_elementStyle = element.getStyle();

        m_height = CmsDomUtil.getCurrentStyleInt(element, CmsDomUtil.Style.height);
        m_targetHeight = targetHeight;
        m_heightDiff = m_targetHeight - m_height;
    }

    /**
     * Slides the given element into view executing the callback afterwards.<p>
     *
     * @param element the element to slide in
     * @param targetHeight the height when the animation should stop
     * @param callback the callback executed after the animation is completed
     * @param duration the animation duration
     *
     * @return the running animation object
     */
    public static CmsChangeHeightAnimation change(Element element, int targetHeight, Command callback, int duration) {

        CmsChangeHeightAnimation animation = new CmsChangeHeightAnimation(element, targetHeight, callback);
        animation.run(duration);
        return animation;
    }

    /**
     * @see com.google.gwt.animation.client.Animation#onComplete()
     */
    @Override
    protected void onComplete() {

        m_elementStyle.setHeight(m_targetHeight, Unit.PX);
        if (m_callback != null) {
            m_callback.execute();
        }
    }

    /**
     * @see com.google.gwt.animation.client.Animation#onUpdate(double)
     */
    @Override
    protected void onUpdate(double progress) {

        m_elementStyle.setHeight((m_heightDiff * progress * progress) + m_height, Unit.PX);
    }
}
