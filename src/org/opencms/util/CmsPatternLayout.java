/*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * Extends the default pattern layout of log4j by adding functionality for filtering the
 * stack traces output.<p>
 *
 * CAUTION: Do not use classes which instantiate a logger in this class!!!<p>
 *
 * Usage (log4j.properties):<br/>
 * log4j.appender.OC.layout=org.opencms.util.CmsPatternLayout<br/>
 * log4j.appender.OC.layout.ConversionPattern=%d{DATE} %5p [%30.30C:%4L] %m%n<br/>
 * log4j.appender.OC.layout.Filter=org.apache.tomcat,org.apache.catalina,org.apache.coyote<br/>
 * log4j.appender.OC.layout.Exclude=org.opencms.workplace.list.A_CmsListDialog<br/>
 * log4j.appender.OC.layout.MaxLength=5<p>
 *
 * @since 7.0.5
 */
public class CmsPatternLayout extends PatternLayout {

    /** List of class names which prevents displaying the stack trace. */
    private List<String> m_excludes;

    /** List of class names which should be filtered. */
    private List<String> m_filters;

    /** Maximum length of the filtered stack trace. */
    private int m_maxLength;

    /**
     * Default constructor.<p>
     */
    public CmsPatternLayout() {

        this(DEFAULT_CONVERSION_PATTERN);
    }

    /**
     * Constructs a PatternLayout using the supplied conversion pattern.
     *
     * @param pattern the pattern to use for the layout
     */
    CmsPatternLayout(String pattern) {

        super(pattern);
        m_filters = new ArrayList<String>();
        m_excludes = new ArrayList<String>();
        m_maxLength = Integer.MAX_VALUE;
    }

    /**
     * @see org.apache.log4j.PatternLayout#format(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public String format(LoggingEvent event) {

        String result = super.format(event);

        ThrowableInformation ti = event.getThrowableInformation();
        if (ti != null) {
            // buffer for the complete filtered trace
            StringBuffer trace = new StringBuffer();
            // buffer for the minimum trace if an exclusion matches
            StringBuffer minTrace = new StringBuffer();

            boolean exclFound = false;
            int count = 0;
            int filtered = 0;
            int truncated = 0;

            String[] elements = ti.getThrowableStrRep();
            for (int i = 0; i < elements.length; i++) {
                String elem = elements[i];

                // if entry not start with "at" -> put in minimum trace
                if (!elem.trim().startsWith("at ") && !elem.trim().startsWith("...")) {
                    minTrace.append(elem).append(Layout.LINE_SEP);
                }

                // if cause trace starts reset counter (subtrace)
                if (elem.trim().startsWith("Caused")) {
                    if (!exclFound && ((truncated > 0) || (filtered > 0))) {
                        trace.append(createSummary(truncated, filtered));
                    }
                    count = 0;
                    filtered = 0;
                    truncated = 0;
                }

                // filter the entry
                if (!matches(elem, m_filters) && !exclFound) {
                    if (count < m_maxLength) {
                        trace.append(elem).append(Layout.LINE_SEP);

                        count++;
                    } else {
                        truncated++;
                    }
                } else {
                    filtered++;
                }

                // check for exclusion
                if (!exclFound && matches(elem, m_excludes)) {
                    exclFound = true;
                }
            }

            if (exclFound) {
                result += minTrace.toString();
            } else {
                if ((truncated > 0) || (filtered > 0)) {
                    trace.append(createSummary(truncated, filtered));
                }
                result += trace.toString();
            }
        }

        return result;
    }

    /**
     * @see org.apache.log4j.PatternLayout#ignoresThrowable()
     */
    @Override
    public boolean ignoresThrowable() {

        return false;
    }

    /**
     * Sets an exclusion for preventing the stack trace output.<p>
     *
     * @param exclude the names of a classes (comma separated) which should prevent the stack trace output
     */
    public void setExclude(String exclude) {

        String[] entries = exclude.split(",");
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i].trim();

            if (!entry.startsWith("at ")) {
                entry = "at " + entry;
            }

            m_excludes.add(entry);
        }
    }

    /**
     * Sets a filter for the stack trace output.<p>
     *
     * @param filter the names of a classes (comma separated) which should be filtered in the stack trace output
     */
    public void setFilter(String filter) {

        String[] entries = filter.split(",");
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i].trim();

            if (!entry.startsWith("at ")) {
                entry = "at " + entry;
            }

            m_filters.add(entry);
        }
    }

    /**
     * Sets the maximum length of the stack trace.<p>
     *
     * @param len the maximum length (lines) of the stack trace
     */
    public void setMaxLength(String len) {

        try {
            m_maxLength = Integer.parseInt(len);
        } catch (NumberFormatException ex) {
            m_maxLength = Integer.MAX_VALUE;
        }
    }

    /**
     * Creates a string with the count of filtered and truncated elements.<p>
     *
     * @param truncated the number of truncated elements
     * @param filtered the number of filtered elements
     *
     * @return a string with the count of filtered and truncated elements
     */
    private String createSummary(int truncated, int filtered) {

        StringBuffer result = new StringBuffer(128);

        result.append("\t... ");
        result.append(filtered + truncated);
        result.append(" more (");
        result.append(filtered);
        result.append(" filtered; ");
        result.append(truncated);
        result.append(" truncated)");
        result.append(Layout.LINE_SEP);

        return result.toString();
    }

    /**
     * Checks if the element in the stack trace is filtered.<p>
     *
     * @param element the element in the stack trace to check
     * @param list the list to check against
     *
     * @return true if filtered otherwise false
     */
    private boolean matches(String element, List<String> list) {

        boolean result = false;

        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            String rule = iter.next();

            if (element.trim().startsWith(rule)) {
                return true;
            }
        }

        return result;
    }
}
