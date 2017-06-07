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

package org.opencms.util;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;
import org.apache.oro.text.PatternCache;
import org.apache.oro.text.PatternCacheFIFO;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * Provides a resource name / path translation facility.<p>
 *
 * This facility is used for translating new file names that contain
 * illegal chars to legal names. This feature is most useful (and currently
 * only used) for uploaded files. It is also applied to uploded ZIP directories
 * that are extracted after upload.
 * The rules that are used for resource name translation are available from
 * {@link org.opencms.file.CmsRequestContext#getFileTranslator()}.<p>
 *
 * Optionally, resource name translation is also applied to all files read
 * from the VFS, so it can be used for accessing files out of teir usual context.
 * This feature is called directoy translation, and the configured directory
 * translations are available from {@link org.opencms.file.CmsRequestContext#getDirectoryTranslator()}.<p>
 *
 * Directory translation was originally required for backward compatibility
 * to the directory layout before OpenCms 5.0 beta 2. In a modern installation,
 * directory translation is usually disabled.<p>
 *
 * The translations can be configured in <code>opencms-vfs.xml</code>
 * in the <code>opencms\vfs\resources\translations</code> node.<p>
 *
 * The default file name translation setting is:<br>
 * <pre>
 * &lt;filetranslations enabled="true"&gt;
 *    &lt;translation&gt;s#[\s]+#_#g&lt;/translation&gt;
 *    &lt;translation&gt;s#\\#/#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&auml;#ae#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&Auml;#Ae#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&ouml;#oe#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&Ouml;#Oe#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&uuml;#ue#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&Uuml;#Ue#g&lt;/translation&gt;
 *    &lt;translation&gt;s#&szlig;#ss#g&lt;/translation&gt;
 *    &lt;translation&gt;s#[^0-9a-zA-Z_$~\.\-\/]#!#g&lt;/translation&gt;
 *    &lt;translation&gt;s#!+#x#g&lt;/translation&gt;
 * &lt;/filetranslations&gt;
 * </pre><p>
 *
 * Directory translation is now usually not required and since disabled by default.
 * The directory translation setting to convert an OpenCms 5.0 to 6.0 VFS is:<br>
 * <pre>
 * &lt;foldertranslations enabled="true"&gt;
 *    &lt;translation&gt;s#/content/bodys/(.*)#/system/bodies/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/pics/system/(.*)#/system/workplace/resources/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/pics/(.*)#/system/galleries/pics/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/download/(.*)#/system/galleries/download/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/externallinks/(.*)#/system/galleries/externallinks/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/htmlgalleries/(.*)#/system/galleries/htmlgalleries/$1#&lt;/translation&gt;
 *    &lt;translation&gt;s#/content/(.*)#/system/$1#&lt;/translation&gt;
 * &lt;/foldertranslations&gt;
 * </pre><p>
 *
 * @since 6.0.0
 */
public class CmsResourceTranslator {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTranslator.class);

    /** Flag to indicate if one or more matchings should be tried. */
    private boolean m_continueMatching;

    /** Perl5 patter cache to avoid unecessary re-parsing of properties. */
    private PatternCache m_perlPatternCache;

    /** Perl5 utility class. */
    private Perl5Util m_perlUtil;

    /** Internal array containing the translations from opencms.properties. */
    private String[] m_translations;

    /**
     * Constructor for the CmsResourceTranslator.
     *
     * @param translations The array of translations read from the
     *      opencms,properties
     * @param continueMatching if <code>true</code>, matching will continue after
     *      the first match was found
     */
    public CmsResourceTranslator(String[] translations, boolean continueMatching) {

        super();
        m_translations = translations;
        m_continueMatching = continueMatching;
        // pre-cache the patterns
        m_perlPatternCache = new PatternCacheFIFO(m_translations.length + 1);
        for (int i = 0; i < m_translations.length; i++) {
            try {
                m_perlPatternCache.addPattern(m_translations[i]);
            } catch (MalformedPatternException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_MALFORMED_TRANSLATION_RULE_1, m_translations[i]),
                    e);
            }
        }
        // initialize the Perl5Util
        m_perlUtil = new Perl5Util(m_perlPatternCache);
        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_NUM_TRANSLATION_RULES_INITIALIZED_1,
                    new Integer(translations.length)));
        }
    }

    /**
     * Returns a copy of the initialized translation rules.<p>
     *
     * @return String[] a copy of the initialized translation rules
     */
    public String[] getTranslations() {

        String[] copy = new String[m_translations.length];
        System.arraycopy(m_translations, 0, copy, 0, m_translations.length);
        return copy;
    }

    /**
     * Translate a resource name according to the expressions set in
     * <code>opencms-vfs.xml</code>. If no match is found,
     * the resource name is returned unchanged.<p>
     *
     * @param resourceName The resource name to translate
     * @return The translated name of the resource
     */
    public String translateResource(String resourceName) {

        if (m_translations.length == 0) {
            // no translations defined
            return resourceName;
        }
        if (resourceName == null) {
            return null;
        }

        StringBuffer result;
        String current = resourceName;
        int size = current.length() * 2;

        for (int i = 0; i < m_translations.length; i++) {
            result = new StringBuffer(size);
            try {
                if (m_perlUtil.substitute(result, m_translations[i], current) != 0) {

                    if (m_continueMatching) {
                        // continue matching
                        current = result.toString();
                    } else {
                        // first pattern matched, return the result
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(
                                Messages.get().getBundle().key(
                                    Messages.LOG_TRANSLATION_MATCH_3,
                                    new Integer(i),
                                    resourceName,
                                    result));
                        }
                        // Return first match result
                        return result.toString();
                    }
                }
            } catch (MalformedPerl5PatternException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_MALFORMED_TRANSLATION_RULE_1, m_translations[i]),
                    e);
            }
        }

        // the pattern matched, return the result
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_TRANSLATION_MATCH_2, resourceName, current));
        }
        // return last translation (or original if no matching translation found)
        return current;
    }
}