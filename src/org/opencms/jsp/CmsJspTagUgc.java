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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.ugc.CmsUgcSession;
import org.opencms.ugc.CmsUgcSessionFactory;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Jsp tag to initialize an editing session for user generated content.<p>
 */
public class CmsJspTagUgc extends TagSupport {

    /** The serial version id. */
    private static final long serialVersionUID = 7290192201848437667L;

    /** The default page context attribute name for the form error message. */
    public static final String DEFAULT_ERROR_MESSAGE_ATTR = "formError";

    /** The default page context attribute name for the form session id. */
    public static final String DEFAULT_SESSION_ID_ATTR = "formSessionId";

    /** The site path to the edit configuration file. */
    private String m_configPath;

    /** The structure id of the edit resource. */
    private String m_editId;

    /** The file name of the edit resource. */
    private String m_editName;

    /** The page context attribute name for the form error message. */
    private String m_error;

    /** The page context attribute name for the form session id. */
    private String m_var;

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        // reset all tag attribute values
        m_configPath = null;
        m_editId = null;
        m_editName = null;
        m_error = null;
        m_var = null;
        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        CmsFlexController controller = CmsFlexController.getController(req);
        if (controller != null) {
            CmsObject cms = controller.getCmsObject();
            try {
                CmsUgcSession ugcsession = null;
                if ((m_editName == null) && (m_editId != null)) {
                    CmsResource res = cms.readResource(new CmsUUID(m_editId));
                    m_editName = res.getName();
                }
                if (m_editName != null) {
                    ugcsession = CmsUgcSessionFactory.getInstance().createSessionForFile(
                        cms,
                        req,
                        m_configPath,
                        m_editName);
                } else {
                    ugcsession = CmsUgcSessionFactory.getInstance().createSession(cms, req, m_configPath);
                    ugcsession.createXmlContent();
                }
                pageContext.setAttribute(
                    m_var == null ? DEFAULT_SESSION_ID_ATTR : m_var,
                    ugcsession.getId().toString());
            } catch (Exception e) {
                pageContext.setAttribute(
                    m_error == null ? DEFAULT_ERROR_MESSAGE_ATTR : m_error,
                    e.getLocalizedMessage());
            }
        }
        return super.doStartTag();
    }

    /**
     * Returns the site path to the edit configuration file.<p>
     *
     * @return site path to the edit configuration file
     */
    public String getConfigPath() {

        return m_configPath;
    }

    /**
     * Returns the structure id of the edit resource.<p>
     *
     * @return structure id of the edit resource
     */
    public String getEditId() {

        return m_editId;
    }

    /**
     * Returns the file name of the edit resource.<p>
     *
     * @return the file name of the edit resource
     */
    public String getEditName() {

        return m_editName;
    }

    /**
     * Returns the page context attribute name for the form error message.<p>
     *
     * @return the page context attribute name for the form error message
     */
    public String getError() {

        return m_error;
    }

    /**
     * Returns the page context attribute name for the form session id.<p>
     *
     * @return the page context attribute name for the form session id
     */
    public String getVar() {

        return m_var;
    }

    /**
     * Sets the site path to the edit configuration file.<p>
     *
     * @param configPath the site path to the edit configuration file
     */
    public void setConfigPath(String configPath) {

        m_configPath = CmsStringUtil.isEmptyOrWhitespaceOnly(configPath) ? null : configPath;
    }

    /**
     * Sets the structure id of the edit resource.<p>
     *
     * @param editId the structure id of the edit resource
     */
    public void setEditId(String editId) {

        m_editId = CmsStringUtil.isEmptyOrWhitespaceOnly(editId) ? null : editId;
    }

    /**
     * Sets the file name of the edit resource.<p>
     *
     * @param editName the file name of the edit resource
     */
    public void setEditName(String editName) {

        m_editName = CmsStringUtil.isEmptyOrWhitespaceOnly(editName) ? null : editName;
    }

    /**
     * Sets the page context attribute name for the form error message.<p>
     *
     * @param error the page context attribute name for the form error message
     */
    public void setError(String error) {

        m_error = CmsStringUtil.isEmptyOrWhitespaceOnly(error) ? null : error;
    }

    /**
     * Sets the page context attribute name for the form session id.<p>
     *
     * @param var page context attribute name for the form session id
     */
    public void setVar(String var) {

        m_var = CmsStringUtil.isEmptyOrWhitespaceOnly(var) ? null : var;
    }

}
