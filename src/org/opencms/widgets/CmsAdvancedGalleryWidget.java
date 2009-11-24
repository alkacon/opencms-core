/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/Attic/CmsAdvancedGalleryWidget.java,v $
 * Date   : $Date: 2009/11/24 11:37:55 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsStringUtil;

/**
 * Provides a widget that allows access to the available OpenCms default advanced galleries (e.g. download),
 * for use on a widget dialog.<p>
 * 
 * The accessible types for this widget should be set through configuration. 
 *
 * @author Polina Smagina 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 
 */
public class CmsAdvancedGalleryWidget extends A_CmsAdvancedGalleryWidget {

    /**
     * Creates a new default advanced gallery widget.<p>
     */
    public CmsAdvancedGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new default advanced gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsAdvancedGalleryWidget(String configuration) {

        super(configuration);

    }

    /**
     * @see org.opencms.widgets.A_CmsAdvancedGalleryWidget#getNameLower()
     */
    @Override
    public String getNameLower() {

        return "default";
    }

    /**
     * @see org.opencms.widgets.A_CmsAdvancedGalleryWidget#getNameUpper()
     */
    @Override
    public String getNameUpper() {

        return "Default";
    }

    /**
     * @see org.opencms.widgets.A_CmsAdvancedGalleryWidget#showPreview(java.lang.String)
     */
    @Override
    public boolean showPreview(String value) {

        return CmsStringUtil.isNotEmpty(value) && value.startsWith("/");
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsAdvancedGalleryWidget(getConfiguration());
    }

}
