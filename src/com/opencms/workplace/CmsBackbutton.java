/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsBackbutton.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
* Version: $Revision: 1.10 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import com.opencms.core.I_CmsSession;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;

import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;

import org.w3c.dom.Element;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 *
 * Creation date: (09.08.00 15:21:44)
 * @author Hanjo Riege
 * @version $Name:  $ $Revision: 1.10 $ $Date: 2004/02/13 13:41:44 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsBackbutton extends A_CmsWpElement implements I_CmsWpElement {

    /**
     * Handling of the special workplace <CODE>&lt;BACKBUTTON&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * A backbutton can be referenced in any workplace template by <br>
     * <CODE>&lt;BACKBUTTON name="..." label="..." action="..." href="..." target="..."/&gt;</CODE>
     *
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;BACKBUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */

    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc,
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        CmsXmlWpTemplateFile buttondef = this.getBackbuttonDefinitions(cms);
        CmsXmlWpConfigFile confFile = new CmsXmlWpConfigFile(cms);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String navPos = (String)session.getValue(I_CmsConstants.C_SESSION_ADMIN_POS);
        if((navPos == null) || (navPos.equals(confFile.getWorkplaceAdministrationPath()))) {

            // first call or on top-level => disable Button
            return buttondef.getProcessedDataValue("disable", callingObject);
        }
        else {

            // enable backbutton
            String linkValue = navPos.substring(0, navPos.length() - 1);
            linkValue = linkValue.substring(0, linkValue.lastIndexOf("/") + 1);
            buttondef.setData("linkTo", "?sender=" + linkValue);
            return buttondef.getProcessedDataValue("enable", callingObject);
        }
    } //end of handleSpecialWorkplaceTag
}
