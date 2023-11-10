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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.Locale;

/**
 * Widget for restriction fields.
 *
 * <p>For now, this does nothing different from a checkbox.
 */
public class CmsAccessRestrictionWidget extends CmsCheckboxWidget {

    /** Configuration key for the JSON configuration. */
    public static final String ATTR_GROUP = "group";

    /**
     * Creates a new instance.
     */
    public CmsAccessRestrictionWidget() {

    }

    /**
     * Creates a new instance.
     *
     * @param configuration the configuration
     */
    public CmsAccessRestrictionWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.CmsCheckboxWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        return "";
    }

    /**
     * @see org.opencms.widgets.CmsCheckboxWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsAccessRestrictionWidget(getConfiguration());
    }

}
