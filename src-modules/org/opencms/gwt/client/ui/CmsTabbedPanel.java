/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTabbedPanel.java,v $
 * Date   : $Date: 2010/03/31 13:35:36 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper class for @see com.google.user.client.ui.TabLayoutPanel.<p>
 * 
 * Layout class for a panel with several tabs. The tabbed panel should be set inside a widget with given width and height.
 * For table based layouts the height of the parent cell should be set explicitly.
 * 
 * As layout options two height for the tabbar are provided: 32px("standard") and 25px("small").
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsTabbedPanel extends Composite {

    /** The TabLayoutPanel widget. */
    private TabLayoutPanel m_tabPanel;

    /** Enumeration with layout keys. */
    public enum CmsTabLayout {
        /** Standard layout size. */
        standard("32"),

        /** Small layout size. */
        small("25");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private CmsTabLayout(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /** The default tabbar height. */
        public static final CmsTabLayout DEFAULT = standard;

    }

    /**
     * The default constructor for an empty tabbed panel. <p>
     */
    public CmsTabbedPanel() {

        m_tabPanel = new TabLayoutPanel(Double.parseDouble(CmsTabLayout.DEFAULT.getName()), Unit.PX);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabPanel);

        List<Element> tabBarDivs = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabs(),
            CmsDomUtil.Tag.div,
            m_tabPanel.getElement());

        // set an additional css class for the parent element of the .gwt-TabLayoutPanelTabs element
        if (tabBarDivs.size() == 1) {
            tabBarDivs.get(0).getParentElement().setClassName(
                I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabBar());
        }

        I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        m_tabPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanel());
    }

    /**
     * The constructor for an empty tabbed panel. <p>
     * 
     * @param tabbarHeight the pre-defined height of the tabbar, can be "small" or "standard"      
     */
    public CmsTabbedPanel(CmsTabLayout tabbarHeight) {

        m_tabPanel = new TabLayoutPanel(Double.parseDouble(tabbarHeight.getName()), Unit.PX);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabPanel);

        Element tabRootEl = m_tabPanel.getElement();
        // set an additional css class for the parent element of the .gwt-TabLayoutPanelTabs element
        List<Element> tabBarDivs = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabs(),
            CmsDomUtil.Tag.div,
            tabRootEl);
        if (tabBarDivs.size() == 1) {
            tabBarDivs.get(0).getParentElement().setClassName(
                I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabBar());
        }

        I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        m_tabPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanel());
    }

    /**
     * The constructor for an empty tabbed panel. <p>
     * 
     * @param tabbarHeight the pre-defined height of the tabbar, can be "small" or "standard" 
     * @param isInside if true an additional padding will be added, so that the tabbed panel can be inside a widget with border
     */
    public CmsTabbedPanel(CmsTabLayout tabbarHeight, boolean isInside) {

        this(tabbarHeight);
        if (isInside) {
            m_tabPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsInternalTab());
        }
    }

    /**
     * Add a new tab with the provided name and content.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#add(Widget, String)}
     * 
     * @param tabContent the widget to add as a tab 
     * @param tabName the name of the tab to display in the tabbar
     */
    public void add(Widget tabContent, String tabName) {

        m_tabPanel.add(tabContent, tabName);

    }

    /**
     * Add a new tab with the provided name and content and additional left margin.<p>
     * 
     * @param tabContent the widget to add as a tab 
     * @param tabName the name of the tab to display in the tabbar
     */
    public void addWithLeftMargin(Widget tabContent, String tabName) {

        m_tabPanel.add(tabContent, tabName);

        int tabIndex = m_tabPanel.getWidgetIndex(tabContent);
        Element tabRootEl = m_tabPanel.getElement();
        // set an additional css class for the parent element of the .gwt-TabLayoutPanelTabs element
        List<Element> tabDivs = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTab(),
            CmsDomUtil.Tag.div,
            tabRootEl);
        if ((tabDivs != null) && (tabDivs.size() > tabIndex)) {
            tabDivs.get(tabIndex).addClassName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().tabLeftMargin());
            I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        }
    }

    /**
     * Inserts a widget into the panel. If the Widget is already attached, it will be moved to the requested index.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#insert(Widget, String, int)}
     * 
     * @param tabContent the widget to be added
     * @param tabName the text to be shown on its tab
     * @param beforeIndex  the index before which it will be inserted
     */
    public void insert(Widget tabContent, String tabName, int beforeIndex) {

        m_tabPanel.insert(tabContent, tabName, beforeIndex);
    }

    /**
     * Programmatically selects the specified tab.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#selectTab(int index)}
     * 
     * @param tabIndex the index of the tab to be selected
     */
    public void selectTab(int tabIndex) {

        m_tabPanel.selectTab(tabIndex);
    }

    /**
     * Gets the index of the currently-selected tab.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#getSelectedIndex()}
     * 
     * @return the selected index, or -1 if none is selected.
     */
    public int getSelectedIndex() {

        return m_tabPanel.getSelectedIndex();

    }

    /**
     * Gets the child widget at the specified index.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#getWidget(int)}
     * 
     * @param tabIndex the child widget's index
     * @return the child widget
     */
    public Widget getWidget(int tabIndex) {

        return m_tabPanel.getWidget(tabIndex);
    }

    /**
     * Gets the number of child widgets in this panel.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#getWidgetCount()}
     * 
     * @return the number of children
     */
    public int getTabCount() {

        return m_tabPanel.getWidgetCount();
    }

    /**
     * Add the before selection handler to the tabbed panel.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#addBeforeSelectionHandler(BeforeSelectionHandler)}
     * 
     * @param handler the before selection handler
     * @return the registration for the event
     */
    public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {

        return m_tabPanel.addBeforeSelectionHandler(handler);
    }

    /**
     * Adds a SelectionEvent handler to the tabbed panel.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#addSelectionHandler(SelectionHandler)}
     * 
     * @param handler the selection handler
     * @return the registration for the event
     */
    public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {

        return m_tabPanel.addSelectionHandler(handler);
    }
}
