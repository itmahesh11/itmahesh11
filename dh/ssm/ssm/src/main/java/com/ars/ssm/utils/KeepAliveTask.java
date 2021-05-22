package com.ars.ssm.utils;

import java.util.TimerTask;
import com.ars.ssm.connection.StopSystemBrokerManager;
/**
 * 
 * @author Abhijith Ravindran
 * This class sends Alive status to StopSystemBroker
 * at a set interval.
 */
public class KeepAliveTask extends TimerTask {
    @Override
    public void run() {
        completeTask();
    }
    
    private void completeTask() {
    	StopSystemBrokerManager.publishKeepAliveMessage();
        try {
            Thread.sleep(8000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
