/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package com.cybozu.labs.langdetect;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;

import com.cybozu.labs.langdetect.util.LangProfile;

/**
 * Language detection wrapper.<p>
 */
public final class CmsLanguageUtil {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLanguageUtil.class);

    /**
     * Hide the constructor for Utility class.<p>
     */
    private CmsLanguageUtil() {

        // noop
    }

    /**
     * Loads language profiles from a referenced ZIP.<p>
     * 
     * @param cms the cms object
     * @param path the path to the language profiles ZIP in the VFS
     * 
     * @throws LangDetectException if something goes wrong
     */
    public static void loadProfile(CmsObject cms, String path) throws LangDetectException {

        CmsFile file = null;
        try {
            file = cms.readFile(path);
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }

        if (file != null) {
            ZipInputStream zipInput = new ZipInputStream(new ByteArrayInputStream(file.getContents()));
            List<LangProfile> profiles = new ArrayList<LangProfile>();
            try {
                while (true) {
                    // handle the single entries ...
                    ZipEntry entry = zipInput.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    if (entry.isDirectory() || entry.getName().startsWith(".")) {
                        // handle files only
                        continue;
                    }
                    ByteArrayInputStream is = null;
                    try {
                        byte[] content;
                        int fileByteSize = Long.valueOf(entry.getSize()).intValue();
                        if (fileByteSize == -1) {
                            content = CmsFileUtil.readFully(zipInput, false);
                        } else {
                            content = CmsFileUtil.readFully(zipInput, fileByteSize, false);
                        }
                        is = new ByteArrayInputStream(content);
                        LangProfile profile = JSON.decode(is, LangProfile.class);
                        profiles.add(profile);
                    } catch (JSONException e) {
                        throw new LangDetectException(ErrorCode.FormatError, "profile format error in '"
                            + file.getName()
                            + "'");
                    } catch (IOException e) {
                        throw new LangDetectException(ErrorCode.FileLoadError, "can't open '" + file.getName() + "'");
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException e) {
                            // noop
                        }
                    }
                    // close the entry ...
                    zipInput.closeEntry();
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                try {
                    zipInput.close();
                } catch (IOException e) {
                    // noop
                }
            }

            if (!profiles.isEmpty()) {
                DetectorFactory.clear();
                int count = 0;
                int langsize = profiles.size();
                for (LangProfile profile : profiles) {
                    DetectorFactory.addProfile(profile, count, langsize);
                    count++;
                }
            }
        }
    }
}
