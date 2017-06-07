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

import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.I_CmsJspDeviceSelector;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * This class provides a <code>&lt;cms:device type="..."&gt;</code>-Tag
 * with the attribute <code>type</code> to specify the device.<p>
 *
 * @since 8.0.0
 */
public class CmsJspTagDevice extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagDevice.class);

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
        I_CmsJspDeviceSelector selector = OpenCms.getSystemInfo().getDeviceSelector();

        List<String> supportedDevices = selector.getDeviceTypes();
        List<String> selectedDevices = CmsStringUtil.splitAsList(m_type, ",", true);

        // check if the selected device is in the list of supported devices
        for (String selectedDevice : selectedDevices) {
            if (supportedDevices.contains(selectedDevice)) {
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
                if (selectedDevices.contains(device)) {
                    return EVAL_BODY_INCLUDE;
                } else {
                    return SKIP_BODY;
                }
            } else {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_WRONG_DEVICE_TYPE_2,
                        selectedDevice,
                        controller.getCurrentRequest().getElementUri()));
            }
        }
        return SKIP_BODY;
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
     * Releases any resources we may have (or inherit).<p>
     */
    @Override
    public void release() {

        super.release();
        m_type = null;
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