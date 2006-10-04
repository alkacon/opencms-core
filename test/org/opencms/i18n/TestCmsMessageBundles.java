/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/TestCmsMessageBundles.java,v $
 * Date   : $Date: 2006/10/04 07:47:48 $
 * Version: $Revision: 1.13.4.2 $
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

package org.opencms.i18n;

import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestCase;

/**
 * Tests for the CmsMessageBundles.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13.4.2 $
 * 
 * @since 6.0.0
 */
public abstract class TestCmsMessageBundles extends TestCase {

    /** The source folder to copy the resource bundles from. */
    private static final String SOURCE_FOLDER = "modules/org.opencms.locale.de/resources/system/workplace/locales/";

    /** The taget folder to copy the resource bundles to. */
    private static final String TARGET_FOLDER = "bin/";

    /** cache the resource bundle to exclude from additional locales tests. */
    private List m_excludedBundles;

    /**
     * Tests if message will be returned in the correct locale.<p>
     * 
     * @throws Exception if the test fails
     */
    public final void testLocale() throws Exception {

        CmsMessages messages = new CmsMessages("org.opencms.i18n.messages", Locale.GERMANY);
        String value = messages.key("LOG_LOCALE_MANAGER_FLUSH_CACHE_1", new Object[] {"TestEvent"});
        assertEquals("Locale manager leerte die Caches nachdem Event TestEvent empfangen wurde.", value);
    }

    /**
     * Checks all OpenCms internal message bundles if the are correctly build.<p>
     * 
     * @throws Exception if the test fails
     */
    public final void testMessagesBundleConstants() throws Exception {

        I_CmsMessageBundle[] bundles = getTestMessageBundles();
        for (int i = 0; i < bundles.length; i++) {
            doPreTestBundle(bundles[i]);
            doTestBundle(bundles[i], Locale.ENGLISH);
            if (!getExludedLocalisedBundles().contains(bundles[i])) {
                doTestBundle(bundles[i], Locale.GERMAN);
            }
        }
    }

    /**
     * Prepares the test for the given bundle and locale and
     * returns a message bundle that DOES NOT include the default keys.<p>
     * 
     * @param bundle the resource bundle to prepare
     * @param locale the locale to prepare the resource bundle for
     * 
     * @throws Exception if something goes wrong
     */
    protected CmsMessages getMessageBundle(I_CmsMessageBundle bundle, Locale locale) throws IOException {

        if (locale == Locale.ENGLISH) {
            return bundle.getBundle(locale);
        }
        String fileName = CmsStringUtil.substitute(bundle.getBundleName(), ".", "/")
            + "_"
            + locale.getLanguage()
            + ".properties";
        String source = SOURCE_FOLDER + locale.getLanguage() + "/messages/" + fileName;
        String target = TARGET_FOLDER + fileName;
        CmsFileUtil.copy(source, target);
        return new CmsMessages(bundle.getBundleName() + "_" + locale.getLanguage(), locale);
    }

    /**
     * Returns a list of bundles not to be localized.<p>
     * 
     * @return a list of bundles not to be localized
     */
    protected abstract List getNotLocalisedBundles();

    /**
     * Template method that has to be overwritten to return the <code>I_CmsMessageBundle</code> 
     * instances that will be tested.<p> 
     * 
     * @return the <code>I_CmsMessageBundle</code> instances to test: these will be the 
     *         singleton instances of the <code>Messages</code> classes residing in every localized package. 
     */
    protected abstract I_CmsMessageBundle[] getTestMessageBundles();

    /**
     * Performs some key and language independent tests.<p>
     * 
     * @param bundle the bundle to test
     */
    private void doPreTestBundle(I_CmsMessageBundle bundle) {

        String className = bundle.getClass().getName();
        if (!className.endsWith(".Messages")) {
            fail("bundle '" + className + "' is not a 'Messages' class.");
        }
        if (!className.toLowerCase().equals(bundle.getBundleName())) {
            fail("bundle name '" + bundle.getBundleName() + "' has not the form: packagename.messages");
        }
        if (!bundle.getBundleName().endsWith(".messages")) {
            fail("The Message bundle name '"
                + bundle.getBundleName()
                + "' does not ends with: '.messages'. \n "
                + "Change the constant literal ('private static final String BUNDLE_NAME')");
        }
    }

    /**
     * Tests an individual message bundle.<p>
     * 
     * @param bundle the bundle to test
     * @param locale the locale to test
     * 
     * @throws Exception if the test fails
     */
    private void doTestBundle(I_CmsMessageBundle bundle, Locale locale) throws Exception {

        List keys = new ArrayList();

        System.out.println("\nValidating all keys in bundle " + bundle.getBundleName() + " for locale " + locale + ":");

        CmsMessages messages = getMessageBundle(bundle, locale);

        List errorMessages = new ArrayList();
        // use reflection on all member constants
        Field[] fields = bundle.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getType().equals(String.class)) {
                // check all String fields
                String key = field.getName();

                String value;
                try {
                    value = (String)field.get(bundle);
                } catch (IllegalAccessException e) {
                    continue;
                }

                System.out.println("Validating key '" + key + "': '" + messages.key(key) + "'");

                // ensure the name id identical to the value
                if (!key.equals(value)) {
                    errorMessages.add("Key '" + key + "' has bad value '" + value + "'");
                }
                // check if key exists in bundle for constant
                String message = messages.key(key);
                boolean isPresent = !CmsMessages.isUnknownKey(message);
                boolean testKeyValue = false;
                if (Locale.ENGLISH == locale) {
                    testKeyValue = true;
                } else {
                    boolean isAdditionalKey = !key.toUpperCase().equals(key);
                    testKeyValue = (isAdditionalKey || key.startsWith("GUI_") || key.startsWith("RPT_"));
                }
                if (testKeyValue && !isPresent) {
                    errorMessages.add("No message for '" + key + "' in bundle");
                }

                // ensure key has the form
                // "{ERR|LOG|INIT|GUI|RPT}_KEYNAME_{0-9}";
                if (key.length() < 7) {
                    errorMessages.add("Key '" + key + "' is too short (length must be at least 7)");
                }
                if (!key.equals(key.toUpperCase())) {
                    errorMessages.add("Key '" + key + "' must be all upper case");
                }
                if ((key.charAt(key.length() - 2) != '_')
                    || (!key.startsWith("ERR_")
                        && !key.startsWith("LOG_")
                        && !key.startsWith("INIT_")
                        && !key.startsWith("GUI_") && !key.startsWith("RPT_"))) {
                    errorMessages.add("Key '" + key + "' must have the form {ERR|LOG|INIT|GUI|RPT}_KEYNAME_{0-9}");
                }
                int argCount = Integer.valueOf(key.substring(key.length() - 1)).intValue();

                if (testKeyValue && isPresent) {
                    for (int j = 0; j < argCount; j++) {
                        String arg = "{" + j;
                        int pos = message.indexOf(arg);
                        if (pos < 0) {
                            errorMessages.add("Message '"
                                + message
                                + "' for key '"
                                + key
                                + " misses argument {"
                                + j
                                + "}");
                        }
                    }
                    for (int j = argCount; j < 10; j++) {
                        String arg = "{" + j;
                        int pos = message.indexOf(arg);
                        if (pos >= 0) {
                            errorMessages.add("Message '"
                                + message
                                + "' for key '"
                                + key
                                + " contains unused argument {"
                                + j
                                + "}");
                        }
                    }
                }
                // store this key for later check against all properties in the
                // bundle
                keys.add(key);
            }
        }

        Enumeration bundleKeys = messages.getResourceBundle().getKeys();
        while (bundleKeys.hasMoreElements()) {
            String bundleKey = (String)bundleKeys.nextElement();
            if (bundleKey.toUpperCase().equals(bundleKey)) {
                // only check keys which are all upper case
                if (!keys.contains(bundleKey)) {
                    errorMessages.add("Bundle contains unreferenced message " + bundleKey);
                }
            } else {
                System.out.println("Additional key '" + bundleKey + "' in bundle");
            }
        }

        // for locales other than ENGLISH
        if (locale != Locale.ENGLISH) {
            // compare the additional key names and params with the ENGLISH ones
            ResourceBundle resBundle = messages.getResourceBundle();
            ResourceBundle enResBundle = bundle.getBundle().getResourceBundle();
            bundleKeys = enResBundle.getKeys();
            while (bundleKeys.hasMoreElements()) {
                String bundleKey = (String)bundleKeys.nextElement();
                if (!bundleKey.toUpperCase().equals(bundleKey)) {
                    // additional key found
                    String keyValue;
                    try {
                        // try to retrieve the key for the given locale
                        keyValue = resBundle.getString(bundleKey);
                    } catch (MissingResourceException e) {
                        errorMessages.add("Additional key '" + bundleKey + "' missing");
                        continue;
                    }
                    String enKeyValue = enResBundle.getString(bundleKey);
                    int argCount = 0;
                    for (int j = 0; j < 9; j++) {
                        String arg = "{" + j;
                        int pos = enKeyValue.indexOf(arg);
                        if (pos < 0) {
                            argCount = j;
                            break;
                        }
                    }
                    for (int j = 0; j < argCount; j++) {
                        String arg = "{" + j;
                        int pos = keyValue.indexOf(arg);
                        if (pos < 0) {
                            errorMessages.add("Message '"
                                + keyValue
                                + "' for key '"
                                + bundleKey
                                + " misses argument {"
                                + j
                                + "}");
                        }
                    }
                    for (int j = argCount; j < 10; j++) {
                        String arg = "{" + j;
                        int pos = keyValue.indexOf(arg);
                        if (pos >= 0) {
                            errorMessages.add("Message '"
                                + keyValue
                                + "' for key '"
                                + bundleKey
                                + " contains unused argument {"
                                + j
                                + "}");
                        }
                    }
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            String msg = "Errors for bundle name '" + bundle.getBundleName() + "' and Locale '" + locale + "'";
            Iterator it = errorMessages.iterator();
            while (it.hasNext()) {
                msg += "\n";
                msg += it.next();
            }
            fail(msg);
        }
    }

    /**
     * Returns the resource bundles to be excluded from additional locales tests.<p>
     * 
     * @return the resource bundles to be excluded from additional locales tests
     */
    private List getExludedLocalisedBundles() {

        if (m_excludedBundles == null) {
            List notLocalized = getNotLocalisedBundles();
            if (notLocalized == null) {
                m_excludedBundles = new ArrayList();
            } else {
                m_excludedBundles = new ArrayList(getNotLocalisedBundles());
            }
            for (int i = 0; i < getTestMessageBundles().length; i++) {
                I_CmsMessageBundle bundle = getTestMessageBundles()[i];
                if (m_excludedBundles.contains(bundle)) {
                    continue;
                }
                boolean exclude = true;
                // test if the bundle contains keys prefixed by GUI_ or RPT_
                Field[] fields = bundle.getClass().getDeclaredFields();
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    if (field.getType().equals(String.class)) {
                        // check all String fields
                        String key = field.getName();
                        if (key.startsWith("GUI_") || key.startsWith("RPT_")) {
                            exclude = false;
                            break;
                        }
                    }
                }
                if (!exclude) {
                    continue;
                }
                // test if the bundle contains additional keys
                CmsMessages messages = new CmsMessages(bundle.getBundleName(), Locale.ENGLISH);
                Enumeration bundleKeys = messages.getResourceBundle().getKeys();
                while (bundleKeys.hasMoreElements()) {
                    String bundleKey = (String)bundleKeys.nextElement();
                    if (!bundleKey.toUpperCase().equals(bundleKey)) {
                        exclude = false;
                        break;
                    }
                }
                if (exclude) {
                    m_excludedBundles.add(bundle);
                }
            }
        }
        return m_excludedBundles;
    }
}
