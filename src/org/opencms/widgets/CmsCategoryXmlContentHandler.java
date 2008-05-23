/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsCategoryXmlContentHandler.java,v $
 * Date   : $Date: 2008/05/23 12:48:39 $
 * Version: $Revision: 1.1 $
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

package org.opencms.widgets;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsXmlContentWidgetVisitor;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 *  This handler adds the categories to the current resource and all siblings.<p>
 *  
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 *  
 * @since 7.0.5
 */
public class CmsCategoryXmlContentHandler extends CmsDefaultXmlContentHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategoryXmlContentHandler.class);

    /**
     * Default constructor.<p>
     */
    public CmsCategoryXmlContentHandler() {

        super();
    }

    /**
     * Returns the default locale in the content of the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource path to get the default locale for
     * 
     * @return the default locale of the resource
     */
    public static Locale getDefaultLocale(CmsObject cms, String resource) {

        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, resource);
        if (locale == null) {
            List locales = OpenCms.getLocaleManager().getAvailableLocales();
            if (locales.size() > 0) {
                locale = (Locale)locales.get(0);
            } else {
                locale = Locale.ENGLISH;
            }
        }
        return locale;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        super.prepareForWrite(cms, content, file);
        if (!CmsWorkplace.isTemporaryFile(file)) {
            // get the temporary file
            String folderPath = cms.getRequestContext().removeSiteRoot(CmsResource.getFolderPath(file.getRootPath()));
            String tmpName = CmsWorkplace.getTemporaryFileName(file.getName());
            if (cms.existsResource(folderPath + tmpName, CmsResourceFilter.ALL)) {
                // read all siblings
                List listsib = cms.readSiblings(cms.getSitePath(file), CmsResourceFilter.ALL);
                for (int i = 0; i < listsib.size(); i++) {
                    CmsResource resource = (CmsResource)listsib.get(i);
                    try {
                        // get the default locale of the resource and set the category
                        Locale locale = getDefaultLocale(cms, cms.getSitePath(resource));
                        resetCategories(cms, resource);
                        setCategories(cms, content, resource, locale);
                    } catch (CmsException ex) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(ex.getLocalizedMessage(), ex);
                        }
                    }
                }
            }
        }
        return file;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#resolveValidation(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    public CmsXmlContentErrorHandler resolveValidation(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler) {

        CmsXmlContentErrorHandler handler = super.resolveValidation(cms, value, errorHandler);
        I_CmsWidget widget = null;
        try {
            widget = value.getContentDefinition().getContentHandler().getWidget(value);
        } catch (CmsXmlException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (widget instanceof CmsCategoryWidget) {
            String stringValue = value.getStringValue(cms);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(stringValue)) {
                errorHandler.addError(value, Messages.get().getBundle(value.getLocale()).key(
                    Messages.GUI_CATEGORY_CHECK_EMPTY_ERROR_0));
                return handler;
            }
            if (((CmsCategoryWidget)widget).isOnlyLeafs()) {
                try {
                    if (!cms.getSubFolders(stringValue).isEmpty()) {
                        errorHandler.addError(value, Messages.get().getBundle(value.getLocale()).key(
                            Messages.GUI_CATEGORY_CHECK_NOLEAF_ERROR_0));
                    }
                } catch (CmsException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return handler;
    }

    /**
     * Returns all categories set for the given locale.<p>
     * 
     * @param content the xml content to check
     * @param locale the locale to use
     * 
     * @return <code>true</code>, if there is a category widget present
     */
    protected List getCategories(CmsXmlContent content, Locale locale) {

        List categories = new ArrayList();
        CmsXmlContentWidgetVisitor widgetCollector = new CmsXmlContentWidgetVisitor(locale);
        content.visitAllValuesWith(widgetCollector);
        Iterator itWidgets = widgetCollector.getValues().entrySet().iterator();
        while (itWidgets.hasNext()) {
            Map.Entry entry = (Map.Entry)itWidgets.next();
            String xpath = (String)entry.getKey();
            I_CmsWidget widget = (I_CmsWidget)widgetCollector.getWidgets().get(xpath);
            if (widget instanceof CmsCategoryWidget) {
                categories.add(entry.getValue());
            }
        }
        return categories;
    }

    /**
     * Removes this resource from all categories.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to reset the categories for
     * 
     * @throws CmsException if something goes wrong
     */
    protected void resetCategories(CmsObject cms, CmsResource resource) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        filter = filter.filterType(CmsRelationType.CATEGORY);
        filter = filter.filterResource(cms.readResource("/system/categories/"));
        filter = filter.filterIncludeChildren();
        cms.deleteRelationsFromResource(cms.getSitePath(resource), filter);
    }

    /**
     * Sets categories from any category widget for the given locale.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to set the categories for
     * @param content the xml content 
     * @param locale the locale to read the categories from
     * 
     * @throws CmsException if an error occurred.
     */
    protected void setCategories(CmsObject cms, CmsXmlContent content, CmsResource resource, Locale locale)
    throws CmsException {

        Iterator itCats = getCategories(content, locale).iterator();
        while (itCats.hasNext()) {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)itCats.next();
            String category = value.getStringValue(cms);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(category)) {
                continue;
            }
            // add the file to the selected categories
            CmsCategoryService.getInstance().addResourceToCategory(
                cms,
                cms.getSitePath(resource),
                value.getStringValue(cms).substring("/system/categories".length()));
        }
    }
}
