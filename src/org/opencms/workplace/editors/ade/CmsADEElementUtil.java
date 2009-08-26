/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEElementUtil.java,v $
 * Date   : $Date: 2009/08/26 12:59:05 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeContainerPage;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Maintains recent and favorite element lists.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 7.6
 */
public final class CmsADEElementUtil {

    /** HTML id prefix constant. */
    protected static final String ADE_ID_PREFIX = "ade_";

    /** Container page loader reference. */
    private static final CmsContainerPageLoader LOADER = (CmsContainerPageLoader)OpenCms.getResourceManager().getLoader(
        CmsContainerPageLoader.RESOURCE_LOADER_ID);

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEElementUtil.class);

    /** The cms context. */
    private CmsObject m_cms;

    /** The http request. */
    private HttpServletRequest m_req;

    /** The http response. */
    private HttpServletResponse m_res;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param req the http request
     * @param res the http response
     */
    public CmsADEElementUtil(CmsObject cms, HttpServletRequest req, HttpServletResponse res) {

        m_cms = cms;
        m_req = req;
        m_res = res;
    }

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param resource the element resource
     * @param formatterUri the formatter uri
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    public String getElementContent(CmsResource resource, String formatterUri)
    throws CmsException, ServletException, IOException {

        CmsResource resFormatter = m_cms.readResource(formatterUri);

        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            resFormatter), resource, resFormatter);

        CmsResource loaderRes = loaderFacade.getLoaderStartResource();
        // TODO: is this going to be cached? most likely not! any alternative?
        // HACK: use the __element param for the element uri
        return new String(loaderFacade.getLoader().dump(
            m_cms,
            loaderRes,
            m_cms.getSitePath(resource),
            null,
            m_req,
            m_res));
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param resource the resource
     * @param types the types supported by the container page
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(CmsResource resource, Collection<String> types) throws CmsException, JSONException {

        // create new json object for the element
        JSONObject resElement = new JSONObject();
        resElement.put(CmsADEServer.P_ID, CmsADEElementUtil.ADE_ID_PREFIX + resource.getStructureId().toString());
        resElement.put(CmsADEServer.P_FILE, m_cms.getSitePath(resource));
        resElement.put(CmsADEServer.P_DATE, resource.getDateLastModified());
        resElement.put(CmsADEServer.P_USER, m_cms.readUser(resource.getUserLastModified()).getName());
        resElement.put(CmsADEServer.P_NAVTEXT, m_cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            false).getValue(""));
        resElement.put(CmsADEServer.P_TITLE, m_cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false).getValue(""));
        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        resElement.put(
            CmsADEServer.P_ALLOWEDIT,
            resUtil.getLock().isLockableBy(m_cms.getRequestContext().currentUser()) && resUtil.isEditable());
        resElement.put(CmsADEServer.P_LOCKED, resUtil.getLockedByName());
        resElement.put(CmsADEServer.P_STATUS, "" + resUtil.getStateAbbreviation());
        // add formatted elements
        JSONObject resContents = new JSONObject();
        resElement.put(CmsADEServer.P_CONTENTS, resContents);
        // add formatter uris
        JSONObject formatters = new JSONObject();
        resElement.put(CmsADEServer.P_FORMATTERS, formatters);
        if (resource.getTypeId() == CmsResourceTypeContainerPage.getStaticTypeId()) {
            Iterator itTypes = types.iterator();
            while (itTypes.hasNext()) {
                String type = (String)itTypes.next();
                formatters.put(type, ""); // empty formatters
                resContents.put(type, ""); // empty contents
            }
            // this container page should contain exactly one container
            JSONObject localeData = LOADER.getCache(m_cms, resource, m_cms.getRequestContext().getLocale());
            JSONObject containers = localeData.optJSONObject(CmsContainerPageLoader.N_CONTAINER);
            JSONObject container = containers.getJSONObject(containers.names().getString(0));

            // add subitems
            JSONArray subitems = new JSONArray();
            resElement.put(CmsADEServer.P_SUBITEMS, subitems);
            // iterate the elements
            JSONArray elements = container.optJSONArray(CmsContainerPageLoader.N_ELEMENT);
            // get the actual number of elements to render
            int renderElems = elements.length();
            for (int i = 0; i < renderElems; i++) {
                JSONObject element = elements.optJSONObject(i);
                String id = element.optString(CmsContainerPageLoader.N_ID);
                // collect ids
                subitems.put(CmsADEElementUtil.ADE_ID_PREFIX + id.toString());
            }
        } else {
            // TODO: this may not be performing well, any way to access the content handler without unmarshal??
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(resource));
            Iterator it = content.getContentDefinition().getContentHandler().getFormatters().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String type = (String)entry.getKey();
                if (!types.contains(type) && !type.equals(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE)) {
                    // skip not supported types
                    continue;
                }
                String formatterUri = (String)entry.getValue();
                formatters.put(type, formatterUri);
                // execute the formatter jsp for the given element
                try {
                    String jspResult = getElementContent(resource, formatterUri);
                    // set the results
                    resContents.put(type, jspResult);
                } catch (Exception e) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_GENERATE_FORMATTED_ELEMENT_3,
                        m_cms.getSitePath(resource),
                        formatterUri,
                        type), e);
                }
            }
        }

        return resElement;
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param structureId the element structure id
     * @param types the types supported by the container page
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(CmsUUID structureId, Collection<String> types) throws CmsException, JSONException {

        return getElementData(m_cms.readResource(structureId), types);
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param elementUri the element uri
     * @param types the types supported by the container page
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(String elementUri, Collection<String> types) throws CmsException, JSONException {

        return getElementData(m_cms.readResource(elementUri), types);
    }

    /**
     * Parses an element id.<p>
     * 
     * @param id the element id
     * 
     * @return the corresponding structure id
     * 
     * @throws CmsIllegalArgumentException if the id has not the right format
     */
    public static CmsUUID parseId(String id) throws CmsIllegalArgumentException {

        if ((id == null) || (!id.startsWith(ADE_ID_PREFIX))) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
        try {
            return new CmsUUID(id.substring(ADE_ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
    }
}
