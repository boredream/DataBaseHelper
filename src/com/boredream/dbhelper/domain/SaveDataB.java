package com.boredream.dbhelper.domain;

import com.boredream.dbhelper.BaseData;


public class SaveDataB extends BaseData{
	public long id;
	public String name;
	public float f;
	public int type;
	@Override
	public String toString() {
		return "SaveDataB [id=" + id + ", name=" + name + ", f=" + f
				+ ", type=" + type + ", _id=" + _id + "]";
	}
	
}
