/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/locale/Attic/CmsLocaleManager.java,v $
 * Date   : $Date: 2004/01/19 17:14:14 $
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
package org.opencms.locale;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.util.Utils;

import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.collections.ExtendedProperties;

/**
 * @version $Revision: 1.1 $ $Date: 2004/01/19 17:14:14 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsLocaleManager {
    
    private String[] m_defaultLocaleNames;
    
    private String[] m_availableLocaleNames;

    private HashMap m_availableLocales;

    private Locale[] m_defaultLocales;
    
    /**
     * Initializes the CmsLocaleManager by reading the properties
     * <code>locale.available</code>
     * <code>locale.default</code>
     * in <code>opencms.properties</code>
     *
     * @param configuration the configuration from <code>opencms.properties</code>
     */
    public CmsLocaleManager (ExtendedProperties configuration) {

        // init available locales
        m_availableLocaleNames = configuration.getStringArray("locale.available");
        m_availableLocales = new HashMap();
        for (int i = 0; i < m_availableLocaleNames.length; i++) {
            String localeName[] = Utils.split(m_availableLocaleNames[i], "_");
            Locale locale = new Locale(localeName[0],
                    (localeName.length > 1) ? localeName[1] : "",
                    (localeName.length > 2) ? localeName[2] : ""
            );
            
            m_availableLocales.put(m_availableLocaleNames[i], locale);
            if (localeName.length > 2 && !m_availableLocales.containsKey(localeName[0] + "_" + localeName[1])) {
                m_availableLocales.put(localeName[0] + "_" + localeName[1], locale);
            }
            if (localeName.length > 1 && !m_availableLocales.containsKey(localeName[0])) {
                m_availableLocales.put(localeName[0], locale);
            }
        }
    
        // init default locales
        m_defaultLocaleNames = configuration.getStringArray("locale.default");
        m_defaultLocales = new Locale[m_defaultLocaleNames.length];
        for (int i = 0; i < m_defaultLocaleNames.length; i++) {
            String localeName = m_defaultLocaleNames[i];
            m_defaultLocales[i] = (Locale)m_availableLocales.get(localeName);
        }
    }
    
    /**
     * Returns the default locale configured in <code>opencms.properties</code>.<p>
     * 
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        return m_defaultLocales[0];
    }
    
    /** 
     * Returns an array of available locales configured in <code>opencms.properties</code>.<p>
     *
     * @return the array of available locales
     */
    public Locale[] getAvailableLocales() {
        Locale[] availableLocales = new Locale[m_availableLocales.size()];
        for (int i = 0; i < m_availableLocaleNames.length; i++) {
            availableLocales[i] = (Locale)m_availableLocales.get(m_availableLocaleNames[i]);
        }
        return availableLocales;   
    }

    /**
     * Returns an array of default locales configured in <code>opencms.properties</code>.<p>
     *
     * @return the array of default locales
     */
    public Locale[] getDefaultLocales() {
        return m_defaultLocales;
    }

    /**
     * Returns the name of the default locale configured in <code>opencms.properties</code>.<p>
     *
     * The default locale is the first locale of the configured default locales.
     *
     * @return the namer of the default locale
     */
    public String getDefaultLocaleName() {
        return m_defaultLocaleNames[0];
    }

    /**
     * Returns the list of default locale names configured in <code>opencms.properties</code>.<p>
     * 
     * @return the list of default locale names, i.e <code>en, de</code>
     */
    public String getDefaultLocaleNames() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_defaultLocaleNames.length; i++) {
            buf.append(((i > 0) ? "," : "") + m_defaultLocaleNames[i]);
        }
        return buf.toString(); 
    }
    
    /**
     * Returns the list of available locale names configured in <code>opencms.properties</code>.<p>
     *
     * @return the list of available locale names, i.e. <code>en, de</code>
     */
    public String getAvailableLocaleNames() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < m_availableLocaleNames.length; i++) {
            buf.append(((i > 0) ? "," : "") + m_availableLocaleNames[i]);
        }
        return buf.toString();
    }

    /**
     * Returns the default locale for a given resource name.<p>
     * 
     * The default locale is obtained by reading the property <code>locale</code>
     * or as fallback via <code>getDefaultLocale()</code>
     * 
     * @param cms the cms object
     * @param resourcename the name of the resource
     * @return the default locale for the given resource
     */
    public Locale getLocale(CmsObject cms, String resourcename) {

        try {
            String defaultLocales = cms.readProperty(resourcename, I_CmsConstants.C_PROPERTY_LOCALE);
            if (defaultLocales != null) {
                String localeNames[] = Utils.split(defaultLocales, ",");
                for (int i = 0; i < localeNames.length; i++) {
                    Locale l = getLocale(localeNames[i]);
                    if (l != null) {
                        return l;
                    }
                }
            }
        } catch (CmsException exc) {
            // noop, return configured default locale
        }
        
        return getDefaultLocale();
    }

    /**
     * Returns an available locale identified by the given full name.<p>
     * The full name consists of language code, country code(optional), variant(optional) separated by "_"
     * 
     * @param fullName the full name
     * @return the locale or <code>null</code> if not available
     */
    public Locale getLocale(String fullName) {
        String name[] = Utils.split(fullName, "_");
        Locale l;
        
        l = (Locale)m_availableLocales.get(fullName);
        if (l != null) {
            return l;
        }
        
        l = (Locale)m_availableLocales.get(name[0] + "_" + name[1]);
        if (l != null) {
            return l;
        }
        
        l = (Locale)m_availableLocales.get(name[0]);
        if (l != null) {
            return l;
        }
        
        return null;
    }
    
    /**
     * Returns an array of locales available for a given resource name.<p>
     * 
     * The available locales are obtained by reading the property <code>locale-available</code>
     * or as fallback via <code>getAvailableLocales()</code>
     * 
     * @param cms the cms object
     * @param resourcename the name of the resource
     * @return an array of locales for the resource
     */
    public Locale[] getAvailableLocales(CmsObject cms, String resourcename) {
       
        try {
            String availableNames = cms.readProperty(resourcename, I_CmsConstants.C_PROPERTY_AVAILABLE_LOCALES, true);
            if (availableNames != null) {
                String localeNames[] = Utils.split(availableNames, ",");
                Locale locales[] = new Locale[localeNames.length];
                int length = 0;
                for (int i = 0; i < localeNames.length; i++) {
                    if (m_availableLocales.containsKey(localeNames[i])) {
                        locales[length++] = (Locale)m_availableLocales.get(localeNames[i]);
                    }
                }
                
                Locale[] availableLocales = new Locale[length];
                System.arraycopy(locales, 0, availableLocales, 0, length);
                return availableLocales;
            }
        } catch (CmsException exc) {
            // noop, return configured locales
        }

        return getAvailableLocales();
    }
}
