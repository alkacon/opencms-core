/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsElementUtil.java,v $
 * Date   : $Date: 2009/09/25 08:45:22 $
 * Version: $Revision: 1.1.2.8 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
 * @version $Revision: 1.1.2.8 $
 * 
 * @since 7.6
 */
public final class CmsElementUtil {

    /** HTML id prefix constant. */
    private static final String ADE_ID_PREFIX = "ade_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementUtil.class);

    /** The cms context. */
    private CmsObject m_cms;

    /** The entity resolver. */
    private CmsXmlEntityResolver m_entityResolver;

    /** The http request. */
    private HttpServletRequest m_req;

    /** The http response. */
    private HttpServletResponse m_res;

    /** The actual container page uri. */
    private String m_uri;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param req the http request
     * @param res the http response
     * @param uri the container page uri
     */
    public CmsElementUtil(CmsObject cms, HttpServletRequest req, HttpServletResponse res, String uri) {

        m_cms = cms;
        m_req = req;
        m_res = res;
        m_uri = uri;
        m_entityResolver = new CmsXmlEntityResolver(m_cms);
    }

    /**
     * Creates a valid html id from an uuid.<p>
     * 
     * @param id the uuid
     * 
     * @return the generated html id
     */
    public static String createId(CmsUUID id) {

        return ADE_ID_PREFIX + id.toString();
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

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param resource the element resource
     * @param formatter the formatter uri
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    public String getElementContent(CmsResource resource, CmsResource formatter)
    throws CmsException, ServletException, IOException {

        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            formatter), resource, formatter);

        CmsResource loaderRes = loaderFacade.getLoaderStartResource();

        // get the current Flex controller
        CmsObject cms = CmsFlexController.getController(m_req).getCmsObject();
        String oldUri = cms.getRequestContext().getUri();
        try {
            cms.getRequestContext().setUri(m_uri);

            // to enable 'old' direct edit features for content-collector-elements, set the direct-edit-provider-attribute in the request
            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(cms, CmsDirectEditMode.TRUE, m_cms.getSitePath(resource));
            m_req.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);

            OpenCms.getLocaleManager();
            // TODO: is this going to be cached? most likely not! any alternative?
            // HACK: use the __element param for the element uri! 
            return new String(loaderFacade.getLoader().dump(
                m_cms,
                loaderRes,
                m_cms.getSitePath(resource),
                m_cms.getRequestContext().getLocale(),
                m_req,
                m_res), CmsLocaleManager.getResourceEncoding(cms, resource));
        } finally {
            cms.getRequestContext().setUri(oldUri);
        }
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
        resElement.put(CmsADEManager.P_OBJTYPE, CmsADEManager.ELEMENT_TYPE);
        resElement.put(CmsADEManager.P_ID, CmsElementUtil.ADE_ID_PREFIX + resource.getStructureId().toString());
        resElement.put(CmsADEManager.P_FILE, m_cms.getSitePath(resource));
        resElement.put(CmsADEManager.P_DATE, resource.getDateLastModified());
        resElement.put(CmsADEManager.P_USER, m_cms.readUser(resource.getUserLastModified()).getName());
        resElement.put(CmsADEManager.P_NAVTEXT, m_cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            false).getValue(""));
        resElement.put(CmsADEManager.P_TITLE, m_cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false).getValue(""));
        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        resElement.put(CmsADEManager.P_ALLOWEDIT, resUtil.getLock().isLockableBy(
            m_cms.getRequestContext().currentUser())
            && resUtil.isEditable());
        resElement.put(CmsADEManager.P_LOCKED, resUtil.getLockedByName());
        resElement.put(CmsADEManager.P_STATUS, "" + resUtil.getStateAbbreviation());
        // add formatted elements
        JSONObject resContents = new JSONObject();
        resElement.put(CmsADEManager.P_CONTENTS, resContents);
        // add formatter uris
        JSONObject formatters = new JSONObject();
        resElement.put(CmsADEManager.P_FORMATTERS, formatters);
        if (resource.getTypeId() == CmsResourceTypeContainerPage.getStaticTypeId()) {
            Iterator<String> itTypes = types.iterator();
            while (itTypes.hasNext()) {
                String type = itTypes.next();
                formatters.put(type, ""); // empty formatters
                resContents.put(type, ""); // empty contents
            }
            // this container page should contain exactly one container
            CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
                m_cms,
                resource,
                m_cms.getRequestContext().getLocale());
            CmsContainerBean container = cntPage.getContainers().values().iterator().next();

            // add subitems
            JSONArray subitems = new JSONArray();
            resElement.put(CmsADEManager.P_SUBITEMS, subitems);
            // iterate the elements
            for (CmsContainerElementBean element : container.getElements()) {
                CmsUUID id = element.getElement().getStructureId();
                // collect ids
                subitems.put(createId(id));
            }
        } else {
            // get the content definition
            List<CmsRelation> relations = m_cms.getRelationsForResource(
                resource,
                CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
            CmsXmlContentDefinition contentDef = null;
            if ((relations != null) && !relations.isEmpty()) {
                String xsd = m_cms.getSitePath(relations.get(0).getTarget(m_cms, CmsResourceFilter.ALL));
                contentDef = m_entityResolver.getCachedContentDefinition(xsd);
            }
            if (contentDef == null) {
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(resource));
                contentDef = content.getContentDefinition();
            }

            // iterate the formatters
            Iterator<Map.Entry<String, String>> it = contentDef.getContentHandler().getFormatters().entrySet().iterator();
            if (!it.hasNext()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_WARN_NO_FORMATTERS_DEFINED_1,
                    contentDef.getSchemaLocation()));
            }
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String type = entry.getKey();
                if (!types.contains(type) && !type.equals(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE)) {
                    // skip not supported types
                    continue;
                }
                String formatterUri = entry.getValue();
                formatters.put(type, formatterUri);
                // execute the formatter jsp for the given element
                try {
                    String jspResult = getElementContent(resource, m_cms.readResource(formatterUri));
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
}
