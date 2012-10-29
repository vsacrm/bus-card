package zlk.buscardreader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends TabActivity {

	// private String cardId;
	BusCard currentCard;

	// DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("北京公交一卡通");
		// 隐藏标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		Button btnDownload = (Button) findViewById(R.id.btnDownload);
		btnDownload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Runnable downloadRun = new Runnable() {

					public void run() {
						String url = "http://www.bjsuperpass.com/pagecontrol.do?object=ecard&action=query";

						try {
							Document doc = Jsoup.connect(url)
									.data("card_id", currentCard.getNumber()) // "10007510639517344"
									.timeout(10000).post();

							Elements tables = doc.select("table");
							if (tables.size() < 7) {
								// txtContent.setText("再次查询请间隔30秒");
								return;
							}
							Elements rows = doc.select("table").get(7)
									.select("tr");
							int count = rows.size();
							ArrayList<BusCardRecord> records = new ArrayList<BusCardRecord>();
							for (int i = 2; i < count; i++) {
								Element item = rows.get(i);
								Elements tds = item.select("td");

								String time = tds.get(0).text();
								String type = tds.get(1).text();
								String money = tds.get(2).text();
								String balance = tds.get(3).text();
								String bus = tds.get(4).text();
								String interval = tds.get(5).text();

								BusCardRecord record = new BusCardRecord();

								record.setTime(time);

								record.setConsumeType(type);
								record.setMoney(Double.parseDouble(money));
								record.setBus(bus);
								record.setBalance(Double.parseDouble(balance));
								record.setInterval(interval);
								record.setCardId(String.valueOf(currentCard
										.getId()));
								records.add(record);
							}

							// 存储到数据库
							for (BusCardRecord record : records) {
								insertRecord(record);
							}

							// 读取数据
							loadRecords(currentCard);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				new Thread(downloadRun).start();
			}
		});
		// 取得TabHost对象
		TabHost mTabHost = getTabHost();

		// 新建一个newTabSpec(newTabSpec)
		// 设置其标签和图标(setIndicator)
		// 设置内容(setContent)
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("Info")
				.setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2")
				.setIndicator("Records").setContent(R.id.tab2));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator("Log")
				.setContent(R.id.tab3));

		onNewIntent(getIntent());
	}

	private void insertRecord(BusCardRecord record) {
		try {
			DatabaseHelper openHelper = new DatabaseHelper(this);
			SQLiteDatabase db = openHelper.getWritableDatabase();
			// todo: 先查询是不是有重复的
			String[] col = { "ConsumeTime", "money" };
			Cursor cur = db.query(
					"records",
					col,
					"CardId=? and ConsumeTime=?",
					new String[] { String.valueOf(currentCard.getId()),
							record.getTime() }, null, null, null);
			if (cur.getCount() > 0)
				return;

			String sql = "insert into records (CardId,ConsumeTime,money) values ('"
					+ record.getCardId()
					+ "','"
					+ record.getTime()
					+ "','"
					+ record.getMoney() + "')";
			db.execSQL(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		TextView txtCardNumber = (TextView) findViewById(R.id.txtCardNumber);
		TextView txtCardVersion = (TextView) findViewById(R.id.txtCardVersion);
		TextView txtCardBalance = (TextView) findViewById(R.id.txtCardBalance);
		TextView txtCardUsedTimes = (TextView) findViewById(R.id.txtCardUsedTimes);
		TextView txtCardValidDate = (TextView) findViewById(R.id.txtCardValidDate);

		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		if (tag == null)
			return;

		try {

			Resources res = getResources();
			// 根据tag返回的类型，判断卡的类型是IsoDep，NfcV还是其他类型
			final IsoDep isodep = IsoDep.get(tag);
			if (isodep != null) {
				// PbocCard card = PbocCard.load(isodep, res);
				BusCard card = BusCardReader.readCard(isodep, res);
				currentCard = card;
				// cardId = card.getNumber();

				txtCardNumber.setText(card.getNumber());
				txtCardVersion.setText(card.getVersion());
				DecimalFormat df = new DecimalFormat("##.00");// 取到小数点后2位
				txtCardBalance.setText(df.format(card.getBalance()) + "元");
				txtCardUsedTimes.setText(String.valueOf(card.getUsedTimes()));
				txtCardValidDate.setText(card.getValidDate());

				int cardId;
				// todo:记录本张卡的信息
				DatabaseHelper openHelper = new DatabaseHelper(this);
				SQLiteDatabase db = openHelper.getWritableDatabase();
				// 先查询是不是有重复的
				String[] col = { "Id" };
				Cursor cur = db.query("BusCards", col, "CodeNumber=?",
						new String[] { card.getNumber() }, null, null, null);
				// cardId = cur.getInt(0);
				if (cur.getCount() == 0) {
					// todo:存储卡信息
					String sql = "insert into BusCards (CodeNumber,Version,UsedTimes,ValidPeriod) values ('"
							+ card.getNumber()
							+ "','"
							+ card.getVersion()
							+ "','"
							+ card.getUsedTimes()
							+ "','"
							+ card.getValidDate() + "')";
					db.execSQL(sql);
					// 取出cardId
				}
				cur = db.query("BusCards", col, "CodeNumber=?",
						new String[] { card.getNumber() }, null, null, null);
				if (cur.moveToNext()) {
					cardId = cur.getInt(0);
					currentCard.setId(cardId);
					// 记录本次查询记录
					String sql = "insert into QueryLogs (CardId,QueryTime,Banlance) values ('"
							+ cardId
							+ "','"
							+ new Date()
							+ "','"
							+ card.getBalance() + "')";
					db.execSQL(sql);
					// 写卡存数据到数据库
					ArrayList<BusCardRecord> records = card.getRecords();
					// 存储到数据库
					for (BusCardRecord record : records) {
						record.setCardId(String.valueOf(currentCard.getId()));
						insertRecord(record);
					}

					loadRecords(card);

					loadQueryLogs();
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadRecords(BusCard card) {
		try {

			// ArrayList<BusCardRecord> records = card.getRecords();
			// fill records
			ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

			// for (BusCardRecord record : records) {
			// Map<String, Object> item = new HashMap<String, Object>();
			// item.put("date", record.getTime());
			// item.put("money", record.getMoney());
			// data.add(item);
			// }

			DatabaseHelper openHelper = new DatabaseHelper(this);
			SQLiteDatabase db = openHelper.getWritableDatabase();
			String[] col = { "ConsumeTime", "money" };
			Cursor cur = db.query("records", col, "CardId=?",
					new String[] { String.valueOf(currentCard.getId()) }, null,
					null, null);
			ArrayList<BusCardRecord> records = new ArrayList<BusCardRecord>();

			while (cur.moveToNext()) {
				BusCardRecord record = new BusCardRecord();

				record.setTime(cur.getString(0));

				record.setMoney(Double.parseDouble(cur.getString(1)));
				records.add(record);
			}
			// fill records

			for (BusCardRecord record : records) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("date", record.getTime());
				item.put("money", record.getMoney());
				data.add(item);
			}

			ListView lvRecord = (ListView) findViewById(R.id.lvNetworkRecords);
			lvRecord.setAdapter(new SimpleAdapter(this, data,
					R.layout.record_item, new String[] { "date", "money" },
					new int[] { R.id.txtTime, R.id.txtMoney }));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadQueryLogs() {
		try {

			ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

			DatabaseHelper openHelper = new DatabaseHelper(this);
			SQLiteDatabase db = openHelper.getWritableDatabase();
			String[] col = { "QueryTime", "Banlance" };
			Cursor cur = db.query("QueryLogs", col, null, null, null, null,
					null);
			ArrayList<BusCardRecord> records = new ArrayList<BusCardRecord>();

			while (cur.moveToNext()) {

				Map<String, Object> item = new HashMap<String, Object>();
				item.put("time", cur.getString(0));
				item.put("banlance", cur.getFloat(1));
				data.add(item);
			}

			ListView lvRecord = (ListView) findViewById(R.id.lvLogs);
			lvRecord.setAdapter(new SimpleAdapter(this, data,
					R.layout.record_item, new String[] { "time", "banlance" },
					new int[] { R.id.txtQueryTime, R.id.txtQueryBanlance }));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
