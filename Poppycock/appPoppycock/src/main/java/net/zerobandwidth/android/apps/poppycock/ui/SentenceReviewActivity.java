package net.zerobandwidth.android.apps.poppycock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.zerobandwidth.android.apps.poppycock.PoppycockService;
import net.zerobandwidth.android.apps.poppycock.R;
import net.zerobandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zerobandwidth.android.apps.poppycock.model.Sentence;
import net.zerobandwidth.android.lib.app.AppUtils;
import net.zerobandwidth.android.lib.content.ContentUtils;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;
import net.zerobandwidth.android.lib.ui.MultitapAlertCompatDialog;

/**
 * Displays a sentence that has already been written to the historical record,
 * allowing the user to review or dispose it.
 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
 */
public class SentenceReviewActivity
extends AppCompatActivity
implements SimpleServiceConnection.Listener<PoppycockService>
{
	public static final String LOG_TAG =
			SentenceReviewActivity.class.getSimpleName() ;

	/**
	 * Constant value to indicate that the activity was called without selecting
	 * a sentence to be displayed.
	 */
	protected static final long NO_SENTENCE_SELECTED = Sentence.NOT_IDENTIFIED ;

/// Intent API /////////////////////////////////////////////////////////////////

	/**
	 * Provides static methods for kicking off instances of the
	 * {@link SentenceReviewActivity}.
	 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
	 */
	public static class API
	{
		/**
		 * Qualifier prepended to extras for {@link SentenceReviewActivity}'s
		 * intents.
		 */
		public static final String EXTRA_PREFIX =
			"net.zerobandwidth.android.extras.poppycock.SentenceReviewActivity." ;

		/**
		 * Tag for the extra that contains the ID of the sentence to be
		 * displayed in the activity.
		 */
		public static final String EXTRA_SENTENCE_ID =
				EXTRA_PREFIX + "SENTENCE_ID" ;

		/** Tag for the extra that contains the emparcelled sentence data. */
		public static final String EXTRA_SENTENCE =
				EXTRA_PREFIX + "SENTENCE_DATA" ;

		/**
		 * Start the activity, displaying the specified ID.
		 * @param ctx the context which is requesting the activity
		 * @param o the sentence to be displayed
		 */
		public static void startActivity( Context ctx, Sentence o )
		{
			Log.d( LOG_TAG, "Kicking off sentence review activity..." ) ;
			Intent sig = new Intent( ctx, SentenceReviewActivity.class ) ;
//			sig.putExtra( EXTRA_SENTENCE_ID, o.nItemID ) ;
			sig.putExtra( EXTRA_SENTENCE, o ) ;
			ctx.startActivity( sig ) ;
		}
	}

/// Inner classes //////////////////////////////////////////////////////////////

	/**
	 * Deletes the sentence being displayed.
	 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
	 */
	protected class SentenceDeleter
	implements Runnable
	{
		/** A persistent reference back to the activity. */
		protected final SentenceReviewActivity m_act =
				SentenceReviewActivity.this ;

		/** A persistent reference to the DB, obtained by the activity. */
		protected final PoppycockDatabase m_db ;

		/** Sets up the task for execution. */
		public SentenceDeleter( PoppycockDatabase db )
		{ m_db = db ; }

		@Override
		public void run()
		{
			if( m_db.deleteSentence( m_act.m_nSentenceID ) )
				m_act.tapBackButton() ;
		}
	}

/// Instance Members ///////////////////////////////////////////////////////////

	/** A connection to the service which grants access to nonsense. */
	protected SimpleServiceConnection<PoppycockService> m_conn = null ;

	/** The ID of the sentence currently displayed. */
	protected long m_nSentenceID = NO_SENTENCE_SELECTED ;

	/** The full data for the sentence being displayed. */
	protected Sentence m_oSentence = null ;

	protected TextView m_twSentence = null ;
	protected TextView m_twDate = null ;
	protected ImageButton m_btnFavorite = null ;


/// Activity Lifecycle /////////////////////////////////////////////////////////

	@Override
	protected void onCreate( Bundle bndlState )
	{
		super.onCreate(bndlState) ;
		this.setContentView( R.layout.activity_poppycock_itemreview ) ;
		if( bndlState != null && bndlState.containsKey( API.EXTRA_SENTENCE_ID ) )
		{ // Restore the sentence ID. Sentence data loaded in onResume().
			Log.d( LOG_TAG, "Setting sentence ID from saved state." ) ;
			m_nSentenceID = bndlState.getLong( API.EXTRA_SENTENCE_ID ) ;
		}
		else
		{ // See if we are starting from scratch with an intent?
			final Intent sig = this.getIntent() ;
			if( sig != null && sig.hasExtra( API.EXTRA_SENTENCE ) && m_nSentenceID == NO_SENTENCE_SELECTED )
			{ // Set the sentence ID from the intent's extra.
				Log.d( LOG_TAG, "Setting sentence ID from intent extra." ) ;
				m_oSentence = this.getIntent()
					.getParcelableExtra( API.EXTRA_SENTENCE ) ;
				m_nSentenceID = m_oSentence.nItemID ;
			}
		}
		m_twSentence = ((TextView)
				( this.findViewById( R.id.twHistoricalNonsense ) )) ;
		m_twDate = ((TextView)
				( this.findViewById( R.id.twHistoricalDate ) )) ;
		m_btnFavorite = ((ImageButton)
				( this.findViewById( R.id.btnFavorite ) )) ;
		AppUtils.initBackButtonForActivity(this) ;
		PoppycockService.API.kickoff(this) ;
	}

	@Override
	public void onResume()
	{
		super.onResume() ;
		if( m_conn == null )
			m_conn = new SimpleServiceConnection<>( PoppycockService.class ) ;
		if( m_conn.isConnected() )
			this.updateWithData() ;
		else
			m_conn.addListener(this).connect(this) ;
	}

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

	@Override
	public void onSaveInstanceState( Bundle bndlState )
	{
		super.onSaveInstanceState( bndlState ) ;
		bndlState.putLong( API.EXTRA_SENTENCE_ID, m_nSentenceID ) ;
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
		this.updateWithData() ;
	}

	@Override
	public void onServiceDisconnected( SimpleServiceConnection<PoppycockService> conn )
	{
		// Don't particularly care.
	}

/// Action Handlers ////////////////////////////////////////////////////////////

	/**
	 * Deletes the current sentence and returns to the history screen.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onDeleteButtonClicked( View w )
	{
		final PoppycockDatabase db = this.getDBFromService() ;
		if( db == null )
		{ // Give up.
			Log.e( LOG_TAG, "Database unavailable for delete." ) ;
			Toast.makeText( this, R.string.toast_DatabaseNoWorky,
					Toast.LENGTH_SHORT )
				.show() ;
			return  ;
		}
		if( m_oSentence.bIsFavorite )
		{ // Demand more taps on the delete button.
			( new MultitapAlertCompatDialog( this,
					R.string.title_DeleteOneSentence,
					R.string.message_DeleteOneSentence_favorite ))
				.setStandardButtons( new SentenceDeleter(db), null )
				.setPositiveTapsRequired( 10 )
				.show()
				;
		}
		else
		{
			( new MultitapAlertCompatDialog( this,
					R.string.title_DeleteOneSentence,
					R.string.message_DeleteOneSentence ))
				.setStandardButtons( new SentenceDeleter(db), null )
				.setPositiveTapsRequired( 3 )
				.show()
				;
		}
	}

	/**
	 * Toggles the "favorite" status of the sentence on or off.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onFavoriteToggleClicked( View w )
	{
		// TODO
	}

	/**
	 * Traverses to the next sentence in the DB, if any.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onNextButtonClicked( View w )
	{
		// TODO
	}

	/**
	 * Traverses to the previous sentence in the DB, if any.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onPreviousButtonClicked( View w )
	{
		// TODO
	}

	/**
	 * Attempts to share the sentence with the world.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onShareButtonClicked( View w )
	{
		ContentUtils.shareText( this,
			this.getShareText(), R.string.title_ShareTo ) ;
	}

	/**
	 * Attempts to tweet the current sentence.
	 * @param w the element that was tapped, if any (ignored)
	 */
	public void onTweetButtonClicked( View w )
	{
		if( ! ContentUtils.tryToTweetText( this, this.getShareText() ) )
		{ // Tweet attempt failed.
			Toast.makeText( this,
					this.getString( R.string.toast_URLEncodeFailed ),
					Toast.LENGTH_SHORT )
				.show() ;
		}
	}

	/** Ghost-presses the back button to close this activity. */
	public void tapBackButton()
	{
		this.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{ SentenceReviewActivity.this.onBackPressed() ; }
		});
	}

/// Other instance methods /////////////////////////////////////////////////////

	/**
	 * Fetches the database only if it's usable.
	 * @return the app's database, only if it is usable; {@code null} otherwise
	 */
	protected PoppycockDatabase getDBFromService()
	{
		if( m_conn == null || ! m_conn.isConnected() ) return null ;
		final PoppycockDatabase db = m_conn.getServiceInstance().getDB() ;
		if( ! db.isConnected() ) return null ; // Can't use it if not connected.
		return db ;
	}

	/**
	 * Generates the text to be shared to social media. This is the text of the
	 * nonsense currently on-screen, with the {@code #poppycock} hashtag
	 * appended to the end.
	 * @return nonsense to be shared with the world
	 */
	protected String getShareText()
	{
		return (new StringBuilder())
			.append( m_oSentence.sSentence )
			.append( " " )
			.append( this.getString( R.string.hashtag_poppycock ) )
			.toString()
			;
	}

	/**
	 * Updates all of the on-screen elements based on the current sentence.
	 * @return (fluid)
	 */
	protected SentenceReviewActivity updateWithData()
	{
		// TODO fetch the sentence and update the whole screen
		return this ;
	}
}
