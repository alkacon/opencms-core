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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.test.I_CmsLogHandler;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Test case for inherited containers.
 * <p>
 * 
 */
public class TestInheritedContainer extends OpenCmsTestCase {

    /**
     * A special log handler which intercepts log messages from the inherited container configuration
     * cache to count how often new configuration files are loaded into the cache.
     */
    public static class CacheLoadLogHandler implements I_CmsLogHandler {

        /** The number of offline loads. */
        private int m_offlineLoads;

        private int m_offlineReadResource;

        /** The number of online loads. */
        private int m_onlineLoads;
        private int m_onlineReadResource;

        /** 
         * Resets the offline/online load counters to 0.<p>
         */
        public void clear() {

            m_offlineLoads = 0;
            m_onlineLoads = 0;
            m_offlineReadResource = 0;
            m_onlineReadResource = 0;
        }

        /**
         * Gets the number of offline loads since the last call to clear().<p>
         * 
         * @return the number of offline loads 
         */
        public int getOfflineLoads() {

            return m_offlineLoads;
        }

        public int getOfflineReadResource() {

            return m_offlineReadResource;
        }

        /**
         * Gets the number of online loads since the last call to clear().<p>
         * 
         * @return the number of online loads 
         */
        public int getOnlineLoads() {

            return m_onlineLoads;
        }

        public int getOnlineReadResource() {

            return m_onlineReadResource;
        }

        /**
         * @see org.opencms.test.I_CmsLogHandler#handleLogEvent(org.apache.log4j.spi.LoggingEvent)
         */
        public void handleLogEvent(LoggingEvent event) {

            String message = event.getMessage().toString();
            if (event.getLevel().equals(Level.TRACE)) {
                if (message.startsWith("inherited-container-cache offline load")) {
                    m_offlineLoads += 1;
                }
                if (message.startsWith("inherited-container-cache online load")) {
                    m_onlineLoads += 1;
                }
                if (message.startsWith("inherited-container-cache offline readSingleResource")) {
                    m_offlineReadResource += 1;
                }
                if (message.startsWith("inherited-container-cache online readSingleResource")) {
                    m_onlineReadResource += 1;
                }
            }
        }
    }

    /** Constant which represents the offline project. */
    public static final boolean OFFLINE = false;

    /** Constant which represents the online project. */
    public static final boolean ONLINE = true;

    /**
     * The test case constructor.<p>
     * 
     * @param name the name of the test case 
     */
    public TestInheritedContainer(String name) {

        super(name);
    }

    /**
     * Returns the test suite.
     * <p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestInheritedContainer.class, "inheritcontainer", "/");
    }

    /**
     * Tests that new elements in parent configurations which are not explicitly referenced by
     * a child configuration's ordering are inserted at the end of the element list.<p>
     * 
     * @throws Exception
     */
    public void testAppendNew() throws Exception {

        CmsInheritedContainerState result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b c|||a b c"));
        result.addConfiguration(buildConfiguration("d e|||d e"));
        result.addConfiguration(buildConfiguration("a c|||"));
        List<CmsContainerElementBean> elementBeans = result.getElements(true);
        checkSpec(
            elementBeans,
            "key=a new=false visible=true",
            "key=c new=false visible=true",
            "key=b new=false visible=true",
            "key=d new=false visible=true",
            "key=e new=false visible=true");

        result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b c d f e|||f e c d b a"));
        result.addConfiguration(buildConfiguration("g|||g"));
        result.addConfiguration(buildConfiguration("|||"));
        elementBeans = result.getElements(true);
        checkSpec(
            elementBeans,
            "key=g new=false",
            "key=a new=false",
            "key=b new=false",
            "key=c new=false",
            "key=d new=false",
            "key=f new=false",
            "key=e new=false");

        result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b|||b a"));
        result.addConfiguration(buildConfiguration("d c|||c d"));
        result.addConfiguration(buildConfiguration("e f|||e f"));
        elementBeans = result.getElements(true);
        checkSpec(
            elementBeans,
            "key=e new=true",
            "key=f new=true",
            "key=a new=false",
            "key=b new=false",
            "key=d new=false",
            "key=c new=false");
    }

    /**
     * Tests the correctness of the offline cache results.<p>
     * 
     * @throws Exception
     */
    public void testCacheCorrectnessOffline() throws Exception {

        OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);

        writeConfiguration(1, "a");
        writeConfiguration(2, "b");
        writeConfiguration(3, "c");
        CmsObject cms = getCmsObject();
        String level3 = "/system/level1/level2/level3";
        CmsInheritedContainerState state = OpenCms.getADEManager().getInheritedContainerState(cms, level3, "alpha");
        List<CmsContainerElementBean> elementBeans = state.getElements(true);
        // a, b, c
        checkSpecForPoint(level3, "alpha", false, "key=c", "key=a", "key=b");

        writeConfiguration(2, "d");
        // a, d, c
        checkSpecForPoint(level3, "alpha", false, "key=c", "key=a", "key=d");

        writeConfiguration(1, "b");
        // b, d, c
        checkSpecForPoint(level3, "alpha", false, "key=c", "key=b", "key=d");

        writeConfiguration(3, "a");
        // b, d, a
        checkSpecForPoint(level3, "alpha", false, "key=a", "key=b", "key=d");

        deleteConfiguration(2);

        //b, -, a
        checkSpecForPoint(level3, "alpha", false, "key=a", "key=b");

    }

    /**
     * Tests the correctness of the online cache results.<p>
     * 
     * @throws Exception
     */
    public void testCacheCorrectnessOnline() throws Exception {

        OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);

        writeConfiguration(1, "a");
        writeConfiguration(2, "b");
        writeConfiguration(3, "c");
        publish();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        String level3 = "/system/level1/level2/level3";
        CmsInheritedContainerState state = OpenCms.getADEManager().getInheritedContainerState(cms, level3, "alpha");

        // OFFLINE: a, b, c       ONLINE: a, b, c 
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=b");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=b");

        writeConfiguration(2, "d");
        // OFFLINE: a, d, c       ONLINE: a, b, c
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=b");

        publish();
        // OFFLINE: a, d, c       ONLINE: a, d, c
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=d");

        writeConfiguration(1, "b");
        // OFFLINE: b, d, c       ONLINE: a, d, c
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=b", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=d");

        publish();
        // OFFLINE: b, d, c       ONLINE: b,d,c
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=b", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=b", "key=d");

        writeConfiguration(3, "a");
        // OFFLINE: b, d, a       ONLINE: b,d,c
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=a", "key=b", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=b", "key=d");

        publish();
        // OFFLINE: b, d, a       ONLINE: b,d,a
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=a", "key=b", "key=d");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=a", "key=b", "key=d");

        deleteConfiguration(2);
        // OFFLINE: b, -, a      ONLINE: b,d,a
        checkSpecForPoint(level3, "alpha", OFFLINE, "key=a", "key=b");
        checkSpecForPoint(level3, "alpha", ONLINE, "key=a", "key=b", "key=d");

        publish();
        checkSpecForPoint(level3, "alpha", ONLINE, "key=a", "key=b");

    }

    /**
     * Tests whether the cache only loads configuration files when necessary.<p>
     * 
     * @throws Exception
     */
    public void testCacheLoadCounts() throws Exception {

        try {
            OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
            CacheLoadLogHandler logHandler = new CacheLoadLogHandler();
            OpenCmsTestLogAppender.setHandler(logHandler);
            writeConfiguration(1, "a");
            writeConfiguration(2, "b");
            writeConfiguration(3, "c");
            publish();
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            String level3 = "/system/level1/level2/level3";
            // OFFLINE: a, b, c       ONLINE: a, b, c 
            checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            assertEquals(0, logHandler.getOnlineLoads());
            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            assertEquals(3, logHandler.getOnlineLoads());

            checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=b");
            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            assertEquals(3, logHandler.getOnlineLoads());

            logHandler.clear();
            writeConfiguration(2, "d");
            // OFFLINE: a, d, c       ONLINE: a, b, c
            checkSpecForPoint(level3, "alpha", OFFLINE, "key=c", "key=a", "key=d");
            assertEquals(0, logHandler.getOnlineLoads());
            assertEquals(1, logHandler.getOfflineLoads());

            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=b");
            assertEquals(0, logHandler.getOnlineLoads());
            assertEquals(1, logHandler.getOfflineLoads());
            publish();

            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=d");
            assertEquals(1, logHandler.getOnlineLoads());
            assertEquals(1, logHandler.getOfflineLoads());
            // OFFLINE: a, d, c       ONLINE: a, d, c

            deleteConfiguration(3);
            writeConfiguration(1, "b");
            // OFFLINE: b, d, -      ONLINE: a,d,c
            logHandler.clear();
            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=d");
            checkSpecForPoint(level3, "alpha", OFFLINE, "key=d", "key=b");
            assertEquals(2, logHandler.getOfflineLoads());
            assertEquals(0, logHandler.getOnlineLoads());
            checkSpecForPoint(level3, "alpha", ONLINE, "key=c", "key=a", "key=d");
            checkSpecForPoint(level3, "alpha", OFFLINE, "key=d", "key=b");
            assertEquals(2, logHandler.getOfflineLoads());
            assertEquals(0, logHandler.getOnlineLoads());

            publish();
            logHandler.clear();
            // OFFLINE: b, d, -    ONLINE: b, d, -
            checkSpecForPoint(level3, "alpha", ONLINE, "key=d", "key=b");
            assertEquals(0, logHandler.getOfflineLoads());
            checkSpecForPoint(level3, "alpha", OFFLINE, "key=d", "key=b");
            assertEquals(0, logHandler.getOfflineLoads());
            assertEquals(2, logHandler.getOnlineLoads());
        } finally {
            OpenCmsTestLogAppender.setHandler(null);
        }
    }

    /**
     * Tests rearrangement of inherited elements.
     * <p>
     * 
     * @throws Exception
     */
    public void testChangeOrder() throws Exception {

        CmsInheritedContainerState result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b c|||a b c"));
        result.addConfiguration(buildConfiguration("b c a|||"));
        List<CmsContainerElementBean> elementBeans = result.getElements(true);
        checkSpec(
            elementBeans,
            "key=b new=false visible=true",
            "key=c new=false visible=true",
            "key=a new=false visible=true");
    }

    /**
     * Tests whether the inherited container cache is cleared correctly.<p>
     * 
     * @throws Exception 
     */
    public void testClearCaches() throws Exception {

        try {
            OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
            CacheLoadLogHandler logHandler = new CacheLoadLogHandler();
            OpenCmsTestLogAppender.setHandler(logHandler);
            writeConfiguration(1, "a");
            writeConfiguration(2, "b");
            writeConfiguration(3, "c");
            publish();
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a", "key=b");
            checkSpecForPoint("/system/level1/level2/level3", "alpha", ONLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            assertEquals(3, logHandler.getOnlineLoads());
            OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a", "key=b");
            checkSpecForPoint("/system/level1/level2/level3", "alpha", ONLINE, "key=c", "key=a", "key=b");
            assertEquals(6, logHandler.getOfflineLoads());
            assertEquals(6, logHandler.getOnlineLoads());

        } finally {
            OpenCmsTestLogAppender.setHandler(null);
        }

    }

    /**
     * Test for hiding of inherited elements.
     * <p>
     * 
     * @throws Exception
     */
    public void testHideElements() throws Exception {

        CmsInheritedContainerState result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b c d|||a b c d"));
        result.addConfiguration(buildConfiguration("||b d|"));
        List<CmsContainerElementBean> elementBeans = result.getElements(true);
        checkSpec(
            elementBeans,
            "key=a visible=true",
            "key=c visible=true",
            "key=b visible=false",
            "key=d visible=false");

        elementBeans = result.getElements(false);
        checkSpec(elementBeans, "key=a visible=true", "key=c visible=true");
    }

    /**
     * Tests that the 'new' states of inherited container elements are correct.<p>
     * 
     * @throws Exception
     */
    public void testNewElements() throws Exception {

        CmsInheritedContainerState result = new CmsInheritedContainerState();
        result.addConfiguration(buildConfiguration("a b|||a b"));
        checkSpec(result.getElements(true), "key=a new=true", "key=b new=true");
        result.addConfiguration(buildConfiguration("c b a d|||c d"));
        checkSpec(result.getElements(true), "key=c new=true", "key=b new=false", "key=a new=false", "key=d new=true");
    }

    public void testOnlyReadProperlyNamedFiles() throws Exception {

        CacheLoadLogHandler logHandler = new CacheLoadLogHandler();
        try {
            OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(logHandler);
            writeConfiguration(1, "a");
            writeConfiguration(2, "b");
            writeConfiguration(3, "c");
            writeConfiguration("/system/dummy.config", "d");
            publish();
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            deleteConfiguration(2);
            writeConfiguration("/system/level1/level2/baz.config", "b");
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a");
            assertEquals(3, logHandler.getOfflineLoads());
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(null);
            deleteConfiguration("/system/dummy.config");
            deleteConfiguration("/system/level1/level2/baz.config");
            publish();
        }
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
            + "              </FileList>\r\n"
            + "            </Value>\r\n"
            + "          </Properties>\r\n"
            + "        </Element>\r\n"
            + "      </NewElement>\r\n"
            + "    </Configuration>\r\n"
            + "  </AlkaconInheritConfigGroup>\r\n"
            + "</AlkaconInheritConfigGroups>\r\n";
        byte[] xmlData = xmlText.getBytes("UTF-8");
        CmsFile file = new CmsFile(
            new CmsUUID()/* structureid */,
            new CmsUUID()/* resourceid */,
            "/test"/* rootpath */,
            303/* typeid */,
            0/* flags */,
            new CmsUUID()/* projectlastmodified */,
            CmsResourceState.STATE_NEW/* state */,
            0/* datecreated */,
            new CmsUUID()/* usercreated */,
            0/* datemodified */,
            new CmsUUID()/* usermodified */,
            0/* datereleased */,
            0/* dateexpired */,
            0/* siblingcount */,
            xmlData.length/* length */,
            0/* datacontent */,
            0/* version */,
            xmlData/* content */);
        CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(getCmsObject());
        parser.parse(file);
        Map<Locale, Map<String, CmsContainerConfiguration>> results = parser.getParsedResults();
        Map<String, CmsContainerConfiguration> configurationGroup = results.get(new Locale("en"));
        assertNotNull(configurationGroup);
        CmsContainerConfiguration config = configurationGroup.get("blubb");
        assertNotNull(config);
        Map<String, CmsContainerElementBean> newElements = config.getNewElements();
        assertNotNull(newElements);
        CmsContainerElementBean elementBean = newElements.get("this/is/another/key");
        assertEquals(new CmsUUID("00000001-0000-0000-0000-000000000000"), elementBean.getId());
        Map<String, String> settings = elementBean.getIndividualSettings();
        assertNotNull(settings);
        assertEquals("testvalue", settings.get("testsetting"));
        assertEquals("00000001-0000-0000-0000-000000000000", settings.get("testsetting2"));
    }

    /**
     * Tests serialization of a single configuration node.<p>
     * 
     * @throws Exception
     */
    public void testSerialization1() throws Exception {

        CmsContainerConfiguration config = buildConfiguration("a b c|d|e f|a b c");
        CmsXmlContentProperty setting1def = new CmsXmlContentProperty(
            "setting_a",
            "string",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
        Map<String, CmsXmlContentProperty> settingDefs = new HashMap<String, CmsXmlContentProperty>();
        settingDefs.put("setting_a", setting1def);
        CmsContainerConfigurationWriter writer = new CmsContainerConfigurationWriter();
        writer.setPropertyConfiguration(settingDefs);
        Element element = writer.serializeSingleConfiguration(getCmsObject(), "configname", config);
        assertNotNull(element);
        assertEquals("Configuration", element.getName());
        List<Node> nodes = CmsCollectionsGenericWrapper.list(element.selectNodes(N_ORDERKEY));
        assertEquals(3, nodes.size());
        Element element0 = (Element)nodes.get(0);
        checkCDATA(element0, "a");
        checkCDATA((Element)nodes.get(1), "b");
        checkCDATA((Element)nodes.get(2), "c");

        List<Node> visibleNodes = CmsCollectionsGenericWrapper.list(element.selectNodes(N_VISIBLE));
        assertEquals(1, visibleNodes.size());
        checkCDATA((Element)visibleNodes.get(0), "d");

        List<Node> invisibleNodes = CmsCollectionsGenericWrapper.list(element.selectNodes(N_HIDDEN));
        assertEquals(2, invisibleNodes.size());
        Set<String> actualInvisible = new HashSet<String>();
        Set<String> expectedInvisible = new HashSet<String>();
        expectedInvisible.add("e");
        expectedInvisible.add("f");
        for (Node node : invisibleNodes) {
            actualInvisible.add(getNestedText((Element)node));
        }
        assertEquals(expectedInvisible, actualInvisible);
        {
            Node targetIdNode = element.selectSingleNode("NewElement[Key='a']/Element/Uri/link/uuid");
            String uuidString = getNestedText((Element)targetIdNode);
            assertEquals(CmsUUID.getConstantUUID("a"), new CmsUUID(uuidString));
        }
        {
            Node targetIdNode = element.selectSingleNode("NewElement[Key='b']/Element/Uri/link/uuid");
            String uuidString = getNestedText((Element)targetIdNode);
            assertEquals(CmsUUID.getConstantUUID("b"), new CmsUUID(uuidString));
        }
        {
            Node targetIdNode = element.selectSingleNode("NewElement[Key='c']/Element/Uri/link/uuid");
            String uuidString = getNestedText((Element)targetIdNode);
            assertEquals(CmsUUID.getConstantUUID("c"), new CmsUUID(uuidString));
        }
        assertEquals(3, element.selectNodes("NewElement").size());
        assertEquals(
            "value_a",
            getNestedText((Element)element.selectSingleNode("NewElement[Key='a']/Element/Properties[Name='setting_a']/Value/String")));

    }

    /**
     * Tests whether a configuration file which has been first updated and then deleted is removed correctly from the cache.<p>
     * 
     * @throws Exception
     */
    public void testUpdateAndRemove() throws Exception {

        CacheLoadLogHandler logHandler = new CacheLoadLogHandler();
        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(logHandler);

            writeConfiguration(1, "a");
            writeConfiguration(2, "b");
            writeConfiguration(3, "c");
            publish();
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a", "key=b");
            assertEquals(3, logHandler.getOfflineLoads());
            logHandler.clear();
            writeConfiguration(2, "d");
            deleteConfiguration(2);
            assertEquals(0, logHandler.getOfflineReadResource());
            checkSpecForPoint("/system/level1/level2/level3", "alpha", OFFLINE, "key=c", "key=a");
            assertEquals(0, logHandler.getOfflineLoads());
            assertEquals(0, logHandler.getOfflineReadResource());
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCmsTestLogAppender.setHandler(null);
        }
    }

    /**
     * Helper method to create a dummy configuration from a specification string.<p>
     * 
     * The specification string consists of 4 fields separated by the pipe symbol, which correspond to different 
     * parts of the configuration bean. The first field describes the ordering keys of the container configuration;
     * the second field describes the keys which have been made visible by the configuration; the third field describes
     * which keys have been hidden by the configuration, and the last field describes which new elements are contained in 
     * the configuration.<p>
     * 
     * @param spec
     * @return the test configuration 
     */
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

    /**
     * Checks whether a given element contains a nested CDATA node with a given text.<p>
     * 
     * @param element the parent element 
     * @param expectedValue the text which should be contained in the CDATA node 
     */
    protected void checkCDATA(Element element, String expectedValue) {

        Node childNode = element.node(0);
        assertEquals("cdata", childNode.getNodeTypeName().toLowerCase());
        assertEquals(expectedValue, childNode.getText());
    }

    /**
     * Checks whether a container element bean with inheritance information fulfills a given specification string.<p>
     * 
     * The string has the form 'key1=value1 key2=value2....'.<p>
     * 
     * Each key-value pair corresponds to a check to perform on the given container element bean: 
     * The key 'key' performs a test whether the key of the inheritance info has a given value.<p>
     * The key 'new' performs a test whether the element bean was inherited from a parent configuration or is new.<p>
     * The key 'visible' performs a test whether the element bean is marked as visible 
     * @param element
     * @param spec
     */
    protected void checkSpec(CmsContainerElementBean element, String spec) {

        Map<String, String> specMap = CmsStringUtil.splitAsMap(spec, " ", "=");
        for (Map.Entry<String, String> entry : specMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("visible".equals(key)) {
                boolean expectVisible = Boolean.parseBoolean(value);
                assertEquals(expectVisible, element.getInheritanceInfo().getVisibility().booleanValue());
            } else if ("key".equals(key)) {
                String expectedKey = value;
                assertEquals(expectedKey, element.getInheritanceInfo().getKey());
            } else if ("new".equals(key)) {
                boolean expectNew = Boolean.parseBoolean(value);
                assertEquals(expectNew, element.getInheritanceInfo().isNew());
            } else {
                throw new IllegalArgumentException("Invalid specification string: " + spec);
            }
        }
    }

    /**
     * Checks whether the container element beans contained in a list satisfy a list of constraints.<p>
     * 
     * @param elements the elements for which the checks should be performed  
     * 
     * @param specs the constraints for the element beans, in the same order as the container element beans 
     */
    protected void checkSpec(List<CmsContainerElementBean> elements, String... specs) {

        assertEquals(specs.length, elements.size());
        for (int i = 0; i < elements.size(); i++) {
            CmsContainerElementBean elementBean = elements.get(i);
            String spec = specs[i];
            checkSpec(elementBean, spec);
        }
    }

    /**
     * Checks whether the inherited container configuration given by the ADE manager for a specific
     * root path satisfies a list of constraints.<p>
     * 
     * @param rootPath the root path for which the inherited container configuration should be tested
     * @param name the name of the configuration to test 
     * @param online if true, checks in the online project, else in the offline project 
     * @param specs the list of constraints
     *  
     * @throws CmsException if something goes wrong 
     */
    protected void checkSpecForPoint(String rootPath, String name, boolean online, String... specs) throws CmsException {

        CmsObject cms = getCmsObject();
        if (online) {
            cms = OpenCms.initCmsObject(cms);
            cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        }
        CmsInheritedContainerState state = OpenCms.getADEManager().getInheritedContainerState(cms, rootPath, name);
        List<CmsContainerElementBean> elementBeans = state.getElements(true);
        checkSpec(elementBeans, specs);
    }

    /**
     * Deletes a configuration at the given level of the /level1/level2/level3 tree branch.<p>
     * 
     * @param level the level id (1-3)
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void deleteConfiguration(int level) throws CmsException {

        String dirPath = getLevelPath(level);
        String configPath = CmsStringUtil.joinPaths(dirPath, ".container-config");
        deleteConfiguration(configPath);
    }

    protected void deleteConfiguration(String path) throws CmsException {

        CmsObject cms = getCmsObject();
        if (cms.existsResource(path)) {
            lock(path);
            cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
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
            CmsUUID.getConstantUUID(key),
            CmsUUID.getNullUUID(),
            settings,
            false);
        return elementBean;
    }

    /**
     * Helper method for generating a test configuration which contains a single configured container elements.<p>
     *  
     * @param newName the key of the container element which should be put in the container element 
     * @return the contents of the generated container element
     * 
     * @throws CmsException if something goes wrong 
     * @throws UnsupportedEncodingException
     */
    protected byte[] generateTestConfig(String newName) throws CmsException, UnsupportedEncodingException {

        CmsResource resource = getCmsObject().readResource("/system/content/" + newName + ".txt");
        CmsUUID structureId = resource.getStructureId();
        String rootPath = resource.getRootPath();

        String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
            + "\r\n"
            + "<AlkaconInheritConfigGroups xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.containerpage/schemas/inherit_config_group.xsd\">\r\n"
            + "  <AlkaconInheritConfigGroup language=\"en\">\r\n"
            + "    <Title><![CDATA[blah]]></Title>\r\n"
            + "    <Configuration>\r\n"
            + "      <Name><![CDATA[alpha]]></Name>\r\n"
            + "      <OrderKey><![CDATA["
            + newName
            + "]]></OrderKey>\r\n"
            + "      <NewElement>\r\n"
            + "        <Key><![CDATA["
            + newName
            + "]]></Key>\r\n"
            + "        <Element>\r\n"
            + "          <Uri>\r\n"
            + "            <link type=\"STRONG\">\r\n"
            + "              <target><![CDATA["
            + rootPath
            + "]]></target>\r\n"
            + "              <uuid>"
            + structureId.toString()
            + "</uuid>\r\n"
            + "            </link>\r\n"
            + "          </Uri>\r\n"
            + "        </Element>\r\n"
            + "      </NewElement>\r\n"
            + "    </Configuration>\r\n"
            + "  </AlkaconInheritConfigGroup>\r\n"
            + "</AlkaconInheritConfigGroups>\r\n";
        byte[] xmlData = xmlText.getBytes("UTF-8");
        return xmlData;

    }

    /**
     * Helper method for getting a path for the given level in the level1/level2/level3 tree branch.<p>
     * 
     * @param level the level index (1-3)
     * 
     * @return the level path 
     */
    protected String getLevelPath(int level) {

        switch (level) {
            case 1:
                return "/system/level1";
            case 2:
                return "/system/level1/level2";
            case 3:
                return "/system/level1/level2/level3";
            default:
                throw new IllegalArgumentException("Invalid level id!");
        }
    }

    protected String getNestedText(Element element) {

        return element.node(0).getText();
    }

    /**
     * Helper method to lock a resource.<p>
     * 
     * @param path the resource path 
     */
    protected void lock(String path) {

        try {
            getCmsObject().lockResource(path);
        } catch (CmsException e) {
            // no other users, this means we already have the lock 
        }
    }

    /**
     * Helper method which publishes the offline project.<p>
     * 
     * @throws Exception
     */
    protected void publish() throws Exception {

        CmsObject cms = getCmsObject();
        CmsPublishManager publishManager = OpenCms.getPublishManager();
        publishManager.publishProject(cms);
        publishManager.waitWhileRunning();
    }

    /**
     * Helper method for reading the contents of the path into a file.<p>
     * 
     * @param path the path of the resource which should be read 
     * @return the content of the resource
     * 
     * @throws Exception if something goes wrong 
     */
    protected String read(String path) throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(path);
        CmsFile file = cms.readFile(resource);
        return new String(file.getContents(), "UTF-8");
    }

    /**
     * Writes a dummy configuration file at a given level in the level1/level2/level3 tree branch.<p>
     * 
     * @param level the level at which to write the configuration file 
     * @param name the name for the element defined in the configuration file
     *  
     * @throws CmsException if something goes wrong 
     * @throws UnsupportedEncodingException
     */
    protected void writeConfiguration(int level, String name) throws CmsException, UnsupportedEncodingException {

        String dirPath = getLevelPath(level);
        String configPath = CmsStringUtil.joinPaths(dirPath, ".container-config");
        writeConfiguration(configPath, name);
    }

    protected void writeConfiguration(String path, String name) throws CmsException, UnsupportedEncodingException {

        String configPath = path;
        CmsObject cms = getCmsObject();
        if (cms.existsResource(configPath)) {
            lock(configPath);
            CmsFile file = cms.readFile(configPath);
            byte[] newContent = generateTestConfig(name);
            file.setContents(newContent);
            cms.writeFile(file);
        } else {
            byte[] newContent = generateTestConfig(name);
            cms.createResource(
                configPath,
                OpenCms.getResourceManager().getResourceType("inheritconfig").getTypeId(),
                newContent,
                new ArrayList<CmsProperty>());
        }
    }
}
