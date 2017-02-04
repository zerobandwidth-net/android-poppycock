package net.zerobandwidth.android.apps.poppycock.ui;

import android.app.Service;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.zerobandwidth.android.apps.poppycock.PoppycockService;
import net.zerobandwidth.android.apps.poppycock.R;
import net.zerobandwidth.android.lib.nonsense.NonsenseBuilder;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;

/**
 * The app's main activity.
 * @since zerobandwidth-net/android-poppycock 0.0.1 (#1)
 */
public class MainActivity
extends AppCompatActivity
implements SimpleServiceConnection.Listener
{
    public static final String LOG_TAG = MainActivity.class.getSimpleName() ;

    /**
     * A unique tag marking the activity-state extra containing our last bit of
     * nonsense.
     */
    protected static final String EXTRA_LAST_NONSENSE =
        "net.zerobandwidth.android.apps.poppycock.ui.MainActivity.LAST_NONSENSE" ;

/// Instance Members ///////////////////////////////////////////////////////////

    /**
     * A connection to the service which produces nonsense.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected SimpleServiceConnection<PoppycockService> m_conn = null ;

    /** The nonsense generator. */
    protected NonsenseBuilder m_xyzzy = null ;

    /** A persistent binding to the activity's main text view. */
    protected TextView m_twNonsense = null ;

    /** Catches the last string of nonsense that was on the screen. */
    protected String m_sLastNonsense = null ;

/// Activity Lifecycle /////////////////////////////////////////////////////////

    @Override
    protected void onCreate( Bundle bndlState )
    {
        super.onCreate( bndlState ) ;
        if( m_xyzzy == null ) m_xyzzy = new NonsenseBuilder(this) ;
        this.setContentView( R.layout.activity_poppycock_main ) ;
        this.bindToElements().restoreText(bndlState) ;
        PoppycockService.API.kickoff(this) ;
    }

    /**
     * Binds persistent references to all of the elements of the layout that we
     * will modify programmatically.
     * @return (fluid)
     */
    protected MainActivity bindToElements()
    {
        m_twNonsense = ((TextView)( this.findViewById( R.id.twNonsense )) ) ;
        return this ;
    }

    /**
     * Restores the last bit of nonsense we generated, or generates new nonsense
     * if we haven't created any yet.
     * @param bndlState the prior activity state
     * @return (fluid)
     */
    protected MainActivity restoreText( Bundle bndlState )
    {
        if( bndlState != null )
        { // Discover the last bit of nonsense we had on the screen, if any.
            CharSequence acNonsense =
                    bndlState.getCharSequence( EXTRA_LAST_NONSENSE ) ;
            m_sLastNonsense = ( acNonsense != null ?
                    acNonsense.toString() : m_xyzzy.getString() ) ;
        }
        else
            m_sLastNonsense = m_xyzzy.getString() ;

        return this ;
    }

    @Override
    public void onResume()
    {
        super.onResume() ;
        this.bindToService().refreshNonsense( m_sLastNonsense ) ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        this.getMenuInflater().inflate( R.menu.menu_poppycock_main, menu ) ;
        return true ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public void onPause()
    {
        m_sLastNonsense = m_twNonsense.getText().toString() ;
        super.onPause() ;
    }

    @Override
    protected void onSaveInstanceState( Bundle bndlState )
    {
        super.onSaveInstanceState(bndlState) ;
        bndlState.putCharSequence( EXTRA_LAST_NONSENSE, m_twNonsense.getText() ) ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public void onDestroy()
    {
        if( m_conn != null && m_conn.isBound() )
        { // Release our service binding.
            m_conn.removeListener(this).disconnect(this) ;
        }
        super.onDestroy() ;
    }

/// SimpleServiceConnection<>.Listener /////////////////////////////////////////

    /**
     * Binds the activity to the {@link PoppycockService}.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected MainActivity bindToService()
    {
        Log.d( LOG_TAG, "Connecting to service..." ) ;
        if( m_conn == null )
            m_conn = new SimpleServiceConnection<>( PoppycockService.class ) ;
        m_conn.addListener(this).connect(this) ;
        return this ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public <S extends Service> void onServiceConnected( SimpleServiceConnection<S> conn )
    {
        if( ! conn.isServiceClass( PoppycockService.class ) ) return ;
        Log.d( LOG_TAG, "Connected to service." ) ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public <S extends Service> void onServiceDisconnected( SimpleServiceConnection<S> conn )
    { Log.d( LOG_TAG, "Disconnected from service." ) ; }

/// AppCompatActivity //////////////////////////////////////////////////////////

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public boolean onOptionsItemSelected( MenuItem mi )
    {
        final int nItem = mi.getItemId() ;
        switch( nItem )
        {
            case R.id.miHistory:
                this.openHistoryScreen() ;
                break ;
        }

        return super.onOptionsItemSelected(mi) ;
    }


/// Other Instance Methods /////////////////////////////////////////////////////

    /**
     * Handles the event in which a user taps any control that should regenerate
     * new nonsense.
     * @param w the control that was tapped (ignored)
     */
    public void onNextNonsenseClicked( View w )
    { this.refreshNonsense( m_xyzzy.getString() ) ; }

    /**
     * Navigates to the screen where the user can view the historical record of
     * nonsense.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public MainActivity openHistoryScreen()
    {
        Log.d( LOG_TAG, "Yep, tapped the history button!" ) ;
        return this ;
    }

    /**
     * Redraws the nonsense text view on the UI thread.
     * @param sNonsense the new nonsense to be displayed
     * @return (fluid)
     */
    protected MainActivity refreshNonsense( final String sNonsense )
    {
        this.runOnUiThread( new Runnable()
        {
            private final MainActivity m_act = MainActivity.this ;

            @Override
            public void run()
            { m_act.m_twNonsense.setText( sNonsense ) ; }
        });
        return this ;
    }
}
