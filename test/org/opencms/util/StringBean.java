
package org.opencms.util;

import java.io.Serializable;

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.Translate;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Extracts the HTML page content.<p>
 */
public class StringBean extends NodeVisitor implements Serializable {

    /**
     * A newline.
     */
    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * The length of the NEWLINE.
     */
    private static final int NEWLINE_SIZE = NEWLINE.length();

    private static final long serialVersionUID = 1596190888769126925L;

    /**
     * The buffer text is stored in while traversing the HTML.
     */
    protected StringBuffer m_buffer;

    /**
     * If <code>true</code> sequences of whitespace characters are replaced
     * with a single space character.
     */
    protected boolean m_collapse;

    /**
     * Set <code>true</code> when traversing a PRE tag.
     */
    protected boolean m_isPre;

    /**
     * Set <code>true</code> when traversing a SCRIPT tag.
     */
    protected boolean m_isScript;

    /**
     * Set <code>true</code> when traversing a STYLE tag.
     */
    protected boolean m_isStyle;

    /**
     * If <code>true</code> the link URLs are embedded in the text output.
     */
    protected boolean m_links;

    /**
     * The strings extracted from the URL.
     */
    protected String m_strings;

    /**
     * Create a StringBean object.
     * Default property values are set to 'do the right thing':
     * <p><code>Links</code> is set <code>false</code> so text appears like a
     * browser would display it, albeit without the colour or underline clues
     * normally associated with a link.</p>
     * <p><code>ReplaceNonBreakingSpaces</code> is set <code>true</code>, so
     * that printing the text works, but the extra information regarding these
     * formatting marks is available if you set it false.</p>
     * <p><code>Collapse</code> is set <code>true</code>, so text appears
     * compact like a browser would display it.</p>
     */
    public StringBean() {

        super(true, true);
        m_strings = null;
        m_links = false;
        m_collapse = true;
        m_buffer = new StringBuffer(4096);
        m_isScript = false;
        m_isPre = false;
        m_isStyle = false;
    }

    /**
     * Get the current 'collapse whitespace' state.
     * If set to <code>true</code> this emulates the operation of browsers
     * in interpretting text where <quote>user agents should collapse input
     * white space sequences when producing output inter-word space</quote>.
     * See HTML specification section 9.1 White space
     * <a href="http://www.w3.org/TR/html4/struct/text.html#h-9.1">
     * http://www.w3.org/TR/html4/struct/text.html#h-9.1</a>.
     * @return <code>true</code> if sequences of whitespace (space '&#92;u0020',
     * tab '&#92;u0009', form feed '&#92;u000C', zero-width space '&#92;u200B',
     * carriage-return '\r' and NEWLINE '\n') are to be replaced with a single
     * space.
     */
    public boolean getCollapse() {

        return (m_collapse);
    }

    /**
     * Get the current 'include links' state.
     * @return <code>true</code> if link text is included in the text extracted
     * from the URL, <code>false</code> otherwise.
     */
    public boolean getLinks() {

        return (m_links);
    }

    /**
     * Return the textual contents of the URL.
     * This is the primary output of the bean.
     * @return The user visible (what would be seen in a browser) text.
     */
    public String getStrings() {

        if (null == m_strings) {
            if (0 == m_buffer.length()) {
                setStrings();
            } else {
                updateStrings(m_buffer.toString());
            }
        }

        return (m_strings);
    }

    /**
     * Set the current 'collapse whitespace' state.
     * If the setting is changed after the URL has been set, the text from the
     * URL will be reacquired, which is possibly expensive.
     * @param collapse If <code>true</code>, sequences of whitespace
     * will be reduced to a single space.
     */
    public void setCollapse(boolean collapse) {

        boolean oldValue = m_collapse;
        if (oldValue != collapse) {
            m_collapse = collapse;
            setStrings();
        }
    }

    /**
     * Set the 'include links' state.
     * If the setting is changed after the URL has been set, the text from the
     * URL will be reacquired, which is possibly expensive.
     * @param links Use <code>true</code> if link text is to be included in the
     * text extracted from the URL, <code>false</code> otherwise.
     */
    public void setLinks(boolean links) {

        boolean oldValue = m_links;
        if (oldValue != links) {
            m_links = links;
            setStrings();
        }
    }

    /**
     * Resets the state of the PRE and SCRIPT flags.
     * @param tag The end tag to process.
     */
    @Override
    public void visitEndTag(Tag tag) {
        
        Node parent = tag.getParent();
        if (parent instanceof LinkTag) {
            if (getLinks()) { // appends the link as text between angle brackets to the output.
                m_buffer.append(" <");
                m_buffer.append(((LinkTag)parent).getLink());
                m_buffer.append(">");
            }
        }  
        
        String name = tag.getTagName().toUpperCase();
        if (name.equals("PRE")) {
            m_isPre = false;
        } else if (name.equals("SCRIPT")) {
            m_isScript = false;
        } else if (name.equals("STYLE")) {
            m_isStyle = false;
        }
        
        if (isHeadTag(name)) {
            carriageReturn();
            carriageReturn(true);
        }
        
        if (isTitleTag(name)) {
            m_buffer.append(" ]");            
            carriageReturn();
            carriageReturn(true);
        }
    }

    private boolean isTitleTag(String name) {
        
        return "TITLE".equals(name);
    }
    
    private boolean isHeadTag(String name) {

        return "H1".equals(name)
            || "H2".equals(name)
            || "H3".equals(name)
            || "H4".equals(name)
            || "H5".equals(name)
            || "H6".equals(name);
    }

    /**
     * Appends the text to the output.
     * @param string The text node.
     */
    @Override
    public void visitStringNode(Text string) {

        if (!m_isScript && !m_isStyle) {
            String text = string.getText();
            if (!m_isPre) {
                text = Translate.decode(text);
                text = text.replace('\u00a0', ' ');
                if (getCollapse()) {
                    collapse(m_buffer, text);
                } else {
                    m_buffer.append(text);
                }
            } else {
                m_buffer.append(text);
            }
        }
    }

    /**
     * Appends a NEWLINE to the output if the tag breaks flow, and
     * possibly sets the state of the PRE and SCRIPT flags.
     * @param tag The tag to examine.
     */
    @Override
    public void visitTag(Tag tag) {
            
        String name = tag.getTagName();
        if (name.equalsIgnoreCase("PRE")) {
            m_isPre = true;
        } else if (name.equalsIgnoreCase("SCRIPT")) {
            m_isScript = true;
        } else if (name.equalsIgnoreCase("STYLE")) {
            m_isStyle = true;
        }
        
        if (isHeadTag(name)) {
            carriageReturn(true);
            m_buffer.append("* ");
        } else if (isTitleTag(name)) { 
            m_buffer.append("[ ");        
        } else {
            if (tag.breaksFlow()) {
                carriageReturn();
            }
        }
        
    }

    /**
     * Appends a newline to the buffer if there isn't one there already.
     * Except if the buffer is empty.
     */
    protected void carriageReturn() {

        carriageReturn(false);
    }

    /**
     * Appends a newline to the buffer if there isn't one there already.
     * Except if the buffer is empty.
     * 
     * @param check a parameter the developer forgot to comment
     */
    protected void carriageReturn(boolean check) {

        int length;

        length = m_buffer.length();
        if ((0 != length) // don't append newlines to the beginning of a buffer
            && (check || ((NEWLINE_SIZE <= length) // not enough chars to hold a NEWLINE
            && (!m_buffer.substring(length - NEWLINE_SIZE, length).equals(NEWLINE))))) {

            m_buffer.append(NEWLINE);
        }
    }
    
    /**
     * Add the given text collapsing whitespace.
     * Use a little finite state machine:
     * <pre>
     * state 0: whitepace was last emitted character
     * state 1: in whitespace
     * state 2: in word
     * A whitespace character moves us to state 1 and any other character
     * moves us to state 2, except that state 0 stays in state 0 until
     * a non-whitespace and going from whitespace to word we emit a space
     * before the character:
     *    input:     whitespace   other-character
     * state\next
     *    0               0             2
     *    1               1        space then 2
     *    2               1             2
     * </pre>
     * @param buffer The buffer to append to.
     * @param string The string to append.
     */
    protected void collapse(StringBuffer buffer, String string) {

        int chars;
        int length;
        int state;
        char character;

        chars = string.length();
        if (0 != chars) {
            length = buffer.length();
            state = ((0 == length) || (buffer.charAt(length - 1) == ' ') || ((NEWLINE_SIZE <= length) && buffer.substring(
                length - NEWLINE_SIZE,
                length).equals(NEWLINE))) ? 0 : 1;
            for (int i = 0; i < chars; i++) {
                character = string.charAt(i);
                switch (character) {
                    // see HTML specification section 9.1 White space
                    // http://www.w3.org/TR/html4/struct/text.html#h-9.1
                    case '\u0020':
                    case '\u0009':
                    case '\u000C':
                    case '\u200B':
                    case '\r':
                    case '\n':
                        if (0 != state) {
                            state = 1;
                        }
                        break;
                    default:
                        if (1 == state) {
                            buffer.append(' ');
                        }
                        state = 2;
                        buffer.append(character);
                }
            }
        }
    }

    /**
     * Fetch the URL contents.
     * Only do work if there is a valid parser with it's URL set.
     */
    protected void setStrings() {

        m_strings = null;
        m_buffer = new StringBuffer(4096);
    }

    /**
     * Assign the <code>Strings</code> property, firing the property change.
     * @param strings The new value of the <code>Strings</code> property.
     */
    protected void updateStrings(String strings) {

        if ((null == m_strings) || !m_strings.equals(strings)) {
            m_strings = strings;
        }
    }
}
