/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONTokener;
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Example implementation of a template context provider for deciding between a desktop template and a mobile template.<p>
 * 
 * The template JSP paths are read from a VFS file "/system/shared/templatecontexts.json" 
 * 
 */
public class CmsDefaultTemplateContextProvider implements I_CmsTemplateContextProvider {

    /** JSON attribute name. */
    public static final String A_HEIGHT = "height";

    /** JSON attribute name. */
    public static final String A_NICE_NAME = "niceName";

    /** JSON attribute name. */
    public static final String A_PATH = "path";

    /** JSON attribute name. */
    public static final String A_VARIANTS = "variants";

    /** JSON attribute name. */
    public static final String A_WIDTH = "width";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultTemplateContextProvider.class);

    /** The stored Cms context. */
    private CmsObject m_cms;

    /** Default constructor. */
    public CmsDefaultTemplateContextProvider() {

    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getAllContexts()
     */
    public synchronized Map<String, CmsTemplateContext> getAllContexts() {

        return Collections.unmodifiableMap(getContextMap());
    }

    /**
     * Returns the absolute VFS path where the configuration property file is stored.<p>
     * 
     * 
     * @return the absolute VFS path where the configuration property file is stored
     */
    public String getConfigurationPropertyPath() {

        return "/system/shared/templatecontexts.json";
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getEditorStyleSheet(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getEditorStyleSheet(CmsObject cms, String editedResourcePath) {

        String templatePath = getContextMap().get("desktop").getTemplatePath();
        String result = null;
        try {
            CmsProperty property = cms.readPropertyObject(templatePath, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
            if (!property.isNullProperty()) {
                result = property.getValue();
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getOverrideCookieName()
     */
    public String getOverrideCookieName() {

        return "templatecontext";
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getTemplateContext(org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, org.opencms.file.CmsResource)
     */
    public synchronized CmsTemplateContext getTemplateContext(
        CmsObject cms,
        HttpServletRequest request,
        CmsResource resource) {

        I_CmsJspDeviceSelector selector = OpenCms.getSystemInfo().getDeviceSelector();
        String deviceType = selector.getDeviceType(request);
        Map<String, CmsTemplateContext> contextMap = getContextMap();
        if (contextMap.containsKey(deviceType)) {
            return contextMap.get(deviceType);
        } else {
            return contextMap.get("desktop");
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        m_cms = cms;
        getContextMap();
    }

    /**
     * Gets the context map, either from a cache or from the VFS if it'S not already cached.<p>
     * 
     * @return the context map 
     */
    @SuppressWarnings("unchecked")
    private Map<String, CmsTemplateContext> getContextMap() {

        Object cachedObj = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(
            m_cms,
            getConfigurationPropertyPath());
        if (cachedObj != null) {
            return (Map<String, CmsTemplateContext>)cachedObj;
        } else {
            try {
                Map<String, CmsTemplateContext> map = initMap();
                CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(
                    m_cms,
                    getConfigurationPropertyPath(),
                    map);
                return map;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return Collections.emptyMap();
            }
        }

    }

    /**
     * Loads the context map from the VFS.<p>
     * 
     * @return the context map  
     * @throws Exception if something goes wrong  
     */
    private Map<String, CmsTemplateContext> initMap() throws Exception {

        Map<String, CmsTemplateContext> result = new LinkedHashMap<String, CmsTemplateContext>();
        String path = getConfigurationPropertyPath();
        CmsResource resource = m_cms.readResource(path);
        CmsFile file = m_cms.readFile(resource);
        String fileContent = new String(file.getContents(), "UTF-8");
        JSONTokener tok = new JSONTokener(fileContent);
        tok.setOrdered(true);
        JSONObject root = new JSONObject(tok, true);
        for (String templateContextName : root.keySet()) {
            JSONObject templateContextJson = (JSONObject)(root.opt(templateContextName));
            CmsJsonMessageContainer jsonMessage = new CmsJsonMessageContainer(templateContextJson.opt(A_NICE_NAME));
            String templatePath = (String)templateContextJson.opt(A_PATH);
            JSONObject variantsJson = (JSONObject)templateContextJson.opt(A_VARIANTS);
            List<CmsClientVariant> variants = new ArrayList<CmsClientVariant>();
            if (variantsJson != null) {
                for (String variantName : variantsJson.keySet()) {
                    JSONObject variantJson = (JSONObject)variantsJson.opt(variantName);
                    CmsJsonMessageContainer variantMessage = new CmsJsonMessageContainer(variantJson.opt(A_NICE_NAME));
                    int width = variantJson.optInt(A_WIDTH, 800);
                    int height = variantJson.optInt(A_HEIGHT, 600);
                    CmsClientVariant variant = new CmsClientVariant(
                        variantName,
                        variantMessage,
                        width,
                        height,
                        new HashMap<String, String>());
                    variants.add(variant);
                }
            }
            CmsTemplateContext templateContext = new CmsTemplateContext(
                templateContextName,
                templatePath,
                jsonMessage,
                this,
                variants,
                false);
            result.put(templateContextName, templateContext);

        }
        return result;
    }
}
