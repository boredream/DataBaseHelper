package com.boredream.dbhelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.boredream.dbhelper.domain.SaveData;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		DBHelper dbHelper = DBHelper.getInstance(this);
		
		SaveData saveData = new SaveData();
		saveData.id = 110;
		saveData.name = "hehe";
		saveData.isSexy = true;
		boolean flag = dbHelper.addData(saveData);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
