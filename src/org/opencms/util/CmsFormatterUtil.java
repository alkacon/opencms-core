/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsFormatterUtil.java,v $
 * Date   : $Date: 2010/11/04 12:28:34 $
 * Version: $Revision: 1.2 $
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

package org.opencms.util;

import org.opencms.main.CmsLog;
import org.opencms.xml.containerpage.CmsFormatterConfigBean;
import org.opencms.xml.content.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * This class contains utility methods for dealing with formatter configurations.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsFormatterUtil {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterUtil.class);

    /**
     * Hidden default constructor.<p>
     */
    private CmsFormatterUtil() {

        // do nothing
    }

    /**
     * Converts a list of formatter configuration beans into two maps, one containing 
     * type based formatters, and the other containing width-based formatters.<p>
     * 
     * @param beans the list of formatter configuration beans 
     * @param location the location of the file containing the formatter configuration (used for error messages)
     *  
     * @return a pair of formatter maps 
     */
    public static CmsPair<Map<String, String>, Map<Integer, String>> getFormatterMapsFromConfigBeans(
        List<CmsFormatterConfigBean> beans,
        String location) {

        Map<String, String> formatters = new HashMap<String, String>();
        Map<Integer, String> widthFormatters = new HashMap<Integer, String>();
        for (CmsFormatterConfigBean configBean : beans) {
            String type = configBean.getType();
            String uri = configBean.getJsp();
            String widthStr = configBean.getWidth();
            String oldUri = null;
            Object key = null;
            if (type.equals("*") || CmsStringUtil.isEmptyOrWhitespaceOnly(type)) {
                // wildcard formatter; index by width
                // if no width available, use -1
                int width = -1;
                try {
                    width = Integer.parseInt(widthStr);
                } catch (NumberFormatException e) {
                    //ignore; width will be -1 
                }
                key = new Integer(width);
                oldUri = widthFormatters.get(key);
                widthFormatters.put((Integer)key, uri);
            } else {
                key = type;
                oldUri = formatters.get(key);
                formatters.put((String)key, uri);
            }
            if (oldUri != null) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_CONTENT_DEFINITION_DUPLICATE_FORMATTER_4,
                    new Object[] {key, oldUri, uri, location}));
            }
        }
        return CmsPair.create(formatters, widthFormatters);
    }

    /**
     * Selects the correct formatter from maps of type-based and width-based formatters
     * based on a container type and width.<p>
     * 
     * This method first tries to find the formatter for the container type. 
     * If this fails, it returns the formatter with the largest width less than the 
     * container width; if the container width is negative, the formatter with the
     * largest width will be returned.
     * 
     * @param typeFormatters the map of type based formatters 
     * @param widthFormatters the map of width based formatters 
     * @param containerType the container type 
     * @param containerWidth the container width
     *  
     * @return the correct formatter, or null if none was found 
     */
    public static String selectFormatter(
        Map<String, String> typeFormatters,
        Map<Integer, String> widthFormatters,
        String containerType,
        int containerWidth) {

        String result = typeFormatters.get(containerType);
        if (result != null) {
            return result;
        }
        if (containerWidth < 0) {
            // interpret negative container size as 'unlimited' 
            containerWidth = Integer.MAX_VALUE;
        }
        List<Integer> keysLessThanMaxWidth = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> entry : widthFormatters.entrySet()) {
            Integer key = entry.getKey();
            if (key.intValue() <= containerWidth) {
                keysLessThanMaxWidth.add(key);
            }
        }
        if (keysLessThanMaxWidth.isEmpty()) {
            return null;
        }
        Integer maxKey = Collections.max(keysLessThanMaxWidth);
        return widthFormatters.get(maxKey);
    }

}
