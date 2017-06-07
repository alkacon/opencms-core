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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the app which shows the content of caches.<p>
 */
public class CmsCacheViewApp extends A_CmsWorkplaceApp {

    /**
     * Modes to run this app.
     */
    public static enum Mode {
        /**Shows FlexCache.*/
        FlexCache,
        /**Shows ImageCache.*/
        ImageCache;
    }

    /**Mode.*/
    private Mode m_mode;

    /** The file table filter input. */
    private TextField m_siteTableFilter;

    /**
     * public constructor.<p>
     *
     * @param mode of app
     */
    public CmsCacheViewApp(Mode mode) {
        m_mode = mode;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            if (Mode.FlexCache.equals(m_mode)) {
                crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_FLEX_0));
            } else {
                crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_IMAGE_0));
            }
            return crumbs;
        }
        return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        //remove filter field
        if (m_siteTableFilter != null) {
            m_infoLayout.removeComponent(m_siteTableFilter);
            m_siteTableFilter = null;
        }

        if (Mode.FlexCache.equals(m_mode)) {
            m_rootLayout.setMainHeightFull(true);
            return getFlexViewComponent();
        }

        if (Mode.ImageCache.equals(m_mode)) {
            m_rootLayout.setMainHeightFull(true);
            return getImageViewComponent();
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;

    }

    /**
     * Layout for the Flex Cache View includings statistics and cache table.<p>
     *
     * @return vaadin component
     */
    private Component getFlexViewComponent() {

        VerticalLayout layout = new VerticalLayout();
        final CmsFlexCacheTable table = new CmsFlexCacheTable();
        m_siteTableFilter = new TextField();
        m_siteTableFilter.setIcon(FontOpenCms.FILTER);
        m_siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_siteTableFilter.setWidth("200px");
        m_siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                table.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_siteTableFilter);

        layout.setSizeFull();
        Component flexCacheStatistics = new CmsFlexCacheInfoLayout();

        table.setSizeFull();
        layout.addComponent(flexCacheStatistics);
        layout.addComponent(table);
        layout.setExpandRatio(table, 1);
        return layout;
    }

    /**
     * Creates the view for the image cache.<p>
     *
     * @return a vaadin vertical layout with the information about the image cache
     */
    private Component getImageViewComponent() {

        VerticalLayout layout = new VerticalLayout();
        final CmsImageCacheTable table = new CmsImageCacheTable();
        m_siteTableFilter = new TextField();
        m_siteTableFilter.setIcon(FontOpenCms.FILTER);
        m_siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_siteTableFilter.setWidth("200px");
        m_siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                table.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_siteTableFilter);

        layout.setSizeFull();
        Component imageCacheStatistics = new CmsImageCacheInfoLayout();
        table.setSizeFull();
        layout.addComponent(imageCacheStatistics);
        layout.addComponent(table);
        layout.setExpandRatio(table, 1);
        return layout;
    }
}
