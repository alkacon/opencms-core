/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/I_CmsReport.java,v $
 * Date   : $Date: 2005/06/22 14:19:39 $
 * Version: $Revision: 1.23 $
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

package org.opencms.report;

import org.opencms.i18n.CmsMessageContainer;

import java.util.List;
import java.util.Locale;

/** 
 * This is the interface for the report classes which are used for the output
 * during operations that run on a spearate Thread in OpenCms,
 * like publish, import, export etc.<p>
 * 
 * @author Alexander Kandzior  
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsReport {

    /** The name of the property file. */
    String C_BUNDLE_NAME = "org.opencms.workplace.workplace";

    /** Indicates default formatting. */
    int C_FORMAT_DEFAULT = 0;

    /** Indicates error formatting. */
    int C_FORMAT_ERROR = 5;

    /** Indicates headline formatting. */
    int C_FORMAT_HEADLINE = 2;

    /** Indicates note formatting. */
    int C_FORMAT_NOTE = 3;

    /** Indicates OK formatting. */
    int C_FORMAT_OK = 4;

    /** Indicates warning formatting. */
    int C_FORMAT_WARNING = 1;

    /** Request parameter value that this report should create an "extended" output. */
    String REPORT_TYPE_EXTENDED = "extended";

    /** Request parameter value that this report should create a "simple" output. */
    String REPORT_TYPE_SIMPLE = "simple";

    /**
     * Adds a bundle specified by it's name to the List of resource bundles.<p>
     * 
     * @param bundleName the name of the resource bundle with localized strings
     */
    void addBundle(String bundleName);

    /**
     * Adds an error object to the list of errors that occured during the report.<p>
     * 
     * @param obj the error object
     */
    void addError(Object obj);

    /**
     * Formats the runtime formatted as "hh:mm:ss".<p>
     * 
     * @return the runtime formatted as "hh:mm:ss"
     */
    String formatRuntime();

    /**
     * Returns a list of all errors that occured during the report.<p>
     * 
     * @return an error list that occured during the report
     */
    List getErrors();

    /**
     * Returns the locale this report was initialized with.<p>
     * 
     * @return the locale this report was initialized with
     */
    Locale getLocale();

    /**
     * Updates this report, this processes all new output added since 
     * the last call to this method.<p>
     * 
     * This is only required in case the output is written to a HTML page,
     * if the shell output is used, this will just return an empty String.<p>
     * 
     * @return new elements that have been added to the report and not yet processed.
     */
    String getReportUpdate();

    /** 
     * Returns the time this report has been running.<p>
     * 
     * @return the time this report has been running
     */
    long getRuntime();

    /**
     * Returns if the report generated an error output.<p>
     * 
     * @return true if the report generated an error, otherwise false
     */
    boolean hasError();

    /**
     * Gets the localized resource string for a given message key.<p>
     * 
     * The internal implementation should be passing the
     * <code>keyName</code> to the class {@link org.opencms.i18n.CmsMessages}.<p>
     *
     * @param keyName the key for the desired string
     * @return the resource string for the given key
     * 
     * @see org.opencms.i18n.CmsMessages#key(String)
     */
    String key(String keyName);

    /**
     * Prints a localized message to the report.<p>
     * 
     * @param container the String to add
     */
    void print(CmsMessageContainer container);

    /**
     * Prints a localized message to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param container the String to add
     * @param format the formatting to use for the output
     */
    void print(CmsMessageContainer container, int format);

    /**
     * Adds a line break to the report.<p>
     */
    void println();

    /**
     * Prints a localized message to the report.<p>
     * 
     * @param container the message container to add
     */
    void println(CmsMessageContainer container);

    /**
     * Prints a localized message to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param container the message container to add
     * @param format the formatting to use for the output
     */
    void println(CmsMessageContainer container, int format);

    /**
     * Adds an Exception to the report, ensuring that the Exception content is
     * processed to generate a valid output esp. for HTML pages.<p>
     * 
     * The exception will be stored and the output will later be processed
     * in a special way.<p>    
     * 
     * @param t the exception to add
     */
    void println(Throwable t);

    /**
     * Prints a localized message followed by a parametera and dots to the report.<p>
     * 
     * @param container the Message to add
     * @param param the Parameter to add
     */
    void printMessageWithParam(CmsMessageContainer container, Object param);

    /**
     * Convenience method to print a localized message, followed by a parameter and dots to the report.<p>
     * 
     * The output follows the pattern: ( 3 / 8 ) Deleting filename.txt ...
     * 
     * @param m the number of the report output
     * @param n the total number of report outputs
     * @param container the Message to add
     * @param param the Parameter to add
     * 
     */
    void printMessageWithParam(int m, int n, CmsMessageContainer container, Object param);

    /**
     * Resets the runtime to 0 milliseconds.<p>
     */
    void resetRuntime();
}