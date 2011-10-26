/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_VISIBLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.dom4j.Element;
import org.dom4j.Node;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContentProperty;

/**
 * Test case for inherited containers.
 * <p>
 * 
 */
public class TestInheritedContainer extends OpenCmsTestCase {

	/**
	 * Returns the test suite.
	 * <p>
	 * 
	 * @return the test suite
	 */
	public static Test suite() {

		OpenCmsTestProperties
				.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
		return generateSetupTestWrapper(TestInheritedContainer.class,
				"inheritcontainer", "/");
	}

	public TestInheritedContainer(String name) {

		super(name);
	}

	protected CmsContainerConfiguration buildConfiguration(String spec) {

		Map<String, Boolean> visibility = new HashMap<String, Boolean>();
		Map<String, CmsContainerElementBean> newElements = new HashMap<String, CmsContainerElementBean>();
		List<String> ordering = new ArrayList<String>();

		String[] tokens = spec.split("\\|", 4);
		String orderingStr = tokens[0];
		String visibleStr = tokens[1];
		String hiddenStr = tokens[2];
		String newElemStr = tokens[3];

		if (orderingStr.length() == 0) {
			ordering = null;
		} else {
			ordering = CmsStringUtil.splitAsList(orderingStr, " ");
		}
		for (String visible : visibleStr.split("\\s+")) {
			if (CmsStringUtil.isEmptyOrWhitespaceOnly(visible)) {
				continue;
			}
			visibility.put(visible, Boolean.TRUE);
		}
		for (String hidden : hiddenStr.split("\\s+")) {
			if (CmsStringUtil.isEmptyOrWhitespaceOnly(hidden)) {
				continue;
			}
			visibility.put(hidden, Boolean.FALSE);
		}
		for (String newElem : newElemStr.split("\\s+")) {
			if (CmsStringUtil.isEmptyOrWhitespaceOnly(newElem)) {
				continue;
			}
			newElements.put(newElem, generateDummyElement(newElem));
		}
		return new CmsContainerConfiguration(ordering, visibility, newElements);
	}

	protected void checkCDATA(Element element, String expectedValue) {
		Node childNode = element.node(0);
		assertEquals("cdata", childNode.getNodeTypeName().toLowerCase());
		assertEquals(expectedValue, childNode.getText());
	}

	/**
	 * Helper method for generating a dummy container element.
	 * <p>
	 * 
	 * @param key
	 *            the key to use
	 * @return the dummy container element
	 */
	protected CmsContainerElementBean generateDummyElement(String key) {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("setting_" + key, "value_" + key);

		CmsContainerElementBean elementBean = new CmsContainerElementBean(
				CmsUUID.getConstantUUID(key), CmsUUID.getNullUUID(), settings,
				false);
		return elementBean;
	}

	protected String getCDATA(Element element) {
		return element.node(0).getText();
	}

	public void testAppendNew() throws Exception {

		CmsInheritedContainerState result = new CmsInheritedContainerState();
		result.addConfiguration(buildConfiguration("a b c|||a b c"));
		result.addConfiguration(buildConfiguration("d e|||d e"));
		result.addConfiguration(buildConfiguration("a c|||"));
		List<CmsContainerElementBean> elementBeans = result.getElements(true);
		assertEquals(5, elementBeans.size());
		CmsInheritanceInfo info1 = elementBeans.get(0).getInheritanceInfo();
		assertEquals("a", info1.getKey());
		CmsInheritanceInfo info2 = elementBeans.get(1).getInheritanceInfo();
		assertEquals("c", info2.getKey());
		CmsInheritanceInfo info3 = elementBeans.get(2).getInheritanceInfo();
		assertEquals("b", info3.getKey());
		CmsInheritanceInfo info4 = elementBeans.get(3).getInheritanceInfo();
		assertEquals("d", info4.getKey());
		CmsInheritanceInfo info5 = elementBeans.get(4).getInheritanceInfo();
		assertEquals("e", info5.getKey());
	}

	/**
	 * Tests rearrangement of inherited elements.
	 * <p>
	 * 
	 * @throws Exception
	 */
	public void testChangeOrder1() throws Exception {

		CmsInheritedContainerState result = new CmsInheritedContainerState();
		result.addConfiguration(buildConfiguration("a b c|||a b c"));
		result.addConfiguration(buildConfiguration("b c a|||"));
		List<CmsContainerElementBean> elementBeans = result.getElements(true);
		assertEquals(3, elementBeans.size());
		CmsInheritanceInfo info1 = elementBeans.get(0).getInheritanceInfo();
		assertEquals("b", info1.getKey());
		CmsInheritanceInfo info2 = elementBeans.get(1).getInheritanceInfo();
		assertEquals("c", info2.getKey());
		CmsInheritanceInfo info3 = elementBeans.get(2).getInheritanceInfo();
		assertEquals("a", info3.getKey());
	}

	/**
	 * Test for hiding of inherited elements.
	 * <p>
	 * 
	 * @throws Exception
	 */
	public void testHideElements1() throws Exception {

		CmsInheritedContainerState result = new CmsInheritedContainerState();
		result.addConfiguration(buildConfiguration("a b c d|||a b c d"));
		result.addConfiguration(buildConfiguration("||b d|"));
		List<CmsContainerElementBean> elementBeans = result.getElements(true);
		assertEquals(4, elementBeans.size());
		CmsInheritanceInfo info1 = elementBeans.get(0).getInheritanceInfo();
		assertEquals("a", info1.getKey());
		CmsInheritanceInfo info2 = elementBeans.get(1).getInheritanceInfo();
		assertEquals("c", info2.getKey());
		CmsInheritanceInfo info3 = elementBeans.get(2).getInheritanceInfo();
		assertEquals("b", info3.getKey());
		CmsInheritanceInfo info4 = elementBeans.get(3).getInheritanceInfo();
		assertEquals("d", info4.getKey());

		elementBeans = result.getElements(false);
		assertEquals(2, elementBeans.size());
		info1 = elementBeans.get(0).getInheritanceInfo();
		assertEquals("a", info1.getKey());
		info2 = elementBeans.get(1).getInheritanceInfo();
		assertEquals("c", info2.getKey());
	}

	/**
	 * Tests parsing of container configuration files.
	 * 
	 * @throws Exception
	 */
	public void testParseContainerConfiguration() throws Exception {

		String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "\r\n"
				+ "<AlkaconInheritConfigGroups xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.containerpage/schemas/inherit_config_group.xsd\">\r\n"
				+ "  <AlkaconInheritConfigGroup language=\"en\">\r\n"
				+ "    <Title><![CDATA[blah]]></Title>\r\n"
				+ "    <Configuration>\r\n"
				+ "      <Name><![CDATA[blubb]]></Name>\r\n"
				+ "      <OrderKey><![CDATA[this/is/the/key]]></OrderKey>\r\n"
				+ "      <Visible><![CDATA[foo]]></Visible>\r\n"
				+ "      <Hidden><![CDATA[bar]]></Hidden>\r\n"
				+ "      <NewElement>\r\n"
				+ "        <Key><![CDATA[this/is/another/key]]></Key>\r\n"
				+ "        <Element>\r\n"
				+ "          <Uri>\r\n"
				+ "            <link type=\"STRONG\">\r\n"
				+ "              <target><![CDATA[/system/test.txt]]></target>\r\n"
				+ "              <uuid>00000001-0000-0000-0000-000000000000</uuid>\r\n"
				+ "            </link>\r\n"
				+ "          </Uri>\r\n"
				+ "          <Properties>\r\n"
				+ "            <Name><![CDATA[testsetting]]></Name>\r\n"
				+ "            <Value>\r\n"
				+ "              <String><![CDATA[testvalue]]></String>\r\n"
				+ "            </Value>\r\n"
				+ "          </Properties>\r\n"
				+ "          <Properties>\r\n"
				+ "            <Name><![CDATA[testsetting2]]></Name>\r\n"
				+ "            <Value>\r\n"
				+ "              <FileList>\r\n"
				+ "                <Uri>\r\n"
				+ "                  <link type=\"WEAK\">\r\n"
				+ "                    <target><![CDATA[/system/test.txt]]></target>\r\n"
				+ "                    <uuid>00000001-0000-0000-0000-000000000000</uuid>\r\n"
				+ "                  </link>\r\n"
				+ "                </Uri>\r\n"
				+ "              </FileList>\r\n" + "            </Value>\r\n"
				+ "          </Properties>\r\n" + "        </Element>\r\n"
				+ "      </NewElement>\r\n" + "    </Configuration>\r\n"
				+ "  </AlkaconInheritConfigGroup>\r\n"
				+ "</AlkaconInheritConfigGroups>\r\n";
		byte[] xmlData = xmlText.getBytes("UTF-8");
		CmsFile file = new CmsFile(new CmsUUID()/* structureid */,
				new CmsUUID()/* resourceid */, "/test"/* rootpath */,
				303/* typeid */, 0/* flags */,
				new CmsUUID()/* projectlastmodified */,
				CmsResourceState.STATE_NEW/* state */, 0/* datecreated */,
				new CmsUUID()/* usercreated */, 0/* datemodified */,
				new CmsUUID()/* usermodified */, 0/* datereleased */,
				0/* dateexpired */, 0/* siblingcount */,
				xmlData.length/* length */, 0/* datacontent */, 0/* version */,
				xmlData/* content */);
		CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(
				getCmsObject());
		parser.parse(file);
		Map<Locale, Map<String, CmsContainerConfiguration>> results = parser
				.getParsedResults();
		Map<String, CmsContainerConfiguration> configurationGroup = results
				.get(new Locale("en"));
		assertNotNull(configurationGroup);
		CmsContainerConfiguration config = configurationGroup.get("blubb");
		assertNotNull(config);
		Map<String, CmsContainerElementBean> newElements = config
				.getNewElements();
		assertNotNull(newElements);
		CmsContainerElementBean elementBean = newElements
				.get("this/is/another/key");
		assertEquals(new CmsUUID("00000001-0000-0000-0000-000000000000"),
				elementBean.getId());
		Map<String, String> settings = elementBean.getIndividualSettings();
		assertNotNull(settings);
		assertEquals("testvalue", settings.get("testsetting"));
		assertEquals("00000001-0000-0000-0000-000000000000",
				settings.get("testsetting2"));
	}

	public void testSerialization1() throws Exception {
		CmsContainerConfiguration config = buildConfiguration("a b c|d|e f|a b c");
		CmsXmlContentProperty setting1def = new CmsXmlContentProperty(
				"setting_a", "string", null, null, null, null, null, null,
				null, null, null);
		Map<String, CmsXmlContentProperty> settingDefs = new HashMap<String, CmsXmlContentProperty>();
		settingDefs.put("setting_a", setting1def);
		CmsContainerConfigurationWriter writer = new CmsContainerConfigurationWriter();
		writer.setPropertyConfiguration(settingDefs);
		Element element = writer.serializeSingleConfiguration(getCmsObject(),
				"configname", config);
		assertNotNull(element);
		assertEquals("Configuration", element.getName());
		List<Node> nodes = CmsCollectionsGenericWrapper.list(element
				.selectNodes(N_ORDERKEY));
		assertEquals(3, nodes.size());
		Element element0 = (Element) nodes.get(0);
		checkCDATA(element0, "a");
		checkCDATA((Element) nodes.get(1), "b");
		checkCDATA((Element) nodes.get(2), "c");

		List<Node> visibleNodes = CmsCollectionsGenericWrapper.list(element
				.selectNodes(N_VISIBLE));
		assertEquals(1, visibleNodes.size());
		checkCDATA((Element) visibleNodes.get(0), "d");

		List<Node> invisibleNodes = CmsCollectionsGenericWrapper.list(element
				.selectNodes(N_HIDDEN));
		assertEquals(2, invisibleNodes.size());
		Set<String> actualInvisible = new HashSet<String>();
		Set<String> expectedInvisible = new HashSet<String>();
		expectedInvisible.add("e");
		expectedInvisible.add("f");
		for (Node node : invisibleNodes) {
			actualInvisible.add(getCDATA((Element) node));
		}
		assertEquals(expectedInvisible, actualInvisible);
		{
			Node targetIdNode = element
					.selectSingleNode("NewElement[Key='a']/Element/Uri/link/uuid");
			String uuidString = getCDATA((Element) targetIdNode);
			assertEquals(CmsUUID.getConstantUUID("a"), new CmsUUID(uuidString));
		}
		{
			Node targetIdNode = element
					.selectSingleNode("NewElement[Key='b']/Element/Uri/link/uuid");
			String uuidString = getCDATA((Element) targetIdNode);
			assertEquals(CmsUUID.getConstantUUID("b"), new CmsUUID(uuidString));
		}
		{
			Node targetIdNode = element
					.selectSingleNode("NewElement[Key='c']/Element/Uri/link/uuid");
			String uuidString = getCDATA((Element) targetIdNode);
			assertEquals(CmsUUID.getConstantUUID("c"), new CmsUUID(uuidString));
		}
		assertEquals(3, element.selectNodes("NewElement").size());
		assertEquals(
				"value_a",
				getCDATA((Element) element
						.selectSingleNode("NewElement[Key='a']/Element/Properties[Name='setting_a']/Value/String")));

	}
}
