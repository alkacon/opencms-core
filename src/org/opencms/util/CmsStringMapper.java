/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsStringMapper.java,v $
 * Date   : $Date: 2005/02/04 16:35:25 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.I_CmsJspTagContentContainer;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.PageContext;

/**
 * A string mapper to resolve EL like strings in tag attributes of Cms JSP tags.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $
 * @since 6.0 alpha 3
 */
public class CmsStringMapper implements I_CmsStringMapper {

    /** The current user's Cms object. */
    private CmsObject m_cms;

    /** The handler that uses the mapper. */
    private I_CmsXmlContentHandler m_handler;

    /** The selected locale. */
    private Locale m_locale;

    /** The JSP's page context. */
    private PageContext m_pageContext;

    /** The name of the Cms resource that a XML-content tag currently visits. */
    private String m_resourceName;

    /** The value to map Strings in. */
    private Map m_values;

    /**
     * Creates a new string mapper for Cms JSP tag attributes.<p>
     * 
     * @param cms the current user's Cms object
     * @param pageContext the JSP's page context
     */
    public CmsStringMapper(CmsObject cms, PageContext pageContext) {

        super();

        m_cms = cms;
        m_pageContext = pageContext;
        
        m_values = null;
        m_handler = null;
        m_locale = null;
    }

    /**
     * Creates a new string mapper for XML content values.<p>
     * 
     * @param values map of additional values
     * @param locale locale to use
     * @param cms a cms object
     * @param handler the XML content handler
     */
    public CmsStringMapper(I_CmsXmlContentHandler handler, Map values, Locale locale, CmsObject cms) {

        super();
        
        m_values = values;
        m_handler = handler;
        m_locale = locale;
        m_cms = cms;
        
        m_pageContext = null;
    }

    /**
     * @see org.opencms.util.I_CmsStringMapper#getValue(java.lang.String)
     */
    public String getValue(String key) {
        
        // TODO: remove following check for colon in key after it is assured that old macros are all replaced
        int colonIndex = key.indexOf(':');
        int dotIndex = key.indexOf('.');
        if (colonIndex != -1 && (colonIndex < dotIndex || dotIndex == -1)) {
            // log as error and replace first appearance of deprecated colon with dot
            if (OpenCms.getLog(this).isErrorEnabled()) {
                String error = "Usage of deprecated macro key: " + key;
                if (m_cms != null) {
                    error += " [" + m_cms.getRequestContext().getUri() + "]";
                }
                OpenCms.getLog(this).error(error);
            }
            key = key.replaceFirst("\\:", ".");
        }

        if (m_pageContext != null && key.startsWith(I_CmsStringMapper.C_KEY_REQUEST_PARAM)) {

            // the key is a request parameter  

            key = key.substring(I_CmsStringMapper.C_KEY_REQUEST_PARAM.length());
            return m_pageContext.getRequest().getParameter(key);

        }

        if (m_cms != null && key.startsWith(I_CmsStringMapper.C_KEY_PROPERTY)) {

            // the key is a cms property to be read on the current request URI

            key = key.substring(I_CmsStringMapper.C_KEY_PROPERTY.length());
            try {
                CmsProperty property = m_cms.readPropertyObject(m_cms.getRequestContext().getUri(), key, true);
                if (property != CmsProperty.getNullProperty()) {
                    return property.getValue();
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(
                        "Error reading property " + key + " of resource " + m_cms.getRequestContext().getUri(),
                        e);
                }
            }

        }

        if (m_cms != null && key.startsWith(I_CmsStringMapper.C_KEY_PROPERTY_ELEMENT)) {

            // the key is a cms property to be read on the current element

            key = key.substring(I_CmsStringMapper.C_KEY_PROPERTY_ELEMENT.length());
            CmsFlexController controller = (CmsFlexController)m_pageContext.getRequest().getAttribute(
                CmsFlexController.ATTRIBUTE_NAME);

            try {
                CmsProperty property = m_cms.readPropertyObject(
                    controller.getCurrentRequest().getElementUri(),
                    key,
                    false);
                if (property != CmsProperty.getNullProperty()) {
                    return property.getValue();
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error(
                        "Error reading property "
                            + key
                            + " of resource "
                            + controller.getCurrentRequest().getElementUri(),
                        e);
                }
            }

        }

        if (m_pageContext != null && key.startsWith(I_CmsStringMapper.C_KEY_PAGE_CONTEXT)) {

            // the key is a page context object

            key = key.substring(I_CmsStringMapper.C_KEY_PAGE_CONTEXT.length());
            int scope = m_pageContext.getAttributesScope(key);
            return m_pageContext.getAttribute(key, scope).toString();

        }

        if (m_resourceName != null && m_cms != null && key.startsWith(I_CmsStringMapper.C_KEY_OPENCMS)) {

            // the key is a shortcut for a cms runtime value

            String originalKey = key;
            key = key.substring(I_CmsStringMapper.C_KEY_OPENCMS.length());
            int index = I_CmsStringMapper.C_VALUE_NAMES_OPENCMS.indexOf(key);
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
                default:
                    // return the key "as is"
                    value = originalKey;
                    break;
            }

            return value;
        }

        if (I_CmsStringMapper.KEY_CURRENT_TIME.equals(key)) {
            // the key is the current system time
            return String.valueOf(System.currentTimeMillis());
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_NAME.equals(key) && m_cms != null) {
            // the key is the current users login name
            return m_cms.getRequestContext().currentUser().getName();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_FIRSTNAME.equals(key) && m_cms != null) {
            // the key is the current users first name
            return m_cms.getRequestContext().currentUser().getFirstname();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_LASTNAME.equals(key) && m_cms != null) {
            // the key is the current users last name
            return m_cms.getRequestContext().currentUser().getLastname();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_FULLNAME.equals(key) && m_cms != null) {
            // the key is the current users full name
            return m_cms.getRequestContext().currentUser().getFullName();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_EMAIL.equals(key) && m_cms != null) {
            // the key is the current users email address
            return m_cms.getRequestContext().currentUser().getEmail();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_STREET.equals(key) && m_cms != null) {
            // the key is the current users address
            return m_cms.getRequestContext().currentUser().getAddress();
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_ZIP.equals(key) && m_cms != null) {
            // the key is the current users zip code
            return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE);
        }

        if (I_CmsStringMapper.KEY_CURRENT_USER_CITY.equals(key) && m_cms != null) {
            // the key is the current users city
            return (String)m_cms.getRequestContext().currentUser().getAdditionalInfo(
                I_CmsConstants.C_ADDITIONAL_INFO_TOWN);
        }

        if (I_CmsStringMapper.KEY_REQUEST_URI.equals(key) && m_cms != null) {
            // the key is the currently requested uri
            return m_cms.getRequestContext().getUri();
        }

        if (I_CmsStringMapper.KEY_REQUEST_FOLDER.equals(key) && m_cms != null) {
            // the key is the currently requested folder
            return CmsResource.getParentFolder(m_cms.getRequestContext().getUri());
        }

        if (I_CmsStringMapper.KEY_REQUEST_ENCODING.equals(key) && m_cms != null) {
            // the key is the current encoding of the request
            return m_cms.getRequestContext().getEncoding();
        }

        if (I_CmsStringMapper.KEY_REQUEST_LOCALE.equals(key) && m_cms != null) {
            // the key is the current locale of the request
            return m_cms.getRequestContext().getLocale().toString();
        }

        if (key.startsWith(I_CmsStringMapper.KEY_LOCALIZED_PREFIX) && m_locale != null) {
            return key(key.substring(I_CmsStringMapper.KEY_LOCALIZED_PREFIX.length()), m_locale);
        }

        if (key.startsWith(I_CmsStringMapper.KEY_LOCALIZED_PREFIX) && m_locale == null) {
            // leave macros for localized keys unchanged if no locale available
            StringBuffer result = new StringBuffer(32);
            result.append(CmsStringUtil.C_MACRO_DELIMITER);
            result.append(CmsStringUtil.C_MACRO_START);
            result.append(key);
            result.append(CmsStringUtil.C_MACRO_END);
            return result.toString();
        }

        if (m_values != null) {
            return (String)m_values.get(key);
        }

        return null;
    }

    /**
     * Maps a specified EL like string to a result string.<p>
     * 
     * @param str an EL like string
     * @param contentContainer the XML-content tag that utilizes this mapper, or null in case of a non-XML-content tag
     * @param keepUnreplacedMacros if true, macros that couldn't be replaced are left unchanged in the content string, otherwise they are removed quietyl from the content string
     * 
     * @return the result string
     */
    public String map(String str, I_CmsJspTagContentContainer contentContainer, boolean keepUnreplacedMacros) {

        if (str == null) {
            return null;
        }

        if (contentContainer != null && contentContainer.getResourceName() != null) {
            m_resourceName = contentContainer.getResourceName();
        } else if (m_cms != null) {
            m_resourceName = m_cms.getRequestContext().getUri();
        } else {
            m_resourceName = null;
        }

        return CmsStringUtil.substituteMacros(str, this, keepUnreplacedMacros);
    }

    /**
     * Returns the current user's Cms object.<p>
     *
     * @return the current user's Cms object
     */
    protected CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the JSP's page context.<p>
     *
     * @return the JSP's page context
     */
    protected PageContext getPageContext() {

        return m_pageContext;
    }

    /**
     * Returns the resourcename of the Cms resource that a XML-content tag currently visits.<p>
     *
     * @return the resourcename of the Cms resource that a XML-content tag currently visits
     */
    protected String getResourceName() {

        return m_resourceName;
    }

    /**
     * Returns the localized resource string for a given message key according to the configured resource bundle
     * of this mapper.<p>
     * 
     * If the key was not found in the configuredd bundle, or no bundle is configured for this 
     * content handler, the return value is
     * <code>"??? " + keyName + " ???"</code>.<p>
     * 
     * @param keyName the key for the desired string 
     * @param locale the locale to get the key from
     * 
     * @return the resource string for the given key 
     */
    protected String key(String keyName, Locale locale) {

        CmsMessages messages = m_handler.getMessages(locale);
        if (messages != null) {
            return messages.key(keyName);
        }
        return CmsMessages.formatUnknownKey(keyName);
    }

}