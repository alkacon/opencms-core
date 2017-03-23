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

import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryStatus;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsFileUtil;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

/**
 * Horizontal Layout to show information about java heap space.<p>
 */
public class CmsJavaHeapInfoLayout extends HorizontalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 7222783783523889843L;

    /**
     * public constructor.<p>
     */
    public CmsJavaHeapInfoLayout() {

        addComponent(getHeapPanel());
        setSpacing(true);
        setMargin(true);
    }

    /**
     * Creates panel with heap information.<p>
     *
     * @return created panel
     */
    private Panel getHeapPanel() {

        CmsMemoryStatus memory = OpenCms.getMemoryMonitor().getMemoryStatus();

        Panel ret = new Panel();

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);

        layout.setSpacing(false);

        layout.addComponent(
            CmsCacheStatisticElements.getTitelElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_FLEXCACHE_LABEL_MEMORY_BLOCK_0)));

        ProgressBar memoryBar = new ProgressBar(memory.getUsage() / 100);
        memoryBar.setWidth(CmsCacheAdminApp.PROGRESSBAR_WIDTH);

        layout.addComponent(memoryBar);

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_MAX_0),
                CmsFileUtil.formatFilesize(
                    memory.getMaxMemory() * 1048576,
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_MAX_HELP_0)));

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_TOTAL_0),
                CmsFileUtil.formatFilesize(
                    memory.getTotalMemory() * 1048576,
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_TOTAL_HELP_0)));

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_USED_0),
                CmsFileUtil.formatFilesize(
                    memory.getUsedMemory() * 1048576,
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_USED_HELP_0)));

        layout.addComponent(
            CmsCacheStatisticElements.getStatisticElement(
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_FREE_0),
                CmsFileUtil.formatFilesize(
                    memory.getFreeMemory() * 1048576,
                    A_CmsUI.getCmsObject().getRequestContext().getLocale()),
                CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_JAVA_HEAP_FREE_HELP_0)));

        ret.setContent(layout);
        ret.setWidth(CmsCacheAdminApp.STATISTIC_INFOBOX_WIDTH);
        return ret;
    }
}
