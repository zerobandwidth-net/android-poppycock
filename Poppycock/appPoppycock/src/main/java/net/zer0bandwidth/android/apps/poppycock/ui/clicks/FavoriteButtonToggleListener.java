package net.zer0bandwidth.android.apps.poppycock.ui.clicks;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import net.zer0bandwidth.android.apps.poppycock.R;
import net.zer0bandwidth.android.apps.poppycock.database.PoppycockDatabase;
import net.zer0bandwidth.android.apps.poppycock.model.Sentence;

/**
 * Provides a common implementation of a click listener for the various
 * manifestations of image buttons that toggle "favorite" status for a sentence.
 * @since zer0bandwidth-net/android-poppycock 1.0.2 (#5)
 */
public class FavoriteButtonToggleListener
implements ImageButton.OnClickListener
{
	protected static final String LOG_TAG =
			FavoriteButtonToggleListener.class.getSimpleName() ;

	protected final AppCompatActivity m_act ;

	protected final PoppycockDatabase m_dbh ;

	protected final Sentence m_oSentence ;

	protected final String m_sLogTag ;

	public FavoriteButtonToggleListener( AppCompatActivity act, PoppycockDatabase dbh, Sentence o )
	{
		super() ;
		m_act = act ;
		m_dbh = dbh ;
		m_oSentence = o ;
		m_sLogTag = m_act.getClass().getSimpleName() ;
	}

	@Override
	public void onClick( final View w )
	{
		if( m_act == null )
		{ Log.e( LOG_TAG, "Cannot execute without activity." ) ; return ; }
		if( m_dbh == null )
		{ Log.e( LOG_TAG, "Cannot execute without database." ) ; return ; }
		if( m_oSentence == null )
		{ Log.e( LOG_TAG, "Cannot execute without sentence." ) ; return ; }
		Log.d( m_sLogTag, (new StringBuilder())
				.append( "Toggle favorite status of sentence " )
				.append( m_oSentence.nItemID )
				.toString()
			);
		m_dbh.toggleFavorite( m_oSentence ) ;
		m_act.runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				((ImageButton)w).setImageResource((
						m_oSentence.bIsFavorite ?
							R.drawable.ic_favorite_black_24dp :
							R.drawable.ic_favorite_border_black_24dp
					));
			}
		});
	}
}
