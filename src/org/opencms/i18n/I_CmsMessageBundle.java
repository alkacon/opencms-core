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

package org.opencms.i18n;

import java.util.Locale;

/**
 * Utility interface for generating localized messages for internal OpenCms operations.<p>
 *
 * Every OpenCms core packages should provide one class implementing this interface.
 * This class should be called <code>Messages</code> and be a subclass of
 * <code>{@link org.opencms.i18n.A_CmsMessageBundle}</code>. Moreover, the implementation
 * should contain a <code>public static CmsMessageBundle#get()</code> method, that returns
 * an instance of the class. Also the implementation should contain public static members
 * for all available localized keys.<p>
 *
 * How to use localization in the OpenCms core:<p>
 *
 * There are 3 main ulitiliy classes that deal with localization. These are<ul>
 *
 * <li>{@link I_CmsMessageBundle} (all classes that implement this interface):
 * These classes describe a pointer to a resource bundle, but without providing a {@link java.util.Locale}.
 * So this is like a list of valid keys that can be localized, without the actual localization.</li>
 *
 * <li>{@link org.opencms.i18n.CmsMessages}: This is a fully localized resource bundle,
 * with a list of keys and a {@link java.util.Locale} to localize these keys. If you have a {@link I_CmsMessageBundle},
 * you can use {@link I_CmsMessageBundle#getBundleName()} to initialize a {@link org.opencms.i18n.CmsMessages} instance
 * using {@link org.opencms.i18n.CmsMessages#CmsMessages(String, Locale)}.</li>
 *
 * <li>{@link org.opencms.i18n.CmsMessageContainer}: In some circumstances, you do not have a {@link java.util.Locale},
 * but you want to generate a message that contains certain parameters anyway. For example, in the deeper layers of the
 * OpenCms core an issue may be generated, but you do not have a full user context available. The {@link org.opencms.i18n.CmsMessageContainer}
 * contains a reference to a {@link I_CmsMessageBundle}, plus the name of the key to use in the bundle, plus a list of
 * (optional) parameters. This container is then passed "upwards" in the core until the GUI layer (usually the Workplace)
 * is reached.</li>
 *
 * <li>{@link org.opencms.i18n.CmsMultiMessages}: This is a convenience class that contains a set
 * of {@link org.opencms.i18n.CmsMessages}. A key lookup is automatically done in all the {@link org.opencms.i18n.CmsMessages}
 * instances contained in the multi message instance, and the first match is returned.</li>
 * </ul>
 *
 * @since 6.0.0
 */
public interface I_CmsMessageBundle {

    /**
     * Creates a message container for this package with the given arguments.<p>
     *
     * Convenience method for a message with no arguments.<p>
     *
     * @param key the message key to use
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key);

    /**
     * Creates a message container for this package with the given arguments.<p>
     *
     * Convenience method for a message with one argument.<p>
     *
     * @param key the message key to use
     * @param arg0 the message argument
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0);

    /**
     * Creates a message container for this package with the given arguments.<p>
     *
     * Convenience method for a message with two arguments.<p>
     *
     * @param key the message key to use
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0, Object arg1);

    /**
     * Creates a message container for this package with the given arguments.<p>
     *
     * Convenience method for a message with three arguments.<p>
     *
     * @param key the message key to use
     * @param arg0 the first message argument
     * @param arg1 the second message argument
     * @param arg2 the third message argument
     *
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object arg0, Object arg1, Object arg2);

    /**
     * Creates a message container for this package with the given arguments.<p>
     *
     * @param key the message key to use
     * @param args the message arguments to use
     * @return a message container for this package with the given arguments
     */
    CmsMessageContainer container(String key, Object[] args);

    /**
     * Returns the localized message bundle wrapped in this instance initialized with the OpenCms default locale.<p>
     *
     * This should be used only for logging and system output, not for generating GUI messages that the
     * user can see. In this case, use {@link #getBundle(Locale)}.<p>
     *
     * @return the localized message bundle wrapped in this instance initialized with the OpenCms default locale
     */
    CmsMessages getBundle();

    /**
     * Returns the localized message bundle wrapped in this instance initialized with the provided locale.<p>
     *
     * @param locale the locale to use
     *
     * @return the localized message bundle wrapped in this instance initialized with the provided locale
     */
    CmsMessages getBundle(Locale locale);

    /**
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    String getBundleName();
}