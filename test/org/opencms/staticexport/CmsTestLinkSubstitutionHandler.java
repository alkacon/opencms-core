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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;
import java.util.Map;

/**
 * Test handler for custom link substitution.<p>
 */
public class CmsTestLinkSubstitutionHandler extends CmsDefaultLinkSubstitutionHandler {

    /** Path to the news folder, missing the locale (for news link replacement). */
    private static final String FOLDER_NEWS = "/news/";

    /** Path to the system folder (for news link replacement). */
    private static final String FOLDER_SYSTEM = "/system/";

    /** Path to the system news folder (for newslink replacement). */
    private static final String FOLDER_SYSTEM_NEWS = FOLDER_SYSTEM + "news/";

    /**
     * Checks if the given path is pointing to a system news folder (like <code>/system/news/</code>),
     * and in this case replaces it with the localized news path (like <code>/en/news/</code>).<p>
     *
     * @param path the path to check and replace
     * @param locale the locale to build the news path with
     *
     * @return the localized news path if the path points to the system news folder, or the path parameter if not
     */
    private static String replaceNewsUri(String path, Locale locale) {

        String result = path;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            return result;
        }

        if (path.startsWith(FOLDER_SYSTEM_NEWS)) {
            String newsPath = path.substring(FOLDER_SYSTEM_NEWS.length());
            String loc = locale.getLanguage();
            // check if a "__locale" parameter is part of the path, if so overwrite the locale
            int pos = newsPath.indexOf('?');
            if ((pos > 0) && (newsPath.indexOf(CmsLocaleManager.PARAMETER_LOCALE, pos) >= 0)) {
                // locale parameter was found
                String query = newsPath.substring(pos);
                Map<String, String[]> params = CmsRequestUtil.createParameterMap(query);
                String[] locs = params.get(CmsLocaleManager.PARAMETER_LOCALE);
                if ((locs != null) && (locs.length > 0)) {
                    // remove locale from parameters and change URI to target language
                    loc = locs[0];
                    params.remove(CmsLocaleManager.PARAMETER_LOCALE);
                    newsPath = CmsRequestUtil.appendParameters(newsPath.substring(0, pos), params, true);
                }
            }
            result = "/" + loc + FOLDER_NEWS + newsPath;
        }

        return result;
    }

    /**
     * Checks if the given path is pointing to a localized news path(like <code>/en/news/</code>),
     * and in this case replaces it with the system news folder (like <code>/system/news/</code>).<p>
     *
     * @param path the path to check and replace
     * @param addLocale if true, the locale parameter <code>__locale=xx</code> is appended
     *
     * @return the system news folder if the path points to a localized news path, or the path parameter if not
     */
    private static String replaceSystemUri(String path, boolean addLocale) {

        String result = path;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path) || (path.length() < 4)) {
            return result;
        }
        String check = path.substring(3);
        if (check.startsWith(FOLDER_NEWS)) {
            String loc = path.substring(1, 3);
            check = check.substring(FOLDER_NEWS.length());
            if (addLocale && (check.indexOf(CmsLocaleManager.PARAMETER_LOCALE) < 0)) {
                // append locale parameter if required, but not for default "en" locale
                check = CmsRequestUtil.appendParameter(check, CmsLocaleManager.PARAMETER_LOCALE, loc);
            }
            result = FOLDER_SYSTEM_NEWS + check;
        }

        return result;
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#getLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public String getLink(CmsObject cms, String link, String siteRoot, boolean forceSecure) {

        // do link replacement for news
        link = replaceNewsUri(link, cms.getRequestContext().getLocale());
        // the rest is just like the default
        return super.getLink(cms, link, siteRoot, forceSecure);
    }

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#getRootPath(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public String getRootPath(CmsObject cms, String targetUri, String basePath) {

        // first do the normal calculation
        String result = super.getRootPath(cms, targetUri, basePath);

        if (result != null) {
            // there was a replacement, so the system assumes this is a root path
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(result);
            // in case a site root was appended, it must be removed
            String path = (siteRoot != null) ? result.substring(siteRoot.length()) : result;
            boolean addLocale = (path.indexOf("/de/") >= 0) || (path.indexOf(CmsLocaleManager.PARAMETER_LOCALE) >= 0);
            String replace = replaceSystemUri(path, addLocale);
            if (!path.equals(replace)) {
                // this is a link to a news folder
                result = replace;
            }
        }
        return result;
    }
}