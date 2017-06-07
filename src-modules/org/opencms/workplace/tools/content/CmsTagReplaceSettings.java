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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;

import org.htmlparser.Attribute;
import org.htmlparser.NodeFactory;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.util.ParserException;

/**
 * Bean to hold the settings needed for the operation of replacing HTML Tags of xmlpage resources in
 * the OpenCms VFS.
 * <p>
 *
 * @since 6.1.7
 *
 */
public final class CmsTagReplaceSettings {

    /**
     * Property for the tag-replace contentool to know the files that have been processed before in
     * case of early terminaton in previous runs.
     */
    public static final String PROPERTY_CONTENTOOLS_TAGREPLACE = "contentools.tagreplace";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTagReplaceSettings.class);

    /** Needed to verify if a path String denotes a folder in VFS. */
    private final CmsObject m_cms;

    /** The tags that should be deleted. */
    private final Set m_deleteTags;

    /** Used to create Tag instances for tags to delete of the proper type in a convenient way. */
    private NodeFactory m_nodeFactory;

    /**
     * The value of the shared {@link #PROPERTY_CONTENTOOLS_TAGREPLACE} to set on resources that
     * have been processed with these settings.
     */
    private String m_propertyValueTagReplaceID;

    /**
     * A map containing lower case tag names of tags to replace as keys and the replacement tag
     * names as their corresponding values.
     */
    private SortedMap m_tags2replacementTags;

    /** The root of all content files to process. */
    private String m_workPath;

    /**
     * Bean constructor with cms object for path validation.
     * <p>
     *
     * @param cms used to test the working path for valididty.
     */
    public CmsTagReplaceSettings(CmsObject cms) {

        // Treemap guarantees no duplicate keys (ambiguous replacements) and the same default
        // property ID's for the same replacement strings due to the ordering:
        m_tags2replacementTags = new TreeMap();
        m_cms = cms;
        // all tags are registered for creation
        m_nodeFactory = new PrototypicalNodeFactory();
        m_deleteTags = new TreeSet(new Comparator() {

            public int compare(Object o1, Object o2) {

                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
        });
    }

    /**
     * Returns the value of the shared {@link #PROPERTY_CONTENTOOLS_TAGREPLACE} to set on resources
     * that have been processed with these settings.
     * <p>
     *
     * @return the value of the shared {@link #PROPERTY_CONTENTOOLS_TAGREPLACE} to set on resources
     *         that have been processed with these settings.
     */
    public String getPropertyValueTagReplaceID() {

        return m_propertyValueTagReplaceID;
    }

    /**
     * Returns the replacements to perform in form of a comma-separated List of "key=value" tokens.
     * <p>
     *
     * @return the replacements to perform in form of a comma-separated List of "key=value" tokens.
     */
    public SortedMap getReplacements() {

        return m_tags2replacementTags;
    }

    /**
     * Returns the path under which files will be processed recursively.
     * <p>
     *
     * @return the path under which files will be processed recursively.
     */
    public String getWorkPath() {

        return m_workPath;
    }

    /**
     * Sets the value of the shared {@link #PROPERTY_CONTENTOOLS_TAGREPLACE} to set on resources
     * that have been processed with these settings.
     * <p>
     *
     * @param propertyValueTagreplaceID the value of the shared
     *            {@link #PROPERTY_CONTENTOOLS_TAGREPLACE} to set on resources that have been
     *            processed with these settings.
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setPropertyValueTagReplaceID(String propertyValueTagreplaceID) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propertyValueTagreplaceID)) {
            m_propertyValueTagReplaceID = getDefaultTagReplaceID();
        } else {
            m_propertyValueTagReplaceID = propertyValueTagreplaceID;
        }
    }

    /**
     * Sets the replacements to perform in form of a comma-separated List of "key=value" tokens.
     * <p>
     *
     * @param replacements the replacements to perform in form of a comma-separated List of
     *            "key=value" tokens.
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setReplacements(SortedMap replacements) throws CmsIllegalArgumentException {

        Iterator itMappings = replacements.entrySet().iterator();
        Map.Entry entry;
        String key, value;
        while (itMappings.hasNext()) {
            entry = (Map.Entry)itMappings.next();
            key = (String)entry.getKey();
            value = (String)entry.getValue();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                // removal
                Tag deleteTag;
                String tagName = (key).toLowerCase().trim();
                try {
                    Vector attributeList = new Vector(1);
                    Attribute tagNameAttribute = new Attribute();
                    tagNameAttribute.setName(tagName);
                    attributeList.add(tagNameAttribute);
                    deleteTag = m_nodeFactory.createTagNode(null, 0, 0, attributeList);
                    m_deleteTags.add(deleteTag);
                    itMappings.remove();
                } catch (ParserException e) {
                    CmsMessageContainer container = Messages.get().container(
                        Messages.GUI_ERR_TAGREPLACE_TAGNAME_INVALID_1,
                        tagName);
                    throw new CmsIllegalArgumentException(container, e);
                }
            } else {
                // nop
            }
            m_tags2replacementTags = replacements;
        }
        // if setPropertyValueTagReplaceID has been invoked earlier with empty value
        // due to missing user input:
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_propertyValueTagReplaceID)) {
            // trigger computation of default ID by empty value:
            setPropertyValueTagReplaceID(null);
        }
    }

    /**
     * Sets the path under which files will be processed recursively.
     * <p>
     *
     * @param workPath the path under which files will be processed recursively.
     *
     * @throws CmsIllegalArgumentException if the argument is not valid.
     */
    public void setWorkPath(String workPath) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(workPath)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_ERR_WIDGETVALUE_EMPTY_0));
        }
        // test if it is a valid path:
        if (!m_cms.existsResource(workPath)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.GUI_ERR_TAGREPLACE_WORKPATH_1, workPath));
        }
        m_workPath = workPath;

    }

    /**
     * Returns the Set&lt;{@link org.htmlparser.Tag}&gt; to delete from transformed output.
     * <p>
     *
     * @return the Set&lt;{@link org.htmlparser.Tag}&gt; to delete from transformed output.
     */
    protected Set getDeleteTags() {

        return m_deleteTags;
    }

    /**
     * Transforms the given Tag into the one it has to become by changing it's name and/or
     * attributes.
     * <p>
     *
     * @param tag the tag to be transformed.
     *
     * @return true if the given tag was modified, false else.
     *
     */
    protected boolean replace(org.htmlparser.Tag tag) {

        boolean result = false;
        String tagName = tag.getTagName().trim().toLowerCase();
        String replacementName = (String)m_tags2replacementTags.get(tagName);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(replacementName)) {
            // judge this as a bug: getTagName() returns plain name, setter needs leading '/' for
            // TODO: when updating htmlparser, verify if this has changed / been fixed
            // closing tags
            if (tag.isEndTag()) {
                replacementName = "/" + replacementName;
            }
            tag.setTagName(replacementName);
            result = true;
            // clear the attributes too:
            List attributes = tag.getAttributesEx();
            Iterator itAttribs = attributes.iterator();
            // skip the "tagname attribute"....
            itAttribs.next();
            Attribute attribute;
            String attName;
            while (itAttribs.hasNext()) {
                attribute = (Attribute)itAttribs.next();
                attName = attribute.getName();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(attName)) {
                    // this is the case for e.g. <h1 >
                    // -> becomes a tag with an attribute for tag name and a null name attribute
                    // (for the whitespace!)
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_DEBUG_TAGREPLACE_TAG_REMOVE_ATTRIB_2,
                                attName,
                                tag.getTagName()));

                    }
                    itAttribs.remove();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_TAG_REMOVE_ATTRIB_OK_0));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Computes the default property value for resources that have to be marked as "processed by
     * these replacement settings".
     * <p>
     *
     * The default value will be the alphabetically sorted string for replacments or the empty
     * String if the replacements have not been set before.
     * <p>
     *
     * @return the default property value for resources that have to be marked as "processed by
     *         these replacement settings".
     */
    private String getDefaultTagReplaceID() {

        if (m_tags2replacementTags.size() == 0) {
            return ""; // to know that no replacements were set before and the ID will still have
            // to be computed later
        } else {
            StringBuffer result = new StringBuffer();
            Map.Entry entry;
            Iterator itEntries = m_tags2replacementTags.entrySet().iterator();
            while (itEntries.hasNext()) {
                entry = (Map.Entry)itEntries.next();
                result.append(entry.getKey()).append('=').append(entry.getValue());
                if (itEntries.hasNext()) {
                    result.append(',');
                }
            }
            return result.toString();

        }
    }
}
