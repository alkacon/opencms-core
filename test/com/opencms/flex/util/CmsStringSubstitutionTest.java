package com.opencms.flex.util;

import com.opencms.flex.util.CmsStringSubstitution;

import junit.framework.TestCase;

/** 
 * Test cases for the class "CmsStringSubstitution"
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
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

	public void testCmsStringSubstitution() {
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
