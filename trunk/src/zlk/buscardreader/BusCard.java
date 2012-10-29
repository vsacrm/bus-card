package zlk.buscardreader;

import java.util.ArrayList;

public class BusCard {
	private String name = "北京市政公交一卡通";

	private int id;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// 编号
	private String number;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getValidDate() {
		return validDate;
	}

	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}

	public int getUsedTimes() {
		return usedTimes;
	}

	public void setUsedTimes(int usedTimes) {
		this.usedTimes = usedTimes;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	// 版本
	private String version;

	// 有效期起止时间
	private String validDate;

	// 使用次数
	private int usedTimes;

	public ArrayList<BusCardRecord> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<BusCardRecord> records) {
		this.records = records;
	}

	// 余额
	private double balance;

	private ArrayList<BusCardRecord> records;
	
}
