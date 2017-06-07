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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Map;

import org.apache.commons.collections.Transformer;

/** Class for keeping the state of the sorting options. */
public class CmsSearchStateSorting implements I_CmsSearchStateSorting {

    /** The selected sort option. */
    I_CmsSearchConfigurationSortOption m_selected;
    /** Lazy map, to check, if a sort option is selected. */
    Map<I_CmsSearchConfigurationSortOption, Boolean> m_selectedMap;

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateSorting#getCheckSelected()
     */
    @Override
    public Map<I_CmsSearchConfigurationSortOption, Boolean> getCheckSelected() {

        if (m_selectedMap == null) {
            m_selectedMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object option) {

                    return Boolean.valueOf(((I_CmsSearchConfigurationSortOption)option).equals(m_selected));
                }
            });
        }
        return m_selectedMap;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateSorting#getSelected()
     */
    @Override
    public I_CmsSearchConfigurationSortOption getSelected() {

        return m_selected;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateSorting#setSelectedOption(org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption)
     */
    @Override
    public void setSelectedOption(final I_CmsSearchConfigurationSortOption option) {

        m_selected = option;

    }

}
