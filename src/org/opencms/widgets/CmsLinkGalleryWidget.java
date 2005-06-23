/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsLinkGalleryWidget.java,v $
 * Date   : $Date: 2005/06/23 11:11:23 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.util.CmsStringUtil;

/**
 * Provides a widget that allows access to the available OpenCms external link galleries, for use on a widget dialog.<p>
 *
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLinkGalleryWidget extends A_CmsGalleryWidget {

    /**
     * Creates a new external link gallery widget.<p>
     */
    public CmsLinkGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new external link gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsLinkGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsGalleryWidget#getNameLower()
     */
    public String getNameLower() {

        return "link";
    }

    /**
     * @see org.opencms.widgets.A_CmsGalleryWidget#getNameUpper()
     */
    public String getNameUpper() {

        return "Link";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsLinkGalleryWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsGalleryWidget#showPreview(java.lang.String)
     */
    public boolean showPreview(String value) {

        return CmsStringUtil.isNotEmpty(value) && (value.startsWith("http://") || (value.startsWith("https://")));
    }
}