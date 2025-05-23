/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsDefaultSet;

import com.google.common.base.Predicate;

/**
 * Search result DND filter which excludes elements of types which should not be shown in the current template context.<p>
 */
public class CmsTemplateContextResultDndFilter implements Predicate<CmsResultItemBean> {

    /**
     * @see com.google.common.base.Predicate#apply(java.lang.Object)
     */
    public boolean apply(CmsResultItemBean result) {

        CmsTemplateContextInfo info = CmsContainerpageController.get().getData().getTemplateContextInfo();
        CmsDefaultSet<String> allowed = info.getAllowedContexts().get(result.getType());
        return (allowed == null) || allowed.contains(info.getCurrentContext());
    }
}
