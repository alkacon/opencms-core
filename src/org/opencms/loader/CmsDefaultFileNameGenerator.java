/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsNumberSuffixNameSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Factory;

/**
 * The default class used for generating file names either for the <code>urlName</code> mapping 
 * or when using a "new" operation in the context of the direct edit interface.<p>
 * 
 * @since 8.0.0
 */
public class CmsDefaultFileNameGenerator implements I_CmsFileNameGenerator {

    /**
     * Factory to use for resolving the %(number) macro.<p>
     */
    public class CmsNumberFactory implements Factory {

        /** The actual number. */
        private int m_number;

        /** Format for file create parameter. */
        private PrintfFormat m_numberFormat;

        /**
         * Create a new number factory.<p>
         * 
         * @param digits the number of digits to use
         */
        public CmsNumberFactory(int digits) {

            m_numberFormat = new PrintfFormat("%0." + digits + "d");
            m_number = 0;
        }

        /**
         * Create the number based on the number of digits set.<p>
         * 
         * @see org.apache.commons.collections.Factory#create()
         */
        public Object create() {

            // formats the number with the amount of digits selected
            return m_numberFormat.sprintf(m_number);
        }

        /**
         * Sets the number to create to the given value.<p>
         * 
         * @param number the number to set
         */
        public void setNumber(int number) {

            m_number = number;
        }
    }

    /** Start sequence for macro with digits. */
    private static final String MACRO_NUMBER_START = "%(" + I_CmsFileNameGenerator.MACRO_NUMBER + ":";

    /**
     * Returns a new resource name based on the provided OpenCms user context and name pattern.<p>
     * 
     * The pattern in this default implementation must be a path which may contain the macro <code>%(number)</code>.
     * This will be replaced by the first "n" digit sequence for which the resulting file name is not already
     * used. For example the pattern <code>"/file_%(number).xml"</code> would result in something like <code>"/file_00003.xml"</code>.<p>
     * 
     * Alternatively, the macro can have the form <code>%(number:n)</code> with <code>n = {1...9}</code>, for example <code>%(number:6)</code>.
     * In this case the default digits will be ignored and instead the digits provided as "n" will be used.<p>
     * 
     * @param cms the current OpenCms user context
     * @param namePattern the  pattern to be used when generating the new resource name
     * @param defaultDigits the default number of digits to use for numbering the created file names 
     * 
     * @return a new resource name based on the provided OpenCms user context and name pattern
     * 
     * @throws CmsException in case something goes wrong
     */
    public String getNewFileName(CmsObject cms, String namePattern, int defaultDigits) throws CmsException {

        String checkPattern = cms.getRequestContext().removeSiteRoot(namePattern);
        String folderName = CmsResource.getFolderPath(checkPattern);

        // must check ALL resources in folder because name doesn't care for type
        List<CmsResource> resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);

        // now create a list of all the file names
        List<String> fileNames = new ArrayList<String>(resources.size());
        for (CmsResource res : resources) {
            fileNames.add(cms.getSitePath(res));
        }

        return getNewFileNameFromList(fileNames, checkPattern, defaultDigits);
    }

    /**
     * @see org.opencms.loader.I_CmsFileNameGenerator#getUniqueFileName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getUniqueFileName(CmsObject cms, String parentFolder, String baseName) {

        Iterator<String> nameIterator = getUrlNameSequence(baseName);
        String result = nameIterator.next();
        // use CmsResourceFilter.ALL because we also want to skip over deleted resources 
        while (cms.existsResource(CmsStringUtil.joinPaths(parentFolder, result), CmsResourceFilter.ALL)) {
            result = nameIterator.next();
        }
        return result;
    }

    /**
     * This default implementation will just generate a 5 digit sequence that is appended to the resource name in case 
     * of a collision of names.<p>
     * 
     * @see org.opencms.loader.I_CmsFileNameGenerator#getUrlNameSequence(java.lang.String)
     */
    public Iterator<String> getUrlNameSequence(String baseName) {

        String translatedTitle = OpenCms.getResourceManager().getFileTranslator().translateResource(baseName).replace(
            "/",
            "-");
        return new CmsNumberSuffixNameSequence(translatedTitle);
    }

    /**
     * Internal method for file name generation, decoupled for testing.<p>
     * 
     * @param fileNames the list of file names already existing in the folder
     * @param checkPattern the pattern to be used when generating the new resource name
     * @param defaultDigits the default number of digits to use for numbering the created file names 
     * 
     * @return a new resource name based on the provided OpenCms user context and name pattern
     */
    protected String getNewFileNameFromList(List<String> fileNames, String checkPattern, int defaultDigits) {

        String checkFileName, checkTempFileName;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        String macro = I_CmsFileNameGenerator.MACRO_NUMBER;
        int useDigits = defaultDigits;

        int prefixIndex = checkPattern.indexOf(MACRO_NUMBER_START);
        if (prefixIndex >= 0) {
            // this macro contains an individual digit setting
            char n = checkPattern.charAt(prefixIndex + MACRO_NUMBER_START.length());
            macro = macro + ':' + n;
            useDigits = Character.getNumericValue(n);
        }

        CmsNumberFactory numberFactory = new CmsNumberFactory(useDigits);
        resolver.addDynamicMacro(macro, numberFactory);

        int j = 0;
        do {
            numberFactory.setNumber(++j);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(checkPattern);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (fileNames.contains(checkFileName) || fileNames.contains(checkTempFileName));

        return checkFileName;
    }
}