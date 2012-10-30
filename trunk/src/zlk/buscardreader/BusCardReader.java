package zlk.buscardreader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.content.res.Resources;
import android.nfc.tech.IsoDep;

/**
 * 公交卡的数据读取类
 * 
 * */
public class BusCardReader {
	private final static int SFI_EXTRA_LOG = 4;
	private final static int SFI_EXTRA_CNT = 5;
	private static final String SP = "<br/><img src=\"spliter\"/><br/>";

	protected final static byte[] DFI_MF = { (byte) 0x3F, (byte) 0x00 };
	protected final static byte[] DFI_EP = { (byte) 0x10, (byte) 0x01 };

	protected final static byte[] DFN_PSE = { (byte) '1', (byte) 'P',
			(byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
			(byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F',
			(byte) '0', (byte) '1', };

	protected final static byte[] DFN_PXX = { (byte) 'P' };

	protected final static int MAX_LOG = 10;

	private BusCardReader(Iso7816.Tag tag, Resources res) {
		this(tag);
		name = res.getString(R.string.name_bj);
	}

	protected final static int SFI_EXTRA = 21;
	protected final static int SFI_LOG = 24;

	protected final static byte TRANS_CSU = 6;
	protected final static byte TRANS_CSU_CPX = 9;

	protected String name;
	protected String id;
	protected String serl;
	protected String version;
	protected String date;
	protected String count;
	protected String cash;
	protected String log;

	public static BusCard readCard(IsoDep tech, Resources res) {
		final Iso7816.Tag tag = new Iso7816.Tag(tech);

		tag.connect();

		BusCard card = parse(tag, res);
		// if (card != null) {
		//
		// }
		tag.close();
		// return (card != null) ? card.toString(res) : null;
		return card;
	}

	private static BusCard parse(Iso7816.Tag tag, Resources res) {
		/*--------------------------------------------------------------*/
		// select PSF (1PAY.SYS.DDF01)
		/*--------------------------------------------------------------*/
		if (tag.selectByName(DFN_PSE).isOkey()) {

			Iso7816.Response INFO, CNT, CASH;

			/*--------------------------------------------------------------*/
			// read card info file, binary (4)
			/*--------------------------------------------------------------*/
			INFO = tag.readBinary(SFI_EXTRA_LOG);
			if (INFO.isOkey()) {

				/*--------------------------------------------------------------*/
				// read card operation file, binary (5)
				/*--------------------------------------------------------------*/
				CNT = tag.readBinary(SFI_EXTRA_CNT);

				/*--------------------------------------------------------------*/
				// select Main Application
				/*--------------------------------------------------------------*/
				if (tag.selectByID(DFI_EP).isOkey()) {

					/*--------------------------------------------------------------*/
					// read balance
					/*--------------------------------------------------------------*/
					CASH = tag.getBalance(true);

					/*--------------------------------------------------------------*/
					// read log file, record (24)
					/*--------------------------------------------------------------*/
					ArrayList<byte[]> LOG = readLog(tag, SFI_LOG);

					/*--------------------------------------------------------------*/
					// build result string
					/*--------------------------------------------------------------*/
					final BusCard card = new BusCard();

					parseBalance(CASH, card);
					parseInfo(INFO, CNT, card);
					parseLog(card, LOG);

					return card;
				}
			}
		}

		return null;
	}

	protected BusCardReader(Iso7816.Tag tag) {
		id = tag.getID().toString();
	}

	protected static boolean addLog(final Iso7816.Response r,
			ArrayList<byte[]> l) {
		if (!r.isOkey())
			return false;

		final byte[] raw = r.getBytes();
		final int N = raw.length - 23;
		if (N < 0)
			return false;

		for (int s = 0, e = 0; s <= N; s = e) {
			l.add(Arrays.copyOfRange(raw, s, (e = s + 23)));
		}

		return true;
	}

	protected static ArrayList<byte[]> readLog(Iso7816.Tag tag, int sfi) {
		final ArrayList<byte[]> ret = new ArrayList<byte[]>(MAX_LOG);
		final Iso7816.Response rsp = tag.readRecord(sfi);
		if (rsp.isOkey()) {
			addLog(rsp, ret);
		} else {
			for (int i = 1; i <= MAX_LOG; ++i) {
				if (!addLog(tag.readRecord(sfi, i), ret))
					break;
			}
		}

		return ret;
	}

	private static void parseInfo(Iso7816.Response info, Iso7816.Response cnt,
			BusCard card) {
		if (!info.isOkey() || info.size() < 32) {
			// serl = version = date = count = null;
			return;
		}

		final byte[] d = info.getBytes();
		card.setNumber(Util.toHexString(d, 0, 8));
		String version = String.format("%02X.%02X%02X", d[8], d[9], d[10]);
		String validDate = String.format(
				"%02X%02X-%02X-%02X - %02X%02X.%02X.%02X", d[24], d[25], d[26],
				d[27], d[28], d[29], d[30], d[31]);
		card.setVersion(version);
		card.setValidDate(validDate);
		String count = null;

		if (cnt != null && cnt.isOkey() && cnt.size() > 4) {
			byte[] e = cnt.getBytes();
			final int n = Util.toInt(e, 1, 4);
			if (e[0] == 0)
				count = String.format("%d ", n);
			else
				count = String.format("%d* ", n);
		}
		card.setUsedTimes(Integer.parseInt(count.trim()));
	}

	private static void parseLog(BusCard card, ArrayList<byte[]>... logs) {
		final StringBuilder r = new StringBuilder();

		ArrayList<BusCardRecord> records = new ArrayList<BusCardRecord>();

		for (final ArrayList<byte[]> log : logs) {
			if (log == null)
				continue;

			// if (r.length() > 0)
			// r.append("<br />--------------");

			for (final byte[] v : log) {
				final int cash = Util.toInt(v, 5, 4);
				if (cash > 0) {
					BusCardRecord record = new BusCardRecord();
					// consume time
					String time = String.format(
							"%02X%02X-%02X-%02X %02X:%02X ", v[16], v[17],
							v[18], v[19], v[20], v[21], v[22]);
					 

					record.setTime(time);

					// consume money
					final char t = (v[9] == TRANS_CSU || v[9] == TRANS_CSU_CPX) ? '-'
							: '+';

					String money = t + Util.toAmountString(cash / 100.0f);
					record.setMoney(Double.parseDouble(money));
					// final int over = Util.toInt(v, 2, 3);
					// if (over > 0)
					// r.append(" [o:")
					// .append(Util.toAmountString(over / 100.0f))
					// .append(']');

					String uid = Util.toHexString(v, 10, 6);
					record.setUid(money);

					records.add(record);
				}
			}
		}

		// todo:把log类型由String改为Record强类型的
		// card.setPlainText(r.toString());
		// this.log = r.toString();
		card.setRecords(records);
	}

	private static void parseBalance(Iso7816.Response data, BusCard card) {
		if (!data.isOkey() || data.size() < 4) {
			card.setBalance(0);// todo: cast to null
			return;
		}

		int n = Util.toInt(data.getBytes(), 0, 4);
		if (n > 100000 || n < -100000)
			n -= 0x80000000;

		card.setBalance(n / 100.0f);
	}

	protected String formatInfo(Resources res) {
		if (serl == null)
			return null;

		final StringBuilder r = new StringBuilder();

		r.append(res.getString(R.string.lab_serl)).append(' ').append(serl);

		if (version != null) {
			final String sv = res.getString(R.string.lab_ver);
			r.append("<br />").append(sv).append(' ').append(version);
		}

		if (date != null) {
			final String sd = res.getString(R.string.lab_date);
			r.append("<br />").append(sd).append(' ').append(date);
		}

		if (count != null) {
			final String so = res.getString(R.string.lab_op);
			final String st = res.getString(R.string.lab_op_time);
			r.append("<br />").append(so).append(' ').append(count).append(st);
		}

		return r.toString();
	}

	protected String formatLog(Resources res) {
		if (log == null || log.length() < 1)
			return null;

		final StringBuilder ret = new StringBuilder();
		final String sl = res.getString(R.string.lab_log);
		ret.append("<b>").append(sl).append("</b><small>");
		ret.append(log).append("</small>");

		return ret.toString();
	}

	protected String formatBalance(Resources res) {
		if (cash == null || cash.length() < 1)
			return null;

		final String s = res.getString(R.string.lab_balance);
		final String c = res.getString(R.string.lab_cur_cny);
		return new StringBuilder("<b>").append(s)
				.append("<font color=\"teal\"> ").append(cash).append(' ')
				.append(c).append("</font></b>").toString();
	}

	protected String toString(Resources res) {
		final String info = formatInfo(res);
		final String hist = formatLog(res);
		final String cash = formatBalance(res);

		return buildResult(name, info, cash, hist);
	}

	public static String buildResult(String n, String i, String d, String x) {
		if (n == null)
			return null;

		final StringBuilder s = new StringBuilder();

		s.append("<br/><b>").append(n).append("</b>");

		if (i != null)
			s.append(SP).append(i);

		if (d != null)
			s.append(SP).append(d);

		if (x != null)
			s.append(SP).append(x);

		return s.append("<br/><br/>").toString();
	}
}
