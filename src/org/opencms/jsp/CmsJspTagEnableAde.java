/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEnableAde.java,v $
 * Date   : $Date: 2010/02/02 10:06:23 $
 * Version: $Revision: 1.19 $
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

package org.opencms.jsp;

import org.opencms.cache.CmsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.Messages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.galleries.CmsGallerySearchServer;
import org.opencms.xml.sitemap.CmsSiteEntryBean;
import org.opencms.xml.sitemap.CmsXmlSitemap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;enable-ade/&gt;</code> tag.<p>
 * 
 * @version $Revision: 1.19 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagEnableAde extends BodyTagSupport {

    /** URI constants. */
    protected enum Uri {

        /** Messages URI constant. */
        ADE_MESSAGES("/system/workplace/editors/ade/cms.messages.jsp"),

        /** ADE Server URI constant. */
        ADE_SERVER("/system/workplace/editors/ade/server.jsp"),

        /** Editor URI constant. */
        EDITOR("/system/workplace/editors/editor.jsp"),

        /** Publish Server URI constant. */
        PUBLISH_SERVER("/system/workplace/editors/ade/publish-server.jsp"),

        /** Default advanced direct edit include file URI. */
        INCLUDE_FILE("/system/workplace/editors/ade/include.txt");

        /** The uri. */
        private String m_uri;

        /**
         * Constructor.<p>
         * 
         * @param uri the uri
         */
        private Uri(String uri) {

            m_uri = uri;
        }

        /**
         * Returns the uri.<p>
         * 
         * @return the uri
         */
        public String getUri() {

            return m_uri;
        }
    }

    /** Macro name constants. */
    protected enum Macro {

        /** Macro name constant. */
        currentContainerPage,

        /** Macro name constant. */
        currentLocale,

        /** Macro name constant. */
        currentUri,

        /** Macro name constant. */
        editorUri,

        /** Macro name constant. */
        galleryAdditionalJavascript,

        /** Macro name constant. */
        galleryServerPath,

        /** Macro name constant. */
        galleryServerUri,

        /** Macro name constant. */
        messagesUri,

        /** Macro name constant. */
        noEditReason,

        /** Macro name constant. */
        publishUri,

        /** Macro name constant. */
        serverUri,

        /** Macro name constant. */
        sitemapUri,

        /** Macro name constant. */
        skinUri;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEnableAde.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 8447599916548975733L;

    /**
     * enable-ade action method.<p>
     * 
     * @param context the current JSP page context
     * 
     * @throws JspException in case something goes wrong
     */
    public static void enableAdeTagAction(PageContext context) throws JspException {

        ServletRequest req = context.getRequest();
        if (CmsHistoryResourceHandler.isHistoryRequest(req)) {
            // don't display advanced direct edit buttons on an historical resource
            return;
        }

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            // advanced direct edit is never enabled in the online project
            return;
        }

        if (CmsResource.isTemporaryFileName(cms.getRequestContext().getUri())) {
            // don't display advanced direct edit buttons if a temporary file is displayed
            return;
        }

        // insert ade header HTML
        String code = getAdeIncludes(cms, req);
        try {
            context.getOut().print(code);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    /**
     * Returns the ade include HTML to insert in the page beginning.<p>
     * 
     * @param cms the current cms context
     * @param req the current request
     *  
     * @return the ade include HTML to insert in the page beginning
     */
    public static String getAdeIncludes(CmsObject cms, ServletRequest req) {

        // check if the selected include file is available in the cache
        CmsMemoryObjectCache cache = CmsMemoryObjectCache.getInstance();
        CmsLinkManager linkMan = OpenCms.getLinkManager();

        String headerInclude = (String)cache.getCachedObject(CmsJspTagEnableAde.class, Uri.INCLUDE_FILE.getUri());
        if (headerInclude == null) {
            // the file is not available in the cache
            try {
                CmsFile file = cms.readFile(Uri.INCLUDE_FILE.getUri());
                // get the encoding for the resource
                CmsProperty property = cms.readPropertyObject(
                    file,
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    true);
                String encoding = property.getValue(OpenCms.getSystemInfo().getDefaultEncoding());
                // create a String with the right encoding
                headerInclude = CmsEncoder.createString(file.getContents(), encoding);

                // resolve macros in include header
                CmsMacroResolver resolver = CmsMacroResolver.newInstance();
                resolver.setKeepEmptyMacros(true); // be sure request macros stay there
                String editorUri = linkMan.substituteLink(cms, Uri.EDITOR.getUri());
                resolver.addMacro(Macro.editorUri.name(), editorUri);
                String serverUri = linkMan.substituteLink(cms, Uri.ADE_SERVER.getUri());
                resolver.addMacro(Macro.serverUri.name(), serverUri);
                String publishUri = linkMan.substituteLink(cms, Uri.PUBLISH_SERVER.getUri());
                resolver.addMacro(Macro.publishUri.name(), publishUri);
                String galleryServerUri = linkMan.substituteLink(cms, CmsGallerySearchServer.ADVANCED_GALLERY_PATH);
                resolver.addMacro(Macro.galleryServerUri.name(), galleryServerUri);
                resolver.addMacro(Macro.galleryServerPath.name(), CmsGallerySearchServer.ADVANCED_GALLERY_PATH);

                String skinUri = CmsWorkplace.getSkinUri();
                resolver.addMacro(Macro.skinUri.name(), skinUri);
                resolver.addMacro(Macro.messagesUri.name(), linkMan.substituteLink(cms, Uri.ADE_MESSAGES.getUri()));

                headerInclude = resolver.resolveMacros(headerInclude);

                // store this in the cache
                cache.putCachedObject(CmsJspTagEnableAde.class, Uri.INCLUDE_FILE.getUri(), headerInclude);

            } catch (CmsException e) {
                // this should better not happen
                headerInclude = "";
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_DIRECT_EDIT_NO_HEADER_1,
                    Uri.INCLUDE_FILE.getUri()), e);
            }
        }

        // these macros are request specific
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        try {
            String currentUri = cms.getRequestContext().getUri();

            // get searchable types for gallery and lookup additional java-script for handling inside the advanced galleries
            Iterator<CmsResource> resIt = OpenCms.getADEManager().getSearchableResourceTypes(cms, currentUri, req).iterator();
            List<I_CmsResourceType> searchableTypes = new ArrayList<I_CmsResourceType>();
            while (resIt.hasNext()) {
                CmsResource resource = resIt.next();
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
                searchableTypes.add(type);
            }
            resolver.addMacro(
                Macro.galleryAdditionalJavascript.name(),
                CmsGallerySearchServer.getAdditionalJavascriptForTypes(searchableTypes));

            boolean isDetailPage = false;
            CmsResource containerPage = cms.readResource(currentUri);
            CmsXmlSitemap sitemap = null;
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(containerPage)) {
                // container page is used as template
                String cntPagePath = cms.readPropertyObject(
                    containerPage,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    true).getValue("");
                try {
                    containerPage = cms.readResource(cntPagePath);
                } catch (CmsException e) {
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                CmsSiteEntryBean sitemapInfo = OpenCms.getSitemapManager().getRuntimeInfo(req);
                if (sitemapInfo != null) {
                    // detail page
                    CmsUUID id = sitemapInfo.getContentId();
                    if (id != null) {
                        currentUri = cms.getSitePath(cms.readResource(id));
                        isDetailPage = true;
                    }
                    // sitemap uri
                    sitemap = OpenCms.getSitemapManager().getSitemapForUri(cms, sitemapInfo.getSitePath(cms));
                }
            }

            String sitemapUri = (sitemap == null) ? "" : linkMan.substituteLink(cms, sitemap.getFile());
            resolver.addMacro(Macro.sitemapUri.name(), sitemapUri);
            String containerPageUri = cms.getSitePath(containerPage);
            resolver.addMacro(Macro.currentUri.name(), currentUri);
            resolver.addMacro(Macro.currentContainerPage.name(), containerPageUri);
            String noEditReason = null;
            Locale workplaceLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            if (isDetailPage) {
                noEditReason = Messages.get().getBundle(workplaceLocale).key(Messages.GUI_NO_EDIT_REASON_DETAIL_PAGE_0);
            } else {
                noEditReason = new CmsResourceUtil(cms, containerPage).getNoEditReason(workplaceLocale);
            }
            resolver.addMacro(Macro.noEditReason.name(), noEditReason);
        } catch (Exception e) {
            if (!LOG.isDebugEnabled()) {
                LOG.warn(e.getLocalizedMessage());
            }
            LOG.debug(e.getLocalizedMessage(), e);
        }
        resolver.addMacro(Macro.currentLocale.name(), cms.getRequestContext().getLocale().toString());

        headerInclude = resolver.resolveMacros(headerInclude);

        return headerInclude;
    }

    /**
     * Close the direct edit tag, also prints the direct edit HTML to the current page.<p>
     * 
     * @return {@link #EVAL_PAGE}
     * 
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doEndTag() throws JspException {

        // only execute action for the first "ade" tag on the page (include file)
        enableAdeTagAction(pageContext);

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }

        return EVAL_PAGE;
    }

    /**
     * Opens the direct edit tag, if manual mode is set then the next 
     * start HTML for the direct edit buttons is printed to the page.<p>
     * 
     * @return {@link #EVAL_BODY_INCLUDE}
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    @Override
    public void release() {

        super.release();
    }
}