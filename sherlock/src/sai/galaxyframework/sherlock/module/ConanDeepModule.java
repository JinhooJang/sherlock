package sai.galaxyframework.sherlock.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import sai.galaxyframework.sherlock.util.CommonUtil;
import sai.galaxyframework.sherlock.util.Logger;
import sai.galaxyframework.sherlock.vo.ConfigVO;


/**
 * 텐서플로우로 분석하는 모듈
 * 
 * @author jinhoo.jang
 * @since 2018.06.07
 */
public class ConanDeepModule {
	private CommonUtil COMMON;
	private ConfigVO CONFIG;
	private Logger LOGGER;
	
	private String modelPath;
	private String dataPath;
	private String trainDataPath;
	
	private float[][] inputArr; // 모델에 입력할 데이터
	private int ROW;
	private int FEATURE;
	
	private List<Float> labelList;
	
	public ConanDeepModule(ConfigVO CONFIG) {
		this.CONFIG = CONFIG;
		LOGGER = new Logger(CONFIG.isDebug(), "GALAXY", CONFIG.getLogPath());
		
		modelPath = CONFIG.getModelPath() + "nambu/java/";
		dataPath = CONFIG.getDataPath() + "test-data/nambu-2/";
		trainDataPath = CONFIG.getDataPath() + "train-data/nambu/";
	}
	
	
	/**
	 * 태그를 초기화 한다
	 * 
	 * @return
	 */
	public List<String> initialize(String job, String quesClss) {
		
		int[] mtrxSize = new int[2];
		mtrxSize = getDataSize(dataPath + job + "-" + quesClss + ".csv");
		ROW = mtrxSize[0];
		FEATURE = mtrxSize[1]-1;
		inputArr = new float[ROW][FEATURE];	// 데이터를 담을 행렬
		
		// csv에 있는 test 데이터를 행렬에 담음
		return csvToMtrx(job + "-" + quesClss);
	}
	
	
	/**
	 * 성향별 돌리기 위해서 초기화를 수행하는 메소드
	 * 
	 * @return
	 */
	public void initializeTrainData(String job, String quesClss) {
		
		int[] mtrxSize = new int[2];
		mtrxSize = getDataSize(trainDataPath + job + "-" + quesClss + ".csv");
		ROW = mtrxSize[0];
		FEATURE = mtrxSize[1]-1;
		inputArr = new float[ROW][FEATURE];	// 데이터를 담을 행렬
		labelList = new ArrayList<Float> ();
		
		System.out.println("ROW: " + ROW);
		System.out.println("FEATURE: " + FEATURE);

		// csv에 있는 test 데이터를 행렬에 담음
		csvToMtrxTrainData(job + "-" + quesClss);
	}
	
	
	/**
	 * 모델에 데이터를 넣은 결과 계산
	 * 
	 * @return
	 */
	public void analysis(String job, String quesClss, HashMap<String, HashMap<String, Object>> scoreMap) {
		LOGGER.debug(job + "-" + quesClss + " initialize...[BEGIN]");
		List<String> resList = initialize(job, quesClss);
		LOGGER.debug(job + "-" + quesClss + " initialize...[END]");
		
		// 모델 번들 로드
		try (SavedModelBundle b = SavedModelBundle.load(modelPath + job + "-" + quesClss, "serve")) {
			
			// 세션 생성
			Session sess = b.session();
			ArrayList<Float> tmp = new ArrayList<Float>();
			for (int i = 0; i < inputArr.length; i++)
				for (int j = 0; j < inputArr[0].length; j++)
					tmp.add(inputArr[i][j]);
			
			// 입력 데이터(inputArr)를 FloatBuffer로 변환
			FloatBuffer fb = FloatBuffer.allocate(ROW * FEATURE);

			for (float f : tmp) {
				fb.put(f);
			}
			fb.rewind();

			// 입력 텐서 생성 (FloatBuffer를 텐서에 넣음)
			Tensor<Float> x = Tensor.create(new long[] { ROW, FEATURE }, fb);

			// 모델 실행 : 입력 텐서 x를 넣어 결과 계산
			// y[][] : 모델의 예측 결과 값
			float[][] y = sess.runner()
					.feed("x", x) // python code : X
					.feed("keep_prob",  Tensor.create(1.0F))
					.fetch("h") // python code : predicted						
					.run().get(0).copyTo(new float[ROW][1]);

			for(int i = 0; i < y.length; i++) {
				for(int j = 0; j < y[i].length; j++) {
					HashMap<String, Object> obj = scoreMap.get(resList.get(i));
					
					if(y[i][j] < 0) {
						obj.put(quesClss + "_score", 0);
					} else if(y[i][j] > 3) {
						obj.put(quesClss + "_score", 3);
					} else {
						obj.put(quesClss + "_score", y[i][j]);
					}
					
					scoreMap.put(resList.get(i), obj);
						
					// System.out.println(resList.get(i) + " " + (job + "-" + quesClss) + " : " + y[i][j] + " " + Math.round(y[i][j]));						
					
					//if(y[i][j])
					/*if(Math.round(y[i][j]) > 4) {
						trueMap.put(resList.get(i), "");
					}*/
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 트레이닝 데이터의 정확률을 계산한다
	 * 
	 * @return
	 */
	public HashMap<String, ArrayList<String>> analysisTrainData(String job, String quesClss) {
		HashMap<String, ArrayList<String>> rtnMap = new HashMap<String, ArrayList<String>>(); 
				
		LOGGER.debug(job + "-" + quesClss + " initialize...[BEGIN]");
		initializeTrainData(job, quesClss);
		LOGGER.debug(job + "-" + quesClss + " initialize...[END]");
		
		// 모델 번들 로드
		try (SavedModelBundle b = SavedModelBundle.load(modelPath + job + "-" + quesClss, "serve")) {
			
			// 세션 생성
			Session sess = b.session();
			ArrayList<Float> tmp = new ArrayList<Float>();
			for (int i = 0; i < inputArr.length; i++)
				for (int j = 0; j < inputArr[0].length; j++)
					tmp.add(inputArr[i][j]);
			
			// 입력 데이터(inputArr)를 FloatBuffer로 변환
			FloatBuffer fb = FloatBuffer.allocate(ROW * FEATURE);

			for (float f : tmp) {
				fb.put(f);
			}
			fb.rewind();

			// 입력 텐서 생성 (FloatBuffer를 텐서에 넣음)
			Tensor<Float> x = Tensor.create(new long[] { ROW, FEATURE }, fb);
			

			// 모델 실행 : 입력 텐서 x를 넣어 결과 계산
			// y[][] : 모델의 예측 결과 값
			float[][] y = sess.runner()
					.feed("x", x) // python code : X
					.feed("keep_prob",  Tensor.create(1.0F))
					.fetch("h") // python code : predicted
					.run().get(0).copyTo(new float[ROW][1]);

			ArrayList<String> resultList = new ArrayList<String> ();
			int acc = 0;
			String flag = "";
			for(int i = 0; i < y.length; i++) {
				for(int j = 0; j < y[i].length; j++) {
					flag = chkGrade(labelList.get(i), y[i][j]);
					
					if(flag.equals("T")) {
						acc++;
					}
					resultList.add(labelList.get(i) + "," + y[i][j] + "," + flag);
				}
			}
			//System.out.println("***predicted " + personality + " : " + y[0][0]);
			rtnMap.put(job + "-" + quesClss, resultList);
			System.out.println(job + "-" + quesClss + "," + y.length + "," + acc + "," + ((double)acc/y.length));
			
			//weight 갑 확인
			/*
			 * if(pi.equals("rnd")) { float[][] weight =
			 * sess.runner().fetch("weight").run().get(0).copyTo(new float[500][1]); for(int
			 * i=0; i<10; i++) { System.out.println(); System.out.print("["+i+"] : ");
			 * for(int j=0; j<weight[i].length; j++) { System.out.print(weight[i][j]+" "); }
			 * } System.out.println("***weight : " + weight); }
			 */
			
			//printMatrix();				
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return rtnMap;
	}
	
	
	/**
	 * csv 파일의 행/열 사이즈 측정
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public int[] getDataSize(String filePath) {
		// 원본 데이터의 행열 크기정보
		int[] mtrxSize = new int[2];
		mtrxSize[0] = 0;
		mtrxSize[1] = 0;
		BufferedReader br = null;

		try {
			// 원본 데이터를 읽음
			File csv = new File(filePath);
			br = new BufferedReader(new FileReader(csv));
			String line = "";
			String[] field = null;

			while ((line = br.readLine()) != null) {
				field = line.split(",");
				mtrxSize[0]++;
			}
			
			mtrxSize[1] = field.length;
			
		} catch (Exception e) {
			LOGGER.error("getDataSize err: " + e.getMessage());
			//e.printStackTrace();
		} finally {
			if(br != null) {
				try { br.close(); } catch (IOException e)  {}
			}
		}
		return mtrxSize;
	}
	
	
	/**
	 * csv 문자열의 사이즈 측정
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public int[] getDataSizeByStr(String str) {
		// 원본 데이터의 행열 크기정보
		int[] mtrxSize = new int[2];
		mtrxSize[0] = 1;
		mtrxSize[1] = str.split(",").length;		
			
		return mtrxSize;
	}
	

	/**
	 * csv 파일 데이터를 행렬로 옮김
	 * 
	 * @param filePath
	 * @param mtrx
	 * @throws IOException
	 */
	public List<String> csvToMtrx(String tag) {
		List<String> resList = new ArrayList<String> ();
		try {
			// csv 파일 읽어옴
			File csv = new File(dataPath + tag + ".csv");
			
			BufferedReader br = new BufferedReader(new FileReader(csv));
			String line = "";
			String[] field = null;

			// 한줄 씩 행렬에 데이터 적재
			for (int i = 0; i < ROW; i++) {
				if ((line = br.readLine()) != null) {
					field = line.split(",");
					
					// 현재 Feature의 값이 -1인 상태이기 때문에 +1 해줘야 함
					for (int j = 0; j < FEATURE+1; j++) {
						if(j == 0) {
							resList.add(field[j]);
						} else {
							inputArr[i][j-1] = Float.parseFloat(field[j]);
						}
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("csvToMtrx err: " + e.getMessage());			
		}
		
		return resList;
	}
	
	
	/**
	 * csv 파일 데이터를 행렬로 옮김
	 * 
	 * @param filePath
	 * @param mtrx
	 * @throws IOException
	 */
	public void csvTextToMtrx(String str) {
		String[] field = null;

		// 한줄 씩 행렬에 데이터 적재
		for (int i = 0; i < ROW; i++) {
			field = str.split(",");
				
			for (int j = 0; j < FEATURE; j++) {
				inputArr[i][j] = Float.parseFloat(field[j]);
			}				
		}
	}
	
	
	/**
	 * csv 파일 데이터를 행렬로 옮김
	 * 
	 * @param filePath
	 * @param mtrx
	 * @throws IOException
	 */
	public void csvToMtrxTrainData(String tag) {
		try {
			// csv 파일 읽어옴
			File csv = new File(trainDataPath + tag + ".csv");
			BufferedReader br = new BufferedReader(new FileReader(csv));
			String line = "";
			String[] field = null;

			// 한줄 씩 행렬에 데이터 적재
			/*LOGGER.debug("ROW : " + ROW);
			LOGGER.debug("FEATURE : " + FEATURE);*/
			
			for (int i = 0; i < ROW; i++) {
				if ((line = br.readLine()) != null) {
					field = line.split(",");
					
					for (int j = 0; j < FEATURE; j++) {
						inputArr[i][j] = Float.parseFloat(field[j]);
					}
					
					// 마지막 라인은 라벨링 세팅
					labelList.add(Float.parseFloat(field[FEATURE]));
					//LOGGER.debug("" + Float.parseFloat(field[FEATURE]));
				}
			}
		} catch (Exception e) {
			LOGGER.error("csvToMtrx err: " + e.getMessage());
			//e.printStackTrace();
		}
		
		//printMatrix();
	}
	

	/**
	 * 행렬로 바꾼 데이터 값 확인용 출력
	 * 
	 * @param mtrx
	 */
	public void printMatrix() {
		System.out.println("============ARRAY VALUES============");
		for (int i = 0; i < ROW; i++) {
			System.out.println("**********************************************************");
			for (int j = 0; j < FEATURE; j++) {
				System.out.print((int) inputArr[i][j] + " ");
			}
			System.out.println();
		}
	}	
	
	
	/**
	 * 유사값 판별
	 *  
	 * @param score
	 * @return
	 */
	public String chkGrade(Float org, Float tar) {
		if(org > tar) {
			if(org - tar < 1.0) return "T";
		} else if(org < tar) {
			if(tar - org < 1.0) return "T";
		} else if(org == tar) {
			return "T";
		} 
		
		return "F";		
	}
}
