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

import org.opencms.i18n.CmsMultiMessages.I_KeyFallbackHandler;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsRegexSubstitution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;

/**
 * Message key fallback handler used to enable reuse of localization keys for editor labels.<p>
 *
 * This handler, when given a key of the form label.foo.bar (starting with 'label' and containing at least three
 * dot-separated components), removes the second component (in the example, this results in the key label.bar).<p>
 *
 * This can be useful if you want to reuse the same key for fields of multiple content types.
 */
public class CmsRemoveInnerNameFallback implements I_KeyFallbackHandler, I_CmsRegexSubstitution {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoveInnerNameFallback.class);

    /** Regex string. */
    public static final String REGEX_STR = "^label\\.[^.]+\\.(.+)$";

    /** Regex pattern instance. */
    public static final Pattern PATTERN = Pattern.compile(REGEX_STR);

    /**
     * Creates a new instance.<p>
     *
     * @param config the configuration (ignored)
     */
    public CmsRemoveInnerNameFallback(String config) {
        // do nothing, we just need this constructor for reflection purposes
    }

    /**
     * @see org.opencms.i18n.CmsMultiMessages.I_KeyFallbackHandler#getFallbackKey(java.lang.String)
     */
    public Optional<String> getFallbackKey(String key) {

        String result = CmsStringUtil.substitute(PATTERN, key, this);
        if (result.equals(key)) {
            return Optional.absent();
        } else {
            LOG.debug("Replacing message key " + key + " with " + result);
            return Optional.fromNullable(result);
        }
    }

    /**
     * @see org.opencms.util.I_CmsRegexSubstitution#substituteMatch(java.lang.String, java.util.regex.Matcher)
     */
    public String substituteMatch(String string, Matcher matcher) {

        return "label." + matcher.group(1);
    }

}
