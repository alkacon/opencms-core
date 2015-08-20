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

import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.Messages;
import org.opencms.ui.components.extensions.CmsMaxHeightExtension;

import java.util.List;

import org.jsoup.nodes.Element;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.DesignContext;

/**
 * Basic dialog class with a content panel and button bar.<p>
 */
public class CmsBasicDialog extends VerticalLayout {

    /** The available window widths. */
    public enum DialogWidth {
        /** The default width of 600px. */
        narrow,

        /** The wide width of 800px. */
        wide,

        /** The maximum width of 90% of the window width. */
        max
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The main panel. */
    private VerticalLayout m_mainPanel;

    /** The content panel. */
    private Panel m_contentPanel;

    /** The button bar. */
    private HorizontalLayout m_buttonPanel;

    /** The resource info component. */
    private Component m_infoComponent;

    /**
     * Creates new instance.<p>
     */
    public CmsBasicDialog() {
        addStyleName(OpenCmsTheme.DIALOG);
        setMargin(true);
        setSpacing(true);
        setWidth("100%");

        m_mainPanel = new VerticalLayout();
        m_mainPanel.addStyleName(OpenCmsTheme.DIALOG_CONTENT);
        m_mainPanel.setSpacing(true);
        m_mainPanel.setSizeFull();

        m_contentPanel = new Panel();
        m_contentPanel.setSizeFull();
        m_contentPanel.addStyleName("v-scrollable");

        m_mainPanel.addComponent(m_contentPanel);
        m_mainPanel.setExpandRatio(m_contentPanel, 3);

        Panel panel = new Panel();
        panel.setContent(m_mainPanel);
        panel.setSizeFull();
        addComponent(panel);
        setExpandRatio(panel, 1);

        m_buttonPanel = new HorizontalLayout();
        m_buttonPanel.setSpacing(true);
        m_buttonPanel.addStyleName(OpenCmsTheme.DIALOG_BUTTON_BAR);
        addComponent(m_buttonPanel);
        setComponentAlignment(m_buttonPanel, Alignment.MIDDLE_RIGHT);
        enableMaxHeight();
    }

    /**
     * Initializes the dialog window.<p>
     *
     * @return the window to be used by dialogs
     */
    public static Window prepareWindow() {

        return prepareWindow(DialogWidth.narrow);
    }

    /**
     * Initializes the dialog window.<p>
     *
     * @param width the dialog width
     *
     * @return the window to be used by dialogs
     */
    public static Window prepareWindow(DialogWidth width) {

        Window window = new Window();
        window.setModal(true);
        window.setClosable(true);
        //TODO: check width available
        switch (width) {
            case wide:
                window.setWidth("800px");
                break;
            case max:
                window.setWidth("90%");
                break;
            case narrow:
            default:
                window.setWidth("600px");
                break;
        }
        window.center();
        return window;
    }

    /**
     * Adds a button to the button bar.<p>
     *
     * @param button the button to add
     */
    public void addButton(Component button) {

        m_buttonPanel.addComponent(button);
    }

    /**
     * Displays the resource infos panel.<p>
     *
     * @param resources the resources
     */
    public void displayResourceInfo(List<CmsResource> resources) {

        if (m_infoComponent != null) {
            m_mainPanel.removeComponent(m_infoComponent);
            m_infoComponent = null;
        }
        if ((resources != null) && !resources.isEmpty()) {
            if (resources.size() == 1) {
                m_infoComponent = new CmsResourceInfo(resources.get(0));
                m_mainPanel.addComponent(m_infoComponent, 0);
            } else {
                m_infoComponent = new Panel(Messages.get().getBundle().key(Messages.GUI_RESOURCE_INFO_0));
                m_infoComponent.addStyleName("v-scrollable");
                m_infoComponent.setSizeFull();
                VerticalLayout resourcePanel = new VerticalLayout();
                ((Panel)m_infoComponent).setContent(resourcePanel);
                resourcePanel.addStyleName(OpenCmsTheme.REDUCED_MARGIN);
                resourcePanel.addStyleName(OpenCmsTheme.REDUCED_SPACING);
                resourcePanel.setSpacing(true);
                resourcePanel.setMargin(true);
                for (CmsResource resource : resources) {
                    resourcePanel.addComponent(new CmsResourceInfo(resource));
                }
                m_mainPanel.addComponent(m_infoComponent, 0);
                m_mainPanel.setExpandRatio(m_infoComponent, 1);

                // reset expand ratio of the content panel
                m_contentPanel.setSizeUndefined();
                m_contentPanel.setWidth("100%");
                m_mainPanel.setExpandRatio(m_contentPanel, 0);
            }

        }
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

        m_contentPanel.setContent(content);
        if (content instanceof Layout.MarginHandler) {
            ((Layout.MarginHandler)content).setMargin(true);
        }
    }

    /**
     * Adds the max height extension to the dialog panel.<p>
     */
    @SuppressWarnings("unused")
    private void enableMaxHeight() {

        // use the window height minus an offset for the window header and some spacing
        int maxHeight = (int)((0.95 * A_CmsUI.get().getPage().getBrowserWindowHeight()) - 40);
        new CmsMaxHeightExtension(this, maxHeight);
    }
}
