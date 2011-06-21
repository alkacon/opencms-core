/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.impl;

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * A chain of ancestors of a DOM element whose Z-Index can be manipulated.<p>
 * 
 * @since 8.0.0
 */
public final class CmsZIndexChain {

    /**
     * An entry in a z index ancestor chain.<p>
     */
    private static class Entry {

        /** The DOM element. */
        private Element m_element;

        /** If true, the original z-index came from the element itself rather than from the CSS. */
        private boolean m_fromElement;

        /** The original Z index, either directly from the element, or from CSS rules. */
        private Integer m_origZIndex;

        /**
         * Creates a new entry from an element.<p>
         * 
         * @param element the entry's element
         */
        public Entry(Element element) {

            m_element = element;
            Style style = element.getStyle();
            String ownStyle = CmsDomUtil.getZIndex(style);
            String computedStyle = CmsDomUtil.getCurrentStyle(element, CmsDomUtil.Style.zIndex);
            m_fromElement = !(CmsStringUtil.isEmptyOrWhitespaceOnly(ownStyle));
            m_origZIndex = null;
            try {
                m_origZIndex = Integer.valueOf(computedStyle);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        /**
         * Bumps the entry's z index to a given value.<p>
         * 
         * @param newZIndex the new value for the z index 
         */
        public void bump(int newZIndex) {

            Style style = m_element.getStyle();
            style.clearZIndex();
            style.setZIndex(newZIndex);
        }

        /**
         * Returns the original Z index of the element.<p>
         * 
         * @return the original z index 
         */
        public int getOriginalZIndex() {

            if (m_origZIndex == null) {
                return 0;
            }
            return m_origZIndex.intValue();
        }

        /**
         * Resets the entry's z index.<p>
         */
        public void reset() {

            if ((m_origZIndex == null) || !m_fromElement) {
                m_element.getStyle().clearZIndex();
            } else {
                m_element.getStyle().setZIndex(m_origZIndex.intValue());
            }
        }
    }

    /** The list of this chain's entries. */
    private List<Entry> m_entries;

    /**
     * Private constructor to create a Z index chain from a list of entries.<p>
     * 
     * @param entries the list of entries
     */
    private CmsZIndexChain(List<Entry> entries) {

        m_entries = entries;
    }

    /**
     * Creates a z index chain from an element's ancestors.<p>
     * 
     * @param elem the element whose ancestors should be put in the Z index chain
     *  
     * @return the z index chain for the element's ancestors
     */
    public static CmsZIndexChain get(Element elem) {

        List<Entry> entries = new ArrayList<Entry>();
        do {
            Entry entry = new Entry(elem);
            entries.add(entry);
            elem = elem.getParentElement();
        } while (elem != null);
        return new CmsZIndexChain(entries);
    }

    /**
     * Bumps all entries of the chain to a given z index value.<p>
     * 
     * @param zIndex a z index value 
     */
    public void bump(int zIndex) {

        for (Entry entry : m_entries) {
            entry.bump(zIndex);
        }
    }

    /**
     * Returns the maximum z index found in the chain.<p>
     * 
     * @return the maximum z index found in the chain 
     */
    public int getMaxZIndex() {

        int result = 0;
        for (Entry entry : m_entries) {
            result = Math.max(result, entry.getOriginalZIndex());
        }
        return result;
    }

    /**
     * Resets the z indices of all entries in the chain.<p>
     */
    public void reset() {

        for (Entry entry : m_entries) {
            entry.reset();
        }
    }

}
