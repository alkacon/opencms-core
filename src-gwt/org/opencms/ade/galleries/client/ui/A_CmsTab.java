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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;

import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;

/**
 * A tab for the gallery dialog.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsTab extends Composite {

    /** The tab text accessor. */
    protected HasText m_tabTextAccessor;

    /** Flag indicating that the tab is currently selected. */
    private boolean m_isSelected;

    /** The tab id. */
    private String m_tabId;

    /**
     * Constructor.<p>
     *
     * @param tabId the tab id
     */
    protected A_CmsTab(String tabId) {

        m_tabId = tabId;
    }

    /**
     * Clears the selected search parameters on this tab.<p>
     */
    public void clearParams() {

        getTabHandler().clearParams();
    }

    /**
     * Returns the search parameters to display within the result tab.<p>
     *
     * @param searchObj the current search object
     *
     * @return the parameter panel
     */
    public abstract List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj);

    /**
     * Returns the height required by this tab.<p>
     *
     * @return the height
     */
    public abstract int getRequiredHeight();

    /**
     * Returns the tab id.<p>
     *
     * @return the tab id
     */
    public String getTabId() {

        return m_tabId;
    }

    /**
     * Returns if the tab is currently selected.<p>
     *
     * @return <code>true</code> if the tab is currently selected
     */
    public boolean isSelected() {

        return m_isSelected;
    }

    /**
     * Will be triggered when a tab is deselected.<p>
     */
    public void onDeselection() {

        getTabHandler().onDeselection();
        m_isSelected = false;
    }

    /**
     * Adjust content when outer dimensions are changed.<p>
     */
    public void onResize() {

        // implement if required
    }

    /**
     * Will be triggered when a tab is selected.<p>
     */
    public void onSelection() {

        getTabHandler().onSelection();
        m_isSelected = true;
    }

    /**
     * Removes the parameter with the given key from the tab.<p>
     *
     * @param paramKey the parameter key
     */
    public void removeParam(String paramKey) {

        getTabHandler().removeParam(paramKey);
    }

    /**
     * Sets the tab text accessor for this tab.<p>
     *
     * @param tabText the tab text accessor
     */
    public void setTabTextAccessor(HasText tabText) {

        m_tabTextAccessor = tabText;
    }

    /**
     * Returns the tab handler.<p>
     *
     * @return the tab handler
     */
    protected abstract A_CmsTabHandler getTabHandler();

}
