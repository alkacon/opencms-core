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

package org.opencms.ui.components;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.declarative.DesignContext;

/**
 * A container for a single widget which uses the max-height CSS property to limit the content height.<p<
 *
 */
public class CmsMaxHeight extends CustomComponent {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Default height (pixels). */
    private static final int DEFAULT_HEIGHT = 250;

    /** The internal layout. */
    private CustomLayout m_layout;

    /** The maximum height in pixels. */
    private int m_maxHeight;

    /**
     * Creates a new instance.<p>
     */
    public CmsMaxHeight() {
        m_layout = new CustomLayout();
        setCompositionRoot(m_layout);
        setMaxHeight(DEFAULT_HEIGHT);
    }

    /**
     * Gets the maximum height.<p>
     *
     * @return the maximum height
     */
    public int getMaxHeight() {

        return m_maxHeight;
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#readDesign(org.jsoup.nodes.Element, com.vaadin.ui.declarative.DesignContext)
     */
    @Override
    public void readDesign(Element design, DesignContext designContext) {

        super.readDesign(design, designContext);
        Elements childElements = design.children();
        if (childElements.size() > 0) {
            Element childElement = childElements.get(0);
            Component childComponent = designContext.readDesign(childElement);
            setContent(childComponent);
        }
    }

    /**
     * Sets the content widget.<p>
     *
     * @param content the content widget
     */
    public void setContent(Component content) {

        m_layout.addComponent(content, "content");
    }

    /**
     * Sets the maximum height.<p>
     *
     * @param maxHeight the maximum height
     */
    public void setMaxHeight(int maxHeight) {

        m_maxHeight = maxHeight;
        m_layout.setTemplateContents(getTemplate(maxHeight));
    }

    /**
     * Helper method to build the template HTML.<p>
     *
     * @param maxHeight the maximum height
     * @return the template HTML
     */
    private String getTemplate(int maxHeight) {

        return "<div style=\"max-height: "
            + maxHeight
            + "px; overflow-y: auto;\">\n"
            + "    <div location=\"content\" />\n"
            + "</div>";

    }
}
