/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/I_CmsHtmlConverterInterface.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.9 $
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

package com.opencms.htmlconverter;

import java.io.*;

/**
 * Interface definition of CmsHtmlConverter
 * with declaration of necessary methods.<p>
 * 
 * @author Andreas Zahner
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsHtmlConverterInterface {

    /**
     * Configures JTidy from file.<p>
     * 
     * @param fileName filename of JTidy configuration file
     */
    void setTidyConfFile(String fileName);

    /**
     * If defined, returns JTidy configuration filename.<p>
     * 
     * @return filename of JTidy configuration file
     */
    String getTidyConfFile();

    /**
     * Checks whether JTidy is already configured or not.<p>
     * 
     * @return true if JTidy configuration file is set, otherwise false
     */
    boolean tidyConfigured();


    /**
     * Configures CmsHtmlConverter from file.<p>
     * 
     * @param confFile filename of configuration file
     */
    void setConverterConfFile(String confFile);

    /**
     * Configures CmsHtmlConverter from string.<p>
     * 
     * @param configuration string with CmsHtmlConverter configuration
     */
    void setConverterConfString(String configuration);

    /**
     * If defined, returns filename of CmsHtmlConverter configuration file.<p>
     * 
     * @return filename of configuration file
     */
    String getConverterConfFile();

    /**
     * Checks whether CmsHtmlConverter is already configured or not.<p>
     * 
     * @return true if CmsHtmlConverter configuration is set, otherwise false
     */
    boolean converterConfigured();


    /**
     * Checks if HTML code has errors.<p>
     * 
     * @param inString String with HTML code
     * @return true if errors were detected, otherwise false
     */
    boolean hasErrors (String inString);

    /**
     * Checks if HTML code has errors.<p>
     * 
     * @param input InputStream with HTML code
     * @return true if errors were detected, otherwise false
     */
    boolean hasErrors (InputStream input);

    /**
     * Returns the number of found errors in last parsed html code.<p>
     * 
     * @return int with number of errors
     */
    int getNumberErrors();


    /**
     * Checks if HTML code has errors and lists errors.<p>
     * 
     * @param inString String with HTML code
     * @return String with detected errors
     */
    String showErrors (String inString);

    /**
     * Checks if HTML code has errors and lists errors.<p>
     * 
     * @param input InputStream with HTML code
     * @param output OutputStream with detected errors
     */
    void showErrors (InputStream input, OutputStream output);


    /**
     * Transforms HTML code into user defined output.<p>
     * 
     * @param inString String with HTML code
     * @return String with transformed code
     */
    String convertHTML (String inString);

    /**
     * Transforms HTML code into user defined output.<p>
     * 
     * @param in InputStream with HTML code
     * @param out OutputStream with transformed code
     */
    void convertHTML (Reader in, Writer out);

}