package com.saramin.sai.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
	
	/**
	 * SHA-256으로 해싱하는 메소드
	 * 
	 * @param bytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String toSha(String msg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(msg.getBytes());

		return bytesToHex(md.digest());
	}

	/**
	 * 바이트를 헥스값으로 변환한다
	 * 
	 * @param bytes
	 * @return
	 */
	public String bytesToHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02x", b));
		}

		return builder.toString();
	}

	/**
	 * value로 맵 key 정렬하는 메소드
	 * 
	 * @param cntMap
	 * @return
	 */
	public List<Object> sortedKeysetByValue(Map map) {
		List<Object> keyset = new ArrayList<Object>();
		if(map.size() == 0) return keyset;
		
		keyset.addAll(map.keySet());
		Collections.sort(keyset, (o1, o2) -> {
			Object v1 = map.get(o1);
			Object v2 = map.get(o2);
			return ((Comparable) v2).compareTo(v1);
		});
		return keyset;
	}

	/**
	 * 파일의 내용을 한줄씩 리스트에 담아서 리턴하는 메소드
	 * 
	 * @param filePath
	 */
	public List<String> readFile(String filePath) {
		List<String> result = new ArrayList<String>();

		File file = new File(filePath);
		if (!file.exists() || !file.isFile())
			return result;

		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

			String line = "";

			while ((line = br.readLine()) != null) {
				result.add(line);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("readFile() : " + e.getMessage());
			return result;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * xml 데이터를 읽어들여 map 형태로 변환해 주는 메소드
	 * 
	 * @author leehyun.kang
	 * @param xmlFile
	 * @return
	 * @throws IOException 
	 * @throws InvalidPropertiesFormatException 
	 */
	public HashMap<String, String> xmlParserProperties(String xmlFile) throws Exception {
		HashMap<String, String> resultMap = new HashMap<String, String>();

		File file = new File(xmlFile + ".xml");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.loadFromXML(fileInput);
		fileInput.close();

		Enumeration<?> enuKeys = properties.keys();
		while (enuKeys.hasMoreElements()) {

			String key = (String) enuKeys.nextElement();
			String value = properties.getProperty(key);

			resultMap.put(key, value.trim());
		}

		return resultMap;
	}

	
	/**
	 * fgf 파일에서 구분점을 기준으로 복수의 데이터를 읽어오는 메소드
	 * 
	 * @param filePath
	 * @param mode
	 */
	public List<Map<String, String>> readFgfFile(String filePath) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		File file = new File(filePath);
		if (!file.exists() || !file.isFile())
			return result;

		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

			Map<String, String> kv = new HashMap<String, String>();
			boolean findPoint = false;
			String line = "";
			String key = "";

			while ((line = br.readLine()) != null) {
				// 정보의 구분 키를 찾는다
				if (!findPoint) {
					if (line.contains("<__")) {
						key = line.substring(3, line.indexOf("__>"));
						findPoint = true;
					}
				}

				if (line.contains("<__" + key + "__>")) {
					if (lines.size() > 0) {
						kv = extractKV(lines);
						result.add(kv);
					}
					lines = new ArrayList<String>();
					lines.add(line);
				} else {
					lines.add(line);
				}
			}
			// 마지막 정보를 담는다
			if (lines.size() > 0) {
				kv = extractKV(lines);
				result.add(kv);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("readFgfFile() : " + e.getMessage());
			return result;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return result;
	}
	
	
	/**
	 * FGF에서 필요한 컬럼을 읽어 들인다.
	 * 
	 * @param filePath
	 * @param mode
	 */
	public List<HashMap<String, String>> readFGF(String filePath, String key, String _fields) {
		HashMap<String, String> colMap = new LinkedHashMap<String, String> ();
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>> ();
		BufferedReader br = null;
		
		String[] fields = _fields.split(",");
		for(String field : fields) {
			colMap.put(field, "");
		}
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = null;
			HashMap<String, String> map = null;
			
			while ((line = br.readLine()) != null) {
				if(line.trim().length() > 0) {
					// key 값이 있으면
					if (line.indexOf("<__" + key + "__>") > -1) {
						// map이 != 이면 데이터 세팅
						if(map != null) {
							list.add(map);
							map = null;
						}
						
						map = new HashMap<String, String> ();
						map.put(key, line.substring(("<__" + key + "__>").length(), line.length()));
					}
					// 컬럼 index가 존재할 경우
					else if (line.indexOf("<__") > -1 && line.indexOf("__>") > -1) {
						// 컬럼 값을 추출하여, temp에 저장
						String temp = line.substring(
								line.indexOf("<__") + "<__".length(), line.indexOf("__>"));				
						
						// 추출한 컬럼이 요청하는 컬럼일 경우 수행
						if(colMap.containsKey(temp)) {
							map.put(temp, line.substring(("<__" + temp + "__>").length(), line.length()));
						}
					} 
				}				
			}
			
			if(map != null) {
				list.add(map);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("readColumn() : " + e.getMessage());			
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return list;
	}
	
	
	/**
	 * 자기소개서를 맵에 세팅한다
	 * 이력서 번호 - 제목 - 컨텐츠
	 * 
	 * @param list
	 * @return
	 */
	public HashMap<String, HashMap<String, String>> parseDocsForSherlock(List<String> list, 
				HashMap<String, String> MEM_RES_MAP) {
		
		HashMap<String, HashMap<String, String>> rtnMap 
							= new HashMap<String, HashMap<String, String>>();
		
		// 자기소개서는 res_idx로 시작하여, seq로 종료
		String resIdx = "";
		String pk = "";
		
		String title = "";
		StringBuffer contents = new StringBuffer();
		HashMap<String, String> map = null;
		
		boolean flag = false;
		
		
		for(String line : list) {
			// 시작점, 자기소개서 번호
			if(line.indexOf("<__res_idx__>") > -1) {
				resIdx = line.substring("<__res_idx__>".length(), line.length()).trim();
				
				if(MEM_RES_MAP.containsKey(resIdx)) {
					pk = MEM_RES_MAP.get(resIdx);
				} else {
					pk = "";
				}
					
				contents = new StringBuffer ();
				flag = false;								
			}
			// 제목
			else if(line.indexOf("<__title__>") > -1) {
				title = line.substring("<__title__>".length(), line.length()).trim();
			}
			// 컨텐츠내용
			else if(line.indexOf("<__contents__>") > -1) {
				contents.append(line.substring("<__contents__>".length(), line.length()).trim());
				flag = true;
			}
			// 시퀀스, 마지막 라인
			else if(line.indexOf("<__seq__>") > -1) {
				map = new HashMap<String, String> ();
				
				if(pk.length() > 0 && rtnMap.containsKey(pk)) {
					map = rtnMap.get(resIdx); 
				}
				
				map.put(title, contents.toString());
				
				if(pk.length() > 0) {
					rtnMap.put(pk, map);
				}
			}
			else if(flag && line.indexOf("<__") == -1){
				contents.append(line);
			}
		}		
		
		return rtnMap;
	}
	

	/**
	 * fgf형태 데이터에서 키,값 추출해서 맵에 담는 메소드
	 * 
	 * @param lines
	 * @return
	 */
	public Map<String, String> extractKV(List<String> lines) {
		Map<String, String> kv = new HashMap<String, String>();
		int startIdx = 0;
		int endIdx = 0;

		String line = "";
		String key = "";
		StringBuffer value = new StringBuffer();

		for (int i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			startIdx = line.indexOf("<__");
			endIdx = line.indexOf("__>");

			// key로 시작할 경우
			if (startIdx > -1) {
				// 이전 키,값은 kv맵에 담는다
				if (key.length() > 0) {
					kv.put(key, value.toString());
					value.setLength(0);
				}

				// 새로운 키,값을 변수에 담는다
				key = line.substring(startIdx + 3, endIdx);
				value.append(line.substring(endIdx + 3));
			}
			// value만 있는 경우
			else if (startIdx == -1) {
				value.append(" " + line);
			}
		}
		// 마지막 키,값을 담는다
		kv.put(key, value.toString());
		return kv;
	}

	/**
	 * 분석 결과 저장하는 메소드
	 * 
	 * @param result
	 * @param dir
	 * @param fileNm
	 * @return
	 */
	public int save(List<String> result, String dir, String fileNm) {
		if (result.size() > 0) {
			File file = new File(dir);
			if (!file.isDirectory())
				file.mkdirs();

			String newLine = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			BufferedWriter bw = null;

			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).length() == 0)
					continue;

				sb.append(result.get(i));
				sb.append(newLine);
			}

			try {
				file = new File(dir + fileNm);
				bw = new BufferedWriter(new OutputStreamWriter(
					 new FileOutputStream(file.getAbsolutePath(), false),
					 StandardCharsets.UTF_8));
				bw.write(sb.toString());
				bw.close();
			} catch (Exception e) {
				System.out.println("save() : " + e.getMessage());
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				}
			}
		} else {
			System.out.println("save() : result 0.");
			return 0;
		}
		return result.size();
	}
	
	/**
	 * 기존 파일에 붙여쓰는 메소드
	 * 
	 * @param result
	 * @param dir
	 * @param fileNm
	 * @return
	 */
	public int appendToFile(List<String> result, String dir, String fileNm) {
		if (result.size() > 0) {
			File file = new File(dir);
			if (!file.isDirectory())
				file.mkdirs();

			String newLine = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			BufferedWriter bw = null;

			for (int i = 0; i < result.size(); i++) {
				if (result.get(i).length() == 0)
					continue;

				sb.append(result.get(i));
				sb.append(newLine);
			}

			try {
				file = new File(dir + fileNm);
				bw = new BufferedWriter(new OutputStreamWriter(
					 new FileOutputStream(file.getAbsolutePath(), true),
					 StandardCharsets.UTF_8));
				bw.write(sb.toString());
				bw.close();
			} catch (Exception e) {
				System.out.println("appendToFile() : " + e.getMessage());
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
						return -1;
					}
				}
			}
		} else {
			System.out.println("appendToFile() : result 0.");
			return 0;
		}
		return result.size();
	}
	
	/**
	 * IDX뒤의 n자리를 상위폴더로 하는 경로 찾는 메소드 ex) 1234567 -> 567/ 12 -> 012/
	 * 
	 * @param idx
	 * @param last
	 * @return
	 */
	public String findPath(String idx, int n) {
		StringBuffer zero = new StringBuffer();
		int len = idx.length();
		String dirPath = "";
		String folder = "";

		// folder명 길이가 n보다 작을 경우
		if (len < n) {
			for (int i = 0; i < n - len; i++) {
				zero.append("0");
			}

			folder = zero.toString() + idx;
		} else {
			folder = zero.toString() + idx.substring(len - n, len);
		}

		dirPath = folder + "/";
		return dirPath;
	}

	/**
	 * 해당년도 기준, 생년->나이 산정하는 메소드
	 * 
	 * @param birthYear
	 * @return
	 */
	public Integer transeAge(String birthYear) {
		// 예외처리
		if (birthYear == null || birthYear.trim().length() == 0)
			return 0;

		// 올해년도를 가져온다
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Date date = new Date();
		int year = Integer.parseInt(sdf.format(date));

		Pattern p = Pattern.compile("(^[0-9]*$)");
		Matcher m = null;

		m = p.matcher(birthYear);

		// 년도길이이고, 숫자로 이루어진 문자열인지 체크한다
		if (birthYear.length() == 4 && m.find()) {
			return year - Integer.parseInt(birthYear) + 1;
		} else {
			return 0;
		}
	}

	/**
	 * 경력개월수->경력연차 산정하는 메소드
	 * 
	 * @param month
	 * @return
	 */
	public Integer transeCrY(String month) {
		// 예외처리
		if (month == null || month.trim().length() == 0)
			return -1;

		// 신입
		if (month.equals("0"))
			return 0;

		Pattern p = Pattern.compile("(^[0-9]*$)");
		Matcher m = null;

		m = p.matcher(month);

		// 숫자로 이루어진 문자열인지 확인한다
		if (m.find()) {
			// 경력개월수 -> 경력연차 구한다
			return (Integer.parseInt(month) / 12) + 1;
		} else {
			return -1;
		}
	}

	/**
	 * 리스트 입력받아, 각 항목의 빈도수 구하는 메소드
	 * 
	 * @param list
	 * @return
	 */
	public Map<Object, Integer> getFrequency(List<Object> list) {
		Map<Object, Integer> frequency = new HashMap<Object, Integer>();

		int cnt = 0;

		for (Object key : list) {
			if (!frequency.containsKey(key))
				cnt = 0;
			else
				cnt = frequency.get(key);

			frequency.put(key, cnt + 1);
		}

		return frequency;
	}

	/**
	 * 날짜 구하는 메소드
	 * beforeDay: 오늘날짜=0,어제날짜=-1
	 * 
	 * @return
	 */
	public String getDate(String format, int beforeDay) {
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, beforeDay);
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
		return sdf.format(cal.getTime());
	}
	
	/**
	 * 사이 날짜 가져오는 메소드
	 * 
	 * @param start
	 * @param end
	 * @param format
	 * @return
	 */
	public List<String> getBetweenDate(String start, String end, String format) {
		List<String> betweenDt = new ArrayList<String>();
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date startDt = sdf.parse(start);
			Date endDt = sdf.parse(end);
			Date currentDt = startDt;
			
			if(!startDt.before(endDt)) return betweenDt;
			
			while (currentDt.compareTo(endDt) <= 0) {
				betweenDt.add(sdf.format(currentDt));
	            Calendar c = Calendar.getInstance();
	            c.setTime(currentDt);
	            c.add(Calendar.DAY_OF_MONTH, 1);
	            currentDt = c.getTime();
	        }
		} catch(Exception e) {
			System.out.println("getBetweenDate() : " + e.getMessage());
			System.out.println("start,end,format : "+start+","+end+","+format);
			return betweenDt;
		}
		return betweenDt;
	}
}
