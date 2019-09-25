package sai.galaxyframework.sherlock.vo;


/**
 * 자기소개서 Value Object
 * 
 * @author jinhoo.jang
 * @since 2019.04.12
 * @team AI Part
 */
public class IntroDocVO {
	/** Primary Key(회사, 차수, 지원구분, 지원분야, 질문분류, 지원자번호) */
	private String pk;
	/** 지원자 번호 */
	private String applyIdx;
	/** 회사명 */
	private String comp;
	/** 차수 */
	private String sequence;
	/** 지원구분 */
	private String jobType;
	/** 지원분야 */	
	private String job;	
	/** 질문 */
	private String ques;
	/** 질문분류 */
	private String quesClss;
	/** 질문순서 */
	private String quesSeq;
	/** 본문 */
	private String content;
	/** 글자수 비율 */
	private double length;
	/** 라벨 데이터 */
	private String label;
	/** 학습용인지 여부 */
	private boolean isTraining;
	
	
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getApplyIdx() {
		return applyIdx;
	}
	public void setApplyIdx(String applyIdx) {
		this.applyIdx = applyIdx;
	}
	public String getComp() {
		return comp;
	}
	public void setComp(String comp) {
		this.comp = comp;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}	
	public String getQues() {
		return ques;
	}
	public void setQues(String ques) {
		this.ques = ques;
	}
	public String getQuesClss() {
		return quesClss;
	}
	public void setQuesClss(String quesClss) {
		this.quesClss = quesClss;
	}	
	public String getQuesSeq() {
		return quesSeq;
	}
	public void setQuesSeq(String quesSeq) {
		this.quesSeq = quesSeq;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean isTraining() {
		return isTraining;
	}
	public void setTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}	
}
