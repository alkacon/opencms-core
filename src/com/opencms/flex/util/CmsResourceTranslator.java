/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/CmsResourceTranslator.java,v $
 * Date   : $Date: 2002/10/18 16:54:23 $
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
 *
 * First created on 13. October 2002
 */

package com.opencms.flex.util;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

/**
 * This class provides a resource name translation facility.<p>
 * 
 * Resource name translation is required for backward compatibility
 * to the old OpenCms (pre 5.0 beta 2) directory layout.
 * It can also be used to translate special characters or 
 * names automatically.<p>
 * 
 * The translations can be configured in the opencms.properties.<p>
 * 
 * TODO: In case performance becomes an issue, a LRU cache could be
 * implemented for translation results.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.0 beta 2
 */
public class CmsResourceTranslator {

    /** Internal array containing the translations from opencms.properties */
    private String[] m_translations = null;
    
    /** Perl5 utility class */
    private Perl5Util m_perlUtil = null;
    
    /** DEBUG flag */
    private static final int DEBUG = 0;
    
    /**
     * Constructor for the CmsResourceTranslator.
     * 
     * @param translations The array of translations read from the 
     *    opencms,properties
     */
    public CmsResourceTranslator(String[] translations) {
        super();
        m_translations = translations;
        m_perlUtil = new Perl5Util();
        if (DEBUG > 0) System.out.println("["+this.getClass().getName()+"] Directory translation: Iinitialized " + translations.length + " rules.");        
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "["+this.getClass().getName()+"] Directory translation: Iinitialized " + translations.length + " rules.");
        }          
    }    
    
    /**
     * Translate a resource name according to the expressions set in 
     * the opencms.properties. If no match is found, 
     * the resource name is returned unchanged.<p>
     * 
     * @param resourceName The resource name to translate
     * @return The translated name of the resource
     */
    public String translateResource(String resourceName) {        
        // Check all translations in the list
        if (DEBUG > 1) System.out.println("["+this.getClass().getName()+"] Directory Translation: Checking: " + resourceName);
        for(int i=0; i<m_translations.length; i++) {
            try {
                StringBuffer result = new StringBuffer();
                if(m_perlUtil.substitute(result, m_translations[i], resourceName) != 0) {
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "["+this.getClass().getName()+"] Directory translation: " + resourceName + " --> " + result);
                    }                    
                    if (DEBUG > 0) System.out.println(this.getClass().getName()+"] Directory translation: " + resourceName + " --> " + result);
                    return result.toString();
                }
            } catch(MalformedPerl5PatternException e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] Malformed directory translation rule:\""+m_translations[i]+"\"");
                }
            }
        }
        // No translation found, return original
        return resourceName;
    }
}
