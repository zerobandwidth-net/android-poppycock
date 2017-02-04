package net.zerobandwidth.android.apps.poppycock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.zerobandwidth.android.lib.nonsense.NonsenseBuilder;

/**
 * The app's main activity.
 * @since zerobandwidth-net/android-poppycock 0.0.1 (#1)
 */
public class MainActivity
extends AppCompatActivity
{
    public static final String LOG_TAG = MainActivity.class.getSimpleName() ;

    /**
     * A unique tag marking the activity-state extra containing our last bit of
     * nonsense.
     */
    protected static final String EXTRA_LAST_NONSENSE =
        "net.zerobandwidth.android.apps.poppycock.MainActivity.LAST_NONSENSE" ;

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
    }

    /**
     * Binds persistent references to all of the elements of the layout that we
     * will modify programmatically.
     *
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
     *
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
        this.refreshNonsense( m_sLastNonsense ) ;
    }

    @Override
    protected void onSaveInstanceState( Bundle bndlState )
    {
        super.onSaveInstanceState(bndlState) ;
        bndlState.putCharSequence( EXTRA_LAST_NONSENSE, m_twNonsense.getText() ) ;
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
