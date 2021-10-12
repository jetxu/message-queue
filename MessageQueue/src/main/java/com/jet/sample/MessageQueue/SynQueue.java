package com.jet.sample.MessageQueue;

public class SynQueue {

	int capacity;
	int count;

	Data[] datas;
	int startIdx = 0;

	Object publishLock = new Object();

	Object subscribeLock = new Object();

	public SynQueue(int capacity) {
		this.capacity = capacity;
		this.datas = new Data[capacity];
	}

	public Data retriveData() {
		this.count--;
		return this.datas[startIdx++];
	}

	public void addData(Data data) {
		this.datas[(this.startIdx + this.count) % this.capacity] = data;
		this.count++;
	}

	public boolean isFull() {
		return this.capacity == this.count;
	}

	public boolean isEmpty() {
		return this.count == 0;
	}

	public void publish(Data data) throws Exception {
		boolean add = false;
		synchronized (this.publishLock) {
			if (this.isFull()) {
				this.publishLock.wait();
				this.addData(data);
				add = true;
			}
		}

		synchronized (this.subscribeLock) {
			if (this.isEmpty()) {
				if (!add)
					this.addData(data);
				this.subscribeLock.notifyAll();
			}
		}

	}

	public Data subscribe() throws Exception {
		Data data = null;
		synchronized (this.publishLock) {
			if (this.isFull()) {
				data = this.retriveData();
				this.publishLock.notifyAll();
			}
		}

		synchronized (this.subscribeLock) {
			if (this.isEmpty()) {

				this.subscribeLock.wait();
				if (data == null)
					data = this.retriveData();
			}
		}

		return data;
	}

	public static class Data {
		String id;
		byte[] data;

		public Data(String id) {
			this.id = id;
		}

	}

}
