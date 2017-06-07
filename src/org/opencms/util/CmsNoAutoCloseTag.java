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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.htmlparser.nodes.TagNode;

/**
 * A <code>{@link TagNode}</code> with an arbitrary name which is misused for avoiding the creation of
 * the corresponding end tag in case the HTML to parse is not balanced.<p>
 *
 * The trick is: The free name (constructor) is used by the tag factory which allows to use these
 * tags as replacement for the regular ones. And these tags do not extend
 * <code>{@link org.htmlparser.tags.CompositeTag}</code>: They are not supposed to have a closing tag and following tags are
 * not treated as their children but siblings. <p>
 *
 * @since  7.5.1
 *
 */
public class CmsNoAutoCloseTag extends TagNode {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 7794834973417480443L;

    /** The names of this tag. */
    private String[] m_ids;

    /**
     * Creates an instance with the given names.
     *
     * @param ids the names of this tag.
     */
    CmsNoAutoCloseTag(String[] ids) {

        super();
        m_ids = ids;
    }

    /**
     * @see org.htmlparser.nodes.TagNode#getIds()
     */
    @Override
    public String[] getIds() {

        return m_ids;
    }

}
