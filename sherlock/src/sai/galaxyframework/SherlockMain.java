package sai.galaxyframework;

import sai.galaxyframework.sherlock.service.ConanExecutor;
import sai.galaxyframework.sherlock.service.SherlockExecutor;

/**
 * Sherlock Main Class
 * 
 * @author Jinhoo Jang
 * @since 2018.12.13
 */
public class SherlockMain {

	public static void main(String[] args) {
		SherlockExecutor sherlock = null;
		// 자기소개서를 해시화 한 후 처리
		if(args[0].equals("init")) {
			if(args[1].equals("B")) {
				sherlock = new SherlockExecutor("sherlock.local", true);
			} else {
				sherlock = new SherlockExecutor("sherlock.local", false);
			}
			sherlock.initialize();
		}
		else if(args[0].equals("pre")) {
			if(args[1].equals("B")) {
				sherlock = new SherlockExecutor("sherlock.local", true);
			} else {
				sherlock = new SherlockExecutor("sherlock.local", false);
			}
			sherlock.pre();
		}
		// HR 컨설팅 전용
		else if(args[0].equals("conan")) {
			if(args[1].equals("B")) {
				sherlock = new SherlockExecutor("sherlock.local", true);
			} else {
				sherlock = new SherlockExecutor("sherlock.local", false);
			}
			sherlock.pre();
			//sherlock.parseExcel();
			sherlock.execute();
		}
		// 자기소개서를 연산한다
		else if(args[0].equals("exec")) {
			if(args[1].equals("B")) {
				sherlock = new SherlockExecutor("sherlock.local", true);
			} else {
				sherlock = new SherlockExecutor("sherlock.local", false);
			}
			sherlock.execute();
		}
		else {
			System.out.println("제대로 입력 하시오...");
		}
	}
}
