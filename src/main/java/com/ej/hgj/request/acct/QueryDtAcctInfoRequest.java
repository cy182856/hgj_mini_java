package com.ej.hgj.request.acct;

import lombok.Data;

import java.io.Serializable;
@Data
public class QueryDtAcctInfoRequest implements Serializable{

	private String custId;
	
	private String huSeqId;
	
	private String houseSeqId;

	
}
