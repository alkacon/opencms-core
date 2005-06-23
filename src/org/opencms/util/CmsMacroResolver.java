/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsMacroResolver.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.14 $
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

package org.opencms.util;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Resolves macros in the form of <code>${key}</code> in an input String.<p>
 * 
 * The macro names that can be resolved depend of the context objects provided to the resolver
 * using the <code>set...</code> methods.<p>
 * 
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMacroResolver implements I_CmsMacroResolver {

    /** Identifier for "magic" parameter names. */
    public static final String C_KEY_OPENCMS = "opencms.";

    /** The prefix indicating that the key represents a page context object. */
    public static final String C_KEY_PAGE_CONTEXT = "pageContext.";

    /** The prefix indicating that the key represents a Cms property to be read on the current request URI. */
    public static final String C_KEY_PROPERTY = "property.";

    /** The prefix indicating that the key represents a Cms property to be read on the current element. */
    public static final String C_KEY_PROPERTY_ELEMENT = "elementProperty.";

    /** The prefix indicating that the key represents a Http request parameter. */
    public static final String C_KEY_REQUEST_PARAM = "param.";

    /** Identified for "magic" parameter commands. */
    public static final String[] C_VALUE_NAMES_ARRAY_OPENCMS = {"uri", "filename", "folder", "default.encoding"};

    /** The "magic" commands wrapped in a List. */
    public static final List C_VALUE_NAMES_OPENCMS = Collections.unmodifiableList(Arrays.asList(C_VALUE_NAMES_ARRAY_OPENCMS));

    /** Key used to specify the current time as macro value. */
    public static final String KEY_CURRENT_TIME = "currenttime";

    /** Key used to specify the city of the current user as macro value. */
    public static final String KEY_CURRENT_USER_CITY = "currentuser.city";

    /** Key used to specify the email address of the current user as macro value. */
    public static final String KEY_CURRENT_USER_EMAIL = "currentuser.email";

    /** Key used to specify the first name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_FIRSTNAME = "currentuser.firstname";

    /** Key used to specify the full name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_FULLNAME = "currentuser.fullname";

    /** Key used to specify the last name of the current user as macro value. */
    public static final String KEY_CURRENT_USER_LASTNAME = "currentuser.lastname";

    /** Key used to specify the username of the current user as macro value. */
    public static final String KEY_CURRENT_USER_NAME = "currentuser.name";

    /** Key used to specify the street of the current user as macro value. */
    public static final String KEY_CURRENT_USER_STREET = "currentuser.street";

    /** Key used to specify the zip code of the current user as macro value. */
    public static final String KEY_CURRENT_USER_ZIP = "currentuser.zip";

    /** Key prefix used to specify the value of a localized key as macro value. */
    public static final String KEY_LOCALIZED_PREFIX = "key.";

    /** Key used to specify the request encoding as macro value. */
    public static final String KEY_REQUEST_ENCODING = "request.encoding";

    /** Key used to specify the folder of the request uri as macro value. */
    public static final String KEY_REQUEST_FOLDER = "request.folder";

    /** Key user to specify the request locale as macro value. */
    public static final String KEY_REQUEST_LOCALE = "request.locale";

    /** Key used to specify the request uri as macro value. */
    public static final String KEY_REQUEST_URI = "request.uri";

    /** Key used to specify the validation path as macro value. */
    public static final String KEY_VALIDATION_PATH = "validation.path";

    /** Key used to specify the validation regex as macro value. */
    public static final String KEY_VALIDATION_REGEX = "validation.regex";

    /** Key used to specifiy the validation value as macro value. */
    public static final String KEY_VALIDATION_VALUE = "validation.value";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMacroResolver.class);

    /** A map of additional values provided by the calling class. */
    protected Map m_additionalMacros;

    /** The OpenCms user context to use for resolving macros. */
    protected CmsObject m_cms;

    /** The JSP's page context to use for resolving macros. */
    protected PageContext m_jspPageContext;

    /** Indicates if unresolved macros should be kept "as is" or replaced by an empty String. */
    protected boolean m_keepEmptyMacors;

    /** The messages resource bundle to resolve localized keys with. */
    protected CmsMessages m_messages;

    /** The resource name to use for resolving macros. */
    protected String m_resourceName;

    /**
     * Adds macro delelimiters to the given input, 
     * for example <code>key</code> becomes <code>${key}</code>.<p>
     * 
     * @param input the input to format as a macro
     * 
     * @return the input formatted as a macro
     */
    public static String formatMacro(String input) {

        StringBuffer result = new StringBuffer(32);
        result.append(I_CmsMacroResolver.MACRO_DELIMITER);
        result.append(I_CmsMacroResolver.MACRO_START);
        result.append(input);
        result.append(I_CmsMacroResolver.MACRO_END);
        return result.toString();
    }

    /**
     * Returns <code>true</code> if the given input String if formatted like a macro,
     * that is it starts with <code>{@link I_CmsMacroResolver#MACRO_DELIMITER} +
     * {@link I_CmsMacroResolver#MACRO_START}</code> and ends with 
     * <code>{@link I_CmsMacroResolver#MACRO_END}</code>.<p>
     * 
     * @param input the input to check for a macro
     * @return <code>true</code> if the given input String if formatted like a macro
     */
    public static boolean isMacro(String input) {

        if (CmsStringUtil.isEmpty(input) || (input.length() < 3)) {
            return false;
        }

        return ((input.charAt(0) == I_CmsMacroResolver.MACRO_DELIMITER)
            && (input.charAt(1) == I_CmsMacroResolver.MACRO_START) && (input.charAt(input.length() - 1) == I_CmsMacroResolver.MACRO_END));
    }

    /**
     * Factory method to create a new {@link CmsMacroResolver} instance.<p>
     * 
     * @return a new instance of a {@link CmsMacroResolver}
     */
    public static CmsMacroResolver newInstance() {

        return new CmsMacroResolver();
    }

    /**
     * Resolves the macros in the given input using the provided parameters.<p>
     * 
     * A macro in the form <code>${key}</code> in the content is replaced with it's assigned value
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
        resolver.m_keepEmptyMacors = true;
        return resolver.resolveMacros(input);
    }

    /**
     * Resolves macros in the provided input String using the given macro resolver.<p>
     * 
     * A macro in the form <code>${key}</code> in the content is replaced with it's assigned value
     * returned by the <code>{@link I_CmsMacroResolver#getMacroValue(String)}</code> method of the given 
     * <code>{@link I_CmsMacroResolver}</code> instance.<p>
     * 
     * If a macro is found that can not be mapped to a value by the given macro resolver,
     * <code>{@link I_CmsMacroResolver#isKeepEmptyMacros()}</code> controls if the macro is replaced by
     * an empty String, or is left untoched in the input.<p>
     * 
     * @param input the input in which to resolve the macros
     * @param resolver the macro resolver to use
     * 
     * @return the input with all macros resolved
     */
    public static String resolveMacros(final String input, I_CmsMacroResolver resolver) {

        int len;
        if ((input == null) || ((len = input.length()) < 3)) {
            // macro must have at last 3 chars "${}"
            return input;
        }

        int p = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER);
        if (p == -1) {
            // no macro delimiter found in input
            return input;
        }

        StringBuffer result = new StringBuffer(input.length() * 2);
        int np, pp1, pp2, e;
        String macro, value;
        boolean keep = resolver.isKeepEmptyMacros();
        boolean resolvedNone = true;

        // append chars before the first delimiter found
        result.append(input.substring(0, p));
        do {
            pp1 = p + 1;
            pp2 = pp1 + 1;
            if (pp2 >= len) {
                // remaining chars cant be a macro (minumum size is 3)
                result.append(input.substring(p, len));
                break;
            }
            // get the next macro delimiter
            np = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER, pp1);
            if (np == -1) {
                // none found, make sure remaining chars in this segement are appended
                np = len;
            }
            // check if the next char is a "macro start"
            if (input.charAt(pp1) == I_CmsMacroResolver.MACRO_START) {
                // we have a starting macro sequence "${", now check if this segment contains a "}"
                e = input.indexOf(I_CmsMacroResolver.MACRO_END, p);
                if ((e > 0) && (e < np)) {
                    // this segment contains a closing macro delimiter "}", so we have found a macro
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
                    // no complete macro "${...}" in this segment
                    e = p;
                }
            } else {
                // no macro start char after the "$"
                e = p;
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
     * Adds a customized macro to this macro resolver.<p>
     * 
     * @param key the macro to add
     * @param value the value to return if the macro is encountered
     */
    public void addMacro(String key, String value) {

        if (m_additionalMacros == null) {
            // use lazy initializing
            m_additionalMacros = new HashMap();
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

        if (m_jspPageContext != null) {

            if (macro.startsWith(CmsMacroResolver.C_KEY_REQUEST_PARAM)) {
                // the key is a request parameter  
                macro = macro.substring(CmsMacroResolver.C_KEY_REQUEST_PARAM.length());
                return m_jspPageContext.getRequest().getParameter(macro);
            }

            if (macro.startsWith(CmsMacroResolver.C_KEY_PAGE_CONTEXT)) {
                // the key is a page context object
                macro = macro.substring(CmsMacroResolver.C_KEY_PAGE_CONTEXT.length());
                int scope = m_jspPageContext.getAttributesScope(macro);
                return m_jspPageContext.getAttribute(macro, scope).toString();
            }

            if ((m_cms != null) && macro.startsWith(CmsMacroResolver.C_KEY_PROPERTY_ELEMENT)) {

                // the key is a cms property to be read on the current element

                macro = macro.substring(CmsMacroResolver.C_KEY_PROPERTY_ELEMENT.length());
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
                        LOG.warn(Messages.get().key(
                            Messages.LOG_PROPERTY_READING_FAILED_2,
                            macro,
                            controller.getCurrentRequest().getElementUri()), e);
                    }
                }
            }
        }

        if (m_cms != null) {

            if (macro.startsWith(CmsMacroResolver.C_KEY_PROPERTY)) {
                // the key is a cms property to be read on the current request URI
                macro = macro.substring(CmsMacroResolver.C_KEY_PROPERTY.length());
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

            }

            if (macro.startsWith(CmsMacroResolver.C_KEY_OPENCMS)) {

                // the key is a shortcut for a cms runtime value

                String originalKey = macro;
                macro = macro.substring(CmsMacroResolver.C_KEY_OPENCMS.length());
                int index = C_VALUE_NAMES_OPENCMS.indexOf(macro);
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
                    default:
                        // return the key "as is"
                        value = originalKey;
                        break;
                }

                return value;
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_NAME.equals(macro)) {
                // the key is the current users login name
                return m_cms.getRequestContext().currentUser().getName();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_FIRSTNAME.equals(macro)) {
                // the key is the current users first name
                return m_cms.getRequestContext().currentUser().getFirstname();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_LASTNAME.equals(macro)) {
                // the key is the current users last name
                return m_cms.getRequestContext().currentUser().getLastname();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_FULLNAME.equals(macro)) {
                // the key is the current users full name
                return m_cms.getRequestContext().currentUser().getFullName();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_EMAIL.equals(macro)) {
                // the key is the current users email address
                return m_cms.getRequestContext().currentUser().getEmail();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_STREET.equals(macro)) {
                // the key is the current users address
                return m_cms.getRequestContext().currentUser().getAddress();
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_ZIP.equals(macro)) {
                // the key is the current users zip code
                return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                    CmsUserSettings.ADDITIONAL_INFO_ZIPCODE);
            }

            if (CmsMacroResolver.KEY_CURRENT_USER_CITY.equals(macro)) {
                // the key is the current users city
                return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                    CmsUserSettings.ADDITIONAL_INFO_TOWN);
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

        }

        if (CmsMacroResolver.KEY_CURRENT_TIME.equals(macro)) {
            // the key is the current system time
            return String.valueOf(System.currentTimeMillis());
        }

        if (m_additionalMacros != null) {
            return (String)m_additionalMacros.get(macro);
        }

        return null;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return m_keepEmptyMacors;
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
    public CmsMacroResolver setAdditionalMacros(Map additionalMacros) {

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
     * @param keepEmptyMacros the replacemanet flag to use
     * 
     * @return this instance of the macro resolver
     * 
     * @see #isKeepEmptyMacros()
     */
    public CmsMacroResolver setKeepEmptyMacros(boolean keepEmptyMacros) {

        m_keepEmptyMacors = keepEmptyMacros;
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
}