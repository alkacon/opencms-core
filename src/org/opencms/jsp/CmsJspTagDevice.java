/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagDevice.java,v $
 * Date   : $Date: 2009/12/15 15:24:39 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * This class provides a <code>&lt;cms:device type="..."&gt;</code>-Tag 
 * with the attribute <code>type</code> to specify the device.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @since 7.9.3
 * 
 * @version 1.0
 */
public class CmsJspTagDevice extends BodyTagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 9175484824140856283L;

    /** Device for output. */
    protected String m_type;

    /**
     * Close the device tag.<p>
     * 
     * @return {@link #EVAL_PAGE}
     */
    @Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }

        return EVAL_PAGE;
    }

    /**
     * Decides on the base of the device selector interface whether the user's device is in
     * the selected device types or not.<p>
     * 
     * If the user's device is in the list of possible devices the content inside the tag is printed out
     * and otherwise the content won't be printed out.<p> 
     * 
     * @return {@link #EVAL_BODY_INCLUDE}<br/>{@link #SKIP_BODY}
     */
    @Override
    public int doStartTag() {

        // get the flex controller
        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());

        // get the device selector
        I_CmsJspDeviceSelector selector = controller.getCmsCache().getDeviceSelector();

        // get the current device from the request
        HttpServletRequest req = controller.getTopRequest();
        String device = (String)req.getAttribute(I_CmsJspDeviceSelector.REQUEST_ATTRIBUTE_DEVICE);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(device)) {
            // device not found in request
            device = selector.getDeviceType(req);
            if (CmsStringUtil.isNotEmpty(device)) {
                // put the detected device into the request
                req.setAttribute(I_CmsJspDeviceSelector.REQUEST_ATTRIBUTE_DEVICE, device);
            }
        }

        // check if the detected device is in the list of given types
        List<String> devices = CmsStringUtil.splitAsList(m_type, ",");
        if (devices.contains(device)) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    @Override
    public void release() {

        super.release();
        m_type = null;
    }

    /**
     * Returns the device type.<p>
     *
     * @return the device type
     */
    public String getType() {

        return m_type != null ? m_type : "";
    }

    /**
     * Sets the type for the device.<p>
     *
     * @param type the device type
     */
    public void setType(String type) {

        if (type != null) {
            m_type = type;
        }
    }
}