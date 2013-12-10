package stork;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * For storing dap responses into mobile db for showing progress of jobs
 * 
 * @author Rishi
 * 
 */
public class DatabaseWrapper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	
	private static final String TABLE_NAME = "storkClientTable";
	private static final String DB_NAME = "storkAndroidClient";

	private static final String JOB_ID = "jobId";
	private static final String SERVER_ONE = "serverOneUrl";
	private static final String SERVER_TWO = "serverTwoURL";

	public DatabaseWrapper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	/**
	 * called the very first time only to create the table
	 */
	public void onCreate(SQLiteDatabase db) {
		String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" 
				+ JOB_ID 	 + " INTEGER PRIMARY KEY	," 
				+ SERVER_ONE + " TEXT ,"
				+ SERVER_TWO + " TEXT " + ");";
		db.execSQL(TABLE_CREATE);
	}

	/**
	 * Insert the response from server into the table for progress views
	 * 
	 * @param response
	 * @return
	 */
	public boolean onInsertDAP(String response){
		
		//TODO make sure it's a DAP response
		
		String data[] = response.split(";");
		
		return onInsert(data[0], data[1], data[2]);
	}
	
	/**
	 * insert in table
	 * 
	 * @param jobId
	 * @param one_url
	 * @param two_url
	 * @return
	 */
	private boolean onInsert(String jobId, String one_url, String two_url) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		Log.v("onInsert", "called");
		cv.put(JOB_ID, jobId);
		cv.put(SERVER_ONE, one_url);
		cv.put(SERVER_TWO, two_url);
		long retVal = db.insert(TABLE_NAME, jobId, cv);
		db.close();
		return (retVal >= 0);
	}

	/**
	 * delete from table
	 * 
	 * @param jobId
	 * @return
	 */
	public boolean onDelete(Long jobId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int retVal = db.delete(TABLE_NAME, JOB_ID + " = '" + jobId + "'", null);
		Log.v(getClass().getSimpleName(),"On Delete returns :" + retVal);
		db.close();
		return (retVal > 0);
	}

	/**
	 * execute any set of queries
	 * 
	 * @param query
	 */
	public void onQuery(String... query) {
		SQLiteDatabase db = this.getWritableDatabase();
		for (String q : query)		db.execSQL(q);//TODO check if transaction successful
		db.close();
	}
	
	/**
	 * update entries in table
	 * 
	 * @param jobId
	 * @param one_url
	 * @param two_url
	 * @return
	 */
	public boolean onUpdate(String jobId, String one_url, String two_url) {
		// for future
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(SERVER_ONE, one_url);
		cv.put(SERVER_TWO, two_url);
		int retVal = db.update(TABLE_NAME, cv, JOB_ID + "=?", new String[] { jobId });
		db.close();
		return (retVal > 0);
	}
	
	/**
	 * Select All Job Ids
	 * 
	 * @return
	 */
	public String[][] onSelectAll(){
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cur = db.query(TABLE_NAME, null, null, null, null, null, null);
		Log.v(getClass().getName(), "cur.getCount() : " + cur.getCount() );
		String[][] queryResult = new String[cur.getCount()][3];
		int N = cur.getCount();
		int i = 0;
		cur.moveToFirst();
		while (i < N) {
			queryResult[i][0] = cur.getString(0);
			queryResult[i][1] = cur.getString(1);
			queryResult[i][2] = cur.getString(2);
			cur.moveToNext();
			Log.v(getClass().getName(), "Row " + i + " : " + queryResult[i][0] + "," + queryResult[i][1] + "," + queryResult[i][2]);
			i++;
		}
		cur.close();
		db.close();
		
		return queryResult;
	}
			
	@Override
	/**
	 * when db is being upgraded
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}
