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

import org.opencms.cache.CmsLruCache;
import org.opencms.flex.CmsFlexCache;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryStatus;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.ProgressBar;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

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
     * Creates in info button for java cache statistics.<p>
     *
     * @return CmsInfoButton
     */
    public static CmsInfoButton getJavaStatisticButton() {

        return getJavaStatisticButton(OpenCms.getMemoryMonitor().getMemoryStatus());

    }

    /**
     * Creates in info button for java cache statistics.<p>
     * @param memory memory object
     *
     * @return CmsInfoButton
     */
    public static CmsInfoButton getJavaStatisticButton(CmsMemoryStatus memory) {

        Map<String, String> infoMap = new LinkedHashMap<String, String>();

        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_MAX_0),
            CmsFileUtil.formatFilesize(
                memory.getMaxMemory() * 1048576,
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_TOTAL_0),
            CmsFileUtil.formatFilesize(
                memory.getTotalMemory() * 1048576,
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_USED_0),
            CmsFileUtil.formatFilesize(
                memory.getUsedMemory() * 1048576,
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_FREE_0),
            CmsFileUtil.formatFilesize(
                memory.getFreeMemory() * 1048576,
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));

        CmsInfoButton info = new CmsInfoButton(infoMap);
        VerticalLayout prog = new VerticalLayout();
        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(
            "<p>" + CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_MEMORY_BLOCK_0) + "</p>");
        prog.addComponent(label);
        prog.addComponent(getProgressBar((((float)memory.getUsage() / 100))));
        info.addAdditionalElement(prog, 0);
        info.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));
        info.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));
        return info;
    }

    /**
     * Gets a Progressbar with css style set.<p>
     *
     * @param value of the bar
     * @return vaadin ProgressBar
     */
    public static ProgressBar getProgressBar(float value) {

        ProgressBar res = new ProgressBar(value);
        String style = "";
        if (value > 0.75) {
            style = "o-nearlyfull";
        }
        if (value > 0.9) {
            style = "o-full";
        }
        res.addStyleName(style);
        return res;
    }

    /**
     * Creates in info button for flex cache statistics.<p>
     *
     * @return CmsInfoButton
     */
    protected static CmsInfoButton getFlexStatisticButton() {

        Map<String, String> infoMap = new LinkedHashMap<String, String>();

        CmsFlexCache cache = OpenCms.getFlexCache();
        CmsLruCache entryLruCache = cache.getEntryLruCache();

        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_KEYS_0),
            String.valueOf(cache.keySize()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_VARIATIONS_0),
            String.valueOf(cache.size()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_MEMORY_MAXSIZE_0),
            CmsFileUtil.formatFilesize(
                entryLruCache.getMaxCacheCosts(),
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));
        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_MEMORY_CURSIZE_0),
            CmsFileUtil.formatFilesize(
                entryLruCache.getObjectCosts(),
                A_CmsUI.getCmsObject().getRequestContext().getLocale()));
        CmsInfoButton info = new CmsInfoButton(infoMap);
        VerticalLayout prog = new VerticalLayout();
        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        label.setValue(
            "<p>" + CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_MEMORY_BLOCK_0) + "</p>");
        prog.addComponent(label);

        prog.addComponent(
            getProgressBar((float)entryLruCache.getObjectCosts() / (float)entryLruCache.getMaxCacheCosts()));
        info.addAdditionalElement(prog, 0);
        info.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));
        info.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEX_0));
        return info;
    }

    /**
     * Creates in info button for image cache statistics.<p>
     *
     * @return CmsInfoButton
     */
    protected static CmsInfoButton getImageStatisticButton() {

        long size = 0L;
        if (new File(CmsImageLoader.getImageRepositoryPath()).exists()) {
            size = FileUtils.sizeOfDirectory(new File(CmsImageLoader.getImageRepositoryPath()));
        }

        Map<String, String> infoMap = new LinkedHashMap<String, String>();

        infoMap.put(
            CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LABEL_MEMORY_BLOCK_0),
            CmsFileUtil.formatFilesize(size, A_CmsUI.getCmsObject().getRequestContext().getLocale()));

        CmsInfoButton info = new CmsInfoButton(infoMap);

        info.setWindowCaption(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGE_0));
        info.setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGE_0));
        return info;
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

        m_uiContext.addToolbarButton(getFlexStatisticButton());
        m_uiContext.addToolbarButton(CmsFlushCache.getFlushToolButton());

        table.setSizeFull();
        return table;
    }

    /**
     * Creates the view for the image cache.<p>
     *
     * @return a vaadin vertical layout with the information about the image cache
     */
    private Component getImageViewComponent() {

        m_siteTableFilter = new TextField();

        HorizontalSplitPanel sp = new HorizontalSplitPanel();
        sp.setSizeFull();
        VerticalLayout intro = CmsVaadinUtils.getInfoLayout(Messages.GUI_CACHE_IMAGE_INTRODUCTION_0);
        VerticalLayout nullResult = CmsVaadinUtils.getInfoLayout(Messages.GUI_CACHE_IMAGE_NO_RESULTS_0);
        final CmsImageCacheTable table = new CmsImageCacheTable(intro, nullResult, m_siteTableFilter);

        sp.setFirstComponent(new CmsImageCacheInput(table));

        VerticalLayout secC = new VerticalLayout();
        secC.setSizeFull();
        secC.addComponent(intro);
        secC.addComponent(nullResult);
        secC.addComponent(table);

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

        m_uiContext.addToolbarButton(getImageStatisticButton());
        m_uiContext.addToolbarButton(CmsFlushCache.getFlushToolButton());
        table.setSizeFull();
        sp.setSecondComponent(secC);
        sp.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);

        table.setVisible(false);
        nullResult.setVisible(false);
        m_siteTableFilter.setVisible(false);

        return sp;
    }
}
