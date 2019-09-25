package sai.galaxyframework.sherlock.vo;


/**
 * 자소서 Value Object
 * 
 * @author jinhoo.jang
 * @since 2019.03.28
 * @team A.I Part
 */
public class HashedItdcVO {
	
	/** mem_idx + seq 를 합친 것 해시 10글자 */
	private String pk;
	/** 자소서번호 */
	private String resIdx;
	/** 멤버번호 */
	private String memIdx;
	/** 자소서 소제목 */
	private String title;
	/** 자소서 소제목 해시값 */
	private String clssTitle;
	/** 자소서 내용 */
	private String contents;
	/** 시퀀스값 */
	private String seq;
	/** 자소서 길이 */
	private int length;
	/** 해시값 */
	private String hashed;
	
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getMemIdx() {
		return memIdx;
	}
	public void setMemIdx(String memIdx) {
		this.memIdx = memIdx;
	}	
	public String getResIdx() {
		return resIdx;
	}
	public void setResIdx(String resIdx) {
		this.resIdx = resIdx;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}	
	public String getClssTitle() {
		return clssTitle;
	}
	public void setClssTitle(String clssTitle) {
		this.clssTitle = clssTitle;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}	
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getHashed() {
		return hashed;
	}
	public void setHashed(String hashed) {
		this.hashed = hashed;
	}	
}
