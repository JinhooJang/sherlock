package com.saramin.sai.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 로그 클래스
 * 
 * @author jinhoo.jang
 * @team a.i part
 * @since 2018.08.16
 */
public class Logger {
	private boolean isDebug;
	private String service;
	private String logPath;
	
	
	final String NEWLINE = System.getProperty("line.separator");
	
	
	public Logger(boolean isDebug, String service, String logPath) {
		this.isDebug = isDebug;
		this.service = service;
		this.logPath = logPath;
	}
	
	
	/**
	 * 디버깅 모드일 경우 호출
	 * @param msg
	 */
	public void debug(String msg) {
		if(!isDebug) 
			return;
		
		System.out.println(msg);
		writeLog(msg, "DEBUG");
	}
	
	
	/**
	 * 무조건 호출
	 * @param msg
	 */
	public void info(String msg) {
		System.out.println(msg);
		writeLog(msg, "INFO");
	}
	
	
	/**
	 * 에러 호출
	 * @param msg
	 */
	public void error(String msg) {
		System.out.println(msg);
		writeLog(msg, "ERROR");
	}
	
	
	/**
	 * 로그 쓰기
	 * @param msg
	 * @param mode
	 */
	public void writeLog(String msg, String mode) {
		BufferedWriter bw;
		String filepath = "log.txt";
		
		// 에러일 경우, 파일명을 변경
		if(mode.equals("ERROR"))
			filepath = "error.txt";
		
		// 날짜생성
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdfYYYMMDD = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Date now = new Date();
		
		
		try {   
			bw = new BufferedWriter(
			new OutputStreamWriter(
					new FileOutputStream(logPath + "/sherlock" + sdfYYYMMDD + "-" + filepath, true),	// true to append 
					StandardCharsets.UTF_8));	// set encoding utf-8
			
			bw.write("[" + sdf.format(now) + "] " + mode + " " + service + ": " + msg + NEWLINE);
			bw.close();
		}catch(IOException e){
			System.err.println(e.getMessage());
		}
	}
}
