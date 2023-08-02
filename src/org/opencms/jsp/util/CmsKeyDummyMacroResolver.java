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

package org.opencms.jsp.util;

import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Macro resolver used to temporarily replace localization message macros with random UUIDs and then replace the UUIDs
 * with the original key after all other macro processing has happened.
 *
 * <p>We need this because we want to preserve the message key, but evaluate macros they may be nested in.
 */
public class CmsKeyDummyMacroResolver extends CmsMacroResolver {

    /** Pattern to match message key macros. */
    public static final Pattern PATTERN_MESSAGE = Pattern.compile("^%\\(key\\.([^\\)]++)\\)$");

    /** Pattern to match UUIDs. */
    private static final Pattern UUID_PATTERN = Pattern.compile(CmsUUID.UUID_REGEX);

    /** The macro resolver to delegate to. */
    private CmsMacroResolver m_delegate;

    /** The map containing the original string for each ID it was replaced with. */
    private Map<CmsUUID, String> m_keys = new HashMap<>();

    /**
     * Creates a new instance
     *
     * @param delegate the macro resolver to delegate to
     */
    public CmsKeyDummyMacroResolver(CmsMacroResolver delegate) {

        m_delegate = delegate;
    }

    /**
     * Extracts the message from a string of the form %(key.{message}), or returns null if the input string
     * does not have this form.
     *
     * @param s the input string
     * @return the key the extracted message key
     */
    public static String getKey(String s) {

        if (s == null) {
            return null;
        }
        Matcher matcher = PATTERN_MESSAGE.matcher(s);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    /**
     * Gets the localization key name of a (potentially nested) key macro in a base string.<p>
     *
     *
     * @param s the base string
     * @param delegate the macro resolver used to resolve macros which the key macro may be nested in
     * @return the localization key
     */
    public static String getKey(String s, CmsMacroResolver delegate) {

        if (s == null) {
            return null;
        }
        CmsKeyDummyMacroResolver resolver = new CmsKeyDummyMacroResolver(delegate);
        String resolved = resolver.resolveMacros(s);
        return getKey(resolved);
    }

    /**
     * @see org.opencms.util.CmsMacroResolver#getMacroValue(java.lang.String)
     */
    @Override
    public String getMacroValue(String macro) {

        if (macro.startsWith(CmsMacroResolver.KEY_LOCALIZED_PREFIX)) {
            String key = macro.substring(CmsMacroResolver.KEY_LOCALIZED_PREFIX.length());
            CmsUUID id = new CmsUUID();
            m_keys.put(id, key);
            return id.toString();
        } else {
            String result = m_delegate.getMacroValue(macro);
            return result;
        }
    }

    /**
     * @see org.opencms.util.CmsMacroResolver#resolveMacros(java.lang.String)
     */
    @Override
    public String resolveMacros(String input) {

        String processedInput = super.resolveMacros(input);
        String result = CmsStringUtil.substitute(UUID_PATTERN, processedInput, (s, matcher) -> {
            CmsUUID id = new CmsUUID(matcher.group());
            if (m_keys.containsKey(id)) {
                return "%(key." + m_keys.get(id) + ")";
            } else {
                return matcher.group();
            }
        });
        return result;
    }

}
