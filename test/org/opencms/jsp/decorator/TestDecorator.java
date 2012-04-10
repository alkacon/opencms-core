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

package org.opencms.jsp.decorator;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the decoration postprocessor.<p>
 * 
 * @since 6.1.3
 */
public class TestDecorator extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestDecorator(String arg0) {

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
        suite.setName(TestDecorator.class.getName());

        suite.addTest(new TestDecorator("testDecoratorBasics"));
        suite.addTest(new TestDecorator("testDecoratorConfiguration"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("decoration", "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the decoration postprocessor.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testDecoratorBasics() throws Exception {

        // define test values and results
        String inputText1 = "The CMS stores its data in the VFS and exports some of it to the RFS./r/n To export files form the VFS to the RFS you must configure your CMS correctly.";
        String resultText1 = "The <abbr title=\"Content Management System\">CMS</abbr> stores its data in the <abbr title=\"Virtual File System\">VFS</abbr> and exports some of it to the <abbr title=\"Real File System\">RFS</abbr>./r/n To export files form the <abbr>VFS</abbr> to the <abbr>RFS</abbr> you must configure your <abbr>CMS</abbr> correctly.";

        String inputText2 = "<h1>The CMS</h1>The CMS stores its data in the <a href=\"#\">VFS</a> and exports some of it to the RFS./r/n To export files form the VFS to the RFS you must configure your CMS correctly.";
        String resultText2 = "<h1>The <abbr title=\"Content Management System\">CMS</abbr></h1>The <abbr>CMS</abbr> stores its data in the <a href=\"#\"><abbr title=\"Virtual File System\">VFS</abbr></a> and exports some of it to the <abbr title=\"Real File System\">RFS</abbr>./r/n To export files form the <abbr>VFS</abbr> to the <abbr>RFS</abbr> you must configure your <abbr>CMS</abbr> correctly.";

        String inputText3 = "This is a test for VFS&nbsp;RFS and VFS.";
        String resultText3 = "This is a test for <abbr>VFS</abbr>&nbsp;<abbr>RFS</abbr> and <abbr>VFS</abbr>.";

        String inputText4 = "This is a test German Umlaute: T"
            + C_UUML_UPPER
            + "V is fine, but does T&UumlV work as well?";
        String resultText4 = "This is a test German Umlaute: <abbr title=\"Technischer "
            + C_UUML_UPPER
            + "berwachungsverein\">T"
            + C_UUML_UPPER
            + "V</abbr> is fine, but does <abbr>T"
            + C_UUML_UPPER
            + "V</abbr> work as well?";

        String inputText5 = "The CMS has a nice user interface, the so called CMS-UI . This must not be mixed up with the CMS-IU!";
        String resultText5 = "The <abbr>CMS</abbr> has a nice user interface, the so called <abbr title=\"Content Management System User Interface\">CMS-UI</abbr> . This must not be mixed up with the <abbr>CMS</abbr>-IU!";

        String inputText6 = "Does it work to use a '?' as a delimiter for the CMS?";
        String resultText6 = "Does it work to use a '?' as a delimiter for the <abbr>CMS</abbr>?";

        String inputText6a = "This is VFS@CMS.";
        String resultText6a = "This is <abbr>VFS</abbr>@<abbr>CMS</abbr>.";

        String inputText6b = "This is ABC@DEF.";
        String resultText6b = "This is <abbr title=\"A strange thing with a @ in it\">ABC@DEF</abbr>.";

        String inputText7 = "<Strong>CMS</strong>. Will it be decorated? <h1>VFS</h1>: Is very nice!";
        String resultText7 = "<Strong><abbr>CMS</abbr></strong>. Will it be decorated? <h1><abbr>VFS</abbr></h1>: Is very nice!";

        String inputText7a = "Test with a dash - the dashes should be still there - even if I write RFS.";
        String resultText7a = "Test with a dash - the dashes should be still there - even if I write <abbr>RFS</abbr>.";

        String inputText8 = "Hello, my name is Dr. Bull!";
        String resultText8 = "Hello, my name is <abbr title=\"Doctor\">Dr.</abbr> Bull!";

        String inputText8a = "<p>Dr. Dr.</p>";
        String resultText8a = "<p><abbr>Dr.</abbr> <abbr>Dr.</abbr></p>";

        String inputText9 = "Do we find words with multiple lookups, e.g. CMS?";
        String resultText9 = "Do we find words with multiple lookups, <abbr title=\"example given\">e.g.</abbr> <abbr>CMS</abbr>?";

        String inputText9a = "Another example: z. B. is very nice.";
        String resultText9a = "Another example: <abbr title=\"zum Beispiel\">z. B.</abbr> is very nice.";

        String inputText10 = "Let's see: ZB MED - a CMS decoration in the VFS with a blank in it.";
        String resultText10 = "Let's see: <abbr title=\"a decoarion with a blank in it\">ZB MED</abbr> - a <abbr>CMS</abbr> decoration in the <abbr>VFS</abbr> with a blank in it.";

        String inputText10a = "Let's see: ZB&nbsp;MED - a CMS decoration in the VFS with a blank in it.";
        String resultText10a = "Let's see: <abbr>ZB&nbsp;MED</abbr> - a <abbr>CMS</abbr> decoration in the <abbr>VFS</abbr> with a blank in it.";

        String inputText11 = "This is a word inside quotations: \"CMS\".";
        String resultText11 = "This is a word inside quotations: \"<abbr>CMS</abbr>\".";

        String inputText11a = "This is a word inside quotations: &quot;CMS&quot;.";
        String resultText11a = "This is a word inside quotations: &quot;<abbr>CMS</abbr>&quot;.";

        String preTextFirst = "<abbr title=\"${decoration}\">";
        String postTextFirst = "</abbr>";
        String preText = "<abbr>";
        String postText = "</abbr>";

        System.out.println("Testing the OpenCms decorator.");

        // add a DecorationDefintinion
        CmsDecorationDefintion decDef1 = new CmsDecorationDefintion();
        decDef1.setMarkFirst(true);
        decDef1.setPreTextFirst(preTextFirst);
        decDef1.setPostTextFirst(postTextFirst);
        decDef1.setPreText(preText);
        decDef1.setPostText(postText);
        decDef1.setName("Abbr-Decoration");

        Locale locale = new Locale("en");
        // add some text decorations to the decoration map
        CmsDecorationBundle decorationMap = new CmsDecorationBundle();
        decorationMap.put("CMS", new CmsDecorationObject("CMS", "Content Management System", decDef1, locale));
        decorationMap.put("VFS", new CmsDecorationObject("VFS", "Virtual File System", decDef1, locale));
        decorationMap.put("RFS", new CmsDecorationObject("RFS", "Real File System", decDef1, locale));
        decorationMap.put("T" + C_UUML_UPPER + "V", new CmsDecorationObject("T" + C_UUML_UPPER + "V", "Technischer "
            + C_UUML_UPPER
            + "berwachungsverein", decDef1, locale));
        decorationMap.put("Dr.", new CmsDecorationObject("Dr.", "Doctor", decDef1, locale));
        decorationMap.put("z. B.", new CmsDecorationObject("z. B.", "zum Beispiel", decDef1, locale));
        decorationMap.put("e.g.", new CmsDecorationObject("e.g.", "example given", decDef1, locale));
        decorationMap.put(
            "ZB MED",
            new CmsDecorationObject("ZB MED", "a decoarion with a blank in it", decDef1, locale));
        decorationMap.put("ZB&nbsp;MED", new CmsDecorationObject(
            "ZB&nbsp;MED",
            "a decoarion with a blank in it",
            decDef1,
            locale));
        decorationMap.put("CMS-UI", new CmsDecorationObject(
            "CMS-UI",
            "Content Management System User Interface",
            decDef1,
            locale));
        decorationMap.put("ABC@DEF", new CmsDecorationObject(
            "ABC@DEF",
            "A strange thing with a @ in it",
            decDef1,
            locale));

        // create a decorator  configuration
        CmsDecoratorConfiguration configuration = new CmsDecoratorConfiguration(getCmsObject());
        configuration.setDecorations(decorationMap);

        // create the decorator
        CmsHtmlDecorator processor = new CmsHtmlDecorator(getCmsObject(), configuration);
        processor.resetDecorationDefinitions();

        // excecute the tests
        System.out.println("Testing decoration");
        String result = processor.doDecoration(inputText1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText1);
        System.out.println(result);
        assertEquals(resultText1, result);
        System.out.println("");

        processor.resetDecorationDefinitions();

        System.out.println("Testing decoration in HTML");
        result = processor.doDecoration(inputText2, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText2);
        System.out.println(result);
        assertEquals(resultText2, result);
        System.out.println("");

        System.out.println("Testing decoration with nbsp");
        result = processor.doDecoration(inputText3, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText3);
        System.out.println(result);
        assertEquals(resultText3, result);
        System.out.println("");

        System.out.println("Testing decoration with Umlaute");
        result = processor.doDecoration(inputText4, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText4);
        System.out.println(result);
        assertEquals(resultText4, result);
        System.out.println("");

        System.out.println("Testing decoration with composed words");
        result = processor.doDecoration(inputText5, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText5);
        System.out.println(result);
        assertEquals(resultText5, result);
        System.out.println("");

        System.out.println("Testing decoration with additional delimiters");
        result = processor.doDecoration(inputText6, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText6);
        System.out.println(result);
        assertEquals(resultText6, result);
        result = processor.doDecoration(inputText6a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText6a);
        System.out.println(result);
        assertEquals(resultText6a, result);
        result = processor.doDecoration(inputText6b, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText6b);
        System.out.println(result);
        assertEquals(resultText6b, result);
        System.out.println("");

        System.out.println("Testing decoration after closing tags");
        result = processor.doDecoration(inputText7, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText7);
        System.out.println(result);
        assertEquals(resultText7, result);
        result = processor.doDecoration(inputText7a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText7a);
        System.out.println(result);
        assertEquals(resultText7a, result);
        System.out.println("");

        System.out.println("Testing decoration with decoration keys including delimiters (Dr.)");
        result = processor.doDecoration(inputText8, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText8);
        System.out.println(result);
        assertEquals(resultText8, result);
        result = processor.doDecoration(inputText8a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText8a);
        System.out.println(result);
        assertEquals(resultText8a, result);
        System.out.println("");

        System.out.println("Testing decoration with decoration keys including multiple delimiters (z.B.)");
        result = processor.doDecoration(inputText9, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText9);
        System.out.println(result);
        assertEquals(resultText9, result);
        result = processor.doDecoration(inputText9a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText9a);
        System.out.println(result);
        assertEquals(resultText9a, result);
        System.out.println("");

        System.out.println("Testing decoration with blank in it");
        result = processor.doDecoration(inputText10, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText10);
        System.out.println(result);
        assertEquals(resultText10, result);
        result = processor.doDecoration(inputText10a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText10a);
        System.out.println(result);
        assertEquals(resultText10a, result);
        System.out.println("");

        System.out.println("Testing decoration inside of quatations");
        result = processor.doDecoration(inputText11, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText11);
        System.out.println(result);
        assertEquals(resultText11, result);
        result = processor.doDecoration(inputText11a, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputText11a);
        System.out.println(result);
        assertEquals(resultText11a, result);
        System.out.println("");
    }

    /**
     * Tests the decoration configuration.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testDecoratorConfiguration() throws Exception {

        String configFile = "/decoration/configuration.xml";

        String inputTextEn1 = "CMS";
        String resultTextEn1 = "<abbr title=\"Concerned Member State\" lang=\"en\">CMS</abbr>";

        String resultTextEn2 = "BKK";

        String inputTextDe1 = "BKK";
        String resultTextDe1 = "<abbr title=\"Betriebskrankenkasse\" lang=\"de\">BKK</abbr>";

        String resultTextDe2 = "<abbr title=\"Content Management System\" lang=\"\">CMS</abbr>";

        System.out.println("Testing the OpenCms decorator configuration.");

        // create a decorator  configuration
        CmsDecoratorConfiguration configuration = new CmsDecoratorConfiguration(getCmsObject(), configFile);
        CmsHtmlDecorator decorator = new CmsHtmlDecorator(getCmsObject(), configuration);

        System.out.println("Testing english decoration in english bundle");
        String result = decorator.doDecoration(inputTextEn1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputTextEn1);
        System.out.println(result);
        assertEquals(resultTextEn1, result);
        System.out.println("");

        System.out.println("Testing german decoration in english bundle");
        result = decorator.doDecoration(inputTextDe1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputTextDe1);
        System.out.println(result);
        assertEquals(resultTextEn2, result);
        System.out.println("");

        // create a german decorator configuration
        configuration = new CmsDecoratorConfiguration(getCmsObject(), configFile, new Locale("de"));
        decorator = new CmsHtmlDecorator(getCmsObject(), configuration);

        System.out.println("Testing german decoration in german bundle");
        result = decorator.doDecoration(inputTextDe1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputTextDe1);
        System.out.println(result);
        assertEquals(resultTextDe1, result);
        System.out.println("");

        System.out.println("Testing neutral decoration in german bundle");
        result = decorator.doDecoration(inputTextEn1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(inputTextEn1);
        System.out.println(result);
        assertEquals(resultTextDe2, result);
        System.out.println("");

    }

}
