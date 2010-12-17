/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagLink.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
 * Version: $Revision: 1.7 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.sitemap.I_CmsDetailPageFinder;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implements the <code>&lt;cms:link&gt;[filename]&lt;/cms:link&gt;</code> 
 * tag to add OpenCms managed links to a JSP page, required for link
 * management and the static 
 * export to work properly.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagLink extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagLink.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2361021288258405388L;

    /** The value of the <code>detailview</code> attribute. */
    private String m_detailView;

    /**
     * Returns a link to a file in the OpenCms VFS 
     * that has been adjusted according to the web application path and the 
     * OpenCms static export rules.<p>
     * 
     * Since OpenCms version 7.0.2, you can also use this method in case you are not sure
     * if the link is internal or external, as  
     * {@link CmsLinkManager#substituteLinkForUnknownTarget(org.opencms.file.CmsObject, String)}
     * is used to calculate the link target.<p>
     * 
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     * 
     * @param target the link that should be calculated, can be relative or absolute
     * @param detailView the optional detail view URI
     * @param req the current request
     * 
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     * 
     * @see org.opencms.staticexport.CmsLinkManager#substituteLinkForUnknownTarget(org.opencms.file.CmsObject, String)
     */
    public static String linkTagAction(String target, String detailView, ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);

        // be sure the link is absolute
        String absoluteLink = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());

        // deal with possible anchors
        int pos = absoluteLink.length();
        int anchorPos = absoluteLink.lastIndexOf('#');
        if ((anchorPos != -1) && (anchorPos < pos)) {
            pos = anchorPos;
        }
        // deal with possible parameters
        int paramPos = absoluteLink.lastIndexOf('?');
        if ((paramPos != -1) && (paramPos < pos)) {
            pos = paramPos;
        }
        // get the vfs name
        String uri = absoluteLink.substring(0, pos);
        // get the rest
        String linkInfo = (pos == absoluteLink.length()) ? "" : absoluteLink.substring(pos);

        CmsObject cms = controller.getCmsObject();
        try {
            // check for detail view
            CmsResource res = cms.readResource(uri);
            I_CmsDetailPageFinder finder = OpenCms.getSitemapManager().getDetailPageFinder();
            String detailPage = finder.getDetailPage(cms, res, cms.getRequestContext().getOriginalUri());
            if (detailPage != null) {
                uri = CmsStringUtil.joinPaths(detailPage, cms.getDetailName(res), "/");
            }
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        // append again anchors & parameters
        uri += linkInfo;

        // generate the link
        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, uri);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * 
     * @return EVAL_PAGE
     * 
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            try {
                // Get link-string from the body and reset body 
                String link = getBodyContent().getString();
                getBodyContent().clear();
                // Calculate the link substitution
                String newlink = linkTagAction(link, m_detailView, req);
                // Write the result back to the page                
                getBodyContent().print(newlink);
                getBodyContent().writeOut(pageContext.getOut());

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "link"), ex);
                }
                throw new JspException(ex);
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Returns the set detail view URI.<p>
     * 
     * @return the set detail view URI 
     */
    public String getDetailview() {

        return m_detailView != null ? m_detailView : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_detailView = null;
    }

    /**
     * Sets the detail view URI.<p>
     * 
     * @param detailView the detail view URI to set
     */
    public void setDetailview(String detailView) {

        m_detailView = detailView;
    }
}