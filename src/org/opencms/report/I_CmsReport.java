/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/I_CmsReport.java,v $
 * Date   : $Date: 2004/01/22 11:50:01 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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


/** 
 * This is the interface for the report classes which are used for the output
 * during operations that run on a spearate Thread in OpenCms,
 * like publish, import, export etc.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * @version $Revision: 1.6 $
 */
public interface I_CmsReport {
        
    /** The name of the property file */
    String C_BUNDLE_NAME = "com.opencms.workplace.workplace";

    /** Indicates default formatting */
    int C_FORMAT_DEFAULT = 0;
    
    /** Indicates headline formatting */
    int C_FORMAT_HEADLINE = 2;
    
    /** Indicates note formatting */
    int C_FORMAT_NOTE = 3;  
    
    /** Indicates OK formatting */
    int C_FORMAT_OK = 4;      
    
    /** Indicates warning formatting */
    int C_FORMAT_WARNING = 1;
    
    /**
     * Adds a bundle specified by it's name to the List of resource bundles.<p>
     * 
     * @param bundleName the name of the resource bundle with localized strings
     * @param locale a 2-letter language code according to ISO 639 
     */
    void addBundle(String bundleName, String locale);
           
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
     * Gets the localized resource string for a given message key.<p>
     * 
     * The internal implementation should be passing the
     * <code>keyName</code> to the class {@link com.opencms.flex.util.CmsMessages}.<p>
     *
     * @param keyName the key for the desired string
     * @return the resource string for the given key
     * 
     * @see com.opencms.flex.util.CmsMessages#key(String)
     */
    String key(String keyName);

    /**
     * Prints a String to the report.<p>
     * 
     * @param value the String to add
     */
    void print(String value);

    /**
     * Prints a String to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param value the String to add
     * @param format the formatting to use for the output
     */
    void print(String value, int format);
        
    /**
     * Adds a line break to the report.<p>
     */
    void println();     

    /**
     * Prints a String with line break to the report.<p>
     *
     * @param value the String to add
     */
    void println(String value);

    /**
     * Prints a String with line break to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param value the String to add
     * @param format the formatting to use for the output
     */
    void println(String value, int format);

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
     * Formats the runtime formatted as "hh:mm:ss".<p>
     * 
     * @return the runtime formatted as "hh:mm:ss"
     */
    String formatRuntime();
    
    /**
     * Resets the runtime to 0 milliseconds.<p>
     */
    void resetRuntime();
}