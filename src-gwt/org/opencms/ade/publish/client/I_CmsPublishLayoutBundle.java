/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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

package org.opencms.ade.publish.client;

import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 *
 * The layout bundle used for the publish module.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsPublishLayoutBundle extends ClientBundle {

    /** The instance of the layout bundle. */
    I_CmsPublishLayoutBundle INSTANCE = GWT.create(I_CmsPublishLayoutBundle.class);

    /**
     * The accessor for the CSS constants bundle.<p>
     *
     * @return the constants bundle
     */
    I_CmsConstantsBundle constants();

    /**
     * The accessor for the CSS bundle.<p>
     *
     * @return a css bundle
     */
    @Source("publish.gss")
    I_CmsPublishCss publishCss();

}
