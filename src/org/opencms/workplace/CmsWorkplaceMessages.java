/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceMessages.java,v $
 * Date   : $Date: 2005/04/29 16:05:53 $
 * Version: $Revision: 1.33 $
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

package org.opencms.workplace;

import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Provides access to the localized messages for the workplace.<p>
 * 
 * The workplace messages are collected from the workplace resource bundles of all installed modules,
 * plus the default workplace messages of the OpenCms core.
 * To be recognized as a workplace resource bundle,
 * the workplace property file must follow the naming convention <code>${module_package_name}.workplace${locale}.properties</code>,
 * for example like <code>com.mycompany.module.workplace_en.properties</code>.<p> 
 * 
 * Workplace messages are cached for faster lookup. If a localized key is contained in more then one module,
 * it will be used only from the module where it was first found in. The module order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a module.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.33 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceMessages extends CmsMultiMessages {

    /** The name of the property file. */
    public static final String DEFAULT_WORKPLACE_MESSAGE_BUNDLE = "org.opencms.workplace.workplace";

    /**
     * Constructor for creating a new messages object
     * initialized with the provided locale.<p>
     * 
     * @param locale the locale to initialize 
     */
    public CmsWorkplaceMessages(Locale locale) {

        super(collectModuleMessages(locale));
    }

    /**
     * Gathers all localization files for the workplace from the different modules.<p>
     * 
     * For a module named "my.module.name" the locale file must be named 
     * "my.module.name.workplace" and be located in the classpath so that the resource loader
     * can find it.<p>
     * 
     * @param locale the selected locale
     * 
     * @return an initialized set of module messages
     */
    private static synchronized List collectModuleMessages(Locale locale) {

//////////// collect the workplace.properties ////////////////
        
        // create a new list and add the base bundle
        ArrayList result = new ArrayList();
        // try to load the default resource bundle
        CmsMessages wpMsg = new CmsMessages(DEFAULT_WORKPLACE_MESSAGE_BUNDLE, locale);
        // bundle was loaded, add to list of bundles
        if (wpMsg.isInitialized()) {
            result.add(wpMsg);
        }

        Set names = OpenCms.getModuleManager().getModuleNames();
        if (names != null) {
            // iterate all module names
            Iterator i = names.iterator();
            while (i.hasNext()) {
                // this should result in a name like "my.module.name.workplace"
                String bundleName = ((String)i.next()) + ".workplace";
                // try to load a bundle with the module names
                CmsMessages msg = new CmsMessages(bundleName, locale);
                // bundle was loaded, add to list of bundles
                if (msg.isInitialized()) {
                    result.add(msg);
                }
            }
        }
        
        
////////////collect the messages.properties ////////////////
        
        List msgs = new ArrayList(names);
        if (names!=null) {
            msgs.addAll(names);
        }
        // add additional core workplace packages, like administration, tools, etc
        msgs.add("org.opencms.workplace.list");
        msgs.add("org.opencms.workplace.tools");
        msgs.add("org.opencms.workplace.administration");
        // iterate all module names
        Iterator i = msgs.iterator();
        while (i.hasNext()) {
            // this should result in a name like "org.opencms.workplace.xxx.messages"
            String bundleName = ((String)i.next()) + ".messages";
            // try to load a bundle with the module names
            CmsMessages msg = new CmsMessages(bundleName, locale);
            // bundle was loaded, add to list of bundles
            if (msg.isInitialized()) {
                result.add(msg);
            }
        }
        return result;
    }

}