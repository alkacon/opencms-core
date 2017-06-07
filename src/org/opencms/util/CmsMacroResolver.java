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

package org.opencms.util;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Factory;
import org.apache.commons.logging.Log;

import com.google.common.base.Function;

/**
 * Resolves macros in the form of <code>%(key)</code> or <code>${key}</code> in an input String.<p>
 *
 * Starting with OpenCms 7.0, the preferred form of a macro is <code>%(key)</code>. This is to
 * avoid conflicts / confusion with the JSP EL, which also uses the <code>${key}</code> syntax.<p>
 *
 * The macro names that can be resolved depend of the context objects provided to the resolver
 * using the <code>set...</code> methods.<p>
 *
 * @since 6.0.0
 */
public class CmsMacroResolver implements I_CmsMacroResolver {

    /** The prefix indicating that the key represents an OpenCms runtime attribute. */
    public static final String KEY_ATTRIBUTE = "attribute.";

    /** Key used to specify the context path as macro value. */
    public static final String KEY_CONTEXT_PATH = "contextPath";

    /** Key used to specify the description of the current organizational unit as macro value. */
    public static final String KEY_CURRENT_ORGUNIT_DESCRIPTION = "currentou.description";

    /** Key used to specify the full qualified name of the current organizational unit as macro value. */
    public static final String KEY_CURRENT_ORGUNIT_FQN = "currentou.fqn";

    /** Key used to specify the current time as macro value. */
    public static final String KEY_CURRENT_TIME = "currenttime";

    /** Key used to specify the city of the current user as macro value. */
    public static final String KEY_CURRENT_USER_CITY = "currentuser.city";

    /** Key used to specify the country of the current user as macro value. */
    public static final String KEY_CURRENT_USER_COUNTRY = "currentuser.country";

    /** Key used to specify the display name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_DISPLAYNAME = "currentuser.displayname";

    /** Key used to specify the email address of the current user as macro value. */
    public static final String KEY_CURRENT_USER_EMAIL = "currentuser.email";

    /** Key used to specify the first name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_FIRSTNAME = "currentuser.firstname";

    /** Key used to specify the full name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_FULLNAME = "currentuser.fullname";

    /** Key used to specify the institution of the current user as macro value. */
    public static final String KEY_CURRENT_USER_INSTITUTION = "currentuser.institution";

    /** Key used to specify the last login date of the current user as macro value. */
    public static final String KEY_CURRENT_USER_LASTLOGIN = "currentuser.lastlogin";

    /** Key used to specify the last name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_LASTNAME = "currentuser.lastname";

    /** Key used to specify the user name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_NAME = "currentuser.name";

    /** Key used to specify the street of the current user as macro value. */
    public static final String KEY_CURRENT_USER_STREET = "currentuser.street";

    /** Key used to specify the zip code of the current user as macro value. */
    public static final String KEY_CURRENT_USER_ZIP = "currentuser.zip";

    /** Key prefix used to specify the value of a localized key as macro value. */
    public static final String KEY_LOCALIZED_PREFIX = "key.";

    /** Identifier for "magic" parameter names. */
    public static final String KEY_OPENCMS = "opencms.";

    /** The prefix indicating that the key represents a page context object. */
    public static final String KEY_PAGE_CONTEXT = "pageContext.";

    /** Key used to specify the project id as macro value. */
    public static final String KEY_PROJECT_ID = "projectid";

    /** The prefix indicating that the key represents a property to be read on the current request URI. */
    public static final String KEY_PROPERTY = "property.";

    /** The prefix indicating that the key represents a property to be read on the current element. */
    public static final String KEY_PROPERTY_ELEMENT = "elementProperty.";

    /** Key used to specify the request encoding as macro value. */
    public static final String KEY_REQUEST_ENCODING = "request.encoding";

    /** Key used to specify the folder of the request URI as macro value. */
    public static final String KEY_REQUEST_FOLDER = "request.folder";

    /** Key user to specify the request locale as macro value. */
    public static final String KEY_REQUEST_LOCALE = "request.locale";

    /** The prefix indicating that the key represents a HTTP request parameter. */
    public static final String KEY_REQUEST_PARAM = "param.";

    /** Key used to specify the request site root as macro value. */
    public static final String KEY_REQUEST_SITEROOT = "request.siteroot";

    /** Key used to specify the request uri as macro value. */
    public static final String KEY_REQUEST_URI = "request.uri";

    /** Key used to specify the validation path as macro value. */
    public static final String KEY_VALIDATION_PATH = "validation.path";

    /** Key used to specify the validation regex as macro value. */
    public static final String KEY_VALIDATION_REGEX = "validation.regex";

    /** Key used to specify the validation value as macro value. */
    public static final String KEY_VALIDATION_VALUE = "validation.value";

    /** Identified for "magic" parameter commands. */
    static final String[] VALUE_NAMES_ARRAY = {
        "uri", // 0
        "filename", // 1
        "folder", // 2
        "default.encoding", // 3
        "remoteaddress", // 4
        "webapp", // 5
        "webbasepath", // 6
        "version", // 7
        "versionid" // 8
    };

    /** The "magic" commands wrapped in a List. */
    public static final List<String> VALUE_NAMES = Collections.unmodifiableList(Arrays.asList(VALUE_NAMES_ARRAY));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMacroResolver.class);

    /** A map of additional values provided by the calling class. */
    protected Map<String, String> m_additionalMacros;

    /** The OpenCms user context to use for resolving macros. */
    protected CmsObject m_cms;

    /** The JSP's page context to use for resolving macros. */
    protected PageContext m_jspPageContext;

    /** Indicates if unresolved macros should be kept "as is" or replaced by an empty String. */
    protected boolean m_keepEmptyMacros;

    /** The messages resource bundle to resolve localized keys with. */
    protected CmsMessages m_messages;

    /** The resource name to use for resolving macros. */
    protected String m_resourceName;

    /** The request parameter map, used for better compatibility with multi part requests. */
    protected Map<String, String[]> m_parameterMap;

    /** A map from names of dynamic macros to the factories which generate their values. */
    private Map<String, Factory> m_factories;

    /**
     * Adds macro delimiters to the given input,
     * for example <code>key</code> becomes <code>%(key)</code>.<p>
     *
     * @param input the input to format as a macro
     *
     * @return the input formatted as a macro
     */
    public static String formatMacro(String input) {

        StringBuffer result = new StringBuffer(input.length() + 4);
        result.append(I_CmsMacroResolver.MACRO_DELIMITER);
        result.append(I_CmsMacroResolver.MACRO_START);
        result.append(input);
        result.append(I_CmsMacroResolver.MACRO_END);
        return result.toString();
    }

    /**
     * Returns <code>true</code> if the given input String if formatted like a macro,
     * that is it starts with <code>{@link I_CmsMacroResolver#MACRO_DELIMITER_OLD} +
     * {@link I_CmsMacroResolver#MACRO_START_OLD}</code> and ends with
     * <code>{@link I_CmsMacroResolver#MACRO_END_OLD}</code>.<p>
     *
     * @param input the input to check for a macro
     * @return <code>true</code> if the given input String if formatted like a macro
     */
    public static boolean isMacro(String input) {

        if (CmsStringUtil.isEmpty(input) || (input.length() < 3)) {
            return false;
        }

        return (((input.charAt(0) == I_CmsMacroResolver.MACRO_DELIMITER_OLD)
            && ((input.charAt(1) == I_CmsMacroResolver.MACRO_START_OLD)
                && (input.charAt(input.length() - 1) == I_CmsMacroResolver.MACRO_END_OLD)))
            || ((input.charAt(0) == I_CmsMacroResolver.MACRO_DELIMITER)
                && ((input.charAt(1) == I_CmsMacroResolver.MACRO_START)
                    && (input.charAt(input.length() - 1) == I_CmsMacroResolver.MACRO_END))));
    }

    /**
     * Returns <code>true</code> if the given input String is a macro equal to the given macro name.<p>
     *
     * @param input the input to check for a macro
     * @param macroName the macro name to check for
     *
     * @return <code>true</code> if the given input String is a macro equal to the given macro name
     */
    public static boolean isMacro(String input, String macroName) {

        if (isMacro(input)) {
            return input.substring(2, input.length() - 1).equals(macroName);
        }
        return false;
    }

    /**
     * Returns a macro for the given localization key with the given parameters.<p>
     *
     * @param keyName the name of the localized key
     * @param params the optional parameter array
     *
     * @return a macro for the given localization key with the given parameters
     */
    public static String localizedKeyMacro(String keyName, Object[] params) {

        String parameters = "";
        if ((params != null) && (params.length > 0)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    parameters += "|" + params[i].toString();
                }
            }
        }
        return ""
            + I_CmsMacroResolver.MACRO_DELIMITER
            + I_CmsMacroResolver.MACRO_START
            + CmsMacroResolver.KEY_LOCALIZED_PREFIX
            + keyName
            + parameters
            + I_CmsMacroResolver.MACRO_END;
    }

    /**
     * Factory method to create a new {@link CmsMacroResolver} instance.<p>
     *
     * @return a new instance of a {@link CmsMacroResolver}
     */
    public static CmsMacroResolver newInstance() {

        return new CmsMacroResolver();
    }

    /** Returns a new macro resolver that loads message keys from the workplace bundle in the user setting's language.
     * @param cms the CmsObject.
     * @return a new macro resolver with messages from the workplace bundle in the current users locale.
     */
    public static I_CmsMacroResolver newWorkplaceLocaleResolver(final CmsObject cms) {

        // Resolve macros in the property configuration
        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setCmsObject(cms);
        CmsUserSettings userSettings = new CmsUserSettings(cms.getRequestContext().getCurrentUser());
        CmsMultiMessages multimessages = new CmsMultiMessages(userSettings.getLocale());
        multimessages.addMessages(OpenCms.getWorkplaceManager().getMessages(userSettings.getLocale()));
        resolver.setMessages(multimessages);
        resolver.setKeepEmptyMacros(true);

        return resolver;
    }

    /**
     * Resolves the macros in the given input using the provided parameters.<p>
     *
     * A macro in the form <code>%(key)</code> or <code>${key}</code> in the content is replaced with it's assigned value
     * returned by the <code>{@link I_CmsMacroResolver#getMacroValue(String)}</code> method of the given
     * <code>{@link I_CmsMacroResolver}</code> instance.<p>
     *
     * If a macro is found that can not be mapped to a value by the given macro resolver,
     * it is left untouched in the input.<p>
     *
     * @param input the input in which to resolve the macros
     * @param cms the OpenCms user context to use when resolving macros
     * @param messages the message resource bundle to use when resolving macros
     *
     * @return the input with the macros resolved
     */
    public static String resolveMacros(String input, CmsObject cms, CmsMessages messages) {

        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.m_cms = cms;
        resolver.m_messages = messages;
        resolver.m_keepEmptyMacros = true;
        return resolver.resolveMacros(input);
    }

    /**
     * Resolves macros in the provided input String using the given macro resolver.<p>
     *
     * A macro in the form <code>%(key)</code> or <code>${key}</code> in the content is replaced with it's assigned value
     * returned by the <code>{@link I_CmsMacroResolver#getMacroValue(String)}</code> method of the given
     * <code>{@link I_CmsMacroResolver}</code> instance.<p>
     *
     * If a macro is found that can not be mapped to a value by the given macro resolver,
     * <code>{@link I_CmsMacroResolver#isKeepEmptyMacros()}</code> controls if the macro is replaced by
     * an empty String, or is left untouched in the input.<p>
     *
     * @param input the input in which to resolve the macros
     * @param resolver the macro resolver to use
     *
     * @return the input with all macros resolved
     */
    public static String resolveMacros(final String input, I_CmsMacroResolver resolver) {

        if ((input == null) || (input.length() < 3)) {
            // macro must have at last 3 chars "${}" or "%()"
            return input;
        }

        int pn = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER);
        int po = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER_OLD);

        if ((po == -1) && (pn == -1)) {
            // no macro delimiter found in input
            return input;
        }

        int len = input.length();
        StringBuffer result = new StringBuffer(len << 1);
        int np, pp1, pp2, e;
        String macro, value;
        boolean keep = resolver.isKeepEmptyMacros();
        boolean resolvedNone = true;
        char ds, de;
        int p;

        if ((po == -1) || ((pn > -1) && (pn < po))) {
            p = pn;
            ds = I_CmsMacroResolver.MACRO_START;
            de = I_CmsMacroResolver.MACRO_END;
        } else {
            p = po;
            ds = I_CmsMacroResolver.MACRO_START_OLD;
            de = I_CmsMacroResolver.MACRO_END_OLD;
        }

        // append chars before the first delimiter found
        result.append(input.substring(0, p));
        do {
            pp1 = p + 1;
            pp2 = pp1 + 1;
            if (pp2 >= len) {
                // remaining chars can't be a macro (minimum size is 3)
                result.append(input.substring(p, len));
                break;
            }
            // get the next macro delimiter
            if ((pn > -1) && (pn < pp1)) {
                pn = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER, pp1);
            }
            if ((po > -1) && (po < pp1)) {
                po = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER_OLD, pp1);
            }
            if ((po == -1) && (pn == -1)) {
                // none found, make sure remaining chars in this segment are appended
                np = len;
            } else {
                // check if the next delimiter is old or new style
                if ((po == -1) || ((pn > -1) && (pn < po))) {
                    np = pn;
                } else {
                    np = po;
                }
            }
            // check if the next char is a "macro start"
            char st = input.charAt(pp1);
            if (st == ds) {
                // we have a starting macro sequence "${" or "%(", now check if this segment contains a "}" or ")"
                e = input.indexOf(de, p);
                if ((e > 0) && (e < np)) {
                    // this segment contains a closing macro delimiter "}" or "]", so we may have found a macro
                    macro = input.substring(pp2, e);
                    // resolve macro
                    value = resolver.getMacroValue(macro);
                    e++;
                    if (value != null) {
                        // macro was successfully resolved
                        result.append(value);
                        resolvedNone = false;
                    } else if (keep) {
                        // macro was unknown, but should be kept
                        result.append(input.substring(p, e));
                    }
                } else {
                    // no complete macro "${...}" or "%(...)" in this segment
                    e = p;
                }
            } else {
                // no macro start char after the "$" or "%"
                e = p;
            }
            // set macro style for next delimiter found
            if (np == pn) {
                ds = I_CmsMacroResolver.MACRO_START;
                de = I_CmsMacroResolver.MACRO_END;
            } else {
                ds = I_CmsMacroResolver.MACRO_START_OLD;
                de = I_CmsMacroResolver.MACRO_END_OLD;
            }
            // append the remaining chars after the macro to the start of the next macro
            result.append(input.substring(e, np));
            // this is a nerdy joke ;-)
            p = np;
        } while (p < len);

        if (resolvedNone && keep) {
            // nothing was resolved and macros should be kept, return original input
            return input;
        }

        // input was changed during resolving of macros
        return result.toString();
    }

    /**
     * Strips the macro delimiters from the given input,
     * for example <code>%(key)</code> or <code>${key}</code> becomes <code>key</code>.<p>
     *
     * In case the input is not a macro, <code>null</code> is returned.<p>
     *
     * @param input the input to strip
     *
     * @return the macro stripped from the input, or <code>null</code>
     */
    public static String stripMacro(String input) {

        if (isMacro(input)) {
            return input.substring(2, input.length() - 1);
        }
        return null;
    }

    /**
     * Adds a macro whose value will be dynamically generated at macro resolution time.<p>
     *
     * The value will be generated for each occurence of the macro in a string.<p>
     *
     * @param name the name of the macro
     * @param factory the macro value generator
     */
    public void addDynamicMacro(String name, Factory factory) {

        if (m_factories == null) {
            m_factories = new HashMap<String, Factory>();
        }
        m_factories.put(name, factory);
    }

    /**
     * Adds a customized macro to this macro resolver.<p>
     *
     * @param key the macro to add
     * @param value the value to return if the macro is encountered
     */
    public void addMacro(String key, String value) {

        if (m_additionalMacros == null) {
            // use lazy initializing
            m_additionalMacros = new HashMap<String, String>();
        }
        m_additionalMacros.put(key, value);
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String macro) {

        if (m_messages != null) {
            if (macro.startsWith(CmsMacroResolver.KEY_LOCALIZED_PREFIX)) {
                String keyName = macro.substring(CmsMacroResolver.KEY_LOCALIZED_PREFIX.length());
                return m_messages.keyWithParams(keyName);
            }
        }

        if (m_factories != null) {
            Factory factory = m_factories.get(macro);
            if (factory != null) {
                String value = (String)factory.create();
                return value;
            }
        }

        if (m_jspPageContext != null) {

            if (m_jspPageContext.getRequest() != null) {
                if (macro.startsWith(CmsMacroResolver.KEY_REQUEST_PARAM)) {
                    // the key is a request parameter
                    macro = macro.substring(CmsMacroResolver.KEY_REQUEST_PARAM.length());
                    String result = null;
                    if (m_parameterMap != null) {
                        String[] param = m_parameterMap.get(macro);
                        if ((param != null) && (param.length >= 1)) {
                            result = param[0];
                        }
                    } else {
                        result = m_jspPageContext.getRequest().getParameter(macro);
                    }
                    if ((result == null) && macro.equals(KEY_PROJECT_ID)) {
                        result = m_cms.getRequestContext().getCurrentProject().getUuid().toString();
                    }
                    return result;
                }

                if ((m_cms != null) && macro.startsWith(CmsMacroResolver.KEY_PROPERTY_ELEMENT)) {

                    // the key is a cms property to be read on the current element

                    macro = macro.substring(CmsMacroResolver.KEY_PROPERTY_ELEMENT.length());
                    CmsFlexController controller = CmsFlexController.getController(m_jspPageContext.getRequest());
                    try {
                        CmsProperty property = m_cms.readPropertyObject(
                            controller.getCurrentRequest().getElementUri(),
                            macro,
                            false);
                        if (property != CmsProperty.getNullProperty()) {
                            return property.getValue();
                        }
                    } catch (CmsException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(
                                Messages.get().getBundle().key(
                                    Messages.LOG_PROPERTY_READING_FAILED_2,
                                    macro,
                                    controller.getCurrentRequest().getElementUri()),
                                e);
                        }
                    }
                }
            }

            if (macro.startsWith(CmsMacroResolver.KEY_PAGE_CONTEXT)) {
                // the key is a page context object
                macro = macro.substring(CmsMacroResolver.KEY_PAGE_CONTEXT.length());
                int scope = m_jspPageContext.getAttributesScope(macro);
                return m_jspPageContext.getAttribute(macro, scope).toString();
            }
        }

        if (m_cms != null) {

            if (macro.startsWith(CmsMacroResolver.KEY_PROPERTY)) {
                // the key is a cms property to be read on the current request URI
                macro = macro.substring(CmsMacroResolver.KEY_PROPERTY.length());
                try {
                    CmsProperty property = m_cms.readPropertyObject(m_cms.getRequestContext().getUri(), macro, true);
                    if (property != CmsProperty.getNullProperty()) {
                        return property.getValue();
                    }
                } catch (CmsException e) {
                    if (LOG.isWarnEnabled()) {
                        CmsMessageContainer message = Messages.get().container(
                            Messages.LOG_PROPERTY_READING_FAILED_2,
                            macro,
                            m_cms.getRequestContext().getUri());
                        LOG.warn(message.key(), e);
                    }
                }
                return null;
            }

            if (macro.startsWith(CmsMacroResolver.KEY_ATTRIBUTE)) {
                // the key is an OpenCms runtime attribute
                macro = macro.substring(CmsMacroResolver.KEY_ATTRIBUTE.length());
                Object attribute = m_cms.getRequestContext().getAttribute(macro);
                if (attribute != null) {
                    return attribute.toString();
                }
                return null;
            }

            if (macro.startsWith(CmsMacroResolver.KEY_OPENCMS)) {

                // the key is a shortcut for a cms runtime value

                String originalKey = macro;
                macro = macro.substring(CmsMacroResolver.KEY_OPENCMS.length());
                int index = VALUE_NAMES.indexOf(macro);
                String value = null;

                switch (index) {
                    case 0:
                        // "uri"
                        value = m_cms.getRequestContext().getUri();
                        break;
                    case 1:
                        // "filename"
                        value = m_resourceName;
                        break;
                    case 2:
                        // folder
                        value = m_cms.getRequestContext().getFolderUri();
                        break;
                    case 3:
                        // default.encoding
                        value = OpenCms.getSystemInfo().getDefaultEncoding();
                        break;
                    case 4:
                        // remoteaddress
                        value = m_cms.getRequestContext().getRemoteAddress();
                        break;
                    case 5:
                        // webapp
                        value = OpenCms.getSystemInfo().getWebApplicationName();
                        break;
                    case 6:
                        // webbasepath
                        value = OpenCms.getSystemInfo().getWebApplicationRfsPath();
                        break;
                    case 7:
                        // version
                        value = OpenCms.getSystemInfo().getVersionNumber();
                        break;
                    case 8:
                        // versionid
                        value = OpenCms.getSystemInfo().getVersionId();
                        break;
                    default:
                        // return the key "as is"
                        value = originalKey;
                        break;
                }

                return value;
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_NAME.equals(macro)) {
                // the key is the current users login name
                return m_cms.getRequestContext().getCurrentUser().getName();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_FIRSTNAME.equals(macro)) {
                // the key is the current users first name
                return m_cms.getRequestContext().getCurrentUser().getFirstname();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_LASTNAME.equals(macro)) {
                // the key is the current users last name
                return m_cms.getRequestContext().getCurrentUser().getLastname();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_DISPLAYNAME.equals(macro)) {
                // the key is the current users display name
                try {
                    if (m_messages != null) {
                        return m_cms.getRequestContext().getCurrentUser().getDisplayName(m_cms, m_messages.getLocale());
                    } else {
                        return m_cms.getRequestContext().getCurrentUser().getDisplayName(
                            m_cms,
                            m_cms.getRequestContext().getLocale());
                    }
                } catch (CmsException e) {
                    // ignore, macro can not be resolved
                }
            }

            if (CmsMacroResolver.KEY_CURRENT_ORGUNIT_FQN.equals(macro)) {
                // the key is the current organizational unit fully qualified name
                return m_cms.getRequestContext().getOuFqn();
            }

            if (CmsMacroResolver.KEY_CURRENT_ORGUNIT_DESCRIPTION.equals(macro)) {
                // the key is the current organizational unit description
                try {
                    CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                        m_cms,
                        m_cms.getRequestContext().getOuFqn());
                    if (m_messages != null) {
                        return ou.getDescription(m_messages.getLocale());
                    } else {
                        return ou.getDescription(m_cms.getRequestContext().getLocale());
                    }
                } catch (CmsException e) {
                    // ignore, macro can not be resolved
                }
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_FULLNAME.equals(macro)) {
                // the key is the current users full name
                return m_cms.getRequestContext().getCurrentUser().getFullName();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_EMAIL.equals(macro)) {
                // the key is the current users email address
                return m_cms.getRequestContext().getCurrentUser().getEmail();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_STREET.equals(macro)) {
                // the key is the current users address
                return m_cms.getRequestContext().getCurrentUser().getAddress();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_ZIP.equals(macro)) {
                // the key is the current users zip code
                return m_cms.getRequestContext().getCurrentUser().getZipcode();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_COUNTRY.equals(macro)) {
                // the key is the current users country
                return m_cms.getRequestContext().getCurrentUser().getCountry();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_CITY.equals(macro)) {
                // the key is the current users city
                return m_cms.getRequestContext().getCurrentUser().getCity();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_LASTLOGIN.equals(macro) && (m_messages != null)) {
                // the key is the current users last login timestamp
                return m_messages.getDateTime(m_cms.getRequestContext().getCurrentUser().getLastlogin());
            }

            if (CmsMacroResolver.KEY_REQUEST_SITEROOT.equals(macro)) {
                // the key is the currently requested site root
                return m_cms.getRequestContext().getSiteRoot();
            }

            if (CmsMacroResolver.KEY_REQUEST_URI.equals(macro)) {
                // the key is the currently requested uri
                return m_cms.getRequestContext().getUri();
            }

            if (CmsMacroResolver.KEY_REQUEST_FOLDER.equals(macro)) {
                // the key is the currently requested folder
                return CmsResource.getParentFolder(m_cms.getRequestContext().getUri());
            }

            if (CmsMacroResolver.KEY_REQUEST_ENCODING.equals(macro)) {
                // the key is the current encoding of the request
                return m_cms.getRequestContext().getEncoding();
            }

            if (CmsMacroResolver.KEY_REQUEST_LOCALE.equals(macro)) {
                // the key is the current locale of the request
                return m_cms.getRequestContext().getLocale().toString();
            }

            if (CmsMacroResolver.KEY_CONTEXT_PATH.equals(macro)) {
                // the key is the OpenCms context path
                return OpenCms.getSystemInfo().getContextPath();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_INSTITUTION.equals(macro)) {
                // the key is the current users institution
                return m_cms.getRequestContext().getCurrentUser().getInstitution();
            }

        }

        if (CmsMacroResolver.KEY_CURRENT_TIME.equals(macro)) {
            // the key is the current system time
            return String.valueOf(System.currentTimeMillis());
        } else if (macro.startsWith(CmsMacroResolver.KEY_CURRENT_TIME)) {
            // the key starts with the current system time
            macro = macro.substring(CmsMacroResolver.KEY_CURRENT_TIME.length()).trim();
            char operator = macro.charAt(0);
            macro = macro.substring(1).trim();
            long delta = 0;
            try {
                delta = Long.parseLong(macro);
            } catch (NumberFormatException e) {
                // ignore, there will be no delta
            }
            long resultTime = System.currentTimeMillis();
            switch (operator) {
                case '+':
                    // add delta to current time
                    resultTime += delta;
                    break;
                case '-':
                    // subtract delta from current time
                    resultTime -= delta;
                    break;
                default:
                    break;
            }
            return String.valueOf(resultTime);
        }

        if (m_additionalMacros != null) {
            return m_additionalMacros.get(macro);
        }

        return null;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return m_keepEmptyMacros;
    }

    /**
     * Resolves the macros in the given input.<p>
     *
     * Calls <code>{@link #resolveMacros(String)}</code> until no more macros can
     * be resolved in the input. This way "nested" macros in the input are resolved as well.<p>
     *
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        String result = input;

        if (input != null) {
            String lastResult;
            do {
                // save result for next comparison
                lastResult = result;
                // resolve the macros
                result = CmsMacroResolver.resolveMacros(result, this);
                // if nothing changes then the final result is found
            } while (!result.equals(lastResult));
        }

        // return the result
        return result;
    }

    /**
     * Provides a set of additional macros to this macro resolver.<p>
     *
     * Macros added with {@link #addMacro(String, String)} are added to the same set
     *
     * @param additionalMacros the additional macros to add
     *
     * @return this instance of the macro resolver
     */
    public CmsMacroResolver setAdditionalMacros(Map<String, String> additionalMacros) {

        m_additionalMacros = additionalMacros;
        return this;
    }

    /**
     * Provides an OpenCms user context to this macro resolver, required to resolve certain macros.<p>
     *
     * @param cms the OpenCms user context
     *
     * @return this instance of the macro resolver
     */
    public CmsMacroResolver setCmsObject(CmsObject cms) {

        m_cms = cms;
        return this;
    }

    /**
     * Provides a JSP page context to this macro resolver, required to resolve certain macros.<p>
     *
     * @param jspPageContext the JSP page context to use
     *
     * @return this instance of the macro resolver
     */
    public CmsMacroResolver setJspPageContext(PageContext jspPageContext) {

        m_jspPageContext = jspPageContext;
        return this;
    }

    /**
     * Controls of macros that can't be resolved are left unchanged in the input,
     * or are replaced with an empty String.<p>
     *
     * @param keepEmptyMacros the replacement flag to use
     *
     * @return this instance of the macro resolver
     *
     * @see #isKeepEmptyMacros()
     */
    public CmsMacroResolver setKeepEmptyMacros(boolean keepEmptyMacros) {

        m_keepEmptyMacros = keepEmptyMacros;
        return this;
    }

    /**
     * Provides a set of <code>{@link CmsMessages}</code> to this macro resolver,
     * required to resolve localized macros.<p>
     *
     * @param messages the message resource bundle to use
     *
     * @return this instance of the macro resolver
     */
    public CmsMacroResolver setMessages(CmsMessages messages) {

        m_messages = messages;
        return this;
    }

    /**
     * Sets the parameter map.<p>
     *
     * @param parameterMap the parameter map to set
     */
    public void setParameterMap(Map<String, String[]> parameterMap) {

        m_parameterMap = parameterMap;
    }

    /**
     * Provides a resource name to this macro resolver, required to resolve certain macros.<p>
     *
     * @param resourceName the resource name to use
     *
     * @return this instance of the macro resolver
     */
    public CmsMacroResolver setResourceName(String resourceName) {

        m_resourceName = resourceName;
        return this;
    }

    /**
     * Returns a function which applies the macro substitution of this resolver to its argument.<p>
     *
     * @return a function performing string substitution with this resolver
     */
    public Function<String, String> toFunction() {

        return new Function<String, String>() {

            public String apply(String input) {

                return resolveMacros(input);

            }
        };
    }
}
