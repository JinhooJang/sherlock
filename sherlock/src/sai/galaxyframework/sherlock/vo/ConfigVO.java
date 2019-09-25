package sai.galaxyframework.sherlock.vo;


/**
 * 설정 Value Object
 * @author Jinhoo Jang
 * @since 2018.12.13
 */
public class ConfigVO {

	/** 로그 경로 */
	private String logPath;
	/** 데이터 경로 */
	private String dataPath;
	/** 모델 경로 */
	private String modelPath;
	/** 개발 모드 여부 */
	private boolean debug;	
	
	
	public String getLogPath() {
		return logPath;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	public String getDataPath() {
		return dataPath;
	}
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}	
	public String getModelPath() {
		return modelPath;
	}
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}	
}
