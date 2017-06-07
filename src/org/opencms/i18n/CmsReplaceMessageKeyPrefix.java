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

package org.opencms.i18n;

import org.opencms.util.CmsStringUtil;

import java.util.Map;

import com.google.common.base.Optional;

/**
 * Message key fallback handler which replaces a given set of prefixes with alternative prefixes.<p>
 */
public class CmsReplaceMessageKeyPrefix implements CmsMultiMessages.I_KeyFallbackHandler {

    /** The prefix substitutions. */
    private Map<String, String> m_substitutions;

    /**
     * Creates a new instance.<p>
     *
     * @param configuration a pipe-separated list of colon-separated key-value pairs, where the key is the prefix and the value is the replacement
     */
    public CmsReplaceMessageKeyPrefix(String configuration) {

        m_substitutions = CmsStringUtil.splitAsMap(configuration, "|", ":");

    }

    /**
     * @see org.opencms.i18n.CmsMultiMessages.I_KeyFallbackHandler#getFallbackKey(java.lang.String)
     */
    public Optional<String> getFallbackKey(String key) {

        for (String prefix : m_substitutions.keySet()) {
            if (key.startsWith(prefix)) {
                return Optional.fromNullable(m_substitutions.get(prefix) + key.substring(prefix.length()));
            }
        }
        return Optional.absent();
    }

}
