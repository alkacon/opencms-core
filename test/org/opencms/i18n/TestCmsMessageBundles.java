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

package org.opencms.i18n;

import org.opencms.gwt.I_CmsClientMessageBundle;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Tests for the CmsMessageBundles.<p>
 *
 * @since 6.0.0
 */
public abstract class TestCmsMessageBundles extends OpenCmsTestCase {

    /** Prefix for the error messages in the bundles. */
    private static final String KEY_PREFIX_ERR = "ERR_";

    /** Prefix for the gui messages in the bundles. */
    private static final String KEY_PREFIX_GUI = "GUI_";

    /** Prefix for the initialization messages in the bundles. */
    private static final String KEY_PREFIX_INIT = "INIT_";

    /** Prefix for the log messages in the bundles. */
    private static final String KEY_PREFIX_LOG = "LOG_";

    /** Prefix for the report messages in the bundles. */
    private static final String KEY_PREFIX_RPT = "RPT_";

    /** The source folder to copy the resource bundles from. */
    private static final String SOURCE_FOLDER_INFIX = "/resources/system/workplace/locales/";

    /** The source folder to copy the resource bundles from. */
    private static final String SOURCE_FOLDER_PREFIX = "modules/org.opencms.locale.";

    /** The source folder to copy the resource bundles from. */
    private static final String SOURCE_FOLDER_SUFFIX = "/messages/";

    /** Cache the resource bundle to exclude from additional locales tests. */
    private Map<Locale, List<I_CmsMessageBundle>> m_excludedBundles = new HashMap<Locale, List<I_CmsMessageBundle>>();

    /**
     * Checks all message bundles for the DE locale.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLocale_DE_MessagesBundles() throws Exception {

        messagesBundleConstantTest(Locale.GERMAN);
    }

    /**
     * Checks all message bundles for the EN locale.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLocale_EN_MessagesBundles() throws Exception {

        messagesBundleConstantTest(Locale.ENGLISH);
    }

    /**
     * Performs some key and language independent tests.<p>
     *
     * @param className the bundle implementing class name
     * @param bundleName the bundle name to test
     * @param locale the locale to test
     * @param client if the bundle to test is a client bundle
     *
     * @return a description of all errors found
     */
    protected String doPreTestBundle(String className, String bundleName, Locale locale, boolean client) {

        String expectedClass = client ? ".ClientMessages" : ".Messages";
        int indexOfMessages = className.lastIndexOf(expectedClass);
        if (indexOfMessages < 0) {
            return "Bundle '" + className + "' is not a 'Messages' class.\n";
        }
        if (!className.toLowerCase().equals(bundleName.toLowerCase())) {
            return "Bundle '" + bundleName + "' has not the form: packagename" + expectedClass.toLowerCase() + ".\n";
        }
        indexOfMessages = bundleName.lastIndexOf(expectedClass.toLowerCase());
        if (indexOfMessages < 0) {
            return "The Message bundle '"
                + bundleName
                + "' does not ends with: '\"+expectedClass.toLowerCase()+\"'. \n "
                + "Change the constant literal ('private static final String BUNDLE_NAME')\n";
        }
        String fileName = getMessageBundleSourceName(bundleName, locale);
        if (!Locale.ENGLISH.equals(locale)) {
            if (!(new File(fileName)).canRead()) {
                return "Bundle '" + className + "' has no input for locale '" + locale + "'.\n";
            }
        }
        // in case of no errors, return the empty String
        return "";
    }

    /**
     * Tests an individual message bundle.<p>
     *
     * @param clazz the  bundle class
     * @param bundleName the bundle class name
     * @param locale the locale to test
     *
     * @return a description of all errors found
     *
     * @throws Exception if the test fails
     */
    protected String doTestBundle(Class<?> clazz, String bundleName, Locale locale) throws Exception {

        List<String> keys = new ArrayList<String>();

        System.out.println("\nValidating all keys in bundle " + bundleName + " for locale " + locale + ":");

        CmsMessages messages = getMessageBundle(bundleName, locale);

        List<String> errorMessages = new ArrayList<String>();
        // use reflection on all member constants
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.getType().equals(String.class) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // check all String fields
            String key = field.getName();

            String value;
            try {
                value = (String)field.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }
            // ensure the name id identical to the value
            if (!key.equals(value)) {
                errorMessages.add("Key '" + key + "' has bad value '" + value + "'.");
            }
            // check if key exists in bundle for constant
            String message = messages.key(key);
            boolean isPresent = !CmsMessages.isUnknownKey(message);
            boolean testKeyValue = false;
            if (Locale.ENGLISH.equals(locale)) {
                testKeyValue = true;
            } else {
                boolean isAdditionalKey = !key.toUpperCase().equals(key);
                testKeyValue = (isAdditionalKey
                    || key.startsWith(KEY_PREFIX_ERR)
                    || key.startsWith(KEY_PREFIX_GUI)
                    || key.startsWith(KEY_PREFIX_RPT));
            }
            if (testKeyValue && !isPresent) {
                errorMessages.add("No message for '" + key + "' in bundle.");
            }

            // ensure key has the form
            // "{ERR|LOG|INIT|GUI|RPT}_KEYNAME_{0-9}";
            if (key.length() < 7) {
                errorMessages.add("Key '" + key + "' is too short (length must be at least 7).");
            }
            if (!key.equals(key.toUpperCase())) {
                errorMessages.add("Key '" + key + "' must be all upper case.");
            }
            if ((key.charAt(key.length() - 2) != '_')
                || (!key.startsWith(KEY_PREFIX_ERR)
                    && !key.startsWith(KEY_PREFIX_GUI)
                    && !key.startsWith(KEY_PREFIX_INIT)
                    && !key.startsWith(KEY_PREFIX_LOG)
                    && !key.startsWith(KEY_PREFIX_RPT))) {
                errorMessages.add("Key '" + key + "' must have the form {ERR|LOG|INIT|GUI|RPT}_KEYNAME_{0-9}.");
                continue;
            }
            int argCount = 0;
            try {
                argCount = Integer.valueOf(key.substring(key.length() - 1)).intValue();
            } catch (Throwable e) {
                errorMessages.add("Key '" + key + "' must end in the number of parameters.");
                continue;
            }
            if (testKeyValue && isPresent) {
                for (int j = 0; j < argCount; j++) {
                    String arg = "{" + j;
                    int pos = message.indexOf(arg);
                    if (pos < 0) {
                        errorMessages.add(
                            "Message '" + message + "' for key '" + key + "' misses argument {" + j + "}.");
                    }
                }
                for (int j = argCount; j < 10; j++) {
                    String arg = "{" + j;
                    int pos = message.indexOf(arg);
                    if (pos >= 0) {
                        errorMessages.add(
                            "Message '" + message + "' for key '" + key + "' contains unused argument {" + j + "}.");
                    }
                }
            }
            // store this key for later check against all properties in the bundle
            keys.add(key);
        }

        Enumeration<String> bundleKeys = messages.getResourceBundle().getKeys();
        while (bundleKeys.hasMoreElements()) {
            String bundleKey = bundleKeys.nextElement();
            if (bundleKey.toUpperCase().equals(bundleKey)) {
                // only check keys which are all upper case
                if (!keys.contains(bundleKey)) {
                    errorMessages.add("Bundle contains unreferenced message '" + bundleKey + "'.");
                }
            } else {
                System.out.println("Additional key '" + bundleKey + "' in bundle.");
            }
        }

        Locale defLocale = Locale.ENGLISH;
        // for locales other than ENGLISH
        if (!defLocale.equals(locale)) {
            // compare the additional key names and params with the ENGLISH ones
            ResourceBundle resBundle = messages.getResourceBundle();
            ResourceBundle enResBundle = getMessageBundle(bundleName, defLocale).getResourceBundle();
            bundleKeys = enResBundle.getKeys();
            while (bundleKeys.hasMoreElements()) {
                String bundleKey = bundleKeys.nextElement();
                if (!bundleKey.toUpperCase().equals(bundleKey)) {
                    // additional key found
                    String keyValue;
                    try {
                        // try to retrieve the key for the given locale
                        keyValue = resBundle.getString(bundleKey);
                    } catch (MissingResourceException e) {
                        errorMessages.add("Additional key '" + bundleKey + "' missing.");
                        continue;
                    }
                    String enKeyValue = enResBundle.getString(bundleKey);
                    boolean[] args = new boolean[10];
                    for (int j = 0; j < args.length; j++) {
                        String arg = "{" + j;
                        int pos = enKeyValue.indexOf(arg);
                        args[j] = pos >= 0;
                    }
                    for (int j = 0; j < args.length; j++) {
                        String arg = "{" + j;
                        int pos = keyValue.indexOf(arg);
                        if ((pos < 0) && (args[j])) {
                            // not in locale bundle but in master bundle
                            errorMessages.add(
                                "Additional message '"
                                    + keyValue
                                    + "' for key '"
                                    + bundleKey
                                    + "' misses argument {"
                                    + j
                                    + "} from master bundle.");
                        } else if ((pos >= 0) && (!args[j])) {
                            // in locale bundle but not in master bundle
                            errorMessages.add(
                                "Additional message '"
                                    + keyValue
                                    + "' for key '"
                                    + bundleKey
                                    + "' contains argument {"
                                    + j
                                    + "} not used in master bundle.");
                        }
                    }
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            String msg = "Errors for bundle '" + bundleName + "' and Locale '" + locale + "':";
            Iterator<String> it = errorMessages.iterator();
            while (it.hasNext()) {
                msg += "\n     - ";
                msg += it.next();
            }
            return msg + "\n";
        }
        // in case there was no error, return an empty String
        return "";
    }

    /**
     * Returns the resource bundles to be excluded from additional locales tests.<p>
     *
     * @param locale the locale to get the excluded bundles for
     *
     * @return the resource bundles to be excluded from additional locales tests
     */
    protected List<I_CmsMessageBundle> getExcludedLocalizedBundles(Locale locale) {

        if (Locale.ENGLISH.equals(locale)) {
            return new ArrayList<I_CmsMessageBundle>();
        }
        if (m_excludedBundles.get(locale) == null) {
            List<I_CmsMessageBundle> excludedBundles;
            List<I_CmsMessageBundle> notLocalized = getNotLocalizedBundles(locale);
            if (notLocalized == null) {
                excludedBundles = new ArrayList<I_CmsMessageBundle>();
            } else {
                excludedBundles = new ArrayList<I_CmsMessageBundle>(notLocalized);
            }
            for (int i = 0; i < getTestMessageBundles().length; i++) {
                I_CmsMessageBundle bundle = getTestMessageBundles()[i];
                if (excludedBundles.contains(bundle)) {
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
                        if (key.startsWith(KEY_PREFIX_ERR)
                            || key.startsWith(KEY_PREFIX_GUI)
                            || key.startsWith(KEY_PREFIX_RPT)) {
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
                Enumeration<String> bundleKeys = messages.getResourceBundle().getKeys();
                while (bundleKeys.hasMoreElements()) {
                    String bundleKey = bundleKeys.nextElement();
                    if (!bundleKey.toUpperCase().equals(bundleKey)) {
                        exclude = false;
                        break;
                    }
                }
                if (exclude) {
                    excludedBundles.add(bundle);
                }
            }
            m_excludedBundles.put(locale, excludedBundles);
        }
        return m_excludedBundles.get(locale);
    }

    /**
     * Prepares the test for the given bundle and locale and
     * returns a message bundle that DOES NOT include the default keys.<p>
     *
     * @param bundleName the resource bundle to prepare
     * @param locale the locale to prepare the resource bundle for
     *
     * @return a message bundle that DOES NOT include the default keys
     *
     * @throws IOException if something goes wrong
     */
    protected CmsMessages getMessageBundle(String bundleName, Locale locale) throws IOException {

        if (Locale.ENGLISH.equals(locale)) {
            return new CmsMessages(bundleName, locale);
        }
        String source = getMessageBundleSourceName(bundleName, locale);
        String fileName = CmsStringUtil.substitute(bundleName, ".", "/") + "_" + locale.toString() + ".properties";
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        String target = OpenCmsTestProperties.getInstance().getTestBuildFolder() + "/" + fileName;
        CmsFileUtil.copy(source, target);
        return new CmsMessages(bundleName + "_" + locale.toString(), locale);
    }

    /**
     * Returns the file name of the source message bundle.<p>
     *
     * @param bundleName the resource bundle to get the file name for
     * @param locale the locale to get the file name for
     *
     * @return the file name of the source message bundle
     */
    protected String getMessageBundleSourceName(String bundleName, Locale locale) {

        if (Locale.ENGLISH.equals(locale)) {
            return bundleName;
        }
        String fileName = CmsStringUtil.substitute(bundleName, ".", "/") + "_" + locale.toString() + ".properties";
        String source = SOURCE_FOLDER_PREFIX
            + locale.toString()
            + SOURCE_FOLDER_INFIX
            + locale.toString()
            + SOURCE_FOLDER_SUFFIX
            + fileName;

        // if file from the localized folder is not readable take the file from the original module
        if (!new File(source).canRead()) {
            source = getModuleMessagesBundleSourceName(bundleName, locale);
        }
        return source;
    }

    /**
     * Returns the file name of the source message bundle from the module.<p>
     *
     * @param bundleName the resource bundle to get the file name for
     * @param locale the locale to get the file name for
     *
     * @return the file name of the source message bundle of the module
     */
    protected String getModuleMessagesBundleSourceName(String bundleName, Locale locale) {

        if (Locale.ENGLISH.equals(locale)) {
            return bundleName;
        }
        // substitute the last occurring "." of the bundle name with "/" to build the correct filename
        String packageName = bundleName.substring(0, bundleName.lastIndexOf('.'));
        packageName += "/";
        String source = "modules/"
            + packageName
            + "resources/system/modules/"
            + packageName
            + "classes/"
            + CmsStringUtil.substitute(bundleName, ".", "/")
            + "_"
            + locale.toString()
            + ".properties";
        return source;
    }

    /**
     * Returns a list of bundles not to be localized.<p>
     *
     * @param locale the locale to get the not localized bundles for
     *
     * @return a list of bundles not to be localized
     */
    protected abstract List<I_CmsMessageBundle> getNotLocalizedBundles(Locale locale);

    /**
     * Template method that has to be overwritten to return the client class that will be tested.<p>
     *
     * @return the classes to test
     *
     * @throws Exception if the test fails
     */
    protected abstract List<I_CmsClientMessageBundle> getTestClientMessageBundles() throws Exception;

    /**
     * Template method that has to be overwritten to return the <code>I_CmsMessageBundle</code>
     * instances that will be tested.<p>
     *
     * @return the <code>I_CmsMessageBundle</code> instances to test: these will be the
     *         singleton instances of the <code>Messages</code> classes residing in every localized package.
     */
    protected abstract I_CmsMessageBundle[] getTestMessageBundles();

    /**
     * Checks all OpenCms internal message bundles if the are correctly build.<p>
     *
     * @param locale the locale to test
     *
     * @throws Exception if the test fails
     */
    protected void messagesBundleConstantTest(Locale locale) throws Exception {

        // the default locale MUST be ENGLISH (this call will also set the default locale to ENGLISH if required)
        // assertEquals(CmsLocaleManager.getDefaultLocale(), Locale.ENGLISH);

        StringBuffer errors = new StringBuffer();
        I_CmsMessageBundle[] bundles = getTestMessageBundles();
        for (int i = 0; i < bundles.length; i++) {
            I_CmsMessageBundle bundle = bundles[i];
            String tmp = bundle.getBundleName();
            tmp = doPreTestBundle(bundle.getClass().getName(), bundle.getBundleName(), locale, false);
            errors.append(tmp);
            if (CmsStringUtil.isEmpty(tmp)) {
                if (!getExcludedLocalizedBundles(locale).contains(bundle)) {
                    errors.append(doTestBundle(bundle.getClass(), bundle.getBundleName(), locale));
                }
            }
        }
        List<I_CmsClientMessageBundle> clientBundles = getTestClientMessageBundles();
        for (I_CmsClientMessageBundle clientBundle : clientBundles) {
            String tmp = clientBundle.getBundleName();
            tmp = doPreTestBundle(clientBundle.getClass().getName(), clientBundle.getBundleName(), locale, true);
            errors.append(tmp);
            if (CmsStringUtil.isEmpty(tmp)) {
                errors.append(doTestBundle(clientBundle.getClientImpl(), clientBundle.getBundleName(), locale));
            }
        }
        if (errors.length() > 0) {
            fail("\n" + errors);
        }
    }
}
