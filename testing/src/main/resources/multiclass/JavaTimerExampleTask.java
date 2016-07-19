package com.jcg.example;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class JavaTimerExampleTask extends TimerTask {

	@Override
	public void run() {
		System.out.println("The execution of task started at: " + new Date());
		// put task implementation here

		// put a sleep
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("The execution of task finished at: " + new Date());

	}

}

