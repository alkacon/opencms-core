/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import static org.opencms.gwt.Messages.GUI_STATUS_TAB_FROM_CP_0;
import static org.opencms.gwt.Messages.GUI_STATUS_TAB_FROM_XML_0;
import static org.opencms.gwt.Messages.GUI_STATUS_TAB_STATUS_0;
import static org.opencms.gwt.Messages.GUI_STATUS_TAB_TO_CP_0;
import static org.opencms.gwt.Messages.GUI_STATUS_TAB_TO_OTHER_0;
import static org.opencms.gwt.Messages.get;
import static org.opencms.gwt.shared.CmsResourceStatusTabId.tabRelationsFrom;
import static org.opencms.gwt.shared.CmsResourceStatusTabId.tabRelationsTo;
import static org.opencms.gwt.shared.CmsResourceStatusTabId.tabStatus;

import org.opencms.gwt.shared.CmsResourceStatusTabId;
import org.opencms.i18n.CmsMessageContainer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Constants for the resource info dialog.<p>
 */
public final class CmsResourceStatusConstants {

    /** Tab configuration for container pages. */
    public static final Map<CmsResourceStatusTabId, CmsMessageContainer> STATUS_TABS_CONTAINER_PAGE;

    /** Tab configuration for contents. */
    public static final Map<CmsResourceStatusTabId, CmsMessageContainer> STATUS_TABS_CONTENT;

    /** Tab configuration for other resources. */
    public static final Map<CmsResourceStatusTabId, CmsMessageContainer> STATUS_TABS_OTHER;

    /**
     * Hidden default constructor.<p>
     */
    private CmsResourceStatusConstants() {

    }

    static {
        LinkedHashMap<CmsResourceStatusTabId, CmsMessageContainer> pageTabs = Maps.newLinkedHashMap();
        pageTabs.put(tabRelationsFrom, get().container(GUI_STATUS_TAB_FROM_CP_0));
        pageTabs.put(tabStatus, get().container(GUI_STATUS_TAB_STATUS_0));
        pageTabs.put(tabRelationsTo, get().container(GUI_STATUS_TAB_TO_CP_0));
        STATUS_TABS_CONTAINER_PAGE = Collections.unmodifiableMap(pageTabs);

        LinkedHashMap<CmsResourceStatusTabId, CmsMessageContainer> contentTabs = Maps.newLinkedHashMap();
        contentTabs.put(tabRelationsFrom, get().container(GUI_STATUS_TAB_FROM_XML_0));
        contentTabs.put(tabStatus, get().container(GUI_STATUS_TAB_STATUS_0));
        contentTabs.put(tabRelationsTo, get().container(GUI_STATUS_TAB_TO_OTHER_0));
        STATUS_TABS_CONTENT = Collections.unmodifiableMap(contentTabs);

        LinkedHashMap<CmsResourceStatusTabId, CmsMessageContainer> otherTabs = Maps.newLinkedHashMap();
        otherTabs.put(tabStatus, get().container(GUI_STATUS_TAB_STATUS_0));
        otherTabs.put(tabRelationsTo, get().container(GUI_STATUS_TAB_TO_OTHER_0));
        STATUS_TABS_OTHER = Collections.unmodifiableMap(otherTabs);
    }

}
