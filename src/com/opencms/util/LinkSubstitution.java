/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/util/Attic/LinkSubstitution.java,v $
* Date   : $Date: 2001/11/20 10:06:10 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.util;

import com.opencms.file.CmsObject;
import com.opencms.core.*;
import org.apache.oro.text.perl.*;

/**
 * Title:        OpenCms
 * Description:
 * @author Hanjo Riege
 * @version 1.0
 */

public class LinkSubstitution {

    /**
     * Reference to the CmsElementCache object containing locators for all
     * URIs and elements in cache.
     */
    private static Perl5Util c_perlUtil = null;

    public LinkSubstitution() {
        c_perlUtil = new Perl5Util();
    }

    public String substituteEditorContent(String content)throws CmsException{
        return content;
    }
    /**
     * Replaces the link according to the rules and registers it to the
     * requestcontex if we are in export modus.
     * @param cms. The cms object.
     * @param link. The link to process.
     * @return String The substituded link.
     */
    public String getLinkSubstitution(CmsObject cms, String link){
        if(link == null || "".equals(link)){
            return "";
        }
        // first ask if this is the export
        int modus = cms.getMode();
        if(modus == cms.C_MODUS_EXPORT){
            // we have to register this link to the requestcontex
            cms.getRequestContext().addLink(link);
            // and we have to process the startrule
            String startRule = OpenCms.getLinkRuleStart();
            if(startRule != null && !"".equals(startRule)){
                try{
                    link = c_perlUtil.substitute(startRule, link);
                }catch(Exception e){
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with startrule:\""+startRule+"\" (" + e + "). ");
                    }
                }
            }
        }
        String[] rules = cms.getLinkRules(modus);
        if(rules == null || rules.length == 0){
            return link;
        }
        String retValue = link;
        for(int i=0; i<rules.length; i++){
            try{
                retValue = c_perlUtil.substitute(rules[i], link);
                if(!link.equals(retValue)){
                    // found the match
                    return retValue;
                }
            }catch(MalformedPerl5PatternException e){
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "["+this.getClass().getName()+"] problems with rule:\""+rules[i]+"\" (" + e + "). ");
                }
            }
        }

        return retValue;
    }
}