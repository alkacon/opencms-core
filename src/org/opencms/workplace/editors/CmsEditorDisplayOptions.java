/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditorDisplayOptions.java,v $
 * Date   : $Date: 2004/08/19 11:26:34 $
 * Version: $Revision: 1.1 $
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
package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides methods to determine the display options of an editor.<p> 
 * 
 * Define your editor display options in the propereties file 
 * /system/workplace/jsp/editors/edit_options.properties.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1.14
 */
public class CmsEditorDisplayOptions {
    
    /** The name of the property file. */
    public static final String C_PROPERTY_FILE = CmsEditor.C_PATH_EDITORS + "edit_options";
    
    private static Properties m_displayOptions;

    /**
     * Constructor that initializes the properties.<p>
     * 
     * @param cms the CmsObject
     */
    public CmsEditorDisplayOptions(CmsObject cms) {      
        if (m_displayOptions == null) {
            // display options were not read, so initialize them
            init(cms);
        }
    }
    
    /**
     * Constructor that initializes the properties.<p>
     * 
     * @param cms the CmsObject
     * @param forceInit if true, display options will always be read new
     */
    public CmsEditorDisplayOptions(CmsObject cms, boolean forceInit) {
        if (forceInit || m_displayOptions == null) {
            // display options were not read or forced to be read, so initialize them
            init(cms);
        }
    }
    
    /**
     * Initializes the display property file from the OpenCms VFS.<p>
     * 
     * @param cms the CmsObject
     */
    private void init(CmsObject cms) {
        m_displayOptions = new Properties();
        synchronized (m_displayOptions) {
            try {
                CmsFile optionFile = cms.readFile(C_PROPERTY_FILE, CmsResourceFilter.IGNORE_EXPIRATION);
                InputStream in = new ByteArrayInputStream(optionFile.getContents());
                m_displayOptions.load(in);
                
            } catch (CmsException e) {
                // set display options to null
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }                
                m_displayOptions = null;
            } catch (IOException e) {
                // set display options to null
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }                       
                m_displayOptions = null;
            }
        }
    }
    
    /**
     * Determines if the given element should be shown in the editor.<p>
     * 
     * @param key the element key name which should be displayed
     * @return true if the element should be shown, otherwise false
     */
    public boolean showElement(String key) {
        return (m_displayOptions != null && "true".equals(m_displayOptions.getProperty(key)));
    }

}
