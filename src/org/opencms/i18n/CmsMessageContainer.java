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

package org.opencms.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * Contains a localized message key, it's arguments and a <code>{@link I_CmsMessageBundle}</code>.<p>
 * 
 * Used for delaying the actual message lookup from the bundle to the time the message is displayed, 
 * not generated. This is used for localizing internal OpenCms messages. If a message is generated internally by OpenCms,
 * at the time no information about the context of the current user may be available. The message is therefore 
 * passed to the class generating the output, where a user context usually exists. Finally, the message is rendered
 * with the locale of the available user, or the OpenCms default locale if no user is available.<p> 
 * 
 * @since 6.0.0 
 *   
 * @see org.opencms.i18n.I_CmsMessageBundle
 */
public class CmsMessageContainer implements Serializable, I_CmsMessageContainer {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 2844402574674092147L;

    /** The message arguments to use. */
    protected Object[] m_args;

    /** The OpenCms message bundle to read the message from. */
    protected I_CmsMessageBundle m_bundle;

    /** The message key to use. */
    protected String m_key;

    /**
     * Creates a new message container for a key without arguments.<p>
     * 
     * @param bundle the OpenCms message bundle to read the message from
     * @param key the message key to use
     */
    public CmsMessageContainer(I_CmsMessageBundle bundle, String key) {

        m_bundle = bundle;
        m_key = key;
    }

    /**
     * Creates a new message container.<p>
     * 
     * @param bundle the OpenCms message bundle to read the message from
     * @param key the message key to use
     * @param args the message arguments to use
     */
    public CmsMessageContainer(I_CmsMessageBundle bundle, String key, Object... args) {

        m_bundle = bundle;
        m_key = key;
        m_args = args;
    }

    /**
     * Returns the message arguments to use.<p>
     *
     * @return the message arguments to use
     */
    public Object[] getArgs() {

        return m_args;
    }

    /**
     * Returns the message bundle used by this container.<p>
     *
     * @return the message bundle used by this container
     */
    public I_CmsMessageBundle getBundle() {

        return m_bundle;
    }

    /**
     * Returns the message key to use.<p>
     *
     * @return the message key to use
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the localized message described by this container for the OpenCms default locale.<p>
     * 
     * @return the localized message described by this container for the OpenCms default locale
     */
    public String key() {

        if (getBundle() == null) {
            return getKey();
        }
        return getBundle().getBundle().key(getKey(), getArgs());
    }

    /**
     * Returns the localized message described by this container for the given locale.<p>
     * 
     * @param locale the locale to use
     * @return the localized message described by this container for the given locale
     */
    public String key(Locale locale) {

        if (getBundle() == null) {
            return getKey();
        }
        return getBundle().getBundle(locale).key(getKey(), getArgs());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append('[');
        result.append(this.getClass().getName());
        result.append(", bundle: ");
        result.append(getBundle().getBundleName());
        result.append(", key: ");
        result.append(getKey());
        Object[] args = getArgs();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                result.append(", arg");
                result.append(i + 1);
                result.append(": ");
                result.append(args[i]);
            }
        }
        result.append(']');

        return result.toString();
    }
}
