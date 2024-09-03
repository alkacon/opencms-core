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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A lookup table used to find out which VFS based message bundles (XML/property bundles) contain a given key.
 */
public class CmsMessageToBundleIndex {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageToBundleIndex.class);

    /** The internal map used to store the bundle information for each key. */
    private Multimap<String, CmsVfsBundleParameters> m_map = HashMultimap.create();

    /**
     * Creates a new instance that will read its data from a given set of bundles.
     *
     * @param bundleNames the set of bundle names from which to read the information
     * @param locale the locale to use
     */
    public CmsMessageToBundleIndex(Collection<String> bundleNames, Locale locale) {

        for (String bundleName : bundleNames) {
            try {
                ResourceBundle bundle = CmsResourceBundleLoader.getBundle(bundleName, locale);
                if (bundle instanceof CmsVfsResourceBundle) {
                    CmsVfsResourceBundle vfsBundle = (CmsVfsResourceBundle)bundle;
                    CmsVfsBundleParameters parameters = vfsBundle.getParameters();
                    for (String key : bundle.keySet()) {
                        m_map.put(key, parameters);
                    }
                }
            } catch (MissingResourceException e) {
                LOG.debug("missing resource for " + bundleName + ":" + e.getMessage(), e);
            }
        }
    }

    /**
     * Reads the bundle information for the whole system.
     *
     * <p>This uses the request context locale of the CmsObject passed as an argument.
     *
     * @param cms the CMS context to use
     * @return the bundle information
     * @throws CmsException if something goes wrong
     */
    public static CmsMessageToBundleIndex read(CmsObject cms) throws CmsException {

        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        List<CmsResource> resources = new ArrayList<>();
        for (String typeName : Arrays.asList(
            CmsVfsBundleManager.TYPE_XML_BUNDLE,
            CmsVfsBundleManager.TYPE_PROPERTIES_BUNDLE)) {
            try {
                I_CmsResourceType xmlType = OpenCms.getResourceManager().getResourceType(typeName);
                resources.addAll(cms.readResources("/", CmsResourceFilter.ALL.addRequireType(xmlType), true));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        Set<String> bundleNames = resources.stream().map(
            res -> CmsVfsBundleManager.getNameAndLocale(res).getName()).collect(Collectors.toSet());
        CmsMessageToBundleIndex result = new CmsMessageToBundleIndex(bundleNames, cms.getRequestContext().getLocale());
        return result;
    }

    /**
     * Gets the root path of the bundle file for the given message key.
     *
     * <p>If no bundle is found, null is returned.
     *
     * @param key the message key
     * @return the bundle root path
     */
    public Collection<String> getBundlesPathForKey(String key) {

        Collection<CmsVfsBundleParameters> params = m_map.get(key);
        return params.stream().map(CmsVfsBundleParameters::getBasePath).collect(Collectors.toSet());
    }

}
