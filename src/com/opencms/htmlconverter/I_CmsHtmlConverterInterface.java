/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/I_CmsHtmlConverterInterface.java,v $
* Date   : $Date: 2002/09/03 11:57:06 $
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

package com.opencms.htmlconverter;

import java.io.*;

/**
 * Interface definition of CmsHtmlConverter
 * with declaration of necessary methods.
 * @author Andreas Zahner
 * @version 1.0
 */
public interface I_CmsHtmlConverterInterface {

    /**
     * Configures JTidy from file
     * @param fileName filename of JTidy configuration file
     */
    public void setTidyConfFile(String fileName);

    /**
     * If defined, returns JTidy configuration filename
     * @return filename of JTidy configuration file
     */
    public String getTidyConfFile();

    /**
     * Checks whether JTidy is already configured or not
     * @return true if JTidy configuration file is set, otherwise false
     */
    public boolean tidyConfigured();


    /**
     * Configures CmsHtmlConverter from file
     * @param confFile filename of configuration file
     */
    public void setConverterConfFile(String confFile);

    /**
     * Configures CmsHtmlConverter from string
     * @param configuration string with CmsHtmlConverter configuration
     */
    public void setConverterConfString(String configuration);

    /**
     * If defined, returns filename of CmsHtmlConverter configuration file
     * @return filename of configuration file
     */
    public String getConverterConfFile();

    /**
     * Checks whether CmsHtmlConverter is already configured or not
     * @return true if CmsHtmlConverter configuration is set, otherwise false
     */
    public boolean converterConfigured();


    /**
     * Checks if HTML code has errors
     * @param inString String with HTML code
     * @return true if errors were detected, otherwise false
     */
    public boolean hasErrors (String inString);

    /**
     * Checks if HTML code has errors
     * @param in InputStream with HTML code
     * @return true if errors were detected, otherwise false
     */
    public boolean hasErrors (InputStream input);

    /**
     * returns number of found errors in last parsed html code
     * @return int with number of errors
     */
    public int getNumberErrors();


    /**
     * Checks if HTML code has errors and lists errors
     * @param inString String with HTML code
     * @return String with detected errors
     */
    public String showErrors (String inString);

    /**
     * Checks if HTML code has errors and lists errors
     * @param input InputStream with HTML code
     * @param output OutputStream with detected errors
     */
    public void showErrors (InputStream input, OutputStream output);


    /**
     * Transforms HTML code into user defined output
     * @param inString String with HTML code
     * @return String with transformed code
     */
    public String convertHTML (String inString);

    /**
     * Transforms HTML code into user defined output
     * @param input InputStream with HTML code
     * @param output OutputStream with transformed code
     */
    //Gridnine AB Aug 9, 2002
    // byte streams are replaced with character streams to support correct encodings handling
    public void convertHTML (Reader in, Writer out);

}