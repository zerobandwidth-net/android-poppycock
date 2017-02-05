package net.zerobandwidth.android.apps.poppycock.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.zerobandwidth.android.apps.poppycock.model.Sentence;
import net.zerobandwidth.android.lib.database.SQLitePortal;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to an SQLite database in which we keep historical nonsense.
 * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
 */
public class PoppycockDatabase
extends SQLitePortal
{
/// Statics ////////////////////////////////////////////////////////////////////

    public static final String LOG_TAG =
            PoppycockDatabase.class.getSimpleName() ;

    /**
     * The current version of the Poppycock DB schema.
     * <table>
     *     <thead>
     *         <tr>
     *             <th>App Version</th>
     *             <th>Schema Version</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>1.0.1</td>
     *             <td>1</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    public static final int SCHEMA_VERSION = 1 ;

    /** The filename for the SQLite database. */
    public static final String DATABASE_NAME = "poppycock_db" ;

    /**
     * The table name where the historical records of sentences will be stored.
     */
    public static final String SENTENCE_TABLE_NAME = "sentence" ;

    /**
     * The SQL statement to create the table where all historical entries will
     * be stored.
     */
    public static final String SENTENCE_TABLE_SQL =
              "CREATE TABLE " + SENTENCE_TABLE_NAME + " ("
            + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "item_ts INTEGER, "
            + "sentence TEXT, "
            + "favorite INT "
            + ") ;"
            ;

/// Constructor ////////////////////////////////////////////////////////////////

    /**
     * Constructor which sets the context and then defines parameters for
     * Poppycock's database.
     * @param ctx the context in which the database will exist
     */
    public PoppycockDatabase( Context ctx )
    { super( ctx, DATABASE_NAME, null, SCHEMA_VERSION ) ; }

/// android.database.sqlite.SQLiteOpenHelper ///////////////////////////////////

    @Override
    public void onCreate( SQLiteDatabase db )
    { db.execSQL( SENTENCE_TABLE_SQL ) ; }

    @Override
    public void onUpgrade( SQLiteDatabase db, int nOld, int nNew )
    {
        // Nothing to do yet.
    }

/// net.zerobandwidth.android.lib.database.SQLitePortal (trivial) //////////////

    @Override
    public PoppycockDatabase openDB() { super.openDB() ; return this ; }

    @Override
    public PoppycockDatabase closeDB() { super.closeDB() ; return this ; }

/// Data Marshalling Functions /////////////////////////////////////////////////

    /**
     * Writes nonsense to the historical record, updating its ID along the way.
     * @param o the nonsense to be hoarded
     * @return the same nonsense, lightly massaged
     */
    public synchronized Sentence insertSentence( Sentence o )
    {
        if( m_db == null || o == null ) return null ;
        o.nItemID =
                m_db.insert( SENTENCE_TABLE_NAME, null, o.toContentValues() ) ;
        Log.d( LOG_TAG, (new StringBuilder())
                .append( "Inserted sentence: " )
                .append( o.sSentence )
                .append(( o.bIsFavorite ? " ***" : "" ))
                .toString()
            );
        return o ;
    }

    /**
     * Reads a specific bit of nonsense from the historical record.
     * @param nID the ID of the nonsense
     * @return the nonsense itself
     */
    public synchronized Sentence getSentence( long nID )
    {
        if( m_db == null || nID < 0 ) return null ;
        Cursor crs = m_db.query( SENTENCE_TABLE_NAME, null, "item_id=?",
                new String[] { Long.toString(nID) }, null, null, null, "1" ) ;
        Sentence o = ( crs.moveToFirst() ? Sentence.fromCursor(crs) : null ) ;
        crs.close() ;
        return o ;
    }

    public synchronized ArrayList<Sentence> getFavorites( boolean bOldestFirst )
    {
        if( m_db == null ) return null ;
        ArrayList<Sentence> ao = new ArrayList<>() ;
        Cursor crs = null ;
        try
        {
            crs = m_db.query( SENTENCE_TABLE_NAME, null, "favorite=?",
                    new String[] { Integer.toString(boolToInt(true)) },
                    null, null,
                    ( bOldestFirst ? "item_ts ASC" : "item_ts DESC" ), null ) ;
            final int nCount = crs.getCount() ;
            Log.d( LOG_TAG, (new StringBuilder())
                    .append( "Retrieved [" )
                    .append( nCount )
                    .append(( nCount == 1 ? "] item " : "] items " ))
                    .append( "from the Nonsense Hall of Fame." )
                    .toString()
                );
            if( crs.moveToFirst() )
            {
                do ao.add( Sentence.fromCursor(crs) ) ;
                while( crs.moveToNext() ) ;
            }
        }
        finally
        { SQLitePortal.closeCursor(crs) ; }

        return ao ;
    }

    /**
     * Reads all the nonsense from the historical record.
     * @param bOldestFirst specifies whether to sort oldest-first ({@code true})
     *                     or newest-first ({@code false})
     * @return all the nonsense
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    public synchronized ArrayList<Sentence> getHistory( boolean bOldestFirst )
    {
        if( m_db == null ) return null ;
        ArrayList<Sentence> ao = new ArrayList<>() ;
        Cursor crs = null ;
        try
        {
            crs = m_db.query( SENTENCE_TABLE_NAME, null, null, null, null, null,
                    ( bOldestFirst ? "item_ts ASC" : "item_ts DESC" ), null ) ;
            final int nCount = crs.getCount() ;
            Log.d( LOG_TAG, (new StringBuilder())
                    .append( "Retrieved [" )
                    .append( nCount )
                    .append(( nCount == 1 ? "] item " : "] items " ))
                    .append( "from the Historical Record." )
                    .toString()
                );
            if( crs.moveToFirst() )
            {
                do ao.add( Sentence.fromCursor(crs) ) ;
                while( crs.moveToNext() ) ;
            }
        }
        finally
        { SQLitePortal.closeCursor(crs) ; }

        return ao ;
    }

	/**
	 * Anoints some nonsense to the hall of fame, or demotes it to obscurity.
     * @param o the sentence to be toggled
     * @return the updated sentence
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#3)
     */
    public synchronized Sentence toggleFavorite( Sentence o )
    {
        if( m_db == null ) return null ;
        o.bIsFavorite = ! o.bIsFavorite ;
        final int nUpdated =m_db.update( SENTENCE_TABLE_NAME,
                o.toContentValues(), "item_id=?",
                new String[] { Long.toString( o.nItemID ) } ) ;
        Log.d( LOG_TAG, (new StringBuilder())
                .append( "Updated [" ).append( nUpdated )
                .append(( nUpdated == 1 ? "] row." : "] rows." ))
                .toString()
            );
        return o ;
    }

	/**
	 * Deletes a set of nonsense from the historical record.
     * @param bFavorites if true, then only favorites will be deleted; if false,
     *                   then only non-favorites will be deleted
     * @return the number of records deleted
     */
    @SuppressWarnings( "UnnecessaryLocalVariable" ) // I respectlessly disagree.
    public synchronized int delete( boolean bFavorites )
    {
        if( m_db == null ) return 0 ;
        final int nCount = m_db.delete( SENTENCE_TABLE_NAME, "favorite=?",
                new String[] { Integer.toString( boolToInt(bFavorites) ) } ) ;
        return nCount ;
    }
}
