/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsDefaultFileNameGenerator.java,v $
 * Date   : $Date: 2011/04/28 14:27:27 $
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.I_CmsFileNameGenerator;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The default class used for generating file names either for the <code>urlName</code> mapping 
 * or when using a "new" operation in the context of the direct edit interface.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDefaultFileNameGenerator implements I_CmsFileNameGenerator {

    /**
     * This default implementation will just generate a 5 digit sequence that is appended to the resource name in case 
     * of a collision of names.<p>
     * 
     * @see org.opencms.loader.I_CmsFileNameGenerator#getUrlNameSequence(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.file.CmsResource)
     */
    public Iterator<String> getUrlNameSequence(
        CmsObject cms,
        CmsXmlContent content,
        I_CmsXmlContentValue value,
        CmsResource sibling) {

        String translatedTitle = OpenCms.getResourceManager().getFileTranslator().translateResource(
            value.getStringValue(cms)).replace("/", "_");
        return new CmsNumberSuffixNameSequence(translatedTitle);
    }

    /**
     * The pattern in this default implementation must be a path which may contain the macro <code>%(number)</code>.
     * This will be replaced by the first 5 digit sequence for which the resulting file name is not already
     * used.<p>
     * 
     * @see org.opencms.loader.I_CmsFileNameGenerator#getNewFileName(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getNewFileName(CmsObject cms, String namePattern) throws CmsException {

        String checkPattern = cms.getRequestContext().removeSiteRoot(namePattern);
        String folderName = CmsResource.getFolderPath(checkPattern);

        // must check ALL resources in folder because name doesn't care for type
        List<CmsResource> resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);

        // now create a list of all the file names
        List<String> fileNames = new ArrayList<String>(resources.size());
        for (CmsResource res : resources) {
            fileNames.add(cms.getSitePath(res));
        }

        String checkFileName, checkTempFileName, number;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        int j = 0;
        do {
            number = I_CmsFileNameGenerator.NUMBER_FORMAT.sprintf(++j);
            resolver.addMacro(I_CmsFileNameGenerator.MACRO_NUMBER, number);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(checkPattern);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (fileNames.contains(checkFileName) || fileNames.contains(checkTempFileName));

        return checkFileName;
    }
}