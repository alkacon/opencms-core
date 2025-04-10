/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml;

import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Comparator;

/**
 * Comparator for XPaths of an XML content.
 * Paths are sorted according to the sequence definitions in the content definition.
 * That means the sort order corresponds to the order in which the elements
 * are displayed in the content editor.
 */
public class CmsXmlDisplayOrderPathComparator implements Comparator<String> {

    /** The content definition used to determine the order of the node sequence. */
    private CmsXmlContentDefinition m_definition;

    /**
     * Constructs a comparator for paths that fit for the provided content definition.
     * @param definition the content definition to sort paths for.
     */
    public CmsXmlDisplayOrderPathComparator(CmsXmlContentDefinition definition) {

        m_definition = definition;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(String p1, String p2) {

        int firstSlash1 = p1.indexOf('/');
        int firstSlash2 = p2.indexOf('/');
        String firstPart1 = firstSlash1 > 0 ? p1.substring(0, firstSlash1) : p1;
        String firstPart2 = firstSlash2 > 0 ? p2.substring(0, firstSlash2) : p2;
        if (firstPart1.equals(firstPart2)) {
            CmsXmlDisplayOrderPathComparator subComparator = new CmsXmlDisplayOrderPathComparator(
                m_definition.getSchemaType(
                    org.opencms.acacia.shared.CmsContentDefinition.removeIndex(firstPart1)).getContentDefinition());
            return subComparator.compare(
                p1.substring(firstSlash1 + 1, p1.length()),
                p2.substring(firstSlash2 + 1, p2.length()));
        }
        String firstNode1 = CmsXmlUtils.removeXpathIndex(firstPart1);
        int idx1 = CmsXmlUtils.getXpathIndexInt(firstPart1);
        String firstNode2 = CmsXmlUtils.removeXpathIndex(firstPart2);
        int idx2 = CmsXmlUtils.getXpathIndexInt(firstPart2);
        if (firstNode1.equals(firstNode2)) {
            return idx1 - idx2;
        }
        for (I_CmsXmlSchemaType t : m_definition.getTypeSequence()) {
            if (t.getName().equals(firstNode1)) {
                return -1;
            }
            if (t.getName().equals(firstNode2)) {
                return 1;
            }
        }
        return 0;
    }
}
