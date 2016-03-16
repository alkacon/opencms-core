/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsQuickLaunchData;

import com.google.common.base.Optional;

/**
 * Provides a quick launch menu entry for the page editor and sitemap editor.<p>
 */
public interface I_CmsHasADEQuickLaunchData {

    /**
     * Gets the quick launch data bean for the menu entry.<p>
     *
     * Returns Optional.absent if the entry should not be displayed for the given user or in the given context.<p>
     *
     * @param cms the CMS context
     * @param context the context from which the quick launcher was opened
     * @return the optional quick launch data bean
     */
    Optional<CmsQuickLaunchData> getADEQuickLaunchData(CmsObject cms, String context);

}
