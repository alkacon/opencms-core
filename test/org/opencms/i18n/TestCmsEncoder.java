/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.i18n;

import org.opencms.test.OpenCmsTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the CmsEncoder.<p>
 *
 * @since 6.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsEncoder extends OpenCmsTestRunner {

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
     * Initializes the shared test configuration without booting OpenCms.<p>
     */
    @BeforeAll
    public void setUpConfiguration(TestInfo testInfo) {

        initConfiguration();
    }

    /**
     * @see CmsEncoder#decodeHtmlEntities(String, String)
     */
    @Order(11)
    @Test
    public void testDecodeHtmlEntities() {

        for (int i = 0; i < TESTS_DECODE.length; i++) {
            String source = TESTS_DECODE[i][0];
            String dest = TESTS_DECODE[i][1];
            String encoding = TESTS_DECODE[i][2];

            String result = CmsEncoder.decodeHtmlEntities(source, encoding);
            assertEquals(dest, result);
        }
    }

    /**
     * Tests decoding german "umlaute".<p>
     */
    @Order(9)
    @Test
    public void testDecodeUmlauts() {

        Charset defaultCs = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        System.out.println("Default Charset: " + defaultCs.name());
        String param = "%C3%BC"; // utf-8 bytes for '�'
        String decoded = CmsEncoder.decode(param, CmsEncoder.ENCODING_UTF_8);
        String decoded2 = CmsEncoder.decode(param, CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals(C_UUML_LOWER, decoded);
        assertFalse(C_UUML_LOWER.equals(decoded2));
    }

    /**
     * Tests wether two subsequent calls to
     * <code>{@link CmsEncoder#escapeWBlanks(String, String)}</code>
     * lead to an expected result and ensures that the 2nd call does not
     * do any further modifications. <p>
     *
     */
    @Order(4)
    @Test
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
    @Order(13)
    @Test
    public void testEncodeForHtml() {

        for (int i = 0; i < TESTS_ENCODE.length; i++) {
            String source = TESTS_ENCODE[i][0];
            String dest = TESTS_ENCODE[i][1];
            String encoding = TESTS_ENCODE[i][2];

            String result = CmsEncoder.encodeHtmlEntities(source, encoding);
            assertEquals(dest, result);
        }
    }

    /**
     * @see CmsEncoder#encodeJavaEntities(String, String)
     */
    @Order(1)
    @Test
    public void testEncodeNonIsoEntities() {

        String result = CmsEncoder.encodeJavaEntities(STRING_1, CmsEncoder.ENCODING_US_ASCII);
        System.out.println("\n\n" + STRING_1);
        System.out.println(result + "\n\n");
        assertEquals(STRING_6, result);
    }

    /**
     * Encodes a single '%' and ensures that it is transformed. Encodes
     * a sequence that is already an encoded special character (e.g.: "%25")
     * and ensures that this sequence is not encoded several times. <p>
     *
     */
    @Order(12)
    @Test
    public void testEncodePercent() {

        String original = "% abc";
        String encoded = CmsEncoder.encode(original);
        assertFalse(original.equals(encoded), "A single '%' charater must be transformed by encoding.");
        original = "%25 abc";
        encoded = CmsEncoder.encode(original);
        assertFalse(original.equals(encoded), "A encoded sequence \"%25\" must be transformed by a further encoding.");
    }

    /**
     * Tests encoding of string lists as request parameters using base64 encoding.<p>
     */
    @Order(5)
    @Test
    public void testEncodeStringsAsParameter() {

        List<String> strings = Arrays.asList("zzzzzz", "~~~~~~~", "cow", "shark", "cat", "dog");
        String param = CmsEncoder.encodeStringsAsBase64Parameter(strings);
        assertEquals(strings, CmsEncoder.decodeStringsFromBase64Parameter(param));
    }

    /**
     * Tests XML escaping.
     */
    @Order(10)
    @Test
    public void testEscapeXml() {

        String input = "<>&'\"";
        assertEquals("&lt;&gt;&amp;&apos;&quot;", CmsEncoder.escapeXml(input));
    }

    /**
     * @see CmsEncoder#lookupEncoding(String, String)
     */
    @Order(6)
    @Test
    public void testLookupEncoding() {

        assertEquals(CmsEncoder.ENCODING_UTF_8, CmsEncoder.lookupEncoding("UTF-8", null));
        assertEquals(CmsEncoder.ENCODING_UTF_8, CmsEncoder.lookupEncoding("utf-8", null));
        assertEquals(CmsEncoder.ENCODING_UTF_8, CmsEncoder.lookupEncoding("UTF8", null));
        assertEquals(CmsEncoder.ENCODING_UTF_8, CmsEncoder.lookupEncoding("utf8", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("ISO-8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("iso-8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("ISO8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("iso8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("ISO_8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("iso_8859-1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("ISO_8859_1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("iso_8859_1", null));
        assertEquals("ISO-8859-1", CmsEncoder.lookupEncoding("latin1", null));
    }

    /**
     * Tests the encoding of a single parameter.<p>
     */
    @Order(7)
    @Test
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
    @Order(2)
    @Test
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
     * Tests punycode conversion of domain names with special characters in them.
     *
     * @throws Exception
     */
    @Order(8)
    @Test
    public void testPunycodeConversion() throws Exception {

        assertEquals("http://xn--wrmer-kva.de", CmsEncoder.convertHostToPunycode("http://würmer.de"));
        // test with more complicated URI (with port, path, query, special characters)
        assertEquals(
            "http://xn--wrmer-kva.de:77/%26/?f=%26&g=%26",
            CmsEncoder.convertHostToPunycode("http://würmer.de:77/%26/?f=%26&g=%26"));
        String ipv6Uri = "http://[ed19:f7ae:ac5b:8b46:bbb0:c02c:1d29:e337]:8080/f1";
        assertEquals(ipv6Uri, CmsEncoder.convertHostToPunycode(ipv6Uri));
        ipv6Uri = "http://[ed19:f7ae:ac5b:8b46:bbb0:c02c:1d29:e337]/f1";
        assertEquals(ipv6Uri, CmsEncoder.convertHostToPunycode(ipv6Uri));
        assertEquals(
            "http://user:password@xn--wrmer-kva.de",
            CmsEncoder.convertHostToPunycode("http://user:password@würmer.de"));
        assertEquals(
            "http://test.invalid:8080/foo?bar=qux",
            CmsEncoder.convertHostToPunycode("http://test.invalid:8080/foo?bar=qux"));

    }

    /**
     * Tests wether two subsequent calls to
     * <code>{@link CmsEncoder#escapeWBlanks(String, String)}</code>
     * are undone by onde decode call (the 2nd encode call must not modify anything.<p>
     *
     */
    @Order(3)
    @Test
    public void testRecursiveDecodingOfDoubleEncoded() {

        String original = "Online Project (VFS)";
        String encode1 = CmsEncoder.escapeWBlanks(original, ENC_UTF_8);
        String encode2 = CmsEncoder.escapeWBlanks(encode1, ENC_UTF_8);
        String decoded = CmsEncoder.decode(encode2, ENC_UTF_8);
        assertEquals(encode1, decoded);
    }
}
