package zlk.buscardreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "buscard.db";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "Create table records (Id int primary key ,CardId int not null,ConsumeTime varchar(20) not null,money text not null);";
		String sql2 = "create table BusCards (Id INTEGER PRIMARY KEY AUTOINCREMENT,CodeNumber VARCHAR(20) not null,Version varchar(20) not null,UsedTimes INT, ValidPeriod  VARCHAR(30));";
		String sql3 = "Create table QueryLogs (Id int primary key ,CardId int not null,QueryTime Date not null,Banlance text not null);";
		db.execSQL(sql);
		db.execSQL(sql2);
		db.execSQL(sql3);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
