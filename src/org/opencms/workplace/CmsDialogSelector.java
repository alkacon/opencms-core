/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsDialogSelector.java,v $
 * Date   : $Date: 2005/06/22 15:33:02 $
 * Version: $Revision: 1.16 $
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

package org.opencms.workplace;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import org.apache.commons.logging.Log;

/**
 * Selects the dialog which should be displayed by OpenCms depending on the registry value.<p>
 * 
 * You can define the class of your dialog handler in the OpenCms registry.xml in the &lt;dialoghandler&gt; node. 
 * The following files use this class:
 * <ul>
 * <li>/commons/property_html
 * <li>/commons/delete_html
 * <li>/commons/lock_html
 * <li>/commons/lockchange_html
 * <li>/commons/unlock_html
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.16 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.workplace.I_CmsDialogHandler
 */
public class CmsDialogSelector {

    // Constants for the dialog handler key names used for the runtime properties.
    // For each handler, a constant has to be added here. 
    /** Constant for the delete dialog handler key name. */
    public static final String DIALOG_DELETE = "class_dialog_delete";
    /** Constant for the lock dialog handler key name. */
    public static final String DIALOG_LOCK = "class_dialog_lock";
    /** Constant for the property dialog handler key name. */
    public static final String DIALOG_PROPERTY = "class_dialog_property";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialogSelector.class);
    private String m_handler;

    // necessary member variables
    private CmsJspActionElement m_jsp;
    private String m_paramResource;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param handler the key name of the dialog handler (use the constants in your classes!)
     */
    public CmsDialogSelector(CmsJspActionElement jsp, String handler) {

        setJsp(jsp);
        setHandler(handler);
        setParamResource(CmsEncoder.decode(jsp.getRequest().getParameter(I_CmsWpConstants.C_PARA_RESOURCE)));
    }

    /**
     * Returns the uri of the dialog which will be displayed.<p>
     *  
     * @return the uri of the property dialog
     */
    public String getSelectedDialogUri() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_DIALOG_HANDLER_CLASS_2, getClass().getName(), getHandler()));
            LOG.debug(Messages.get().key(Messages.LOG_PARAM_RESOURCE_2, getClass().getName(), getParamResource()));
        }
        // get the handler class from the OpenCms runtime property
        I_CmsDialogHandler dialogClass = (I_CmsDialogHandler)OpenCms.getWorkplaceManager().getDialogHandler(
            getHandler());
        if (dialogClass == null) {
            // error getting the dialog class, return to file list
            return CmsWorkplace.C_FILE_EXPLORER_FILELIST;
        }
        // get the dialog URI from the class defined in the registry 
        return dialogClass.getDialogUri(getParamResource(), getJsp());
    }

    /**
     * Returns the key name of the dialog handler.<p>
     * 
     * @return the key name of the dialog handler
     */
    private String getHandler() {

        return m_handler;
    }

    /**
     * Returns the CmsJspActionElement.<p>
     * 
     * @return the CmsJspActionElement
     */
    private CmsJspActionElement getJsp() {

        return m_jsp;
    }

    /**
     * Returns the resource parameter String.<p>
     * 
     * @return the resource parameter String
     */
    private String getParamResource() {

        return m_paramResource;
    }

    /**
     * Sets the key name of the dialog handler.<p>
     * 
     * @param handler the key name of the dialog handler
     */
    private void setHandler(String handler) {

        m_handler = handler;
    }

    /**
     * Sets the CmsJspActionElement.<p>
     * 
     * @param jsp the CmsJspActionElement
     */
    private void setJsp(CmsJspActionElement jsp) {

        m_jsp = jsp;
    }

    /**
     * Sets the resource parameter String.<p>
     * 
     * @param resource the resource parameter String
     */
    private void setParamResource(String resource) {

        m_paramResource = resource;
    }

}
