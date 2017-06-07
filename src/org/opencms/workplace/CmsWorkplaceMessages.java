/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

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
 * @since 6.0.0
 */
public class CmsWorkplaceMessages extends CmsMultiMessages {

    /** The title key prefix used for the "new resource" dialog. */
    public static final String GUI_NEW_RESOURCE_TITLE_PREFIX = "title.new";

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
     * Returns the description of the given resource type name.<p>
     *
     * If this key is not found, the value of the name input will be returned.<p>
     *
     * @param wp an instance of a {@link CmsWorkplace} to resolve the key name with
     * @param name the resource type name to generate the nice name for
     *
     * @return the description of the given resource type name
     */
    public static String getResourceTypeDescription(CmsWorkplace wp, String name) {

        // try to find the localized key
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(name).getInfo();
        return wp.keyDefault(key, name);
    }

    /**
     * Returns the description of the given resource type name.<p>
     *
     * If this key is not found, the value of the name input will be returned.<p>
     *
     * @param locale the right locale to use
     * @param name the resource type name to generate the nice name for
     *
     * @return the description of the given resource type name
     */
    public static String getResourceTypeDescription(Locale locale, String name) {

        // try to find the localized key
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(name).getInfo();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(key)) {
            return "";
        }
        return OpenCms.getWorkplaceManager().getMessages(locale).keyDefault(key, name);
    }

    /**
     * Returns the localized name of the given resource type name.<p>
     *
     * If this key is not found, the value of the name input will be returned.<p>
     *
     * @param wp an instance of a {@link CmsWorkplace} to resolve the key name with
     * @param name the resource type name to generate the nice name for
     *
     * @return the localized name of the given resource type name
     */
    public static String getResourceTypeName(CmsWorkplace wp, String name) {

        // try to find the localized key
        CmsExplorerTypeSettings typeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(name);
        if (typeSettings == null) {
            return name;
        }
        String key = typeSettings.getKey();
        return wp.keyDefault(key, name);
    }

    /**
     * Returns the localized name of the given resource type name.<p>
     *
     * If this key is not found, the value of the name input will be returned.<p>
     *
     * @param locale the right locale to use
     * @param name the resource type name to generate the nice name for
     *
     * @return the localized name of the given resource type name
     */
    public static String getResourceTypeName(Locale locale, String name) {

        // try to find the localized key
        String key = OpenCms.getWorkplaceManager().getExplorerTypeSetting(name).getKey();
        return OpenCms.getWorkplaceManager().getMessages(locale).keyDefault(key, name);
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
    private static List<CmsMessages> collectModuleMessages(Locale locale) {

        // create a new list and add the base bundle
        ArrayList<CmsMessages> result = new ArrayList<CmsMessages>();

        //////////// iterate over all registered modules ////////////////
        Set<String> names = OpenCms.getModuleManager().getModuleNames();
        if (names != null) {
            // iterate all module names
            Iterator<String> i = names.iterator();
            while (i.hasNext()) {
                String modName = i.next();
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

        /////////// collect bundles configured in module configurations ////////
        Set<String> bundleNames = OpenCms.getADEManager().getConfiguredWorkplaceBundles();
        for (String bundleName : bundleNames) {
            CmsMessages msg = new CmsMessages(bundleName, locale);
            if (msg.isInitialized()) {
                result.add(msg);
            }
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