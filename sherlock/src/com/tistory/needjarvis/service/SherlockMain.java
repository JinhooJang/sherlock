package com.tistory.needjarvis.service;


/**
 * Sherlock Main Class
 * 
 * @author Jinhoo Jang
 * @since 2018.12.13
 */
public class SherlockMain {

	public static void main(String[] args) {
		SherlockExecutor sherlock = new SherlockExecutor("sherlock.local.");
		sherlock.execute();
	}
}
