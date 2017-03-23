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

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsFileUtil;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for getting a Horizontal Layout with statistics to Image Cache.<p>
 */
public class CmsImageCacheInfoLayout extends HorizontalLayout {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = -3497060774380492083L;

    /**Image Cache Helper.*/
    private CmsImageCacheHelper m_imageCache;

    /**
     * public constructor. <p>
     */
    public CmsImageCacheInfoLayout() {

        m_imageCache = new CmsImageCacheHelper(A_CmsUI.getCmsObject(), false, false, true);

        addComponent(getStatisticsPanel());

        setSpacing(true);
        setMargin(true);
    }

    /**
     * Get Panel with statistics.<p>
     *
     * @return vaadin Panel
     */
    private Panel getStatisticsPanel() {

        Panel ret = new Panel();
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(false);

        //Title

        layout.addComponent(
            CmsCacheStatisticElements.getTitelElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_BLOCK_0)));

        //Statistics

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_KEYS_0),
                String.valueOf(m_imageCache.getFilesCount()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_KEYS_HELP_0)));
        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_VARIATIONS_0),
                String.valueOf(m_imageCache.getVariationsCount()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_STATS_VARIATIONS_HELP_0)));
        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LABEL_MEMORY_BLOCK_0),
                CmsFileUtil.formatFilesize(
                    m_imageCache.getVariationsSize(),
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                ""));

        ret.setContent(layout);

        ret.setWidth(CmsCacheAdminApp.STATISTIC_INFOBOX_WIDTH);

        return ret;
    }
}
