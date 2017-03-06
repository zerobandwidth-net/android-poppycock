package net.zerobandwidth.android.apps.poppycock.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.zerobandwidth.android.apps.poppycock.PoppycockService;
import net.zerobandwidth.android.apps.poppycock.R;
import net.zerobandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zerobandwidth.android.apps.poppycock.model.Sentence;
import net.zerobandwidth.android.apps.poppycock.ui.clicks.FavoriteButtonToggleListener;
import net.zerobandwidth.android.lib.app.AppUtils;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;
import net.zerobandwidth.android.lib.ui.MultitapAlertCompatDialog;
import net.zerobandwidth.android.lib.view.updaters.MenuItemUpdater;

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
implements SimpleServiceConnection.Listener<PoppycockService>
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

        /** Tag for the extra that specifies sort order. */
        public static final String EXTRA_TAG_SORT_ORDER =
                EXTRA_PREFIX + "SORT_ORDER" ;

        /** Indicates that the display is sorted ascending (oldest first). */
        public static final int SORTING_ASC = 1 ;
        /** Indicates that the display is sorted descending (newest first). */
        public static final int SORTING_DESC = -1 ;

        /**
         * Start the activity in "historical record" mode.
         * @param ctx the context which is requesting the activity
         */
        public static void startHistoryActivity( Context ctx )
        {
            Log.d( LOG_TAG, "Kicking off history activity..." ) ;
            Intent sig = new Intent( ctx, HistoryActivity.class ) ;
            sig.putExtra( EXTRA_TAG_MODE, MODE_HISTORY ) ;
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
            @SuppressLint( "ViewHolder" ) View wRow = infl.inflate(
                    R.layout.listitem_poppycock_sentence, awParent, false ) ;

	        LinearLayout layListItemTextArea = ((LinearLayout)
			        ( wRow.findViewById( R.id.laySentenceListItemTextArea ) )) ;
	        layListItemTextArea.setOnClickListener(
			        new ListItemClickListener( oSentence ) ) ;

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
		        new FavoriteButtonToggleListener( HistoryActivity.this,
			        HistoryActivity.this.getDBFromService(), oSentence ) ) ;

            TextView twHistoricalNonsense = ((TextView)
                        ( wRow.findViewById( R.id.twHistoricalNonsense ) )) ;
            twHistoricalNonsense.setText( oSentence.sSentence ) ;

            TextView twDate = ((TextView)
                        ( wRow.findViewById( R.id.twHistoricalDate ) )) ;
            twDate.setText( SimpleDateFormat.getDateTimeInstance()
                    .format( new Date( oSentence.nItemTS ) )) ;

            return wRow ;
        }
    }

	/**
	 * Handles the event where the user has clicked on the text area containing
	 * one of the sentences.
	 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
	 */
	protected class ListItemClickListener
    implements View.OnClickListener
    {
	    /** A persistent reference back to the activity. */
        protected final HistoryActivity m_act = HistoryActivity.this ;

	    /** A reference to the bit of nonsense shown in this element. */
	    protected Sentence m_oSentence = null ;

	    /**
	     * A constructor which binds the listener to a sentence instance.
	     * @param o the bit of nonsense to bind
	     */
	    public ListItemClickListener( Sentence o )
	    { super() ; m_oSentence = o ; }

	    @Override
	    public void onClick( final View w )
	    {
		    Log.d( LOG_TAG, "I got clicked!" ) ;
		    SentenceReviewActivity.API.startActivity( m_act, m_oSentence ) ;
	    }
    }

	/**
     * Purges nonsense from the historical record, and updates the UI.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    protected class WinstonSmith
    implements Runnable
    {
        protected final HistoryActivity m_act = HistoryActivity.this ;

        protected int m_zDeletionSet = API.MODE_HISTORY ;

        /** Defines which set of records should be deleted. */
        public WinstonSmith( int zDeletionSet )
        { this.m_zDeletionSet = zDeletionSet ; }

        @Override
        public void run()
        {
            final PoppycockDatabase db = this.m_act.getDBFromService() ;
            if( db == null )
            {
                Toast.makeText( this.m_act, R.string.toast_DatabaseNoWorky,
                        Toast.LENGTH_SHORT )
                    .show()
                    ;
                return ;
            }
            db.deleteCategory(( m_zDeletionSet == API.MODE_FAVORITES )) ;
            this.m_act.populate() ;
        }

    }

/// Instance Members ///////////////////////////////////////////////////////////

    /** A connection to the service which deals with historical nonsense. */
    protected SimpleServiceConnection<PoppycockService> m_conn = null ;

    /**
     * The current operating mode of the activity.
     * Defaults to "historical record" mode.
     */
    protected int m_zMode = API.MODE_HISTORY ;

    /**
     * A persistent binding to the list of historical records.
     */
    protected ListView m_awSentences = null ;

	/**
     * A persistent binding to the menu item for sorting the list of nonsense.
     * The icon, caption, and behavior of this item change based on state.
     */
    protected MenuItem m_miSortHistory = null ;

	/**
	 * The current sort order for the displayed records.
     * Defaults to descending (newest first).
     */
    protected int m_zSortOrder = API.SORTING_DESC ;

	/**
	 * A persistent binding to the menu item for deleting nonsense from the
     * record. The function of this item changes depending on window mode.
     */
    protected MenuItem m_miDeleteHistory = null ;

	/**
     * A persistent binding to the menu item for switching window modes.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    protected MenuItem m_miSwitchMode = null ;

/// Activity Lifecycle /////////////////////////////////////////////////////////

    @Override
    protected void onCreate( Bundle bndlState )
    {
        super.onCreate(bndlState) ;
        this.setContentView( R.layout.activity_poppycock_history ) ;
        if( bndlState != null )
        { // Restore operating mode and sorting order.
            m_zMode = bndlState.getInt( API.EXTRA_TAG_MODE ) ;
            Log.d( LOG_TAG, ( m_zMode == API.MODE_FAVORITES ? "state bundle favorites" : "state bundle history" ) ) ;
            final int zSortOrder = bndlState.getInt( API.EXTRA_TAG_SORT_ORDER );
            m_zSortOrder = ( zSortOrder == 0 ? API.SORTING_DESC : zSortOrder ) ;
        }
        switch( m_zMode )
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
        if( m_conn.isConnected() )
            this.populate() ;
        else
            m_conn.addListener(this).connect(this) ; // and populate on connect
        this.updateTitleForMode() ;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        this.getMenuInflater().inflate( R.menu.menu_poppycock_history, menu ) ;
        return true ;
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu )
    {
        m_miSortHistory = menu.findItem( R.id.miSortHistory ) ;
        m_miSwitchMode = menu.findItem( R.id.miSwitchMode ) ;
        this.updateModeMenuItem() ;
        m_miDeleteHistory = menu.findItem( R.id.miDeleteHistory ) ;
        this.updateSortMenuItem() ;
        return super.onPrepareOptionsMenu( menu ) ;
    }

    @Override
    protected void onSaveInstanceState( Bundle bndlState )
    {
        super.onSaveInstanceState( bndlState ) ;
        bndlState.putInt( API.EXTRA_TAG_MODE, m_zMode ) ;
        bndlState.putInt( API.EXTRA_TAG_SORT_ORDER, m_zSortOrder ) ;
    }

    @Override
    public void onDestroy()
    {
        if( m_conn != null && m_conn.isConnected() )
            m_conn.removeListener(this).disconnect(this) ;
        super.onDestroy() ;
    }

/// SimpleServiceConnection.Listener<PoppycockService> /////////////////////////

    @Override
    public void onServiceConnected( SimpleServiceConnection<PoppycockService> conn )
    {
        Log.d( LOG_TAG, "Connected to service." ) ;
        this.populate() ;
    }

    @Override
    public void onServiceDisconnected( SimpleServiceConnection<PoppycockService> conn )
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
            case R.id.miSortHistory:
                this.onSortButtonPressed() ;
                break ;
            case R.id.miSwitchMode:
                this.switchMode() ;
                break ;
            case R.id.miDeleteHistory:
                this.onDeleteButtonPressed() ;
                break ;
        }
        return super.onOptionsItemSelected(mi) ;
    }

/// Other Instance Methods /////////////////////////////////////////////////////

	/**
     * Fetches the database only if it's usable.
     * @return the app's database, only if it is usable, or {@code null} otherwise
     */
    protected PoppycockDatabase getDBFromService()
    {
        if( m_conn == null || ! m_conn.isConnected() ) return null ;
        final PoppycockDatabase db = m_conn.getServiceInstance().getDB() ;
        if( ! db.isConnected() ) return null ;  // It's no good to us like this.
        return db ;
    }

	/**
     * Deletes the set of records corresponding to the current mode.
     * @return (fluid)
     */
    protected HistoryActivity onDeleteButtonPressed()
    {
        final PoppycockDatabase db = this.getDBFromService() ;
        if( db == null )
        { // Give up.
            Log.e( LOG_TAG, "Database unavailable for delete operation." ) ;
            Toast.makeText( this, R.string.toast_DatabaseNoWorky,
                    Toast.LENGTH_SHORT )
                .show()
                ;
            return this ;
        }
        switch( m_zMode )
        {
            case API.MODE_FAVORITES:
            { // Delete only favorites.
                (new MultitapAlertCompatDialog(
                        this, R.string.title_DeleteFavorites,
                        R.string.message_DeleteFavorites ))
                    .setStandardButtons(
                            new WinstonSmith(API.MODE_FAVORITES), null )
                    .setPositiveTapsRequired(10)
                    .show()
                    ;
            } break ;
            case API.MODE_HISTORY:
            default:
            { // Delete only non-favorites.
                (new MultitapAlertCompatDialog(
                        this, R.string.title_DeleteHistory,
                        R.string.message_DeleteHistory ))
                    .setStandardButtons(
                            new WinstonSmith(API.MODE_HISTORY), null )
                    .setPositiveTapsRequired(3)
                    .show()
                    ;
            }
        }
        return this ;
    }

	/**
     * Inverts the sort order and updates the menu item.
     * @return (fluid)
     */
    protected HistoryActivity onSortButtonPressed()
    {
        m_zSortOrder = ( m_zSortOrder == API.SORTING_DESC ?
                            API.SORTING_ASC : API.SORTING_DESC ) ;
        this.populate().updateSortMenuItem() ;

        Toast.makeText( this,
                ( m_zSortOrder == API.SORTING_DESC ?
                        R.string.toast_SortNewestFirst :
                        R.string.toast_SortOldestFirst ),
                Toast.LENGTH_SHORT )
            .show()
            ;

        return this ;
    }

    /**
     * Populates the screen with the appropriate historical records.
     * @return (fluid)
     */
    protected HistoryActivity populate()
    {
        ArrayList<Sentence> aoSentences ;
        final PoppycockDatabase db = this.getDBFromService() ;
        if( db != null )
        {
            final boolean bSortOrder = ( m_zSortOrder != API.SORTING_DESC ) ;
            aoSentences = ( m_zMode == API.MODE_FAVORITES ?
                    db.getFavorites( bSortOrder ) :
                    db.getHistory( bSortOrder )
                );
        }
        else aoSentences = new ArrayList<>() ;

        final SentenceListAdapter adapter = new SentenceListAdapter( this,
            R.layout.listitem_poppycock_sentence, aoSentences ) ;
        m_awSentences.setAdapter( adapter ) ;

        return this ;
    }

	/**
	 * Switches operating mode in response to a button press.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    protected HistoryActivity switchMode()
    {
        m_zMode = ( m_zMode == API.MODE_FAVORITES ?
                API.MODE_HISTORY : API.MODE_FAVORITES ) ;
        this.populate().updateModeMenuItem().updateTitleForMode() ;
        return this ;
    }

	/**
     * Updates the icon and caption of the sort button.
     * @return (fluid)
     */
    protected HistoryActivity updateSortMenuItem()
    {
        int resLabel, resIcon ;
        switch( m_zSortOrder )
        {
            case API.SORTING_DESC:
                resLabel = R.string.label_miSort_asc ;
                resIcon = R.drawable.ic_arrow_upward_black_24dp ;
                break ;
            case API.SORTING_ASC:
            default:
                resLabel = R.string.label_miSort_desc ;
                resIcon = R.drawable.ic_arrow_downward_black_24dp ;
        }
        this.runOnUiThread( new MenuItemUpdater(
                m_miSortHistory, this, resLabel, resIcon ) ) ;

        return this ;
    }

	/**
	 * Updates the icon and caption for the mode switcher button.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    protected HistoryActivity updateModeMenuItem()
    {
        int resLabel, resIcon ;
        switch( m_zMode )
        {
            case API.MODE_FAVORITES:
                resLabel = R.string.label_miHistoryMode_history ;
                resIcon = R.drawable.ic_history_black_24dp ;
                break ;
            case API.MODE_HISTORY:
            default:
                resLabel = R.string.label_miHistoryMode_favorites ;
                resIcon = R.drawable.ic_favorite_black_24dp ;
        }
        this.runOnUiThread( new MenuItemUpdater(
                m_miSwitchMode, this, resLabel, resIcon ) ) ;

        return this ;
    }

	/**
     * Updates the window title in response to a mode switch.
     * @return (fluid)
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    protected HistoryActivity updateTitleForMode()
    {
        this.runOnUiThread( new Runnable()
        {
            protected final HistoryActivity m_act = HistoryActivity.this ;

            @Override
            public void run()
            {
                this.m_act.setTitle(( m_zMode == API.MODE_FAVORITES ?
                        R.string.title_HistoryActivity_favorites :
                        R.string.title_HistoryActivity_history )) ;
            }
        });

        return this ;
    }
}
