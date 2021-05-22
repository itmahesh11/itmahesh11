package com.mm.service;

import java.io.File;

import com.mm.config.Configuration;
import com.mm.config.MMConfig;

public class DiskspaceCheckService {
	
	private Configuration config;
	private MMConfig mmConfig;
	
	public DiskspaceCheckService(Configuration config,MMConfig mmConfig) {
		this.config= config;
		this.mmConfig=mmConfig;
		
	}
	
	public void checkDiskspace() {
		try {
			File diskPartition = new File("D:");
			 
	        long totalCapacity = diskPartition.getTotalSpace(); 
	 
	        long freePartitionSpace = diskPartition.getFreeSpace();
	        
	        System.out.println("Total C partition size : " + totalCapacity / (1024*1024) + " MB");
	        System.out.println("Free Space : " + freePartitionSpace / (1024 *1024) + " MB");
		} catch (Exception e) {
			
		}
	}
	
	public static void main(String[] args) {
		 
        File diskPartition = new File("D:");
 
        long totalCapacity = diskPartition.getTotalSpace(); 
 
        long freePartitionSpace = diskPartition.getFreeSpace(); 
        long usablePatitionSpace = diskPartition.getUsableSpace(); 
 
        System.out.println("**** Sizes in Mega Bytes ****\n");
 
        System.out.println("Total C partition size : " + totalCapacity / (1024*1024) + " MB");
        System.out.println("Usable Space : " + usablePatitionSpace / (1024 *1024) + " MB");
        System.out.println("Free Space : " + freePartitionSpace / (1024 *1024) + " MB");
 
        System.out.println("\n**** Sizes in Giga Bytes ****\n");
 
        System.out.println("Total C partition size : " + totalCapacity / (1024*1024*1024) + " GB");
        System.out.println("Usable Space : " + usablePatitionSpace / (1024 *1024*1024) + " GB");
        System.out.println("Free Space : " + freePartitionSpace / (1024 *1024*1024) + " GB");
    }


}
