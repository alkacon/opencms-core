/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsWorkplaceModuleMessages.java,v $
 * Date   : $Date: 2006/03/27 14:52:43 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Provides access to the localized messages for modules of the workplace.<p>
 * 
 * The workplace module messages are collected from the workplace resource bundles of all installed modules,
 * as well as all OpenCms core packages.<p>
 * 
 * To be recognized as a workplace module resource bundle,
 * the workplace property file must follow the naming convention <code>${module_package_name}.workplace${locale}.properties</code>,
 * or <code>${module_package_name}.messages${locale}.properties</code>
 * for example like <code>com.mycompany.module.workplace_en.properties</code> or 
 * <code>com.mycompany.module.messages_en.properties</code>.<p>
 * 
 * Workplace module messages are cached for faster lookup. If a localized key is contained in more then one module,
 * it will be used only from the module where it was first found in. The module order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a module.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsWorkplaceModuleMessages extends CmsMultiMessages {

    /** Constant for the <code>".messages"</code> prefix. */
    public static final String PREFIX_BUNDLE_MESSAGES = ".messages";

    /** Constant for the <code>".workplace"</code> prefix. */
    public static final String PREFIX_BUNDLE_WORKPLACE = ".workplace";

    /** Constant for the multi bundle name. */
    public static final String WORKPLACE_BUNDLE_NAME = CmsWorkplaceModuleMessages.class.getName();

    /**
     * Constructor for creating a new messages object
     * initialized with the provided locale.<p>
     * 
     * @param locale the locale to initialize 
     */
    public CmsWorkplaceModuleMessages(Locale locale) {

        super(locale);
        setBundleName(WORKPLACE_BUNDLE_NAME);
        addMessages(collectModuleMessages(locale));
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
    private static List collectModuleMessages(Locale locale) {

        // create a new list and add the base bundle
        ArrayList result = new ArrayList();

        //////////// iterate over all registered modules ////////////////        
        Set names = OpenCms.getModuleManager().getModuleNames();
        if (names != null) {
            // iterate all module names
            Iterator i = names.iterator();
            while (i.hasNext()) {
                String modName = (String)i.next();
                //////////// collect the workplace.properties ////////////////
                // this should result in a name like "my.module.name.workplace"
                String bundleName = modName + PREFIX_BUNDLE_WORKPLACE;
                // try to load a bundle with the module names
                CmsMessages msg = new CmsMessages(bundleName, locale);
                // bundle was loaded, add to list of bundles
                if (msg.isInitialized()) {
                    result.add(msg);
                }
                //////////// collect the messages.properties ////////////////
                // this should result in a name like "my.module.name.messages"
                bundleName = modName + PREFIX_BUNDLE_MESSAGES;
                // try to load a bundle with the module names
                msg = new CmsMessages(bundleName, locale);
                // bundle was loaded, add to list of bundles
                if (msg.isInitialized()) {
                    result.add(msg);
                }
            }
        }

        //////////// collect additional core packages ////////////////
        I_CmsMessageBundle[] coreMsgs = A_CmsMessageBundle.getOpenCmsMessageBundles();
        for (int i = 0; i < coreMsgs.length; i++) {
            I_CmsMessageBundle bundle = coreMsgs[i];
            result.add(bundle.getBundle(locale));
        }

        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsWorkplaceModuleMessages) {
            // workplace module messages are equal if the locale is equal (since all bundles are the same)
            CmsMessages other = (CmsMessages)obj;
            return other.getLocale().equals(getLocale());
        }
        return false;
    }
}