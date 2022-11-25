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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

/**
 * Tests the OpenCms XML content validation with regex rules<p>
 */
public class TestCmsXmlContentValidation extends OpenCmsTestCase {
	final String SCHEMA_SYSTEM_ID = "dummy://xmlcontent-definition-testregex.xsd";

	/**
	 * Default JUnit constructor.<p>
	 *
	 * @param arg0 JUnit parameters
	 */
	public TestCmsXmlContentValidation(String arg0) {

		super(arg0);
	}

	/**
	 * Test suite for this test class.<p>
	 *
	 * @return the test suite
	 */
	public static Test suite() {

		OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

		TestSuite suite = new TestSuite();
		suite.setName(TestCmsXmlContentValidation.class.getName());

		suite.addTest(new TestCmsXmlContentValidation("testHandlingOfPatternSyntaxExceptionDuringValidation"));
		suite.addTest(new TestCmsXmlContentValidation("testHandlingOfStackOverflowErrorDuringValidation"));

		TestSetup wrapper = new TestSetup(suite) {

			@Override
			protected void setUp() {

				setupOpenCms("simpletest", "/");
			}

			@Override
			protected void tearDown() {

				removeOpenCms();
			}
		};

		return wrapper;
	}

	public void testHandlingOfStackOverflowErrorDuringValidation() throws Exception {
		final CmsXmlContentErrorHandler validationResult = validateXmlFile("org/opencms/xml/content/xmlcontent-definition-evilregex.xsd");

		assertEquals("Number of registered errors", 1, validationResult.getErrors().size());
		final String recordedError = validationResult.getErrors(Locale.ENGLISH).get("String[1]");
		final String expectedMessage = Messages.get().getBundle(Locale.ENGLISH).key(Messages.GUI_EDITOR_XMLCONTENT_CANNOT_VALIDATE_ERROR_3).split("\\{")[0];
		assertTrue("Expected error during validation not registered in the error handler. Recorded error: '" + recordedError + "'. Expected message: '" + expectedMessage + "'",
				recordedError.contains(expectedMessage));
	}

	public void testHandlingOfPatternSyntaxExceptionDuringValidation() throws Exception {
		final CmsXmlContentErrorHandler validationResult = validateXmlFile("org/opencms/xml/content/xmlcontent-definition-malformedregex.xsd");

		assertEquals("Number of registered errors", 1, validationResult.getErrors().size());
		final String recordedError = validationResult.getErrors(Locale.ENGLISH).get("String[1]");
		final String expectedMessage = Messages.get().getBundle(Locale.ENGLISH).key(Messages.GUI_EDITOR_XMLCONTENT_INVALID_RULE_3).split("\\{")[0];
		assertTrue("Expected error during validation not registered in the error handler. Recorded error: '" + recordedError + "'. Expected message: '" + expectedMessage + "'",
				recordedError.contains(expectedMessage));
	}

	private CmsXmlContentErrorHandler validateXmlFile(String SCHEMA_FILENAME) throws Exception {
		echo("Testing the handling of errors during validation using schema " + SCHEMA_FILENAME);
		CmsObject cms = getCmsObject();
		CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

		cacheSchema(resolver, SCHEMA_SYSTEM_ID, SCHEMA_FILENAME);

		// now read the XML content
		String content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-1-mod7.xml", CmsEncoder.ENCODING_UTF_8);
		CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

		// validate the XML structure
		final CmsXmlContentErrorHandler validationResult = xmlcontent.validate(getCmsObject());
		assertFalse("Warning were recorded but not expected. " + validationResult.getWarnings(Locale.ENGLISH), validationResult.hasWarnings());
		return validationResult;
	}

	/**
	 * Updates the OpenCms XML entity resolver cache with a changed XML schema id.<p>
	 *
	 * @param resolver the OpenCms XML entity resolver to use
	 * @param id       the XML schema id to update in the resolver
	 * @param filename the name of the file in the RFS where to read the new schema content from
	 * @throws Exception if something goes wrong
	 */
	private void cacheSchema(CmsXmlEntityResolver resolver, String id, String filename) throws Exception {

		// fire "clear cache" event to clear up previously cached schemas
		OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
		// read the XML from the given file and store it in the resolver
		String content = CmsFileUtil.readFile(filename, CmsEncoder.ENCODING_UTF_8);
		CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, id, resolver);
		System.out.println(definition.getSchema().asXML());
		CmsXmlEntityResolver.cacheSystemId(id, definition.getSchema().asXML().getBytes(StandardCharsets.UTF_8));
	}
}