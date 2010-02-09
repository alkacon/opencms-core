/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/sitemap/Attic/CmsSitemapServer.java,v $
 * Date   : $Date: 2010/02/09 11:28:49 $
 * Version: $Revision: 1.45 $
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

package org.opencms.workplace.editors.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.A_CmsAjaxServer;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.sitemap.CmsSitemapBean;
import org.opencms.xml.sitemap.CmsSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.CmsXmlSitemap;
import org.opencms.xml.sitemap.CmsXmlSitemapFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Sitemap server used for client/server communication.<p>
 * 
 * see jsp file <tt>/system/workplace/editors/sitemap/server.jsp</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.45 $
 * 
 * @since 7.6
 */
public class CmsSitemapServer extends A_CmsAjaxServer {

    /** Element Property json property constants. */
    public enum JsonProperty {

        /** Property's default value. */
        defaultValue,
        /** Property's description. */
        description,
        /** Property's error message. */
        error,
        /** Property's nice name. */
        niceName,
        /** Property's validation regular expression. */
        ruleRegex,
        /** Property's validation rule type. */
        ruleType,
        /** Property's type. */
        type,
        /** Property's value. */
        value,
        /** Property's widget. */
        widget,
        /** Property's widget configuration. */
        widgetConf;
    }

    /** Request parameter action value constants. */
    protected enum Action {
        /** First call to get all the data. */
        ALL,
        /** To fill in the content of a sitemap entry. */
        CONTENT,
        /** To retrieve the favorite or recent list. */
        GET,
        /** To create a new resource as a sitemap entry */
        NEW_ENTRY,
        /** To create a new sitemap */
        NEW_SITEMAP,
        /** To retrieve the sitemap's property definitions. */
        PROPS,
        /** To save the sitemap. */
        SAVE,
        /** To retrieve the favorite or recent list. */
        SET,
        /** To lock the sitemap. */
        STARTEDIT,
        /** To unlock the sitemap. */
        STOPEDIT,
        /** To validate a site entry name. */
        VALIDATE
    }

    /** Json property name constants for request parameters. */
    protected enum JsonRequest {

        /** To retrieve or save the favorites list. */
        fav,
        /** The validated/converted site entry name. */
        name,
        /** To retrieve or save the recent list. */
        rec,
        /** Title for a new sitemap */
        title,
        /** The type for which a new entry should be created. */
        type;
    }

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** The description property. */
        desciption,
        /** List of sitemap entries */
        entries,
        /** Single sitemap entry */
        entry,
        /** The favorites list. */
        favorites,
        /** The image property. */
        image,
        /** Models of creatable types */
        models,
        /** The validated/converted site entry name. */
        name,
        /** path of newly created sitemap */
        path,
        /** The properties configuration. */
        properties,
        /** The recent list. */
        recent,
        /** The path of the resource which references the sitemap by its sitemap property */
        referencePath,
        /** The sitemap tree. */
        sitemap,
        /** The sitepath. */
        sitepath,
        /** Super sitemap URI */
        superSitemap,
        /** The title property. */
        title,
        /** Creatable types */
        types
    }

    /** Json property name constants for sitemap entries. */
    protected enum JsonSiteEntry {

        /** The HTML content. */
        content,
        /** The entry id. */
        id,
        /** The resource id. */
        linkId,
        /** The name. */
        name,
        /** The entry's VFS path */
        path,
        /** The properties. */
        properties,
        /** The sub-entries. */
        subentries,
        /** The title. */
        title;
    }

    /** Json property name constants for template information. */
    protected enum JsonTemplate {

        /** The default template */
        defaultTemplate,

        /** The description. */
        description,

        /** The image path. */
        imagepath,

        /** The template site path. */
        sitepath,

        /** The template. */
        template,

        /** The title. */
        title

    }

    /** Request parameter name constants. */
    protected enum ReqParam {

        /** The action of execute. */
        action,
        /** Generic data parameter. */
        data,
        /** The current locale. */
        locale,
        /** The sitemap uri. */
        sitemap;
    }

    /** Request attribute name constant for the current sitemap entry bean. */
    public static final String ATTR_SITEMAP_ENTRY = "__sitemapEntry";

    /** Formatters map key constant. */
    public static final Integer KEY_DEFAULT_FORMATTER = new Integer(-100);

    /** Path constant. */
    public static final String PATH_DEFAULT_FORMATTER = "/system/workplace/editors/sitemap/default-formatter.jsp";

    /** Template image property name. */
    public static final String PROPERTY_TEMPLATE_IMAGE = "ade.image";

    /** User additional info key constant. */
    protected static final String ADDINFO_SITEMAP_FAVORITE_LIST = "SITEMAP_FAVORITE_LIST";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapServer.class);

    /** The site map entry formatters. */
    private Map<Integer, CmsResource> m_formatters;

    /** The session cache. */
    private CmsSitemapSessionCache m_sessionCache;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsSitemapServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        m_sessionCache = (CmsSitemapSessionCache)req.getSession().getAttribute(
            CmsSitemapSessionCache.SESSION_ATTR_SITEMAP_CACHE);
        if (m_sessionCache == null) {
            m_sessionCache = new CmsSitemapSessionCache(getCmsObject());
            req.getSession().setAttribute(CmsSitemapSessionCache.SESSION_ATTR_SITEMAP_CACHE, m_sessionCache);
        }
        if (m_formatters == null) {
            m_formatters = new HashMap<Integer, CmsResource>();
            try {
                m_formatters.put(KEY_DEFAULT_FORMATTER, getCmsObject().readResource(
                    CmsSitemapServer.PATH_DEFAULT_FORMATTER));
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns a JSON object representing a given template.<p>
     * 
     * @param cms the CmsObject to use for VFS operations.
     * @param template the template resource
     * 
     * @return a JSON object that represents the template
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if a JSON operation goes wrong
     */
    public static JSONObject getTemplateJson(CmsObject cms, CmsResource template) throws CmsException, JSONException {

        CmsProperty titleProp = cms.readPropertyObject(template, CmsPropertyDefinition.PROPERTY_TITLE, false);
        CmsProperty descProp = cms.readPropertyObject(template, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        CmsProperty imageProp = cms.readPropertyObject(template, PROPERTY_TEMPLATE_IMAGE, false);
        JSONObject jTemp = new JSONObject();
        jTemp.put(JsonTemplate.sitepath.name(), cms.getSitePath(template));
        jTemp.put(JsonTemplate.title.name(), titleProp.getValue());
        jTemp.put(JsonTemplate.description.name(), descProp.getValue());
        jTemp.put(JsonTemplate.imagepath.name(), imageProp.getValue());
        return jTemp;
    }

    /**
     * Handles all sitemap requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     * 
     * @see org.opencms.workplace.A_CmsAjaxServer#executeAction()
     */
    @Override
    public JSONObject executeAction() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();

        if (!checkParameters(request, result, ReqParam.action.name(), ReqParam.locale.name(), ReqParam.sitemap.name())) {
            // every request needs to have at least these parameters 
            return result;
        }
        String actionParam = request.getParameter(ReqParam.action.name());
        Action action = Action.valueOf(actionParam.toUpperCase());
        String localeParam = request.getParameter(ReqParam.locale.name());
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        JSONObject data = new JSONObject();
        if (checkParameters(request, null, ReqParam.data.name())) {
            String dataParam = request.getParameter(ReqParam.data.name());
            data = new JSONObject(dataParam);
        }
        String sitemapParam = request.getParameter(ReqParam.sitemap.name());

        if (action.equals(Action.ALL)) {
            // this should also work for historical requests
            CmsResource sitemapRes = CmsHistoryResourceHandler.getResourceWithHistory(cms, sitemapParam);
            // first load, get everything
            CmsXmlSitemap xmlSitemap = CmsXmlSitemapFactory.unmarshal(cms, sitemapRes, request);
            result = getSitemap(xmlSitemap);

            CmsSitemapManager manager = OpenCms.getSitemapManager();
            List<CmsResource> creatableElements = manager.getCreatableElements(cms, sitemapParam, request);
            JSONArray creatableTypes = new JSONArray();
            JSONArray models = new JSONArray();
            for (CmsResource resource : creatableElements) {
                String typeName = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();
                creatableTypes.put(typeName);
                CmsSitemapEntry siteEntryData = resourceToSiteEntryBean(cms, resource);
                if (typeName.equals(CmsResourceTypeXmlContainerPage.getStaticTypeName())) {
                    models.put(jsonifyEntry(siteEntryData, getPropertyConfig(sitemapRes)));
                }
            }
            addContents(models, getPropertyConfig(sitemapRes));
            result.put(JsonResponse.types.name(), creatableTypes);
            result.put(JsonResponse.models.name(), models);
            result.put(JsonTemplate.template.name(), getTemplates());
            CmsResource defaultTemplate = OpenCms.getSitemapManager().getDefaultTemplate(cms, sitemapParam, request);
            if (defaultTemplate != null) {
                JSONObject templateJson = getTemplateJson(cms, defaultTemplate);
                result.put(JsonTemplate.defaultTemplate.name(), templateJson);
            }
        } else if (action.equals(Action.GET)) {
            if (checkParameters(data, null, JsonRequest.fav.name())) {
                // get the favorite list
                result.put(JsonResponse.favorites.name(), getFavoriteList());
            } else if (checkParameters(data, result, JsonRequest.rec.name())) {
                CmsResource sitemapRes = CmsHistoryResourceHandler.getResourceWithHistory(cms, sitemapParam);
                // get recent list
                result.put(JsonResponse.recent.name(), addIds(addContents(
                    m_sessionCache.getRecentList(),
                    getPropertyConfig(sitemapRes))));
            } else {
                return result;
            }
        } else if (action.equals(Action.SET)) {
            if (checkParameters(data, null, JsonRequest.fav.name())) {
                // save the favorite list
                JSONArray list = data.optJSONArray(JsonRequest.fav.name());
                saveFavoriteList(cms, list);
            } else if (checkParameters(data, result, JsonRequest.rec.name())) {
                // save the recent list
                JSONArray list = data.optJSONArray(JsonRequest.rec.name());
                m_sessionCache.setRecentList(list);
            } else {
                return result;
            }
        } else if (action.equals(Action.VALIDATE)) {
            if (!checkParameters(data, result, JsonRequest.name.name())) {
                return result;
            }
            String name = data.optString(JsonRequest.name.name());
            result.put(JsonResponse.name.name(), cms.getRequestContext().getFileTranslator().translateResource(name));
        } else if (action.equals(Action.PROPS)) {
            CmsResource sitemapRes = cms.readResource(sitemapParam);
            result.put(JsonResponse.properties.name(), CmsXmlContentPropertyHelper.getPropertyInfoJSON(cms, sitemapRes));
        } else if (action.equals(Action.SAVE)) {
            // save the sitemap
            CmsResource sitemapRes = cms.readResource(sitemapParam);
            saveSitemap(sitemapRes, data);
        } else if (action.equals(Action.STARTEDIT)) {
            // lock the sitemap
            try {
                cms.lockResourceTemporary(sitemapParam);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                error(result, e.getLocalizedMessage(getWorkplaceLocale()));
            }
        } else if (action.equals(Action.STOPEDIT)) {
            // lock the sitemap
            try {
                cms.unlockResource(sitemapParam);
            } catch (CmsException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } else if (action.equals(Action.CONTENT)) {
            CmsResource sitemapRes = CmsHistoryResourceHandler.getResourceWithHistory(cms, sitemapParam);
            JSONArray entries = data.getJSONArray(JsonResponse.entries.name().toLowerCase());
            addContents(entries, getPropertyConfig(sitemapRes));
            result.put(JsonResponse.entries.name().toLowerCase(), entries);
        } else if (action.equals(Action.NEW_ENTRY)) {
            CmsResource sitemapRes = CmsHistoryResourceHandler.getResourceWithHistory(cms, sitemapParam);
            CmsSitemapManager manager = OpenCms.getSitemapManager();
            String type = data.getString(JsonRequest.type.name());
            CmsResource newResource = manager.createNewElement(cms, sitemapParam, request, type);
            CmsProperty titleProp = cms.readPropertyObject(newResource, CmsPropertyDefinition.PROPERTY_TITLE, false);
            String title = titleProp.getValue();
            String name = newResource.getName();
            CmsSitemapEntry entryBean = new CmsSitemapEntry(new CmsUUID(), null, // this is not needed since it will be correctly set while saving
                newResource.getStructureId(),
                name,
                title,
                new HashMap<String, String>(),
                new ArrayList<CmsSitemapEntry>());
            Map<String, CmsXmlContentProperty> propertyConf = getPropertyConfig(sitemapRes);
            JSONObject jsonEntry = jsonifyEntry(entryBean, propertyConf);
            JSONArray entries = new JSONArray();
            entries.put(jsonEntry);
            result.put(JsonResponse.entry.name(), jsonEntry);
        } else if (action.equals(Action.NEW_SITEMAP)) {
            CmsSitemapManager manager = OpenCms.getSitemapManager();
            CmsResource newSitemapRes = manager.createSitemap(
                cms,
                data.getString(JsonRequest.title.name()),
                sitemapParam,
                request);
            saveSitemap(newSitemapRes, data);
            //String sitemapId = newSitemapRes.getStructureId().toString();
            result.put(JsonResponse.path.name(), cms.getSitePath(newSitemapRes));
        } else {
            error(result, Messages.get().getBundle(getWorkplaceLocale()).key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                ReqParam.action.name(),
                actionParam));
        }
        return result;
    }

    /**
     * Returns the content of an entry when rendered with the given formatter.<p> 
     * 
     * @param entry the site entry bean
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    public String getEntryContent(CmsSitemapEntry entry) throws CmsException, ServletException, IOException {

        CmsObject cms = getCmsObject();
        CmsResource elementRes = cms.readResource(entry.getResourceId());
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(elementRes);
        // HACK: note the here we do not have any runtime properties
        return type.getFormattedContent(
            cms,
            getRequest(),
            getResponse(),
            I_CmsResourceType.Formatter.SITEMAP,
            Collections.<String, Object> singletonMap(ATTR_SITEMAP_ENTRY, entry));
    }

    /**
     * Returns the current user's favorites list.<p>
     * 
     * @return the current user's favorites list
     * 
     * @throws CmsException if something goes wrong
     */
    public JSONArray getFavoriteList() throws CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.getRequestContext().currentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_SITEMAP_FAVORITE_LIST);

        JSONArray jsonFavList = new JSONArray();
        if (obj instanceof String) {
            try {
                jsonFavList = new JSONArray((String)obj);
            } catch (Throwable e) {
                // should never happen, catches JSON parsing
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } else {
            // save to be better next time
            try {
                saveFavoriteList(cms, jsonFavList);
            } catch (CmsException e) {
                // should never happen, error writing user's additional info 
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        // TODO: this will fail, but the favorite list will be completely changed anyway 
        return addContents(jsonFavList, null);
    }

    /**
     * Returns the data for the given sitemap.<p>
     * 
     * @param xmlSitemap the XML sitemap to use
     * 
     * @return the data for the given sitemap
     * 
     * @throws JSONException if something goes wrong with the JSON manipulation
     * @throws CmsException if something goes wrong
     */
    public JSONObject getSitemap(CmsXmlSitemap xmlSitemap) throws JSONException, CmsException {

        CmsObject cms = getCmsObject();
        CmsSitemapBean sitemap = xmlSitemap.getSitemap(cms, cms.getRequestContext().getLocale());
        // create empty result object
        JSONObject result = new JSONObject();

        // get properties configuration 
        Map<String, CmsXmlContentProperty> propertiesConf = getPropertyConfig(xmlSitemap.getFile());

        // collect the site map entries
        JSONArray siteEntries = new JSONArray();
        for (CmsSitemapEntry entry : sitemap.getSiteEntries()) {
            JSONObject siteEntry = jsonifyEntry(entry, propertiesConf);
            if (siteEntry != null) {
                siteEntries.put(siteEntry);
            }
        }
        result.put(JsonResponse.sitemap.name(), siteEntries);

        CmsXmlSitemap superSitemap = OpenCms.getSitemapManager().getParentSitemap(cms, xmlSitemap);
        String superSitemapPath = superSitemap == null ? "" : cms.getSitePath(superSitemap.getFile());
        result.put(JsonResponse.superSitemap.name(), superSitemapPath);
        result.put(JsonResponse.referencePath.name(), cms.getRequestContext().removeSiteRoot(sitemap.getEntryPoint()));

        // collect the properties
        result.put(JsonResponse.properties.name(), CmsXmlContentPropertyHelper.getPropertyInfoJSON(
            cms,
            xmlSitemap.getFile()));

        return result;
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param favoriteList the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveFavoriteList(CmsObject cms, JSONArray favoriteList) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_SITEMAP_FAVORITE_LIST, favoriteList.toString());
        cms.writeUser(user);
    }

    /**
     * Saves the new state of the sitemap.<p>
     * 
     * @param sitemapRes the sitemap resource to save
     * @param sitemap the sitemap data
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    public void saveSitemap(CmsResource sitemapRes, JSONObject sitemap) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        CmsXmlSitemap xmlSitemap = CmsXmlSitemapFactory.unmarshal(cms, cms.readFile(sitemapRes), getRequest());
        xmlSitemap.save(cms, jsonToEntryList(
            sitemap.getJSONArray(JsonResponse.sitemap.name()),
            getPropertyConfig(sitemapRes),
            true));
    }

    /**
     * Adds the HTML content to the JSON list of site entries.<p>
     * 
     * @param jsonEntriesList the JSON list of site entries
     * @param propertyConf the property configuration to use
     * 
     * @return the original JSON list of site entries with HTML contents
     * 
     * @throws CmsException if something goes wrong
     */
    protected JSONArray addContents(JSONArray jsonEntriesList, Map<String, CmsXmlContentProperty> propertyConf)
    throws CmsException {

        List<CmsSitemapEntry> list = jsonToEntryList(jsonEntriesList, propertyConf, false);
        for (int i = 0; i < jsonEntriesList.length(); i++) {
            try {
                JSONObject json = jsonEntriesList.getJSONObject(i);
                json.put(JsonSiteEntry.content.name(), getEntryContent(list.get(i)));
                addContents(json.getJSONArray(JsonSiteEntry.subentries.name()), propertyConf);
            } catch (Exception e) {
                // should never happen, catches JSON parsing
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return jsonEntriesList;
    }

    /**
     * Adds UUIDs to the JSON list of site entries.<p>
     * 
     * @param jsonEntriesList the JSON list of site entries
     * 
     * @return the original JSON list of site entries with newly generated ids 
     * 
     */
    protected JSONArray addIds(JSONArray jsonEntriesList) {

        for (int i = 0; i < jsonEntriesList.length(); i++) {
            try {
                JSONObject json = jsonEntriesList.getJSONObject(i);
                json.put(JsonSiteEntry.id.name(), (new CmsUUID()).toString());
                addIds(json.getJSONArray(JsonSiteEntry.subentries.name()));
            } catch (Exception e) {
                // should never happen, catches JSON parsing
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return jsonEntriesList;
    }

    /**
     * Returns the property configuration for the given sitemap.<p>
     * 
     * @param sitemap the sitemap resource for which the property configuration should be returned
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    protected Map<String, CmsXmlContentProperty> getPropertyConfig(CmsResource sitemap) throws CmsException {

        return CmsXmlContentDefinition.getContentHandlerForResource(getCmsObject(), sitemap).getProperties();
    }

    /**
     * Returns the templates available.<p>
     * 
     * @return a JSON object containing the templates available.
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject getTemplates() throws CmsException, JSONException {

        JSONObject result = new JSONObject();
        CmsObject cms = getCmsObject();
        List<CmsResource> templates = cms.readResources(
            "/",
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(CmsResourceTypeJsp.getContainerPageTemplateTypeId()),
            true);
        templates.addAll(cms.readResources(
            "/system/",
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(CmsResourceTypeJsp.getContainerPageTemplateTypeId()),
            true));
        Iterator<CmsResource> templateIt = templates.iterator();
        while (templateIt.hasNext()) {
            CmsResource template = templateIt.next();
            JSONObject jTemp = CmsSitemapServer.getTemplateJson(cms, template);
            result.put(cms.getSitePath(template), jTemp);
        }
        return result;
    }

    /**
     * Converts a site entry bean into a JSON object.<p>
     * 
     * @param entry the entry to convert
     * @param propertyConf the property configuration to use for converting vfslist properties from ids to paths 
     * 
     * @return the JSON representation, can be <code>null</code> in case of not enough permissions
     * 
     * @throws JSONException if an error occurs during the JSON processing
     * @throws CmsException if something goes wrong
     */
    protected JSONObject jsonifyEntry(CmsSitemapEntry entry, Map<String, CmsXmlContentProperty> propertyConf)
    throws JSONException, CmsException {

        JSONObject result = new JSONObject();
        result.put(JsonSiteEntry.id.name(), entry.getId().toString());
        result.put(JsonSiteEntry.name.name(), entry.getName());
        result.put(JsonSiteEntry.title.name(), entry.getTitle());
        result.put(JsonSiteEntry.linkId.name(), entry.getResourceId().toString());

        // properties
        CmsObject cms = getCmsObject();
        JSONObject props = new JSONObject();
        for (Map.Entry<String, String> prop : entry.getProperties().entrySet()) {
            String key = prop.getKey();
            CmsXmlContentProperty currentPropertyConf = propertyConf.get(key);
            String propType = CmsXmlContentProperty.PropType.string.name();
            if (currentPropertyConf != null) {
                propType = currentPropertyConf.getPropertyType();
            }
            props.put(key, CmsXmlContentPropertyHelper.getPropValuePaths(cms, propType, prop.getValue()));
        }
        result.put(JsonSiteEntry.properties.name(), props);

        // content
        try {
            result.put(JsonSiteEntry.content.name(), getEntryContent(entry));
        } catch (CmsVfsResourceNotFoundException e) {
            // most likely the user has no read permission on this entry
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        } catch (CmsPermissionViolationException e) {
            // most likely the user has no read permission on this entry
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            result.put(JsonSiteEntry.content.name(), e.getLocalizedMessage());
        }

        // subentries
        JSONArray subentries = new JSONArray();
        for (CmsSitemapEntry subentry : entry.getSubEntries()) {
            JSONObject jsonSubEntry = jsonifyEntry(subentry, propertyConf);
            if (jsonSubEntry != null) {
                subentries.put(jsonSubEntry);
            }
        }
        result.put(JsonSiteEntry.subentries.name(), subentries);

        return result;
    }

    /**
     * Returns a list of sitemap entries from a json array.<p>
     * 
     * @param array the json array
     * @param propertyConf the property configuration to use for the path-to-UUID translation
     * @param recursive if recursive
     * @throws CmsException if the path-to-UUID translation fails
     * 
     * @return a list of sitemap entries
     */
    protected List<CmsSitemapEntry> jsonToEntryList(
        JSONArray array,
        Map<String, CmsXmlContentProperty> propertyConf,
        boolean recursive) throws CmsException {

        List<CmsSitemapEntry> result = new ArrayList<CmsSitemapEntry>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            CmsUUID id;
            String entryId = json.optString(JsonSiteEntry.id.name());
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(entryId)) {
                try {
                    id = new CmsUUID(entryId);
                } catch (NumberFormatException e) {
                    LOG.error(e.getLocalizedMessage());
                    continue;
                }
            } else {
                // create a new id if missing
                id = new CmsUUID();
            }
            CmsUUID linkId;
            try {
                linkId = new CmsUUID(json.optString(JsonSiteEntry.linkId.name()));
            } catch (NumberFormatException e) {
                LOG.error(e.getLocalizedMessage());
                continue;
            }

            if (json.has(JsonSiteEntry.path.name())) {
                // if there is a path, it overrides the UUID 
                String path = json.optString(JsonSiteEntry.path.name());
                try {
                    CmsResource resource = getCmsObject().readResource(path);
                    linkId = resource.getStructureId();
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
            CmsObject cms = getCmsObject();
            String name = json.optString(JsonSiteEntry.name.name());
            String title = json.optString(JsonSiteEntry.title.name());
            Map<String, String> properties = new HashMap<String, String>();
            JSONObject jsonProps = json.optJSONObject(JsonSiteEntry.properties.name());
            Iterator<String> itKeys = jsonProps.keys();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                CmsXmlContentProperty currentPropertyConf = propertyConf.get(key);
                String propType = CmsXmlContentProperty.PropType.string.name();
                if (currentPropertyConf != null) {
                    propType = currentPropertyConf.getPropertyType();
                }
                properties.put(key, CmsXmlContentPropertyHelper.getPropValueIds(
                    cms,
                    propType,
                    jsonProps.optString(key)));
            }
            JSONArray jsonSub = json.optJSONArray(JsonSiteEntry.subentries.name());
            CmsSitemapEntry entry = new CmsSitemapEntry(id, null, linkId, name, title, properties, recursive
            ? jsonToEntryList(jsonSub, propertyConf, true)
            : Collections.<CmsSitemapEntry> emptyList());
            result.add(entry);
        }
        return result;
    }

    /**
     * Converts a resource to a sitemap entry bean.<p>
     *
     * @param cms the CmsObject to use for VFS operations 
     * @param resource the resource to convert
     * 
     * @return the sitemap entry bean for the resource
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapEntry resourceToSiteEntryBean(CmsObject cms, CmsResource resource) throws CmsException {

        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
        String title = titleProp.getValue();
        String name = resource.getName();

        int dotPos = name.lastIndexOf('.');
        if (dotPos != -1) {
            name = name.substring(0, dotPos);
        }

        CmsSitemapEntry entryBean = new CmsSitemapEntry(
            new CmsUUID(),
            null,
            resource.getStructureId(),
            name,
            title,
            new HashMap<String, String>(),
            new ArrayList<CmsSitemapEntry>());

        return entryBean;
    }
}
