/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/CmsMessages.java,v $
 * Date   : $Date: 2002/11/04 11:27:00 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 
package com.opencms.flex.util;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Final class to read localized resource Strings from a Java ResourceBundle 
 * that provides some convenience methods to access the Strings from a template.<p>
 * 
 * This class is to be used from templates. Because of that, throwing of 
 * exceptions related to the access of the resource bundle are suppressed
 * so that a template can at last execute. The class that an {@link #isInitialized()) flag
 * that can be checked to see if the instance was properly initilized.
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.0 beta 2
 */
public final class CmsMessages extends Object {  
       
    private ResourceBundle m_bundle; 
    private Locale m_locale;
       
    /**
     * Constructor for the locales.
     * 
     * @param baseName the base classname of the locale
     * @param theLocale the locale to use, eg. "de", "en" etc.
     */
    public CmsMessages( String baseName, Locale locale ) {
        try {
            m_bundle = ResourceBundle.getBundle( baseName, locale );        
        } catch (MissingResourceException e) {
            m_bundle = null;
        }
    }  
    
    /**
     * Checks if the bundle was properly initialized.
     * 
     * @return true if bundle was initialized, false otherwise
     */
    public boolean isInitialized() {
        return (m_bundle != null);
    }
        
    /**
     * Gets a localized resource value for a given key.<p>
     * 
     * If the key could not be looked up, it is returned in a form 
     * <code>"??? keyName ???"</code>.
     * 
     * @param keyName the key for the desired string 
     * @return the string for the given key 
     */
    public String key( String keyName ) {   
        try {            
            if (m_bundle != null) return m_bundle.getString( keyName );
        } catch (MissingResourceException e) {
            // not found, return warning
        }
        return "??? " + keyName + " ???";
    }    
}
