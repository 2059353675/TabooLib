package me.skymc.taboolib.thread;

import java.util.*;

public class ThreadUtils {
	
	private static PoolWorker[] threads;
	private static LinkedList<Runnable> queue = new LinkedList<>();

	/**
	 * ���췽��
	 * 
	 * @param number �߳�����
	 */
	public ThreadUtils(int number) {
		threads = new PoolWorker[number];

		for (int i = 0; i < number; i++)  {
			threads[i] = new PoolWorker();
			threads[i].setName("TabooLib WorkThread - " + i);
			threads[i].start();
		}
	}
	
	/**
	 * ֹͣ����
	 * 
	 */
	public void stop() {
		for (PoolWorker p : threads) {
			p.stop();
		}
	}

	/**
	 * �������
	 * 
	 * @param r
	 */
	public void execute(Runnable r) {
		// �߳���
		synchronized (queue) {
			// �������
			queue.addLast(r);
			// ��ʼ����
			queue.notify();
		}
	}

	private class PoolWorker extends Thread {
		
		@Override
		public void run()  {
			Runnable runnable;
			
			while (true) {
				
				// �߳���
				synchronized (queue) {
					
					// �������Ϊ��
					while (queue.isEmpty())  {
						// �ȴ�����
						try {
							queue.wait();
						} 
						catch (InterruptedException e) {
							
						}
					}
					// ��ȡ����
					runnable = queue.removeFirst();
				}
				
				// ��������
				try {
					runnable.run();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}