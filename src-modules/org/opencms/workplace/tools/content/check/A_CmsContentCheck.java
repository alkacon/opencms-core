/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/A_CmsContentCheck.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.I_CmsToolHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for a CmsContentCheck.<p>
 * 
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.2 
 */
public abstract class A_CmsContentCheck implements I_CmsContentCheck, I_CmsToolHandler {

    /** Closelink path. */
    private static final String CLOSELINK = "/contenttools/checkconfig";

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#executeContentCheck(org.opencms.file.CmsObject, org.opencms.workplace.tools.content.check.CmsContentCheckResource)
     */
    public abstract CmsContentCheckResource executeContentCheck(CmsObject cms, CmsContentCheckResource testResource)
    throws CmsException;

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getConfirmationMessage()
     */
    public String getConfirmationMessage() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getDialogParameterName()
     */
    public abstract String getDialogParameterName();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getDisabledHelpText()
     */
    public String getDisabledHelpText() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getGroup()
     */
    public String getGroup() {

        return org.opencms.workplace.tools.content.Messages.get().key(
            org.opencms.workplace.tools.content.Messages.GUI_CHECKCONTENT_CONFIGURATION_ADMIN_TOOL_NAME_0);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getHelpText()
     */
    public abstract String getHelpText();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getIconPath()
     */
    public abstract String getIconPath();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getLink()
     */

    public abstract String getLink();

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getMessageBundles()
     */
    public abstract List getMessageBundles();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getName()
     */
    public abstract String getName();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getParameters(org.opencms.workplace.CmsWorkplace)
     */
    public Map getParameters(CmsWorkplace wp) {

        Map parameters = new HashMap();
        parameters.put(CmsDialog.PARAM_CLOSELINK, CmsToolManager.linkForToolPath(wp.getJsp(), CLOSELINK));

        return parameters;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPath()
     */
    public abstract String getPath();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPosition()
     */
    public abstract float getPosition();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getShortName()
     */
    public abstract String getShortName();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getSmallIconPath()
     */
    public String getSmallIconPath() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#init(org.opencms.file.CmsObject)
     */
    public abstract void init(CmsObject cms) throws CmsException;

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#isActive()
     */
    public abstract boolean isActive();

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#setActive(boolean)
     */
    public abstract void setActive(boolean value);

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#setup(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean setup(CmsObject cms, String resourcePath) {

        return true;
    }

}
