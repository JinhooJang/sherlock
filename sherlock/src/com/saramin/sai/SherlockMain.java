package com.saramin.sai;

import com.saramin.sai.service.SherlockExecutor;

/**
 * Sherlock Main Class
 * 
 * @author Jinhoo Jang
 * @since 2018.12.13
 */
public class SherlockMain {

	public static void main(String[] args) {
		SherlockExecutor sherlock = new SherlockExecutor("sherlock.local");
		
		if(args[0].equals("pre")) {
			sherlock.pre(Boolean.valueOf(args[1]));
		} else {
			sherlock.bulk();
		}
	}
}
