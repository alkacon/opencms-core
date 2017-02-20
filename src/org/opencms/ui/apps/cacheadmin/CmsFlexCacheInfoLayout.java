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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsFileUtil;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

/**
 * Layout to display flex cache statistics and memory data.<p>
 */
public class CmsFlexCacheInfoLayout extends HorizontalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6318933765172254196L;

    /**current flex cache.*/
    private CmsFlexCache m_cache;

    /**
     *  public constructor. <p>
     */
    public CmsFlexCacheInfoLayout() {

        m_cache = OpenCms.getFlexCache();
        addComponent(getStatisticsPanel());

        CmsLruCache entryLruCache = m_cache.getEntryLruCache();

        if (entryLruCache != null) {
            addComponent(getMemoryPanel(entryLruCache));
        }

        setSpacing(true);
        setMargin(true);
    }

    /**
     * Panel with Memory information.<p>
     *
     * @param entryLruCache passed information from CmsFlexCache
     * @return vaadin component
     */
    private Panel getMemoryPanel(CmsLruCache entryLruCache) {

        Panel ret = new Panel();
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(false);

        ProgressBar memoryBar = new ProgressBar(entryLruCache.getObjectCosts() / entryLruCache.getMaxCacheCosts());
        memoryBar.setWidth(CmsCacheAdminApp.PROGRESSBAR_WIDTH);

        layout.addComponent(
            CmsCacheStatisticElements.getTitelElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_BLOCK_0)));
        layout.addComponent(memoryBar);

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_MAXSIZE_0),
                CmsFileUtil.formatFilesize(
                    entryLruCache.getMaxCacheCosts(),
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_MAXSIZE_HELP_0)));
        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_AVGSIZE_0),
                CmsFileUtil.formatFilesize(
                    entryLruCache.getAvgCacheCosts(),
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_AVGSIZE_HELP_0)));
        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_CURSIZE_0),
                CmsFileUtil.formatFilesize(
                    entryLruCache.getObjectCosts(),
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_MEMORY_CURSIZE_HELP_0)));

        ret.setContent(layout);
        ret.setWidth(CmsCacheAdminApp.STATISTIC_INFOBOX_WIDTH);

        return ret;
    }

    /**
     * Panel showing information about memory settings and current use of memory. <p>
     *
     * @return vaadin component
     */
    private Panel getStatisticsPanel() {

        Panel ret = new Panel();
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(false);

        layout.addComponent(
            CmsCacheStatisticElements.getTitelElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_BLOCK_0)));

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_KEYS_0),
                String.valueOf(m_cache.keySize()),
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_KEYS_HELP_0)));

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_VARIATIONS_0),
                String.valueOf(m_cache.size()),
                CmsVaadinUtils.getMessageText(Messages.GUI_FLEXCACHE_LABEL_STATS_VARIATIONS_HELP_0)));

        ret.setContent(layout);
        ret.setWidth("300px");
        ret.setHeight("100%");

        return ret;
    }
}
