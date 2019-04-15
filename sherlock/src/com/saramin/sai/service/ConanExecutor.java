package com.saramin.sai.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.saramin.sai.module.ConanDeepModule;
import com.saramin.sai.module.ConanModule;
import com.saramin.sai.util.CommonUtil;
import com.saramin.sai.util.Logger;
import com.saramin.sai.vo.ApplyVO;
import com.saramin.sai.vo.ConfigVO;
import com.saramin.sai.vo.HashedItdcVO;
import com.saramin.sai.vo.IntroDocVO;


/**
 * 자기소개서 분석 시스템(HR 컨설팅용)
 * 
 * @author jinhoo.jang
 * @since 2019.04.12
 * @team AI Part
 */
public class ConanExecutor {
	private CommonUtil COMMON;
	private ConanModule CONAN_MODULE;
	private ConanDeepModule CONAN_DEEP_MODULE;
	private ConfigVO CONFIG;
	private Logger LOGGER;
	
	private HashMap<String, HashMap<String, List<String>>> MEM_TIT_HASHED_LIST;
	
	final String NEWLINE = System.getProperty("line.separator");
	
	
	/**
	 * 생성자
	 */
	public ConanExecutor(String propNm) {
		COMMON = new CommonUtil();
		CONFIG = new ConfigVO();		
		
		try {
			// xml을 읽는다
			HashMap<String, String> map = COMMON.xmlParserProperties(propNm);
			
			// 데이터 파싱
			CONFIG.setDataPath(map.get("config.dataPath"));;
			CONFIG.setLogPath(map.get("config.logPath"));;
			CONFIG.setModelPath(map.get("config.modelPath"));
			CONFIG.setDebug(Boolean.valueOf(map.get("config.debugMode")));
			
			
			LOGGER = new Logger(CONFIG.isDebug(), "SHERLOCK", CONFIG.getLogPath());
			CONAN_MODULE = new ConanModule(CONFIG);
			CONAN_DEEP_MODULE = new ConanDeepModule(CONFIG);
			
			MEM_TIT_HASHED_LIST = new HashMap<String, HashMap<String, List<String>>> ();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[ERROR] parsing error");
			System.exit(1);			
		}
	}
	
	
	/**
	 * 엑셀로 된, 파일을 읽어서 FGF 포맷으로 변경한다
	 */
	public void pre() {
		
		LOGGER.info("BEGIN: start conan executor");
		
		/*
		LOGGER.info("BEGIN: EXCEL to FGF"); 
		String name = "토목_신입";
		HashMap<String, IntroDocVO> EXCEL_MAP = CONAN_MODULE.readResultExcel(name);
		CONAN_MODULE.setContentExcel(name, EXCEL_MAP);
		CONAN_MODULE.makeFGF(EXCEL_MAP, name);
		LOGGER.info("END: EXCEL to FGF");
		*/
		
		LOGGER.info("BEGIN: make job dictionary");
		// job 별 직업 사전 생성 (label = 1, ques_clss = 0)
		// CONAN_MODULE.makeJobDic();
		LOGGER.info("END: make job dictionary");
		
		LOGGER.info("BEGIN: verify with job dictionary");
		// 직업사전으로 직무역량 검증, pass, fail
		/*HashMap<String, String> TASK_MAP = CONAN_MODULE.readTaskDic(); 
		CONAN_MODULE.verifyJob(TASK_MAP);*/
		LOGGER.info("END: verify with job dictionary");
		
		LOGGER.info("BEGIN: calculate tf");
		String[] quesClssArr = {"1","2","3","4"};
		String[] jobArr = {"건축","기계","사무","전기","토목","화학"};
		
		// 직업(job) + 질문별(ques_clss, 총 4개) 별 tf 수행
		// 직업별 tf csv(키워드, 카운트, 등장비율) 생성
		/*for(String quesClss : quesClssArr) {
			for(String job : jobArr) {
				List<String> tfList = CONAN_MODULE.tf(quesClss, job);
				int total = CONAN_MODULE.makeTfCsv(tfList, quesClss, job);
			}
		}*/
		
		LOGGER.info("END: calculate tf");
		
		// 직업별 IDF를 구하여, 최종 CSV를 생성해낸다, 나중에 작업
		/*LOGGER.info("BEGIN: calculate idf");
		LOGGER.info("ENd: calculate idf");*/
		
		LOGGER.info("BEGIN: make training & test data");
		// 최종 CSV를 기반으로 자소서별 Training 데이터와 검증용 데이터를 생성한다
		//HashMap<String, List<String>> termMap = new HashMap<String, List<String>> ();  
		for(String quesClss : quesClssArr) {
			for(String job : jobArr) {
				//termMap.put(job + "-" + quesClss, CONAN_MODULE.readTermList(job, quesClss));
				List<String> termList = CONAN_MODULE.readTermList(job, quesClss);
				
				System.out.println(quesClss + "=>" + termList.size());
				// make training data
				CONAN_MODULE.makeTrainNTestCSV(termList, job, quesClss);
				// make test data
			}
		}
		LOGGER.info("END: make training & test data");
		
		LOGGER.info("END: start conan executor");
	}
	
	
	public void parseExcel() {
		LOGGER.info("BEGIN: EXCEL to FGF");
		String[] jobs = {"사무","ICT","건축","기계","전기","토목","화학"};
		String[] exps = {"신입","경력"};
		
		for(String job : jobs) {
			for(String exp : exps ) {
				HashMap<String, IntroDocVO> EXCEL_MAP = CONAN_MODULE.readResultExcel(job, exp);
				
				if(EXCEL_MAP != null && EXCEL_MAP.size() > 0) {
					CONAN_MODULE.makeFGF(EXCEL_MAP, job);
					LOGGER.info(job + " " + exp + " complete.");
				}
			}
		}
		
		LOGGER.info("END: EXCEL to FGF");
	}
	
	
	/**
	 * 연산을 수행하는 스크립터
	 */
	public void execute() {
		
		//LOGGER.info("BEGIN: make test data");
		// 자기소개서 데이터를 테스트용 데이터로 변환한다
		//String[] quesClssArr = {"1","2","3","4"};
		String[] quesClssArr = {"1","2","3","4"};
		String[] jobArr = {"건축"};
		//String[] jobArr = {"건축","기계","사무","전기","토목","화학"};
		
		HashMap<String, HashMap<String, Object>> scoreMap = new HashMap<String, HashMap<String, Object>> ();
		// blind map을 읽는다
		HashMap<String, String> blindMap = CONAN_MODULE.blindMap();
		
		for(String job : jobArr) {
			for(String quesClss : quesClssArr) {
				List<String> termList = CONAN_MODULE.readTermList(job, quesClss);
				System.out.println(quesClss + "=>" + termList.size());
				
				// make training data
				CONAN_MODULE.makeTestCSV(termList, job, quesClss, blindMap, scoreMap);
			}
		}
		LOGGER.info("END: make test data");
		
		LOGGER.info("BEGIN: call deep learning");
		for(String job : jobArr) {
			for(String quesClss : quesClssArr) {
				CONAN_DEEP_MODULE.analysis(job, quesClss, scoreMap);
			}
		}
		LOGGER.info("END: call deep learning");
		
		/*for(String pk : scoreMap.keySet()) {
			HashMap<String, Object> map = scoreMap.get(pk);
			for(String key : map.keySet()) {
				System.out.println(key);
			}
		}*/
		
		LOGGER.info("BEGIN: print score map");
		CONAN_MODULE.makeScoreCSV(scoreMap);
		LOGGER.info("END: print score map");
		
		System.out.println("BEGIN: set dictionary");
		//HashMap<String, String> titMap = setTitleDic();
		System.out.println("END: set dictionary");
		
		// 타이틀, 해시, 이력서 번호의 맵으로 세팅한다
		/*long startTime = System.currentTimeMillis();
		System.out.println("BEGIN: TIT_HASHED_MEM_LIST");
		HashMap<String, HashMap<String, List<String>>> TIT_HASHED_MEM_LIST = getHashedData();
		System.out.println("END: " + TIT_HASHED_MEM_LIST.size() + " TIT_HASHED_MEM_LIST");
		
		HashMap<String, List<String>> resultMap = new HashMap<String, List<String>> ();
		int cnt = 0;
		
		for(String memIdx : MEM_TIT_HASHED_LIST.keySet()) {
			cnt = 0;
			resultMap = new HashMap<String, List<String>> ();
			List<String> resultList = new ArrayList<String> ();
			HashMap<String, List<String>> dailyMap = MEM_TIT_HASHED_LIST.get(memIdx);
						
			for(String title : dailyMap.keySet()) {
				System.out.println("title : " + title);				
				HashMap<String, List<String>> targetHashedMap = TIT_HASHED_MEM_LIST.get(title);
				System.out.println("target=>" + targetHashedMap);
				
				List<String> hashedList = dailyMap.get(title);
				System.out.println("hashedList=>" + hashedList);
				
				for(String hashed : hashedList) {
					cnt++;
					
					for(String mem : targetHashedMap.get(hashed)) {
						if(mem.equals(memIdx)) {
							continue;
						}
						resultList.add(mem);
					}
				}
			}
			
			// 최종 결과
			HashMap<String, Integer> cntMap = new HashMap<String, Integer> ();
			for(String mem : resultList) {
				int score = 1;
				if(cntMap.containsKey(mem)) {
					score += cntMap.get(mem);
				}
				cntMap.put(mem, score);
			}*/
			
			/*if(MEM_APPLY_MAP.containsKey(memIdx)) {
				// 표절 리스트
				for(String targetMem : cntMap.keySet()) {
					HashMap<String, ApplyVO> recApplyMap = MEM_APPLY_MAP.get(memIdx);
					for(String rec: recApplyMap.keySet()) {
						ApplyVO applyVO = recApplyMap.get(rec);
						
						System.out.println(rec + "," + applyVO.getResIdx() + "," + targetMem + "," + String.format("%.2f", (double)cntMap.get(targetMem)/cnt));
					}						
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		LOGGER.info("getHashedData elapsed " + (endTime-startTime) + "(ms)");*/		
	}
	
	
	/**
	 * Mem_idx와 res_idx를 매핑한다
	 * 
	 * @return
	 */
	public HashMap<String, String> getApplyKeyValue(int key, int value) {
		HashMap<String, String> rtnMap = new HashMap<String, String> (); 
		
		BufferedReader br = null;
		File[] applys = null;		
		applys = new File(CONFIG.getDataPath() + "raw-data/apply/inc/").listFiles();
		
		int cnt = 0;
		
		for(File apply : applys) {
			try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(apply.getAbsolutePath()), "UTF8"));
				
				String line = null;				
				while ((line = br.readLine()) != null) {
					cnt++;
					
					if(line.trim().length() > 0 && cnt > 1) {
						String[] temp = line.split(",");
						rtnMap.put(temp[2], temp[3]);
					}
				}

				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("getMemResData : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		}
		
		return rtnMap;
	}
	
	
	/**
	 * Mem_idx와 res_idx를 매핑한다
	 * 
	 * @return
	 */
	public HashMap<String, HashMap<String, ApplyVO>> getApplyDataByMem() {
		HashMap<String, HashMap<String, ApplyVO>> rtnMap 
						= new HashMap<String, HashMap<String, ApplyVO>> (); 
		
		BufferedReader br = null;
		File[] applys = null;
		
		HashMap<String, ApplyVO> recApplyVO = null;
		ApplyVO vo = null;
		int cnt = 0;
		
		applys = new File(CONFIG.getDataPath() + "raw-data/apply/inc/").listFiles();
		
		for(File apply : applys) {
			try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(apply.getAbsolutePath()), "UTF8"));
				
				String line = null;				
				while ((line = br.readLine()) != null) {
					cnt++;
					recApplyVO = new HashMap<String, ApplyVO> ();
					
					if(line.trim().length() > 0 && cnt > 1) {
						String[] value = line.split(",");
						vo = new ApplyVO();
						
						vo.setApplyIdx(value[0]);
						vo.setRecIdx(value[1]);
						vo.setResIdx(value[2]);
						vo.setMemIdx(value[3]);
						
						recApplyVO.put(value[1], vo);
						rtnMap.put(value[3], recApplyVO);
					}
				}

				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("getMemResData : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		}
		
		return rtnMap;
	}
	
	
	/**
	 * 해시값을 가져온다
	 *  
	 * @return
	 */
	public HashMap<String, HashMap<String, List<String>>> getHashedData() {
		
		BufferedReader br = null;
		HashMap<String, HashMap<String, List<String>>> TIT_HASHED_MEM_LIST = 
				new HashMap<String, HashMap<String, List<String>>> ();
		
		
		List<String> filePathList = new ArrayList<String> ();
		// all 데이터
		File[] docs = new File(CONFIG.getDataPath() + "pre-data/galaxy/sherlock/hashed-itdc/all/").listFiles();		
		for(File doc : docs) {
			filePathList.add(doc.getAbsolutePath());
		}
		// daily7 데이터
		docs = new File(CONFIG.getDataPath() + "pre-data/galaxy/sherlock/hashed-itdc/daily/").listFiles();
		for(File doc : docs) {
			filePathList.add(doc.getAbsolutePath());
		}
		
		boolean isDaily = false;
		
		try {
			for(String filePath : filePathList) {
				isDaily = false;
				if(filePath.indexOf("daily") > -1)
					isDaily = true;
				
				
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(filePath), "UTF8"));
				
				String line = null;
				String pk = "";
				StringBuffer contents = new StringBuffer();
				HashedItdcVO vo = new HashedItdcVO();
				
				while ((line = br.readLine()) != null) {
					
					// pk가 처음, hashed가 마지막
					if(line.trim().length() > 0) {
						if(line.indexOf("<__pk__>") > -1) {
							vo.setPk(line.substring("<__pk__>".length(), line.length()));
						} else if(line.indexOf("<__mem_idx__>") > -1) {
							vo.setMemIdx(line.substring("<__mem_idx__>".length(), line.length()));
						} else if(line.indexOf("<__title__>") > -1) {
							//vo.setTitle(line.substring("<__title__>".length(), line.length()));
							vo.setTitle("etc");
						} else if(line.indexOf("<__contents__>") > -1) {
							vo.setContents(line.substring("<__contents__>".length(), line.length()));
						} else if(line.indexOf("<__length__>") > -1) {
							vo.setLength(Integer.parseInt(line.substring("<__length__>".length(), line.length())));
						} 
						// 마지막
						else if(line.indexOf("<__hashed__>") > -1) {
							vo.setHashed(line.substring("<__hashed__>".length(), line.length()));
							
							// 해시값 처리
							// 제목이 있을 경우
							HashMap<String, List<String>> HASHED_MEM_LIST = null;
							if(TIT_HASHED_MEM_LIST.containsKey(vo.getTitle())) {
								HASHED_MEM_LIST = TIT_HASHED_MEM_LIST.get(vo.getTitle());
							} else {
								HASHED_MEM_LIST = new HashMap<String, List<String>> ();								
							}
							
							String[] hasheds = vo.getHashed().split(",");
							
							if(hasheds != null && hasheds.length > 0) {
								List<String> list = null;
								
								for(String hashed : hasheds) {
									// 
									if(HASHED_MEM_LIST.containsKey(hashed)) {
										list = HASHED_MEM_LIST.get(hashed);
									} else {
										list = new ArrayList<String> ();
									}
									
									list.add(vo.getMemIdx());
									HASHED_MEM_LIST.put(hashed, list);									
								}
								
								TIT_HASHED_MEM_LIST.put(vo.getTitle(), HASHED_MEM_LIST);
							}
							
							// daily일 경우 추가 세팅
							if(isDaily) {
								HashMap<String, List<String>> TIT_HASHED_LIST = null;							
								if(MEM_TIT_HASHED_LIST.containsKey(vo.getMemIdx())) {
									TIT_HASHED_LIST = MEM_TIT_HASHED_LIST.get(vo.getMemIdx());
								} else {
									TIT_HASHED_LIST = new HashMap<String, List<String>> ();
								}
								
								List<String> dailyHashedList = null;
								
								if(TIT_HASHED_LIST.containsKey(vo.getTitle())) {
									dailyHashedList = TIT_HASHED_LIST.get(vo.getTitle());
								} else {
									dailyHashedList = new ArrayList<String> ();
								}
								
								for(String hashed : hasheds) {
									dailyHashedList.add(hashed);
								}
								
								TIT_HASHED_LIST.put(vo.getTitle(), dailyHashedList);
								MEM_TIT_HASHED_LIST.put(vo.getMemIdx(), TIT_HASHED_LIST);
							}
						}
					}
				}

				br.close();
				LOGGER.info(filePath + " get hashed data...");				
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("getHashedData() : " + e.getMessage());			
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		for(String tit : TIT_HASHED_MEM_LIST.keySet()) {
			System.out.println(tit + "==>" + TIT_HASHED_MEM_LIST.get(tit));
		}
		
		return TIT_HASHED_MEM_LIST;
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
