package com.opencms.modules.search.lucene;

/*
    $RCSfile: PdfParser.java,v $
    $Author: g.huhn $
    $Date: 2002/02/28 13:00:11 $
    $Revision: 1.5 $
    Copyright (C) 2000  The OpenCms Group
    This File is part of OpenCms -
    the Open Source Content Mananagement System
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    For further information about OpenCms, please see the
    OpenCms Website: http://www.opencms.com
    You should have received a copy of the GNU General Public License
    long with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
import java.util.zip.InflaterInputStream;
import java.io.*;
import java.util.*;
import java.text.*;

import java.util.List;

/**
 *  Bases on the PDFHandler from David Duddleston but serves some problems with
 *  serveral PDF-formats and the german Sonderzeichen.
 *
 *@author     grehuh
 *@created    28. Februar 2002
 */
public class PdfParser implements I_ContentParser {

    private InputStream in;
    private final static boolean debug = false;

    /*
        Input cache.  This is much faster than calling down to a synchronized
        method of BufferedReader for each byte.  Measurements done 5/30/97
        show that there's no point in having a bigger buffer:  Increasing
        the buffer to 8192 had no measurable impact for a program discarding
        one character at a time (reading from an http URL to a local machine).
      */
    private byte buf[] = new byte[256];
    private int pos;
    private int len;
    /*
        tracks position relative to the beginning of the
        document.
      */
    private int currentPosition;

    // 1996.07.10 15:08:56 PST
    SimpleDateFormat dateFormatter;

    // Content Data
    private String author;
    private long published;
    private String m_published;
    private String keywords;
    private String description;
    private String title;
    private StringBuffer contents;

    // Flags
    private boolean streamHit = false;
    private boolean parseNextStream = false;

    // Compression
    private final static int NONE = 0;
    private final static int FLATE = 1;
    private final static int LZW = 2;
    private int compression = NONE;

    // TOKENS
    private final static char[] AUTHOR = "/Author".toCharArray();
    private final static char[] CREATIONDATE = "/CreationDate".toCharArray();
    private final static char[] ENDSTREAM = "endstream".toCharArray();
    private final static char[] KEYWORDS = "/Keywords".toCharArray();
    private final static char[] STREAM = "stream".toCharArray();
    private final static char[] SUBJECT = "/Subject".toCharArray();
    private final static char[] TITLE = "/Title".toCharArray();
    private final static char[] NEWLINE = {'\n'};
    private final static char[] RETURN = {'\r'};
    private final static char[] PARAMSTART = {'<', '<'};

    private final static char[][] tokens = {
            AUTHOR, CREATIONDATE, ENDSTREAM, KEYWORDS, STREAM, SUBJECT,
            TITLE, PARAMSTART
            };


    /**
     *  PdfParser constructor comment.
     */
    public PdfParser() {
        contents = new StringBuffer();
        published = -1;

        // 19960710150856
        dateFormatter = new SimpleDateFormat("yyyyMMdd");
    }


    /**
     *  Look for tokens. This is not effiecent. Should use low, hi method with
     *  ordered array. NEED TO RECODE
     *
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    private char[] findToken() throws IOException {

        // flags if token still matches.
        boolean[] match = new boolean[tokens.length];
        for(int i = 0; i < match.length; i++) {
            match[i] = true;
        }

        // how many tokens still match;
        int matchCount = tokens.length;

        // current position to look for char match in tokens
        int charPosition = 0;

        // look for matching tokens.
        while(true) {
            int b = read();
            if(b == -1) {
                break;
            }
            char ch = (char) b;

            // loop through all tokens
            for(int i = 0; i < tokens.length; i++) {
                // check to see if match flag is true for this token

                if(match[i] == true) {
                    // get the token
                    char[] token = tokens[i];
                    // check if char array of token is in bounds
                    if(charPosition >= token.length) {
                        // out of bounds, check to see if other tokens still match
                        if(matchCount >= 2) {
                            // other tokens still match, set this one to false.
                            match[i] = false;
                            matchCount--;
                        } else {
                            // last matching token;
                            return token;
                        }
                        // token is in bounds, check for match on char at charPosition.
                    } else {
                        if(token[charPosition] != ch) {
                            // did not match, set match to false;
                            match[i] = false;
                            matchCount--;
                        }
                    }
                }
            }
            if(matchCount <= 0) {
                break;
            }
            charPosition++;
        }

        return null;
    }


    /**
     *  Parse Content.
     *
     *@return    The author value
     */
    public String getAuthor() {
        return author;
    }


    /**
     *  Return categories (from META tags)
     *
     *@return    The categories value
     */
    public String getCategories() {
        return null;
    }


    /**
     *  Parse Content.
     *
     *@return    The contents value
     */
    public String getContents() {
        return contents.toString();
    }


    /**
     *  Parse Content.
     *
     *@return    The description value
     */
    public String getDescription() {
        return description;
    }


    /**
     *  Return META HREF
     *
     *@return    The hREF value
     */
    public String getHREF() {
        return null;
    }


    /**
     *  Parse Content.
     *
     *@return    The keywords value
     */
    public String getKeywords() {
        return keywords;
    }


    /**
     *  Return links
     *
     *@return    The links value
     */
    public List getLinks() {
        return null;
    }


    /**
     *  Parse Content.
     *
     *@return    The published value
     */
    public String getPublished() {

        return m_published;
    }


    /**
     *@return    The title value
     */
    public String getTitle() {
        return title;
    }


    /**
     *  Check for new line chars
     *
     *@param  ch  true for new Line
     *@return     The newLineChar value
     */
    private boolean isNewLineChar(char ch) {
        switch (ch) {
            case '\n':
                return true;
            case '\r':
                return true;
            default:
                return false;
        }

    }


    /**
     *  Insert the method's description here. Creation date: (2/21/2001 7:50:24
     *  PM)
     *
     *@param  args  java.lang.String[]
     */
    public static void main(String[] args) {

        //System.out.println("test");
        try {
            //String path = "z:/webapps/dev/pdf/HealthPlansOnline.pdf";]
            String path = "D:/ablage/Urlaubsantrag3_02.pdf";
            PdfParser p = new PdfParser();
            p.parse(new FileInputStream(path));
            System.out.println("Title: " + p.getTitle());
            System.out.println("Author: " + p.getAuthor());
            System.out.println("Published " + p.getPublished());
            System.out.println("Keywords: " + p.getKeywords());
            System.out.println("Description: " + p.getDescription());
            System.out.println("Content: " + p.getContents());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Looks for new line.
     *
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    private boolean nextLine() throws IOException {
        //System.out.println("look for new line");
        while(true) {
            int b = read();
            if(b == -1) {
                return false;
            }
            if(isNewLineChar((char) b)) {
                return true;
            }
        }

    }


    /**
     *  Starts ParseContent.
     *
     *@param  in  Description of the Parameter
     */
    public void parse(InputStream in) {

        //System.out.println("mark supported" + in.markSupported());

        try {
            this.in = new BufferedInputStream(in);
            reset();
            parseContent();
            if(debug) {
                System.out.println("Title: " + getTitle());
                System.out.println("Author: " + getAuthor());
                System.out.println("Published " + getPublished());
                System.out.println("Keywords: " + getKeywords());
                System.out.println("Description: " + getDescription());
                System.out.println("Content: " + getContents());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Starts the Parsers for every Token.
     *
     *@exception  IOException  Description of the Exception
     */
    private void parseContent() throws IOException {
        Thread curThread = Thread.currentThread();
        while(true) {
            if(curThread.isInterrupted()) {
                curThread.interrupt();
                // resignal the interrupt
                break;
            }
            char[] token;
            while(true) {
                token = findToken();
                if(token != null) {
                    //System.out.println("found a token : " + token);
                    if(token == AUTHOR) {
                        author = parseData();
                    } else if(token == CREATIONDATE) {
                        m_published = parseDate();
                    } else if(token == KEYWORDS) {
                        keywords = parseData();
                    } else if(token == SUBJECT) {
                        description = parseData();
                    } else if(token == TITLE) {
                        title = parseData();
                    } else if(token == PARAMSTART) {
                        //System.out.println("param set mark");
                        in.mark(10000);
                        //parseDataParams();
                    } else if(token == STREAM) {
                        if(!streamHit) {
                            //System.out.println("new stream hit");
                            // first time this stream has been hit
                            // go back and parseDataParams.
                            in.reset();
                            streamHit = true;
                            parseDataParams();
                        } else {
                            //System.out.println("second stream hit");
                            if(parseNextStream) {
                                //System.out.println("parseDataStream");
                                contents.append(parseDataStream());
                                parseNextStream = false;
                            }
                            streamHit = false;
                        }
                    }
                }
                if(!nextLine()) {
                    //System.out.println("new line");
                    break;
                }
                //System.out.println("new line");
            }

            //System.out.println("hello");
            break;
        }
        replaceSpChars();
    }


    /**
     *  Look for tokens. This is not effiecent. Should use low, hi method with
     *  ordered array. NEED TO RECODE
     *
     *@return                  Description of the Return Value
     *@exception  IOException  IOException
     */
    private String parseData() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        // look for start '('
        while(true) {
            int b = read();
            if(b == -1) {
                break;
            }
            char ch = (char) b;
            if(ch == '(') {
                break;
            }
        }
        while(true) {
            int b = read();
            if(b == -1) {
                break;
            }
            char ch = (char) b;
            if(ch == ')') {
                break;
            }
            temp.write(b);
        }

        return new String(temp.toByteArray());
    }


    /**
     *  Look for tokens. This is not effiecent. Should use low, hi method with
     *  ordered array. NEED TO RECODE
     *
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    private String parseDataParams() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        boolean end = false;
        int b = read();

        while(true) {
            // check to see if new line;
            if((char) b == '>') {
                b = read();
                if((char) b == '>') {
                    end = true;
                    break;
                } else {
                    temp.write(b);
                }
            } else {
                temp.write(b);
            }
            if(end) {
                break;
            }
            b = read();
        }
        String params = new String(temp.toByteArray());
        //System.out.println(params.length());
        //System.out.println(params);
        if(params.length() < 38
        /*
            && params.indexOf("0 R") != -1
          */
                 && params.indexOf("/Length ") != -1) {
            if(params.indexOf("/FlateDecode") != -1) {
                compression = FLATE;
            }
            if(params.indexOf("/LZWDecode") != -1) {
                compression = LZW;
            }
            parseNextStream = true;
            //System.out.println();
            //System.out.println(params);
        }

        return new String(temp.toByteArray());
    }


    /**
     *  Look for tokens. This is not effiecent. Should use low, hi method with
     *  ordered array. NEED TO RECODE
     *
     *@return                  The content as a String
     *@exception  IOException  Description of the Exception
     */
    private String parseDataStream() throws IOException {

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        ByteArrayOutputStream tmp = new ByteArrayOutputStream(ENDSTREAM.length);
        boolean endstream = false;

        int b = read();
        char ch = (char) b;
        while(true) {
            // check to see if new line;
            if(isNewLineChar(ch)) {
                // check to see if it is endstream
                tmp.reset();
                boolean notMatch = false;
                for(int i = 0; i < ENDSTREAM.length; i++) {
                    b = read();
                    tmp.write(b);
                    if((char) b != ENDSTREAM[i]) {
                        // not endsteam break..
                        notMatch = true;
                        tmp.writeTo(temp);
                        break;
                    }
                }
                if(!notMatch) {
                    endstream = true;
                }
            } else {
                // not new line append byte
                temp.write(b);
                b = read();
                ch = (char) b;
            }
            if(endstream) {
                break;
            }
            // endstream found
        }

        // Uncompress if flateDecode is used
        if(compression == FLATE) {
            //System.out.println("FlateDecode = " +flateDecode);
            ByteArrayInputStream bis = new ByteArrayInputStream(temp.toByteArray());
            InflaterInputStream iin = new InflaterInputStream(bis);
            temp.reset();
            while((b = iin.read()) != -1) {
                temp.write(b);
            }
        }

        //System.out.println(temp.size());
        //System.out.println(new String(temp.toByteArray()));

        // parse content out from formating data. Content is wrapped in a
        // bunch of ()

        // look for start '('
        ByteArrayInputStream bis = new ByteArrayInputStream(temp.toByteArray());
        tmp.reset();
        boolean end = false;
        while(true) {
            b = bis.read();
            if(b == -1) {
                break;
            }
            if((char) b == '(') {
                while(true) {
                    b = bis.read();
                    if(b == -1) {
                        end = true;
                        break;
                    }
                    // look for end ')'
                    if((char) b == ')') {
                        break;
                    }
                    tmp.write(b);
                }
            }
            if(end) {
                break;
            }
        }

        // reset flateDecode flag
        compression = NONE;
        //System.out.println(tmp.size());
        //System.out.println(new String(tmp.toByteArray()));
        return new String(tmp.toByteArray());
    }


    /**
     *  To replace the german Sonderzeichen.
     */
    private void replaceSpChars() {

        int index = 0;
        int length = contents.length();
        boolean weiter;
        while(true) {
            weiter = false;
            if(index + 4 < length) {
                index = contents.toString().indexOf("\\237");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ue");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\212");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ae");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\232");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "oe");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\206");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "Ue");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\312");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, " ");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\320");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "-");
                    weiter = true;
                }
                /*
                    index=contents.toString().indexOf("\\237");
                    if (index!=-1) {
                    contents=contents.replace(index,index+4,"Ae");
                    weiter=true;
                    }
                    index=contents.toString().indexOf("\\237");
                    if (index!=-1) {
                    contents=contents.replace(index,index+4,"Oe");
                    weiter=true;
                    }
                  */
                index = contents.toString().indexOf("\\321");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, " ");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\247");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ss");
                    weiter = true;
                }
                //next codeset
                index = contents.toString().indexOf("\\344");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ae");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\304");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "Ae");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\374");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ue");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\334");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "Ue");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\366");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "oe");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\326");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "Oe");
                    weiter = true;
                }
                index = contents.toString().indexOf("\\337");
                if(index != -1) {
                    contents = contents.replace(index, index + 4, "ss");
                    weiter = true;
                }
            }
            if(!weiter) {
                break;
            }
        }
    }


    /**
     *  Format the parsed date.
     *
     *@return                  the publishing date in format dd.MM.yyyy
     *@exception  IOException  Description of the Exception
     */
    private String parseDate() throws IOException {

        try {
            String date = parseData();
            SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
            return sd.format(dateFormatter.parse(date.substring(2, 10)));
        } catch(ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     *  Read a character from the input-stream
     *
     *@return                  the read character as int
     *@exception  IOException  Description of the Exception
     */
    private final int read() throws IOException {

        ++currentPosition;
        return in.read();
    }


    /**
     *  Read a character from the input-stream
     *
     *@return                  the read character
     *@exception  IOException  Description of the Exception
     */
    private final char readCh() throws IOException {

        ++currentPosition;
        return (char) in.read();
    }


    /**
     *  Return contents
     */
    private void reset() {

        // Content
        title = null;
        description = null;
        keywords = null;
        author = null;

        contents.setLength(0);
        published = -1;

        // Flags
        streamHit = false;
        parseNextStream = false;
        compression = NONE;
    }
}
