/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsMultiMessages.java,v $
 * Date   : $Date: 2005/07/28 15:18:32 $
 * Version: $Revision: 1.11 $
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

package org.opencms.i18n;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides access to the localized messages for several resource bundles simultaneously.<p>
 * 
 * Messages are cached for faster lookup. If a localized key is contained in more then one resource bundle,
 * it will be used only from the resource bundle where it was first found in. The resource bundle order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a resource bundle.<p>
 * 
 * @author Alexnader Kandzior 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMultiMessages extends CmsMessages {

    /** Null String value for caching of null message results. */
    public static final String NULL_STRING = "null";

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsMultiMessages.class);

    /** A cache for the messages to prevent multiple lookups in many bundles. */
    private Map m_messageCache;

    /** List of resource bundles from the installed modules. */
    private List m_messages;

    /**
     * Constructor for creating a new messages object
     * initialized with the provided bundles.<p>
     * 
     * @param message1 a message
     * @param message2 a message
     */
    public CmsMultiMessages(CmsMessages message1, CmsMessages message2) {

        this(new CmsMessages[] {message1, message2});
    }

    /**
     * Constructor for creating a new messages object
     * initialized with the provided array of bundles.<p>
     * 
     * @param messages array of <code>{@link CmsMessages}</code>, should not be null or empty
     */
    public CmsMultiMessages(CmsMessages[] messages) {

        this(Arrays.asList(messages));
    }

    /**
     * Constructor for creating a new messages object
     * initialized with the provided list of bundles.<p>
     * 
     * @param messages list of <code>{@link CmsMessages}</code>, should not be null or empty
     * 
     * @throws CmsIllegalArgumentException if the given <code>List</code> is null or empty
     */
    public CmsMultiMessages(List messages)
    throws CmsIllegalArgumentException {

        super();
        if ((messages == null) || (messages.size() == 0)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_MULTIMSG_EMPTY_LIST_0));
        }
        // use "old" Hashtable since it is the most efficient synchronized HashMap implementation
        m_messageCache = new Hashtable();
        // set messages
        m_messages = messages;
        // set the locale
        m_locale = ((CmsMessages)m_messages.get(0)).getLocale();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#getString(java.lang.String)
     */
    public String getString(String keyName) {

        return resolveKey(keyName);
    }

    /**
     * @see org.opencms.i18n.CmsMessages#isInitialized()
     */
    public boolean isInitialized() {

        return (m_messages != null) && !m_messages.isEmpty();
    }

    /**
     * @see org.opencms.i18n.CmsMessages#key(java.lang.String, boolean)
     */
    public String key(String keyName, boolean allowNull) {

        // special implementation since we uses several bundles for the messages
        String result = resolveKey(keyName);
        if ((result == null) && !allowNull) {
            result = formatUnknownKey(keyName);
        }
        return result;
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
            LOG.debug(Messages.get().key(Messages.LOG_RESOLVE_MESSAGE_KEY_1, keyName));
        }

        String result = (String)m_messageCache.get(keyName);
        if (result == NULL_STRING) {
            // key was already checked and not found   
            return null;
        }
        if (result == null) {
            // so far not in the cache
            for (int i = 0; (result == null) && (i < m_messages.size()); i++) {
                try {
                    result = ((CmsMessages)m_messages.get(i)).getString(keyName);
                    // if no exception is thrown here we have found the result
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
                LOG.debug(Messages.get().key(Messages.LOG_MESSAGE_KEY_FOUND_CACHED_2, keyName, result));
            }
            return result;
        }
        if (result == null) {
            // key was not found in "regular" bundle as well as module messages
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_MESSAGE_KEY_NOT_FOUND_1, keyName));
            }
            // ensure null values are also cached
            m_messageCache.put(keyName, NULL_STRING);
        } else {
            // optional debug output
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_MESSAGE_KEY_FOUND_2, keyName, result));
            }
            // cache the result
            m_messageCache.put(keyName, result);
        }
        // return the result        
        return result;
    }
}