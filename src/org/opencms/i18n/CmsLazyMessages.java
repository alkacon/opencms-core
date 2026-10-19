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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.i18n;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lazily creates and delegates to another messages instance.<p>
 *
 * The locale, identity comparisons, hash code and string representation are available without
 * invoking the factory. All other public message operations initialize the delegate on first use.
 * The factory must return a non-null instance using the supplied locale. Successful initialization
 * is performed once, including with concurrent callers; failed initialization is retried on the
 * next use.<p>
 *
 * Instances use object identity for equality, so distinct wrappers are not deduplicated when added
 * to {@link CmsMultiMessages}.<p>
 */
public final class CmsLazyMessages extends CmsMessages {

    /** The delegate, guarded by this instance's monitor. */
    private CmsMessages m_delegate;

    /** The factory, released after successful initialization. */
    private Supplier<? extends CmsMessages> m_factory;

    /**
     * Creates a lazy messages wrapper.<p>
     *
     * @param locale the locale, available without initializing the delegate
     * @param factory the factory for the messages instance with that locale
     */
    public CmsLazyMessages(Locale locale, Supplier<? extends CmsMessages> factory) {

        setLocale(Objects.requireNonNull(locale, "locale"));
        m_factory = Objects.requireNonNull(factory, "factory");
        // Ordinary CmsMessages.equals() reads the other instance's bundle name.
        // Use a wrapper-specific name instead of exposing the delegate's value equality.
        setBundleName(getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        return this == obj;
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDate(Date)
     */
    @Override
    public String getDate(Date date) {

        return getDelegate().getDate(date);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDate(Date, int)
     */
    @Override
    public String getDate(Date date, int format) {

        return getDelegate().getDate(date, format);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDate(long)
     */
    @Override
    public String getDate(long time) {

        return getDelegate().getDate(time);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDateTime(Date)
     */
    @Override
    public String getDateTime(Date date) {

        return getDelegate().getDateTime(date);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDateTime(Date, int)
     */
    @Override
    public String getDateTime(Date date, int format) {

        return getDelegate().getDateTime(date, format);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getDateTime(long)
     */
    @Override
    public String getDateTime(long time) {

        return getDelegate().getDateTime(time);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getResourceBundle()
     */
    @Override
    public ResourceBundle getResourceBundle() {

        return getDelegate().getResourceBundle();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getString(String)
     */
    @Override
    public String getString(String keyName) throws CmsMessageException {

        return getDelegate().getString(keyName);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return System.identityHashCode(this);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#isInitialized()
     */
    @Override
    public boolean isInitialized() {

        return getDelegate().isInitialized();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#isUncacheable()
     */
    @Override
    public boolean isUncacheable() {

        return getDelegate().isUncacheable();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String)
     */
    @Override
    public String key(String keyName) {

        return getDelegate().key(keyName);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, boolean)
     */
    @Override
    public String key(String keyName, boolean allowNull) {

        return getDelegate().key(keyName, allowNull);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, Object)
     */
    @Override
    public String key(String key, Object arg0) {

        return getDelegate().key(key, arg0);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, Object, Object)
     */
    @Override
    public String key(String key, Object arg0, Object arg1) {

        return getDelegate().key(key, arg0, arg1);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, Object, Object, Object)
     */
    @Override
    public String key(String key, Object arg0, Object arg1, Object arg2) {

        return getDelegate().key(key, arg0, arg1, arg2);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, Object[])
     */
    @Override
    public String key(String key, Object[] args) {

        return getDelegate().key(key, args);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(String, Object[], Function)
     */
    @Override
    public String key(String key, Object[] args, Function<String, String> unknownKeyFormatter) {

        return getDelegate().key(key, args, unknownKeyFormatter);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#keyDefault(String, String)
     */
    @Override
    public String keyDefault(String keyName, String defaultValue) {

        return getDelegate().keyDefault(keyName, defaultValue);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#keyWithParams(String)
     */
    @Override
    public String keyWithParams(String keyName) {

        return getDelegate().keyWithParams(keyName);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#keyWithParams(String, Function)
     */
    @Override
    public String keyWithParams(String keyName, Function<String, String> unknownKeyFormatter) {

        return getDelegate().keyWithParams(keyName, unknownKeyFormatter);
    }

    /**
     * Returns the delegate, creating it on first use.<p>
     *
     * @return the messages delegate
     */
    private synchronized CmsMessages getDelegate() {

        if (m_delegate == null) {
            m_delegate = Objects.requireNonNull(m_factory.get(), "The messages factory returned null");
            m_factory = null;
        }
        return m_delegate;
    }
}
