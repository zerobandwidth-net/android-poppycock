package net.zerobandwidth.android.apps.poppycock.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import net.zerobandwidth.android.lib.database.SQLitePortal;

import java.util.Date;

/**
 * Container for a nonsense sentence, suitable for marshalling into the
 * database. Unlike other data objects, this one does not hide its fields.
 * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
 */
public class Sentence
implements Parcelable
{
    /** Indicates that the instance has not yet been stored in the database. */
    public static long NOT_IDENTIFIED = -1L ;

    /** The numeric index of the record. */
    public long nItemID = NOT_IDENTIFIED ;

    /** The timestamp at which the sentence was created. */
    public long nItemTS = (new Date()).getTime() ;

    /** The nonsense itself. */
    public String sSentence = null ;

    /** Indicates whether the sentence has been marked as a favorite. */
    public boolean bIsFavorite = false ;

/// Database Exchange //////////////////////////////////////////////////////////

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

/// Parcel Exchange ////////////////////////////////////////////////////////////

    /** Required by {@link Parcelable}. */
    public static final Creator<Sentence> CREATOR = new Sentence.Parceler() ;

    /**
     * Parcelizer for the {@link Sentence} class, required by the
     * {@link Parcelable} interface.
     * @since zerobandwidth-net/android-poppycock 1.0.1 (#2)
     */
    public static class Parceler
    implements Creator<Sentence>
    {
        @Override
        public Sentence createFromParcel( Parcel pcl )
        { return (new Sentence()).readFromParcel(pcl) ; }

        @Override
        public Sentence[] newArray( int nSize )
        { return new Sentence[nSize] ; }
    }

    /**
     * Populates the fields of this instance based on the extras in a
     * {@link Parcel}.
     * @param pcl the parcel containing items for this class
     * @return this instance, updated with values from the parcel
     */
    public Sentence readFromParcel( Parcel pcl )
    {
        this.nItemID = pcl.readLong() ;
        this.nItemTS = pcl.readLong() ;
        this.sSentence = pcl.readString() ;
        this.bIsFavorite = SQLitePortal.intToBool( pcl.readInt() ) ;
        return this ;
    }

    @Override
    public void writeToParcel( Parcel pcl, int zFlags )
    {
        pcl.writeLong( this.nItemID ) ;
        pcl.writeLong( this.nItemTS ) ;
        pcl.writeString( this.sSentence ) ;
        pcl.writeInt( SQLitePortal.boolToInt(this.bIsFavorite) ) ;
    }

    @Override
    public int describeContents()
    { return 0 ; }
}
