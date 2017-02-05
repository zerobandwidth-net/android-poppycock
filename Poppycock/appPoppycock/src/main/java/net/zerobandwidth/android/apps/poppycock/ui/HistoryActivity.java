package net.zerobandwidth.android.apps.poppycock.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import net.zerobandwidth.android.apps.poppycock.PoppycockService;
import net.zerobandwidth.android.apps.poppycock.R;
import net.zerobandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zerobandwidth.android.apps.poppycock.model.Sentence;
import net.zerobandwidth.android.lib.AppUtils;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

/// Inner Classes //////////////////////////////////////////////////////////////

    /**
     * Adapter for the view that shows the list of sentences in the historical
     * record.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected class SentenceListAdapter
    extends ArrayAdapter<Sentence>
    {
        /** The context in which the adapter was created. */
        protected Context m_ctx = null ;

        /** The list of sentences to be displayed. */
        protected List<Sentence> m_aoSentences = null ;

        public SentenceListAdapter( Context ctx, int resListViewID, List<Sentence> aoSentences )
        {
            super( ctx, resListViewID, aoSentences ) ;
            this.m_ctx = ctx ;
            this.m_aoSentences = aoSentences ;
        }

        @NonNull
        @Override
        public View getView( int nIndex, View w, @NonNull ViewGroup awParent )
        {
            final Sentence oSentence = this.m_aoSentences.get(nIndex) ;

            LayoutInflater infl = ((LayoutInflater)
                ( m_ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE ) )) ;
            View wRow = infl.inflate(
                    R.layout.listitem_poppycock_sentence, awParent, false ) ;

            ImageButton btnFavorite = ((ImageButton)
                        ( wRow.findViewById( R.id.btnFavorite ) )) ;
            if( oSentence.bIsFavorite )
            {
                btnFavorite.setImageResource(
                        R.drawable.ic_favorite_black_24dp ) ;
                btnFavorite.setContentDescription( this.m_ctx.getString(
                        R.string.label_btnFavoriteTrue ) ) ;
            }
            else
            {
                btnFavorite.setImageResource(
                        R.drawable.ic_favorite_border_black_24dp ) ;
                btnFavorite.setContentDescription( this.m_ctx.getString(
                        R.string.label_btnFavoriteFalse ) ) ;
            }
            btnFavorite.setOnClickListener(
                    new FavoriteButtonClickListener( oSentence ) ) ;

            TextView twHistoricalNonsense = ((TextView)
                        ( wRow.findViewById( R.id.twHistoricalNonsense ) )) ;
            twHistoricalNonsense.setText(
                    this.m_aoSentences.get(nIndex).sSentence ) ;

            TextView twDate = ((TextView)
                        ( wRow.findViewById( R.id.twHistoricalDate ) )) ;
            twDate.setText( SimpleDateFormat.getDateTimeInstance()
                        .format( new Date( oSentence.nItemTS ) )) ;

            return wRow ;
        }
    }

    /**
     * Handles the event where the user has clicked on the favorite indicator.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    protected class FavoriteButtonClickListener
    implements View.OnClickListener
    {
        /**
         * A reference to the bit of nonsense that corresponds to this element.
         */
        protected Sentence m_oSentence = null ;

        /**
         * A constructor which binds the listener to a sentence instance.
         * @param oSentence the bit of nonsense to bind
         */
        public FavoriteButtonClickListener( Sentence oSentence )
        {
            super() ;
            m_oSentence = oSentence ;
        }

        @Override
        public void onClick( View w )
        {
            Log.d( LOG_TAG, (new StringBuilder())
                    .append( "Clicked favorite button for sentence [" )
                    .append( m_oSentence.nItemID )
                    .append( "]: " )
                    .append( m_oSentence.sSentence )
                    .toString()
                );
            Log.d( LOG_TAG, "Save the rest of the work for issue 3! ^_^" ) ;
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

    /**
     * A persistent binding to the list of historical records.
     */
    protected ListView m_awSentences = null ;

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
        m_awSentences = ((ListView)(this.findViewById( R.id.awSentences ))) ;
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
        this.populate() ;
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

    /**
     * Populates the screen with the appropriate historical records.
     * @return (fluid)
     */
    protected HistoryActivity populate()
    {
        ArrayList<Sentence> aoSentences = null ;
        if( m_conn != null && m_conn.isConnected() )
        {
            PoppycockDatabase db = m_conn.getServiceInstance().getDB() ;
            if( db.isConnected() )
                aoSentences = db.getHistory(true) ;
        }
        else aoSentences = new ArrayList<>() ;

        final SentenceListAdapter adapter = new SentenceListAdapter( this,
            R.layout.listitem_poppycock_sentence, aoSentences ) ;
        m_awSentences.setAdapter( adapter ) ;

        return this ;
    }
}
