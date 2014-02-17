package com.boredream.dbhelper.domain;

import com.boredream.dbhelper.BaseData;


public class SaveData extends BaseData {
	public Integer id;
	public String name;
	public boolean isSexy;
	@Override
	public String toString() {
		return "SaveData [id=" + id + ", name=" + name + ", isSexy=" + isSexy
				+ ", _id=" + _id + "]";
	}
	
}
