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

import org.opencms.flex.CmsFlexCache;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin app for Cache Administration.<p>
 * Functions: view flex and image caches, flush cashes.<p>
 */
public class CmsCacheAdminApp extends A_CmsWorkplaceApp {

    /**App icon path.*/
    public static final String ICON = "apps/cacheAdmin/cache.png";

    /**App icon path.*/
    public static final String TABLE_ICON = "apps/cache.png";

    /**Path to clean cash options.*/
    static final String PATH_CLEAN = "clean";

    /**Path to flex cache view.*/
    static final String PATH_VIEW_FLEX = "viewflex";

    /**Path to image cache view.*/
    static final String PATH_VIEW_IMAGE = "viewimage";

    /**Width of the progressbars to show current memory usage.*/
    static final String PROGRESSBAR_WIDTH = "200px";

    /**variable for state to view variations.*/
    static final String RESOURCE = "resource";

    /**width of the statistic info boxes.*/
    static final String STATISTIC_INFOBOX_WIDTH = "300px";

    /**Icon for flush caches.*/
    private static final String ICON_CLEAN = "apps/cacheAdmin/cache_clean.png";

    /**Icon for flex cache view.*/
    private static final String ICON_VIEW_FLEX = "apps/cacheAdmin/cache_view_flex.png";

    /**Icon for image cache view.*/
    private static final String ICON_VIEW_IMAGE = "apps/cacheAdmin/cache_view_image.png";

    /** The file table filter input. */
    private TextField m_siteTableFilter;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        // Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_ADMIN_TOOL_NAME_SHORT_0));
            return crumbs;
        }

        //Deeper path
        crumbs.put(
            CmsCacheAdminConfiguration.APP_ID,
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_ADMIN_TOOL_NAME_SHORT_0));

        //View Flex Cache
        if (state.startsWith(PATH_VIEW_FLEX)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));
        } else if (state.startsWith(PATH_VIEW_IMAGE)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGE_0));
        } else if (state.startsWith(PATH_CLEAN)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_0));
        }

        if (crumbs.size() > 1) {
            return crumbs;
        } else {
            return new LinkedHashMap<String, String>(); //size==1 & state was not empty -> state doesn't match to known path
        }
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

        if (state.startsWith(PATH_VIEW_FLEX)) {
            m_rootLayout.setMainHeightFull(true);
            return getFlexViewComponent();
        }

        if (state.startsWith(PATH_VIEW_IMAGE)) {
            m_rootLayout.setMainHeightFull(true);
            return getImageViewComponent();
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            m_rootLayout.setMainHeightFull(false);
            return getStartComponent();
        }
        if (state.startsWith(PATH_CLEAN)) {
            m_rootLayout.setMainHeightFull(false);
            return new CmsFlushCache();
        }

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        List<NavEntry> subNav = new ArrayList<NavEntry>();
        subNav.add(
            new NavEntry(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_FLEX_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_FLEX_HELP_0),
                new ExternalResource(OpenCmsTheme.getImageLink(ICON_VIEW_FLEX)),
                PATH_VIEW_FLEX));

        subNav.add(
            new NavEntry(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_IMAGE_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_VIEW_IMAGE_HELP_0),
                new ExternalResource(OpenCmsTheme.getImageLink(ICON_VIEW_IMAGE)),
                PATH_VIEW_IMAGE));

        subNav.add(
            new NavEntry(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_CLEAN_HELP_0),
                new ExternalResource(OpenCmsTheme.getImageLink(ICON_CLEAN)),
                PATH_CLEAN));

        return subNav;

    }

    /**
     * Reads image path from given state.<p>
     *
     * @param state to be read out
     * @return the path of an image
     */
    String getImagePathFromState(String state) {

        return A_CmsWorkplaceApp.getParamFromState(state, RESOURCE);
    }

    /**
     * Reads resource from state.<p>
     *
     * @param state to be read
     * @return resource as string
     */
    String getResourceFromState(String state) {

        return A_CmsWorkplaceApp.getParamFromState(state, RESOURCE);
    }

    /**
     * Reads resource name from state.<p>
     *
     * @param state to be read
     * @return resource as string
     */
    String getResourceNameFromState(String state) {

        String resource = A_CmsWorkplaceApp.getParamFromState(state, RESOURCE);
        String resName = "";
        if (resource.endsWith(CmsFlexCache.CACHE_OFFLINESUFFIX)) {
            resName = resource.substring(0, resource.length() - CmsFlexCache.CACHE_OFFLINESUFFIX.length());
        }
        if (resource.endsWith(CmsFlexCache.CACHE_ONLINESUFFIX)) {
            resName = resource.substring(0, resource.length() - CmsFlexCache.CACHE_ONLINESUFFIX.length());
        }
        return resName;
    }

    /**
     * Layout for the Flex Cache View includings statistics and cache table.<p>
     *
     * @return vaadin component
     */
    private Component getFlexViewComponent() {

        VerticalLayout layout = new VerticalLayout();
        final CmsFlexCacheTable table = new CmsFlexCacheTable(this);
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
        final CmsImageCacheTable table = new CmsImageCacheTable(this);
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

    /**
     * Creates the component to be shown on accessing the app with some statistical information about the caches.<p>
     *
     * @return a vaadin vertical layout component
     */
    private Component getStartComponent() {

        VerticalLayout outer = new VerticalLayout();
        HorizontalLayout layout = new HorizontalLayout();

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        //Statistic about heap space
        Panel java = new Panel();
        //        java.setWidth("400px");
        java.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_0));
        java.setContent(new CmsJavaHeapInfoLayout());

        layout.addComponent(java);

        Panel flex = new Panel();
        //        flex.setWidth("400px");
        flex.setContent(new CmsFlexCacheInfoLayout());
        flex.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));

        layout.addComponent(flex);

        Panel image = new Panel();
        //        image.setWidth("400px");
        image.setContent(new CmsImageCacheInfoLayout());
        image.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGE_0));

        layout.addComponent(image);
        outer.addComponent(layout);
        return outer;
    }
}
