/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagLabel.java,v $
 * Date   : $Date: 2005/05/12 10:27:40 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the labels stored in the
 * language files of the OpenCms workplace.<p>
 * 
 * Instead of using the XML based workplace tags one should 
 * consider using standard Java resource bundles to provide language independent 
 * implementations.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.11 $
 */
public class CmsJspTagLabel extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagLabel.class);

    /**
     * Internal action method.<p>
     * 
     * @param label the label to look up
     * @param req the current request
     * @return String the value of the selected label
     */
    public static String wpLabelTagAction(String label, ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(
            controller.getCmsObject().getRequestContext().getLocale());
        return messages.key(label);
    }

    /**
     * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
     */
    public int doAfterBody() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            try {

                // Get label string from the body and reset body 
                BodyContent body = this.getBodyContent();
                String label = body.getString();
                body.clearBody();

                // Get the result...
                String result = wpLabelTagAction(label, req);
                this.getPreviousOut().print(result);

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.ERR_TAG_LABEL_0), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

}
