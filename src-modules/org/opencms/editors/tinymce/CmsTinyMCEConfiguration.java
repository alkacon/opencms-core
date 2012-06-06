/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.editors.tinymce;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Class for processing extended TinyMCE configuration options.<p>
 * 
 * It does this by generating the Javascript code for a preprocessor function, which can then be used
 * to process the options passed to tinyMCE.init. What goes into the preprocessor function is determined
 * by the contents of the VFS file /system/module/org.opencms.editors.tinymce.config/tinymce.properties.<p>
 * 
 * Available options are:
 * <ul>
 * <li>pastefix.enabled (boolean): Enables a fix for copy/pasting from Word to IE9
 * <li>pastefix.valid_children (string): The valid_children option which is used for the paste fix 
 * <li>pastefix.javascript_condition (string): An additional Javascript expression which can be used to enable/disable the paste fix
 * <li>command (string, multivalue): Adds a Javascript command directly to the preprocessor function    
 * </ul>
 * 
 * Since this uses the {@link CmsParameterConfiguration} class for parsing, commas in Javascript fragments need to be escaped with '\'.<p>
 */
public class CmsTinyMCEConfiguration {

    /** The configuration file name. */
    public static final String CONFIG_FILE_NAME = "tinymce.properties";

    /** The configuration module name. */
    public static final String CONFIG_MODULE_NAME = "org.opencms.editors.tinymce.config";

    /** The key for the javascript condition for the paste fix. */
    public static final String KEY_PASTE_FIX_CONDITION = "pastefix.javascript_condition";

    /** The key to enable/disable the paste fix. */
    public static final String KEY_PASTE_FIX_ENABLED = "pastefix.enabled";

    /** The key for the valid children option used for the paste fix. */
    public static final String KEY_PASTE_FIX_VALID_CHILDREN = "pastefix.valid_children";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTinyMCEConfiguration.class);

    /** The parameter configuration. */
    private CmsParameterConfiguration m_parameterConfiguration = new CmsParameterConfiguration();

    /**
     * Gets the configuration object, using a cache.<p>
     * 
     * @param cms the current CMS context 
     * 
     * @return the TinyMCE configuration 
     */
    public static CmsTinyMCEConfiguration get(CmsObject cms) {

        String configPath = getConfigurationPath();
        CmsTinyMCEConfiguration cachedConfig = (CmsTinyMCEConfiguration)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(
            cms,
            configPath);
        if (cachedConfig == null) {
            LOG.info("Loading tinymce configuration from VFS.");
            cachedConfig = new CmsTinyMCEConfiguration();
            try {
                cachedConfig.load(cms, configPath);
            } catch (CmsException e) {
                LOG.warn("Could not read tinymce configuration", e);
            }
            CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(cms, configPath, cachedConfig);
        } else {
            LOG.info("Retrieving cached tinymce  configuration.");
        }
        return cachedConfig;
    }

    /**
     * Gets the path of the configuration file.<p>
     * 
     * @return the configuration file path 
     */
    public static String getConfigurationPath() {

        return "/system/modules/" + CONFIG_MODULE_NAME + "/" + CONFIG_FILE_NAME;
    }

    /** 
     * Quotes a string for use as a Javascript string.<p>
     * 
     * @param s the string to quote
     * 
     * @return the quoted string 
     */
    public static String quote(String s) {

        return "\"" + (s.replaceAll("\"", "\\\"")) + "\"";
    }

    /**
     * Generates the Javascript code for the option preprocessor.<p>
     * 
     * @param functionName the name for the option preprocessor function
     * 
     * @return the code for the preprocessor function 
     */
    public String generateOptionPreprocessor(String functionName) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("function " + functionName + "(options){\n");

        buffer.append(getPasteFix());
        buffer.append(getCommands());

        buffer.append("return options;\n");
        buffer.append("}\n");

        return buffer.toString();
    }

    /**
     * Gets the internal parameter configuration.<p>
     * 
     * @return the parameter configuration 
     */
    public CmsParameterConfiguration getParameters() {

        return m_parameterConfiguration;
    }

    /**
     * Gets the Javascript configured by the 'commands' parameters.<p>
     * 
     * @return the Javascript from the 'commands' parameters 
     */
    protected String getCommands() {

        StringBuffer buffer = new StringBuffer();
        List<String> commands = m_parameterConfiguration.getList("command", Collections.<String> emptyList());
        for (String command : commands) {
            buffer.append(command);
        }
        return buffer.toString();
    }

    /**
     * Gets the Javascript configured by the 'pastefix' parameters.<p>
     * 
     * @return the Javascript from the 'pastefix' parameters 
     */
    protected String getPasteFix() {

        boolean isPastefixEnabled = m_parameterConfiguration.getBoolean(KEY_PASTE_FIX_ENABLED, false);
        if (!isPastefixEnabled) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        String validChildren = m_parameterConfiguration.getString(KEY_PASTE_FIX_VALID_CHILDREN, "");
        String condition = m_parameterConfiguration.getString(KEY_PASTE_FIX_CONDITION, "true");
        buffer.append("  if (" + condition + ") {\n");
        buffer.append("    options.valid_children =  " + quote(validChildren) + ";\n");
        buffer.append("  }\n");
        return buffer.toString();
    }

    /**
     * Loads the configuration data from a VFS resource.<p>
     * 
     * @param cms the CMS context to use 
     * @param rootPath the path of the configuration file 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void load(CmsObject cms, String rootPath) throws CmsException {

        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("");
        try {
            CmsResource configResource = cms.readResource(rootPath);
            CmsFile configFile = cms.readFile(configResource);
            byte[] configContent = configFile.getContents();
            try {
                m_parameterConfiguration.load(new ByteArrayInputStream(configContent));
            } catch (IOException e) {
                // should never happen; log anyway 
                LOG.error(e.getLocalizedMessage(), e);
            }
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

}
