/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsLinkManager.java,v $
 * Date   : $Date: 2003/08/06 16:32:48 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.util.LinkSubstitution;
import com.opencms.util.Utils;

import java.util.Vector;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

/**
 * Does the link replacement for the &lg;link&gt; tags.<p> 
 *
 * Since this functionality is closley related to the static export,
 * this class resides in the static export package.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public final class CmsLinkManager {

    /** Provides Perl style regular expression functionality */
    private static Perl5Util m_perlUtil = new Perl5Util();
    
    /**
     * Hides the public constructor.<p>
     */
    private CmsLinkManager() { }

    /**
     * Replaces the link according to the configured rules and registers it to the
     * request context during static export.<p>
     * 
     * @param cms the cms context
     * @param link the link to process
     * @return the substituted link
     */
    public static String substituteLink(CmsObject cms, String link) {
        if (link == null || "".equals(link)) {
            return "";
        }
        if (!link.startsWith("/")) {
            // this is a relative link, lets make an absolute out of it
            link = Utils.mergeAbsolutePath(cms.getRequestContext().getRequest().getRequestedResource(), link);
        }
        String linkparam = link;
        link = cms.getRequestContext().addSiteRoot(link);
        String siteRoot = cms.getRequestContext().getAdjustedFullSiteRoot(link);
        // first ask if this is the export
        int modus = cms.getMode();
        if (modus == I_CmsConstants.C_MODUS_EXPORT) {
            // we have to register this link to the request context
            cms.getRequestContext().addLink(linkparam);
            // and we have to process the startrule
            String startRule = A_OpenCms.getStaticExportProperties().getStartRule();
            if (startRule != null && !"".equals(startRule)) {
                try {
                    link = m_perlUtil.substitute(startRule, link);
                } catch (Exception e) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[LinkSubstitution.getLinkSubstitution()/1] problems with startrule:\"" + startRule + "\" (" + e + "). ");
                    }
                }
            }
        }
    
        String[] rules = A_OpenCms.getStaticExportProperties().getLinkRules(modus);
        if (rules == null || rules.length == 0) {
            return cms.getRequestContext().removeSiteRoot(link);
        }
        String retValue = link;
        for (int i = 0; i < rules.length; i++) {
            try {
                boolean nextRule = true;
                if ("*dynamicRules*".equals(rules[i])) {
                    // here we go trough our dynamic rules
                    Vector booleanReplace = new Vector();
                    retValue = CmsStaticExport.handleDynamicRules(cms, link, modus, booleanReplace);
                    Boolean goOn = (Boolean)booleanReplace.firstElement();
                    if (goOn.booleanValue()) {
                        link = retValue;
                    } else {
                        nextRule = false;
                    }
                } else {
                    StringBuffer result = new StringBuffer();
                    int matches = m_perlUtil.substitute(result, rules[i], link);
                    if (matches != 0) {
                        retValue = result.toString();
                        nextRule = false;
                    }
                }
                if (!nextRule) {
                    // found the match
                    if (A_OpenCms.getStaticExportProperties().relativLinksInExport() && modus == I_CmsConstants.C_MODUS_EXPORT && (retValue != null) && retValue.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
                        // we want the path relative
                        retValue = LinkSubstitution.getRelativePath(cms.getRequestContext().getRequest().getRequestedResource(), retValue.substring((A_OpenCms.getStaticExportProperties().getExportPrefix()).length()));
                    }
                    // HACK: The site root must be removed, but the servlet context might have been appended as prefix
                    if (!retValue.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
                        // this is not an exported file 
                        int pos = retValue.indexOf(siteRoot);
                        if (pos >= 0) {
                            retValue = retValue.substring(0, pos) + retValue.substring(pos + siteRoot.length());
                        }
                    }
                    return retValue;
                }
            } catch (MalformedPerl5PatternException e) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[LinkSubstitution.getLinkSubstitution()/2] problems with rule:\"" + rules[i] + "\" (" + e + "). ");
                }
            }
        }
        if (A_OpenCms.getStaticExportProperties().relativLinksInExport() && modus == I_CmsConstants.C_MODUS_EXPORT && (retValue != null) && retValue.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
            // we want the path relative
            retValue = LinkSubstitution.getRelativePath(cms.getRequestContext().getRequest().getRequestedResource(), retValue.substring((A_OpenCms.getStaticExportProperties().getExportPrefix()).length()));
        }
        // HACK: The site root must be removed, but the servlet context might have been appended as prefix
        if (!retValue.startsWith(A_OpenCms.getStaticExportProperties().getExportPrefix())) {
            // this is not an exported file 
            int pos = retValue.indexOf(siteRoot);
            if (pos >= 0) {
                retValue = retValue.substring(0, pos) + retValue.substring(pos + siteRoot.length());
            }
        }
        return retValue;
    }

}