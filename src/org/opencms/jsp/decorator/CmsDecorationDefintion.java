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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.decorator;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * This class defines text decoration to be made by the postprocessor.<p>
 *
 * @since 6.1.3
 */
public class CmsDecorationDefintion {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDecorationDefintion.class);

    /** The name of the configuration file holding all word substitutions. */
    private String m_configurationFile;

    /** Flag, signaling if the first occurance of a word must be marked differntly. */
    private boolean m_markFirst;

    /** The name of the substitution. */
    private String m_name;

    /** The post to be added after the occurance of a word. */
    private String m_postText;

    /** The post to be added after the occurance of a word on its first occurance. */
    private String m_postTextFirst;

    /** The prefix to be added in front of the occurance of a word. */
    private String m_preText;

    /** The prefix to be added in front of the occurance of a word on its first occurance. */
    private String m_preTextFirst;

    /**
     * Constructor, creates a new empty CmsDecorationDefintion.<p>
     */
    public CmsDecorationDefintion() {

        m_configurationFile = null;
        m_markFirst = false;
        m_name = null;
        m_postText = null;
        m_postTextFirst = null;
        m_preText = null;
        m_preTextFirst = null;
    }

    /**
     * Constructor, creates a new CmsDecorationDefintion with given values.<p>
     *
     * @param name the name of the decoration defintinion
     * @param preText the preText to be used
     * @param postText the postText to be used
     * @param preTextFirst the preTextFirst to be used
     * @param postTextFirst the postTextFirst to be used
     * @param markFrist the flag to use different decorations for the first occurance
     * @param configurationFile the name of the configuration file
     */
    public CmsDecorationDefintion(
        String name,
        String preText,
        String postText,
        String preTextFirst,
        String postTextFirst,
        boolean markFrist,
        String configurationFile) {

        m_configurationFile = configurationFile;
        m_markFirst = markFrist;
        m_name = name;
        m_postText = postText;
        m_postTextFirst = postTextFirst;
        m_preText = preText;
        m_preTextFirst = preTextFirst;
    }

    /**
     * Returns all different decoration configuration names (like "abbr" or "acronym") that
     * are in the config file pointed to by module parameter "configfile".<p>
     *
     * @param cms needed to access the decoration definition XML content
     *
     * @return  all different decoration configuration names (like "abbr" or "acronym") that
     *      are in the config file pointed to by module parameter "configfile"
     *
     * @throws CmsException if sth goes wrong
     */
    public static List<String> getDecorationDefinitionNames(CmsObject cms) throws CmsException {

        List<String> result = new ArrayList<String>();
        CmsModule module = OpenCms.getModuleManager().getModule("com.alkacon.opencms.extendeddecorator");
        String configFile = module.getParameter("configfile");
        if (CmsStringUtil.isEmpty(configFile)) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_CONFIG_MISSING_0));
        } else {
            CmsDecoratorConfiguration config = new CmsDecoratorConfiguration(cms, configFile);
            List<CmsDecorationDefintion> decorationDefinitions = config.getDecorationDefinitions();
            Iterator<CmsDecorationDefintion> it = decorationDefinitions.iterator();
            CmsDecorationDefintion decDef;
            while (it.hasNext()) {
                decDef = it.next();
                result.add(decDef.getName());
            }

        }

        return result;
    }

    /**
     * Creates a CmsDecorationBundle of text decoration to be used by the decorator.<p>
     *
     * @param cms the CmsObject
     * @param locale the locale to build the decoration bundle for. If no locale is given, a bundle of all locales is build
     * @return CmsDecorationBundle including all decoration lists that match the locale
     * @throws CmsException if something goes wrong
     */
    public CmsDecorationBundle createDecorationBundle(CmsObject cms, Locale locale) throws CmsException {

        // get configfile basename and the list of all decoration map files
        List<CmsResource> decorationMapFiles = getDecorationMapFiles(cms);
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_DECORATION_DEFINITION_MAP_FILES_2,
                    decorationMapFiles,
                    locale));
        }

        // create decoration maps
        List<CmsDecorationMap> decorationMaps = getDecorationMaps(cms, decorationMapFiles);
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DECORATION_DEFINITION_MAPS_2, decorationMaps, locale));
        }

        // now that we have all decoration maps we can build the decoration bundle
        // the bundele is depending on the locale, if a locale is given, only those decoration maps that contain the
        // locale (or no locale at all) must be used. If no locale is given, all decoration maps are
        // put into the decoration bundle
        return createDecorationBundle(decorationMaps, locale);
    }

    /**
     * Creates a CmsDecorationBundle of text decoration to be used by the decorator based on a list of decoration maps.<p>
     *
     * @param decorationMaps the decoration maps to build the bundle from
     * @param locale the locale to build the decoration bundle for. If no locale is given, a bundle of all locales is build
     * @return CmsDecorationBundle including all decoration lists that match the locale
     */
    public CmsDecorationBundle createDecorationBundle(List<CmsDecorationMap> decorationMaps, Locale locale) {

        CmsDecorationBundle decorationBundle = new CmsDecorationBundle(locale);
        // sort the bundles
        Collections.sort(decorationMaps);
        // now process the decoration maps to see which of those must be added to the bundle
        Iterator<CmsDecorationMap> i = decorationMaps.iterator();
        while (i.hasNext()) {
            CmsDecorationMap decMap = i.next();
            // a decoration map must be added to the bundle if one of the following conditions match:
            // 1) the bundle has no locale
            // 2) the bundle has a locale and the locale of the map is equal or a sublocale
            // 3) the bundle has a locale and the map has no locale
            if ((locale == null)
                || ((decMap.getLocale() == null))
                || (locale.getDisplayLanguage().equals(decMap.getLocale().getDisplayLanguage()))) {
                decorationBundle.putAll(decMap.getDecorationMap());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DECORATION_DEFINITION_CREATE_BUNDLE_2,
                            decMap.getName(),
                            locale));
                }
            }
        }
        return decorationBundle;
    }

    /**
     * Returns the configurationFile.<p>
     *
     *
     * @return the configurationFile
     */
    public String getConfigurationFile() {

        return m_configurationFile;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the postText.<p>
     *
     * @return the postText
     */
    public String getPostText() {

        return m_postText;
    }

    /**
     * Returns the postTextFirst.<p>
     *
     * @return the postTextFirst
     */
    public String getPostTextFirst() {

        return m_postTextFirst;
    }

    /**
     * Returns the preText.<p>
     *
     * @return the preText
     */
    public String getPreText() {

        return m_preText;
    }

    /**
     * Returns the preTextFirst.<p>
     *
     * @return the preTextFirst
     */
    public String getPreTextFirst() {

        return m_preTextFirst;
    }

    /**
     * Returns the markFirst flag.<p>
     *
     * @return the markFirst flag
     */
    public boolean isMarkFirst() {

        return m_markFirst;
    }

    /**
     * Sets the configurationFile.<p>
     *
     * @param configurationFile the configurationFile to set
     */
    public void setConfigurationFile(String configurationFile) {

        m_configurationFile = configurationFile;
    }

    /**
     * Sets the markFirst flag.<p>
     *
     * @param markFirst the markFirst flag to set
     */
    public void setMarkFirst(boolean markFirst) {

        m_markFirst = markFirst;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the postText.<p>
     *
     * @param postText the postText to set
     */
    public void setPostText(String postText) {

        m_postText = postText;
    }

    /**
     * Sets the postTextFirst.<p>
     *
     * @param postTextFirst the postTextFirst to set
     */
    public void setPostTextFirst(String postTextFirst) {

        m_postTextFirst = postTextFirst;
    }

    /**
     * Sets the preText.<p>
     *
     * @param preText the preText to set
     */
    public void setPreText(String preText) {

        m_preText = preText;
    }

    /**
     * Sets the preTextFirst.<p>
     *
     * @param preTextFirst the preTextFirst to set
     */
    public void setPreTextFirst(String preTextFirst) {

        m_preTextFirst = preTextFirst;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getName());
        buf.append(" [name = '");
        buf.append(m_name);
        buf.append("', markFirst = '");
        buf.append(m_markFirst);
        buf.append("', preText = '");
        buf.append(m_preText);
        buf.append("', postText = '");
        buf.append(m_postText);
        buf.append("', preTextFirst = '");
        buf.append(m_preTextFirst);
        buf.append("', postTextFirst = '");
        buf.append(m_postTextFirst);
        buf.append("', configFile = ");
        buf.append(m_configurationFile);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Gets the list of all decoartion map files that match to the current basename.<p>
     *
     * @param cms the CmsObject
     * @return list of CmsResources of the decoration map files
     * @throws CmsException if something goes wrong.
     */
    private List<CmsResource> getDecorationMapFiles(CmsObject cms) throws CmsException {

        List<CmsResource> files = new ArrayList<CmsResource>();

        // calcualte the basename for the decoration map files
        // the basename is the filename without the fileextension and any "_locale" postfixes
        // e.g. decoration_en.csv will generate "decoration" as basename
        StringBuffer baseFilename = new StringBuffer();
        baseFilename.append(CmsResource.getParentFolder(m_configurationFile));
        String filename = cms.readResource(m_configurationFile).getName();
        // get rid of the fileextension if there is one
        if (filename.lastIndexOf(".") > -1) {
            filename = filename.substring(0, filename.lastIndexOf("."));
        }
        // extract the basename
        if (filename.lastIndexOf("_") > -1) {
            filename = filename.substring(0, filename.lastIndexOf("_"));
        }
        baseFilename.append(filename);
        String basename = baseFilename.toString();

        // get all config files which belong to this basename
        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        List<CmsResource> resources = cms.readResources(
            CmsResource.getParentFolder(m_configurationFile),
            CmsResourceFilter.DEFAULT);
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
            if (cms.getSitePath(res).startsWith(basename) && (res.getTypeId() == plainId)) {
                files.add(res);
            }
        }

        return files;
    }

    /**
     * Creates a list of decoration map objects from a given list of decoration files.<p>
     *
     * @param cms the CmsObject
     * @param decorationListFiles the list of decoration files
     * @return list of decoration map objects
     */
    private List<CmsDecorationMap> getDecorationMaps(CmsObject cms, List<CmsResource> decorationListFiles) {

        List<CmsDecorationMap> decorationMaps = new ArrayList<CmsDecorationMap>();
        Iterator<CmsResource> i = decorationListFiles.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
            try {
                CmsDecorationMap decMap = (CmsDecorationMap)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(
                    cms,
                    res.getRootPath());
                if (decMap == null) {
                    decMap = new CmsDecorationMap(cms, res, this);
                    CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(cms, res.getRootPath(), decMap);
                }

                decorationMaps.add(decMap);
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_DECORATION_DEFINITION_CREATE_MAP_2,
                            res.getName(),
                            e));
                }
            }
        }
        return decorationMaps;
    }

}
