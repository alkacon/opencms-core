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

package org.opencms.ui.sitemap;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.OpenCmsTheme;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Widget displaying a sitemap tree node, with an openable area for its children.<p>
 */
public class CmsSitemapTreeNode extends CssLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The container for the children. */
    protected ComponentContainer m_children;

    /** The container for the content widget (usually a resource box). */
    protected CssLayout m_contentContainer;

    /** True if the child area is opened. */
    private boolean m_isOpen;

    /** The button to open/close the llst of children. */
    private CmsSitemapTreeNodeOpener m_opener;

    /** Creates a new instance. */
    public CmsSitemapTreeNode() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setOpen(false);
        setContent(new Label("[content not set]"));
    }

    /**
     * Gets the container for the children.<p>
     *
     * @return the container for the children
     */
    public ComponentContainer getChildren() {

        return m_children;
    }

    /**
     * Gets the opener button.<p>
     *
     * @return the opener button
     */
    public CmsSitemapTreeNodeOpener getOpener() {

        return m_opener;
    }

    /**
     * Returns true if the child list is opened.<p>
     *
     * @return true if the child list is opened
     */
    public boolean isOpen() {

        return m_isOpen;
    }

    /**
     * Sets additional styles.<p>
     *
     * Useful for declarative layouts.
     *
     * TODO: check if this is actually used anywhere
     *
     * @param additionalStyles the additional styles to set
     */
    public void setAdditionalStyles(String additionalStyles) {

        addStyleName(additionalStyles);
    }

    /**
     * Sets the content widget.<p>
     *
     * @param content the content widget
     */
    public void setContent(Component content) {

        m_contentContainer.removeAllComponents();
        m_contentContainer.addComponent(content);
    }

    /**
     * Opens / closes the list of children.<p<
     *
     * @param isOpen true if the children should be opened, false if they should be closed
     */
    public void setOpen(boolean isOpen) {

        m_opener.setStyleOpen(isOpen);
        m_children.setVisible(isOpen);
        m_isOpen = isOpen;
    }

    /**
     * Shows or hides the opener button.<p>
     *
     * @param visible true if the opener should be shown, else false
     */
    public void setOpenerVisible(boolean visible) {

        if (visible) {
            m_opener.removeStyleName(OpenCmsTheme.BUTTON_INVISIBLE);
        } else {
            m_opener.addStyleName(OpenCmsTheme.BUTTON_INVISIBLE);
        }
    }

}
