/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/report/Attic/I_CmsReport.java,v $
 * Date   : $Date: 2002/12/12 18:41:36 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2001  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.opencms.report;

import com.opencms.linkmanagement.CmsPageLinks;

/** 
 * This is the interface for the report classes which are used to process the output
 * during database import and export operations.
 * 
 * @author Hanjo Riege
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * 
 * @version $Revision: 1.2 $
 */
public interface I_CmsReport {

    public static final String C_DB_IMPORT_BEGIN = "report.import_db_begin";
    public static final String C_DB_IMPORT_END = "report.import_db_end";
    public static final String C_DB_EXPORT_BEGIN = "report.export_db_begin";
    public static final String C_DB_EXPORT_END = "report.export_db_end";
    public static final String C_MODULE_IMPORT_BEGIN = "report.import_module_begin";
    public static final String C_MODULE_IMPORT_END = "report.import_module_end";
    public static final String C_MODULE_EXPORT_BEGIN = "report.export_module_begin";
    public static final String C_MODULE_EXPORT_END = "report.export_module_end";
    public static final String C_MODULE_DELETE_BEGIN = "report.delete_module_begin";
    public static final String C_MODULE_DELETE_END = "report.delete_module_end";
    public static final String C_PUBLISH_PROJECT_BEGIN = "report.publish_project_begin";
    public static final String C_PUBLISH_PROJECT_END = "report.publish_project_end";
    public static final String C_PUBLISH_RESOURCE_BEGIN = "report.publish_resource_begin";
    public static final String C_PUBLISH_RESOURCE_END = "report.publish_resource_end";
    public static final String C_STATIC_EXPORT_BEGIN = "report.static_export_begin";
    public static final String C_STATIC_EXPORT_END = "report.static_export_end";
    public static final String C_STATIC_EXPORT_NONE = "report.static_export_none";
    public static final String C_LINK_CHECK_BEGIN = "report.check_links_begin";
    public static final String C_LINK_CHECK_END = "report.check_links_end";   

    public static final int C_FORMAT_DEFAULT = 0;
    public static final int C_FORMAT_WARNING = 1;
    public static final int C_FORMAT_HEADLINE = 2;
    public static final int C_FORMAT_NOTE = 3;  
    public static final int C_FORMAT_OK = 4;      
        
    /** The name of the property file */
    public static final String C_BUNDLE_NAME = "com.opencms.report.report";
        
    /**
     * Adds a predefined localized seperator.<p>
     * 
     * @param message indicates which (localized) seperator should be used
     */
    public void addSeperator(String message);

    /**
     * Adds a predefined localized seperator with some additional information.<p>
     *
     * @param message indicates which seperator should be used
     * @param info an optional String that can contain additional information to be added
     */
    public void addSeperator(String message, String info);

    /**
     * Adds the standard seperator.<p>
     */
    public void addSeperator();

    /**
     * Prints a String to the report.<p>
     * 
     * @param value the String to add
     */
    public void print(String value);

    /**
     * Prints a String with line break to the report.<p>
     *
     * @param value the String to add
     */
    public void println(String value);

    /**
     * Prints a String to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param value the String to add
     * @param format the formatting to use for the output
     */
    public void print(String value, int format);

    /**
     * Prints a String with line break to the report, using the indicated formatting.<p>
     * 
     * Use the contants starting with <code>C_FORMAT</code> from this interface
     * to indicate which formatting to use.<p>
     *
     * @param value the String to add
     * @param format the formatting to use for the output
     */
    public void println(String value, int format);
    
    /**
     * Adds a CmsPageLinks object to the report<p>
     *
     * @param value the CmsPageLinks object to add to the report
     */
    public void println(CmsPageLinks value);        

    /**
     * Adds an Exception to the report, ensuring that the Exception content is
     * processed to generate a valid output esp. for HTML pages.<p>
     *
     * @param e the exception to add
     */
    public void println(Throwable t);
        
    /**
     * Updates this report, this processes all new output added since 
     * the last call to this method.<p>
     * 
     * This is only required in case the output is written to a HTML page,
     * if the shell output is used this will just return an empty String.<p>
     * 
     * @return new elements that have been added to the report and not yet processed.
     */
    public String getReportUpdate();
    
    /**
     * Returns <code>true</code> if broken links where reported, <code>false</code>
     * otherwise.
     * 
     * @return <code>true</code> if broken links where reported, <code>false</code>
     * otherwise
     */
    public boolean hasBrokenLinks();
    
    /**
     * Gets the localized resource string for a given message key.<p>
     * 
     * The internal implementation should be passing the
     * keyName to the class {@link com.opencms.flex.util.CmsMessages}.
     *
     * @param keyName the key for the desired string
     * @return the resource string for the given key
     * 
     * @see com.opencms.flex.util.CmsMessages#key(String)
     */
    public String key(String keyName);
}