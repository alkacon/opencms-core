/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagLink.java,v $
 * Date   : $Date: 2011/03/23 14:51:34 $
 * Version: $Revision: 1.24 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;

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
 * @version $Revision: 1.24 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagLink extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagLink.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2361021288258405388L;

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
     * @param req the current request
     * 
     * @return the target link adjusted according to the web application path and the OpenCms static export rules
     * 
     * @see org.opencms.staticexport.CmsLinkManager#substituteLinkForUnknownTarget(org.opencms.file.CmsObject, String)
     */
    public static String linkTagAction(String target, ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);

        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            controller.getCmsObject(),
            CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri()));
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @return EVAL_PAGE
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
                String newlink = linkTagAction(link, req);
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
}