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

package org.opencms.ui.util.table;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to mark up bean properties with metadata for table columns.<p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Expand ratio for the column (use negative values for no expand ratio).<p>
     *
     * @return the expand ratoio
     */
    float expandRatio() default -1.0f;

    /**
     * The message key for the column header (if no given message is found, will be used as a literal header).<p>
     *
     * @return the message key for the column header
     */
    String header();

    /**
     * The order key.<p>
     *
     * The ordering of columns is determined by the relative sizes of their order keys.<p>
     *
     * @return the order key
     */
    int order() default -1;

    String view() default "";

    /**
     * The width (use -1 to not set the width).<p>
     *
     * @return the width
     */
    int width() default -1;

}
