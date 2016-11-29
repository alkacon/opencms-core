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

import org.opencms.file.CmsResource;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.extensions.CmsMaxHeightExtension;

import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

import com.google.common.collect.Lists;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.declarative.DesignContext;

/**
 * Basic dialog class with a content panel and button bar.<p>
 */
public class CmsBasicDialog extends VerticalLayout {

    /** The available window widths. */
    public enum DialogWidth {

        /** Depending on the content. */
        content,

        /** The maximum width of 90% of the window width. */
        max,

        /** The default width of 600px. */
        narrow,

        /** The wide width of 800px. */
        wide
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The shortcut action handler. */
    private Handler m_actionHandler;

    /** The left button panel. */
    private HorizontalLayout m_buttonPanelLeft;

    /** The button bar. */
    private HorizontalLayout m_buttonPanelRight;

    /** The content panel. */
    private Panel m_contentPanel;

    /** The resource info component. */
    private Component m_infoComponent;

    /** The resources for which the resource info boxes should be displayed. */
    private List<CmsResource> m_infoResources = Lists.newArrayList();

    /** The main panel. */
    private VerticalLayout m_mainPanel;

    /** Extension used to regulate max height. */
    private CmsMaxHeightExtension m_maxHeightExtension;

    /** The window resize listener. */
    private BrowserWindowResizeListener m_windowResizeListener;

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
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidth("100%");
        buttons.addStyleName(OpenCmsTheme.DIALOG_BUTTON_BAR);
        addComponent(buttons);
        m_buttonPanelLeft = new HorizontalLayout();
        m_buttonPanelLeft.setSpacing(true);
        buttons.addComponent(m_buttonPanelLeft);
        buttons.setComponentAlignment(m_buttonPanelLeft, Alignment.MIDDLE_LEFT);
        m_buttonPanelLeft.setVisible(false);
        m_buttonPanelRight = new HorizontalLayout();
        m_buttonPanelRight.setSpacing(true);
        buttons.addComponent(m_buttonPanelRight);
        buttons.setComponentAlignment(m_buttonPanelRight, Alignment.MIDDLE_RIGHT);
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
        int pageWidth = Page.getCurrent().getBrowserWindowWidth();
        if (((width == DialogWidth.wide) && (pageWidth < 810))
            || ((width == DialogWidth.narrow) && (pageWidth < 610))) {
            // in case the available page width does not allow the desired width, use max
            width = DialogWidth.max;
        }
        if (width == DialogWidth.max) {
            // in case max width would result in a width very close to wide or narrow, use their static width instead of relative width
            if ((pageWidth >= 610) && (pageWidth < 670)) {
                width = DialogWidth.narrow;
            } else if ((pageWidth >= 810) && (pageWidth < 890)) {
                width = DialogWidth.wide;
            }
        }
        switch (width) {
            case content:
                // do nothing
                break;
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

        addButton(button, true);
    }

    /**
     * Adds a button to the button bar.<p>
     *
     * @param button the button to add
     * @param right to align the button right
     */
    public void addButton(Component button, boolean right) {

        if (right) {
            m_buttonPanelRight.addComponent(button);
        } else {
            m_buttonPanelLeft.addComponent(button);
            m_buttonPanelLeft.setVisible(true);
        }
    }

    /**
     * Displays the resource infos panel.<p>
     *
     * @param resources the resources
     */
    public void displayResourceInfo(List<CmsResource> resources) {

        m_infoResources = Lists.newArrayList(resources);
        if (m_infoComponent != null) {
            m_mainPanel.removeComponent(m_infoComponent);
            m_infoComponent = null;
        }
        if ((resources != null) && !resources.isEmpty()) {
            if (resources.size() == 1) {
                m_infoComponent = new CmsResourceInfo(resources.get(0));
                m_mainPanel.addComponent(m_infoComponent, 0);
            } else {
                m_infoComponent = createResourceListPanel(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_RESOURCE_INFO_0),
                    resources);
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
     * Gets the resources for which the resource info boxes should be displayed.<p>
     *
     * @return the resource info resources
     */
    public List<CmsResource> getInfoResources() {

        return m_infoResources;
    }

    /**
     * Initializes action handler.<p>
     *
     * @param window the parent window
     */
    public void initActionHandler(final Window window) {

        if (m_actionHandler != null) {
            window.addActionHandler(m_actionHandler);
            window.addCloseListener(new CloseListener() {

                private static final long serialVersionUID = 1L;

                public void windowClose(CloseEvent e) {

                    clearActionHandler(window);
                }
            });
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
            boolean aboveRead = false;
            boolean belowRead = false;
            if ("content".equals(child.tagName()) && !contentRead) {
                Component content = designContext.readDesign(child.child(0));
                setContent(content);
                contentRead = true;
            } else if ("buttons".equals(child.tagName()) && !buttonsRead) {
                for (Element buttonElement : child.children()) {
                    Component button = designContext.readDesign(buttonElement);
                    Attributes attr = buttonElement.attributes();
                    addButton(button, !attr.hasKey(":left"));
                }
                buttonsRead = true;
            } else if ("above".equals(child.tagName()) && !aboveRead) {
                Component aboveContent = designContext.readDesign(child.child(0));
                setAbove(aboveContent);
                aboveRead = true;
            } else if ("below".equals(child.tagName()) && !belowRead) {
                Component belowContent = designContext.readDesign(child.child(0));
                setBelow(belowContent);
                belowRead = true;
            }
        }
    }

    /**
     * Sets the content to be displayed above the main content.<p>
     *
     * @param aboveContent the above content
     */
    public void setAbove(Component aboveContent) {

        if (m_mainPanel.getComponentIndex(m_contentPanel) == 0) {
            m_mainPanel.addComponent(aboveContent, 0);
        } else {
            m_mainPanel.replaceComponent(m_mainPanel.getComponent(0), aboveContent);
        }
    }

    /**
     * Sets the shortcut action handler.<p>
     * Set this before opening the window, so it will be initialized properly.<p>
     *
     * @param actionHandler the action handler
     */
    public void setActionHandler(Handler actionHandler) {

        m_actionHandler = actionHandler;
    }

    /**
     * Sets the content to be displayed below the main content.<p>
     * @param belowContent the below content
     */
    public void setBelow(Component belowContent) {

        int i = m_mainPanel.getComponentIndex(m_mainPanel);
        Component oldBelow = m_mainPanel.getComponent(i + 1);
        if (oldBelow == null) {
            m_mainPanel.addComponent(belowContent);
        } else {
            m_mainPanel.replaceComponent(oldBelow, belowContent);
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
     * Creates an 'Cancel' button.<p>
     *
     * @return the button
     */
    protected Button createButtonCancel() {

        return new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_CANCEL_0));
    }

    /**
     * Creates an 'OK' button.<p>
     *
     * @return the button
     */
    protected Button createButtonOK() {

        return new Button(CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
    }

    /**
     * Creates a resource list panel.<p>
     *
     * @param caption the caption to use
     * @param resources the resources
     *
     * @return the panel
     */
    protected Panel createResourceListPanel(String caption, List<CmsResource> resources) {

        Panel result = new Panel(caption);
        result.addStyleName("v-scrollable");
        result.setSizeFull();
        VerticalLayout resourcePanel = new VerticalLayout();
        result.setContent(resourcePanel);
        resourcePanel.addStyleName(OpenCmsTheme.REDUCED_MARGIN);
        resourcePanel.addStyleName(OpenCmsTheme.REDUCED_SPACING);
        resourcePanel.setSpacing(true);
        resourcePanel.setMargin(true);
        for (CmsResource resource : resources) {
            resourcePanel.addComponent(new CmsResourceInfo(resource));
        }
        return result;
    }

    /**
     * Removes the action handler.<p>
     *
     * @param window the window the action handler is attached to
     */
    void clearActionHandler(Window window) {

        if (m_actionHandler != null) {
            window.removeActionHandler(m_actionHandler);
        }
    }

    /**
     * Calculates max dialog height given the window height.<p>
     *
     * @param windowHeight the window height
     * @return the maximal dialog height
     */
    private int calculateMaxHeight(int windowHeight) {

        return (int)((0.95 * windowHeight) - 40);
    }

    /**
     * Adds the max height extension to the dialog panel.<p>
     */
    private void enableMaxHeight() {

        // use the window height minus an offset for the window header and some spacing
        int maxHeight = calculateMaxHeight(A_CmsUI.get().getPage().getBrowserWindowHeight());
        m_maxHeightExtension = new CmsMaxHeightExtension(this, maxHeight);
        m_maxHeightExtension.addHeightChangeHandler(new Runnable() {

            public void run() {

                Window wnd = CmsVaadinUtils.getWindow(CmsBasicDialog.this);
                if (wnd != null) {
                    wnd.center();
                }
            }
        });

        addDetachListener(new DetachListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void detach(DetachEvent event) {

                A_CmsUI.get().getPage().removeBrowserWindowResizeListener(m_windowResizeListener);
            }
        });

        m_windowResizeListener = new BrowserWindowResizeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void browserWindowResized(BrowserWindowResizeEvent event) {

                int newHeight = event.getHeight();
                m_maxHeightExtension.updateMaxHeight(calculateMaxHeight(newHeight));
            }
        };
        A_CmsUI.get().getPage().addBrowserWindowResizeListener(m_windowResizeListener);

    }
}
