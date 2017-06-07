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

import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/** Class for keeping the state common for all facet types. */
public class CmsSearchStateFacet implements I_CmsSearchStateFacet {

    /** The checked facet entries. */
    List<String> m_checked;
    /** Map with "facet entry" - "isChecked" pairs. */
    private Map<String, Boolean> m_checkedMap;
    /** Indicator if the configured limit for the maximal number of facet entries should be used. */
    private boolean m_useLimit;
    /** Indicator if checked facet entries should be ignored (when building the Solr query). */
    private boolean m_ignoreChecked;

    /** Default constructor. */
    public CmsSearchStateFacet() {

        m_checked = new LinkedList<String>();
        m_useLimit = true;
        m_ignoreChecked = false;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#addChecked(java.lang.String)
     */
    @Override
    public void addChecked(final String entry) {

        m_checked.add(entry);

    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#clearChecked()
     */
    @Override
    public void clearChecked() {

        m_checked.clear();

    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#getCheckedEntries()
     */
    @Override
    public List<String> getCheckedEntries() {

        return m_checked;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#getIgnoreChecked()
     */
    @Override
    public boolean getIgnoreChecked() {

        return m_ignoreChecked;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#getIsChecked()
     */
    @Override
    public Map<String, Boolean> getIsChecked() {

        if (m_checkedMap == null) {
            m_checkedMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object option) {

                    return Boolean.valueOf(m_checked.contains(option));
                }
            });
        }
        return m_checkedMap;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#getUseLimit()
     */
    @Override
    public boolean getUseLimit() {

        return m_useLimit;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#setIgnoreChecked(boolean)
     */
    @Override
    public void setIgnoreChecked(final boolean ignore) {

        m_ignoreChecked = ignore;
    }

    /**
     * @see org.opencms.jsp.search.state.I_CmsSearchStateFacet#setUseLimit(boolean)
     */
    @Override
    public void setUseLimit(final boolean useLimit) {

        m_useLimit = useLimit;

    }

}
