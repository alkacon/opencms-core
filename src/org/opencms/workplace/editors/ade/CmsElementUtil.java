/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsElementUtil.java,v $
 * Date   : $Date: 2009/12/21 09:05:50 $
 * Version: $Revision: 1.8 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContentProperty;

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
 * @version $Revision: 1.8 $
 * 
 * @since 7.6
 */
public final class CmsElementUtil {

    /** Element json property constants. */
    public enum JsonElement {

        /** If the current user is not allowed to edit the given resource, this is the reason of it. */
        NO_EDIT_REASON("noEditReason"),
        /** Array of HTML code resulting from formatter execution. */
        CONTENTS("contents"),
        /** The last modification date. */
        DATE("date"),
        /** The description. */
        DESCRIPTION("description"),
        /** The URI. */
        FILE("file"),
        /** Array of formatter URIs. */
        FORMATTERS("formatters"),
        /** Element's structure id. */
        ID("id"),
        /** Element's navigation text. */
        NAVTEXT("navText"),
        /** The object type, container or element. */
        OBJTYPE("objtype"),
        /** The element property information. */
        PROPERTIES("properties"),
        /** Element's status. */
        STATUS("status"),
        /** Element's subelements in case of a subcontainer, as list of client IDs. */
        SUBITEMS("subItems"),
        /** Element's title. */
        TITLE("title"),
        /** Container types. */
        TYPES("types"),
        /** The name of the user that last modified the element. */
        USER("user");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonElement(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Element Property json property constants. */
    public enum JsonProperty {

        /** Property's default value. */
        DEFAULT_VALUE("defaultValue"),
        /** Property's description. */
        DESCRIPTION("description"),
        /** Property's error message. */
        ERROR("error"),
        /** Property's nice name. */
        NICE_NAME("niceName"),
        /** Property's validation regular expression. */
        RULE_REGEX("ruleRegex"),
        /** Property's validation rule type. */
        RULE_TYPE("ruleType"),
        /** Property's type. */
        TYPE("type"),
        /** Property's value. */
        VALUE("value"),
        /** Property's widget. */
        WIDGET("widget"),
        /** Property's widget configuration. */
        WIDGET_CONF("widgetConf");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonProperty(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementUtil.class);

    /** The cms context. */
    private CmsObject m_cms;

    /** The actual container page uri. */
    private String m_cntPageUri;

    /** The container page manager. */
    private CmsADEManager m_manager;

    /** The http request. */
    private HttpServletRequest m_req;

    /** The http response. */
    private HttpServletResponse m_res;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the cms context
     * @param cntPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsElementUtil(CmsObject cms, String cntPageUri, HttpServletRequest req, HttpServletResponse res)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_req = req;
        m_res = res;
        m_cntPageUri = cntPageUri;
        m_manager = OpenCms.getADEManager();
    }

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param element the element bean
     * @param formatter the formatter uri
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    public String getElementContent(CmsContainerElementBean element, CmsResource formatter)
    throws CmsException, ServletException, IOException {

        CmsResource elementRes = m_cms.readResource(element.getElementId());
        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            formatter), elementRes, formatter);

        CmsResource loaderRes = loaderFacade.getLoaderStartResource();

        String oldUri = m_cms.getRequestContext().getUri();
        try {
            m_cms.getRequestContext().setUri(m_cntPageUri);

            // to enable 'old' direct edit features for content-collector-elements, 
            // set the direct-edit-provider-attribute in the request
            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(m_cms, CmsDirectEditMode.TRUE, m_cms.getSitePath(elementRes));
            m_req.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);

            Object currentElement = m_req.getAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT);
            m_req.setAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT, element);
            try {
                return new String(loaderFacade.getLoader().dump(
                    m_cms,
                    loaderRes,
                    null,
                    m_cms.getRequestContext().getLocale(),
                    m_req,
                    m_res), CmsLocaleManager.getResourceEncoding(m_cms, elementRes));
            } finally {
                m_req.setAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT, currentElement);
            }
        } finally {
            m_cms.getRequestContext().setUri(oldUri);
        }
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param element the resource
     * @param types the types supported by the container page
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(CmsContainerElementBean element, Collection<String> types)
    throws CmsException, JSONException {

        // create new json object for the element
        JSONObject resElement = new JSONObject();
        CmsResource resource = m_cms.readResource(element.getElementId());
        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, resource);
        resElement.put(JsonElement.OBJTYPE.getName(), TYPE_ELEMENT);
        resElement.put(JsonElement.ID.getName(), element.getClientId());
        resElement.put(JsonElement.FILE.getName(), resUtil.getFullPath());
        resElement.put(JsonElement.DATE.getName(), resource.getDateLastModified());
        resElement.put(JsonElement.USER.getName(), m_cms.readUser(resource.getUserLastModified()).getName());
        resElement.put(JsonElement.NAVTEXT.getName(), resUtil.getNavText());
        resElement.put(JsonElement.TITLE.getName(), resUtil.getTitle());
        resElement.put(
            JsonElement.NO_EDIT_REASON.getName(),
            CmsEncoder.escapeHtml(resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms))));
        resElement.put(JsonElement.STATUS.getName(), "" + resUtil.getStateAbbreviation());
        // add formatted elements
        JSONObject resContents = new JSONObject();
        resElement.put(JsonElement.CONTENTS.getName(), resContents);
        // add formatter uris
        JSONObject formatters = new JSONObject();
        resElement.put(JsonElement.FORMATTERS.getName(), formatters);

        if (resource.getTypeId() == CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_ID) {
            CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(m_cms, resource, m_req);
            CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                m_cms,
                m_cms.getRequestContext().getLocale());
            resElement.put(JsonElement.DESCRIPTION.getName(), subContainer.getDescription());
            JSONArray jTypes = new JSONArray();
            resElement.put(JsonElement.TYPES.getName(), jTypes);
            if (subContainer.getTypes().isEmpty()) {
                if (subContainer.getElements().isEmpty()) {
                    //TODO: use formatter to generate the 'empty'-content
                    String emptySub = "<div>NEW AND EMPTY</div>";
                    for (String type : types) {
                        formatters.put(type, "formatter");

                        resContents.put(type, emptySub);
                    }
                } else {
                    // TODO: throw appropriate exception
                    return null;
                }
            } else {
                // add formatter and content entries for the supported types
                for (String type : subContainer.getTypes()) {
                    jTypes.put(type);
                    if (types.contains(type)) {
                        formatters.put(type, "formatter"); // empty formatters
                        resContents.put(type, "<div>should not be used</div>"); // empty contents
                    }
                }
            }
            String defaultFormatter = m_manager.getXmlContentFormatters(m_cms, resource).get(
                CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE);
            String jspResult;
            try {
                jspResult = getElementContent(element, m_cms.readResource(defaultFormatter));
                // set the results
                formatters.put(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE, defaultFormatter);
                resContents.put(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE, jspResult); // empty contents
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_GENERATE_FORMATTED_ELEMENT_3,
                    m_cms.getSitePath(resource),
                    defaultFormatter,
                    CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE), e);
            }
            // add subitems
            JSONArray subitems = new JSONArray();
            resElement.put(JsonElement.SUBITEMS.getName(), subitems);
            // iterate the elements
            for (CmsContainerElementBean subElement : subContainer.getElements()) {
                // collect ids
                subitems.put(subElement.getClientId());
            }

        } else {
            Iterator<Map.Entry<String, String>> it = m_manager.getXmlContentFormatters(m_cms, resource).entrySet().iterator();
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
                    String jspResult = getElementContent(element, m_cms.readResource(formatterUri));
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

    /** JSON response state value constant. */
    public static final String TYPE_ELEMENT = "Element";

    /**
     * Returns the property information for the given element as a JSON object.<p>
     * 
     * @param element the element
     * @return the property information
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong generating the JSON
     */
    public JSONObject getElementPropertyInfo(CmsContainerElementBean element) throws CmsException, JSONException {

        CmsResource elementRes = m_cms.readResource(element.getElementId());
        CmsUserSettings settings = new CmsUserSettings(m_cms.getRequestContext().currentUser());
        CmsMessages messages = CmsXmlContentDefinition.getContentHandlerForResource(m_cms, elementRes).getMessages(
            settings.getLocale());
        JSONObject result = new JSONObject();
        JSONObject jSONProperties = new JSONObject();
        Map<String, CmsXmlContentProperty> propertiesConf = m_manager.getElementPropertyConfiguration(m_cms, elementRes);
        Map<String, CmsProperty> properties = m_manager.getElementProperties(m_cms, element);
        Iterator<Map.Entry<String, CmsXmlContentProperty>> itProperties = propertiesConf.entrySet().iterator();
        while (itProperties.hasNext()) {
            Map.Entry<String, CmsXmlContentProperty> entry = itProperties.next();
            String propertyName = entry.getKey();
            CmsXmlContentProperty conf = entry.getValue();
            CmsMacroResolver.resolveMacros(conf.getWidgetConfiguration(), m_cms, Messages.get().getBundle());
            JSONObject jSONProperty = new JSONObject();
            jSONProperty.put(JsonProperty.VALUE.getName(), properties.get(propertyName).getStructureValue());
            jSONProperty.put(JsonProperty.DEFAULT_VALUE.getName(), conf.getDefault());
            jSONProperty.put(JsonProperty.TYPE.getName(), conf.getPropertyType());
            jSONProperty.put(JsonProperty.WIDGET.getName(), conf.getWidget());
            jSONProperty.put(JsonProperty.WIDGET_CONF.getName(), CmsMacroResolver.resolveMacros(
                conf.getWidgetConfiguration(),
                m_cms,
                messages));
            jSONProperty.put(JsonProperty.RULE_TYPE.getName(), conf.getRuleType());
            jSONProperty.put(JsonProperty.RULE_REGEX.getName(), conf.getRuleRegex());
            jSONProperty.put(JsonProperty.NICE_NAME.getName(), CmsMacroResolver.resolveMacros(
                conf.getNiceName(),
                m_cms,
                messages));
            jSONProperty.put(JsonProperty.DESCRIPTION.getName(), CmsMacroResolver.resolveMacros(
                conf.getDescription(),
                m_cms,
                messages));
            jSONProperty.put(JsonProperty.ERROR.getName(), CmsMacroResolver.resolveMacros(
                conf.getError(),
                m_cms,
                messages));
            jSONProperties.put(propertyName, jSONProperty);
        }
        result.put(JsonElement.PROPERTIES.getName(), jSONProperties);
        return result;
    }
}
