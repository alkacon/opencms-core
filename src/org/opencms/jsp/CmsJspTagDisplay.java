/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspContentAccessValueWrapper;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * The 'display' tag can be used to display a single resource using a formatter. It also allows to activate direct editing.<p>
 */
public class CmsJspTagDisplay extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** The serial version id. */
    private static final long serialVersionUID = 2285680951218629093L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagDisplay.class);

    /** The editable flag. */
    private boolean m_editable;

    /** The settings parameter map. */
    private Map<String, String> m_parameterMap;

    /** The pass settings flag. */
    private boolean m_passSettings;

    /** The display formatter paths. */
    private Map<String, String> m_displayFormatterPaths;

    /** The display formatter ids. */
    private Map<String, CmsUUID> m_displayFormatterIds;

    /** The site path to the resource to display. */
    private String m_value;

    /** The type of the resource that should be created. */
    private String m_createType;

    /** The tag attribute's value, specifying the path to the (sub)sitemap where new content should be created. */
    private String m_creationSiteMap;

    /** The fully qualified class name of the post create handler to use. */
    private String m_postCreateHandler;

    /** Flag, indicating if the delete option should be displayed. */
    private boolean m_canDelete;

    /** Flag, indicating if the create option should be displayed. */
    private boolean m_canCreate;

    /** The parent element to this container. */
    private CmsContainerElementBean m_parentElement;

    /**
     * Constructor.<p>
     */
    public CmsJspTagDisplay() {
        m_parameterMap = new HashMap<String, String>();
        m_displayFormatterPaths = new HashMap<String, String>();
        m_displayFormatterIds = new HashMap<String, CmsUUID>();
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
    public int doEndTag() {

        ServletRequest request = pageContext.getRequest();
        ServletResponse response = pageContext.getResponse();

        if (CmsFlexController.isCmsRequest(request)) {
            // this will always be true if the page is called through OpenCms
            CmsObject cms = CmsFlexController.getCmsObject(request);
            Locale locale = cms.getRequestContext().getLocale();
            boolean isOnline = cms.getRequestContext().getCurrentProject().isOnlineProject();
            m_parentElement = CmsJspStandardContextBean.getInstance(request).getElement();

            if (m_value != null) {
                try {
                    CmsResource res = cms.readResource(m_value);
                    I_CmsFormatterBean formatter = getFormatterForType(cms, res, isOnline);
                    if (formatter != null) {
                        CmsContainerElementBean element = new CmsContainerElementBean(
                            res.getStructureId(),
                            formatter.getJspStructureId(),
                            m_parameterMap,
                            false);
                        element.initResource(cms);
                        element.initSettings(cms, formatter);
                        boolean openedEditable = false;
                        if (Boolean.valueOf(m_editable).booleanValue()) {
                            openedEditable = CmsJspTagEdit.insertDirectEditStart(
                                cms,
                                pageContext,
                                res,
                                m_canCreate,
                                m_canDelete,
                                m_createType,
                                m_creationSiteMap,
                                m_postCreateHandler);
                        }
                        CmsJspStandardContextBean.getInstance(request).setElement(element);
                        try {
                            CmsJspTagInclude.includeTagAction(
                                pageContext,
                                cms.getRequestContext().removeSiteRoot(formatter.getJspRootPath()),
                                null,
                                locale,
                                false,
                                isOnline,
                                null,
                                CmsRequestUtil.getAtrributeMap(request),
                                request,
                                response);
                        } catch (JspException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                        if (openedEditable) {
                            CmsJspTagEdit.insertDirectEditEnd(pageContext);
                        }
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            CmsJspStandardContextBean.getInstance(request).setElement(m_parentElement);
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
        m_passSettings = false;
        m_editable = false;
        m_value = null;
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

    /** Setter for the "createType" attribute of the tag.<p>
     *
     * @param typeName value of the "createType" attribute of the tag.
     */
    public void setCreateType(String typeName) {

        m_createType = typeName;
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
    public void setPassSettings(Boolean passSettings) {

        m_passSettings = passSettings.booleanValue();
    }

    /** Setter for the "postCreateHandler" attribute of the tag.
     * @param postCreateHandler fully qualified class name of the {@link I_CmsCollectorPostCreateHandler} to use.
     */
    public void setPostCreateHandler(final String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
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

        I_CmsXmlContentValue val = formatterItem.obtainContentValue();
        if (val instanceof CmsXmlDisplayFormatterValue) {
            CmsXmlDisplayFormatterValue value = (CmsXmlDisplayFormatterValue)val;
            String type = value.getDisplayType();
            CmsUUID formatterId = value.getFormatterId();
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
        I_CmsFormatterBean result = null;
        if (m_displayFormatterPaths.containsKey(typeName)) {
            try {
                CmsResource res = cms.readResource(m_displayFormatterPaths.get(typeName));
                result = OpenCms.getADEManager().getCachedFormatters(isOnline).getFormatters().get(
                    res.getStructureId());
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                cms,
                cms.addSiteRoot(cms.getRequestContext().getFolderUri()));
            if (config != null) {
                CmsFormatterConfiguration formatters = config.getFormatters(cms, resource);
                if (formatters != null) {
                    result = formatters.getDisplayFormatter();
                }
            }
        }
        return result;
    }
}
