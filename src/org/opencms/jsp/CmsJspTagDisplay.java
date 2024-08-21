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

package org.opencms.jsp;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.CmsElementUtil;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspContentAccessValueWrapper;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;

/**
 * The 'display' tag can be used to display a single resource using a formatter. It also allows to activate direct editing.<p>
 */
public class CmsJspTagDisplay extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** Setting used to store the display formatter key. */
    public static final String DISPLAY_FORMATTER_SETTING = "SYSTEM::DISPLAY_FORMATTER";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagDisplay.class);

    /** The serial version id. */
    private static final long serialVersionUID = 2285680951218629093L;

    /** The base URI. */
    private String m_baseUri;

    /** True if the display formatter include should go through the flex cache. */
    private Boolean m_cacheable;

    /** Flag, indicating if the create option should be displayed. */
    private boolean m_canCreate;

    /** Flag, indicating if the delete option should be displayed. */
    private boolean m_canDelete;

    /** The container type used to select a formatter. */
    private String m_containerType;

    /** The tag attribute's value, specifying the path to the (sub)sitemap where new content should be created. */
    private String m_creationSiteMap;

    /** The display formatter ids. */
    private Map<String, String> m_displayFormatterIds;

    /** The display formatter paths. */
    private Map<String, String> m_displayFormatterPaths;

    /** The editable flag. */
    private boolean m_editable;

    /** The settings parameter map. */
    private Map<String, String> m_parameterMap;

    /** The pass settings flag. */
    private boolean m_passSettings;

    /** The fully qualified class name of the post create handler to use. */
    private String m_postCreateHandler;

    /** The element settings to be used. */
    private Map<String, String> m_settings;

    /** The upload folder. */
    private String m_uploadFolder;

    /** The site path to the resource to display. */
    private String m_value;

    /**
     * Constructor.<p>
     */
    public CmsJspTagDisplay() {

        m_parameterMap = new LinkedHashMap<>();
        m_displayFormatterPaths = new HashMap<>();
        m_displayFormatterIds = new HashMap<>();
    }

    /**
     * Includes the formatter rendering the given element.<p>
     *
     * @param element the element
     * @param formatter the formatter configuration bean
     * @param cacheable true if the flex cache should be used for calling the display formatter
     * @param editable if editable
     * @param canCreate if new resources may be created
     * @param canDelete if the resource may be deleted
     * @param creationSiteMap the create location sub site
     * @param postCreateHandler the post create handler
     * @param uploadFolder the upload folder to use
     * @param context the page context
     * @param request the request
     * @param response the response
     */
    public static void displayAction(
        CmsContainerElementBean element,
        I_CmsFormatterBean formatter,
        boolean cacheable,
        boolean editable,
        boolean canCreate,
        boolean canDelete,
        String creationSiteMap,
        String postCreateHandler,
        String uploadFolder,
        PageContext context,
        ServletRequest request,
        ServletResponse response) {

        if (CmsFlexController.isCmsRequest(request)) {
            // this will always be true if the page is called through OpenCms
            CmsObject cms = CmsFlexController.getCmsObject(request);
            CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfigurationWithCache(
                cms,
                cms.getRequestContext().getRootUri());
            Locale locale = cms.getRequestContext().getLocale();
            boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
            CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(request);
            CmsContainerElementBean parentElement = contextBean.getElement();

            try {
                if (formatter != null) {
                    element.initResource(cms);
                    element.initSettings(cms, adeConfig, formatter, locale, request, null);
                    element.getSettings().put(DISPLAY_FORMATTER_SETTING, formatter.getKeyOrId());
                    boolean openedEditable = false;
                    contextBean.setElement(element);
                    if (editable && contextBean.getIsEditMode()) {
                        if (CmsJspTagEditable.getDirectEditProvider(context) == null) {
                            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
                            eb.init(cms, CmsDirectEditMode.TRUE, element.getSitePath());
                            request.setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, eb);
                        }

                        openedEditable = CmsJspTagEdit.insertDirectEditStart(
                            cms,
                            context,
                            element.getResource(),
                            canCreate,
                            canDelete,
                            null,
                            creationSiteMap,
                            postCreateHandler,
                            uploadFolder);
                    }
                    if (contextBean.getIsEditMode()) {
                        CmsADESessionCache.getCache(
                            (HttpServletRequest)(context.getRequest()),
                            cms).setCacheContainerElement(element.editorHash(), element);
                    }
                    try {
                        CmsJspTagInclude.includeTagAction(
                            context,
                            cms.getRequestContext().removeSiteRoot(formatter.getJspRootPath()),
                            null,
                            locale,
                            false,
                            isOnline && cacheable,
                            CmsRequestUtil.createParameterMap(element.getSettings()),
                            CmsRequestUtil.getAtrributeMap(request),
                            request,
                            response);
                    } catch (Exception e) {
                        if (CmsJspLoader.isJasperCompilerException(e)) {
                            LOG.error(e.getLocalizedMessage());
                        } else {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    if (openedEditable) {
                        CmsJspTagEdit.insertDirectEditEnd(context);
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            contextBean.setElement(parentElement);
        }

    }

    /**
     * Includes the formatter rendering the given element.<p>
     *
     * @param element the element
     * @param formatter the formatter configuration bean
     * @param context the page context
     * @param request the request
     * @param response the response
     */
    public static void displayAction(
        CmsContainerElementBean element,
        I_CmsFormatterBean formatter,
        PageContext context,
        ServletRequest request,
        ServletResponse response) {

        displayAction(element, formatter, true, false, false, false, null, null, null, context, request, response);
    }

    /**
     * Includes the formatter rendering the given element.<p>
     *
     * @param elementResource the element resource
     * @param formatter the formatter configuration bean
     * @param settings the element settings
     * @param cacheable true if the flex cache should be used for calling the display formatter
     * @param editable if editable
     * @param canCreate if new resources may be created
     * @param canDelete if the resource may be deleted
     * @param creationSiteMap the create location sub site
     * @param postCreateHandler the post create handler
     * @param uploadFolder the upload folder
     * @param context the page context
     * @param request the request
     * @param response the response
     */
    public static void displayAction(
        CmsResource elementResource,
        I_CmsFormatterBean formatter,
        Map<String, String> settings,
        boolean cacheable,
        boolean editable,
        boolean canCreate,
        boolean canDelete,
        String creationSiteMap,
        String postCreateHandler,
        String uploadFolder,
        PageContext context,
        ServletRequest request,
        ServletResponse response) {

        CmsContainerElementBean element = new CmsContainerElementBean(
            elementResource.getStructureId(),
            formatter.getJspStructureId(),
            settings,
            false);
        displayAction(
            element,
            formatter,
            cacheable,
            editable,
            canCreate,
            canDelete,
            creationSiteMap,
            postCreateHandler,
            uploadFolder,
            context,
            request,
            response);
    }

    /**
     * If the setting key starts with the key or id of the given formatter, returns the remaining suffix, else null.
     *
     * @param config the current sitemap configuration
     * @param formatter the formatter bean
     * @param settingKey the setting key
     *
     * @return the remaining setting name suffix
     */
    public static String getSettingKeyForMatchingFormatterPrefix(
        CmsADEConfigData config,
        I_CmsFormatterBean formatter,
        String settingKey) {

        if (CmsElementUtil.isSystemSetting(settingKey)) {
            return null;
        }

        int underscoreIndex = settingKey.indexOf("_");
        if (underscoreIndex < 0) {
            return null;
        }
        String prefix = settingKey.substring(0, underscoreIndex);
        String suffix = settingKey.substring(underscoreIndex + 1);
        I_CmsFormatterBean dynamicFmt = config.findFormatter(prefix, /*noWarn=*/true);
        if (dynamicFmt == null) {
            return null;
        }
        boolean keyMatch = (dynamicFmt.getKey() != null) && dynamicFmt.getKey().equals(formatter.getKey());
        boolean idMatch = (dynamicFmt.getId() != null) && dynamicFmt.getId().equals(formatter.getId());
        if (!keyMatch && !idMatch) {
            return null;
        }
        if (!dynamicFmt.getSettings(config).containsKey(suffix)) {
            return null;
        }
        return suffix;
    }

    /**
     * Adds a display formatter.<p>
     *
     * @param type the resource type
     * @param path the path to the formatter configuration file.<p>
     */
    public void addDisplayFormatter(String type, String path) {

        m_displayFormatterPaths.put(type, path);
    }

    /**
     * Adds a display formatter key for a type.
     *
     * @param type the resource type
     * @param key the display formatter key
     */
    public void addDisplayFormatterKey(String type, String key) {

        m_displayFormatterIds.put(type, key);
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        // No null values allowed in parameters
        if ((name == null) || (value == null)) {
            return;
        }

        m_parameterMap.put(name, value);
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest request = pageContext.getRequest();
        ServletResponse response = pageContext.getResponse();
        if (CmsFlexController.isCmsRequest(request)) {
            // this will always be true if the page is called through OpenCms
            CmsObject cms = CmsFlexController.getCmsObject(request);
            try {
                boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
                CmsResource res = null;
                if (CmsUUID.isValidUUID(m_value)) {
                    CmsUUID structureId = new CmsUUID(m_value);
                    res = isOnline
                    ? cms.readResource(structureId)
                    : cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                } else {
                    res = isOnline
                    ? cms.readResource(m_value)
                    : cms.readResource(m_value, CmsResourceFilter.IGNORE_EXPIRATION);
                }

                CmsObject cmsForFormatterLookup = cms;
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_baseUri)) {
                    cmsForFormatterLookup = OpenCms.initCmsObject(cms);
                    cmsForFormatterLookup.getRequestContext().setUri(m_baseUri);
                }
                I_CmsFormatterBean formatter = getFormatterForType(cmsForFormatterLookup, res, isOnline);
                CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                    cmsForFormatterLookup,
                    cms.getRequestContext().getRootUri());
                if (formatter == null) {
                    String error = "cms:display - could not find display formatter for " + m_value + "\n";
                    try {
                        error += "\n\nTag instance: " + ReflectionToStringBuilder.toString(this);
                    } catch (Exception e) {
                        // ignore
                    }
                    throw new JspException(error);
                }

                Map<String, String> settings = prepareSettings(config, formatter);

                displayAction(
                    res,
                    formatter,
                    settings,
                    isCacheable(),
                    m_editable,
                    m_canCreate,
                    m_canDelete,
                    m_creationSiteMap,
                    m_postCreateHandler,
                    m_uploadFolder,
                    pageContext,
                    request,
                    response);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        release();
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
     */
    @Override
    public int doStartTag() {

        if (Boolean.valueOf(m_passSettings).booleanValue()) {
            CmsContainerElementBean element = CmsJspStandardContextBean.getInstance(
                pageContext.getRequest()).getElement();
            if (element != null) {
                m_parameterMap.putAll(element.getSettings());
            }
        }
        if (m_settings != null) {
            m_parameterMap.putAll(m_settings);
        }

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the editable.<p>
     *
     * @return the editable
     */
    public boolean getEditable() {

        return m_editable;
    }

    /**
     * Returns the passSettings.<p>
     *
     * @return the passSettings
     */
    public boolean getPassSettings() {

        return m_passSettings;
    }

    /**
     * Returns the element settings to be used.<p>
     *
     * @return the element settings to be used
     */
    public Map<String, String> getSettings() {

        return m_settings;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        return m_value;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
     */
    @Override
    public void release() {

        super.release();
        m_parameterMap.clear();
        m_displayFormatterPaths.clear();
        m_displayFormatterIds.clear();
        m_settings = null;
        m_passSettings = false;
        m_editable = false;
        m_value = null;
    }

    /**
     * Sets the base URI to use for finding the 'default' display formatter.
     *
     * @param uri the base URI
     */
    public void setBaseUri(String uri) {

        m_baseUri = uri;
    }

    /**
     * Enables/disables the use of the flex cache for the display formatter include.
     *
     * @param cacheable true if the flex cache should be used for the display formatter include
     */
    public void setCacheable(boolean cacheable) {

        m_cacheable = Boolean.valueOf(cacheable);
    }

    /**
     * Sets the container type used to select a formatter.
     *
     * @param containerType the container type
     */
    public void setContainerType(String containerType) {

        m_containerType = containerType;
    }

    /** Setter for the "create" attribute of the tag.
     * @param canCreate value of the tag's attribute "create".
     */
    public void setCreate(boolean canCreate) {

        m_canCreate = canCreate;
    }

    /** Setter for the "create" attribute of the tag.
     * @param canCreate value of the tag's attribute "create".
     */
    public void setCreate(String canCreate) {

        m_canCreate = Boolean.valueOf(canCreate).booleanValue();
    }

    /** Setter for the "creationSiteMap" attribute of the tag.
     * @param sitePath value of the "creationSiteMap" attribute of the tag.
     */
    public void setCreationSiteMap(String sitePath) {

        m_creationSiteMap = sitePath;
    }

    /**Setter for the "delete" attribute of the tag.
     * @param canDelete value of the "delete" attribute of the tag.
     */
    public void setDelete(boolean canDelete) {

        m_canDelete = canDelete;
    }

    /**Setter for the "delete" attribute of the tag.
     * @param canDelete value of the "delete" attribute of the tag.
     */
    public void setDelete(String canDelete) {

        m_canDelete = Boolean.valueOf(canDelete).booleanValue();
    }

    /**
     * Sets the items.<p>
     *
     * @param displayFormatters the items to set
     */
    public void setDisplayFormatters(Object displayFormatters) {

        if (displayFormatters instanceof List) {
            for (Object formatterItem : ((List<?>)displayFormatters)) {
                if (formatterItem instanceof CmsJspContentAccessValueWrapper) {
                    addFormatter((CmsJspContentAccessValueWrapper)formatterItem);
                }
            }
        } else if (displayFormatters instanceof CmsJspContentAccessValueWrapper) {
            addFormatter((CmsJspContentAccessValueWrapper)displayFormatters);
        } else if (displayFormatters instanceof String) {
            String[] temp = ((String)displayFormatters).split(CmsXmlDisplayFormatterValue.SEPARATOR);
            if (temp.length == 2) {
                addDisplayFormatter(temp[0], temp[1]);
            }
        }
    }

    /**
     * Sets the editable.<p>
     *
     * @param editable the editable to set
     */
    public void setEditable(boolean editable) {

        m_editable = editable;
    }

    /**
     * Sets the editable.<p>
     *
     * @param editable the editable to set
     */
    public void setEditable(String editable) {

        m_editable = Boolean.valueOf(editable).booleanValue();
    }

    /**
     * Sets the passSettings.<p>
     *
     * @param passSettings the passSettings to set
     */
    public void setPassSettings(boolean passSettings) {

        m_passSettings = passSettings;
    }

    /**
     * Sets the passSettings.<p>
     *
     * @param passSettings the passSettings to set
     */
    public void setPassSettings(String passSettings) {

        m_passSettings = Boolean.valueOf(passSettings).booleanValue();
    }

    /** Setter for the "postCreateHandler" attribute of the tag.
     * @param postCreateHandler fully qualified class name of the {@link I_CmsCollectorPostCreateHandler} to use.
     */
    public void setPostCreateHandler(final String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /**
     * Sets the element settings to be used.<p>
     *
     * @param settings the element settings to be used
     */
    public void setSettings(Map<String, String> settings) {

        m_settings = settings;
    }

    /**
     * Sets the upload folder.
     *
     * @param uploadFolder the upload folder
     */
    public void setUploadFolder(String uploadFolder) {

        m_uploadFolder = uploadFolder;
    }

    /**
     * Sets the value.<p>
     *
     * @param value the value to set
     */
    public void setValue(String value) {

        m_value = value;
    }

    /**
     * Adds a formatter.<p>
     *
     * @param formatterItem the formatter value
     */
    private void addFormatter(CmsJspContentAccessValueWrapper formatterItem) {

        I_CmsXmlContentValue val = formatterItem.getContentValue();
        if (val instanceof CmsXmlDisplayFormatterValue) {
            CmsXmlDisplayFormatterValue value = (CmsXmlDisplayFormatterValue)val;
            String type = value.getDisplayType();
            String formatterId = value.getFormatterId();
            if (formatterId != null) {
                m_displayFormatterIds.put(type, formatterId);
            }
        }
    }

    /**
     * Returns the config for the requested resource, or <code>null</code> if not available.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param isOnline the is online flag
     *
     * @return the formatter configuration bean
     */
    private I_CmsFormatterBean getFormatterForType(CmsObject cms, CmsResource resource, boolean isOnline) {

        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
            cms,
            cms.getRequestContext().getRootUri());
        I_CmsFormatterBean result = null;
        if (m_displayFormatterPaths.containsKey(typeName)) {
            try {
                CmsResource res = cms.readResource(m_displayFormatterPaths.get(typeName));
                result = OpenCms.getADEManager().getCachedFormatters(isOnline).getFormatters().get(
                    res.getStructureId());
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else if (m_displayFormatterIds.containsKey(typeName)) {
            result = config.findFormatter(m_displayFormatterIds.get(typeName));
        } else {
            if (config != null) {
                if (m_containerType != null) {
                    result = config.getActiveFormattersWithContainerType(m_containerType).stream().filter(
                        formatter -> formatter.getResourceTypeNames().contains(typeName)).collect(
                            Collectors.maxBy((a, b) -> Integer.compare(a.getRank(), b.getRank()))).orElse(null);
                }
                if (result == null) {
                    CmsFormatterConfiguration formatters = config.getFormatters(cms, resource);
                    if (formatters != null) {
                        result = formatters.getDisplayFormatter();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks if this tag instance should use the flex cache for including the formatter.
     *
     * @return true if this tag instance should use the flex cache for including the formatter
     */
    private boolean isCacheable() {

        return (m_cacheable == null) || m_cacheable.booleanValue();
    }

    /**
     * Prepares the settings before the call to displayAction().
     *
     * @param config the sitemap configuration
     * @param formatter the display formatter
     *
     * @return the settings to use
     */
    private Map<String, String> prepareSettings(CmsADEConfigData config, I_CmsFormatterBean formatter) {

        Map<String, String> settings = new HashMap<String, String>();
        for (Entry<String, String> entry : m_parameterMap.entrySet()) {
            if (CmsContainerElement.ELEMENT_INSTANCE_ID.equals(entry.getKey())) {
                // remove any instance id to make sure to generate a unique one
                continue;
            }
            String fmtSetting = getSettingKeyForMatchingFormatterPrefix(config, formatter, entry.getKey());
            if (entry.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                settings.put(entry.getKey(), formatter.getId());
            } else if (fmtSetting != null) {
                settings.put(fmtSetting, entry.getValue());
            } else if (!settings.containsKey(entry.getKey())) {
                settings.put(entry.getKey(), entry.getValue());
            }
        }
        return settings;
    }
}
