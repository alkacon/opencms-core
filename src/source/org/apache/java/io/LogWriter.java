/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

package source.org.apache.java.io;

import java.io.*;
import java.text.*;
import java.util.*;
import source.org.apache.java.util.*;

/**
 * A <code>LogWriter</code> allows an application to incapsulate an output
 * stream and dumps its logs on it.
 * <p>
 * To control the different kinds of log messages they are separated into
 * <i>channels</i> that can be activated setting to <i>true</i> the right
 * property.
 * <p>
 * The configurations that control this writer are:
 * <ul>
 *  <li>"identifier"   a boolean value that enables the whole logging
 *  <li>"identifier".dateFormat   the date format used to time stamp
 *  <li>"identifier".timestamp   a boolean value that enables time stamping
 *  <li>"identifier".channel."channelName"  a boolean value that enables
 *  the specified channel
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/05 17:27:18 $
 */

public class LogWriter 
  implements Logger {

    /**
     * This string identifies this log writer and its used to
     * differentiate between configurations.
     */
    public static final String DEFAULT_IDENTIFIER = "log";

    /**
     * This string control the standard date format used
     * used for log timestamping.
     */
    public static final String DEFAULT_DATEFORMAT = "[dd/MM/yyyy HH:mm:ss zz] ";

    public static final String KEYWORD_DATEFORMAT = "dateFormat";
	
	public static final String C_TOKEN_CHANNELFORMAT = "%channel";

	public static final String C_DEFAULT_CHANNELFORMAT = "<%channel>";
	
    public static final String C_KEYWORD_CHANNELFORMAT = "channelFormat";
	
	public static final String C_DEFAULT_SEPERATOR = " ";
	
	public static final String C_KEYWORD_SEPERATOR = "channelSeperator";
	
    public static final String KEYWORD_TIMESTAMP = "timestamp";
    
    public static final String KEYWORD_CHANNEL = "channel";

    public static final String KEYWORD_FILE = "file";
    
    public static final String KEYWORD_QUEUE_MAXAGE = "queue.maxage";
    
    public static final String KEYWORD_QUEUE_MAXSIZE = "queue.maxsize";

    public static final String CH_QUEUE_STATUS = "info";
    	
    /**
     * String identifier for the exception tracing channel.
     */
    public static final String CH_EXCEPTION_TRACING = "servletException";
    
    /**
     * Configuration parameters.
     */
    public Configurations configurations;

    /**
     * Tells if this log is active
     */
    public boolean active = false;

    /**
     * Maximum age of a queued log record, in milliseconds.
     * <p>
     * 
     * When the {@link #logDaemon log daemon} detects the record older than
     * this, it flushes the queue, just to make the observer happy, even if
     * that is a single message in the queue.
     *
     * <p>
     * Default is hardcoded to 5000 ms.
     */
    private long queue_maxage;

    /**   
     * Maximum size of a log queue.
     * <p>
     *
     * When one of the <code>log(...)</code> messages detects that the log
     * queue size is more than this variable permits, it flushes the queue,
     * to keep the memory clean.
     * <p>
     *
     * Default is 1000.
     */
    private long queue_maxsize;

    /**
     * This string identifies this log writer and its used to
     * discriminate between configuration parameters.
     */
    private String identifier;

    /**
     * Tells if the log message should be started by a time stamp.
     * Default is false.
     */
    private boolean timestamp;

    /**
     * The timestamp formatter.
     */
    private SimpleDateFormat formatter;

	/**
	 * Format of the channels.
	 */
	private String m_channelStart;
	private String m_channelEnd;
	
	/**
	 * Seperator token.
	 */
	private String m_channelSeperator;
	
    /**
     * The writer encapsulated.
     */
    private PrintWriter writer;

    /**
     * Background logger thread.
     */
    private Agent logDaemon;
    
    /**
     * Tells if the log message should contain a channel.
     * Default is false.
     */
    private boolean logChannel;
    /**
     * Constructs this class and gets output file from configurations.
     *
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (Configurations configurations) throws IOException {
        this((Writer) null, DEFAULT_IDENTIFIER, configurations);
    }

    /**
     * Constructs this class using identifier to discriminate between
     * configuration parameters and gets output file from configurations.
     *
     * @param   identifier    the identifier for this log writer.
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (String identifier, Configurations configurations) throws IOException {
        this((Writer) null, identifier, configurations);
    }

    /**
     * Constructs this class with the output writer to encapsulate using
     * default identifier.
     *
     * @param   output        the output writer to encapsulate.
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (Writer output, Configurations configurations) throws IOException {
        this(output, DEFAULT_IDENTIFIER, configurations);
    }

    /**
     * Constructs this class with the output stream to encapsulate using
     * default identifier.
     *
     * @param   output        the output stream to encapsulate.
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (OutputStream output, Configurations configurations) throws IOException {
        this(new BufferedWriter(new OutputStreamWriter(output)), DEFAULT_IDENTIFIER, configurations);
    }

    /**
     * Constructs this class with the output stream to encapsulate.
     * The log identifier is used to discriminate between configuration
     * parameters.
     *
     * @param   output        the output stream to encapsulate.
     * @param   identifier    the identifier for this log writer.
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (OutputStream output, String identifier, Configurations configurations) throws IOException {
        this(new BufferedWriter(new OutputStreamWriter(output)), identifier, configurations);
    }

    /**
     * Constructs this class with the output writer to encapsulate.
     * The log identifier is used to discriminate between configuration
     * parameters.
     *
     * @param   output        the output writer to encapsulate.
     * @param   identifier    the identifier for this log writer.
     * @param   configurations    the configurations needed at initialization.
     */
    public LogWriter (Writer output, String identifier, Configurations configurations) throws IOException {
        if (configurations == null) {
            return;
        } else {
            this.configurations = configurations;
        }

        if (identifier == null) {
            this.identifier = DEFAULT_IDENTIFIER;
        } else {
            this.identifier = identifier;
        }

        this.active = configurations.getBoolean(identifier, false);
        this.logChannel = configurations.getBoolean(identifier+"."+KEYWORD_CHANNEL, false);
		
		this.m_channelSeperator = configurations.getString(identifier + "." + C_KEYWORD_SEPERATOR, C_DEFAULT_SEPERATOR);
		if (m_channelSeperator == null) m_channelSeperator = "";
		
		String channelStamp = configurations.getString(identifier + "." + C_KEYWORD_CHANNELFORMAT, C_DEFAULT_CHANNELFORMAT);
		int pos;
		if ((pos = channelStamp.indexOf(C_TOKEN_CHANNELFORMAT)) < 0) {
			// Invalid channel stamp, replace with default
			channelStamp = C_DEFAULT_CHANNELFORMAT;
			pos = channelStamp.indexOf(C_TOKEN_CHANNELFORMAT);
		}
		m_channelStart = m_channelSeperator + channelStamp.substring(0, pos);
		m_channelEnd = channelStamp.substring(pos + C_TOKEN_CHANNELFORMAT.length());
		
        if (active) {
            if ((output == null)) {
                String logFileName = configurations.getString(identifier + "." + KEYWORD_FILE);
                if (logFileName == null) {
                    throw new IOException("Log is active, but log file ("
                                          + identifier + "." + KEYWORD_FILE
                                          + ") is not specified");
                }
                File file = new File(logFileName);
                if (file.exists()) {
                    // make sure the file it's writable so the reason is obvious
                    // instead of FileNotFoundException
                    if (!file.canWrite()) {
                        throw new IOException("Not writable: " + file.getAbsolutePath());
                    }
                } else {
                    try {
                        // make sure the directory in which this file is located
                        // is writable.
                        File parent = new File(file.getParent());
                        if (!parent.canWrite()) {
                            throw new IOException("Directory not writable: "
                                + parent.getAbsolutePath() );
                        }
                    } catch (NullPointerException e) {
                        // This means that the file doesn't have a parent.
                    }
                }

                this.writer = new PrintWriter(new FileWriter(file.getAbsolutePath(), true));
            } else {
                this.writer = new PrintWriter(output);
            }

            this.timestamp = configurations.getBoolean(identifier + "." + KEYWORD_TIMESTAMP, false);
            if (timestamp) {
                String dateFormat = configurations.getString(identifier + "." + KEYWORD_DATEFORMAT, DEFAULT_DATEFORMAT);
                formatter = new SimpleDateFormat(dateFormat);
                /*
                 * This is a workaround for the bug in SimpleDateFormat that
                 * doesn't use TimeZone.getDefault() when constructing
                 * its format. They use an hard coded PST.
                 */
                formatter.setTimeZone(TimeZone.getDefault());
            }
            
            this.queue_maxage = configurations.getInteger(KEYWORD_QUEUE_MAXAGE, 5000);
            this.queue_maxsize = configurations.getInteger(KEYWORD_QUEUE_MAXSIZE, 10000);

            this.logDaemon = new Agent();
            this.logDaemon.setDaemon(true);
            this.logDaemon.start();
        }
    }

    /**
     * Tells if it is active.
     */
    public boolean isActive() { return active; }

    /**
     * Tells if the given channel is active.
     *
     * @param  channel  the channel to test.
     */
    public boolean isActive(String channel) {
        return ((channel != null) && (configurations.getBoolean(identifier + "." + KEYWORD_CHANNEL + "." + channel, false)));
    }

    /**
     * Prints the log message on the right channel.
     * <p>
     * A "channel" is a virtual log that may be enabled or disabled by
     * setting the property "identifier".channel.???=true where ??? is the
     * channel identifier that must be passed with the message.
     * If a channel is not recognized or its property is set to false
     * the message is not written.
     *
     * @param   channel       the channel to put the message on.
     * @param   name          the message to log.
     */
    public void log(String channel, String message) {
        if (active) {
            this.logQueue.put(new LogRecord(channel, message, null));

            if (this.logQueue.size() > queue_maxsize) {
                this.logQueue.put(new LogRecord(CH_QUEUE_STATUS, "Log queue size limit exceeded", null));
                flush();
            }
        }
    }

    /**
     * Prints the error message and stack trace if channel enabled.
     *
     * @param t the error thrown.
     */
    public void log(String channel, Throwable t) {
        if (active) {
            this.logQueue.put(new LogRecord(channel == null? CH_EXCEPTION_TRACING : channel, null, t));
            if ( this.logQueue.size() > queue_maxsize ) {
                this.logQueue.put(new LogRecord(CH_QUEUE_STATUS, "Log queue size limit exceeded", null));
                flush();
            }
        }
    }

   /**
    * @deprecated
    */
    public void log(Throwable t) {
        log(null, t);
    }

    /**
     * Flush the log.
     *
     * Write any pending messages into the log media.
     */
    public void flush() {
        if (logDaemon != null) {
            
            // It would be a very bad idea to make the unfortunate thread
            // which triggered the queue overflow condition suffer, so
            // instead I create another high priority thread which is going
            // to take care about stuff.
            
            Runnable plumber = new Runnable() {
                public void run() {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    logDaemon.flush();
                }
            };
            
            (new Thread(plumber)).start();
        }
    }

    /**
     * Log message queue.
     *
     * Supposed to keep the messages until they get processed by the
     * background logger thread.
     */
    protected SimpleQueue logQueue = new SimpleQueue();

    /**
     * Class implementing the background logging.
     */
    protected class Agent extends Thread {

        /**
         * Wait for the messages in the log message queue and pass 
         * them to the log media, whatever it is.
         */
        public void run() {

            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            while (true) {
                try {
                    LogRecord lr = (LogRecord) logQueue.waitObject();
                    write(lr);
                    
                    if ((System.currentTimeMillis() - lr.date) > queue_maxage) {
                    
                		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                		
    			        // This record goes with the time out of sync, hope noone will notice
                        write(new LogRecord(CH_QUEUE_STATUS, "Log queue age limit exceeded", null));
                        flush();
            		    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    }
                } catch (InterruptedException iex) {
                    if (!logQueue.isEmpty()) {
                        write(new LogRecord(CH_EXCEPTION_TRACING, "Logger thread interrupted, flushing...", iex));
                        flush();
                        write(new LogRecord(CH_EXCEPTION_TRACING, "Flushed, logging stopped.", null));
                    }
                    return;
                }
            }
        }

        /**
         * Write the log record on the log stream.
         */
        public synchronized void write(LogRecord lr) {
            if (isActive(lr.channel)) {
                if (timestamp) {
                    writer.print(formatter.format(new Date(lr.date)));
                }
                
                if (logChannel && lr.channel!=null) {
					writer.print(m_channelStart);
                    writer.print(lr.channel);
                    writer.print(m_channelEnd);
                }
                
                writer.print(m_channelSeperator);

                if (lr.message != null) {
                    writer.println(lr.message);
                }

                if (lr.t != null) {
                    lr.t.printStackTrace(writer);
                }

                writer.flush();
            }
        }

        /**
         * Flush the log record queue.
         */
        public void flush() {
            
            // Double locking to prevent the NullPointerException starting
            // to happen quite a lot as the priorities grow.
            
            while (!logQueue.isEmpty()) {
                LogRecord next = (LogRecord) logQueue.get();
                if (next != null) write(next);
            }
        }
    }
}
