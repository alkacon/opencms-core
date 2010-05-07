/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsSerializationPolicy.java,v $
 * Date   : $Date: 2010/05/07 10:04:37 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.main.CmsLog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.LegacySerializationPolicy;

/**
 * A GWT serialization policy which, in addition to the types allowed by the GWT legacy serialization policy, also
 * allows a list of classes specified in a text file on the class path.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 * 
 */
public final class CmsSerializationPolicy extends SerializationPolicy {

    /** The logger used for this class. */
    private static Log LOG = CmsLog.getLog(CmsSerializationPolicy.class);

    /** The singleton instance of this serialization policy. */
    private static CmsSerializationPolicy m_instance;

    /** The location of the whitelist file on the classpath. */
    private static final String SERIALIZATION_WHITELIST = "org/opencms/gwt/serialization-whitelist.txt";

    /** An instance of the legacy serialization policy. */
    private LegacySerializationPolicy m_legacyPolicy = LegacySerializationPolicy.getInstance();

    /** A set of class names for which serialization should be enabled .*/
    private Set<String> m_whitelist;

    /**
     * Hidden default constructor.<p>
     */
    private CmsSerializationPolicy() {

    }

    /**
     * Returns the singleton instance of this class.<p>
     * 
     * @return the singleton instance of this class 
     */
    public static CmsSerializationPolicy instance() {

        if (m_instance == null) {
            m_instance = new CmsSerializationPolicy();
        }
        return m_instance;
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#getClientFieldNamesForEnhancedClass(java.lang.Class)
     */
    @Override
    public Set<String> getClientFieldNamesForEnhancedClass(Class<?> clazz) {

        return m_legacyPolicy.getClientFieldNamesForEnhancedClass(clazz);
    }

    /**
     * Gets the whitelist of classes for which serialization should also be allowed.<p>
     * 
     * @return the whitelist of class names 
     */
    public Set<String> getWhitelist() {

        if (m_whitelist == null) {
            m_whitelist = readWhitelist();
        }
        return m_whitelist;

    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldDeserializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {

        return m_legacyPolicy.shouldDeserializeFields(clazz) || isClassInWhitelist(clazz);
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldSerializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {

        return m_legacyPolicy.shouldSerializeFields(clazz) || isClassInWhitelist(clazz);
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateDeserialize(java.lang.Class)
     */
    @Override
    public void validateDeserialize(Class<?> clazz) throws SerializationException {

        if (!isClassInWhitelist(clazz)) {
            m_legacyPolicy.validateDeserialize(clazz);
        }
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateSerialize(java.lang.Class)
     */
    @Override
    public void validateSerialize(Class<?> clazz) throws SerializationException {

        if (!isClassInWhitelist(clazz)) {
            m_legacyPolicy.validateSerialize(clazz);
        }
    }

    /**
     * Checks whether the whitelist contains a given class.<p>
     * 
     * @param clazz the class for which it should be checked whether it is in the whitelist
     * 
     * @return true if the whitelist contains the class
     */
    private boolean isClassInWhitelist(Class<?> clazz) {

        return getWhitelist().contains(clazz.getName());
    }

    /**
     * Reads the whitelist of allowed classes from a text file on the classpath.<p>
     * 
     * @return the whitelist as a set of class names.
     */
    private Set<String> readWhitelist() {

        Set<String> result = new HashSet<String>();
        try {
            ClassLoader loader = CmsSerializationPolicy.class.getClassLoader();
            InputStream stream = loader.getResourceAsStream(SERIALIZATION_WHITELIST);
            if (stream == null) {
                throw new Exception(SERIALIZATION_WHITELIST + " not found on classpath!");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    result.add(line);
                }
            }

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

}
