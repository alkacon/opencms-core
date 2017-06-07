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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to the data of the currently logged in user.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagUser extends TagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4520173761363738542L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagUser.class);

    /** Static array of the possible user properties. */
    private static final String[] USER_PROPERTIES = {
        "name",
        "firstname",
        "lastname",
        "email",
        "street",
        "zip",
        "city",
        "description",
        "group",
        "currentgroup",
        "defaultgroup",
        "otherstuff",
        "institution"};

    /** Array list for fast lookup. */
    private static final List<String> USER_PROPERTIES_LIST = Arrays.asList(USER_PROPERTIES);

    /** The property name. */
    private String m_property;

    /**
     * Internal action method.<p>
     *
     * @param property the selected user property
     * @param req the current request
     * @return String the value of the selected user property
     */
    public static String userTagAction(String property, ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);

        CmsObject cms = controller.getCmsObject();
        CmsUser user = cms.getRequestContext().getCurrentUser();

        if (property == null) {
            property = USER_PROPERTIES[0];
        }

        String result = null;
        switch (USER_PROPERTIES_LIST.indexOf(property)) {
            case 0: // name
                result = user.getName();
                break;
            case 1: // firstname
                result = user.getFirstname();
                break;
            case 2: // lastname
                result = user.getLastname();
                break;
            case 3: // email
                result = user.getEmail();
                break;
            case 4: // street
                result = user.getAddress();
                break;
            case 5: // zip
                result = user.getZipcode();
                break;
            case 6: // city
                result = user.getCity();
                break;
            case 7: // description
                result = user.getDescription(cms.getRequestContext().getLocale());
                break;
            // following 3 attributes are no longer supported
            case 8: // group
            case 9: // currentgroup
            case 10: // defaultgroup
                result = "";
                break;
            case 11: // otherstuff
                Iterator<String> it = user.getAdditionalInfo().keySet().iterator();
                CmsMessageContainer msgContainer = Messages.get().container(Messages.GUI_TAG_USER_ADDITIONALINFO_0);
                result = Messages.getLocalizedMessage(msgContainer, req);
                while (it.hasNext()) {
                    Object o = it.next();
                    result += " " + o + "=" + user.getAdditionalInfo((String)o);
                }
                break;
            case 12: // institution
                result = user.getInstitution();
                break;
            default:
                msgContainer = Messages.get().container(Messages.GUI_ERR_INVALID_USER_PROP_1, property);
                result = Messages.getLocalizedMessage(msgContainer, req);
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws javax.servlet.jsp.JspException {

        javax.servlet.ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String result = userTagAction(m_property, req);
                // Return value of selected property
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "user"), ex);
                }
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the property name.<p>
     *
     * @return String the property name
     */
    public String getProperty() {

        return m_property != null ? m_property : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_property = null;
    }

    /**
     * Sets the property name.<p>
     *
     * @param name the property name
     */
    public void setProperty(String name) {

        if (name != null) {
            m_property = name.toLowerCase();
        }
    }

}
