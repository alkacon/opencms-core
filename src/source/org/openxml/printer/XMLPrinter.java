/**
 * The contents of this file are subject to the OpenXML Public
 * License Version 1.0; you may not use this file except in
 * compliance with the License. You may obtain a copy of the
 * License at http://www.openxml.org/license/
 *
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED ON AN "AS IS" BASIS
 * WITHOUT WARRANTY OF ANY KIND EITHER EXPRESSED OR IMPLIED,
 * INCLUDING AND WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE
 * AND DOCUMENTATION IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGING. SEE THE LICENSE FOR THE
 * SPECIFIC LANGUAGE GOVERNING RIGHTS AND LIMITATIONS UNDER THE
 * LICENSE.
 *
 * The Initial Developer of this code under the License is
 * OpenXML.org. Portions created by OpenXML.org and/or Assaf Arkin
 * are Copyright (C) 1998, 1999 OpenXML.org. All Rights Reserved.
 *
 * $Id: XMLPrinter.java,v 1.1 2000/01/13 13:44:20 a.lucas Exp $
 */


package source.org.openxml.printer;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;

import org.w3c.dom.*;
import org.xml.sax.DocumentHandler;
import org.xml.sax.AttributeList;


/**
 * Implements an XML printer supporting both DOM and SAX pretty
 * printing. For usage instructions see either {@link Printer}
 * or {@link BasePrinter}.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The printer supports both DOM and SAX. DOM printing is done
 * by calling {@link #print} and SAX printing is done by firing
 * SAX events and using the printer as a document handler.
 * <p>
 * If an I/O exception occurs while printing, the printer
 * will not throw an exception directly, but only throw it
 * at the end of printing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the printer will potentially break long text lines at space
 * boundaries, indent lines, and print elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:44:20 $
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see Printer
 * @see DocumentHandler
 */
public final class XMLPrinter
    extends BasePrinter
{


    //---------------------------------------//
    // SAX document handler printing methods //
    //---------------------------------------//


    public void startDocument()
    {
	if ( ! _format.getOmitXMLDeclaration() ) {
	    StringBuffer    buffer;

	    // Print the document declaration appreaing at the head
	    // of very XML document (unless asked not to).
	    buffer = new StringBuffer( "<?xml version=\"" );
	    if ( _format.getVersion() != null )
		buffer.append( _format.getVersion() );
	    else
		buffer.append( "1.0" );
	    buffer.append( '"' );
	    if ( _format.getEncoding() != null ) {
		buffer.append( " encoding=\"" );
		buffer.append( _format.getEncoding() );
		buffer.append( '"' );
	    }
	    if ( _format.getStandalone() && _format.getDoctypeSystem() == null &&
		 _format.getDoctypePublic() == null )
		buffer.append( " standalone=\"yes\"" );
	    buffer.append( "?>" );
	    printText( buffer.toString() );
	    breakLine();
	}
    }


    public void startElement( String tagName, AttributeList attrs )
    {
	int          i;
	boolean      preserveSpace;
	ElementState state;
	String       name;
	String       value;

	state = getElementState();
	if ( state == null ) {
	    // If this is the root element handle it differently.
	    // If the first root element in the document, print
	    // the document's DOCTYPE. Space preserving defaults
	    // to that of the output format.
	    if ( ! _started )
		startDocument( tagName );
	    preserveSpace = _format.getPreserveSpace();
	} else {
	    // For any other element, if first in parent, then
	    // close parent's opening tag and use the parnet's
	    // space preserving.
	    if ( state.empty )
		printText( ">" );
	    preserveSpace = state.preserveSpace;
	    // Indent this element on a new line if the first
	    // content of the parent element or immediately
	    // following an element.
	    if ( _format.getIndenting() && ! state.preserveSpace &&
		 ( state.empty || state.afterElement ) )
		breakLine();
	}
	// Do not change the current element state yet.
	// This only happens in endElement().

	printText( '<' + tagName );
	indent();

	// For each attribute print it's name and value as one part,
	// separated with a space so the element can be broken on
	// multiple lines.
	for ( i = 0 ; i < attrs.getLength() ; ++i ) {
	    printSpace();
	    name = attrs.getName( i );
	    value = attrs.getValue( i );
	    if ( value == null )
		value = "";
	    printText( name + "=\"" + escape( value ) + '"' );

	    // If the attribute xml:space exists, determine whether
	    // to preserve spaces in this and child nodes based on
	    // its value.
	    if ( name.equals( "xml:space" ) ) {
		if ( value.equals( "preserve" ) )
		    preserveSpace = true;
		else
		    preserveSpace = _format.getPreserveSpace();
	    }
	}

	// Now it's time to enter a new element state
	// with the tag name and space preserving.
	// We still do not change the curent element state.
	enterElementState( tagName, preserveSpace );
    }


    public void endElement( String tagName )
    {
	ElementState state;

	// Works much like content() with additions for closing
	// an element. Note the different checks for the closed
	// element's state and the parent element's state.
	unindent();
	state = getElementState();
	if ( state.empty ) {
	    printText( "/>" );
	} else {
	    // This element is not empty and that last content was
	    // another element, so print a line break before that
	    // last element and this element's closing tag.
	    if ( _format.getIndenting() && ! state.preserveSpace &&  state.afterElement )
		breakLine();
	    printText( "</" + tagName + ">" );
	}
	// Leave the element state and update that of the parent
	// (if we're not root) to not empty and after element.
	state = leaveElementState();
	if ( state != null ) {
	    state.afterElement = true;
	    state.empty = false;
	}
    }


    //----------------------------------------//
    // Generic node printing methods methods //
    //---------------------------------------//


    /**
     * Called to print the document's DOCTYPE by the root element.
     * The document type declaration must name the root element,
     * but the root element is only known when that element is printed,
     * and not at the start of the document.
     * <p>
     * This method will check if it has not been called before ({@link #_started}),
     * will print the document type declaration, and will print all
     * pre-root comments and PIs that were accumulated in the document
     * (see {@link #printPreRoot}). Pre-root will be printed even if
     * this is not the first root element of the document.
     */
    protected void startDocument( String rootTagName )
    {
	int i;

	if ( ! _started && _format.getDoctypeSystem() != null ) {
	    // System identifier must be specified to print DOCTYPE.
	    // If public identifier is specified print 'PUBLIC
	    // <public> <system>', if not, print 'SYSTEM <system>'.
	    printText( "<!DOCTYPE " );
	    printText( rootTagName );
	    if ( _format.getDoctypePublic() != null ) {
		printText( " PUBLIC " );
		printDoctypeURL( _format.getDoctypePublic() );
		breakLine();
		for ( i = 0 ; i < 18 + rootTagName.length() ; ++i )
		    printText( " " );
		printDoctypeURL( _format.getDoctypeSystem() );
	    }
	    else {
		printText( " SYSTEM " );
		printDoctypeURL( _format.getDoctypeSystem() );
	    }
	    printText( ">" );
	    breakLine();
	}
	_started = true;
	// Always print these, even if not te first root element.
	printPreRoot();
    }


    /**
     * Called to print a DOM element. Equivalent to calling {@link
     * #startElement}, {@link #endElement} and printing everything
     * inbetween, but better optimized.
     */
    protected void printElement( Element elem )
    {
	Attr         attr;
	NamedNodeMap attrMap;
	int          i;
	Node         child;
	ElementState state;
	boolean      preserveSpace;
	String       name;
	String       value;

	state = getElementState();
	if ( state == null ) {
	    // If this is the root element handle it differently.
	    // If the first root element in the document, print
	    // the document's DOCTYPE. Space preserving defaults
	    // to that of the output format.
	    if ( ! _started )
		startDocument( elem.getTagName() );
	    preserveSpace = _format.getPreserveSpace();
	} else {
	    // For any other element, if first in parent, then
	    // close parent's opening tag and use the parnet's
	    // space preserving.
	    if ( state.empty )
		printText( ">" );
	    preserveSpace = state.preserveSpace;
	    // Indent this element on a new line if the first
	    // content of the parent element or immediately
	    // following an element.
	    if ( _format.getIndenting() && ! state.preserveSpace &&
		 ( state.empty || state.afterElement ) )
		breakLine();
	}
	// Do not change the current element state yet.
	// This only happens in endElement().

	printText( '<' + elem.getTagName() );
	indent();

	// Lookup the element's attribute, but only print specified
	// attributes. (Unspecified attributes are derived from the DTD.
	// For each attribute print it's name and value as one part,
	// separated with a space so the element can be broken on
	// multiple lines.
	attrMap = elem.getAttributes();
	for ( i = 0 ; i < attrMap.getLength() ; ++i ) {
	    attr = (Attr) attrMap.item( i );
	    name = attr.getName();
	    value = attr.getValue();
	    if ( value == null )
		value = "";
	    if ( attr.getSpecified() ) {
		printSpace();
		printText( name + "=\"" + escape( value ) + '"' );
	    }
	    // If the attribute xml:space exists, determine whether
	    // to preserve spaces in this and child nodes based on
	    // its value.
	    if ( name.equals( "xml:space" ) ) {
		if ( value.equals( "preserve" ) )
		    preserveSpace = true;
		else
		    preserveSpace = _format.getPreserveSpace();		    
	    }
	}

	// If element has children, then print them, otherwise
	// print en empty tag.
	if ( elem.hasChildNodes() ) {
	    // Enter an element state, and print the children
	    // one by one. Finally, end the element.
	    enterElementState( elem.getTagName(), preserveSpace );
	    child = elem.getFirstChild();
	    while ( child != null ) {
		printNode( child );
		child = child.getNextSibling();
	    }
	    endElement( elem.getTagName() );
	} else {
	    unindent();
	    printText( "/>" );
	    // After element but parent element is no longer empty.
	    state.afterElement = true;
	    state.empty = false;
	}
    }


    protected String getEntityRef( char ch )
    {
	// Encode special XML characters into the equivalent character references.
	// These five are defined by default for all XML documents.
        switch ( ch ) {
	case '<':
	    return "lt";
	case '>':
	    return "gt";
	case '"':
	    return "quot";
	case '\'':
	    return "apos";
	case '&':
	    return "amp";
        }
        return null;
    }


    public XMLPrinter()
    {
	super();
    }


}


