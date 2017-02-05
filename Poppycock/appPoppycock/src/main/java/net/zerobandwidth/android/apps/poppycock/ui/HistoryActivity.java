package net.zerobandwidth.android.apps.poppycock.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import net.zerobandwidth.android.apps.poppycock.PoppycockService;
import net.zerobandwidth.android.apps.poppycock.R;
import net.zerobandwidth.android.lib.AppUtils;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;

/**
 * This activity shows the historical record of nonsense, or the nonsense hall
 * of fame.
 * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
 */
public class HistoryActivity
extends AppCompatActivity
implements SimpleServiceConnection.Listener
{
    public static final String LOG_TAG = HistoryActivity.class.getSimpleName() ;

/// Static Intent API //////////////////////////////////////////////////////////

    /**
     * Provides static methods for creating the {@link Intent}s that change the
     * behavior of the activity.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public static class API
    {
        /** Qualifier prepended to extras for this activity's intents. */
        public static final String EXTRA_PREFIX =
            "net.zerobandwidth.android.extras.poppycock.HistoryActivity." ;

        /** Tag for the extra that dictates the behavior of the activity. */
        public static final String EXTRA_TAG_MODE =
                EXTRA_PREFIX + "HISTORY_MODE" ;

        /** Constant dictating "history" mode for the activity. */
        public static final int MODE_HISTORY = 0 ;
        /** Constant dictating "favorites" mode for the activity. */
        public static final int MODE_FAVORITES = 1 ;

        /**
         * Start the activity in "historical record" mode.
         * @param ctx the context which is requesting the activity
         */
        public static void startHistoryActivity( Context ctx )
        {
            Intent sig = new Intent( ctx, HistoryActivity.class ) ;
            sig.putExtra( EXTRA_TAG_MODE, MODE_HISTORY ) ;
            ctx.startActivity(sig) ;
        }

        /**
         * Start the activity in "nonsense hall of fame" mode.
         * @param ctx the context which is requesting the activity.
         */
        public static void startFavoritesActivity( Context ctx )
        {
            Intent sig = new Intent( ctx, HistoryActivity.class ) ;
            sig.putExtra( EXTRA_TAG_MODE, MODE_FAVORITES ) ;
            ctx.startActivity(sig) ;
        }
    }

/// Instance Members ///////////////////////////////////////////////////////////

    /** A connection to the service which deals with historical nonsense. */
    protected SimpleServiceConnection<PoppycockService> m_conn = null ;

    /**
     * The current operating mode of the activity.
     * Defaults to "historical record" mode.
     */
    protected int m_mMode = API.MODE_HISTORY ;

/// Activity Lifecycle /////////////////////////////////////////////////////////

    @Override
    protected void onCreate( Bundle bndlState )
    {
        super.onCreate(bndlState) ;
        this.setContentView( R.layout.activity_poppycock_history ) ;
        if( bndlState != null )
        { // Restore operating mode.
            m_mMode = bndlState.getInt( API.EXTRA_TAG_MODE ) ;
        }
        switch( m_mMode )
        { // Set the title based on the mode we just discovered.
            case API.MODE_FAVORITES:
                this.setTitle( R.string.title_HistoryActivity_favorites ) ;
                break ;
            case API.MODE_HISTORY:
            default:
                this.setTitle( R.string.title_HistoryActivity_history ) ;
        }
        AppUtils.initBackButtonForActivity(this) ;
        PoppycockService.API.kickoff(this) ;                 // Just in case...?
    }

    @Override
    public void onResume()
    {
        super.onResume() ;
        if( m_conn == null )
            m_conn = new SimpleServiceConnection<>( PoppycockService.class ) ;
        m_conn.addListener(this).connect(this) ;
    }

    @Override
    protected void onSaveInstanceState( Bundle bndlState )
    {
        super.onSaveInstanceState( bndlState ) ;
        bndlState.putInt( API.EXTRA_TAG_MODE, m_mMode ) ;
    }

    @Override
    public void onDestroy()
    {
        if( m_conn != null && m_conn.isBound() )
            m_conn.removeListener(this).disconnect(this) ;
        super.onDestroy() ;
    }

/// SimpleServiceConnection<>.Listener /////////////////////////////////////////

    @Override
    public <S extends Service> void onServiceConnected( SimpleServiceConnection<S> conn )
    {
        if( ! conn.isServiceClass( PoppycockService.class ) ) return ;
        Log.d( LOG_TAG, "Connected to service." ) ;
    }

    @Override
    public <S extends Service> void onServiceDisconnected( SimpleServiceConnection<S> conn )
    { Log.d( LOG_TAG, "Disconnected from service." ) ; }

/// AppCompatActivity //////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected( MenuItem mi )
    {
        final int nItem = mi.getItemId() ;
        switch( nItem )
        {
            case android.R.id.home:
                this.onBackPressed() ;
                break ;
        }
        return super.onOptionsItemSelected(mi) ;
    }

/// Other Instance Methods /////////////////////////////////////////////////////


}
