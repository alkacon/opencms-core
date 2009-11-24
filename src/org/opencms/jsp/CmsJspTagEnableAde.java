/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEnableAde.java,v $
 * Date   : $Date: 2009/11/24 16:32:40 $
 * Version: $Revision: 1.9 $
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
import org.opencms.xml.containerpage.CmsContainerPageBean;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;enable-ade/&gt;</code> tag.<p>
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 7.6 
 */
public class CmsJspTagEnableAde extends BodyTagSupport {

    /** Messages URI constant. */
    public static final String ADE_MESSAGES_URI = "/system/workplace/editors/ade/cms.messages.jsp";

    /** ADE Server URI constant. */
    public static final String ADE_SERVER_URI = "/system/workplace/editors/ade/server.jsp";

    /** Editor URI constant. */
    public static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** Gallery Server URI constant. */
    public static final String GALLERY_SERVER_URI = "/system/workplace/galleries/gallerySearch.jsp";

    /** Macro name constant. */
    public static final String MACRO_CURRENT_CNTPAGE = "currentContainerPage";

    /** Macro name constant. */
    public static final String MACRO_CURRENT_LOCALE = "currentLocale";

    /** Macro name constant. */
    public static final String MACRO_CURRENT_URI = "currentUri";

    /** Macro name constant. */
    public static final String MACRO_EDITOR_URI = "editorUri";

    /** Macro name constant. */
    public static final String MACRO_MESSAGES_URI = "messagesUri";

    /** Macro name constant. */
    public static final String MACRO_NO_EDIT_REASON = "noEditReason";

    /** Macro name constant. */
    public static final String MACRO_PUBLISH_URI = "publishUri";

    /** Macro name constant. */
    public static final String MACRO_SERVER_URI = "serverUri";

    /** Macro name constant. */
    public static final String MACRO_GALLERY_SERVER_URI = "galleryServerUri";

    /** Macro name constant. */
    public static final String MACRO_SKIN_URI = "skinUri";

    /** Publish Server URI constant. */
    public static final String PUBLISH_SERVER_URI = "/system/workplace/editors/ade/publish-server.jsp";

    /** Default advanced direct edit include file URI. */
    protected static final String INCLUDE_FILE = "/system/workplace/editors/ade/include.txt";

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

        String headerInclude = (String)cache.getCachedObject(CmsJspTagEnableAde.class, INCLUDE_FILE);
        if (headerInclude == null) {
            // the file is not available in the cache
            try {
                CmsFile file = cms.readFile(INCLUDE_FILE);
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
                String editorUri = linkMan.substituteLink(cms, EDITOR_URI);
                resolver.addMacro(MACRO_EDITOR_URI, editorUri);
                String serverUri = linkMan.substituteLink(cms, ADE_SERVER_URI);
                resolver.addMacro(MACRO_SERVER_URI, serverUri);
                String publishUri = linkMan.substituteLink(cms, PUBLISH_SERVER_URI);
                resolver.addMacro(MACRO_PUBLISH_URI, publishUri);
                String galleryServerUri = linkMan.substituteLink(cms, GALLERY_SERVER_URI);
                resolver.addMacro(MACRO_GALLERY_SERVER_URI, galleryServerUri);
                String skinUri = CmsWorkplace.getSkinUri();
                resolver.addMacro(MACRO_SKIN_URI, skinUri);
                resolver.addMacro(MACRO_MESSAGES_URI, linkMan.substituteLink(cms, ADE_MESSAGES_URI));

                headerInclude = resolver.resolveMacros(headerInclude);

                // store this in the cache
                cache.putCachedObject(CmsJspTagEnableAde.class, INCLUDE_FILE, headerInclude);

            } catch (CmsException e) {
                // this should better not happen
                headerInclude = "";
                LOG.error(Messages.get().getBundle().key(Messages.LOG_DIRECT_EDIT_NO_HEADER_1, INCLUDE_FILE), e);
            }
        }

        // these macros are request specific
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        try {
            String currentUri = cms.getRequestContext().getUri();
            CmsResource containerPage = cms.readResource(currentUri);
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
            } else if (req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER) != null) {
                CmsUUID id = new CmsUUID(req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER));
                currentUri = cms.getSitePath(cms.readResource(id));
            }
            String containerPageUri = cms.getSitePath(containerPage);
            resolver.addMacro(MACRO_CURRENT_URI, currentUri);
            resolver.addMacro(MACRO_CURRENT_CNTPAGE, containerPageUri);
            String noEditReason = new CmsResourceUtil(cms, containerPage).getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
                cms));
            resolver.addMacro(MACRO_NO_EDIT_REASON, CmsEncoder.escapeHtml(noEditReason));
        } catch (Exception e) {
            if (!LOG.isDebugEnabled()) {
                LOG.warn(e.getLocalizedMessage());
            }
            LOG.debug(e.getLocalizedMessage(), e);
        }
        resolver.addMacro(MACRO_CURRENT_LOCALE, cms.getRequestContext().getLocale().toString());

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