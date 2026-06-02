/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.logging.Log;

/**
 * Content handler for HTML redirects.<p>
 *
 * <p>Note: The NewWindow field is handled in a special way. It does not actually store data. It's only visible if the sitemap attribute template.redirect.newwindow.keyword is set,
 * and in that case, it is dynamically initialized with the value 'true' or 'false' depending on whether the keyword from the sitemap attribute is present in the NavInfo property. When
 * the content is written, the NewWindow field is emptied, but depending on its value, the keyword will be either added to or removed from the NavInfo property.
 */
public class CmsHtmlRedirectHandler extends CmsDefaultXmlContentHandler {

    public static final String ATTR_NEWWINDOW_KEYWORD = "template.redirect.newwindow.keyword";

    /** The content field used to (temporarily) store the information whether the redirect should be opened in another window when used as part of the navigation. */
    public static final String NEW_WINDOW_FIELD = "NewWindow";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlRedirectHandler.class);

    /**
     * Gets the keyword from the sitemap configuration whose presence in NavInfo should determine the value of the NewWindow field.
     *
     * @param config the sitemap configuration
     * @return the keyword (may be null if none configured)
     */
    public static String getNewWindowKeyword(CmsADEConfigData config) {

        String result = config.getAttribute(ATTR_NEWWINDOW_KEYWORD, null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result) || "none".equals(result)) {
            return null;
        }
        return result.trim();

    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#hasVisibilityHandlers()
     */
    @Override
    public boolean hasVisibilityHandlers() {

        // we do not technically have "visibility handlers", but we still have to return true so isVisible() is called
        return true;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#isVisible(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlSchemaType, java.lang.String, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public boolean isVisible(
        CmsObject cms,
        I_CmsXmlSchemaType contentValue,
        String valuePath,
        CmsResource resource,
        Locale contentLocale) {

        if (NEW_WINDOW_FIELD.equals(valuePath)) {
            // only show NewWindow field if the template.redirect.newwindow.keyword sitemap attribute is set
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(cms, resource.getRootPath());
            String attr = getNewWindowKeyword(config);
            if (attr == null) {
                return false;
            }
            if (OpenCms.getDefaultFiles().contains(resource.getName())) {
                try {
                    CmsProperty prop = cms.readPropertyObject(
                        CmsResource.getParentFolder(cms.getSitePath(resource)),
                        CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                        false);
                    if (CmsJspNavBuilder.NAVIGATION_LEVEL_FOLDER.equals(prop.getValue())) {
                        // for nav levels, the navigation properties are set on the folder, not on the redirect itself.
                        // arguably, we could read/write the property to the folder, but that comes with complications.
                        // but since the 'Open in New Window' feature is mainly intended for external links, we hide the content
                        // field itself in the navlevel case
                        return false;
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return true;
        } else {
            return super.isVisible(cms, contentValue, valuePath, resource, contentLocale);
        }
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#prepareForUse(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent)
     */
    @Override
    public CmsXmlContent prepareForUse(CmsObject cms, CmsXmlContent content) {

        CmsXmlContent result = super.prepareForUse(cms, content);
        if (content.getFile() != null) {

            // dynamically initialize NewWindow field based on NavInfo
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                cms,
                content.getFile().getRootPath());
            String keyword = getNewWindowKeyword(config);
            if (keyword != null) {
                I_CmsXmlContentValue newWindowVal = content.getValue(NEW_WINDOW_FIELD, CmsLocaleManager.MASTER_LOCALE);
                if (newWindowVal == null) {
                    newWindowVal = content.addValue(cms, NEW_WINDOW_FIELD, CmsLocaleManager.MASTER_LOCALE, 0);
                }

                // prepareForUse is called every time the content is unmarshalled, even during saving,
                // so we can't blindly update the field from the NavInfo property every time (or the changes made by the user would
                // be overwritten).
                // We use the condition where the field value is empty as a signal that this is a 'first' unmarshal operation where
                // the data to unmarshal came straight from the database. We can do that because we always clear out the field value
                // before saving.

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(newWindowVal.getStringValue(cms))) {
                    boolean newWindow = false;
                    try {
                        CmsProperty navInfoProp = cms.readPropertyObject(
                            content.getFile(),
                            CmsPropertyDefinition.PROPERTY_NAVINFO,
                            false);
                        String navInfo = navInfoProp.getValue();
                        if (navInfo != null) {
                            newWindow = CmsStringUtil.hasKeyword(navInfo, keyword);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                    newWindowVal.setStringValue(cms, "" + newWindow);
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        try {
            String linkStr = getStringValue(cms, content, "Link");
            String typeStr = getStringValue(cms, content, "Type");

            if ("sublevel".equals(typeStr)) {
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, file);
                String title = org.opencms.xml.containerpage.Messages.get().getBundle(locale).key(
                    org.opencms.xml.containerpage.Messages.GUI_REDIRECT_SUBLEVEL_TITLE_0);
                CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                cms.writePropertyObjects(file, Arrays.asList(titleProp));
            } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(linkStr)) {
                boolean hasScheme = false;
                try {
                    URI uri = new URI(linkStr);
                    hasScheme = uri.getScheme() != null;
                } catch (URISyntaxException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
                if (!hasScheme) {
                    linkStr = cms.getRequestContext().removeSiteRoot(linkStr);
                }
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, file);
                String title = org.opencms.xml.containerpage.Messages.get().getBundle(locale).key(
                    org.opencms.xml.containerpage.Messages.GUI_REDIRECT_TITLE_1,
                    linkStr);
                CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                cms.writePropertyObjects(file, Arrays.asList(titleProp));
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        // apply NewWindow field value to NavInfo property, also remove contents of the NewWindow field from the actual XML
        try {
            CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                cms,
                content.getFile().getRootPath());
            String keyword = getNewWindowKeyword(config);
            if (keyword != null) {
                boolean newWindow = false;
                I_CmsXmlContentValue newWindowVal = content.getValue(NEW_WINDOW_FIELD, CmsLocaleManager.MASTER_LOCALE);
                if (newWindowVal == null) {
                    newWindowVal = content.addValue(cms, NEW_WINDOW_FIELD, CmsLocaleManager.MASTER_LOCALE, 0);
                }
                newWindow = Boolean.parseBoolean(newWindowVal.getStringValue(cms));
                newWindowVal.setStringValue(cms, "");

                CmsProperty navInfoProp = cms.readPropertyObject(
                    content.getFile(),
                    CmsPropertyDefinition.PROPERTY_NAVINFO,
                    false);
                String navInfo = navInfoProp.getValue();
                if (navInfo == null) {
                    navInfo = "";
                }

                String newNavInfo = CmsStringUtil.toggleKeyword(navInfo, keyword, newWindow);
                if (!Objects.equals(navInfo, newNavInfo)) {
                    cms.writePropertyObjects(
                        file,
                        Arrays.asList(new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVINFO, newNavInfo, null)));
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        CmsFile result = super.prepareForWrite(cms, content, file);
        return result;
    }

    private String getStringValue(CmsObject cms, CmsXmlContent content, String node) {

        I_CmsXmlContentValue val = content.getValue(node, Locale.ENGLISH);
        if (val == null) {
            return null;
        }
        return val.getStringValue(cms);
    }

}
