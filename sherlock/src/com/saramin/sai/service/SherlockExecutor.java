package com.saramin.sai.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.saramin.sai.module.SherlockModule;
import com.saramin.sai.util.CommonUtil;
import com.saramin.sai.util.Logger;
import com.saramin.sai.vo.ConfigVO;


/**
 * 자기소개서 표절 시스템
 * 
 * @author jinhoo.jang
 * @since 2018.12.24
 * @team AI Part
 */
public class SherlockExecutor {
	private CommonUtil COMMON;
	private SherlockModule SHRELOCK_MODULE;
	private ConfigVO CONFIG;
	private Logger LOGGER;
	
	final String NEWLINE = System.getProperty("line.separator");
	
	
	/**
	 * 생성자
	 */
	public SherlockExecutor(String propNm) {
		COMMON = new CommonUtil();
		CONFIG = new ConfigVO();		
		
		try {
			// xml을 읽는다
			HashMap<String, String> map = COMMON.xmlParserProperties(propNm);
			
			// 데이터 파싱
			CONFIG.setDataPath(map.get("config.dataPath"));;
			CONFIG.setLogPath(map.get("config.logPath"));;
			CONFIG.setDebug(Boolean.valueOf(map.get("config.debugMode")));
			
			
			LOGGER = new Logger(CONFIG.isDebug(), "SHERLOCK", CONFIG.getLogPath());
			SHRELOCK_MODULE = new SherlockModule(CONFIG);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[ERROR] parsing error");
			System.exit(1);			
		}
	}
	
	
	/**
	 * PreData를 위한 스크립터
	 */
	public void pre(boolean isBulk) {
		List<String> list = null;
		HashMap<String, HashMap<String, String>> result = null;
		
		BufferedReader br = null;
		File[] raws = null;
		// 자기소개서의 경로
		String mode = "raw-data/intro-doc/inc/";
		
		if(isBulk) {
			mode = "raw-data/intro-doc/bulk/";
		}
		raws = new File(CONFIG.getDataPath() + mode).listFiles();
		
		for(File doc : raws) {
			try {
				list = new ArrayList<String>();
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				
				while ((line = br.readLine()) != null) {
					if(line.trim().length() > 0)
						list.add(line);
				}

				br.close();
				
				// 텍스트를 자소서 구조로 map에 저장
				result = COMMON.parseDocsForSherlock(list);
				
				// 전체 자소서를 해시화 한 후, list로 저장 
				list = SHRELOCK_MODULE.changeHashed(result);
				
				// csv 파일로 생성한다
				chkError(SHRELOCK_MODULE.makePreCSV(list, doc.getName()), "makePreCSV");			
				LOGGER.info(doc.getName() + " made pre-data...");
				
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("pre : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		}
	}
	
	
	/**
	 * 연산을 수행하는 스크립터
	 */
	public void bulk() {
		// 타이틀, 해시, 이력서 번호의 맵으로 세팅한다
		long startTime = System.currentTimeMillis();
		HashMap<String, HashMap<String, List<String>>> hashedMap = getHashedData();
		long endTime = System.currentTimeMillis();
		LOGGER.info("getHashedData elapsed " + (endTime-startTime) + "(ms)");
		
		// debug
		for(String title : hashedMap.keySet()) {
			HashMap<String, List<String>> map = hashedMap.get(title);
			
			for(String sentence : map.keySet()) {
				if(map.get(sentence).size() > 2) {
					System.out.println("title : " + title);
					System.out.println(sentence + " " + map.get(sentence));
				}
			}
		}
		
		// 신규 문서를 읽은 후, 형태소 분석 -> 해시 수행 후 유사 이력서 번호 리스트를 가져온다
		BufferedReader br = null;
		File[] docs = new File(CONFIG.getDataPath() + "pre-data/match/plagiarize2/").listFiles();
		
		try {
			for(File doc : docs) {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				int total = 0;
				
				while ((line = br.readLine()) != null) {
					total = 0;
					
					if(line.trim().length() > 0) {
						String[] temp = line.split(",");
						String[] items = temp[1].split("\\#");
						HashMap<String, Integer> resCnt = new HashMap<String, Integer> ();
						
						// 항목별
						for(String item : items) {
							String[] value = item.split("\\^");
							
							
						}
						
						// 
						
					}
				}

				br.close();
				LOGGER.info(doc.getName() + " get hashed data...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("getHashedData() : " + e.getMessage());
			hashedMap = null;
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		
		// 최종적으로 원본문장 개수 -> 유사 이력서 리스트의 카운트를 비교하여 제일 유사한 이력서를 sort한다		

	}
	
	
	/**
	 * 해시값을 가져온다
	 *  
	 * @return
	 */
	public HashMap<String, HashMap<String, List<String>>> getHashedData() {
		
		BufferedReader br = null;
		HashMap<String, HashMap<String, List<String>>> hashedMap = null;
		File[] docs = new File(CONFIG.getDataPath() + "pre-data/galaxy/sherlock/plagiarize/").listFiles();
		
		// 이력서-제목별 유사 이력서 찾기
		HashMap<String, HashMap<String, List<String>>> resTitle 
					= new HashMap<String, HashMap<String, List<String>>> (); 
		
		try {
			hashedMap = new HashMap<String, HashMap<String, List<String>>> ();
			
			for(File doc : docs) {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				
				String line = null;
				
				while ((line = br.readLine()) != null) {
					if(line.trim().length() > 0) {
						SHRELOCK_MODULE.parseHashData(line, hashedMap);
						
						// sherlock module
						for(String title : hashedMap.keySet()) {
							// 제목이 유사한 맵을 찾는다
							HashMap<String, List<String>> map = hashedMap.get(title);
							
							// 맵별, 문장들로 루프 수행 
							for(String sentence : map.keySet()) {
								if(map.get(sentence).size() > 1) {
									System.out.println("title : " + title);
									System.out.println(sentence + " " + map.get(sentence));
									Thread.sleep(100);
									
									/*for(int i = 0; i < map.get(sentence).size(); i++) {
										if(resTitle.containsKey(key))
									}*/
									
								}
							}
														
						}						
					}
				}

				br.close();
				LOGGER.info(doc.getName() + " get hashed data...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("getHashedData() : " + e.getMessage());
			hashedMap = null;
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return hashedMap;
	}
	
		
	/**
	 * 분석해야될 자기소개서를 해시값으로 우선 변경한다
	 * 
	 * @param isBulk
	 * @return
	 */
	public boolean chngHashAllDocs(boolean isBulk) {
		// 자기소개서의 경로
		File[] docs = null;
		BufferedReader br = null;
		List<String> list = null;
		HashMap<String, HashMap<String, String>> result = null;
		
		if(isBulk) {
			docs = new File(CONFIG.getDataPath() + "raw-data/intro-doc/bulk/").listFiles();
		} else {			
			docs = new File(CONFIG.getDataPath() + "raw-data/intro-doc/inc/").listFiles();
		}		
		
		try {
			// 한 파일씩 분석한다. 한 파일은 약 10만개의 자기소개서 존재
			// RES-IDX, TITLE, HASH 형태
			// (String)Hash -> (String)title, List<res_idx>
			for(File doc : docs) {
				list = new ArrayList<String>();
				
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				
				while ((line = br.readLine()) != null) {
					if(line.trim().length() > 0) {
						list.add(line);
					}
				}

				br.close();
				
				// 파일에 있는 자소서 구조를 map에 저장한다
				result = COMMON.parseDocsForSherlock(list);
				
				// 자소서 구조를 해시화
				list = SHRELOCK_MODULE.changeHashed(result);
				LOGGER.info(doc.getName() + " made pre-data...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("chngHashAllDocs() : " + e.getMessage());
			return false;
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return true;		
	}
	
	
	/**
	 * 에러를 체크하여, 에러 발생 시 종료
	 * 
	 * @param flag
	 * @param service
	 */
	public void chkError(boolean flag, String service) {
		if(!flag) {
			System.out.println(service + " error!!");
			System.out.println("program terminated.");
			System.exit(1);
		}
	}
}
