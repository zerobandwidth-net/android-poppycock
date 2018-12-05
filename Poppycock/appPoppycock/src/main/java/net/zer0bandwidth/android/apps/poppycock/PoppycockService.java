package net.zer0bandwidth.android.apps.poppycock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import net.zer0bandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zer0bandwidth.android.lib.content.IntentUtils;
import net.zer0bandwidth.android.lib.services.SimpleServiceConnection;

/**
 * Provides a persistent object to handle database connections, etc.
 * @since zer0bandwidth-net/android-poppycock 1.0.1 (#2)
 */
public class PoppycockService
extends Service
{
//    protected static final String LOG_TAG = PoppycockService.class.getSimpleName() ;

/// Static Service API /////////////////////////////////////////////////////////

    /**
     * Provides static methods for creating the {@link Intent}s that trigger
     * events inside the service.
     * @since zer0bandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public static class API
    {
        /** Qualifier prepended to all actions. */
        public static final String ACTION_PREFIX =
            "net.zer0bandwidth.android.actions.poppycock.PoppycockService." ;

        /** Action to kick off the service. */
        public static final String ACTION_KICKOFF = ACTION_PREFIX + "KICKOFF" ;

        /**
         * Dispatches an intent to start the service, from the specified context.
         * @param ctx the context from which the intent will be emitted
         */
        public static void kickoff( Context ctx )
        {
            ctx.startService( IntentUtils.getBoundIntent( ctx,
                    PoppycockService.class, ACTION_KICKOFF ) ) ;
        }
    }

/// Service Binding Management /////////////////////////////////////////////////

    /**
     * An implementation of the {@link android.os.Binder} class which simply
     * returns this instance of the service.
     * @since zer0bandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public class Binder extends android.os.Binder
    implements SimpleServiceConnection.InstanceBinder
    {
        @Override
        public PoppycockService getServiceInstance()
        { return PoppycockService.this ; }
    }

    /** A constant binding to this service. */
    protected final PoppycockService.Binder m_bind =
            new PoppycockService.Binder() ;

    @Override
    public PoppycockService.Binder onBind( Intent sig )
    { return m_bind ; }

/// Instance Members ///////////////////////////////////////////////////////////

    /** A persistent reference to the historical record of nonsense. */
    protected PoppycockDatabase m_dbh = null ;

/// Service Lifecycle //////////////////////////////////////////////////////////

    @Override
    public void onCreate()
    {
        super.onCreate() ;
        m_dbh = (new PoppycockDatabase(this)).openDB() ;
    }

    @Override
    public int onStartCommand( Intent sig, int zFlags, int nStartID )
    {
        super.onStartCommand( sig, zFlags, nStartID ) ;
        return START_STICKY ;
    }

    @Override
    public void onDestroy()
    {
        if( m_dbh != null ) m_dbh.closeDB().close() ;
        super.onDestroy() ;
    }

/// Instance Methods ///////////////////////////////////////////////////////////

    /** Accessor for the SQLite database instance. */
    public PoppycockDatabase getDB()
    { return m_dbh ; }
}
