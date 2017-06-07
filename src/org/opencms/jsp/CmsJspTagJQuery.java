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
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;cms:jquery/&gt;</code> tag.<p>
 *
 * Since OpenCms version 7.0.5, there is a new core module providing JQuery plus some additional plugins.
 * This tag will include the JQuery javascript library depending on the current project. If the current
 * Project is offline the unpacked version is used, if online the packed version will be used.
 *
 * @since 7.0.5
 */
public class CmsJspTagJQuery extends BodyTagSupport {

    /** File extension constant. */
    private static final String EXTENSION_CSS = ".css";

    /** File extension constant. */
    private static final String EXTENSION_JS = ".js";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagJQuery.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 3257908962507552558L;

    /** VFS path constant. */
    private static final String VFS_PATH_CSS = "jquery/css/";

    /** VFS path constant. */
    private static final String VFS_PATH_JQUERY = "jquery/";

    /** VFS path constant. */
    private static final String VFS_PATH_LOAD_JS = "jquery/load.js";

    /** VFS path constant. */
    private static final String VFS_PATH_PACKED = "packed/";

    /** VFS path constant. */
    private static final String VFS_PATH_UNPACKED = "unpacked/";

    /** The optional css file to include. */
    protected String m_css;

    /** If the inclusion should be dynamic with js or not. */
    protected String m_dynamic;

    /** The javascript file to include. */
    protected String m_js;

    /**
     * Opens the direct edit tag, if manual mode is set then the next
     * start HTML for the direct edit buttons is printed to the page.<p>
     *
     * @return {@link #EVAL_BODY_INCLUDE}
     *
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms
        if (!CmsFlexController.isCmsRequest(req)) {
            return SKIP_BODY;
        }
        if (getJs() == null) {
            if (isDynamic()) {
                // in case we want to include the needed js functions
                try {
                    pageContext.getOut().print("<script type='text/javascript' src='"
                        + CmsWorkplace.getSkinUri()
                        + VFS_PATH_LOAD_JS
                        + "' ></script>");
                } catch (Exception ex) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "jquery"), ex);
                    }
                    throw new JspException(ex);
                }
            }
            return SKIP_BODY;
        }

        // get the server prefix
        CmsObject cms = CmsFlexController.getCmsObject(req);

        // first handle js file
        String path = VFS_PATH_JQUERY;
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // online
            path += VFS_PATH_PACKED;
        } else {
            // offline
            path += VFS_PATH_UNPACKED;
        }
        String file = path + getJs() + EXTENSION_JS;
        try {
            cms.readResource(CmsWorkplace.VFS_PATH_RESOURCES + file);
            if (isDynamic()) {
                pageContext.getOut().print("<script type='text/javascript'>load_script('"
                    + CmsWorkplace.getSkinUri()
                    + file
                    + "', 'js');</script>");
            } else {
                pageContext.getOut().print(
                    "<script type='text/javascript' src='" + CmsWorkplace.getSkinUri() + file + "' ></script>");
            }
        } catch (Exception ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "jquery"), ex);
            }
            throw new JspException(ex);
        }
        if (getCss() == null) {
            return SKIP_BODY;
        }

        // now handle css file
        path = VFS_PATH_CSS;
        file = path + getCss() + EXTENSION_CSS;
        try {
            cms.readResource(CmsWorkplace.VFS_PATH_RESOURCES + file);
            pageContext.getOut().println();
            if (isDynamic()) {
                pageContext.getOut().print("<script type='text/javascript'>load_script('"
                    + CmsWorkplace.getSkinUri()
                    + file
                    + "', 'css');</script>");
            } else {
                pageContext.getOut().print(
                    "<link href='" + CmsWorkplace.getSkinUri() + file + "' rel='stylesheet' type='text/css' >");
            }
        } catch (Exception ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "jquery"), ex);
            }
            throw new JspException(ex);
        }
        return SKIP_BODY;
    }

    /**
     * Returns the optional css file to include.<p>
     *
     * @return the optional css file to include
     */
    public String getCss() {

        return m_css;
    }

    /**
     * Returns the dynamic flag.<p>
     *
     * @return the dynamic flag
     */
    public String getDynamic() {

        return m_dynamic;
    }

    /**
     * Returns the javascript file to include.<p>
     *
     * @return the javascript file
     */
    public String getJs() {

        return m_js;
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    @Override
    public void release() {

        super.release();
        m_js = null;
        m_css = null;
    }

    /**
     * Sets the optional css file to include.<p>
     *
     * @param css the css file to set
     */
    public void setCss(String css) {

        m_css = css;
    }

    /**
     * Sets the dynamic flag.<p>
     *
     * @param dynamic the dynamic flag to set
     */
    public void setDynamic(String dynamic) {

        m_dynamic = dynamic;
    }

    /**
     * Sets the javascript file to include.<p>
     *
     * @param js the javascript file to set
     */
    public void setJs(String js) {

        if (js != null) {
            m_js = js;
        }
    }

    /**
     * Checks if the inclusion is dynamic or not.<p>
     *
     * @return <code>true</code> if the inclusion is dynamic
     */
    private boolean isDynamic() {

        return Boolean.valueOf(getDynamic()).booleanValue();
    }
}