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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigDataInternal.AttributeValue;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Map;

/**
 * Wrapper object for accessing sitemap configuration information from JSPs.
 */
public class CmsJspSitemapConfigWrapper {

    /** The standard context bean instance. */
    private CmsJspStandardContextBean m_context;

    /**
     * Creates a new instance.
     *
     * @param context the standard context bean whose sitemap configuration / CmsObject should be used
     */
    public CmsJspSitemapConfigWrapper(CmsJspStandardContextBean context) {

        m_context = context;
    }

    /**
     * Returns a lazy map that maps sitemap attribute names to sitemap attribute values (as CmsJspObjectValueWrapper instances)
     *
     * @return the lazy map
     */
    public Map<String, CmsJspObjectValueWrapper> getAttribute() {

        return CmsCollectionsGenericWrapper.createLazyMap(key -> {
            Map<String, AttributeValue> attrs = m_context.getSitemapConfigInternal().getAttributes();
            AttributeValue value = attrs.get(key);
            String strValue = value == null ? null : value.getValue();
            if (strValue == null) {
                // CmsJspObjectValueWrapper#createWrapper returns CmsJspObjectValueWrapper#NULL_VALUE_WRAPPER when it receives a null argument,
                // but that object doesn't support getUseDefault(), so we use an empty string here instead of null.
                strValue = "";
            }
            CmsJspObjectValueWrapper wrapper = CmsJspObjectValueWrapper.createWrapper(
                m_context.getCmsObject(),
                strValue);
            return wrapper;
        });
    }

}
