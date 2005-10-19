/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/CmsPointerLinkValidationResult.java,v $
 * Date   : $Date: 2005/10/19 09:48:05 $
 * Version: $Revision: 1.6.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.validation;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores the result of a pointer link validation. <p>
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.6.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPointerLinkValidationResult {

    /**  The broken links that were found.<p> */
    private Map m_brokenLinks;

    /**  The date of the validation.<p> */
    private Date m_validationDate;

    /**
     * Constructs a new pointer link validation result.<p>
     * 
     * @param brokenLinks a list of the broken links
     */
    public CmsPointerLinkValidationResult(Map brokenLinks) {

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

        if (m_brokenLinks.size() > 0) {
            StringBuffer result = new StringBuffer(1024);
            Iterator brokenLinks = m_brokenLinks.entrySet().iterator();
            result.append(
                Messages.get().key(
                    locale,
                    Messages.GUI_LINK_VALIDATION_RESULTS_INTRO_1,
                    new Object[] {m_validationDate})).append("<ul>");
            while (brokenLinks.hasNext()) {
                Entry link = (Map.Entry)brokenLinks.next();
                String linkPath = (String)link.getKey();
                String linkUrl = (String)link.getValue();
                String msg = Messages.get().key(
                    locale,
                    Messages.GUI_LINK_POINTING_TO_2,
                    new Object[] {linkPath, linkUrl});
                result.append("<li>").append(msg).append("</li>");
            }
            return result.append("</ul>").toString();
        } else {
            return Messages.get().key(
                locale,
                Messages.GUI_LINK_VALIDATION_RESULTS_ALL_VALID_1,
                new Object[] {m_validationDate});
        }
    }
}