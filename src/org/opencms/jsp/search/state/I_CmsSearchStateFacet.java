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

package org.opencms.jsp.search.state;

import java.util.List;
import java.util.Map;

/** Interface for the state all facet types have in common. */
public interface I_CmsSearchStateFacet {

    /** Add a facet entry to the collection of checked entries.
     * @param entry The facet entry.
     */
    void addChecked(String entry);

    /**
     * Clear the collection of checked entries - i.e., tell that nothing is checked.
     */
    void clearChecked();

    /** Returns all checked entries of a facet.
     * @return List of the facet's entries that are checked.
     */
    List<String> getCheckedEntries();

    /** Returns a flag, indicating if the checked entries should be ignored.
     * @return Flag, indicating if the checked entries should be ignored.
     */
    boolean getIgnoreChecked();

    /** Returns a map that tells for each facet entry (an arbitrary string), if it is checked.
     * @return Map from facet entries to their check state.
     */
    Map<String, Boolean> getIsChecked();

    /** Returns a flag, indicating if the limit for the maximal number of facet entries should be used.
     * @return A flag, indicating if the limit for the maximal number of facet entries should be used.
     */
    boolean getUseLimit();

    /** Set, if the checked facet entries should be ignored. (This influences the generated query part, by (not) adding specific filter queries.)
     * @param ignore Flag, indicating if the checked entries should be ignored or not.
     */
    void setIgnoreChecked(boolean ignore);

    /** Set, if the limit for the maximal number of facet entries should be used.
     * @param useLimit Flag, indicating if the configured limit for the maximal number of facet entries should be used.
     */
    void setUseLimit(boolean useLimit);
}
