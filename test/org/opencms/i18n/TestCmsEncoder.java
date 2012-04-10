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

package org.opencms.i18n;

import org.opencms.test.OpenCmsTestCase;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import junit.framework.TestCase;

/**
 * Tests for the CmsEncoder.<p>
 * 
 * @since 6.0.0
 */
public class TestCmsEncoder extends TestCase {

    private static final String ENC_ISO_8859_1 = CmsEncoder.ENCODING_ISO_8859_1;
    private static final String ENC_ISO_8859_15 = "ISO-8859-15";
    private static final String ENC_US_ASCII = CmsEncoder.ENCODING_US_ASCII;
    private static final String ENC_UTF_8 = CmsEncoder.ENCODING_UTF_8;
    private static final String ENC_WINDOWS_1252 = "Cp1252";

    // working around encoding issues (e.g. of CVS) by using unicode values 
    // the values of C_STRING_1 are: ae oe ue Ae Oe Ue scharfes-s euro-symbol
    private static final String STRING_1 = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac";
    private static final String STRING_2 = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df&#8364;";
    private static final String STRING_3 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364;";
    private static final String STRING_4 = "\u00e4\u00f6\u00fc\u20ac#|#12|&#12|&#;\u00c4\u00d6\u00dctest";
    private static final String STRING_5 = "&#228;&#246;&#252;&#8364;#|#12|&#12|&#;&#196;&#214;&#220;test";
    private static final String STRING_6 = "Test: \\u00e4\\u00f6\\u00fc\\u00c4\\u00d6\\u00dc\\u00df\\u20ac";

    private static final String[][] TESTS_DECODE = {
        {STRING_3, STRING_2, ENC_ISO_8859_1},
        {STRING_3, STRING_1, ENC_ISO_8859_15},
        {STRING_3, STRING_1, ENC_UTF_8},
        {STRING_3, STRING_3, ENC_US_ASCII},
        {STRING_3, STRING_1, ENC_WINDOWS_1252},
        {STRING_5, STRING_4, ENC_UTF_8}};

    private static final String[][] TESTS_ENCODE = {
        {STRING_1, STRING_2, ENC_ISO_8859_1},
        {STRING_1, STRING_1, ENC_ISO_8859_15},
        {STRING_1, STRING_1, ENC_UTF_8},
        {STRING_1, STRING_3, ENC_US_ASCII},
        {STRING_1, STRING_1, ENC_WINDOWS_1252},
        {STRING_4, STRING_5, ENC_US_ASCII}};

    /**
     * @see CmsEncoder#decodeHtmlEntities(String, String) 
     */
    public void testDecodeHtmlEntities() {

        for (int i = 0; i < TESTS_DECODE.length; i++) {
            String source = TESTS_DECODE[i][0];
            String dest = TESTS_DECODE[i][1];
            String encoding = TESTS_DECODE[i][2];

            String result = CmsEncoder.decodeHtmlEntities(source, encoding);
            assertEquals(result, dest);
        }
    }

    /**
     * Tests decoding german "umlaute".<p>
     */
    public void testDecodeUmlauts() {

        Charset defaultCs = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        System.out.println("Default Charset: " + defaultCs.name());
        String param = "%C3%BC"; // utf-8 bytes for '�'
        String decoded = CmsEncoder.decode(param, CmsEncoder.ENCODING_UTF_8);
        String decoded2 = CmsEncoder.decode(param, CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals(OpenCmsTestCase.C_UUML_LOWER, decoded);
        assertFalse(OpenCmsTestCase.C_UUML_LOWER.equals(decoded2));
    }

    /**
     * Tests wether two subsequent calls to 
     * <code>{@link CmsEncoder#escapeWBlanks(String, String)}</code> 
     * lead to an expected result and ensures that the 2nd call does not  
     * do any further modifications. <p>
     *
     */
    public void testDoubleEncoding() {

        String original = "Online Project (VFS)";
        String encode1 = CmsEncoder.escapeWBlanks(original, ENC_UTF_8);
        String encode2 = CmsEncoder.escapeWBlanks(encode1, ENC_UTF_8);
        assertFalse(encode1.equals(encode2));
        assertEquals("Online%2520Project%2520%2528VFS%2529", encode2);
    }

    /**
     * @see CmsEncoder#encodeHtmlEntities(String, String)
     */
    public void testEncodeForHtml() {

        for (int i = 0; i < TESTS_ENCODE.length; i++) {
            String source = TESTS_ENCODE[i][0];
            String dest = TESTS_ENCODE[i][1];
            String encoding = TESTS_ENCODE[i][2];

            String result = CmsEncoder.encodeHtmlEntities(source, encoding);
            assertEquals(result, dest);
        }
    }

    /**
     * @see CmsEncoder#encodeJavaEntities(String, String) 
     */
    public void testEncodeNonIsoEntities() {

        String result = CmsEncoder.encodeJavaEntities(STRING_1, CmsEncoder.ENCODING_US_ASCII);
        System.out.println("\n\n" + STRING_1);
        System.out.println(result + "\n\n");
        assertEquals(result, STRING_6);
    }

    /**
     * Encodes a single '%' and ensures that it is transformed. Encodes 
     * a sequence that is already an encoded special character (e.g.: "%25") 
     * and ensures that this sequence is not encoded several times. <p>
     *
     */
    public void testEncodePercent() {

        String original = "% abc";
        String encoded = CmsEncoder.encode(original);
        assertFalse("A single '%' charater must be transformed by encoding.", original.equals(encoded));
        original = "%25 abc";
        encoded = CmsEncoder.encode(original);
        assertFalse("A encoded sequence \"%25\" must be transformed by a further encoding.", original.equals(encoded));
    }

    /**
     * @see CmsEncoder#lookupEncoding(String, String)
     */
    public void testLookupEncoding() {

        assertEquals(CmsEncoder.lookupEncoding("UTF-8", null), CmsEncoder.ENCODING_UTF_8);
        assertEquals(CmsEncoder.lookupEncoding("utf-8", null), CmsEncoder.ENCODING_UTF_8);
        assertEquals(CmsEncoder.lookupEncoding("UTF8", null), CmsEncoder.ENCODING_UTF_8);
        assertEquals(CmsEncoder.lookupEncoding("utf8", null), CmsEncoder.ENCODING_UTF_8);
        assertEquals(CmsEncoder.lookupEncoding("ISO-8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso-8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO_8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso_8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO_8859_1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso_8859_1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("latin1", null), "ISO-8859-1");
    }

    /**
     * Tests the encoding of a single parameter.<p>
     */
    public void testParamEncoding() {

        String term = "Test ������߀ +-";
        String encoded = CmsEncoder.encodeParameter(term);
        String result = CmsEncoder.decodeParameter(encoded);

        System.out.print(encoded);
        assertEquals(term, result);
    }

    /**
     * Tests encoding of parameters.<p>
     */
    public void testParameterEncoding() {

        String param;
        String result;

        param = "+";

        result = CmsEncoder.encode(param, CmsEncoder.ENCODING_UTF_8);
        result = CmsEncoder.decode(result, CmsEncoder.ENCODING_UTF_8);

        assertEquals(param, result);

        param = "+K�ln -D�sseldorf &value";

        result = CmsEncoder.encode(param, CmsEncoder.ENCODING_UTF_8);
        result = CmsEncoder.decode(result, CmsEncoder.ENCODING_UTF_8);

        assertEquals(param, result);
    }

    /**
     * Tests wether two subsequent calls to 
     * <code>{@link CmsEncoder#escapeWBlanks(String, String)}</code> 
     * are undone by onde decode call (the 2nd encode call must not modify anything.<p>
     *
     */
    public void testRecursiveDecodingOfDoubleEncoded() {

        String original = "Online Project (VFS)";
        String encode1 = CmsEncoder.escapeWBlanks(original, ENC_UTF_8);
        String encode2 = CmsEncoder.escapeWBlanks(encode1, ENC_UTF_8);
        String decoded = CmsEncoder.decode(encode2, ENC_UTF_8);
        assertEquals(encode1, decoded);
    }
}