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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.main.CmsException;

import com.google.common.base.Optional;
import com.vaadin.ui.Component;

/**
 * Interface for classes which display a comparison widget in the comparison view of the history dialog.
 */
public interface I_CmsDiffProvider {

    /**
     * Optionally returns a comparison component to display for the given resource versions.<p>
     *
     * If no value is returned, then no widget is displayed.
     *
     * @param cms the CMS context
     * @param v1 bean representing the first version
     * @param v2 bean representing the second version
     *
     * @return the optional component to display
     *
     * @throws CmsException if something goes wrong
     */
    Optional<Component> diff(CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2) throws CmsException;

}
