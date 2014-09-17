package shelter.android.survey.classes.menus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shelter.android.survey.classes.R;
import shelter.android.survey.classes.forms.ExpandableList;
import shelter.android.survey.classes.forms.FormActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class EditMap extends FormActivity {

	String slum_id = "";
	String s_id = "";	
	String slum_name="";
	DatabaseHandler db;
	ExpandableList listAdapter;
	Boolean bCheckedFlag = true;
	Boolean bExpandFlag = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Intent intent = getIntent();
		if (intent.hasExtra("slumID")) {
			slum_id = intent.getExtras().getString("slumID");
			slum_name= intent.getExtras().getString("slumName");
		}

		db = new DatabaseHandler(this);
		ScrollView sv = new ScrollView(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		Button bt_showmap = new Button(this);
		bt_showmap.setText("Show on map");

		LinearLayout c_layout=new LinearLayout(this);
		/*********************************/
		// get the listview
		final ExpandableListView expListView = new ExpandableListView(getApplicationContext());
		
		final List<String> listDataHeader = new ArrayList<String>();
		final HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();

		// setting list adapter
		expListView.setVerticalScrollBarEnabled(true);		
		expListView.setScrollbarFadingEnabled(false);
		expListView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,370));// 200/*doesnt
													// workLayoutParams.WRAP_CONTENT*/));
		List<String> a_line = new ArrayList<String>();
		
		/**************************************/
		String inputJsonString = parseDownloadToString("all_surveys.json");
		
		try {
			JSONObject obj = new JSONObject(inputJsonString);			
			String c_getid = "";

			/********************************************/
			JSONArray meta = obj.getJSONArray(SCHEMA_KEY_MYMAP);
			
			ArrayList<List<String>> tbl = new ArrayList<List<String>>();
			
			//getting data from sqlite database : passing 4 parameter are ( table name, condition field, condition, order by)
			tbl = db.getAllDataList(db.TABLE_PLOTTEDSHAPE_GROUP,db.KEY_SLUMID ,slum_id, db.KEY_DRAWABLE_COMPONENT);
			
			//conditon for check : list have value or not
			//if not then show on map button invisible 
			if (tbl.size() > 0) {

				for (int j = 0; j < tbl.size(); j++) {
					
					//condition for : drawable component id is same than 
					//a_line Arraylist will add new latlong. vice versa 
					if (!tbl.get(j).get(2).toString().equalsIgnoreCase(c_getid)) {
						a_line = new ArrayList<String>();						
					}					
					// For loop get Name from Code
					for (int i = 0; i < meta.length(); i++) {
						JSONObject name = meta.getJSONObject(i);

						c_getid = tbl.get(j).get(2).toString();

						//Condition Check Drawable Component id match with selected slum drawn shape
						if (tbl.get(j).get(2).toString().equalsIgnoreCase(name.getString("id"))) {
							
							//Adding listDataheader : check not more than one item enter in list
							if (!listDataHeader.contains(name.getString("name")	+ " " + name.getString("extra"))) {
								
								listDataHeader.add(name.getString("name") + " " + name.getString("extra"));								
							}
//							//Adding all values in a_line array 
//							a_line.add(tbl.get(j).get(0).toString() + ";" 	//pk_id
//									+ tbl.get(j).get(1).toString() + ";"  	//slim_id
//									+ tbl.get(j).get(2).toString() + ";" 	//shape_id
//									+ name.getString("name") + ":"       	// Name : shape with name
//									+ name.getString("extra") + ":"      	// shape with dimension
//									+ tbl.get(j).get(3).toString()+";"      //name of shape specify by user
//									+ slum_name);     						//Slum Name;     	
							
							//Adding all values in a_line array 
							a_line.add(tbl.get(j).get(0).toString() + "_" 	//pk_id
									+ tbl.get(j).get(1).toString() + "_"  	//slim_id
									+ tbl.get(j).get(2).toString() + "_" 	//shape_id
									+ name.getString("name") + ":"       	// Name : shape with name
									+ name.getString("extra") + ":"      	// shape with dimension
									+ tbl.get(j).get(3).toString() +"_"
									+ slum_name);    //name of shape specify by user
									  
							
							listDataChild.put(name.getString("name") + " "
									+ name.getString("extra"), a_line);									
						}
					}					
				}
				
				//For Sorting ListHeader
				Collections.sort(listDataHeader, new Comparator<String>() {
			        @Override
			        public int compare(String s1, String s2) {		        	
			        	 return s1.trim().compareToIgnoreCase(s2.trim());
			        }
			    });	

				// Adding Expandable list view in Edit Activity 
				listAdapter = new ExpandableList(this, listDataHeader,	listDataChild);				
				expListView.setAdapter(listAdapter);
				
				//show on map button make visible
				bt_showmap.setVisibility(View.VISIBLE);
				c_layout.setVisibility(View.VISIBLE);
			} else {
				//show on map button make invisible
				bt_showmap.setVisibility(View.INVISIBLE);
				c_layout.setVisibility(View.INVISIBLE);
			}
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		
		/********************** Show on Map button click events ***********************************************/
		
		bt_showmap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		bt_showmap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(EditMap.this, MapActivity.class);
				
				//Here listAdapter.returnSid() return all slum selected in Expandable list view
				intent.putExtra("SID", listAdapter.returnSid().replace("|", ""));
				intent.putExtra("slumID", slum_id);
				intent.putExtra("slumName", slum_name);
				intent.putExtra("slumbutton", "show");
				startActivity(intent);
				finish();				
			}
		});
		
		/********************** Select/UnSelect button click events ***********************************************/
		
		Button bt_selectAll = new Button(this);
		bt_selectAll.setText("Select/UnSelect All");		
		bt_selectAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				listAdapter.g_id = "";
				listAdapter.s_id = "";
				if(bCheckedFlag)
				{
					for(int i=0; i<listDataHeader.size();i++)
					{
						listAdapter.g_id = listAdapter.g_id + ",|" + i + "|";
						for (int j=0;j<listDataChild.get(listDataHeader.get(i)).size();j++){
							listAdapter.s_id = listAdapter.s_id + ",|" + listDataChild.get(listDataHeader.get(i)).get(j).split("_")[0] + "|";
						}
					}
				}
				listAdapter.notifyChange();
				bCheckedFlag = !bCheckedFlag;
				
			}
		});
		
		/********************** Collapse/Expand button click events ***********************************************/
		
		final Button bt_collapse = new Button(this);
		bt_collapse.setText("Expand/Collapse");
		
		bt_collapse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(bExpandFlag)
				{				
					for(int i=0; i<listDataHeader.size();i++)
					{
						expListView.expandGroup(i);
					}
					
				}else
				{				
					for(int i=0; i<listDataHeader.size();i++)
					{
						expListView.collapseGroup(i);
					}
				}
				
				bExpandFlag= !bExpandFlag;			
			}
		});
		
		LinearLayout.LayoutParams childParam1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT);
		childParam1.weight = 0.5f;
		bt_selectAll.setLayoutParams(childParam1);

		LinearLayout.LayoutParams childParam2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT);
		childParam2.weight = 0.5f;
		bt_collapse.setLayoutParams(childParam2);

		c_layout.setWeightSum(1f);
		c_layout.addView(bt_selectAll);
		c_layout.addView(bt_collapse);
	

		/********************** Add New button creation and  click events ***********************************************/
		
		Button bt_addnew = new Button(this);
		bt_addnew.setText("Add new");
		bt_addnew.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		bt_addnew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(EditMap.this, MapActivity.class);
				
				//Here listAdapter.returnSid() return all slum selected in Expandable list view				
				if(listAdapter != null){
					intent.putExtra("SID", listAdapter.returnSid().replace("|", ""));
				}				
				
				intent.putExtra("slumID", slum_id);	
				intent.putExtra("slumName", slum_name);
				intent.putExtra("slumbutton", "addnew");
				startActivity(intent);
				finish();
			}
		});

		/********************** Download creation and  click events ***********************************************/
		Button bt_download = new Button(this);
		bt_download.setText("Download");
		bt_download.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		bt_download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertMessage();
				
			}
		});	

		/************************Adding all component in Layout  ************************************************************************/
		layout.addView(expListView);
		layout.addView(c_layout);
		layout.addView(bt_showmap);		
		layout.addView(bt_addnew);
		layout.addView(bt_download);
				
		//adding layout in Scroll view
		sv.addView(layout);
		setContentView(sv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public String parseDownloadToString(String filename) {
		try {
			FileInputStream stream = openFileInput(filename);
			return parseFileToString(stream);

		} catch (IOException e) {
			Log.i("Log", "IOException: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Trust every server - don't check for any certificate
	 */

	private void restartActivity() {
		Intent intent = new Intent(this, Index.class);
		startActivity(intent);
	}

	public void downloadMap() {
		
		if (isNetworkAvailable() == true) {
					
			Thread t = new Thread() {
				public void run() {
					try {
						Looper.prepare();
						URL url = new URL("https://survey.shelter-associates.org/android/download/slum_map/"+slum_id+"/");
						
						// Ignore unverified certificate
						trustAllHosts();
						HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
						https.setHostnameVerifier(DO_NOT_VERIFY);
						InputStream is = (InputStream) https.getContent();
						String jsonData = convertStreamToString(is);
						int httpResponseCode = https.getResponseCode();
						
						if (httpResponseCode == 200) {
							JSONArray facts = new JSONArray(jsonData);
							
							//retrieving all data from json
							// first deleting all data if exits
							if (facts.length() > 0) {
								db.deletePlottedShapeGroup(slum_id);
							}							
							
							for (int k = 0; k < facts.length(); k++) {
								JSONObject fact = facts.getJSONObject(k);

								/**************** Code for formatting Lat/long ****************/
								JSONObject obj12 = new JSONObject(fact.getString("lat_long"));
								JSONArray cordinate = obj12.getJSONArray("coordinates");
								String c_type = obj12.getString("type");
								String strCor = cordinate.toString().replace(" ", "")
												.replace(",", " ").replace("] [", ",")
												.replace("[", "").replace("]", "");
								String[] C_split = strCor.split(",");
								ArrayList<LatLng> viewltpoints = new ArrayList<LatLng>();

								for (int i = 0; i < C_split.length; i++) {
									String[] val = C_split[i].split(" ");
									LatLng allLatLng = new LatLng(Double.parseDouble(val[1].toString()),
																Double.parseDouble(val[0].toString()));
									viewltpoints.add(allLatLng);
								}

								/************************ Save data according to shape ************************************/
								String latlongData = "";
								if (!c_type.equalsIgnoreCase("POINT")) {
									for (int i = 0; i < viewltpoints.size(); i++) {
										latlongData = latlongData.trim()+ ", "+ viewltpoints.get(i).toString()
														.trim()
														.replace("lat/lng:", "")
														.replace(" ", "");
									}
									/********* Inserting In Database ************************/									
									db.insertPlottedShapeGroup(slum_id,
												fact.getString("drawn_compo"),
												fact.getString("name"), 
												fact.getString("date"),
												latlongData.substring(1,latlongData.length()));
								
								} else {
									
									for (int i = 0; i < viewltpoints.size(); i++) {
										latlongData = latlongData+ ", "	+ viewltpoints.get(i).toString();	
										/********* Inserting In Database ************************/										
										  db.insertPlottedShapeGroup(slum_id,
														fact.getString("drawn_compo"),
														fact.getString("name"),
														fact.getString("date"),
														viewltpoints.get(i).toString().trim().replace("lat/lng:","").replace(" ",""));
									}
								}
								/*******************************/
							}
							Toast.makeText(getBaseContext(),"Download successful.", Toast.LENGTH_LONG).show();
							Intent intent = getIntent();
							intent.putExtra("slumId", slum_id);
							finish();
							startActivity(intent);
						}
						else if (httpResponseCode == 500|| httpResponseCode == 404) {							
							Toast.makeText(getBaseContext(),"This survey wasn't found on the server.",Toast.LENGTH_LONG).show();
						}						
						https.disconnect();						
					} catch (Exception e) {
						e.printStackTrace();
					}
					Looper.loop();
				}
			};

			t.start();
		} else {
			Toast.makeText(getBaseContext(), "Something went wrong.",Toast.LENGTH_LONG).show();
		}
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/*************************** Confirmation Window On Delete record ***********************************************/
	public void alertMessage() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked				
					 downloadMap();
					 break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked				
					break;					
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure? You Want To Overwrite Exitsting Maps")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	
	}	
	/*************************** The End ***********************************/
}
