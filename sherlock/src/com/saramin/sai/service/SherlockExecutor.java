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
import com.saramin.sai.vo.ApplyVO;
import com.saramin.sai.vo.ConfigVO;
import com.saramin.sai.vo.HashedItdcVO;


/**
 * 자기소개서 표절 시스템
 * 
 * @author jinhoo.jang
 * @since 2019.03.28
 * @team AI Part
 */
public class SherlockExecutor {
	private CommonUtil COMMON;
	private SherlockModule SHERLOCK_MODULE;
	private ConfigVO CONFIG;
	private Logger LOGGER;
	
	private HashMap<String, HashMap<String, List<String>>> MEM_TIT_HASHED_LIST;
	
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
			SHERLOCK_MODULE = new SherlockModule(CONFIG);
			
			MEM_TIT_HASHED_LIST = new HashMap<String, HashMap<String, List<String>>> ();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[ERROR] parsing error");
			System.exit(1);			
		}
	}
	
	
	/**
	 * 자기소개서 원문을 읽어서, 해시화 시키고 데일리 폴더에 넣는다
	 * 데일리 폴더의 파일을 최종적으로 병합
	 */
	public void pre() {
		// 먼저 apply 데이터를 읽은 후, res_idx와 mem_idx를 세팅한다
		System.out.println("BEGIN: get mem res data");
		HashMap<String, String> RES_MEM_MAP = getApplyKeyValue(2,3);
		System.out.println("END: get mem res data");
		
		// 제목 사전을 읽는다
		LOGGER.info("BEGIN: read title dictionary");
		HashMap<String, String> titDic = SHERLOCK_MODULE.readTitDictionary();
		LOGGER.info("END: " + titDic.size() + " read title dictionary");
		
		// 자기소개서를 해시화 시킨다
		System.out.println("BEGIN: make hashed");
		List<String> list = null;
		HashMap<String, HashMap<String, String>> result = null;
		
		BufferedReader br = null;
		File[] raws = null;
		HashedItdcVO vo = null;
				
		raws = new File(CONFIG.getDataPath() + "raw-data/introdoc/inc/").listFiles();
		boolean flag = false;
		StringBuffer contents = new StringBuffer();
		
		List<HashedItdcVO> resultList = new ArrayList<HashedItdcVO> ();
		
		for(File doc : raws) {
			resultList = new ArrayList<HashedItdcVO> ();
			
			try {
				list = new ArrayList<String>();
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				System.out.println("read : " + doc.getName());
				
				while ((line = br.readLine()) != null) {
					if(line.trim().length() > 0) {
						// res_idx가 있을 경우, 먼저 PK값을 세팅
						if(line.indexOf("<__res_idx__>") > -1) {
							contents = new StringBuffer();
							flag = false;
							
							String res = line.substring("<__res_idx__>".length(), line.length());
							System.out.println(res);
							if(RES_MEM_MAP.containsKey(res)) {
								System.out.println(res);
								vo = new HashedItdcVO ();
								flag = true;
								
								vo.setMemIdx(RES_MEM_MAP.get(res));
							}
						} 
						// 제목일 경우
						else if(line.indexOf("<__title__>") > -1 && flag) {
							vo.setTitle(line.substring("<__title__>".length(), line.length()).trim());							
						}
						// 컨텐츠내용
						else if(line.indexOf("<__contents__>") > -1) {
							contents.append(line.substring("<__contents__>".length(), line.length()).trim());			
						}
						// 시퀀스, 마지막 라인 값을 세팅
						else if(line.indexOf("<__seq__>") > -1 && flag) {
							vo.setSeq(line.substring("<__seq__>".length(), line.length()).trim());
							vo.setContents(contents.toString());
							vo.setLength(contents.length());
							
							// vo값을 재조정
							resultList.add(SHERLOCK_MODULE.changeVO(vo, titDic));					
						}
						else if(flag && line.indexOf("<__") == -1){
							contents.append(line);
						}
					}
					
				}
				
				br.close();
				
				// FGF 파일로 생성
				chkError(SHERLOCK_MODULE.makePreFGF(resultList, doc.getName()), "makePreFGF");		
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
		
		System.out.println("END: make hashed");
	}
	
	
	/**
	 * 연산을 수행하는 스크립터
	 */
	public void execute() {
		// 먼저 apply 데이터를 읽은 후, res_idx와 mem_idx를 세팅한다
		System.out.println("BEGIN: get mem res data");
		HashMap<String, HashMap<String, ApplyVO>> MEM_APPLY_MAP = getApplyDataByMem();
		System.out.println("END: get mem res data");
		
		System.out.println("BEGIN: set dictionary");
		//HashMap<String, String> titMap = setTitleDic();
		System.out.println("END: set dictionary");
		
		// 타이틀, 해시, 이력서 번호의 맵으로 세팅한다
		long startTime = System.currentTimeMillis();
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
			}
			
			if(MEM_APPLY_MAP.containsKey(memIdx)) {
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
		LOGGER.info("getHashedData elapsed " + (endTime-startTime) + "(ms)");
		
		
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
