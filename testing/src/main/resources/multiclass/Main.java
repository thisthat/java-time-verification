package com.jcg.example;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.jcg.example.JavaTimerExampleTask;

public class Main {
    
    public static void main(String[] args) {
        
        TimerTask task = new JavaTimerExampleTask();
        
        // true means : associated thread should run as a daemon
        Timer timer = new Timer(true);
        timer.getClass().getSimpleName().equals("test");
        
        // Subsequent executions take place at approximately regular intervals,
        // separated by the specified period.
        timer.schedule(task, 0, 5000);
        System.out.println("The schedular has started");
        
        try {
            // Putting a sleep of 10000 ms so that the task can run twice as it
            // is scheduled to run every 500ms
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
