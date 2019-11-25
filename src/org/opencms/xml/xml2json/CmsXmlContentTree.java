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

package org.opencms.xml.xml2json;

import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlContentDefinition.SequenceType;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.dom4j.Element;

/**
 * Tree representation of CmsXmlContent which is suitable for XML-to-JSON transformations.
 */
public class CmsXmlContentTree {

    /**
     * Field of a complex type.
     *
     * <p>
     * This represents one or more elements with the same field name in a sequence in an XML content.
     */
    public class Field {

        /** The field definition. */
        I_CmsXmlSchemaType m_fieldDef;

        /** The nodes for the individual field elements. */
        List<Node> m_nodes;

        /** The nested content definition (may be null). */
        private CmsXmlContentDefinition m_nestedDef;

        /** The parent node of the field. */
        protected Node m_parentNode;

        /**
        * Create a new instance.
        *
        * @param fieldDef the field definition
        * @param nodes the nodes for the individual field elements
        */
        public Field(I_CmsXmlSchemaType fieldDef, List<Node> nodes) {

            m_fieldDef = fieldDef;
            if (fieldDef instanceof CmsXmlNestedContentDefinition) {
                m_nestedDef = ((CmsXmlNestedContentDefinition)fieldDef).getNestedContentDefinition();
            }
            m_nodes = nodes;
        }

        /**
         * Gets the field definition.
         *
         * @return the field definition
         */
        public I_CmsXmlSchemaType getFieldDefinition() {

            return m_fieldDef;
        }

        /**
         * Gets the field name.
         *
         * @return the field name
         */
        public String getName() {

            return m_fieldDef.getName();
        }

        /**
         * Gets the node if there is exactly one in the node list, otherwise throws an error.
         *
         * @return the node
         */
        public Node getNode() {

            if (m_nodes.size() != 1) {
                throw new IllegalStateException(
                    "Can't call getNode for field with a number of nodes different from 1.");
            }
            return m_nodes.get(0);
        }

        /**
         * Gets the nodes for the individual elements with that field name.
         *
         * @return the sub-nodes for this field
         */
        public List<Node> getNodes() {

            return m_nodes;
        }

        /**
         * Gets the parent node.
         *
         * @return the parent node
         */
        public Node getParentNode() {

            return m_parentNode;
        }

        /**
         * Returns true if the field refers to a choice element with maxOccurs greater than 1.
         *
         * @return true if this is a multichoice attribute
         */
        public boolean isMultiChoice() {

            return (m_nestedDef != null) && (m_nestedDef.getChoiceMaxOccurs() > 1);

        }

        /**
         * Returns true if this is a multivalue field.
         *
         * @return true if this is a multivalue field
         */
        public boolean isMultivalue() {

            return m_fieldDef.getMaxOccurs() > 1;
        }

        /**
         * Returns true if this is an optional field.
         *
         * @return true if this is an optional field
         */
        public boolean isOptional() {

            return m_fieldDef.getMinOccurs() == 0;
        }

        /**
         * Sets the parent node of the field.
         *
         * @param parentNode the parent node
         */
        public void setParentNode(Node parentNode) {

            m_parentNode = parentNode;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            String result = "FL " + m_fieldDef.getName() + ":\n";
            for (Node child : m_nodes) {
                String entry = CmsStringUtil.indentLines(child.toString(), 4) + "\n";
                result += entry;

            }
            return result;
        }
    }

    /**
     * Represents a sequence in the XML content.
     */
    public class Node {

        /** The content definition. */
        private CmsXmlContentDefinition m_contentDefinition;

        /** The underlying element. */
        private Element m_elem;

        /** The list of sequence fields. */
        private List<Field> m_fields;

        /** The node type. */
        private NodeType m_type;

        /** The content value. */
        private I_CmsXmlContentValue m_value;

        /**
         * Creates a new instance.
         * @param type the node type
         * @param value the content value
         * @param contentDef the content definition
         * @param elem the underlying XML element
         * @param fields the fields
         */
        public Node(
            NodeType type,
            I_CmsXmlContentValue value,
            CmsXmlContentDefinition contentDef,
            Element elem,
            List<Field> fields) {

            m_type = type;
            m_contentDefinition = contentDef;
            m_elem = elem;
            m_fields = fields;
            m_value = value;
            if (m_fields != null) {
                for (Field field : m_fields) {
                    field.setParentNode(this);
                }
            } else {
                m_fields = Collections.emptyList();
            }
        }

        /**
         * Gets the content definition.
         *
         * @return the contnt definition
         */
        public CmsXmlContentDefinition getContentDefinition() {

            return m_contentDefinition;
        }

        /**
         * Gets the DOM element for the node.
         *
         * @return the DOM element
         */
        public Element getElement() {

            return m_elem;
        }

        /**
         * Gets the fields for the sequence.
         *
         * @return the list of fields
         */
        public List<Field> getFields() {

            return m_fields;
        }

        /**
         * Gets the path of the node.
         *
         * @return the path of the node
         */
        @SuppressWarnings("synthetic-access")
        public String getPath() {

            return getValuePath(m_elem);
        }

        /**
         * Gets the node type.
         *
         * @return the node type
         */
        public NodeType getType() {

            return m_type;
        }

        /**
         * Gets the content value (null for root node).
         *
         * @return the content value
         */
        public I_CmsXmlContentValue getValue() {

            return m_value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            StringBuilder buffer = new StringBuilder();
            buffer.append("ND:\n");
            for (Field field : m_fields) {
                buffer.append(CmsStringUtil.indentLines(field.toString(), 4));
            }
            return buffer.toString();

        }

    }

    /**
     * Enum representing the type of the tree node.
     */
    public enum NodeType {
        /** Complex value: choice. */
        choice,
        /** Complex value: sequence. */
        sequence,

        /** Simple value. */
        simple;
    }

    /** Map from values to nodes. */
    private IdentityHashMap<I_CmsXmlContentValue, Node> m_valueToNodeCache = new IdentityHashMap<>();

    /** The content. */
    private CmsXmlContent m_content;

    /** The locale for which the tree should be generated. */
    private Locale m_locale;

    /** The root node. */
    private Node m_root;

    /**
     * Creates a new instance and initializes the full tree for the given locale.
     *
     * @param content the content from which the tree should be generated
     * @param locale the locale for which the tree should be generated
     */
    public CmsXmlContentTree(CmsXmlContent content, Locale locale) {

        m_content = content;
        m_locale = locale;
        m_root = createNode(content.getLocaleNode(locale), content.getContentDefinition());
        visitNodes(m_root, node -> {
            if (node.getValue() != null) {
                m_valueToNodeCache.put(node.getValue(), node);
            }
        });
    }

    /**
     * Visits all Node instances that are descendants of a given node (including that node itself).
     *
     * @param node the root node
     * @param handler the handler to be invoked for all descendant nodes
     */
    public static void visitNodes(Node node, Consumer<Node> handler) {

        handler.accept(node);
        for (Field field : node.getFields()) {
            for (Node child : field.getNodes()) {
                visitNodes(child, handler);
            }
        }
    }

    /**
     * Creates a node for the given content definition and DOM element.
     *
     * @param elem the XML DOM element
     * @param contentDef the content definition (null for non-nested values)
     *
     * @return the created node
     */
    public Node createNode(Element elem, CmsXmlContentDefinition contentDef) {

        String path = getValuePath(elem);
        I_CmsXmlContentValue value = path.isEmpty() ? null : m_content.getValue(path, m_locale);
        if (contentDef == null) {
            Node node = new Node(NodeType.simple, value, null, elem, null);
            return node;
        }

        SequenceType seqType = contentDef.getSequenceType();
        if ((seqType == SequenceType.MULTIPLE_CHOICE) || (seqType == SequenceType.SINGLE_CHOICE)) {
            List<I_CmsXmlSchemaType> fieldDefinitions = contentDef.getTypeSequence();
            Map<String, I_CmsXmlSchemaType> defMap = new HashMap<>();
            for (I_CmsXmlSchemaType fieldDef : fieldDefinitions) {
                String fieldName = fieldDef.getName();
                defMap.put(fieldName, fieldDef);
            }
            List<Field> choiceFields = new ArrayList<>();
            for (Element child : elem.elements()) {
                I_CmsXmlSchemaType childFieldDef = defMap.get(child.getName());
                CmsXmlContentDefinition nestedDef = null;
                if (childFieldDef instanceof CmsXmlNestedContentDefinition) {
                    CmsXmlNestedContentDefinition nestedDefType = (CmsXmlNestedContentDefinition)childFieldDef;
                    nestedDef = nestedDefType.getNestedContentDefinition();
                }
                Node childNode = createNode(child, nestedDef);
                Field choiceField = new Field(childFieldDef, Arrays.asList(childNode));
                choiceFields.add(choiceField);
            }
            Node node = new Node(NodeType.choice, value, contentDef, elem, choiceFields);
            return node;
        }

        if (seqType == SequenceType.SEQUENCE) {
            List<I_CmsXmlSchemaType> fieldDefinitions = contentDef.getTypeSequence();
            List<Field> fields = new ArrayList<>();
            for (I_CmsXmlSchemaType fieldDef : fieldDefinitions) {
                CmsXmlContentDefinition nestedDef = null;
                if (fieldDef instanceof CmsXmlNestedContentDefinition) {
                    CmsXmlNestedContentDefinition nestedDefType = (CmsXmlNestedContentDefinition)fieldDef;
                    nestedDef = nestedDefType.getNestedContentDefinition();
                }
                String fieldName = fieldDef.getName();
                String fieldPath = CmsXmlUtils.concatXpath(path, fieldName);
                List<I_CmsXmlContentValue> fieldValues = m_content.getValues(fieldPath, m_locale);
                List<Node> fieldChildren = new ArrayList<>();
                for (int i = 0; i < fieldValues.size(); i++) {
                    Element subElement = fieldValues.get(i).getElement();
                    Node fieldChild = createNode(subElement, nestedDef);
                    fieldChildren.add(fieldChild);
                }
                Field field = new Field(fieldDef, fieldChildren);
                fields.add(field);
            }
            Node seqNode = new Node(NodeType.sequence, value, contentDef, elem, fields);
            return seqNode;
        }
        throw new IllegalStateException(
            "Invalid content definition type encounterered while processing " + m_content.getFile().getRootPath());
    }

    /**
     * Gets the node corresponding to the given value.
     *
     * @param value a content value
     * @return the node for the value, or null if no node is found
     */
    public Node getNodeForValue(I_CmsXmlContentValue value) {

        return m_valueToNodeCache.get(value);
    }

    /**
     * Returns the root node.
     *
     * @return the root node
     */
    public Node getRoot() {

        return m_root;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_content.getFile().getRootPath() + ":\n" + CmsStringUtil.indentLines(m_root.toString(), 4);
    }

    /**
     * Gets a path for an element which can be fed to CmsXmlContent.getValue().
     *
     * @param element the element
     * @return the path
     */
    private String getValuePath(Element element) {

        String fullPath = element.getUniquePath();
        String prefix = m_content.getLocaleNode(m_locale).getUniquePath();
        String result = fullPath.substring(prefix.length());
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

}
