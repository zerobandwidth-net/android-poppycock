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
import net.zerobandwidth.android.apps.poppycock.ui.clicks.FavoriteButtonToggleListener;
import net.zerobandwidth.android.lib.app.AppUtils;
import net.zerobandwidth.android.lib.content.ContentUtils;
import net.zerobandwidth.android.lib.services.SimpleServiceConnection;
import net.zerobandwidth.android.lib.ui.MultitapAlertCompatDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

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

		@Override
		public void run()
		{
			if( m_act.m_dbh == null )
			{
				Log.w( LOG_TAG, "DB unavailable during sentence deletion." ) ;
				return ;
			}
			if( m_act.m_dbh.deleteSentence( m_act.m_nSentenceID ) )
				m_act.tapBackButton() ;
		}
	}

	/**
	 * Handles taps on the "next" or "previous" button.
	 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
	 */
	protected class CursorButtonClickListener
	implements ImageButton.OnClickListener
	{
		protected final SentenceReviewActivity m_act =
				SentenceReviewActivity.this ;

		protected final long m_nSentenceID;

		public CursorButtonClickListener( long nSentenceID )
		{ m_nSentenceID = nSentenceID ; }

		@Override
		public void onClick( View w )
		{
			if( m_act.getDatabase() == null )
			{ m_act.toastDatabaseUnavailable() ; return ; }

			m_act.runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					m_act.setSentence(
							m_act.m_dbh.getSentence( m_nSentenceID ) ) ;
					m_act.updateWithData() ;
				}
			});
		}
	}

/// Instance Members ///////////////////////////////////////////////////////////

	/** A connection to the service which grants access to nonsense. */
	protected SimpleServiceConnection<PoppycockService> m_conn = null ;

	/** Persistent handle on the historical record. */
	protected PoppycockDatabase m_dbh = null ;

	/** The ID of the sentence currently displayed. */
	protected long m_nSentenceID = NO_SENTENCE_SELECTED ;

	/** The full data for the sentence being displayed. */
	protected Sentence m_oSentence = null ;

	protected TextView m_twSentence = null ;
	protected TextView m_twDate = null ;
	protected ImageButton m_btnFavorite = null ;
	protected ImageButton m_btnPrev = null ;
	protected ImageButton m_btnNext = null ;

/// Activity Lifecycle /////////////////////////////////////////////////////////

	@Override
	protected void onCreate( Bundle bndlState )
	{
		super.onCreate(bndlState) ;
		this.setContentView( R.layout.activity_poppycock_itemreview ) ;
		m_twSentence = ((TextView)
				( this.findViewById( R.id.twHistoricalNonsense ) )) ;
		m_twDate = ((TextView)
				( this.findViewById( R.id.twHistoricalDate ) )) ;
		m_btnFavorite = ((ImageButton)
				( this.findViewById( R.id.btnFavorite ) )) ;
		m_btnPrev = ((ImageButton)
				( this.findViewById( R.id.btnPrevious ) )) ;
		m_btnNext = ((ImageButton)
				( this.findViewById( R.id.btnNext ) )) ;
		if( bndlState != null && bndlState.containsKey( API.EXTRA_SENTENCE ) )
		{ // Restore the sentence ID. Sentence data loaded in onResume().
			Log.d( LOG_TAG, "Setting sentence from saved state." ) ;
			final Sentence o = bndlState.getParcelable( API.EXTRA_SENTENCE ) ;
			if( o == null )
			{
				Log.e( LOG_TAG, "Got null sentence parcel from intent extra." ) ;
				this.onBackPressed() ;
			}
			this.setSentence( o ) ;
		}
		else
		{ // See if we are starting from scratch with an intent?
			final Intent sig = this.getIntent() ;
			if( sig != null && sig.hasExtra( API.EXTRA_SENTENCE ) && m_nSentenceID == NO_SENTENCE_SELECTED )
			{ // Set from intent extra only if never selected before.
				Log.d( LOG_TAG, "Setting sentence ID from intent extra." ) ;
				final Sentence o = this.getIntent()
					.getParcelableExtra( API.EXTRA_SENTENCE ) ;
				if( o == null )
				{
					Log.e( LOG_TAG, "Got null sentence parcel from state bundle." ) ;
					this.onBackPressed() ;
				}
				this.setSentence( o ) ;
			}
		}
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
		{
			m_dbh = this.getDatabase() ;
			this.updateWithData();
		}
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
		bndlState.putParcelable( API.EXTRA_SENTENCE, m_oSentence ) ;
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
		this.getDatabase() ;
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
		if( this.getDatabase() == null )
		{ // Give up.
			Log.e( LOG_TAG, "Database unavailable for delete." ) ;
			this.toastDatabaseUnavailable() ;
			return  ;
		}
		if( m_oSentence.bIsFavorite )
		{ // Demand more taps on the delete button.
			( new MultitapAlertCompatDialog( this,
					R.string.title_DeleteOneSentence,
					R.string.message_DeleteOneSentence_favorite ))
				.setStandardButtons( new SentenceDeleter(), null )
				.setPositiveTapsRequired( 10 )
				.show()
				;
		}
		else
		{
			( new MultitapAlertCompatDialog( this,
					R.string.title_DeleteOneSentence,
					R.string.message_DeleteOneSentence ))
				.setStandardButtons( new SentenceDeleter(), null )
				.setPositiveTapsRequired( 3 )
				.show()
				;
		}
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
	protected PoppycockDatabase getDatabase()
	{
		if( m_dbh != null ) return m_dbh ;
		if( m_conn == null || ! m_conn.isConnected() ) return null ;
		final PoppycockDatabase db = m_conn.getServiceInstance().getDB() ;
		if( ! db.isConnected() ) return null ; // Can't use it if not connected.
		m_dbh = db ;
		return m_dbh ;
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
	 * Sets the sentence data for the activity.
	 * @param o the sentence to set
	 * @return (fluid)
	 */
	protected SentenceReviewActivity setSentence( Sentence o )
	{
		m_oSentence = o ;
		m_nSentenceID = m_oSentence.nItemID ;
		return this ;
	}

	/**
	 * Displays a toast indicating that the database is unavailable.
	 * @return (fluid)
	 * @since zerobandwidth-net/android-poppycock 1.0.2 (#5)
	 */
	protected SentenceReviewActivity toastDatabaseUnavailable()
	{
		this.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(
						SentenceReviewActivity.this,
						R.string.toast_DatabaseNoWorky,
						Toast.LENGTH_SHORT )
					.show() ;
			}
		});
		return this ;
	}

	/**
	 * Updates all of the on-screen elements based on the current sentence.
	 * @return (fluid)
	 */
	protected SentenceReviewActivity updateWithData()
	{
		if( this.getDatabase() == null )
		{
			this.toastDatabaseUnavailable().onBackPressed() ;
			return this ;
		}

		if( m_oSentence == null || m_oSentence.nItemID != m_nSentenceID )
			this.setSentence( m_dbh.getSentence( m_nSentenceID ) ) ;

		this.runOnUiThread( new Runnable()
		{ // Make sure this happens on the UI thread, so things get updated.

			final SentenceReviewActivity m_act =
					SentenceReviewActivity.this ;

			@Override
			public void run()
			{
				final long nNext = m_act.m_dbh.getNextSentenceID(
						m_act.m_oSentence.nItemID ) ;
				if( nNext == Sentence.NOT_IDENTIFIED )
					m_act.m_btnNext.setEnabled( false ) ;
				else
				{
					m_act.m_btnNext.setEnabled( true ) ;
					m_act.m_btnNext.setOnClickListener(
						new CursorButtonClickListener( nNext ) ) ;
				}
				final long nPrev = m_act.m_dbh.getPreviousSentenceID(
						m_act.m_oSentence.nItemID ) ;
				if( nPrev == Sentence.NOT_IDENTIFIED )
					m_act.m_btnPrev.setEnabled( false ) ;
				else
				{
					m_act.m_btnPrev.setEnabled( true ) ;
					m_act.m_btnPrev.setOnClickListener(
						new CursorButtonClickListener( nPrev ) ) ;
				}
				m_act.m_btnFavorite.setImageResource((
						m_oSentence.bIsFavorite ?
							R.drawable.ic_favorite_black_24dp :
							R.drawable.ic_favorite_border_black_24dp
					)) ;
				if( m_act.m_btnFavorite != null )
				{
					m_act.m_btnFavorite.setOnClickListener(
						new FavoriteButtonToggleListener(
							m_act, m_act.getDatabase(), m_oSentence ) ) ;
				}
				m_act.m_twSentence.setText( m_oSentence.sSentence ) ;
				m_act.m_twDate.setText( SimpleDateFormat.getDateTimeInstance()
					.format( new Date( m_oSentence.nItemTS ) )) ;
			}
		});
		return this ;
	}
}
