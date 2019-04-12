package com.saramin.sai;

import com.saramin.sai.service.ConanExecutor;
import com.saramin.sai.service.SherlockExecutor;

/**
 * Sherlock Main Class
 * 
 * @author Jinhoo Jang
 * @since 2018.12.13
 */
public class SherlockMain {

	public static void main(String[] args) {
		// 자기소개서를 해시화 한 후 처리
		if(args[0].equals("pre")) {
			SherlockExecutor sherlock = new SherlockExecutor("sherlock.local");
			sherlock.pre();
		}
		// HR 컨설팅 전용
		else if(args[0].equals("conan")) {
			ConanExecutor sherlock = new ConanExecutor("sherlock.local");
			sherlock.pre();
		}
		// 데일리 파일을 머지
		else if(args[0].equals("merge")) {
			SherlockExecutor sherlock = new SherlockExecutor("sherlock.local");
		}
		// 자기소개서를 연산한다
		else {
			SherlockExecutor sherlock = new SherlockExecutor("sherlock.local");
			sherlock.execute();
		}
	}
}
