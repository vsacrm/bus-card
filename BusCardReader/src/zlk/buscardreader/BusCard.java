package zlk.buscardreader;

import java.util.ArrayList;

public class BusCard {
	private String name = "������������һ��ͨ";

	private int id;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// ���
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

	// �汾
	private String version;

	// ��Ч����ֹʱ��
	private String validDate;

	// ʹ�ô���
	private int usedTimes;

	public ArrayList<BusCardRecord> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<BusCardRecord> records) {
		this.records = records;
	}

	// ���
	private double balance;

	private ArrayList<BusCardRecord> records;
	
}
