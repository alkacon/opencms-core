/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentErrorHandler.java,v $
 * Date   : $Date: 2005/06/22 10:38:25 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsLog;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Handler for issues found during XML content validation.<p> 
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $
 * @since 5.5.4
 */
public class CmsXmlContentErrorHandler {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentErrorHandler.class);

    /** The list of validation errors. */
    private Map m_errors;

    /** Indicates that the validated content has errors. */
    private boolean m_hasErrors;

    /** Indicates that the validated content has warnings. */
    private boolean m_hasWarnings;

    /** The list of validation warnings. */
    private Map m_warnings;

    /**
     * Create a new instance of the validation handler.<p>
     */
    public CmsXmlContentErrorHandler() {

        // initialize the internal error / warning list
        m_warnings = new HashMap();
        m_errors = new HashMap();
    }

    /**
     * Adds an error message to the internal list of errors,
     * also raised the "has errors" flag.<p>
     * 
     * @param value the value that contains the error
     * @param message the error message to add
     */
    public void addError(I_CmsXmlContentValue value, String message) {

        m_hasErrors = true;
        Locale locale = value.getLocale();
        Map localeErrors = getLocalIssueMap(m_errors, locale);
        localeErrors.put(value.getPath(), message);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(
                Messages.LOG_XMLCONTENT_VALIDATION_ERR_2,
                value.getPath(),
                message));
        }
    }

    /**
     * Adds an warning message to the internal list of errors,
     * also raised the "has warning" flag.<p>
     * 
     * @param value the value that contians the warning
     * @param message the warning message to add
     */
    public void addWarning(I_CmsXmlContentValue value, String message) {

        m_hasWarnings = true;
        Locale locale = value.getLocale();
        Map localeWarnings = getLocalIssueMap(m_warnings, locale);
        localeWarnings.put(value.getPath(), message);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(
                Messages.LOG_XMLCONTENT_VALIDATION_WARN_2,
                value.getPath(),
                message));
        }
    }

    /**
     * Returns the map of validation errors.<p>
     *
     * The map contains further maps. The key of the "first" map is the 
     * {@link java.util.Locale} of the language where issues where found. The key of the "second" map
     * is a mapping from the element node name obtained with {@link I_CmsXmlContentValue#getPath()} to the error message
     * which is a String.<p>
     *
     * @return the map of validation errors
     */
    public Map getErrors() {

        return m_errors;
    }

    /**
     * Returns the Map of errors for the selected locale.<p>
     * 
     * @param locale the locale to get the errors for
     * 
     * @return the Map of errors for the selected locale
     */
    public Map getErrors(Locale locale) {

        return (Map)m_errors.get(locale);
    }

    /**
     * Returns the map of validation warnings.<p>
     * 
     * The map contains further maps. The key of the "first" map is the 
     * {@link java.util.Locale} of the language where issues where found. The key of the "second" map
     * is a mapping from the element node name obtained with {@link I_CmsXmlContentValue#getPath()} to the error message
     * which is a String.<p>
     *
     * @return the map of validation warnings
     */
    public Map getWarnings() {

        return m_warnings;
    }

    /**
     * Returns the Map of warnings for the selected locale.<p>
     * 
     * @param locale the locale to get the warnings for
     * 
     * @return the Map of warnings for the selected locale
     */
    public Map getWarnings(Locale locale) {

        return (Map)m_warnings.get(locale);
    }

    /**
     * Returns true if the validated content had errors.<p>
     *
     * @return true if the validated content had errors
     */
    public boolean hasErrors() {

        return m_hasErrors;
    }

    /**
     * Returns <code>true</code> if there is at last one error in the selected locale.<p>
     * 
     * @param locale the locale to check
     * 
     * @return <code>true</code> if there is at last one error in the selected locale
     */
    public boolean hasErrors(Locale locale) {

        return null != getErrors(locale);
    }

    /**
     * Returns true if the validated content has warnings.<p>
     *
     * @return true if the validated content had warnings
     */
    public boolean hasWarnings() {

        return m_hasWarnings;
    }

    /**
     * Returns <code>true</code> if there is at last one warning in the selected locale.<p>
     * 
     * @param locale the locale to check
     * 
     * @return <code>true</code> if there is at last one warning in the selected locale
     */
    public boolean hasWarnings(Locale locale) {

        return null != getWarnings(locale);
    }

    /**
     * Returns the localized issue map from the given base map.<p>
     * 
     * If the base map does not contains an issue map for the given locale,
     * a new map is added for the locale.<p>
     * 
     * @param base the base issue map
     * @param locale the locale to get the localized issue map for
     * 
     * @return the localized issue map from the given base map
     */
    private Map getLocalIssueMap(Map base, Locale locale) {

        Map result = (Map)base.get(locale);
        if (result == null) {
            result = new HashMap();
            base.put(locale, result);
        }
        return result;
    }
}