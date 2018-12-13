package com.tistory.needjarvis.service;

public class SherlockExecutor {
	
	/**
	 * 생성자
	 */
	public SherlockExecutor(String propNm) {
		
	}
	
	
	/**
	 * 스크립터
	 */
	public void execute() {
		// 자기소개서 파일을 읽어서 메모리에 저장한다
		// 문장별 형태소 분석으로 정제한 후, 명사들을 해시값으로 변환 후 해시값 - 자기소개서 리스트 형태로 저장
		// 사용자가 자기소개서를 작성하면
		// 문장으로 나눈 후
		// 문장별 형태소 분석을 수행한다
		// 명사 부분만 순서대로 모와서 해시값으로 변경 후, 동일한 해시값이 있는지 체크한다
		// 없으면, 해당 문장은 표절이 아니다.
		// 있으면, 표절 후보군 리스트를 저장 한다. 각각의 리스트는 카운트가 올라간다. str(res)-int(cnt) map
		// 모든 문장에 대한 후보군 리스트를 뽑았다면 중복된 후보군 순으로 candidate를 정렬한다.
		// 최종적으로 N를 선정하여 표절율을 계산한다.
		// 전체 자기소개서의 독창성, 표절율, 진부성을 계산한 후
		// 문제가 되는 문장을 사용자에게 보여준다.
	}
}