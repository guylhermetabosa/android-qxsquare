package com.tabosag.qxsquare;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LocaisProximos extends Activity {
	ArrayList<String> lugares;
	private TextView text;
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_select_contact);
		
		lugares = new ArrayList<String>();
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		
		
		
		for (int i = 0; i < 20; i++) {
			lugares.add(extras.getString("local"+i));
			
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,lugares);
		listview = (ListView) findViewById(R.id.list);
		listview.setAdapter(adapter);
		
	}

}
