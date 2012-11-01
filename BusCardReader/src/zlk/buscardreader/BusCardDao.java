package zlk.buscardreader;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BusCardDao {
//	public static void insertRecord(BusCardRecord record) {
//		try {
//			DatabaseHelper openHelper = new DatabaseHelper(this);
//			SQLiteDatabase db = openHelper.getWritableDatabase();
//			// todo: 先查询是不是有重复的
//			String[] col = { "time", "money" };
//			Cursor cur = db
//					.query("records", col, "card_id=? and time=?",
//							new String[] { cardId, record.getTime() }, null,
//							null, null);
//			if (cur.getCount() > 0)
//				return;
//
//			String sql = "insert into records (card_id,time,money) values ('"
//					+ record.getCardId() + "','" + record.getTime() + "','"
//					+ record.getMoney() + "')";
//			db.execSQL(sql);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
