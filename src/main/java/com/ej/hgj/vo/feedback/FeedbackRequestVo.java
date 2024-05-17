package com.ej.hgj.vo.feedback;

import lombok.Data;

import java.io.Serializable;

@Data
public class FeedbackRequestVo implements Serializable {
	private static final long serialVersionUID = 1548330339323601219L;

	private String id;

	private String fileList[];

	private String cstCode;

	private String proNum;

	private String wxOpenId;

	private String feedbackDesc;

	private String cstPhone;

	private Integer pageNum = 1;//页数
	private Integer pageSize = 10;//页大小
}
