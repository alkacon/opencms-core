/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTabbedPanel.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
 * Version: $Revision: 1.15 $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * @param <E> the tab widget type
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.15 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsTabbedPanel<E extends Widget> extends Composite {

    /** Enumeration with layout keys. */
    public enum CmsTabLayout {
        /** Small layout size. */
        small(25),

        /** Standard layout size. */
        standard(32);

        /** The default tabbar height. */
        public static final CmsTabLayout DEFAULT = standard;

        /** Property name. */
        private int m_barHeight;

        /** 
         * Constructor.<p>
         * 
         * @param barHeight the height of the bar
         */
        private CmsTabLayout(int barHeight) {

            m_barHeight = barHeight;
        }

        /** 
         * Returns the tab bar height.<p>
         * 
         * @return the tab bar height
         */
        public int getBarHeight() {

            return m_barHeight;
        }

    }

    /** The TabLayoutPanel widget. */
    private TabLayoutPanel m_tabPanel;

    /** A map from ids to tabs. */
    private Map<String, E> m_tabsById = new HashMap<String, E>();

    /**
     * The default constructor for an empty tabbed panel. <p>
     */
    public CmsTabbedPanel() {

        this(CmsTabLayout.DEFAULT);
    }

    /**
     * The constructor for an empty tabbed panel. <p>
     * 
     * @param tabbarHeight the pre-defined height of the tabbar, can be "small" or "standard"      
     */
    public CmsTabbedPanel(CmsTabLayout tabbarHeight) {

        m_tabPanel = new TabLayoutPanel(tabbarHeight.getBarHeight(), Unit.PX);

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

        m_tabPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanel());
        m_tabPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());

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
    public void add(E tabContent, String tabName) {

        m_tabPanel.add(tabContent, tabName);

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
     * Adds a tab with a user-defined id.<p>
     * 
     * @param tabContent
     * @param tabName
     * @param tabId
     */
    public void addNamed(E tabContent, String tabName, String tabId) {

        add(tabContent, tabName);
        m_tabsById.put(tabId, tabContent);
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

    /**
     * Add a new tab with the provided name and content and additional left margin.<p>
     * 
     * @param tabContent the widget to add as a tab 
     * @param tabName the name of the tab to display in the tabbar
     */
    public void addWithLeftMargin(E tabContent, String tabName) {

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
        }
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
     * Finds a tab with a given id.<p>
     * 
     * @param tabId a tab id
     *  
     * @return the tab with the given id 
     */
    public E getTabById(String tabId) {

        return m_tabsById.get(tabId);
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
     * Returns the tab text for a given tab.<p>
     * 
     * @param pos the index of the tab 
     * 
     * @return the text of the tab 
     */
    public String getTabText(int pos) {

        return m_tabPanel.getTabWidget(pos).getElement().getInnerText();
    }

    /**
     * Gets the child widget at the specified index.<p>
     * 
     * Wrapper function for {@link com.google.gwt.user.client.ui.TabLayoutPanel#getWidget(int)}
     * 
     * @param tabIndex the child widget's index
     * @return the child widget
     */
    @SuppressWarnings("unchecked")
    public E getWidget(int tabIndex) {

        return (E)m_tabPanel.getWidget(tabIndex);
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
    public void insert(E tabContent, String tabName, int beforeIndex) {

        m_tabPanel.insert(tabContent, tabName, beforeIndex);
    }

    /**
     * Returns an iterator over all tabs.<p>
     * 
     * @return the iterator
     */
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {

        return (Iterator<E>)m_tabPanel.iterator();
    }

    /**
     * Removes the tab with the given index.<p>
     * 
     * @param tabIndex the index of the tab which should be removed 
     */
    public void removeTab(int tabIndex) {

        m_tabPanel.remove(tabIndex);
    }

    /**
     * Delegate method.<p>
     * 
     * @see com.google.gwt.user.client.ui.TabLayoutPanel#selectTab(Widget index)
     * 
     * @param tabWidget the tab widget to select
     */
    public void selectTab(E tabWidget) {

        m_tabPanel.selectTab(tabWidget);
    }

    /**
     * Delegate method.<p>
     * 
     * @see com.google.gwt.user.client.ui.TabLayoutPanel#selectTab(int index)
     * 
     * @param tabIndex the index of the tab to be selected
     */
    public void selectTab(int tabIndex) {

        m_tabPanel.selectTab(tabIndex);
    }

    /**
     * Sets the text of a given tab.<p>
     * 
     * @param pos the index of the tab 
     * @param text the new text for the tab 
     */
    public void setTabText(int pos, String text) {

        m_tabPanel.setTabText(pos, text);

    }
}
