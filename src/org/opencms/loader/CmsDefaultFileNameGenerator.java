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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsNumberSuffixNameSequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        protected int m_number;

        /** Format for file create parameter. */
        protected PrintfFormat m_numberFormat;

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
    private static final String MACRO_NUMBER_DIGIT_SEPARATOR = ":";

    /** The copy file name insert. */
    public static final String COPY_FILE_NAME_INSERT = "-copy";

    /**
     * Checks the given pattern for the number macro.<p>
     *
     * @param pattern the pattern to check
     *
     * @return <code>true</code> if the pattern contains the macro
     */
    public static boolean hasNumberMacro(String pattern) {

        // check both macro variants
        return hasNumberMacro(
            pattern,
            "" + I_CmsMacroResolver.MACRO_DELIMITER + I_CmsMacroResolver.MACRO_START,
            "" + I_CmsMacroResolver.MACRO_END)
            || hasNumberMacro(
                pattern,
                "" + I_CmsMacroResolver.MACRO_DELIMITER_OLD + I_CmsMacroResolver.MACRO_START_OLD,
                "" + I_CmsMacroResolver.MACRO_END_OLD);
    }

    /**
     * Removes the file extension if it only consists of letters.<p>
     *
     * @param path the path from which to remove the file extension
     *
     * @return the path without the file extension
     */
    public static String removeExtension(String path) {

        return path.replaceFirst("\\.[a-zA-Z]*$", "");
    }

    /**
     * Checks the given pattern for the number macro.<p>
     *
     * @param pattern the pattern to check
     * @param macroStart the macro start string
     * @param macroEnd the macro end string
     *
     * @return <code>true</code> if the pattern contains the macro
     */
    private static boolean hasNumberMacro(String pattern, String macroStart, String macroEnd) {

        String macro = I_CmsFileNameGenerator.MACRO_NUMBER;
        String macroPart = macroStart + macro + MACRO_NUMBER_DIGIT_SEPARATOR;
        int prefixIndex = pattern.indexOf(macroPart);
        if (prefixIndex >= 0) {
            // this macro contains an individual digit setting
            char n = pattern.charAt(prefixIndex + macroPart.length());
            macro = macro + MACRO_NUMBER_DIGIT_SEPARATOR + n;
        }
        return pattern.contains(macroStart + macro + macroEnd);
    }

    /**
     * @see org.opencms.loader.I_CmsFileNameGenerator#getCopyFileName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCopyFileName(CmsObject cms, String parentFolder, String baseName) {

        String name = baseName;
        int dot = name.lastIndexOf(".");
        if (dot > 0) {
            if (!name.substring(0, dot).endsWith(COPY_FILE_NAME_INSERT)) {
                name = name.substring(0, dot) + COPY_FILE_NAME_INSERT + name.substring(dot);
            }
        } else {
            if (!name.endsWith(COPY_FILE_NAME_INSERT)) {
                name += COPY_FILE_NAME_INSERT;
            }
        }
        return getUniqueFileName(cms, parentFolder, name);
    }

    /**
     * @see org.opencms.loader.I_CmsFileNameGenerator#getNewFileName(org.opencms.file.CmsObject, java.lang.String, int)
     */
    public String getNewFileName(CmsObject cms, String namePattern, int defaultDigits) throws CmsException {

        return getNewFileName(cms, namePattern, defaultDigits, false);
    }

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
     * @param explorerMode if true, the file name is first tried without a numeric macro, also underscores are inserted automatically before the number macro and don't need to be part of the name pattern
     *
     * @return a new resource name based on the provided OpenCms user context and name pattern
     *
     * @throws CmsException in case something goes wrong
     */
    public String getNewFileName(CmsObject cms, String namePattern, int defaultDigits, boolean explorerMode)
    throws CmsException {

        String checkPattern = cms.getRequestContext().removeSiteRoot(namePattern);
        String folderName = CmsResource.getFolderPath(checkPattern);

        // must check ALL resources in folder because name doesn't care for type
        List<CmsResource> resources = cms.readResources(folderName, CmsResourceFilter.ALL, false);

        // now create a list of all the file names
        List<String> fileNames = new ArrayList<String>(resources.size());
        for (CmsResource res : resources) {
            fileNames.add(cms.getSitePath(res));
        }

        return getNewFileNameFromList(fileNames, checkPattern, defaultDigits, explorerMode);
    }

    /**
     * @see org.opencms.loader.I_CmsFileNameGenerator#getUniqueFileName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getUniqueFileName(CmsObject cms, String parentFolder, String baseName) {

        Iterator<String> nameIterator = getUrlNameSequence(baseName);
        String result = nameIterator.next();
        CmsObject onlineCms = null;

        try {
            onlineCms = OpenCms.initCmsObject(cms);
            onlineCms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        } catch (CmsException e) {
            // should not happen, nothing to do
        }
        String path = CmsStringUtil.joinPaths(parentFolder, result);
        // use CmsResourceFilter.ALL because we also want to skip over deleted resources
        while (cms.existsResource(path, CmsResourceFilter.ALL)
            || ((onlineCms != null) && onlineCms.existsResource(path, CmsResourceFilter.ALL))) {
            result = nameIterator.next();
            path = CmsStringUtil.joinPaths(parentFolder, result);
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
     * @param explorerMode if true, first the file name without a number is tried, and also an underscore is automatically inserted before the number macro
     *
     * @return a new resource name based on the provided OpenCms user context and name pattern
     */
    protected String getNewFileNameFromList(
        List<String> fileNames,
        String checkPattern,
        int defaultDigits,
        final boolean explorerMode) {

        if (!hasNumberMacro(checkPattern)) {
            throw new IllegalArgumentException(
                Messages.get().getBundle().key(Messages.ERR_FILE_NAME_PATTERN_WITHOUT_NUMBER_MACRO_1, checkPattern));
        }

        String checkFileName, checkTempFileName;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        Set<String> extensionlessNames = new HashSet<String>();
        for (String name : fileNames) {
            if (name.length() > 1) {
                name = CmsFileUtil.removeTrailingSeparator(name);
            }
            extensionlessNames.add(removeExtension(name));
        }

        String macro = I_CmsFileNameGenerator.MACRO_NUMBER;
        int useDigits = defaultDigits;
        String macroStart = ""
            + I_CmsMacroResolver.MACRO_DELIMITER
            + I_CmsMacroResolver.MACRO_START
            + macro
            + MACRO_NUMBER_DIGIT_SEPARATOR;
        int prefixIndex = checkPattern.indexOf(macroStart);
        if (prefixIndex < 0) {
            macroStart = ""
                + I_CmsMacroResolver.MACRO_DELIMITER_OLD
                + I_CmsMacroResolver.MACRO_START_OLD
                + macro
                + MACRO_NUMBER_DIGIT_SEPARATOR;
            prefixIndex = checkPattern.indexOf(macroStart);
        }
        if (prefixIndex >= 0) {
            // this macro contains an individual digit setting
            char n = checkPattern.charAt(prefixIndex + macroStart.length());
            macro = macro + ':' + n;
            useDigits = Character.getNumericValue(n);
        }

        CmsNumberFactory numberFactory = new CmsNumberFactory(useDigits) {

            @Override
            public Object create() {

                if (explorerMode) {
                    if (m_number == 1) {
                        return "";
                    } else {
                        return "_" + m_numberFormat.sprintf(m_number - 1);
                    }
                } else {
                    return super.create();
                }
            }

        };
        resolver.addDynamicMacro(macro, numberFactory);
        Set<String> checked = new HashSet<String>();
        int j = 0;
        do {
            numberFactory.setNumber(++j);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(checkPattern);
            if (checked.contains(checkFileName)) {
                // the file name has been checked before, abort the search
                throw new RuntimeException(
                    Messages.get().getBundle().key(Messages.ERR_NO_FILE_NAME_AVAILABLE_FOR_PATTERN_1, checkPattern));
            }
            checked.add(checkFileName);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (extensionlessNames.contains(removeExtension(checkFileName))
            || extensionlessNames.contains(removeExtension(checkTempFileName)));

        return checkFileName;
    }

}