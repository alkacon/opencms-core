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

package org.opencms.ui.components;

import org.opencms.ui.shared.CmsBrowserFrameState;

import com.vaadin.server.Resource;
import com.vaadin.ui.BrowserFrame;

/**
 * Extending the browser frame class to allow setting of the iFrame name attribute.<p>
 */
public class CmsBrowserFrame extends BrowserFrame {

    /** The serial version id. */
    private static final long serialVersionUID = -7614391470292599811L;

    /**
     * Creates a new empty browser frame.
     */
    public CmsBrowserFrame() {

    }

    /**
     * Creates a new empty browser frame with the given caption.
     *
     * @param caption
     *            The caption for the component
     */
    public CmsBrowserFrame(String caption) {
        super(caption);
    }

    /**
     * Creates a new browser frame with the given caption and content.
     *
     * @param caption
     *            The caption for the component.
     * @param source
     *            A Resource representing the Web page that should be displayed.
     */
    public CmsBrowserFrame(String caption, Resource source) {
        super(caption, source);
    }

    /**
     * Sets the iFrame name attribute.<p>
     *
     * @return the iFrame name attribute
     */
    public String getName() {

        return getState().getName();
    }

    /**
     * Returns the iFrame name attribute
     *
     * @param name the iFrame name attribute
     */
    public void setName(String name) {

        getState(true).setName(name);
    }

    /**
     * @see com.vaadin.ui.BrowserFrame#getState()
     */
    @Override
    protected CmsBrowserFrameState getState() {

        // TODO Auto-generated method stub
        return (CmsBrowserFrameState)super.getState();
    }

    /**
     * @see com.vaadin.ui.AbstractEmbedded#getState(boolean)
     */
    @Override
    protected CmsBrowserFrameState getState(boolean markAsDirty) {

        // TODO Auto-generated method stub
        return (CmsBrowserFrameState)super.getState(markAsDirty);
    }
}
