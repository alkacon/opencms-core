/*
 * File   : $Source: /alkacon/cvs/opencms/test/com/opencms/flex/util/Attic/CmsStringSubstitutionTest.java,v $
 * Date   : $Date: 2003/02/11 17:40:52 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2003  The OpenCms Group
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
 *
 * First created on 11.02.2003
 */

package com.opencms.flex.util;

import com.opencms.flex.util.CmsStringSubstitution;

import junit.framework.TestCase;

/** 
 * Test cases for the class "CmsStringSubstitution"
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.0
 */
public class CmsStringSubstitutionTest extends TestCase {

	/**
	 * Constructor for CmsStringSubstitutionTest.
	 * @param arg0
	 */
	public CmsStringSubstitutionTest(String arg0) {
		super(arg0);
	}

	public void testSubstitute() {
		String test;
		String content = "<a href=\"/opencms/opencms/test.jpg\">";
		String search = "/opencms/opencms/";
		String replace = "\\${path}";
		
		test = CmsStringSubstitution.substitute(content, search, replace);
		assertEquals(test,"<a href=\"${path}test.jpg\">");
		
		test = CmsStringSubstitution.substitute(test, replace, search);	
		assertEquals(test,"<a href=\"/opencms/opencms/test.jpg\">");
	}

	public void testEscapePattern() {
		String test;
		test = CmsStringSubstitution.escapePattern("/opencms/opencms");
		assertEquals(test, "\\/opencms\\/opencms");	
		test = CmsStringSubstitution.escapePattern("/opencms/$");
		assertEquals(test, "\\/opencms\\/\\$");			
	}
	
	public void testCombined() {
		String test;
		String content = "<p>A paragraph with text...<img src=\"/opencms/opencms/empty.gif\"></p>\n<a href=\"/opencms/opencms/test.jpg\">";
		String search = "/opencms/opencms/";
		String replace = "${path}";
		test = CmsStringSubstitution.substitute(content, CmsStringSubstitution.escapePattern(search), CmsStringSubstitution.escapePattern(replace));	
		assertEquals(test,"<p>A paragraph with text...<img src=\"${path}empty.gif\"></p>\n<a href=\"${path}test.jpg\">");
		test = CmsStringSubstitution.substitute(test, CmsStringSubstitution.escapePattern(replace), CmsStringSubstitution.escapePattern(search));;		
		assertEquals(test,"<p>A paragraph with text...<img src=\"/opencms/opencms/empty.gif\"></p>\n<a href=\"/opencms/opencms/test.jpg\">");
	}

}
