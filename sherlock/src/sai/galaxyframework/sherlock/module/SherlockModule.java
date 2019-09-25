package sai.galaxyframework.sherlock.module;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import ai.sai.sinabro.api.DanbiAPI;
import sai.galaxyframework.sherlock.util.CommonUtil;
import sai.galaxyframework.sherlock.util.Logger;
import sai.galaxyframework.sherlock.vo.ConfigVO;
import sai.galaxyframework.sherlock.vo.HashedItdcVO;


/**
 * 자기소개서의 종합 솔루션을 수행하는 모듈
 * 
 * @author jinhoo.jang
 * @since 2019.04.22
 * @team AI Part
 */
public class SherlockModule {
	private ConfigVO CONFIG;
	private CommonUtil COMMON;
	private DanbiAPI DANBI;
	private Logger LOGGER;
	
	final String NEWLINE = System.getProperty("line.separator");
	
	public SherlockModule(ConfigVO CONFIG) {
		this.CONFIG = CONFIG;
		DANBI = new DanbiAPI(CONFIG.getDataPath());
		COMMON = new CommonUtil();
		
		LOGGER = new Logger(CONFIG.isDebug(), "SHERLOCK", CONFIG.getLogPath());
	}

	
	/**
	 * 자기소개서를 읽는다
	 * 
	 * @param RES_MEM_MAP
	 * @param isBulk
	 * @return
	 */
	public List<HashedItdcVO> readIntroDoc(HashMap<String, String> RES_MEM_MAP, boolean isBulk) {
		BufferedReader br = null;
		File[] raws = null;
		HashedItdcVO vo = null;
		int cnt = 0;
				
		if(isBulk) {
			raws = new File(CONFIG.getDataPath() + "raw-data/introdoc/bulk/").listFiles();
		} else {
			raws = new File(CONFIG.getDataPath() + "raw-data/introdoc/inc/").listFiles();
		}
		
		boolean flag = false;
		StringBuffer contents = new StringBuffer();		
		List<HashedItdcVO> resultList = new ArrayList<HashedItdcVO> ();
		
		for(File doc : raws) {
			
			try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				LOGGER.info(doc.getName() + " read...");
				
				while ((line = br.readLine()) != null) {
					if(line.trim().length() > 0) {
						// res_idx가 있을 경우, 먼저 PK값을 세팅
						if(line.indexOf("<__res_idx__>") > -1) {
							contents = new StringBuffer();
							flag = false;
							
							String res = line.substring("<__res_idx__>".length(), line.length());
							
							// apply에 세팅된 키가 일치할 경우
							if(RES_MEM_MAP.containsKey(res)) {
								vo = new HashedItdcVO ();
								flag = true;
								
								vo.setMemIdx(RES_MEM_MAP.get(res));
								vo.setResIdx(res);
							}
						} 
						// 제목일 경우, 컨텐츠에 포함시킨다
						else if(line.indexOf("<__title__>") > -1 && flag) {
							String temp = line.substring("<__title__>".length(), line.length()).trim();
							
							if(temp.length() > 10) {
								contents.append(line.substring("<__title__>".length(), line.length()).trim() + " ");
							}
						}
						// 컨텐츠내용
						else if(line.indexOf("<__contents__>") > -1) {
							contents.append(line.substring("<__contents__>".length(), line.length()).trim() + " ");			
						}
						// 시퀀스, 마지막 라인 값을 세팅
						else if(line.indexOf("<__seq__>") > -1 && flag) {
							vo.setSeq(line.substring("<__seq__>".length(), line.length()).trim());
							vo.setContents(contents.toString());
							vo.setLength(contents.length());
							
							// vo값을 재조정
							resultList.add(changeVO(vo));
							cnt++;
							
							if(cnt % 10000 == 0) {
								LOGGER.debug("EXECUTE COUNT[" + cnt + "]");
							}
						}
						else if(flag && line.indexOf("<__") == -1){
							contents.append(line);
						}
					}
					
				}
				
				br.close();								
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("readIntroDoc : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		}
		
		return resultList;
	}
	
	
	/**
	 * VO값을 변환한다
	 * 
	 * @param ori
	 * @return
	 */
	public HashedItdcVO changeVO(HashedItdcVO ori, HashMap<String, String> dicMap) {
		HashedItdcVO vo = new HashedItdcVO ();
		
		// pk를 생성 (mem_idx + seq) => 해시화
		try {
			vo.setPk(COMMON.toSha(ori.getMemIdx() + "_" + ori.getSeq()).substring(0, 10));
			vo.setMemIdx(ori.getMemIdx());
			
			List<String> tit = DANBI.extractNoun(ori.getTitle());
			StringBuffer sb = new StringBuffer();
			for(String token : tit) {
				if(sb.toString().length() > 0)
					sb.append(" ");
				sb.append(token);
			}
			
			vo.setTitle(sb.toString());		
			vo.setClssTitle(setClssTitle(sb.toString(), dicMap));
			vo.setContents(ori.getContents());
			vo.setLength(ori.getLength());
			
			// hashed
			String[] contents = ori.getContents().replaceAll("\n", ".").split("\\.");
			StringBuffer contentSB = new StringBuffer();
			
			for(String sentence : contents) {
				sb = new StringBuffer();
				List<String> tokens = DANBI.extractNoun(sentence);
				
				for(String token : tokens) {
					if(sb.length() > 0)
						sb.append(" ");
					
					sb.append(token);
				}
				
				if(sb.length() > 0) {
					if(contentSB.length() > 0)
						contentSB.append(",");
					contentSB.append(COMMON.toSha(sb.toString()).substring(0, 10));
				}
			}
			
			// 제목도 본문에 포함시킨다.
			if(vo.getTitle().length() > 0) {				
				vo.setHashed(COMMON.toSha(vo.getTitle()).substring(0, 10) + "," + contentSB.toString());
			} else {
				vo.setHashed(contentSB.toString());
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return vo;
	}
	
	
	/**
	 * VO값을 변환한다
	 * 
	 * @param ori
	 * @return
	 */
	public HashedItdcVO changeVO(HashedItdcVO ori) {
		HashedItdcVO vo = new HashedItdcVO ();
		
		try {
			// pk를 생성 (mem_idx + seq)로 변환
			vo.setPk(ori.getResIdx() + "_" + ori.getSeq());
			vo.setMemIdx(ori.getMemIdx());
			vo.setLength(ori.getLength());
			vo.setResIdx(ori.getResIdx());
			
			// 문장별로 나눈다
			String[] contents = ori.getContents().replaceAll("\n", ".").split("\\.");
			StringBuffer tempSB = new StringBuffer();
			StringBuffer contentSB = new StringBuffer();
			
			for(String sentence : contents) {
				tempSB = new StringBuffer();
				List<String> tokens = DANBI.extractNoun(sentence);
				
				// 문장별 필요 단어 요구수가 충족되어야 함.  (3개 이상)
				if(tokens != null && tokens.size() >= 3) {
					for(String token : tokens) {
						if(tempSB.length() > 0)
							tempSB.append(" ");
						
						tempSB.append(token);
					}
					
					// 형태소 분석이 완료되면, contentSB에 포함시킨다
					if(tempSB.length() > 0) {
						if(contentSB.length() > 0)
							contentSB.append(",");
						contentSB.append(COMMON.toSha(tempSB.toString()).substring(0, 10));
						//contentSB.append(tempSB.toString());
					}
				}
			}
			
			vo.setHashed(contentSB.toString());
			
			/*if(ori.getResIdx().equals("11793527")) {
				System.out.println();
			}*/
		} catch (Exception e) {
			LOGGER.error("changeVO error : " + e.getMessage());
			e.printStackTrace();
		}
		
		return vo;
	}
	
	
	/**
	 * 이력서당 해시데이터를 저장하는 CSV
	 * 
	 * @param result
	 * @param isBulk
	 * @return
	 */
	public boolean makeHashedItdcCSV(List<HashedItdcVO> result, boolean isBulk) {
		BufferedWriter bw;
		int cnt = 0;
		int seq = 0;
		
		String filePath = CONFIG.getDataPath() + "/pre-data/sherlock/hashed-itdc/daily/hashed-itdc";
		
		if(isBulk) {
			filePath = CONFIG.getDataPath() + "/pre-data/sherlock/hashed-itdc/all/hashed-itdc";
		}
		
		StringBuffer str = new StringBuffer();
		for(HashedItdcVO vo : result) {
			cnt++;
			
			if(vo.getHashed().trim().length() == 0)
				continue;
			
			str.append(vo.getPk() + ",");
			String[] hashed = vo.getHashed().split(",");
			
			for(int i = 0; i < hashed.length; i++) {
				if(i > 0)
					str.append("|");
				str.append(hashed[i]);
			}
			str.append(NEWLINE);
			
			// 10만 건당, 파일을 생성
			if(cnt % 100000 == 0 || cnt == result.size()-1) {
				try {	
					bw = new BufferedWriter(
							new OutputStreamWriter(
							new FileOutputStream(
								filePath + "-" + seq + ".csv", false),	// true to append 
								StandardCharsets.UTF_8));	// set encoding utf-8
					
					bw.write(str.toString());
					bw.close();
				} catch(Exception e){
					e.printStackTrace();
					return false;
				}		
				
				// 초기화
				str = new StringBuffer();
				seq++;
			}
		}
		
		return true;
	}
		
	
	/**
	 * 표절율을 계산하기 위해서, inverse table 데이터를 생성 
	 * 
	 * @param RESULT_LIST
	 * @param isBulk
	 * 
	 * @return
	 */
	public HashMap<String, HashMap<String, String>> makeInvData(boolean isBulk) {
		HashMap<String, HashMap<String, String>> sentenceMap = new HashMap<String, HashMap<String, String>> ();
		
		BufferedReader br = null;
		File[] raws = null;
				
		if(isBulk) {
			raws = new File(CONFIG.getDataPath() + "pre-data/sherlock/hashed-itdc/all/").listFiles();
		} else {
			raws = new File(CONFIG.getDataPath() + "pre-data/sherlock/hashed-itdc/daily/").listFiles();
		}
		
		for(File doc : raws) {
			try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(doc.getAbsolutePath()), "UTF8"));
				String line = null;
				LOGGER.info(doc.getName() + " read...");
				
				while ((line = br.readLine()) != null) {
					String[] temp = line.split(",");
					if(temp != null && temp.length == 2) {
						String[] sentences = temp[1].split("\\|");
						
						// 문장이 3개 이상일 경우만 인정
						if(sentences != null && sentences.length >= 3) {
							for(String sen : sentences) {
								HashMap<String, String> map = new HashMap<String, String> ();
								
								if(sentenceMap.containsKey(sen)) {
									map = sentenceMap.get(sen);
								}
								
								map.put(temp[0], "");
								sentenceMap.put(sen, map);
							}							
						}
					}
				}
				
				br.close();								
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("readIntroDoc : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		}
		
		LOGGER.debug("sentenceMap size=>" + sentenceMap.size());		
		return sentenceMap;
	}
	
	
	/**
	 * Make Inv Data CSV
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makeInvDataCSV(HashMap<String, HashMap<String, String>> invData, boolean isBulk) {
		BufferedWriter bw;
		int seq = 0;
		int cnt = 0;
		
		String filePath = CONFIG.getDataPath() + "/pre-data/sherlock/inv-data/daily/inv-data-";
		
		if(isBulk) {
			filePath = CONFIG.getDataPath() + "/pre-data/sherlock/inv-data/all/inv-data-";
		}
		
		StringBuffer str = new StringBuffer();
		for(String sentence : invData.keySet()) {
			cnt++;
			StringBuffer targetList = new StringBuffer();
				
			str.append(sentence + ",");
			HashMap<String, String> map = invData.get(sentence);
				
			for(String targetPk : map.keySet()) {
				if(targetList.length() > 0)
					targetList.append("|");					
				targetList.append(targetPk);					
			}
			str.append(targetList.toString() + NEWLINE);			
			
			if(cnt % 1000000 == 0 || cnt >= invData.size()) {
				try {	
					bw = new BufferedWriter(
							new OutputStreamWriter(
							new FileOutputStream(
								filePath + seq + ".csv", false),	// true to append 
								StandardCharsets.UTF_8));	// set encoding utf-8
					
					bw.write(str.toString());
					bw.close();
				} catch(Exception e){
					e.printStackTrace();
					return false;
				}
				
				str = new StringBuffer();
				seq++;
			}
		}
		
		return true;
	}
	
	
	/**
	 * 표절률을 CSV로 생성한다
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makeCopyRateCSV(HashMap<String, HashMap<String, Double>> resultMap, boolean isBulk) {
		BufferedWriter bw;
		
		String filePath = CONFIG.getDataPath() + "/result-data/sherlock/copy-rate/daily/sherlock-result.csv";
		
		if(isBulk) {
			filePath = CONFIG.getDataPath() + "/result-data/sherlock/copy-rate/all/sherlock-result.csv";
		}
		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						filePath, false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String resIdx : resultMap.keySet()) {
				HashMap<String, Double> map = resultMap.get(resIdx);
				
				double score = 0.0;
				int cnt = 0;
				
				double maxScore = 0.0;
				String maxRes = "";
				
				for(String subKey : map.keySet()) {
					score += map.get(subKey);
					
					if(map.get(subKey) > maxScore) {
						maxScore = map.get(subKey);
						maxRes = subKey;
					}
					cnt++;
				}
				score = score/cnt;
				
				if(score >= 0.01)
					bw.write(resIdx + "," + maxRes + "," + String.format("%.2f", score) + NEWLINE);
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 문장 및 이력서 번호들을 세팅한다
	 * 
	 * @param isBulk
	 * @return
	 */
	public HashMap<String, List<String>> setInvData(boolean isBulk) {
		HashMap<String, List<String>> invMap = new HashMap<String, List<String>> ();
		BufferedReader br = null;
		String filePath = CONFIG.getDataPath() + "pre-data/sherlock/inv-data/daily/";
		
		if(isBulk)
			filePath = CONFIG.getDataPath() + "pre-data/sherlock/inv-data/all/";
		
		File[] invDatas = new File(filePath).listFiles();
		
		try {
			for(File invData : invDatas) {
				br = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(invData.getAbsolutePath()), "UTF8"));
				
				String line = null;				
				while ((line = br.readLine()) != null) {
					String[] temp = line.split(",");
					
					if(temp != null && temp.length == 2) {
						List<String> tempList = new ArrayList<String> ();
						String[] values = temp[1].split("\\|");
						
						if(values != null && values.length > 0) {
							for(String value : values) {
								tempList.add(value.trim());
							}
							
							invMap.put(temp[0], tempList);
						}
					}
				}
	
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("setInvData : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return invMap;
	}
	
	
	/**
	 * 추가로 appy_idx에 존재하는 res-idx를 기반으로
	 * res-idx->seq->sentence를 맵에 넣는다.
	 * 
	 * @param RES_MEM_MAP
	 * @param isBulk
	 * @return
	 */
	public HashMap<String, HashMap<String, List<String>>> setSentenceMap(HashMap<String, String> RES_MEM_MAP, boolean isBulk) {
		HashMap<String, HashMap<String, List<String>>> 
							sentenceMap = new HashMap<String, HashMap<String, List<String>>> ();
		BufferedReader br = null;
		String filePath = CONFIG.getDataPath() + "pre-data/sherlock/inv-data/daily/";
		
		if(isBulk)
			filePath = CONFIG.getDataPath() + "pre-data/sherlock/inv-data/all/";
		
		File[] invDatas = new File(filePath).listFiles();
		try {
			for(File invData : invDatas) {
				br = new BufferedReader(
						new InputStreamReader(
						new FileInputStream(invData.getAbsolutePath()), "UTF8"));
				LOGGER.debug(invData.getName() + " try set sentence");
				
				String line = null;				
				while ((line = br.readLine()) != null) {
					String[] temp = line.split(",");
					
					if(temp != null) {
						String[] values = temp[1].split("\\|");
						
						for(String value : values) {
							String[] pks = value.split("_");
							
							// res_idx가 있을 경우, 문장을 연관 리스트에 포함시킨다
							if(RES_MEM_MAP.containsKey(pks[0])) {
								HashMap<String, List<String>> map = null;
								
								if(sentenceMap.containsKey(pks[0])) {
									map = sentenceMap.get(pks[0]);
								} else {
									map = new HashMap<String, List<String>> ();
								}
								
								List<String> list = null;
								if(map.containsKey(pks[1])) {
									list = map.get(pks[1]);
								} else {
									list = new ArrayList<String> ();
								}
								
								// 해시 문장을 리스트에 포함시킨다
								list.add(temp[0]);
								
								map.put(pks[1], list);
								sentenceMap.put(pks[0], map);
							}
						}
					}
				}
	
				br.close();
				
				System.out.println("sentenceMap.size() : " + sentenceMap.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("readTitDictionary : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return sentenceMap;
	}
	
	
	/**
	 * 소제목을 읽은 후, 유사값으로 변환
	 * 
	 * @param title
	 * @return
	 */
	public String setClssTitle(String title, HashMap<String, String> dicMap) {
		List<String> tokens = DANBI.extractNoun(title);
		String rtnTitle = "etc";
		
		HashMap<String, Integer> cntMap = new HashMap<String, Integer> ();
		
		if(tokens != null && tokens.size() > 0) {
			for(String token : tokens) {
				if(dicMap.containsKey(token)) {
					int cnt = 1;
					if(cntMap.containsKey(dicMap.get(token))) {
						cnt += cntMap.get(dicMap.get(token));
					}
					cntMap.put(dicMap.get(token), cnt);
				}
			}
		}
		
		if(cntMap.size() > 0) {
			int max = 0;
			
			for(String token : cntMap.keySet()) {
				if(cntMap.get(token) > max) {
					max = cntMap.get(token);
					rtnTitle = token; 
				}					
			}
		} 
		
		return rtnTitle;		
	}
	
	
	/**
	 * 제목과 컨텐츠를 형태소 분석 후, 해시화 한다
	 * 
	 * @param result
	 * @return
	 */
	public List<String> changeHashed(HashMap<String, HashMap<String, String>> result) {
		List<String> resultList = new ArrayList<String> ();
		
		StringBuffer word = null;
		StringBuffer hashStr = new StringBuffer();
		String titleHash = "";
		StringBuffer contentHash = new StringBuffer();
		
		// 자소서의 루프를 돈다
		for(String resIdx : result.keySet()) {
			HashMap<String, String> map = result.get(resIdx);
			
			hashStr = new StringBuffer();
			
			for(String title : map.keySet()) {
				word = new StringBuffer();
				titleHash = "";
				contentHash = new StringBuffer();
				
				// 형태소 분석 후, 가나다순으로 정렬 수행
				List<String> titleList = DANBI.extractNoun(title);
				Collections.sort(titleList);
				
				// 리스트형 단어를 문자로 합친다
				for(String token : titleList) {
					word.append(token);
				}
				
				// 제목의 해시값을 생성하기 위해 다시 titleList에 세팅하고 다시 word에 세팅
				titleList = DANBI.extractNoun(word.toString());
				word = new StringBuffer();
				for(String token : titleList) {
					word.append(token);
				}
				
				// 제목 해시 생성
				try {
					titleHash = COMMON.toSha(word.toString()).substring(0, 10);
				} catch (NoSuchAlgorithmException e1) {	e1.printStackTrace();}
				
				// 본문 처리 부분
				String[] contents = map.get(title).replaceAll("\n", ".").split("\\.");
				
				// 본문을 가져온다
				for(String content : contents) {
					if(content.trim().length() == 0) continue;
					List<String> contentList = DANBI.extractNoun(content);
					if(contentList == null || contentList.size() < 2) continue;
					
					// 정렬
					Collections.sort(contentList);
					
					StringBuffer resultSB = new StringBuffer();
					for(String str : contentList) {
						resultSB.append(str);
					}
					
					try {
						// 본문을 해시화
						if(contentHash.length() > 0) {
							contentHash.append("|");
							
						}
						contentHash.append(COMMON.toSha(resultSB.toString()).substring(0, 10));						
					} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
				} // for content
				
				if(title != null) {
					if(hashStr.length() > 0) {
						hashStr.append("#");						
					}
					hashStr.append(titleHash + "^" + contentHash.toString());
				}
			}
			
			resultList.add(resIdx + "," + hashStr.toString());
		}
		
		return resultList;
	}
	
	
	/**
	 * Hash 데이터를 CSV 형태로 내린다
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makePreCSV(List<String> resultList, String _name) {
		int endIdx = _name.length();
		BufferedWriter bw;
		
		if(_name.lastIndexOf(".") > -1)
			endIdx = _name.lastIndexOf(".");
		
		String name = _name.substring(0, endIdx);
		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "/pre-data/galaxy/sherlock/plagiarize/" + name + ".csv", false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String line : resultList) {
				bw.write(line + NEWLINE);
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 자기소개서를 변형한 데이터를 FGF로 다시 내린다
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makePreFGF(List<HashedItdcVO> resultList, String _name) {
		int endIdx = _name.length();
		BufferedWriter bw;
		
		if(_name.lastIndexOf(".") > -1)
			endIdx = _name.lastIndexOf(".");
		
		String name = _name.substring(0, endIdx);
		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "/pre-data/galaxy/sherlock/hashed-itdc/daily/" + name + ".fgf", false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			StringBuffer sb = new StringBuffer();
			for(HashedItdcVO vo : resultList) {
				sb.append("<__pk__>" + vo.getPk() + NEWLINE);
				sb.append("<__mem_idx__>" + vo.getMemIdx() + NEWLINE);
				sb.append("<__title__>" + vo.getTitle() + NEWLINE);
				sb.append("<__clss_title__>" + vo.getClssTitle() + NEWLINE);
				sb.append("<__contents__>" + vo.getContents() + NEWLINE);
				sb.append("<__length__>" + vo.getLength() + NEWLINE);
				sb.append("<__hashed__>" + vo.getHashed() + NEWLINE);				
			}
			
			bw.write(sb.toString());
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * CSV 형태로 결과를 내린다
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makeCSV(List<String> list) {
		BufferedWriter bw;
		String filePath = CONFIG.getDataPath() + "/result-data/sherlock/bulk/sherlock-bulk.csv";
						
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(filePath, false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String line : list) {
				bw.write(line + NEWLINE);
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/*public void calculate() {
	 * // 타이틀, 해시값, 리스트
		HashMap<String, HashMap<String, List<String>>> titleMap = new HashMap<String, HashMap<String, List<String>>> ();
		HashMap<String, List<String>> hashedMap = new HashMap<String, List<String>> ();
		List<String> list = null;
		if(titleMap.containsKey(title)) {
			hashedMap = titleMap.get(title);					
		}
		
		for(String content : contents) {
			if(content.trim().length() == 0) continue;
			List<String> contentList = DANBI.extractNoun(content);
			if(contentList == null || contentList.size() < 2) continue;

			list = new ArrayList<String> ();
			StringBuffer resultSB = new StringBuffer();
			for(String str : contentList) {
				resultSB.append(str);
			}					
			
			try {
				String shaStr = COMMON.toSha(resultSB.toString()).substring(0, 10);
				
				if(hashedMap.containsKey(shaStr)) {
					list = hashedMap.get(shaStr);							
				}
				
				if(!list.contains(resIdx)) {
					list.add(resIdx);
					hashedMap.put(shaStr, list);
				}
				
				if(list.size() >= 2)
					System.out.println(title + " " + resultSB + " " + shaStr +  " " + list);
			} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		}
		
		titleMap.put(title, hashedMap);
	}*/
	
	
	/**
	 * Value 값을 기반으로 정렬한다
	 * 
	 * @param kwdMap
	 * @return
	 */
	public HashMap<String, Integer> sortByKwdValue(HashMap<String, Integer> kwdMap) {
		HashMap<String, Integer> rtnMap = new LinkedHashMap<String, Integer> ();
		int limit = 10;		
		String maxKwd = "";
		int maxCnt = 0;
		
		if(kwdMap.size() < 10) {
			limit = kwdMap.size(); 
		}
		
		for(int i = 0; i < limit; i++) {
			maxKwd = "";
			maxCnt = 0;
			
			for(String kwd : kwdMap.keySet()) {
				if(kwdMap.get(kwd) >= maxCnt) {
					maxKwd = kwd;
					maxCnt = kwdMap.get(kwd); 
				}
			}
			
			// 마지막이 끝나면 해당 값으로 저장
			rtnMap.put(maxKwd, maxCnt);
			kwdMap.remove(maxKwd);
		}
		
		return rtnMap;
	}
	
	
	/**
	 * str을 파싱하여, hash data를 세팅
	 * 
	 * @param str
	 * @param map
	 */
	public void parseHashData(String _str, HashMap<String, HashMap<String, List<String>>> map) {
		String[] str = _str.split(",");
		String[] item = str[1].split("\\#");
		HashMap<String, List<String>> itemMap = null;
		List<String> resList = null;
		
		// 29
		// 555b5fca13^64bf8ab813|90e1967aae|5c8b33bce4|cffe7e1146
		// 650bf2aa6d^efe6225d08|0938679e83|a8b84c0ef1|5168b33265
		// 329e3cb853^a8be7226b3|d6f6e4b98a|81252734b7|f5823210b2|da7029bcc4|627d09fff3|6934b22a1e|bafacf9bee
		for(int i = 0; i < item.length; i++) {
			String[] value = item[i].split("\\^");
			
			if(value == null || value.length < 2) continue;
			
			// 타이틀이 있을 경우, 값을 가져온다
			if(map.containsKey(value[0])) {
				itemMap = map.get(value[0]);
			} else {
				itemMap = new HashMap<String, List<String>> ();
			}
			
			// 문장이 있을 경우, 값을 가져온다
			String[] sentence = value[1].split("\\|");
			for(int k = 0; k < sentence.length; k++) {
				
				if(itemMap.containsKey(sentence[k])) {
					resList = itemMap.get(sentence[k]);
				} else {
					resList = new ArrayList<String> ();
				}
				
				resList.add(str[0]);
				itemMap.put(sentence[k], resList);
				//System.out.println(sentence[k] + "=>" + resList);
			}
			
			map.put(value[0], itemMap);			
		}
	}
	
	
	public String similar(HashMap<String, HashMap<String, List<String>>> hashedMap, 
						String[] value, HashMap<String, Integer> resCnt) {
		
		// 제목이 맵에 존재할 경우메만 연산 수행
		if(hashedMap.containsKey(value[0]) && value.length == 2) {
			HashMap<String, List<String>> resMap = hashedMap.get(value[0]);
			String[] sentences = value[1].split("\\|");
			
			for(String sentence : sentences) {
				if(resMap.containsKey(sentence)){
					List<String> list = resMap.get(sentence);
					
					for(String res : list) {
						int cnt = 0; 
						if(resCnt.containsKey(res)) {
							cnt = resCnt.get(res);
						}
						cnt++;
						resCnt.put(res, cnt);											
					}
				}
			}							
		}
		
		return "";
	}
	
	
	/**
	 * 제목사전을 읽어서 맵에 세팅
	 * 
	 * @return
	 */
	public HashMap<String, String> readTitDictionary() {
		HashMap<String, String> titDicMap = new HashMap<String, String> ();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "pre-data/galaxy/sherlock/dictionary/title.dic"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\|");
				String[] words = temp[1].split(",");
				
				for(String word : words) {
					titDicMap.put(word, temp[0]);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("readTitDictionary : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return titDicMap;
	}	
}
