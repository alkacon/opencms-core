/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceMessages.java,v $
 * Date   : $Date: 2011/03/23 14:52:41 $
 * Version: $Revision: 1.48 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * Provides access to the localized messages for the OpenCms workplace.<p>
 * 
 * The workplace messages are collected from the workplace resource bundles of all installed modules,
 * plus all the OpenCms core packages.<p>
 * 
 * To be recognized as a workplace module resource bundle,
 * the workplace property file must follow the naming convention <code>${module_package_name}.workplace${locale}.properties</code>,
 * or <code>${module_package_name}.messages${locale}.properties</code>
 * for example like <code>com.mycompany.module.workplace_en.properties</code> or 
 * <code>com.mycompany.module.messages_en.properties</code>.<p>
 * 
 * Workplace messages are cached for faster lookup. If a localized key is contained in more then one module,
 * it will be used only from the module where it was first found in. The module order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a module.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.48 $ 
 * 
 * @since 6.0.0 
 */
public class CmsWorkplaceMessages extends CmsMultiMessages {

    /** The title key prefix used for the "new resource" dialog. */
    public static final String GUI_NEW_RESOURCE_TITLE_PREFIX = "title.new";

    /** The prefix to generate the resource type names with. */
    public static final String GUI_RESOURCE_TYPE_PREFIX = "fileicon.";

    /** Constant for the <code>".messages"</code> prefix. */
    public static final String PREFIX_BUNDLE_MESSAGES = ".messages";

    /** Constant for the <code>".workplace"</code> prefix. */
    public static final String PREFIX_BUNDLE_WORKPLACE = ".workplace";

    /** Constant for the multi bundle name. */
    public static final String WORKPLACE_BUNDLE_NAME = CmsWorkplaceMessages.class.getName();

    /**
     * Constructor for creating a new messages object
     * initialized with the provided locale.<p>
     * 
     * @param locale the locale to initialize 
     */
    public CmsWorkplaceMessages(Locale locale) {

        super(locale);
        setBundleName(WORKPLACE_BUNDLE_NAME);
        addMessages(collectModuleMessages(locale));
    }

    /**
     * Returns the title for the "new resource" dialog.<p>
     * 
     * It will look up a key with the prefix {@link #GUI_NEW_RESOURCE_TITLE_PREFIX} 
     * and the given name appended (converted to lower case).<p>
     * 
     * If this key is not found, the value of 
     * {@link org.opencms.workplace.explorer.Messages#GUI_TITLE_NEWFILEOTHER_0} will be returned.<p>
     * 
     * @param wp an instance of a {@link CmsWorkplace} to resolve the key name with
     * @param name the type to generate the title for
     * 
     * @return the title for the "new resource" dialog
     */
    public static String getNewResourceTitle(CmsWorkplace wp, String name) {

        // try to find the localized key
        String title = wp.key(GUI_NEW_RESOURCE_TITLE_PREFIX + name.toLowerCase());
        if (CmsMessages.isUnknownKey(title)) {
            // still unknown - use default title
            title = wp.key(org.opencms.workplace.explorer.Messages.GUI_TITLE_NEWFILEOTHER_0);
        }
        return title;
    }

    /**
     * Returns the nice name of the given resource type name.<p>
     * 
     * It will look up a key with the prefix {@link #GUI_RESOURCE_TYPE_PREFIX} 
     * and the given name appended.<p>
     * 
     * If this key is not found, the value of 
     * the name input will be returned.<p>
     * 
     * @param wp an instance of a {@link CmsWorkplace} to resolve the key name with
     * @param name the resource type name to generate the nice name for
     * 
     * @return the nice name of the given resource type name
     */
    public static String getResourceName(CmsWorkplace wp, String name) {

        // try to find the localized key
        return wp.keyDefault(GUI_RESOURCE_TYPE_PREFIX + name, name);
    }

    /**
     * Gathers all localization files for the workplace from the different modules.<p>
     * 
     * For a module named "my.module.name" the locale file must be named 
     * "my.module.name.workplace" or "my.module.name.messages" and 
     * be located in the classpath so that the resource loader can find it.<p>
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
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsWorkplaceMessages) {
            // workplace messages are equal if the locale is equal (since all bundles are the same)
            CmsMessages other = (CmsMessages)obj;
            return other.getLocale().equals(getLocale());
        }
        return false;
    }

    /**
     * @see org.opencms.i18n.CmsMessages#hashCode()
     */
    @Override
    public int hashCode() {

        return getLocale().hashCode();
    }
}