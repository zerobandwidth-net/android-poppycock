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
import net.zerobandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zerobandwidth.android.apps.poppycock.model.Sentence;
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
    protected static final String EXTRA_TAG_LAST_NONSENSE =
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

    /** Catches the last bit of nonsense that was created for the screen. */
    protected Sentence m_oLastNonsense = null ;

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
            Sentence oLastNonsense =
                    bndlState.getParcelable( EXTRA_TAG_LAST_NONSENSE ) ;
            if( oLastNonsense != null )
            {
                m_oLastNonsense = oLastNonsense ;
                this.refreshNonsenseOnScreen( m_oLastNonsense.sSentence ) ;
            }
            else
                this.regenerateNonsense() ;
        }
        else
            this.regenerateNonsense() ;

        return this ;
    }

    @Override
    public void onResume()
    {
        super.onResume() ;
        this.bindToService()
            .refreshNonsenseOnScreen( m_oLastNonsense.sSentence )
            ;
    }

    /** @since zerobandwidth-net/android-poppycock 1.0.1 (#2) */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        this.getMenuInflater().inflate( R.menu.menu_poppycock_main, menu ) ;
        return true ;
    }

    @Override
    protected void onSaveInstanceState( Bundle bndlState )
    {
        super.onSaveInstanceState(bndlState) ;
        bndlState.putParcelable( EXTRA_TAG_LAST_NONSENSE, m_oLastNonsense ) ;
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
        this.preserveLastNonsense() ;
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
                this.openHistoryScreen(null) ;
                break ;
//            case R.id.miFavorites:
//                this.openFavoritesScreen(null) ;
//                break ;
        }

        return super.onOptionsItemSelected(mi) ;
    }

/// Other Instance Methods /////////////////////////////////////////////////////

    /**
     * Handles the event in which a user taps any control that should regenerate
     * new nonsense.
     * @param w the control that was tapped, if any (ignored)
     */
    public void onNextNonsenseClicked( View w )
    { this.refreshNonsenseOnScreen( this.regenerateNonsense() ) ; }

	/**
	 * Navigates to the screen where the user can view the Nonsense Hall of
     * Fame.
     * @param w the control that was tapped, if any (ignored)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     * @deprecated This approach failed; the code is left behind to wait for
     *  better ideas.
     */
//    public void openFavoritesScreen( View w )
//    { HistoryActivity.API.startFavoritesActivity(this) ; }

    /**
     * Navigates to the screen where the user can view the historical record of
     * nonsense.
     * @param w the control that was tapped, if any (ignored)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public void openHistoryScreen( View w )
    { HistoryActivity.API.startHistoryActivity(this) ; }

    /**
     * Ensures that any previous bit of nonsense that is already tracked by the
     * activity will be preserved for posterity.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected MainActivity preserveLastNonsense()
    {
        if( m_oLastNonsense != null && m_oLastNonsense.nItemID == Sentence.NOT_IDENTIFIED )
            this.recordForPosterity(m_oLastNonsense) ;
        return this ;
    }

    /**
     * Records a bit of nonsense in the annals of history.
     * @param o the sentence to be recorded
     * @return the recorded sentence, with its ID updated
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected Sentence recordForPosterity( Sentence o )
    {
        if( m_conn != null && m_conn.isConnected() )
        {
            final PoppycockDatabase db =
                    m_conn.getServiceInstance().getDB() ;
            if( db != null )
                db.insertSentence(o) ;
        }
        return o ;
    }

    /**
     * Redraws the nonsense text view on the UI thread.
     * @param sNonsense the new nonsense to be displayed
     * @return (fluid)
     */
    protected MainActivity refreshNonsenseOnScreen( final String sNonsense )
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

    /**
     * Generates new nonsense, and inserts it into the historical record.
     * If we have previous nonsense which has not been added to the historical
     * record, then it will be preserved before it is clobbered.
     * @return the new nonsense
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected String regenerateNonsense()
    {
        this.preserveLastNonsense() ;
        Sentence oNonsense = new Sentence() ;
        oNonsense.sSentence = m_xyzzy.getString() ;
        this.recordForPosterity(oNonsense) ;
        m_oLastNonsense = oNonsense ;
        return oNonsense.sSentence ;
    }
}
