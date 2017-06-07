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

package org.opencms.ugc;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;

/**
 * Parser for form configuration files.<p>
 */
public class CmsUgcConfigurationReader {

    /** XML content value name. */
    public static final String N_CONTENT_TYPE = "ContentType";

    /** XML content value name. */
    public static final String N_CONTENT_PATH = "ContentPath";

    /** XML content value name. */
    public static final String N_NAME_PATTERN = "NamePattern";

    /** XML content value name. */
    public static final String N_LOCALE = "Locale";

    /** XML content value name. */
    public static final String N_UPLOAD_PATH = "UploadPath";

    /** XML content value name. */
    public static final String N_MAX_UPLOAD_SIZE = "MaxUploadSize";

    /** XML content value name. */
    public static final String N_MAX_NUM_CONTENTS = "MaxNumContents";

    /** XML content value name. */
    public static final String N_QUEUE_WAIT_TIME = "QueueWaitTime";

    /** XML content value name. */
    public static final String N_QUEUE_MAX_LENGTH = "QueueMaxLength";

    /** XML content value name. */
    public static final String N_AUTO_PUBLISH = "AutoPublish";

    /** XML content value name. */
    public static final String N_VALID_EXTENSIONS = "ValidExtensions";

    /** XML content value name. */
    public static final String N_USER_FOR_GUEST = "UserForGuest";

    /** XML content value name. */
    public static final String N_PROJECT_GROUP = "ProjectGroup";

    /** The CMS context used by this configuration reader. */
    private CmsObject m_cms;

    /** The XML content from which the configuration is read. */
    private CmsXmlContent m_content;

    /** The separator used for separating the configured valid file name extensions for uploaded files. */
    public static final String EXTENSIONS_SEPARATOR = ";";

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsUgcConfigurationReader(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Reads the given configuration file.<p>
     *
     * @param configFile the configuration file to read
     * @return the configuration data read from the file
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUgcConfiguration readConfiguration(CmsFile configFile) throws CmsException {

        m_content = CmsXmlContentFactory.unmarshal(m_cms, configFile);
        String resourceType = getStringValue(N_CONTENT_TYPE);
        String contentPath = getStringValue(N_CONTENT_PATH);
        CmsResource contentPathResource = m_cms.readResource(contentPath);
        String namePattern = getStringValue(N_NAME_PATTERN);
        String localeStr = getStringValue(N_LOCALE);
        OpenCms.getLocaleManager();
        Locale locale = CmsLocaleManager.getLocale(localeStr);
        Optional<CmsResource> uploadPathResource = Optional.absent();
        String uploadPath = getStringValue(N_UPLOAD_PATH);
        if (uploadPath != null) {
            uploadPathResource = Optional.of(m_cms.readResource(uploadPath));
        }
        Optional<Long> maxUploadSize = getLongValue(N_MAX_UPLOAD_SIZE);
        Optional<Integer> maxNumContents = getIntValue(N_MAX_NUM_CONTENTS);
        Optional<Long> queueWaitTime = getLongValue(N_QUEUE_WAIT_TIME);
        Optional<Integer> maxQueueLength = getIntValue(N_QUEUE_MAX_LENGTH);
        boolean autoPublish = Boolean.parseBoolean(getStringValue(N_AUTO_PUBLISH));
        String validExtensionsStr = getStringValue(N_VALID_EXTENSIONS);
        Optional<List<String>> validExtensions = Optional.absent();
        if (validExtensionsStr != null) {
            validExtensionsStr = validExtensionsStr.trim();
            List<String> extensions = CmsStringUtil.splitAsList(validExtensionsStr, EXTENSIONS_SEPARATOR);
            validExtensions = Optional.of(extensions);
        }

        String userForGuestStr = getStringValue(N_USER_FOR_GUEST);
        Optional<CmsUser> userForGuest = Optional.absent();
        if (userForGuestStr != null) {
            userForGuest = Optional.of(m_cms.readUser(userForGuestStr.trim()));
        }

        String projectGroupStr = getStringValue(N_PROJECT_GROUP);
        CmsGroup projectGroup = m_cms.readGroup(projectGroupStr.trim());
        CmsUUID id = configFile.getStructureId();
        CmsUgcConfiguration result = new CmsUgcConfiguration(
            id,
            userForGuest,
            projectGroup,
            resourceType,
            contentPathResource,
            namePattern,
            locale,
            uploadPathResource,
            maxUploadSize,
            maxNumContents,
            queueWaitTime,
            maxQueueLength,
            autoPublish,
            validExtensions);
        result.setPath(configFile.getRootPath());
        return result;
    }

    /**
     * Parses an optional integer value.<p>
     *
     * @param path the xpath of the content field
     * @return the optional integer value in that field
     */
    private Optional<Integer> getIntValue(String path) {

        return longToInt(getLongValue(path));
    }

    /**
     * Parses an optional long value.<p>
     *
     * @param path the xpath of the content element
     * @return  the optional long value in that field
     */
    private Optional<Long> getLongValue(String path) {

        String stringValue = getStringValue(path);
        if (stringValue == null) {
            return Optional.absent();
        } else {
            try {
                return Optional.<Long> of(Long.valueOf(stringValue.trim()));
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Could not read a number from " + path + " ,value= " + stringValue);
            }
        }
    }

    /**
     * Reads the XML content value for a given xpath.<p>
     *
     * Returns null if no value was found at that path.<p>
     *
     * @param path the XML content xpath
     *
     * @return the value for that path as a string
     */
    private String getStringValue(String path) {

        I_CmsXmlContentValue value = m_content.getValue(path, Locale.ENGLISH);
        if (value == null) {
            return null;
        } else {
            return value.getStringValue(m_cms);
        }
    }

    /**
     * Converts an optional long value to an optional integer value.<p>
     *
     * @param optLong the optional long value
     *
     * @return the optional integer value
     */
    private Optional<Integer> longToInt(Optional<Long> optLong) {

        if (optLong.isPresent()) {
            return Optional.fromNullable((Integer.valueOf((int)optLong.get().longValue())));
        } else {
            return Optional.absent();
        }
    }
}
