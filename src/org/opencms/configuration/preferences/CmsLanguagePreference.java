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

package org.opencms.configuration.preferences;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ComparisonChain;

/**
 * Workplace locale preference.<p>
 */
public class CmsLanguagePreference extends CmsBuiltinPreference {

    /** The nice name. */
    private static final String NICE_NAME = "%(key."
        + org.opencms.workplace.commons.Messages.GUI_LABEL_LANGUAGE_0
        + ")";

    /**
     * Creates a new instance.<p>
     *
     * @param name the name
     */
    public CmsLanguagePreference(String name) {

        super(name);
        m_basic = true;
    }

    /**
     * Gets the language selection options, with the locales as keys and the titles as values.
     *
     * @return the map of language selection options
     */
    public static Map<Locale, String> getOptionMapForLanguage() {

        // get available locales from the workplace manager
        List<Locale> locales = new ArrayList<>(OpenCms.getWorkplaceManager().getLocales());
        List<Locale> contentLocales = OpenCms.getLocaleManager().getAvailableLocales();

        // Put locales that are configured as content locales first
        Collections.sort(locales, (a, b) -> {
            int indexA = contentLocales.indexOf(a);
            int indexB = contentLocales.indexOf(b);
            return ComparisonChain.start().compareTrueFirst(indexA != -1, indexB != -1).compare(indexA, indexB).compare(
                a.toString(),
                b.toString()).result();
        });

        Iterator<Locale> i = locales.iterator();
        LinkedHashMap<Locale, String> result = new LinkedHashMap<>();
        for (Locale currentLocale : locales) {
            // add all locales to the select box
            String language = currentLocale.getDisplayLanguage(currentLocale);
            if (CmsStringUtil.isNotEmpty(currentLocale.getCountry())) {
                language = language + " (" + currentLocale.getDisplayCountry(currentLocale) + ")";
            }
            if (CmsStringUtil.isNotEmpty(currentLocale.getVariant())) {
                language = language + " (" + currentLocale.getDisplayVariant(currentLocale) + ")";
            }
            language = StringUtils.capitalize(language);
            result.put(currentLocale, language);
        }
        return result;

    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition() {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            null, //widget
            null, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

    /**
     * @see org.opencms.configuration.preferences.CmsBuiltinPreference#getPropertyDefinition(org.opencms.file.CmsObject)
     */
    @Override
    public CmsXmlContentProperty getPropertyDefinition(CmsObject cms) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String options = getOptionsForLanguage(locale);
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            getName(), //name
            "string", //type
            "select_notnull", //widget
            options, //widgetconfig
            null, //regex
            null, //ruletype
            null, //default
            NICE_NAME, //nicename
            null, //description
            null, //error
            null//preferfolder
        );
        return prop;
    }

    /**
     * Gets the options for the language selector.<p>
     *
     * @param setLocale the locale for the select options
     *
     * @return the options for the language selector
     */
    private String getOptionsForLanguage(Locale setLocale) {

        Map<Locale, String> options = getOptionMapForLanguage();
        String result = options.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(
            Collectors.joining("|"));
        return result;
    }

}
