package com.saramin.sai.module;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.saramin.sai.util.CommonUtil;
import com.saramin.sai.vo.ConfigVO;

import ai.sai.sinabro.api.DanbiAPI;

public class SherlockModule {
	private ConfigVO CONFIG;
	private CommonUtil COMMON;
	private DanbiAPI DANBI;
	final String NEWLINE = System.getProperty("line.separator");
	
	public SherlockModule(ConfigVO CONFIG) {
		this.CONFIG = CONFIG;
		DANBI = new DanbiAPI(CONFIG.getDataPath());
		COMMON = new CommonUtil();
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
						CONFIG.getDataPath() + "/pre-data/match/plagiarize/" + name + ".csv", false),	// true to append 
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
}
