/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsDefaultXmlStringMapper.java,v $
 * Date   : $Date: 2004/12/11 13:20:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsStringMapper;

import java.util.Locale;
import java.util.Map;

/** 
 * Handles the default macro string substitutions. 
 */
public class CmsDefaultXmlStringMapper implements I_CmsStringMapper {

    /** The current OpenCms user context. */
    private CmsObject m_cms;

    /** The handler that uses the mapper. */
    private I_CmsXmlContentHandler m_handler;

    /** The selected locale. */
    private Locale m_locale;

    /** The value to map Strings in. */
    private Map m_values;

    /**
     * Create a new String mapper based on the given parameters.<p>
     * 
     * @param values map of additional values
     * @param locale locale to use
     * @param cms a cms object
     * @param handler TODO:
     */
    public CmsDefaultXmlStringMapper(I_CmsXmlContentHandler handler, Map values, Locale locale, CmsObject cms) {

        m_values = values;
        m_handler = handler;
        m_locale = locale;
        m_cms = cms;
    }

    /**
     * @see org.opencms.util.I_CmsStringMapper#getValue(java.lang.String)
     */
    public String getValue(String key) {

        if (I_CmsStringMapper.KEY_CURRENT_TIME.equals(key)) {
            return String.valueOf(System.currentTimeMillis());
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_NAME.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getName();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_FIRSTNAME.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getFirstname();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_LASTNAME.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getLastname();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_FULLNAME.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getFullName();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_EMAIL.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getEmail();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_STREET.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().currentUser().getAddress();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_ZIP.equals(key) && m_cms != null) {
            return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE);
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_CITY.equals(key) && m_cms != null) {
            return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                I_CmsConstants.C_ADDITIONAL_INFO_TOWN);
        }

        if (I_CmsStringMapper.KEY_REQUEST_URI.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().getUri();
        }

        if (I_CmsStringMapper.KEY_REQUEST_FOLDER.equals(key) && m_cms != null) {
            return CmsResource.getParentFolder(m_cms.getRequestContext().getUri());
        }

        if (I_CmsStringMapper.KEY_REQUEST_ENCODING.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().getEncoding();
        }

        if (I_CmsStringMapper.KEY_REQUEST_LOCALE.equals(key) && m_cms != null) {
            return m_cms.getRequestContext().getLocale().toString();
        }

        if (key.startsWith(I_CmsStringMapper.KEY_LOCALIZED_PREFIX) && m_locale != null) {
            return key(key.substring(I_CmsStringMapper.KEY_LOCALIZED_PREFIX.length()), m_locale);
        }

        if (key.startsWith(I_CmsStringMapper.KEY_LOCALIZED_PREFIX) && m_locale == null) {
            // leave macros for localized keys unchanged if no locale available
            StringBuffer result = new StringBuffer(32);
            result.append(CmsStringUtil.C_MACRO_DELIMITER);
            result.append(CmsStringUtil.C_MACRO_START);
            result.append(key);
            result.append(CmsStringUtil.C_MACRO_END);
            return result.toString();
        }

        if (m_values != null) {
            return (String)m_values.get(key);
        }

        return null;
    }

    /**
     * Returns the localized resource string for a given message key according to the configured resource bundle
     * of this mapper.<p>
     * 
     * If the key was not found in the configuredd bundle, or no bundle is configured for this 
     * content handler, the return value is
     * <code>"??? " + keyName + " ???"</code>.<p>
     * 
     * @param keyName the key for the desired string 
     * @param locale the locale to get the key from
     * 
     * @return the resource string for the given key 
     */
    protected String key(String keyName, Locale locale) {

        CmsMessages messages = m_handler.getMessages(locale);
        if (messages != null) {
            return messages.key(keyName);
        }
        return CmsMessages.formatUnknownKey(keyName);
    }
}