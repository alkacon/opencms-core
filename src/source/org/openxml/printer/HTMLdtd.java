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
 * $Id: HTMLdtd.java,v 1.1 2000/02/20 16:11:19 a.lucas Exp $
 */


package source.org.openxml.printer;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Hashtable;


/**
 * Utility class for accessing information specific to HTML documents.
 * The HTML DTD is expressed as three utility function groups. Two methods
 * allow for checking whether an element requires an open tag on printing
 * ({@link #isEmptyTag}) or on parsing ({@link #isOptionalClosing}).
 * <P>
 * Two other methods translate character references from name to value and
 * from value to name. A small entities resource is loaded into memory the
 * first time any of these methods is called for fast and efficient access.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2000/02/20 16:11:19 $
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 */
final class HTMLdtd
{


    /**
     * Returns true if element is declared to be empty. HTML elements are
     * defines as empty in the DTD, not by the document syntax.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element is empty
     */
    public static boolean isEmptyTag( String tagName )
    {
        // BR AREA LINK IMG PARAM HR INPUT COL BASE META BASEFONT ISINDEX
        return ( tagName.equals( "BR" ) || tagName.equals( "AREA" ) ||
                 tagName.equals( "LINK" ) || tagName.equals( "IMG" ) ||
                 tagName.equals( "PARAM" ) || tagName.equals( "HR" ) ||
                 tagName.equals( "INPUT" ) || tagName.equals( "COL" ) ||
                 tagName.equals( "BASE" ) || tagName.equals( "META" ) ||
                 tagName.equals( "BASEFONT" ) || tagName.equals( "ISINDEX" ) );
    }


    /**
     * Returns true if element is declared to have element content.
     * Whitespaces appearing inside element content will be ignored,
     * other text will simply report an error.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element content
     */
    public static boolean isElementContent( String tagName )
    {
        // DL OL UL SELECT OPTGROUP TABLE THEAD TFOOT TBODY COLGROUP TR HEAD HTML
        return ( tagName.equals( "DL" ) || tagName.equals( "OL" ) ||
                 tagName.equals( "UL" ) || tagName.equals( "SELECT" ) ||
                 tagName.equals( "OPTGROUP" ) || tagName.equals( "TABLE" ) ||
                 tagName.equals( "THEAD" ) || tagName.equals( "TFOOT" ) ||
                 tagName.equals( "TBODY" ) || tagName.equals( "COLGROUP" ) ||
                 tagName.equals( "TR" ) || tagName.equals( "HEAD" ) ||
                 tagName.equals( "HTML" ) );
    }

    
    /**
     * Returns true if element's textual contents preserves spaces.
     * This only applies to PRE and TEXTAREA, all other HTML elements
     * do not preserve space.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element's text content preserves spaces
     */
    public static boolean isPreserveSpace( String tagName )
    {
        // PRE TEXTAREA
        return ( tagName.equals( "PRE" ) || tagName.equals( "TEXTAREA" ) );
    }


    /**
     * Returns true if element's closing tag is optional and need not
     * exist. An error will not be reported for such elements if they
     * are not closed. For example, <tt>LI</tt> is most often not closed.
     *
     * @param tagName The element tag name (upper case)
     * @return True if closing tag implied
     */
    public static boolean isOptionalClosing( String tagName )
    {
        // BODY HEAD HTML P DT DD LI OPTION THEAD TFOOT TBODY TR COLGROUP TH TD
        return ( tagName.equals( "BODY" ) || tagName.equals( "HEAD" ) ||
                 tagName.equals( "HTML" ) || tagName.equals( "P" ) ||
                 tagName.equals( "DT" ) || tagName.equals( "DD" ) ||
                 tagName.equals( "LI" ) || tagName.equals( "OPTION" ) ||
                 tagName.equals( "THEAD" ) || tagName.equals( "TFOOT" ) ||
                 tagName.equals( "TBODY" ) || tagName.equals( "TR" ) ||
                 tagName.equals( "COLGROUP" ) || tagName.equals( "TH" ) ||
                 tagName.equals( "TD" ) );
    }


    /**
     * Returns true if the opening of one element (<tt>tagName</tt>) implies
     * the closing of another open element (<tt>openTag</tt>). For example,
     * every opening <tt>LI</tt> will close the previously open <tt>LI</tt>,
     * and every opening <tt>BODY</tt> will close the previously open <tt>HEAD</tt>.
     *
     * @param tagName The newly opened element
     * @param openTag The already opened element
     * @return True if closing tag closes opening tag
     */    
    public static boolean isClosing( String tagName, String openTag )
    {
        // BODY (closing HTML, end of document)
        // HEAD (BODY, closing HTML, end of document)
        if ( openTag.equals( "HEAD" ) )
            return ! ( tagName.equals( "ISINDEX" ) || tagName.equals( "TITLE" ) ||
		       tagName.equals( "META" ) || tagName.equals( "SCRIPT" ) ||
		       tagName.equals( "STYLE" ) );
        // P (P, H1-H6, UL, OL, DL, PRE, DIV, BLOCKQUOTE, FORM, HR, TABLE, ADDRESS, FIELDSET, closing BODY, closing HTML, end of document)
        if ( openTag.equals( "P" ) )
            return ( tagName.endsWith( "P" ) || tagName.endsWith( "H1" ) ||
                     tagName.endsWith( "H2" ) || tagName.endsWith( "H3" ) ||
                     tagName.endsWith( "H4" ) || tagName.endsWith( "H5" ) ||
                     tagName.endsWith( "H6" ) || tagName.endsWith( "UL" ) ||
                     tagName.endsWith( "OL" ) || tagName.endsWith( "DL" ) ||
                     tagName.endsWith( "PRE" ) || tagName.endsWith( "DIV" ) ||
                     tagName.endsWith( "BLOCKQUOTE" ) || tagName.endsWith( "FORM" ) ||
                     tagName.endsWith( "HR" ) || tagName.endsWith( "TABLE" ) ||
                     tagName.endsWith( "ADDRESS" ) || tagName.endsWith( "FIELDSET" ) );
        // DT (DD)
        if ( openTag.equals( "DT" ) )
            return tagName.endsWith( "DD" );
        // DD (DT, closing DL)
        if ( openTag.equals( "DD" ) )
            return tagName.endsWith( "DT" );
        // LI (LI, closing UL/OL)
        if ( openTag.equals( "LI" ) )
            return tagName.endsWith( "LI" );
        // OPTION (OPTION, OPTGROUP closing or opening, closing SELECT)
        if ( openTag.equals( "OPTION" ) )
            return tagName.endsWith( "OPTION" );
        // THEAD (TFOOT, TBODY, TR, closing TABLE
        // TFOOT (TBODY, TR, closing TABLE)
        // TBODY (TBODY, closing TABLE)
        // COLGROUP (THEAD, TBODY, TR, closing TABLE)
        // TR (TR, closing THEAD, TFOOT, TBODY, TABLE)
        if ( openTag.equals( "THEAD" ) || openTag.equals( "TFOOT" ) ||
             openTag.equals( "TBODY" ) || openTag.equals( "TR" ) || 
             openTag.equals( "COLGROUP" ) )
            return ( tagName.endsWith( "THEAD" ) || tagName.endsWith( "TFOOT" ) ||
                     tagName.endsWith( "TBODY" ) || tagName.endsWith( "TR" ) ||
                     tagName.endsWith( "COLGROUP" ) );
        // TH (TD, TH, closing TR)
        // TD (TD, TH, closing TR)
        if ( openTag.equals( "TH" ) || openTag.equals( "TD" ) )
            return ( tagName.endsWith( "TD" ) || tagName.endsWith( "TH" ) );
        return false;
    }

        
    /**
     * Returns the value of an HTML character reference by its name. If the
     * reference is not found or was not defined as a character reference,
     * returns EOF (-1).
     *
     * @param name Name of character reference
     * @return Character code or EOF (-1)
     */
    public static int charFromName( String name )
    {
        Object    value;

        initialize();
        value = _byName.get( name );
        if ( value != null && value instanceof Character )
            return ( (Character) value ).charValue();
        else
            return -1;
    }


    /**
     * Returns the name of an HTML character reference based on its character
     * value. Only valid for entities defined from character references. If no
     * such character value was defined, return null.
     *
     * @param value Character value of entity
     * @return Entity's name or null
     */
    public static String fromChar( char value )
    {
        String    name;

        initialize();
        name = (String) _byChar.get( String.valueOf( value ) );
        if ( name == null )
            return null;
        else
            return name;
    }


    /**
     * Initialize upon first access. Will load all the HTML character references
     * into a list that is accessible by name or character value and is optimized
     * for character substitution. This method may be called any number of times
     * but will execute only once.
     */
    private static void initialize()
    {
        InputStream     is = null;
        BufferedReader  reader = null;
        int             index;
        String          name;
        String          value;
        int             code;
        String          line;

        // Make sure not to initialize twice.
        if ( _byName != null )
            return;
        try
        {
            _byName = new Hashtable();
            _byChar = new Hashtable();
            is = HTMLdtd.class.getResourceAsStream( ENTITIES_RESOURCE );
            if ( is == null )
                throw new RuntimeException( "The resource [" + ENTITIES_RESOURCE + "] could not be found." );
            reader = new BufferedReader( new InputStreamReader( is ) );
            line = reader.readLine();
            while ( line != null )
            {
                if ( line.length() == 0 || line.charAt( 0 ) == '#' )
                {
                    line = reader.readLine();
                    continue;
                }
                index = line.indexOf( ' ' );
                if ( index > 1 )
                {
                    name = line.substring( 0, index );
                    ++index;
                    if ( index < line.length() )
                    {
                        value = line.substring( index );
                        index = value.indexOf( ' ' );
                        if ( index > 0 )
                            value = value.substring( 0, index );
                        code = Integer.parseInt( value );
                        defineEntity( name, (char) code );
                    }
                }
                line = reader.readLine();
            }
            is.close();
        }
        catch ( Exception except )
        {
            throw new RuntimeException( "The resource [" + ENTITIES_RESOURCE + "] could not load: " +
					except.toString() );
        }
        finally
        {
            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( Exception except )
                {
                }
            }
        }
    }


    /**
     * Defines a new character reference. The reference's name and value are
     * supplied. Nothing happens if the character reference is already defined.
     * <P>
     * Unlike internal entities, character references are a string to single
     * character mapping. They are used to map non-ASCII characters both on
     * parsing and printing, primarily for HTML documents. '&lt;amp;' is an
     * example of a character reference.
     *
     * @param name The entity's name
     * @param value The entity's value
     */
    private static void defineEntity( String name, char value )
    {
        if ( _byName.get( name ) == null )
        {
            _byName.put( name, new Character( value ) );
            _byChar.put( String.valueOf( value ), name );
        }
    }


    /**
     * Table of reverse character reference mapping. Character codes are held
     * as single-character strings, mapped to their reference name.
     */
    private static Hashtable        _byChar;


    /**
     * Table of entity name to value mapping. Entities are held as strings,
     * character references as <TT>Character</TT> objects.
     */
    private static Hashtable        _byName;


    /**
     * Locates the HTML entities file that is loaded upon initialization.
     * This file is a resource loaded with the default class loader.
     */
    private static final String     ENTITIES_RESOURCE = "HTMLEntities.res";


}

