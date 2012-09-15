package org.opencms.search.solr;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class TestThreadUtils {

    public static TestThreadUtils getInstance() {
    	return new TestThreadUtils();
    }
    
    private ThreadGroup rootThreadGroup = null;
    
    public ThreadInfo[] getAllThreadInfos( ) {
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
        final long[] ids = thbean.getAllThreadIds( );
     
        ThreadInfo[] infos;
        if ( !thbean.isObjectMonitorUsageSupported( ) ||
            !thbean.isSynchronizerUsageSupported( ) )
            infos = thbean.getThreadInfo( ids );
        else
            infos = thbean.getThreadInfo( ids, true, true );
     
        final ThreadInfo[] notNulls = new ThreadInfo[infos.length];
        int nNotNulls = 0;
        for ( ThreadInfo info : infos )
            if ( info != null )
                notNulls[nNotNulls++] = info;
        if ( nNotNulls == infos.length )
            return infos;
        return java.util.Arrays.copyOf( notNulls, nNotNulls );
    }
    
    
    public Thread[] getAllThreads( ) {
        final ThreadGroup root = getRootThreadGroup( );
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
        int nAlloc = thbean.getThreadCount( );
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[ nAlloc ];
            n = root.enumerate( threads, true );
        } while ( n == nAlloc );
        return java.util.Arrays.copyOf( threads, n );
    }
    
    public Thread getBlockingThread( final Thread thread ) {
        final ThreadInfo info = getThreadInfo( thread );
        if ( info == null )
            return null;
        final long id = info.getLockOwnerId( );
        if ( id == -1 )
            return null;
        return getThread( id );
    }
    public Thread getLockingThread( long identity ) {
     
        final Thread[] allThreads = getAllThreads( );
        ThreadInfo info = null;
        MonitorInfo[] monitors = null;
        for ( Thread thread : allThreads ) {
            info = getThreadInfo( thread.getId( ) );
            if ( info == null )
                continue;
            monitors = info.getLockedMonitors( );
            for ( MonitorInfo monitor : monitors )
                if ( identity == monitor.getIdentityHashCode( ) )
                    return thread;
        }
        return null;
    }
        
    public Thread getLockingThread( final Object object ) {
        if ( object == null )
            throw new NullPointerException( "Null object" );
        final long identity = System.identityHashCode( object );
     
        final Thread[] allThreads = getAllThreads( );
        ThreadInfo info = null;
        MonitorInfo[] monitors = null;
        for ( Thread thread : allThreads ) {
            info = getThreadInfo( thread.getId( ) );
            if ( info == null )
                continue;
            monitors = info.getLockedMonitors( );
            for ( MonitorInfo monitor : monitors )
                if ( identity == monitor.getIdentityHashCode( ) )
                    return thread;
        }
        return null;
    }
    
    public ThreadGroup getRootThreadGroup( ) {
        if ( rootThreadGroup != null )
            return rootThreadGroup;
        ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
        ThreadGroup ptg;
        while ( (ptg = tg.getParent( )) != null )
            tg = ptg;
        return tg;
    }
    
    public Thread getThread( final long id ) {
        final Thread[] threads = getAllThreads( );
        for ( Thread thread : threads )
            if ( thread.getId( ) == id )
                return thread;
        return null;
    }
    
    public Thread getThread( final String name ) {
        if ( name == null )
            throw new NullPointerException( "Null name" );
        final Thread[] threads = getAllThreads( );
        for ( Thread thread : threads )
            if ( thread.getName( ).equals( name ) )
                return thread;
        return null;
    }
    
    public ThreadInfo getThreadInfo( final long id ) {
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
     
        if ( !thbean.isObjectMonitorUsageSupported( ) ||
            !thbean.isSynchronizerUsageSupported( ) )
            return thbean.getThreadInfo( id );
     
        final ThreadInfo[] infos = thbean.getThreadInfo(
            new long[] { id }, true, true );
        if ( infos.length == 0 )
            return null;
        return infos[0];
    }

    public ThreadInfo getThreadInfo( final String name ) {
        if ( name == null )
            throw new NullPointerException( "Null name" );
        final Thread[] threads = getAllThreads( );
        for ( Thread thread : threads )
            if ( thread.getName( ).equals( name ) )
                return getThreadInfo( thread.getId( ) );
        return null;
    }

    public ThreadInfo getThreadInfo( final Thread thread ) {
        if ( thread == null )
            throw new NullPointerException( "Null thread" );
        return getThreadInfo( thread.getId( ) );
    }

}
