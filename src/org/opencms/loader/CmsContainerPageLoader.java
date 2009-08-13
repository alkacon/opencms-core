/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsContainerPageLoader.java,v $
 * Date   : $Date: 2009/08/13 10:47:26 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.ade.CmsContainerPageCache;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * OpenCms loader for resources of type <code>{@link org.opencms.file.types.CmsResourceTypeContainerPage}</code>.<p>
 *
 * It is just a xml-content loader with special object caching.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6
 */
public class CmsContainerPageLoader extends A_CmsXmlDocumentLoader {

    /** Xml content node constant name. */
    public static final String N_CONTAINER = "Containers";

    /** Xml content node constant element. */
    public static final String N_ELEMENT = "Elements";

    /** Xml content node constant formatter. */
    public static final String N_FORMATTER = "Formatter";

    /** JSON cache element name constant. */
    public static final String N_LOCALE = "Locale";

    /** JSON cache element name constant. */
    public static final String N_MAXELEMENTS = "MaxElements";

    /** Xml content node constant name. */
    public static final String N_NAME = "Name";

    /** Xml content node constant type. */
    public static final String N_TYPE = "Type";

    /** Xml content node constant uri. */
    public static final String N_URI = "Uri";

    /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID = 11;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerPageLoader.class);

    /** The cache instance. */
    private CmsContainerPageCache m_cache;

    /**
     * Default constructor.<p>
     */
    public CmsContainerPageLoader() {

        m_cache = new CmsContainerPageCache();
    }

    /**
     * Returns the cached JSON object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to look for
     * @param locale optional locale, if <code>null</code> the whole bean will be returned
     *  
     * @return the cached JSON object
     */
    public JSONObject getCache(CmsObject cms, CmsResource resource, Locale locale) {

        // get the cached content
        JSONObject containerPageBean = m_cache.get(cms, resource);
        if (containerPageBean == null) {
            // container page not yet in cache
            try {
                // try to load it
                unmarshalXmlDocument(cms, resource, null);
            } catch (CmsException e) {
                // something really bad happened
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CONTAINER_PAGE_NOT_FOUND_1,
                    cms.getSitePath(resource)), e);
                return null;
            }
            containerPageBean = m_cache.get(cms, resource);
            if (containerPageBean == null) {
                // container page is still not in cache, should never happen
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CONTAINER_PAGE_NOT_FOUND_1,
                    cms.getSitePath(resource)));
                return null;
            }
        }
        // if no locale return the whole thing
        if (locale == null) {
            return containerPageBean;
        }
        // get the locale data
        if (!containerPageBean.has(locale.toString())) {
            LOG.warn(Messages.get().container(
                Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                cms.getSitePath(resource),
                locale.toString()).key());
            locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(cms, resource).get(0);
            if (!containerPageBean.has(locale.toString())) {
                // locale not found!!
                LOG.error(Messages.get().container(
                    Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                    cms.getSitePath(resource),
                    locale).key());
                return null;
            }
        }
        return containerPageBean.optJSONObject(locale.toString()).optJSONObject(CmsContainerPageLoader.N_CONTAINER);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return RESOURCE_LOADER_ID;
    }

    /**
     * Returns a String describing this resource loader, which is (localized to the system default locale)
     * <code>"The OpenCms default resource loader for container page"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {

        return Messages.get().getBundle().key(Messages.GUI_LOADER_CONTAINERPAGE_DEFAULT_DESC_0);
    }

    /**
     * Creates a new json object from the xml content.<p>
     * 
     * @param cms the cms context
     * @param content the xml content
     * 
     * @return the json object for the given content 
     * 
     * @throws JSONException should not happen
     */
    public JSONObject jsonify(CmsObject cms, CmsXmlContent content) throws JSONException {

        JSONObject result = new JSONObject();
        // iterate over every locale
        Iterator itLocales = content.getLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = (Locale)itLocales.next();
            JSONObject containerList = new JSONObject();
            // iterate over every container in the given locale
            Iterator itContainers = content.getValues(N_CONTAINER, locale).iterator();
            while (itContainers.hasNext()) {
                I_CmsXmlContentValue container = (I_CmsXmlContentValue)itContainers.next();
                String containerPath = container.getPath();
                // get the name and type
                String name = content.getValue(CmsXmlUtils.concatXpath(containerPath, N_NAME), locale).getStringValue(
                    cms);
                String type = content.getValue(CmsXmlUtils.concatXpath(containerPath, N_TYPE), locale).getStringValue(
                    cms);
                JSONObject containerBean = new JSONObject();
                containerBean.put(N_NAME, name);
                containerBean.put(N_TYPE, type);
                containerBean.put(N_MAXELEMENTS, -1); // will be updated later while executing the template
                // iterate over the container elements
                Iterator itElements = content.getValues(CmsXmlUtils.concatXpath(containerPath, N_ELEMENT), locale).iterator();
                while (itElements.hasNext()) {
                    I_CmsXmlContentValue element = (I_CmsXmlContentValue)itElements.next();
                    String elementPath = element.getPath();
                    // get uri and formatter
                    String uri = content.getValue(CmsXmlUtils.concatXpath(elementPath, N_URI), locale).getStringValue(
                        cms);
                    // TODO: subcontainers 
                    String formatter = content.getValue(CmsXmlUtils.concatXpath(elementPath, N_FORMATTER), locale).getStringValue(
                        cms);
                    JSONObject elemBean = new JSONObject();
                    elemBean.put(N_URI, uri);
                    elemBean.put(N_FORMATTER, formatter);
                    // add element to container
                    containerBean.append(N_ELEMENT, elemBean);
                }
                // add container to locale
                containerList.put(name, containerBean);
            }
            JSONObject localeData = new JSONObject();
            localeData.put(N_LOCALE, locale.toString());
            localeData.put(N_CONTAINER, containerList);
            // add locale data
            result.put(locale.toString(), localeData);
        }
        return result;
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#getTemplatePropertyDefinition()
     */
    protected String getTemplatePropertyDefinition() {

        return CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS;
    }

    /**
     * Sets the cached JSON object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to set the cache for
     * @param object the JSON object to cache
     */
    protected void setCache(CmsObject cms, CmsResource resource, JSONObject object) {

        m_cache.set(cms, resource, object);
    }

    /**
     * @see org.opencms.loader.A_CmsXmlDocumentLoader#unmarshalXmlDocument(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest)
     */
    protected I_CmsXmlDocument unmarshalXmlDocument(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsException {

        CmsXmlContent content;
        if (req != null) {
            content = CmsXmlContentFactory.unmarshal(cms, resource, req);
        } else {
            // internal calls, to just fill the cache
            content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
        }
        if (m_cache.get(cms, resource) == null) {
            try {
                setCache(cms, resource, jsonify(cms, content));
            } catch (JSONException e) {
                throw new CmsException(Messages.get().container(Messages.ERR_JSON_EXCEPTION_1, e.getLocalizedMessage()));
            }
        }
        return content;
    }
}
