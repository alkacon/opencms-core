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
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.collectors.I_CmsCollectorPostCreateHandler;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

/** This tag is used to attach an edit provider to a snippet of HTML. */
public class CmsJspTagEdit extends CmsJspScopedVarBodyTagSuport {

    /** Identifier to indicate that the new link should be handled by this tag - not by a {@link org.opencms.file.collectors.I_CmsResourceCollector}. */
    public static final String NEW_LINK_IDENTIFIER = "__edit__";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEdit.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3781368910893187306L;

    /** Flag, indicating if the create option should be displayed. */
    private boolean m_canCreate;

    /** Flag, indicating if the delete option should be displayed. */
    private boolean m_canDelete;

    /** The type of the resource that should be created. */
    private String m_createType;

    /** The tag attribute's value, specifying the path to the (sub)sitemap where new content should be created. */
    private String m_creationSiteMap;

    /** Flag, indicating if during rendering the "startDirectEdit" part has been rendered, but not the "endDirectEdit" part. */
    private boolean m_isEditOpen;

    /** The fully qualified class name of the post create handler to use. */
    private String m_postCreateHandler;

    /** UUID of the content to edit. */
    private String m_uuid;

    /** Creates a new resource.
     * @param cmsObject The CmsObject of the current request context.
     * @param newLink A string, specifying where which new content should be created.
     * @param locale The locale for which the
     * @param sitePath site path of the currently edited content.
     * @param modelFileName not used.
     * @param mode optional creation mode
     * @param postCreateHandler optional class name of an {@link I_CmsCollectorPostCreateHandler} which is invoked after the content has been created.
     * @return The site-path of the newly created resource.
     */
    public static String createResource(
        final CmsObject cmsObject,
        final String newLink,
        final Locale locale,
        final String sitePath,
        final String modelFileName,
        final String mode,
        final String postCreateHandler) {

        String[] newLinkParts = newLink.split("\\|");
        String rootPath = newLinkParts[1];
        String typeName = newLinkParts[2];
        CmsFile modelFile = null;
        if (StringUtils.equalsIgnoreCase(mode, CmsEditorConstants.MODE_COPY)) {
            try {
                modelFile = cmsObject.readFile(sitePath);
            } catch (CmsException e) {
                // TODO: localize.
                LOG.warn(
                    "The resource at path" + sitePath + "could not be read. Thus it can not be used as model file.",
                    e);
            }
        }
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cmsObject, rootPath);
        CmsResourceTypeConfig typeConfig = adeConfig.getResourceType(typeName);
        CmsResource newElement = null;
        try {
            newElement = typeConfig.createNewElement(cmsObject, modelFile, rootPath);
            I_CmsCollectorPostCreateHandler handler = A_CmsResourceCollector.getPostCreateHandler(postCreateHandler);
            handler.onCreate(cmsObject, cmsObject.readFile(newElement), modelFile != null);
        } catch (CmsException e) {
            // TODO: Localize and improve error message.
            LOG.error("Could not create resource.", e);
        }
        return newElement == null ? null : cmsObject.getSitePath(newElement);
    }

    /**
     * Inserts the closing direct edit tag.<p>
     *
     * @param pageContext the page context
     */
    public static void insertDirectEditEnd(PageContext pageContext) {

        try {
            CmsJspTagEditable.endDirectEdit(pageContext);
        } catch (JspException e) {
            LOG.error("Could not print closing direct edit tag.", e);
        }
    }

    /**
     * Inserts the opening direct edit tag.<p>
     *
     * @param cms the CMS context
     * @param pageContext the page context
     * @param resource the resource to edit
     * @param canCreate if resource creation is allowed
     * @param canDelete if resource deletion is allowed
     * @param createType the resource type to create, default to the type of the edited resource
     * @param creationSitemap the sitemap context to create the resource in, default to the current requested URI
     * @param postCreateHandler the post create handler if required
     *
     * @return <code>true</code> if an opening direct edit tag was inserted
     */
    public static boolean insertDirectEditStart(
        CmsObject cms,
        PageContext pageContext,
        CmsResource resource,
        boolean canCreate,
        boolean canDelete,
        String createType,
        String creationSitemap,
        String postCreateHandler) {

        boolean result = false;
        CmsDirectEditParams editParams = null;
        if (resource != null) {

            String newLink = null;
            // reconstruct create type from the edit-resource if necessary
            if (canCreate) {
                I_CmsResourceType resType = getResourceType(resource, createType);
                if (resType != null) {
                    newLink = getNewLink(cms, resType, creationSitemap);
                }
            }
            CmsDirectEditButtonSelection buttons = null;
            if (canDelete) {
                if (newLink != null) {
                    buttons = CmsDirectEditButtonSelection.EDIT_DELETE_NEW;
                } else {
                    buttons = CmsDirectEditButtonSelection.EDIT_DELETE;
                }
            } else if (newLink != null) {
                buttons = CmsDirectEditButtonSelection.EDIT_NEW;
            } else {
                buttons = CmsDirectEditButtonSelection.EDIT;
            }
            editParams = new CmsDirectEditParams(cms.getSitePath(resource), buttons, null, newLink);
        } else if (canCreate) {
            I_CmsResourceType resType = getResourceType(null, createType);
            if (resType != null) {
                editParams = new CmsDirectEditParams(
                    cms.getRequestContext().getFolderUri(),
                    CmsDirectEditButtonSelection.NEW,
                    null,
                    getNewLink(cms, resType, creationSitemap));
            }
        }

        if (editParams != null) {
            editParams.setPostCreateHandler(postCreateHandler);
            try {
                CmsJspTagEditable.startDirectEdit(pageContext, editParams);
                result = true;
            } catch (JspException e) {
                // TODO: Localize and improve error message.
                LOG.error("Could not create direct edit start.", e);
            }
        }
        return result;
    }

    /**
     * Returns the context root path.<p>
     *
     * @param cms the CMS context
     * @param creationSitemap the creation sitemap parameter
     *
     * @return the context root path
     */
    private static String getContextRootPath(CmsObject cms, String creationSitemap) {

        String path = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(creationSitemap)) {
            try {
                path = cms.readFolder(creationSitemap).getRootPath();
            } catch (CmsException e) {

                // TODO: Localize log output.
                LOG.warn("The provided creation sitemap " + creationSitemap + " is not a VFS folder.", e);
            }
        }
        if (path == null) {
            path = cms.addSiteRoot(cms.getRequestContext().getFolderUri());
        }

        return path;
    }

    /**
     * Creates the String specifying where which type of resource has to be created.<p>
     *
     * @param cms the CMS context
     * @param resType the resource type to create
     * @param creationSitemap the creation sitemap parameter
     *
     * @return The String identifying which type of resource has to be created where.<p>
     *
     * @see #createResource(CmsObject, String, Locale, String, String, String, String)
     */
    private static String getNewLink(CmsObject cms, I_CmsResourceType resType, String creationSitemap) {

        String contextPath = getContextRootPath(cms, creationSitemap);
        StringBuffer newLink = new StringBuffer(NEW_LINK_IDENTIFIER);
        newLink.append('|');
        newLink.append(contextPath);
        newLink.append('|');
        newLink.append(resType.getTypeName());

        return newLink.toString();
    }

    /**
     * Returns the resource type to create, or <code>null</code> if not available.<p>
     *
     * @param resource the edit resource
     * @param createType the create type parameter
     *
     * @return the resource type
     */
    private static I_CmsResourceType getResourceType(CmsResource resource, String createType) {

        I_CmsResourceType resType = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(createType)) {
            try {
                resType = OpenCms.getResourceManager().getResourceType(createType);
            } catch (CmsLoaderException e) {
                LOG.error("Could not read resource type '" + createType + "' for resource creation.", e);
            }
        } else if (resource != null) {
            resType = OpenCms.getResourceManager().getResourceType(resource);
        }
        return resType;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        if (m_isEditOpen) {
            CmsJspTagEditable.endDirectEdit(pageContext);
        }
        release();
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws CmsIllegalArgumentException {

        CmsObject cms = getCmsObject();
        m_isEditOpen = insertDirectEditStart(
            cms,
            pageContext,
            getResourceToEdit(cms),
            m_canCreate || (null != m_createType),
            m_canDelete,
            m_createType,
            m_creationSiteMap,
            m_postCreateHandler);
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see org.opencms.jsp.CmsJspScopedVarBodyTagSuport#release()
     */
    @Override
    public void release() {

        m_canCreate = false;
        m_canDelete = false;
        m_creationSiteMap = null;
        m_createType = null;
        m_isEditOpen = false;
        m_uuid = null;
        super.release();
    }

    /** Setter for the "create" attribute of the tag.
     * @param canCreate value of the tag's attribute "create".
     */
    public void setCreate(final Boolean canCreate) {

        m_canCreate = canCreate == null ? false : canCreate.booleanValue();
    }

    /** Setter for the "createType" attribute of the tag.<p>
     *
     * @param typeName value of the "createType" attribute of the tag.
     */
    public void setCreateType(final String typeName) {

        m_createType = typeName;
    }

    /** Setter for the "creationSiteMap" attribute of the tag.
     *
     * @param sitePath value of the "creationSiteMap" attribute of the tag.
     */
    public void setCreationSiteMap(final String sitePath) {

        m_creationSiteMap = sitePath;
    }

    /**Setter for the "delete" attribute of the tag.
     * @param canDelete value of the "delete" attribute of the tag.
     */
    public void setDelete(final Boolean canDelete) {

        m_canDelete = canDelete == null ? false : canDelete.booleanValue();
    }

    /** Setter for the "postCreateHandler" attribute of the tag.
     * @param postCreateHandler fully qualified class name of the {@link I_CmsCollectorPostCreateHandler} to use.
     */
    public void setPostCreateHandler(final String postCreateHandler) {

        m_postCreateHandler = postCreateHandler;
    }

    /** Setter for the uuid attribute of the tag, providing the uuid of content that should be edited.
     * If no valid uuid of an existing resource is given, it is assumed the tag is only used for creating new contents.
     * @param uuid the uuid of the content that should be edited.
     */
    public void setUuid(final String uuid) {

        m_uuid = uuid;
    }

    /**
     * Returns the current CMS context.<p>
     *
     * @return the CMS context
     */
    private CmsObject getCmsObject() {

        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
        return controller.getCmsObject();

    }

    /**
     * Returns the resource to edit according to the uuid provided via the tag's attribute "uuid".<p>
     *
     * @param cms the CMS context
     *
     * @return the resource
     */
    private CmsResource getResourceToEdit(CmsObject cms) {

        CmsResource resource = null;
        if (m_uuid != null) {
            try {
                CmsUUID uuid = new CmsUUID(m_uuid);
                resource = cms.readResource(uuid, CmsResourceFilter.ignoreExpirationOffline(cms));

            } catch (NumberFormatException | CmsException e) {
                // TODO: Localize debug message.
                LOG.warn("UUID was not valid or there is no resource with the given UUID.", e);
            }
        }
        return resource;
    }
}
