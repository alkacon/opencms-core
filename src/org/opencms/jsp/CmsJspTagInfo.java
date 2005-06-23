/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagInfo.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to OpenCms and System related information.<p>
 * 
 * This tag supports the following special "property" values:
 * <ul>
 * <li><code>opencms.version</code> returns the current OpenCms version, e.g. <i>5.0 Kaitain</i>
 * <li><code>opencms.url</code> returns the current request URL, e.g. 
 * <i>http://localhost:8080/opencms/opencms/index.jsp</i>
 * <li><code>opencms.uri</code> returns the current request URI, e.g. 
 * <i>/opencms/opencms/index.jsp</i>
 * <li><code>opencms.webapp</code> returns the name of the OpenCms web application, e.g. 
 * <i>opencms</i>
 * <li><code>opencms.webbasepath</code> returns the name of system path to the OpenCms web 
 * application, e.g. <i>C:\Java\Tomcat\webapps\opencms\</i> 
 * <li><code>opencms.request.uri</code> returns the name of the currently requested URI in 
 * the OpenCms VFS, e.g. <i>/index.jsp</i>
 * <li><code>opencms.request.element.uri</code> returns the name of the currently processed element, 
 * which might be a sub-element like a template part, 
 * in the OpenCms VFS, e.g. <i>/system/modules/org.opencms.welcome/jsptemplates/welcome.jsp</i>
 * <li><code>opencms.request.folder</code> returns the name of the parent folder of the currently
 * requested URI in the OpenCms VFS, e.g. <i>/</i>
 * <li><code>opencms.request.encoding</code> returns the content encoding that has been set
 * for the currently requested resource, e.g. <i>ISO-8859-1</i>
 * </ul>
 * 
 * All other property values that are passes to the tag as routed to a standard 
 * <code>System.getProperty(value)</code> call,
 * so you can also get information about the Java VM environment,
 * using values like <code>java.vm.version</code> or <code>os.name</code>.<p>
 * 
 * If the given property value does not match a key from the special OpenCms values
 * and also not the system values, a (String) message is returned with a formatted 
 * error message.<p>
 *  
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagInfo extends TagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagInfo.class);

    /** Static array with allowed info property values. */
    private static final String[] SYSTEM_PROPERTIES = {"opencms.version", // 0
        "opencms.url", // 1
        "opencms.uri", // 2
        "opencms.webapp", // 3
        "opencms.webbasepath", // 4 
        "opencms.request.uri", // 5
        "opencms.request.element.uri", // 6
        "opencms.request.folder", // 7      
        "opencms.request.encoding", // 8
        "opencms.request.locale" // 9   
    };

    /** Array list of allowed property values for more convenient lookup. */
    private static final List SYSTEM_PROPERTIES_LIST = Arrays.asList(SYSTEM_PROPERTIES);

    // member variables    
    private String m_property;

    /**
     * Returns the selected info property value based on the provided 
     * parameters.<p>
     * 
     * @param property the info property to look up
     * @param req the currents request
     * @return the looked up property value 
     */
    public static String infoTagAction(String property, HttpServletRequest req) {

        if (property == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(Messages.GUI_ERR_INVALID_INFO_PROP_0);
            return Messages.getLocalizedMessage(errMsgContainer, req);
        }
        CmsFlexController controller = CmsFlexController.getController(req);

        String result = null;
        switch (SYSTEM_PROPERTIES_LIST.indexOf(property)) {
            case 0: // opencms.version
                result = OpenCms.getSystemInfo().getVersionName();
                break;
            case 1: // opencms.url
                result = req.getRequestURL().toString();
                break;
            case 2: // opencms.uri
                result = req.getRequestURI();
                break;
            case 3: // opencms.webapp
                result = OpenCms.getSystemInfo().getWebApplicationName();
                break;
            case 4: // opencms.webbasepath
                result = OpenCms.getSystemInfo().getWebApplicationRfsPath();
                break;
            case 5: // opencms.request.uri
                result = controller.getCmsObject().getRequestContext().getUri();
                break;
            case 6: // opencms.request.element.uri
                result = controller.getCurrentRequest().getElementUri();
                break;
            case 7: // opencms.request.folder
                result = CmsResource.getParentFolder(controller.getCmsObject().getRequestContext().getUri());
                break;
            case 8: // opencms.request.encoding
                result = controller.getCmsObject().getRequestContext().getEncoding();
                break;
            case 9: // opencms.request.locale
                result = controller.getCmsObject().getRequestContext().getLocale().toString();
                break;
            default:
                result = System.getProperty(property);
                if (result == null) {
                    CmsMessageContainer errMsgContainer = Messages.get().container(
                        Messages.GUI_ERR_INVALID_INFO_PROP_1,
                        property);
                    return Messages.getLocalizedMessage(errMsgContainer, req);
                }
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String result = infoTagAction(m_property, (HttpServletRequest)req);
                // Return value of selected property
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.ERR_PROCESS_TAG_1, "info"), ex);
                }
                throw new JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the selected info property.
     * 
     * @return the selected info property 
     */
    public String getProperty() {

        return m_property != null ? m_property : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        super.release();
        m_property = null;
    }

    /**
     * Sets the info property name.
     * 
     * @param name the info property name to set
     */
    public void setProperty(String name) {

        if (name != null) {
            m_property = name.toLowerCase();
        }
    }

}
