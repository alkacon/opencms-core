/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsStringSubstitution.java,v $
 * Date   : $Date: 2003/11/03 09:05:53 $
 * Version: $Revision: 1.2 $
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

package org.opencms.util;

import com.opencms.workplace.I_CmsWpConstants;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

/**
 * Provides a String substitution functionality
 * with Perl regular expressions.<p>
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.0
 */
public final class CmsStringSubstitution {

    /** DEBUG flag */
    private static final int DEBUG = 0;
    
    // static members, will only be initilized once, for performance reasons    
    private static String contextSearch = null;
    private static String contextReplace = null;
        
    /** 
     * Default constructor (empty), private because this class has only 
     * static methods.<p>
     */
    private CmsStringSubstitution() {
        // empty
    }
    
    /**
     * Substitutes searchString in content with replaceItem.<p>
     * 
     * @param content the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceItem the new String which replaces searchString
     * @return String the substituted String
     */
    public static String substitute(String content, String searchString, String replaceItem) {
        // high performance implementation to avoid regular expression overhead
        int findLength;
        if (content == null) {
            return null;
        }
        int stringLength = content.length();
        if (searchString == null || (findLength = searchString.length()) == 0) {
            return content;
        }
        if (replaceItem == null) {
            replaceItem = "";
        }
        int replaceLength = replaceItem.length();
        int length;
        if (findLength == replaceLength) {
            length = stringLength;
        } else {
            int count;
            int start;
            int end;
            count = 0;
            start = 0;
            while ((end = content.indexOf(searchString, start)) != -1) {
                count++;
                start = end + findLength;
            }
            if (count == 0) {
                return content;
            }
            length = stringLength - (count * (findLength - replaceLength));
        }
        int start = 0;
        int end = content.indexOf(searchString, start);
        if (end == -1) {
            return content;
        }
        StringBuffer sb = new StringBuffer(length);
        while (end != -1) {
            sb.append(content.substring(start, end));
            sb.append(replaceItem);
            start = end + findLength;
            end = content.indexOf(searchString, start);
        }
        end = stringLength;
        sb.append(content.substring(start, end));
        return sb.toString();
    }
    
    /**
     * Substitutes searchString in content with replaceItem.<p>
     * 
     * @param content the content which is scanned
     * @param searchString the String which is searched in content
     * @param replaceItem the new String which replaces searchString
     * @param occurences must be a "g" if all occurences of searchString shall be replaced
     * @return String the substituted String
     */
    public static String substitutePerl(String content, String searchString, String replaceItem, String occurences) {
        String translationRule = "s#" + searchString + "#" + replaceItem + "#" + occurences;
        Perl5Util perlUtil = new Perl5Util();
        try {
            return perlUtil.substitute(translationRule, content);
        } catch (MalformedPerl5PatternException e) {
            if (DEBUG > 0)
                System.err.println("[CmsStringSubstitution]: " + e.toString());
        }
        return content;
    }
        
    /**
     * Substitutes the OpenCms context path (e.g. /opencms/opencms/) in a HTML page with a 
     * special variable so that the content also runs if the context path of the server changes.<p>
     * 
     * @param htmlContent the HTML to replace the context path in 
     * @param context the context path of the server
     * @return the HTML with the replaced context path
     */
    public static String substituteContextPath(String htmlContent, String context) {
        if (contextSearch == null) {
            contextSearch = "([^\\w/])" + context;
            contextReplace = "$1" + CmsStringSubstitution.escapePattern(I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT) + "/"; 
        }       
        return substitutePerl(htmlContent, contextSearch, contextReplace, "g");            
    }
        
    /**
     * Escapes a String so it may be used as a Perl5 regular expression.<p>
     * 
     * This method replaces the following characters in a String:<br>
     * <code>{}[]()\$^.*+/</code>
     * 
     * 
     * @param source the string to escape
     * @return the escaped string
     */
    public static String escapePattern(String source) {
        if (DEBUG > 0)
            System.err.println("[CmsStringSubstitution]: escaping String: " + source);
        if (source == null)
            return null;
        StringBuffer result = new StringBuffer(source.length() * 2);
        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '\\' :
                    result.append("\\\\");
                    break;
                case '/' :
                    result.append("\\/");
                    break;
                case '$' :
                    result.append("\\$");
                    break;
                case '^' :
                    result.append("\\^");
                    break;
                case '.' :
                    result.append("\\.");
                    break;
                case '*' :
                    result.append("\\*");
                    break;
                case '+' :
                    result.append("\\+");
                    break;
                case '|' :
                    result.append("\\|");
                    break;
                case '?' :
                    result.append("\\?");
                    break;
                case '{' :
                    result.append("\\{");
                    break;
                case '}' :
                    result.append("\\}");
                    break;
                case '[' :
                    result.append("\\[");
                    break;
                case ']' :
                    result.append("\\]");
                    break;
                case '(' :
                    result.append("\\(");
                    break;
                case ')' :
                    result.append("\\)");
                    break;
                default :
                    result.append(ch);
            }
        }
        if (DEBUG > 0)
            System.err.println("[CmsStringSubstitution]: escaped String to: " + result.toString());
        return new String(result);
    }
}
