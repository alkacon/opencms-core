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

package org.opencms.relations;

import org.opencms.i18n.CmsMessages;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores the result of a pointer link validation. <p>
 *
 * @since 6.0.0
 */
public class CmsExternalLinksValidationResult {

    /**  The broken links that were found.<p> */
    private Map<String, String> m_brokenLinks;

    /**  The date of the validation.<p> */
    private Date m_validationDate;

    /**
     * Constructs a new pointer link validation result.<p>
     *
     * @param brokenLinks a list of the broken links
     */
    public CmsExternalLinksValidationResult(Map<String, String> brokenLinks) {

        m_brokenLinks = brokenLinks;
        m_validationDate = new Date();
    }

    /**
     * Returns a Html representation of this pointer link validation result.<p>
     *
     * @param locale the Locale to display the result in
     *
     * @return a Html representation of this external link validation result
     */
    public String toHtml(Locale locale) {

        CmsMessages mg = Messages.get().getBundle(locale);
        if (m_brokenLinks.size() > 0) {
            StringBuffer result = new StringBuffer(1024);
            Iterator<Entry<String, String>> brokenLinks = m_brokenLinks.entrySet().iterator();
            result.append(mg.key(Messages.GUI_LINK_VALIDATION_RESULTS_INTRO_1, new Object[] {m_validationDate})).append(
                "<ul>");
            while (brokenLinks.hasNext()) {
                Entry<String, String> link = brokenLinks.next();
                String linkPath = link.getKey();
                String linkUrl = link.getValue();
                String msg = mg.key(Messages.GUI_LINK_POINTING_TO_2, new Object[] {linkPath, linkUrl});
                result.append("<li>").append(msg).append("</li>");
            }
            return result.append("</ul>").toString();
        } else {
            return mg.key(Messages.GUI_LINK_VALIDATION_RESULTS_ALL_VALID_1, new Object[] {m_validationDate});
        }
    }
}