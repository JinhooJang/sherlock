package sai.galaxyframework.sherlock.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sai.galaxyframework.sherlock.module.SherlockModule;
import sai.galaxyframework.sherlock.util.CommonUtil;
import sai.galaxyframework.sherlock.util.Logger;
import sai.galaxyframework.sherlock.vo.ApplyVO;
import sai.galaxyframework.sherlock.vo.ConfigVO;
import sai.galaxyframework.sherlock.vo.HashedItdcVO;


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
	private boolean isBulk;
	
	private HashMap<String, HashMap<String, List<String>>> MEM_TIT_HASHED_LIST;
	
	final String NEWLINE = System.getProperty("line.separator");
	
	
	/**
	 * 생성자
	 */
	public SherlockExecutor(String propNm, boolean isBulk) {
		COMMON = new CommonUtil();
		CONFIG = new ConfigVO();
		this.isBulk = isBulk;
		
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
	 * 초기화 단계, FGF를 읽어서 hashed-itdc를 생성한다
	 */
	public void initialize() {
		// apply_idx를 기반으로 res_idx,mem_idx의 hashmap 생성
		LOGGER.info("BEGIN: get apply data...");
		HashMap<String, String> RES_MEM_MAP = getKeyValue("res-mem", 0, 1, isBulk);
		LOGGER.info("END: get apply data...");
		
		// 전체 자기소개서 데이터를 가져온다 (apply 에 해당되는 것만)
		LOGGER.info("BEGIN: read intro doc & morph analyze...");
		List<HashedItdcVO> RESULT_LIST = SHERLOCK_MODULE.readIntroDoc(RES_MEM_MAP, isBulk);
		LOGGER.info("END: " + RESULT_LIST.size() + " read intro doc & morph analyze...");
		
		// hashed-itdc 파일들을 생성한다
		LOGGER.info("BEGIN: make hashed itdc...");
		chkError(SHERLOCK_MODULE.makeHashedItdcCSV(RESULT_LIST, isBulk), "MAKE HASHED-ITDC");
		LOGGER.info("END: make hashed itdc...");
	}
	
	
	/**
	 * 분석을 위한 2번째 단계, hashed-itdc 파일을 읽어서 최종 분석을 위한 inv-data 생성 
	 */
	public void pre() {
		// hashed-itdc를 읽어서, result list를 생성한다
		LOGGER.info("BEGIN: get hashed itdc...");
		List<HashMap<String, String>> HASHED_LIST = SHERLOCK_MODULE.getHashedItdc(isBulk);
		LOGGER.info("END: get hashed itdc...");
		
		// inverse table을 생성한다
		LOGGER.info("BEGIN: hashed-itdc to inv data...");
		HashMap<String, HashMap<String, String>> invData = SHERLOCK_MODULE.makeInvData(isBulk);
		LOGGER.info("END: hashed-itdc to inv data...");
		
		// inverse data csv를 생성한다
		LOGGER.info("BEGIN: make inv-data csv...");
		chkError(SHERLOCK_MODULE.makeInvDataCSV(invData, isBulk), "MAKE COPY-RATE");
		LOGGER.info("END: make inv-data csv...");
	}
	
	
	/**
	 * inv-data를 읽어서, 전체 표절율을 구한다.
	 * 
	 * @param isBulk
	 */
	public void execute() {
		
		// apply_idx를 기반으로 res_idx,mem_idx의 hashmap 생성 및 apply list 생성
		LOGGER.info("BEGIN: get apply data...");
		HashMap<String, String> RES_MEM_MAP2 = getKeyValue("res-mem", 0, 1, true);
		LOGGER.info("END: get apply data...");		
		
		LOGGER.info("BEGIN: get mem res data");
		HashMap<String, String> RES_MEM_MAP = getKeyValue("apply", 2, 3, isBulk);
		LOGGER.info("END: get mem res data");
		
		// inv-data csv를 읽은 후, res_idx별 표절율을 생성
		// (동일 mem_idx일 경우, 해당 res_idx 계산 제외)
		LOGGER.info("BEGIN: set inverse data");
		HashMap<String, List<String>> INV_MAP = SHERLOCK_MODULE.setInvData(isBulk);
		LOGGER.info("END: " + INV_MAP.size() + " set inverse data");
		
		// 추가로 appy_idx에 존재하는 res-idx를 기반으로, res-idx->seq->sentence를 맵에 넣는다.
		LOGGER.info("BEGIN: set sentence data");
		HashMap<String, HashMap<String, List<String>>> SENTENCE_MAP = SHERLOCK_MODULE.setSentenceMap(RES_MEM_MAP, isBulk);
		LOGGER.info("END: " + SENTENCE_MAP.size() + " set sentence data");
				
		// 시퀀스별 표절율을 구한 후, 가장 높은 max 표절율을 해당 값에 매칭한 후, 매칭되는 이력서 번호를 넣는다
		HashMap<String, String> lastMap = new HashMap<String, String> ();
		for(String resIdx : SENTENCE_MAP.keySet()) {
			HashMap<String, List<String>> seqMap = SENTENCE_MAP.get(resIdx);
			
			for(String seq : seqMap.keySet()) {
				HashMap<String, Integer> scoreMap = new HashMap<String, Integer> (); 
				List<String> sentenceList = seqMap.get(seq);
				
				for(String sentence : sentenceList) {
					List<String> targetList = INV_MAP.get(sentence);
					
					for(String target : targetList) {
						int score = 1;
						if(scoreMap.containsKey(target)) {
							score += scoreMap.get(target);
						}
						
						scoreMap.put(target, score);
					}
				}
				
				// score map에서 가장 높은 값
				Map<String, Integer> resultMap = scoreMap.entrySet().stream()
		                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
		                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
				
				int maxCnt = 0;
				String maxResIdx = "";
				for(String target : resultMap.keySet()) {
					String targetRes = target.substring(0, target.indexOf("_"));
					
					if(RES_MEM_MAP2.containsKey(targetRes) && RES_MEM_MAP2.containsKey(resIdx) &&
							RES_MEM_MAP2.get(targetRes).equals(RES_MEM_MAP2.get(resIdx))) {
						continue;
					}
					
					if(!RES_MEM_MAP2.containsKey(targetRes))
						continue;
					
					maxCnt = resultMap.get(target);
					maxResIdx = target;
					
					break;
				}
				
				lastMap.put(resIdx + "_" + seq, maxResIdx + "," + (double)maxCnt/sentenceList.size());				
			} // seq
		} // res
		
		// 항목별 데이터를 합친다, res_idx, seq, 0.2
		HashMap<String, HashMap<String, Double>> resultMap = new HashMap<String, HashMap<String, Double>> ();
		for(String key : lastMap.keySet()) {
			String[] value = lastMap.get(key).split(",");
			String[] pk = key.split("_");
			
			HashMap<String, Double> scoreMap = null;
			if(resultMap.containsKey(pk[0])) {
				scoreMap = resultMap.get(pk[0]);
			} else {
				scoreMap = new HashMap<String, Double> ();
			}
			
			// 값이 존재하지 않는다면
			if(value[0].length() == 0) {
				scoreMap.put(pk[1], 0.0);
			} else {
				// 값이 있다면, res_idx를 넣는다
				scoreMap.put(value[0], Double.parseDouble(value[1]));
			}
			resultMap.put(pk[0], scoreMap);
		}
		
		// 최종적으로 표절률을 csv로 생성한다
		LOGGER.info("BEGIN: make copy rate...");
		boolean success = SHERLOCK_MODULE.makeCopyRateCSV(resultMap, true);
		LOGGER.info("END: make copy rate...");		

		// apply list를 읽은 후, 해당되는 res_idx와 표절율을 가지고 온 후, copyrate_bulk.csv 생성
	}
	
	
	/**
	 * 연산을 수행하는 스크립터
	 */
	public void executeAsis() {
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
	public HashMap<String, String> getKeyValue(String folderNm, int key, int value, boolean isBulk) {
		HashMap<String, String> rtnMap = new HashMap<String, String> (); 
		
		BufferedReader br = null;
		File[] applys = null;
		if(isBulk) {
			applys = new File(CONFIG.getDataPath() + "raw-data/" + folderNm + "/bulk/").listFiles();
		} else {
			applys = new File(CONFIG.getDataPath() + "raw-data/" + folderNm + "/inc/").listFiles();
		}
		
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
						rtnMap.put(temp[key], temp[value]);
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

