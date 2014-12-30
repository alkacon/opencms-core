/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.regex.Pattern;

/** 
 * Test case for browser matcher.<p>
 * 
 * @since 6.0.0
 */
public class TestCmsBrowserMatcher extends OpenCmsTestCase {

    private static String[] browser = {
        "MSIE_6.x",
        "MSIE_5.5",
        "MSIE_5.0",
        "mozilla_1.4",
        "mozilla_1.3",
        "netscape_7.02",
        "netscape_7.0",
        "other___"};

    private static String[] sampleagent = {
        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
        "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4b) Gecko/20030507",
        "Mozilla/4.0 (compatible; MSIE 5.0; AOL 7.0; Windows 95; DigExt)",
        "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02",
        "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)",
        "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.3) Gecko/20030312",
        "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.0.1) Gecko/20020823 Netscape/7.0",
        "Mozilla/4.0 (compatible; MSIE 5.5; AOL 7.0; Windows 98)",
        "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02",
        "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT)",
        "Mozilla/4.0 (compatible; MSIE 6.0; MSN 2.6; Windows 98)",
        "Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.4a) Gecko/20030419",
        "Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.0.1) Gecko/20020921 Netscape/7.0",
        "Mozilla/5.0 (X11; U; FreeBSD i386; en-US; rv:1.3) Gecko/20030411",

    };

    // default browser match patterns (same as in "opencms.properties")
    private static String[] useragent = {
        "^Mozilla/4\\.0 \\(compatible; MSIE 6\\.\\d*; .*\\)$",
        "^Mozilla/4\\.0 \\(compatible; MSIE 5\\.[56789]\\d*; .*\\)$",
        "^Mozilla/4\\.0 \\(compatible; MSIE 5\\.[01234]\\d*; .*\\)$",
        "^Mozilla/5\\.0 \\(.* rv:1.4.*\\) Gecko/2003\\d*$",
        "^Mozilla/5\\.0 \\(.* rv:1.3\\) Gecko/2003\\d*$",
        "^Mozilla/5\\.0 \\(.* rv:1.0.2\\) Gecko/2003\\d* Netscape/.*$",
        "^Mozilla/5\\.0 \\(.* rv:1.0.1\\) Gecko/2002\\d* Netscape/.*$",
        ".*"};

    /**
     * Tests the browser matching regular expressions.<p>
     */
    public void testMatchBrowser() {

        ArrayList pattern = new ArrayList(useragent.length);
        for (int i = 0; i < useragent.length; i++) {
            pattern.add(Pattern.compile(useragent[i]));
        }
        for (int i = 0; i < sampleagent.length; i++) {
            for (int j = 0; j < pattern.size(); j++) {
                boolean matches = ((Pattern)pattern.get(j)).matcher(sampleagent[i]).matches();
                if (matches) {
                    System.out.println(browser[j] + "\t matches " + sampleagent[i]);
                    break;
                }
            }
        }
    }

    /**
     * Tests for regular expressions.<p>
     */
    public void testRegex() {

        String source = "addMenuEntry(resource_id, \"language_key(explorer.context.copy)\", \"/WORKPLACE/copy.html?initial=true\", \"''\", \"rules_key(d d aaai aaai dddd dddd dddd)\");";
        String target = "addMenuEntry(resource_id, \"language_key(explorer.context.copy)\", \"/system/workplace/jsp/copy.html?initial=true\", \"''\", \"rules_key(d d aaai aaai dddd dddd dddd)\");";
        String regex = "/WORKPLACE/";
        String replace = "/system/workplace/jsp/";

        String result = source.replaceAll(regex, replace);
        System.out.println(result);
        assertEquals(result, target);
    }

}
