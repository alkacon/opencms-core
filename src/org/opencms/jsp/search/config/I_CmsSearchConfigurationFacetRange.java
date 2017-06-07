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

package org.opencms.jsp.search.config;

import java.util.Collection;

/** The interface a field facet configuration must implement. */
public interface I_CmsSearchConfigurationFacetRange extends I_CmsSearchConfigurationFacet {

    enum Other {
        before, after, between, none, all
    }

    /**
     * Returns the value of facet.range.end for the facet.
     * @return the value of facet.range.end for the facet.
     */
    String getEnd();

    /**
     * Returns the value of facet.range.gap for the facet.
     * @return the value of facet.range.gap for the facet.
     */
    String getGap();

    /**
     * Returns the value of facet.range.hardend for the facet.
     * @return the value of facet.range.hardend for the facet.
     */
    boolean getHardEnd();

    /**
     * Returns the values of facet.range.other for the facet.
     * @return the values of facet.range.other for the facet.
     */
    Collection<Other> getOther();

    /** Returns the numeric index field that is used for the facet, i.e., the value of facet.range.
     * @return The numeric index field that is used for the facet, i.e., the value of facet.range.
     */
    String getRange();

    /**
     * Returns the value of facet.range.start for the facet.
     * @return the value of facet.range.start for the facet.
     */
    String getStart();
}
