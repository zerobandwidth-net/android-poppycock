package net.zerobandwidth.android.apps.poppycock.model;

import android.content.ContentValues;
import android.database.Cursor;

import net.zerobandwidth.android.lib.database.SQLitePortal;

/**
 * Container for a nonsense sentence, suitable for marshalling into the
 * database. Unlike other data objects, this one does not hide its fields.
 * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
 */
public class Sentence
{
    /** Indicates that the instance has not yet been stored in the database. */
    public static long NOT_IDENTIFIED = -1L ;

    /**
     * Marshals data out of a {@link Cursor} containing a database row.
     * @param crs the cursor
     * @return an instance with values extracted from the cursor
     */
    public static Sentence fromCursor( Cursor crs )
    {
        Sentence o = new Sentence() ;
        o.nItemID = crs.getLong( crs.getColumnIndex( "item_id" ) ) ;
        o.nItemTS = crs.getLong( crs.getColumnIndex( "item_ts" ) ) ;
        o.sSentence = crs.getString( crs.getColumnIndex( "sentence" ) ) ;
        o.bIsFavorite = SQLitePortal.intToBool(
                crs.getInt( crs.getColumnIndex( "favorite" ) ) ) ;
        return o ;
    }

    /** The numeric index of the record. */
    public long nItemID = NOT_IDENTIFIED ;

    /** The timestamp at which the sentence was created. */
    public long nItemTS = 0L ;

    /** The nonsense itself. */
    public String sSentence = null ;

    /** Indicates whether the sentence has been marked as a favorite. */
    public boolean bIsFavorite = false ;

    /**
     * Marshals the object into {@link ContentValues} for storage in the
     * database.
     * @return the object's fields, for storage in the DB
     */
    public ContentValues toContentValues()
    {
        ContentValues vals = new ContentValues() ;
        if( nItemID != NOT_IDENTIFIED ) vals.put( "item_id", nItemID ) ;
        vals.put( "item_ts", nItemTS ) ;
        vals.put( "sentence", sSentence ) ;
        vals.put( "favorite", SQLitePortal.boolToInt(bIsFavorite) ) ;
        return vals ;
    }
}
