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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.shared.I_CmsEditableDataExtensions;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Helper functions to deal with editable data.
 */
public class CmsEditableDataUtil {

    /**
     * Copies the given editable data extended attributes.
     *
     * @param data the data to copy
     * @return the copied data
     */
    public static I_CmsEditableDataExtensions copy(I_CmsEditableDataExtensions data) {

        String encoded = AutoBeanCodex.encode(CmsCoreProvider.AUTO_BEAN_FACTORY.wrapExtensions(data)).getPayload();
        return AutoBeanCodex.decode(CmsCoreProvider.AUTO_BEAN_FACTORY, I_CmsEditableDataExtensions.class, encoded).as();

    }

    /**
     * Parses editable data extended attributes from JSON.
     *
     * @param jsonData the JSON data
     * @return the editable data extended attributes
     */
    public static I_CmsEditableDataExtensions parseExtensions(String jsonData) {

        return AutoBeanCodex.decode(
            CmsCoreProvider.AUTO_BEAN_FACTORY,
            I_CmsEditableDataExtensions.class,
            jsonData).as();
    }

}
