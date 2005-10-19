/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckProperty.java,v $
 * Date   : $Date: 2005/10/19 08:33:28 $
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
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation of the I_CmsContentCheck interface implments a check for 
 * resource properties.<p>
 * 
 * The following items can be configured and checked:
 * <ul>
 * <li></li>
 * </ul>
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.0 
 */
public class CmsContentCheckProperty implements I_CmsContentCheck {

    /** Name of the dialog parameter. */
    private static final String DIALOG_PARAMETER = "property";

    /** Name of this content check. */
    private static final String NAME = "Property Check";

    /** The active flag, signaling if this content check is active. */
    private boolean m_active = true;

    /**
     * 
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#executeContentCheck(org.opencms.file.CmsObject, org.opencms.workplace.tools.content.check.CmsContentCheckResource)
     */
    public CmsContentCheckResource executeContentCheck(CmsObject cms, CmsContentCheckResource testResource)
    throws CmsException {

        // dummy test code
        String title = cms.readPropertyObject(cms.getSitePath(testResource.getResource()), "Title", false).getValue();
        if (CmsStringUtil.isEmpty(title)) {
            // no title and resource is file -> error
            if (testResource.getResource().isFile()) {
                testResource.addError("No Title");
            } else {
                // if resource is folder -> warning
                testResource.addWarning("No Title on folder");
            }
        } else {
            // there is a title, now check if it is valid
            if (title.length() < 3) {
                testResource.addWarning("Title too short (" + title.length() + ")");
            }
            String filename = testResource.getResource().getName().toLowerCase();
            if (filename.startsWith(title.toLowerCase())) {
                testResource.addWarning("Title '" + title + "' contains filename");
            }
        }

        return testResource;
    }

    /**
     * 
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getDialogParameterName()
     */
    public String getDialogParameterName() {

        return DIALOG_PARAMETER;
    }

    /**
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getMessageBundles()
     */
    public List getMessageBundles() {

        List messages = new ArrayList();
        messages.add(org.opencms.workplace.tools.content.check.Messages.get().getBundleName());
        return messages;
    }

    /**
     * 
     * @see org.opencms.workplace.tools.content.check.I_CmsContentCheck#getName()
     */
    public String getName() {

        return NAME;
    }

    /**
     * Gets the active flag.<p>
     * 
     * @return true if this content check is active, false otherwise.
     */
    public boolean isActive() {

        return isProperty();
    }

    /**
     * Gets the active flag.<p>
     * 
     * This method is required to build the widget dialog frontend.
     * 
     * @return true if this content check is active, false otherwise.
     */
    public boolean isProperty() {

        return m_active;
    }

    /**
     * Sets the active flag.<p>
     * 
     * This method is required to build the widget dialog frontend.
     *
     * @param value true if this content check is set to be active, false otherwise.
     */
    public void setProperty(boolean value) {

        m_active = value;
    }

}
