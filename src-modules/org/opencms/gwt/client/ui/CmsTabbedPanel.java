/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsTabbedPanel.java,v $
 * Date   : $Date: 2010/03/09 10:25:41 $
 * Version: $Revision: 1.1 $
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper class for @see com.google.user.client.ui.TabLayoutPanel
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
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
        standard("30"),

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

        /** The default configuration gallery key. */
        public static final CmsTabLayout DEFAULT = standard;

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

        // TODO: USe util class aus OpenCms 
        // get all div elements
        NodeList<Element> divElements = m_tabPanel.getElement().getElementsByTagName("div");
        // iterate over the div elements and get the node with the "class" attribute "gwt-TabLayoutPanelTabs"
        for (int i = 0; i < divElements.getLength(); i++) {
            Element divElement = divElements.getItem(i);
            if (divElement.getClassName().contains(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabs())) {
                divElement.getParentElement().setClassName(
                    I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanelTabBar());
                break;
            }
        }

        I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        m_tabPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().cmsTabLayoutPanel());
    }

    /**
     * Add a new tab with the provided name and content. <p>
     * 
     * @param tabContent the widget to add as a tab 
     * @param tabName the name of the tab to display in the tabbar
     */
    public void add(Widget tabContent, String tabName) {

        m_tabPanel.add(tabContent, tabName);

    }

    // TODO: extend the inteface. Add a function to add tab with picture and text as name.

    /**
     * Programmatically selects the specified tab. <p>
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

}
