package com.saramin.sai.module;

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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.saramin.sai.util.CommonUtil;
import com.saramin.sai.util.Logger;
import com.saramin.sai.vo.ConfigVO;
import com.saramin.sai.vo.HashedItdcVO;
import com.saramin.sai.vo.IntroDocVO;

import ai.sai.sinabro.api.DanbiAPI;
import ai.sai.sinabro.danbi.vo.MorphemeVO;


/**
 * HR 컨설팅을 위한 전용 모듈
 * 
 * @author jinhoo.jang
 * @since 2019.04.10
 * @team AI. PART
 */
public class ConanModule {
	private ConfigVO CONFIG;
	private CommonUtil COMMON;
	private DanbiAPI DANBI;
	private Logger LOGGER;
	
	final String NEWLINE = System.getProperty("line.separator");
	
	public ConanModule(ConfigVO CONFIG) {
		this.CONFIG = CONFIG;
		DANBI = new DanbiAPI(CONFIG.getDataPath());
		COMMON = new CommonUtil();
		
		LOGGER = new Logger(CONFIG.isDebug(), "SHERLOCK", CONFIG.getLogPath());
	}
	
	
	/**
	 * 엑셀에 있는 데이터를 읽는다
	 * @return
	 */
	public HashMap<String, IntroDocVO> readResultExcel(String name) {
		HashMap<String, IntroDocVO> result = new HashMap<String, IntroDocVO> ();
		File excel = new File(CONFIG.getDataPath() + "nambu/raw-data/excel/라벨_" + name + ".xlsx");
		IntroDocVO vo = null;
        
        try {        	
    		FileInputStream excelFile = new FileInputStream(new File(excel.getAbsolutePath()));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
    
            int idx = 0;
            int row = 0;
            
            String applyIdx = "";
            String jobType = "";
            String job = "";
            String score = "";
            
            while (iterator.hasNext()) {
            	row++;
            	Row currentRow = iterator.next();
            	
            	if(row == 1)
            		continue;
            	
            	Iterator<Cell> cellIterator = currentRow.iterator();
                idx = 0;
                
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    
                    // 수험번호
                    if(idx == 2) {
                    	applyIdx = getStringValue(currentCell);                        
                    } 
                    // 지원구분
                    else if(idx == 3) { 
                    	jobType = getStringValue(currentCell);                                                
                    } 
                    // 지원분야
                    else if(idx == 4) {
                    	job = getStringValue(currentCell);                    	
                    }        
                    // 직무능력소개
                    else if(idx == 7) {
                    	score = getStringValue(currentCell);
                    	
                    	vo = new IntroDocVO();
                    	
                    	vo.setQues("직무능력소개서");
                    	vo.setQuesClss("0");
                    	
                    	vo.setSequence("1");
                        vo.setTraining(true);
                        vo.setComp("nambu");
                        vo.setApplyIdx(applyIdx);
                    	vo.setJobType(jobType);
                    	vo.setJob(job);
                    	vo.setLabel(score);
                        
                    	vo.setPk(vo.getComp() + "_" + vo.getSequence() + "_" + vo.getJobType() 
                    			+ "_" + vo.getJob() + "_" + vo.getQuesClss() + "_" + vo.getApplyIdx());
                    	
                    	result.put(vo.getPk(), vo);
                    }
                    // 1번문항
                    else if(idx == 8) {
                    	score = getStringValue(currentCell);
                    	
                    	vo = new IntroDocVO();
                    	
                    	vo.setQues("지원동기, 노력했던 경험");
                    	vo.setQuesClss("1");
                    	vo.setSequence("1");
                        vo.setTraining(true);
                        vo.setComp("nambu");
                        vo.setApplyIdx(applyIdx);
                    	vo.setJobType(jobType);
                    	vo.setJob(job);
                    	vo.setLabel(score);
                        
                    	vo.setPk(vo.getComp() + "_" + vo.getSequence() + "_" + vo.getJobType() 
            					+ "_" + vo.getJob() + "_" + vo.getQuesClss() + "_" + vo.getApplyIdx());
                    	
                    	result.put(vo.getPk(), vo);
                    }
                    // 2번문항
                    else if(idx == 9) {
                    	score = getStringValue(currentCell);
                    	
                    	vo = new IntroDocVO();
                    	
                    	vo.setQues("봉사정신");
                    	vo.setQuesClss("2");
                    	vo.setSequence("1");
                        vo.setTraining(true);
                        vo.setComp("nambu");
                        vo.setApplyIdx(applyIdx);
                    	vo.setJobType(jobType);
                    	vo.setJob(job);
                    	vo.setLabel(score);
                        
                    	vo.setPk(vo.getComp() + "_" + vo.getSequence() + "_" + vo.getJobType() 
            					+ "_" + vo.getJob() + "_" + vo.getQuesClss() + "_" + vo.getApplyIdx());
                    	
                    	result.put(vo.getPk(), vo);
                    }
                    // 3번 문항
                    else if(idx == 10) {
                    	score = getStringValue(currentCell);
                    	
                    	vo = new IntroDocVO();
                    	
                    	vo.setQues("새로운 시도");
                    	vo.setQuesClss("3");
                    	vo.setSequence("1");
                        vo.setTraining(true);
                        vo.setComp("nambu");
                        vo.setApplyIdx(applyIdx);
                    	vo.setJobType(jobType);
                    	vo.setJob(job);
                    	vo.setLabel(score);
                        
                    	vo.setPk(vo.getComp() + "_" + vo.getSequence() + "_" + vo.getJobType() 
            					+ "_" + vo.getJob() + "_" + vo.getQuesClss() + "_" + vo.getApplyIdx());
                    	
                    	result.put(vo.getPk(), vo);
                    }
                    // 4번 문항
                    else if(idx == 11) {
                    	score = getStringValue(currentCell);
                    	
                    	vo = new IntroDocVO();
                    	
                    	vo.setQues("설득 및 팀워크");
                    	vo.setQuesClss("4");
                    	vo.setSequence("1");
                        vo.setTraining(true);
                        vo.setComp("nambu");
                        vo.setApplyIdx(applyIdx);
                    	vo.setJobType(jobType);
                    	vo.setJob(job);
                    	vo.setLabel(score);
                        
                    	vo.setPk(vo.getComp() + "_" + vo.getSequence() + "_" + vo.getJobType() 
            					+ "_" + vo.getJob() + "_" + vo.getQuesClss() + "_" + vo.getApplyIdx());
                    	
                    	result.put(vo.getPk(), vo);
                    }
                    
                    idx++;
                }
                
                row++;
            }
            
            workbook.close();
        	
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
	}
	
	
	/**
	 * 엑셀에 있는 데이터를 읽는다
	 * 
	 * @return
	 */
	public HashMap<String, IntroDocVO> readResultExcel(String _job, String _jobType) {
		HashMap<String, IntroDocVO> result = new HashMap<String, IntroDocVO> ();
		File excel = new File(CONFIG.getDataPath() + "nambu/raw-data/excel/190415.xlsx");
		IntroDocVO vo = null;
        
        try {        	
    		FileInputStream excelFile = new FileInputStream(new File(excel.getAbsolutePath()));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
    
            int idx = 0;
            int row = 0;
            
            String pk = "";
            String applyIdx = "";
            String jobType = "";
            String job = "";
            String content = "";
            
            while (iterator.hasNext()) {
            	row++;
            	Row currentRow = iterator.next();
            	
            	if(row == 1)
            		continue;            	
            	
                Iterator<Cell> cellIterator = currentRow.iterator();
                idx = 0;
                                
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    //System.out.println(row + " " + getStringValue(currentCell).trim());
                    
                    // 수험번호
                    if(idx == 0) {
                    	applyIdx = getStringValue(currentCell).trim();
                    }
                    else if(idx == 2) {
                    	jobType = getStringValue(currentCell).trim();
                    	if(jobType.indexOf("신입") > -1)
                    		jobType = "신입";
                    	
                    	if(jobType.indexOf("경력") > -1)
                    		jobType = "경력";
                    }
                    else if(idx == 3) {
                    	job = getStringValue(currentCell).trim();
                    }
                    // 직무능력 소개서
                    else if(idx == 4) {
                    	// 자소서 매핑
                    	content = getStringValue(currentCell);                	                    
                    }
                    // 직무능력 소개서 글자수
                    else if(idx == 5) {
                    	if(_job.equals(job) && _jobType.equals(jobType)) {
	                    	pk = "nambu_2_" + jobType + "_" + job + "_" + 0 + "_" + applyIdx;
	                    	vo = new IntroDocVO();
	                    	
	                    	vo.setApplyIdx(applyIdx);
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/1000);
	                    	vo.setPk(pk);
	                    	vo.setComp("nambu");
	                    	vo.setContent(content);
	                    	vo.setJob(job);
	                    	vo.setJobType(jobType);
	                    	vo.setQues("직무능력소개서");
	                    	vo.setQuesClss("0");
	                    	vo.setSequence("2");
	                    	
		                    result.put(pk, vo);
                    	}
                    }
                    // 자소서1
                    else if(idx == 6) {
                    	content = getStringValue(currentCell);
                    }
                    // 자소서1 글자수
                    else if(idx == 7) {
                    	if(_job.equals(job) && _jobType.equals(jobType)) {
	                    	pk = "nambu_2_" + jobType + "_" + job + "_" + 1 + "_" + applyIdx;
	                    	vo = new IntroDocVO();
	                    	
	                    	vo.setApplyIdx(applyIdx);
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	vo.setPk(pk);
	                    	vo.setComp("nambu");
	                    	vo.setContent(content);
	                    	vo.setJob(job);
	                    	vo.setJobType(jobType);
	                    	vo.setSequence("2");
	                    	
	                    	vo.setQues("지원동기, 노력했던 경험");
	                    	vo.setQuesClss("1");                    	
	                    	
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서2
                    else if(idx == 8) {
                    	content = getStringValue(currentCell);
                    }
                    // 자소서2 글자수
                    else if(idx == 9) {
                    	if(_job.equals(job) && _jobType.equals(jobType)) {
	                    	pk = "nambu_2_" + jobType + "_" + job + "_" + 2 + "_" + applyIdx;
	                    	vo = new IntroDocVO();
	                    	
	                    	vo.setApplyIdx(applyIdx);
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	vo.setPk(pk);
	                    	vo.setComp("nambu");
	                    	vo.setContent(content);
	                    	vo.setJob(job);
	                    	vo.setJobType(jobType);
	                    	vo.setSequence("2");
	                    	
	                    	vo.setQues("봉사정신");
	                    	vo.setQuesClss("2");                    	
	                    	
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서3
                    else if(idx == 10) {
                    	content = getStringValue(currentCell);
                    }
                    // 자소서3 글자수
                    else if(idx == 11) {
                    	if(_job.equals(job) && _jobType.equals(jobType)) {
	                    	pk = "nambu_2_" + jobType + "_" + job + "_" + 3 + "_" + applyIdx;
	                    	vo = new IntroDocVO();
	                    	
	                    	vo.setApplyIdx(applyIdx);
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	vo.setPk(pk);
	                    	vo.setComp("nambu");
	                    	vo.setContent(content);
	                    	vo.setJob(job);
	                    	vo.setJobType(jobType);
	                    	vo.setSequence("2");
	                    	
	                    	vo.setQues("새로운 시도");
	                    	vo.setQuesClss("3");                    	
	                    	
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서4
                    else if(idx == 12) {
                    	content = getStringValue(currentCell);                    	
                    }
                    // 자소서4 글자수
                    else if(idx == 13) {
                    	if(_job.equals(job) && _jobType.equals(jobType)) {
	                    	pk = "nambu_2_" + jobType + "_" + job + "_" + 4 + "_" + applyIdx;
	                    	vo = new IntroDocVO();
	                    	
	                    	vo.setApplyIdx(applyIdx);
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	vo.setPk(pk);
	                    	vo.setComp("nambu");
	                    	vo.setContent(content);
	                    	vo.setJob(job);
	                    	vo.setJobType(jobType);
	                    	vo.setSequence("2");
	                    	
	                    	vo.setQues("새로운 시도");
	                    	vo.setQuesClss("4");
	                    	
	                    	result.put(pk, vo);
                    	}
                    }
                    
                    idx++;
                }
            }
            
            workbook.close();        	
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
	}
	
	
	/**
	 * 엑셀에 있는 데이터를 읽는다
	 * 
	 * @return
	 */
	public void setContentExcel(String name, HashMap<String, IntroDocVO> EXCEL_MAP) {
		HashMap<String, IntroDocVO> result = new HashMap<String, IntroDocVO> ();
		File excel = new File(CONFIG.getDataPath() + "nambu/raw-data/excel/자소서_" + name + ".xlsx");
		IntroDocVO vo = null;
        
        try {        	
    		FileInputStream excelFile = new FileInputStream(new File(excel.getAbsolutePath()));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
    
            int idx = 0;
            int row = 0;
            
            String pk = "";
            String applyIdx = "";
            String jobType = "";
            String job = "";
            boolean flag = false;
            
            while (iterator.hasNext()) {
            	row++;
            	Row currentRow = iterator.next();
            	
            	if(row == 1)
            		continue;            	
            	
                Iterator<Cell> cellIterator = currentRow.iterator();
                idx = 0;
                vo = new IntroDocVO();
                
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    System.out.println(row + " " + getStringValue(currentCell).trim());
                    
                    // 수험번호
                    if(idx == 0) {
                    	applyIdx = getStringValue(currentCell).trim();
                    }
                    else if(idx == 2) {
                    	jobType = getStringValue(currentCell).trim();
                    }
                    else if(idx == 3) {
                    	job = getStringValue(currentCell).trim();
                    }
                    // 직무능력 소개서
                    else if(idx == 4) {
                    	pk = "nambu_1_" + jobType + "_" + job + "_" + 0 + "_" + applyIdx;
                    	
                    	if(EXCEL_MAP.containsKey(pk)) {
	                    	vo = EXCEL_MAP.get(pk);
	                    	
	                    	// 자소서 매핑
	                    	vo.setContent(getStringValue(currentCell));
	                    	flag = true;
                    	} else {
                    		flag = false;
                    	}
                    }
                    // 직무능력 소개서 글자수
                    else if(idx == 5) {
                    	if(flag && vo != null) {
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/1000);
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서1
                    else if(idx == 6) {
                    	pk = "nambu_1_" + jobType + "_" + job + "_" + 1 + "_" + applyIdx;
                    	
                    	if(EXCEL_MAP.containsKey(pk)) {
	                    	vo = EXCEL_MAP.get(pk);
	                    	
	                    	// 자소서 매핑
	                    	vo.setContent(getStringValue(currentCell));
	                    	flag = true;
                    	} else {
                    		flag = false;
                    	}
                    }
                    // 자소서1 글자수
                    else if(idx == 7) {
                    	if(flag && vo != null) {
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서2
                    else if(idx == 8) {
                    	pk = "nambu_1_" + jobType + "_" + job + "_" + 2 + "_" + applyIdx;
                    	
                    	if(EXCEL_MAP.containsKey(pk)) {
	                    	vo = EXCEL_MAP.get(pk);
	                    	
	                    	// 자소서 매핑
	                    	vo.setContent(getStringValue(currentCell));
	                    	flag = true;
                    	} else {
                    		flag = false;
                    	}
                    }
                    // 자소서2 글자수
                    else if(idx == 9) {
                    	if(flag && vo != null) {
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서3
                    else if(idx == 10) {
                    	pk = "nambu_1_" + jobType + "_" + job + "_" + 3 + "_" + applyIdx;
                    	
                    	if(EXCEL_MAP.containsKey(pk)) {
	                    	vo = EXCEL_MAP.get(pk);
	                    	
	                    	// 자소서 매핑
	                    	vo.setContent(getStringValue(currentCell));
	                    	flag = true;
                    	} else {
                    		flag = false;
                    	}
                    }
                    // 자소서3 글자수
                    else if(idx == 11) {
                    	if(flag && vo != null) {
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	result.put(pk, vo);
                    	}
                    }
                    // 자소서4
                    else if(idx == 12) {
                    	pk = "nambu_1_" + jobType + "_" + job + "_" + 4 + "_" + applyIdx;
                    	
                    	if(EXCEL_MAP.containsKey(pk)) {
	                    	vo = EXCEL_MAP.get(pk);
	                    	
	                    	// 자소서 매핑
	                    	vo.setContent(getStringValue(currentCell));
	                    	flag = true;
                    	} else {
                    		flag = false;
                    	}
                    }
                    // 자소서4 글자수
                    else if(idx == 13) {
                    	if(flag && vo != null) {
	                    	vo.setLength(Double.parseDouble(getStringValue(currentCell))/700);
	                    	result.put(pk, vo);
                    	}
                    }
                    
                    idx++;
                }
                
                result.put(pk, vo);
            }
            
            workbook.close();        	
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	/**
	 * cell의 데이터를 string으로 변경
	 * 
	 * @param cell
	 * @return
	 */
	public static String getStringValue(Cell cell) {
		if(cell == null)
			return "";
		
	    String rtnValue = "";
	    
	    try {
	        rtnValue = cell.getStringCellValue();
	    } catch(IllegalStateException e) {
	        rtnValue = Integer.toString((int)cell.getNumericCellValue());            
	    }
	    
	    return rtnValue;
	}
	
	
	/**
	 * FGF를 생성하였음
	 * 
	 * @param resultList
	 * @param _name
	 */
	public int makeFGF(HashMap<String, IntroDocVO> EXCEL_MAP, String name) {
		BufferedWriter bw;
		int total = 0;
		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "nambu/raw-data/" + name + ".fgf", false),						 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String pk : EXCEL_MAP.keySet()) {
				IntroDocVO vo = EXCEL_MAP.get(pk);
				//System.out.println(pk);
				
					bw.write("<__pk__>" + pk + NEWLINE);
					bw.write("<__apply_idx__>" + vo.getApplyIdx() + NEWLINE);
					bw.write("<__comp__>" + vo.getComp() + NEWLINE);
					bw.write("<__sequence__>" + vo.getSequence() + NEWLINE);
					bw.write("<__job_type__>" + vo.getJobType() + NEWLINE);
					bw.write("<__job__>" + vo.getJob() + NEWLINE);
					bw.write("<__ques__>" + vo.getQues() + NEWLINE);
					bw.write("<__ques_clss__>" + vo.getQuesClss() + NEWLINE);
					bw.write("<__content__>" + vo.getContent() + NEWLINE);
					bw.write("<__length__>" + vo.getLength() + NEWLINE);
				
				if(vo.getLabel() != null && vo.getLabel().trim().length() > 0) {
					if(vo.getLabel().equalsIgnoreCase("pass")) {
						bw.write("<__label__>1" + NEWLINE);
					} else if(vo.getLabel().equalsIgnoreCase("pass")) {
						bw.write("<__label__>0" + NEWLINE);
					} else {
						bw.write("<__label__>" + vo.getLabel() + NEWLINE);
					}
				}
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();			
		}
		
		return total;
	}	
	
	
	/**
	 * JOB별 직업 사전 생성(label = 1, ques_clss = 0)
	 * 
	 * @return
	 */
	public boolean makeJobDic() {
		HashMap<String, Integer> dicMap = new HashMap<String, Integer> ();
		
		BufferedReader br = null;
		StringBuffer cont = null;
		
		String job = "";
		String ques = "";
		String label = "";
		
		File[] fgfs = new File(CONFIG.getDataPath() + "raw-data/introdoc/nambu/").listFiles();
		
		for(File fgf : fgfs) {
			if(fgf.getName().indexOf("경력") > -1)
				continue;
			
			try {
				br = new BufferedReader(
							new InputStreamReader(
							new FileInputStream(fgf.getAbsolutePath()), "UTF8"));
					
				String line = null;				
				while ((line = br.readLine()) != null) {
					
					// ex: 사무담당원
					if(line.indexOf("<__job__>") > -1) {
						job = line.substring("<__job__>".length(), line.length());
					}
					// 직업내용
					else if(line.indexOf("<__content__>") > -1) {
						cont = new StringBuffer();
						cont.append(line.substring("<__content__>".length(), line.length()));
					}
					// 질문내용(ques_clss -> 0)
					else if(line.indexOf("<__ques_clss__>") > -1) {
						ques = line.substring("<__ques_clss__>".length(), line.length());
					}
					// label
					else if(line.indexOf("<__label__>") > -1) {
						label = line.substring("<__label__>".length(), line.length());
						
						// 데이터를 생성
						if(ques.equals("0") && label.equals("1")) {
							
							// dicMap
							//System.out.println(getNer(cont.toString(), "SCH"));
							List<String> nounLst = DANBI.extractNoun(cont.toString());
							for(String noun : nounLst) {
								int cnt = 1;
								if(dicMap.containsKey(noun)) {
									cnt += dicMap.get(noun);
								}
								dicMap.put(noun, cnt);
							}
						}
					}
					// content의 중간값
					else if(line.indexOf("<__") == -1) {
						cont.append(line);
					}
				}				
	
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("makeJobDic : " + e.getMessage());
				System.exit(1);
			} finally {
				if (br != null) { 
					try { br.close();} 
					catch (IOException e1) {e1.printStackTrace();}
				}
			}
		
			// dicmap을 크기순으로 정렬
			Map<String, Integer> result = dicMap.entrySet().stream()
	                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
			
			BufferedWriter bw;
			
			try {	
				bw = new BufferedWriter(
						new OutputStreamWriter(
						new FileOutputStream(
							CONFIG.getDataPath() + "dictionary/nambu/" + job + ".dic", false),						 
							StandardCharsets.UTF_8));	// set encoding utf-8
				
				for(String key : result.keySet()) {
					if(result.get(key) > 1) 
						bw.write(key + NEWLINE);				
				}
				
				bw.close();
			} catch(Exception e){
				e.printStackTrace();			
			}
		} // files
		
		return true;
	}
	
	
	/**
	 * 직업 직무사전을 읽는다
	 */
	public HashMap<String, String> readTaskDic() {
		HashMap<String, String> dicMap = new HashMap<String, String>();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "dictionary/nambu/사무담당원-user.dic"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				dicMap.put(line.trim(), "");
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("makeJobDic : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return dicMap;
	}
	
	
	/**
	 * 직업 직무사전을 읽는다
	 */
	public List<String> tf(String _quesClss, String _job) {
		HashMap<String, Integer> tfMap = new HashMap<String, Integer> ();
		List<String> rtnList = new ArrayList<String> ();
		
		BufferedReader br = null;
		StringBuffer cont = null;
		
		String job = "";
		String ques = "";
		String label = "";
		int rows = 0;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "raw-data/introdoc/nambu/" + _job + ".fgf"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				if(line.indexOf("<__job__>") > -1) {
					job = line.substring("<__job__>".length(), line.length());
				}
				// 직업내용
				else if(line.indexOf("<__content__>") > -1) {
					cont = new StringBuffer();
					cont.append(line.substring("<__content__>".length(), line.length()));
				}
				// 질문내용(ques_clss -> 0)
				else if(line.indexOf("<__ques_clss__>") > -1) {
					ques = line.substring("<__ques_clss__>".length(), line.length());
				}
				// label
				else if(line.indexOf("<__label__>") > -1) {
					label = line.substring("<__label__>".length(), line.length());
					
					// 지정된 데이터일 경우
					if(ques.equals(_quesClss) && job.equals(_job)) {
						rows++;
						
						// dicMap
						//System.out.println(getNer(cont.toString(), "SCH"));
						HashMap<String, String> refineMap = new HashMap<String, String> (); 
						List<String> nounLst = DANBI.extractNoun(cont.toString());
						for(String noun : nounLst) {
							if(!refineMap.containsKey(noun)) {
								int cnt = 1;
								if(tfMap.containsKey(noun)) {
									cnt += tfMap.get(noun);
								}
								tfMap.put(noun, cnt);
							}
							
							refineMap.put(noun, "");
						}
					}
				}
				// content의 중간값
				else if(line.indexOf("<__") == -1) {
					cont.append(line);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("tf : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		// dicmap을 크기순으로 정렬
		Map<String, Integer> result = tfMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		
		// 직업별 tf csv(키워드, 카운트, 등장비율) 생성
		for(String term : result.keySet()) {
			int termCnt = result.get(term);
			if(termCnt > 1) 
				rtnList.add(term + "," + termCnt + "," + (double)termCnt/rows);			
		}
		
		return rtnList;
	}
	
	
	/**
	 * TF 데이터를 CSV로 내린다
	 * 
	 * @param tfList
	 * @return
	 */
	public int makeTfCsv(List<String> tfList, String _quesClss, String _job) {
		BufferedWriter bw = null;
		int total = 0;
		
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "pre-data/nambu/" + _job + "-" + _quesClss + ".csv", false),						 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String tfStr : tfList) {
				bw.write(tfStr + NEWLINE);
				total++;
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return total;
	}
	
	
	/**
	 * 지정된 내용의 트레이닝 및 테스트 데이터를 생성한다 
	 * 
	 * @param termList
	 * @param job
	 * @param quesClss
	 */
	public void makeTrainNTestCSV(List<String> termList, String _job, String _quesClss) {
		
		BufferedReader br = null;
		StringBuffer cont = null;
		
		List<String> trainList = new ArrayList<String> ();
		List<String> testList = new ArrayList<String> ();
		
		String job = "";
		String ques = "";
		String label = "";
		String length = "";
		
		int rows = 0;
		int size = 500;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "raw-data/introdoc/nambu/" + _job + ".fgf"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				if(line.indexOf("<__job__>") > -1) {
					job = line.substring("<__job__>".length(), line.length());
				}
				// 직업내용
				else if(line.indexOf("<__content__>") > -1) {
					cont = new StringBuffer();
					cont.append(line.substring("<__content__>".length(), line.length()));
				}
				// 질문내용(ques_clss -> 0)
				else if(line.indexOf("<__ques_clss__>") > -1) {
					ques = line.substring("<__ques_clss__>".length(), line.length());
				}
				// 길이
				else if(line.indexOf("<__length__>") > -1) {
					length = line.substring("<__length__>".length(), line.length());
				}
				// label
				else if(line.indexOf("<__label__>") > -1) {
					label = line.substring("<__label__>".length(), line.length());
					
					// 지정된 데이터일 경우
					if(ques.equals(_quesClss) && job.equals(_job)) {
						rows++;
						int termCnt = 0;
						
						StringBuffer sb = new StringBuffer();
						List<String> nounLst = DANBI.extractNoun(cont.toString());
						HashMap<String, String> map = new HashMap<String, String> ();
						for(String noun : nounLst) {
							map.put(noun, "");
						}
						
						// 길이 추가
						if(Double.parseDouble(length) >= 0.8) {
							sb.append("4");
						} else if(Double.parseDouble(length) >= 0.6) {
							sb.append("3");
						} else if(Double.parseDouble(length) >= 0.4) {
							sb.append("2");
						} else if(Double.parseDouble(length) >= 0.2) {
							sb.append("1");
						} else {
							sb.append("0");
						}
						
						//sb.append(String.format("%.2f", Double.parseDouble(length)));
						for(String term : termList) {
							termCnt++;
							
							if(sb.length() > 0)
								sb.append(",");
							
							if(map.containsKey(term)) {
								sb.append("1");
							} else {
								sb.append("0");
							}
							
							if(termCnt >= size) {
								break;
							}
						}
						
						// training은 마지막에 label값 추가
						testList.add(sb.toString());
						trainList.add(sb.toString() + "," + label);
					}
				}
				// content의 중간값
				else if(line.indexOf("<__") == -1) {
					cont.append(line);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("makeTrainNTestCSV : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		// 최종으로 데이터 출력
		BufferedWriter bw;
		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "train-data/nambu/" + _job + "-" + _quesClss + ".csv", false),						 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String line : trainList) {	
				bw.write(line + NEWLINE);				
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();			
		}
		
		bw = null;		
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "test-data/nambu/" + _job + "-" + _quesClss + ".csv", false),						 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String line : testList) {	
				bw.write(line + NEWLINE);				
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();			
		}
	}
	
	
	/**
	 * 지정된 내용의 테스트 데이터를 생성한다 
	 * 
	 * @param termList
	 * @param job
	 * @param quesClss
	 */
	public void makeTestCSV(
			List<String> termList, String _job, String _quesClss, HashMap<String, String> blindMap,
			HashMap<String, HashMap<String, Object>> scoreMap) {
		
		// test 데이터를 만드는 과정에 블라인드 위반 여부를 넣는다
		BufferedReader br = null;
		StringBuffer cont = null;
		
		List<String> testList = new ArrayList<String> ();
		
		String pk = "";
		String job = "";
		String ques = "";
		String length = "";
		
		int rows = 0;
		int size = 500;
		HashMap<String, String> map = new HashMap<String, String> ();
		HashMap<String, Object> object = new HashMap<String, Object> (); 
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "nambu/raw-data/" + _job + ".fgf"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				if(line.indexOf("<__apply_idx__>") > -1) {
					pk = line.substring("<__apply_idx__>".length(), line.length());
					
					map = new HashMap<String, String> ();
				}
				// 직업
				else if(line.indexOf("<__job__>") > -1) {
					job = line.substring("<__job__>".length(), line.length());
				}
				// 직업내용
				else if(line.indexOf("<__content__>") > -1) {
					cont = new StringBuffer();
					cont.append(line.substring("<__content__>".length(), line.length()));
				}
				// 질문내용(ques_clss -> 0)
				else if(line.indexOf("<__ques_clss__>") > -1) {
					ques = line.substring("<__ques_clss__>".length(), line.length());
				}
				// 길이
				else if(line.indexOf("<__length__>") > -1) {
					length = line.substring("<__length__>".length(), line.length());
					
					// 지정된 데이터일 경우
					if(ques.equals(_quesClss) && job.equals(_job)) {
						rows++;
						int termCnt = 0;
						
						StringBuffer sb = new StringBuffer();
						List<String> nounLst = DANBI.extractNoun(cont.toString());
						HashMap<String, List<String>> nerMap = getNer(cont.toString(), "SCH");
						
						StringBuffer blindContent = new StringBuffer(); 
						//StringBuffer blindNerContent = new StringBuffer();
						
						for(String noun : nounLst) {
							map.put(noun, "");
							
							// 블라인드 위반 찾기							
							if(blindMap.containsKey(noun)) {
								if(blindContent.length() > 0) 
									blindContent.append(",");
								blindContent.append(noun);								
							}
						}
						
						// ner 데이터를 세팅
						if(nerMap != null && nerMap.size() > 0) {
							for(String kwd : nerMap.keySet()) {
								if(blindContent.length() > 0) 
									blindContent.append(",");
								
								blindContent.append(kwd);
								//nerMap.get(kwd);
							}
						}
						
						// 길이 추가
						if(Double.parseDouble(length) >= 0.8) {
							sb.append(pk + ",4");
						} else if(Double.parseDouble(length) >= 0.6) {
							sb.append(pk + ",3");
						} else if(Double.parseDouble(length) >= 0.4) {
							sb.append(pk + ",2");
						} else if(Double.parseDouble(length) >= 0.2) {
							sb.append(pk + ",1");
						} else {
							sb.append(pk + ",0");
						}						
						
						for(String term : termList) {
							termCnt++;
							
							if(sb.length() > 0)
								sb.append(",");
							
							if(map.containsKey(term)) {
								sb.append("1");
							} else {
								sb.append("0");
							}
							
							if(termCnt >= size) {
								break;
							}
						}
						
						// score map을 세팅
						if(scoreMap.containsKey(pk)) {
							object = scoreMap.get(pk);
						} else {
							object = new HashMap<String, Object> (); 
						}
						
						object.put(ques + "_blind", blindContent.toString());
						scoreMap.put(pk, object);
						
						testList.add(sb.toString());
					}
				}				
				// content의 중간값
				else if(line.indexOf("<__") == -1) {
					cont.append(line);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("makeTestCSV : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		// 최종으로 데이터 출력
		BufferedWriter bw;
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(
						CONFIG.getDataPath() + "test-data/nambu-2/" + _job + "-" + _quesClss + ".csv", false),						 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			for(String line : testList) {	
				bw.write(line + NEWLINE);				
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();			
		}
	}
	
	
	/**
	 * ScoreMap 데이터를 출력한다
	 * 
	 * @param resultList
	 * @param _name
	 */
	public boolean makeScoreCSV(HashMap<String, HashMap<String, Object>> scoreMap) {
		BufferedWriter bw;
		StringBuffer sb = null;
		String filePath = CONFIG.getDataPath() + "/result-data/sherlock/nambu/nambu-score.csv";
						
		try {	
			bw = new BufferedWriter(
					new OutputStreamWriter(
					new FileOutputStream(filePath, false),	// true to append 
						StandardCharsets.UTF_8));	// set encoding utf-8
			
			
			for(String pk : scoreMap.keySet()) {
				sb = new StringBuffer();
				
				HashMap<String, Object> map = scoreMap.get(pk);
				// pk,1스코어,표절률,블라인드
				sb.append(pk + "," + map.get("1_score") + ",0," + map.get("1_blind"));
				sb.append("," + map.get("2_score") + ",0," + map.get("2_blind"));
				sb.append("," + map.get("2_score") + ",0," + map.get("2_blind"));
				sb.append("," + map.get("2_score") + ",0," + map.get("2_blind"));
				
				bw.write(sb.toString() + NEWLINE);
			}
			
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	
	/**
	 * 지정된 Term 데이터를 리스트화 시킨다
	 * 
	 * @param job
	 * @param quesClss
	 * @return
	 */
	public List<String> readTermList(String job, String quesClss) {
		List<String> termList = new ArrayList<String> ();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "pre-data/nambu/" + job + "-" + quesClss + ".csv"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				String[] temp = line.split(",");
				termList.add(temp[0]);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("readTermList : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return termList;
	}
	
	
	/**
	 * 블라인드 위배 사전을 읽는다
	 * 
	 * @return
	 */
	public HashMap<String, String> blindMap() {
		HashMap<String, String> blindMap = new HashMap<String, String> ();
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "dictionary/blind/blind.dic"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				blindMap.put(line.trim(), "");
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("blindMap : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return blindMap;
	}
	
	
	/**
	 * 직무경력을 검증한다
	 * 
	 * @return
	 */
	public boolean verifyJob(HashMap<String, String> tskMap) {
		BufferedReader br = null;
		StringBuffer cont = null;
		
		String pk = "";
		String job = "";
		String ques = "";
		String label = "";
		
		int passCnt = 0;
		int failCnt = 0;
		boolean flag = false;
		
		try {
			br = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(CONFIG.getDataPath() + "raw-data/introdoc/nambu/경력.fgf"), "UTF8"));
			
			String line = null;				
			while ((line = br.readLine()) != null) {
				// pk
				if(line.indexOf("<__pk__>") > -1) {
					pk = line.substring("<__pk__>".length(), line.length());
					flag = false;
				}
				// ex: 사무담당원
				else if(line.indexOf("<__job__>") > -1) {
					job = line.substring("<__job__>".length(), line.length());
				}
				// 직업내용
				else if(line.indexOf("<__content__>") > -1) {
					cont = new StringBuffer();
					cont.append(line.substring("<__content__>".length(), line.length()));
				}
				// 질문내용(ques_clss -> 0)
				else if(line.indexOf("<__ques_clss__>") > -1) {
					ques = line.substring("<__ques_clss__>".length(), line.length());
				}
				// label
				else if(line.indexOf("<__label__>") > -1) {
					label = line.substring("<__label__>".length(), line.length());
					
					if(ques.equals("0")) {						
						// dicMap
						HashMap<String, List<String>> nerMap = getNer(cont.toString(), "TSK,SKLC,SKLS,JOB");
						List<String> nounList = DANBI.extractNoun(cont.toString());
						
						for(String kwd : nerMap.keySet()) {
							if(tskMap.containsKey(kwd)) {
								flag = true;
								System.out.println(kwd + "=>" + nerMap.get(kwd));
							}
						}
						
						for(String kwd : nounList) {
							if(tskMap.containsKey(kwd)) {
								flag = true;
								System.out.println(kwd);
							}
						}
						
						if(flag) {
							System.out.println(pk + "=>PASS");
							passCnt++;
							//System.out.println(DANBI.extractNoun(cont.toString()));
						} else {
							System.out.println(pk + "=>FAIL");
							failCnt++;
							
							System.out.println(DANBI.extractNoun(cont.toString()));
						}
					}
				}
				// content의 중간값
				else if(line.indexOf("<__") == -1) {
					cont.append(line);
				}
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("makeJobDic : " + e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) { 
				try { br.close();} 
				catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		return true;
	}
	
	
	/**
	 * 지정된 NER이 뽑혔는지 체크한 후 리스트를 가져온다
	 * 
	 * @param str
	 * @param _ners
	 * @return
	 */
	public HashMap<String, List<String>> getNer(String str, String _ners) {
		HashMap<String, List<String>> rtnMap = new HashMap<String, List<String>> ();
		String[] ners = _ners.split(",");
		
		List<HashMap<String, MorphemeVO>> morphLst = DANBI.morphemeAnalyze(str);
		
		if(morphLst != null && morphLst.size() > 0) {
			for(HashMap<String, MorphemeVO> wordMap : morphLst) {
				for(String word : wordMap.keySet()) {
					// 개체명이 있으면 출력
					if(wordMap.get(word).getNer() != null && wordMap.get(word).getNer().size() > 0) {
						//LOGGER.debug(word + "=>" + wordMap.get(word).getNer().size());
						List<String> nerList = null;
						
						HashMap<String, ArrayList<String>> map = wordMap.get(word).getNer();
						for(String kwd : map.keySet()) {
							nerList = map.get(kwd);
							for(int i = 0; i < nerList.size(); i++) {
								ArrayList<String> list = new ArrayList<String> ();
								for(int n = 0; n < ners.length; n++) {
									if(nerList.get(i).equals(ners[n])) {
										list.add(ners[n]);									
									}
								}
								
								if(list.size() > 0)
									rtnMap.put(kwd, list);
							}										
						}									
					}								
				}
			}
		}
		
		return rtnMap;
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
