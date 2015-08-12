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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.DesignContext;

/**
 * Basic dialog class with a content panel and button bar.<p>
 */
public class CmsBasicDialog extends VerticalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The content layout. */
    private VerticalLayout m_content = new VerticalLayout();

    /** The content panel. */
    private Panel m_contentPanel = new Panel();

    /** The button bar. */
    private HorizontalLayout m_buttonPanel = new HorizontalLayout();

    /**
     * Creates new instance.<p>
     */
    public CmsBasicDialog() {
        setSpacing(true);
        setSizeFull();
        m_contentPanel.setContent(m_content);
        addComponent(m_contentPanel);
        m_content.setMargin(true);
        addComponent(m_buttonPanel);
        setExpandRatio(m_contentPanel, 1);
        m_contentPanel.setHeight("100%");
        m_buttonPanel.setSpacing(true);
        setComponentAlignment(m_buttonPanel, Alignment.MIDDLE_RIGHT);
        m_buttonPanel.addStyleName("o-dialog-button-bar");
    }

    /**
     * @see com.vaadin.ui.AbstractOrderedLayout#readDesign(org.jsoup.nodes.Element, com.vaadin.ui.declarative.DesignContext)
     */
    @Override
    public void readDesign(Element design, DesignContext designContext) {

        for (Element child : design.children()) {
            boolean contentRead = false;
            boolean buttonsRead = false;
            if ("content".equals(child.tagName()) && !contentRead) {
                Component content = designContext.readDesign(child.child(0));
                setContent(content);
                contentRead = true;
            } else if ("buttons".equals(child.tagName()) && !buttonsRead) {
                for (Element buttonElement : child.children()) {
                    Component button = designContext.readDesign(buttonElement);
                    addButton(button);
                }
                buttonsRead = true;
            }
        }
    }

    /**
     * Sets the content.<p>
     *
     * @param content the content widget
     */
    public void setContent(Component content) {

        m_content.removeAllComponents();
        m_content.addComponent(content);

    }

    /**
     * Adds a button to the button bar.<p>
     *
     * @param button the button to add
     */
    void addButton(Component button) {

        m_buttonPanel.addComponent(button);
    }
}
