/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/Attic/CmsHtmlImportBackoffice.java,v $
 * Date   : $Date: 2005/07/06 11:40:29 $
 * Version: $Revision: 1.6 $
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

package org.opencms.workplace.tools.database;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;

import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

/**
 * This class contains some utility methods for the HTMLImport Backoffice.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsHtmlImportBackoffice {

    /**
     * Util class, not instanciable.<p>
     */
    private CmsHtmlImportBackoffice() {

        // intentionally left blank
    }

    /**
     * Returns a selectbox with all available locales.<p>
     * @param selectedLocale the selected Locale 
     * @return a selectbox with all available locales
     */
    public static String getLocales(Locale selectedLocale) {

        // the output buffer
        StringBuffer output = new StringBuffer();
        try {
            Iterator i = OpenCms.getLocaleManager().getAvailableLocales().iterator();
            // generate the HTML code for the selectbox
            output.append("<select  name=\"locale\"  width=80 size=\"1\">");

            // loop through all locales and build the entries
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                String language = locale.getLanguage();
                String displayLanguage = locale.getDisplayLanguage();
                output.append("<option ");
                if (selectedLocale.equals(locale)) {
                    output.append("selected ");
                }
                output.append("value=\"");
                output.append(language);
                output.append("\">");
                output.append(displayLanguage);
            }
            output.append("</select>");
        } catch (Exception e) {
            System.err.println(e);
        }

        return new String(output);
    }

    /**
     * Creates a selectbox with all available templates in the OpenCms system.<p>
     * 
     * @param cms the current CmsObject
     * @param selectedTemplate the preselcted template
     * @return HTML-Code for the selectbox, containing all templates
     */
    public static String getTemplates(CmsObject cms, String selectedTemplate) {

        // the output buffer
        StringBuffer output = new StringBuffer();
        TreeMap templates = null;

        try {
            templates = CmsNewResourceXmlPage.getTemplates(cms);

            // generate the HTML code for the selectbox
            output.append("<select name=\"template\" width=\"80\" size=\"1\">");

            // loop through all templates and build the entries
            Iterator i = templates.keySet().iterator();
            while (i.hasNext()) {
                String title = (String)i.next();
                String path = (String)templates.get(title);
                output.append("<option ");
                if (selectedTemplate.equals(path)) {
                    output.append("selected ");
                }
                output.append("value=\"");
                output.append(path);
                output.append("\">");
                output.append(title);
            }
            output.append("</select>");
        } catch (CmsException e) {
            System.err.println(e);
        }

        return new String(output);
    }
}
