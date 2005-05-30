/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsTableGalleryWidget.java,v $
 * Date   : $Date: 2005/05/30 15:47:41 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

/**
 * Provides a widget that allows access to the available OpenCms table galleries, for use on a widget dialog.<p>
 *
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsTableGalleryWidget extends A_CmsHtmlGalleryWidget {

    /**
     * Creates a new table gallery widget.<p>
     */
    public CmsTableGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new table gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTableGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsHtmlGalleryWidget#getNameLower()
     */
    public String getNameLower() {

        return "table";
    }

    /**
     * @see org.opencms.widgets.A_CmsHtmlGalleryWidget#getNameUpper()
     */
    public String getNameUpper() {

        return "Table";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsTableGalleryWidget(getConfiguration());
    }

}