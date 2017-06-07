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

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;

/**
 * Provides access to the localized messages for several resource bundles simultaneously.<p>
 *
 * Messages are cached for faster lookup. If a localized key is contained in more then one resource bundle,
 * it will be used only from the resource bundle where it was first found in. The resource bundle order is undefined. It is therefore
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a resource bundle.<p>
 *
 * @since 6.0.0
 */
public class CmsMultiMessages extends CmsMessages {

    /**
     * Interface to provide fallback keys to be used when the message for a key is not found.<p>
     */
    public interface I_KeyFallbackHandler {

        /**
         * Gets the fallback key for the given key, or the absent value if there is no fallback key.<p>
         *
         * @param key the original key
         *
         * @return the fallback key
         */
        Optional<String> getFallbackKey(String key);
    }

    /** Constant for the multi bundle name. */
    public static final String MULTI_BUNDLE_NAME = CmsMultiMessages.class.getName();

    /** Null String value for caching of null message results. */
    public static final String NULL_STRING = "null";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsMultiMessages.class);

    /** The key fallback handler. */
    private I_KeyFallbackHandler m_keyFallbackHandler;

    /** A cache for the messages to prevent multiple lookups in many bundles. */
    private Map<String, String> m_messageCache;

    /** List of resource bundles from the installed modules. */
    private List<CmsMessages> m_messages;

    /**
     * Constructor for creating a new messages object initialized with the given locale.<p>
     *
     * @param locale the locale to use for localization of the messages
     */
    public CmsMultiMessages(Locale locale) {

        super();
        // set the bundle name and the locale
        setBundleName(CmsMultiMessages.MULTI_BUNDLE_NAME);
        setLocale(locale);
        // generate array for the messages
        m_messages = new ArrayList<CmsMessages>();
        // use "old" Hashtable since it is the most efficient synchronized HashMap implementation
        m_messageCache = new Hashtable<String, String>();
    }

    /**
     * Adds a bundle instance to this multi message bundle.<p>
     *
     * The added bundle will be localized with the locale of this multi message bundle.<p>
     *
     * @param bundle the bundle instance to add
     */
    public void addBundle(I_CmsMessageBundle bundle) {

        // add the localized bundle to the messages
        addMessages(bundle.getBundle(getLocale()));
    }

    /**
     * Adds a messages instance to this multi message bundle.<p>
     *
     * The messages instance should have been initialized with the same locale as this multi bundle,
     * if not, the locale of the messages instance is automatically replaced. However, this will not work
     * if the added messages instance is in face also of type <code>{@link CmsMultiMessages}</code>.<p>
     *
     * @param messages the messages instance to add
     *
     * @throws CmsIllegalArgumentException if the locale of the given <code>{@link CmsMultiMessages}</code> does not match the locale of this multi messages
     */
    public void addMessages(CmsMessages messages) throws CmsIllegalArgumentException {

        Locale locale = messages.getLocale();
        if (!getLocale().equals(locale)) {
            // not the same locale, try to change the locale if this is a simple CmsMessage object
            if (!(messages instanceof CmsMultiMessages)) {
                // match locale of multi bundle
                String bundleName = messages.getBundleName();
                messages = new CmsMessages(bundleName, getLocale());
            } else {
                // multi bundles with wrong locales can't be added this way
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_MULTIMSG_LOCALE_DOES_NOT_MATCH_2,
                    messages.getLocale(),
                    getLocale()));
            }
        }
        if (!m_messages.contains(messages)) {
            if ((m_messageCache != null) && (m_messageCache.size() > 0)) {
                // cache has already been used, must flush because of newly added keys
                m_messageCache = new Hashtable<String, String>();
            }
            m_messages.add(messages);
        }
    }

    /**
     * Adds a list a messages instances to this multi message bundle.<p>
     *
     * @param messages the messages instance to add
     */
    public void addMessages(List<CmsMessages> messages) {

        if (messages == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_MULTIMSG_EMPTY_LIST_0));
        }

        Iterator<CmsMessages> i = messages.iterator();
        while (i.hasNext()) {
            addMessages(i.next());
        }
    }

    /**
     * Returns the list of all individual message objects in this multi message instance.<p>
     *
     * @return the list of all individual message objects in this multi message instance
     */
    public List<CmsMessages> getMessages() {

        return m_messages;
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getString(java.lang.String)
     */
    @Override
    public String getString(String keyName) {

        return resolveKeyWithFallback(keyName);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#isInitialized()
     */
    @Override
    public boolean isInitialized() {

        return (m_messages != null) && !m_messages.isEmpty();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(java.lang.String, boolean)
     */
    @Override
    public String key(String keyName, boolean allowNull) {

        // special implementation since we uses several bundles for the messages
        String result = resolveKeyWithFallback(keyName);
        if ((result == null) && !allowNull) {
            result = formatUnknownKey(keyName);
        }
        return result;
    }

    /**
     * Sets the key fallback handler.<p>
     *
     * @param fallbackHandler the new key fallback handler
     */
    public void setFallbackHandler(I_KeyFallbackHandler fallbackHandler) {

        m_keyFallbackHandler = fallbackHandler;
    }

    /**
     * Returns the localized resource string for a given message key,
     * checking the workplace default resources and all module bundles.<p>
     *
     * If the key was not found, <code>null</code> is returned.<p>
     *
     * @param keyName the key for the desired string
     * @return the resource string for the given key or null if not found
     */
    private String resolveKey(String keyName) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_RESOLVE_MESSAGE_KEY_1, keyName));
        }

        String result = m_messageCache.get(keyName);
        if (result == NULL_STRING) {
            // key was already checked and not found
            return null;
        }
        boolean noCache = false;
        if (result == null) {
            // so far not in the cache
            for (int i = 0; (result == null) && (i < m_messages.size()); i++) {
                try {
                    result = (m_messages.get(i)).getString(keyName);
                    // if no exception is thrown here we have found the result
                    noCache |= m_messages.get(i).isUncacheable();
                } catch (CmsMessageException e) {
                    // can usually be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getMessage(), e);
                    }
                }
            }
        } else {
            // result was found in cache
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_MESSAGE_KEY_FOUND_CACHED_2, keyName, result));
            }
            return result;
        }
        if (result == null) {
            // key was not found in "regular" bundle as well as module messages
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_MESSAGE_KEY_NOT_FOUND_1, keyName));
            }
            // ensure null values are also cached
            m_messageCache.put(keyName, NULL_STRING);
        } else {
            // optional debug output
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_MESSAGE_KEY_FOUND_2, keyName, result));
            }
            if (!noCache) {
                // cache the result
                m_messageCache.put(keyName, result);
            }
        }
        // return the result
        return result;
    }

    /**
     * Resolves a message key, using the key fallback handler if it is set.<p>
     *
     * @param keyName the key to resolve
     *
     * @return the resolved key
     */
    private String resolveKeyWithFallback(String keyName) {

        String result = resolveKey(keyName);
        if ((result == null) && (m_keyFallbackHandler != null)) {
            Optional<String> fallback = m_keyFallbackHandler.getFallbackKey(keyName);
            if (fallback.isPresent()) {
                result = resolveKey(fallback.get());
            }
        }
        return result;
    }
}