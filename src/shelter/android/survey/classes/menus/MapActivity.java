package shelter.android.survey.classes.menus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import shelter.android.survey.classes.R;
import shelter.android.survey.classes.forms.FormActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity implements OnMapClickListener,
		OnMarkerClickListener ,OnMarkerDragListener {

	private GoogleMap googleMap;

	/************ Button *********************/
	Button newMap;
	Button saveMap;
	Button clearmap;
	Button editMap;
	Button dpolygon;
	
	/************* Flags ********************/
	boolean edMap = false;	
	boolean edPoly = false;
	boolean eClear = false;
	boolean C_flag = false;
	boolean C_dragflag = false;
	boolean editflg=false;
	/************* Intent ******************/
	String surveyId = "";
	String surveyShape = "";
	String extrasID = "";
	String lineColor = "";
	String iconstring = "";
	String slumId = "";
	String slumname = "";
	String SID = "";
	String flag = "";
	String compoName="";
	String SIDEdit="";
	String linename="";
	String editlinename="";
	/**************** ArrayList *************************/
	ArrayList<LatLng> ltpoints = new ArrayList<LatLng>();
	ArrayList<LatLng> demoltpoints = new ArrayList<LatLng>();
	ArrayList<LatLng> showltpoints = new ArrayList<LatLng>();
	ArrayList<LatLng> viewltpoints = new ArrayList<LatLng>();
	ArrayList<List> multiltpoints = new ArrayList<List>();
	ArrayList<MarkerOptions> markersPoints = new ArrayList<MarkerOptions>();
	
	/*****************************************/	
	PolylineOptions polyLineOptions = null;
	MarkerOptions options;
	JSONObject mydata;
	
	/*********** Pop Up Component *************************/
	EditText userInput;
	View popupView;

	
	private boolean infoWindowIsShow = false;
	private Marker lastMarker;
	/************* Drawer Window ***********************/
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;

	ArrayList<String> sections = new ArrayList<String>();
	ArrayList<String> sectionsId = new ArrayList<String>();
	/**************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Intent intent = getIntent();		
		/****************Intent from drawable Activity***********************/
		if (intent.hasExtra("surveyId")) {
			surveyId = intent.getExtras().getString("surveyId");
			getData();
			surveyShape = mydata.optString("shape");
			extrasID = mydata.optString("id");
			lineColor = mydata.optString("colour");
			iconstring=mydata.optString("icon");
			compoName=mydata.optString("name");
			
			SID = intent.getExtras().getString("SID");	
			slumname=intent.getExtras().getString("slumName");
		}

		edMap = false;
		edPoly = false;  

		try {
			// Loading map
			buttonInit();

			initilizeMap();
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

			googleMap.setMyLocationEnabled(true);			
			
			googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
				
				@Override
				public boolean onMyLocationButtonClick() {
					// TODO Auto-generated method stub

					GPSTracker mGPS = new GPSTracker(MapActivity.this);				   
				    if(mGPS.canGetLocation ){
				    mGPS.getLocation();
				   
				    googleMap.addMarker(new MarkerOptions().position(new LatLng(mGPS.getLatitude(),mGPS.getLatitude())));
					
				    }else{
				       
				        System.out.println("Unable");
				    }
					
					return false;
				}
			});
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);
			
			/**********************************************/
			// Click on Maps only when we select draw type
			if (surveyId.length() > 0) {
				
				googleMap.setOnMapClickListener(this);
			}		
			
			googleMap.setOnMapClickListener(this);
			googleMap.setOnMarkerClickListener(this);
			googleMap.setOnMarkerDragListener(this);
		
			/************* Preparing Pop up Window **********************************/
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			popupView = layoutInflater.inflate(R.layout.activity_popup, null);

			//userInput = (EditText) popupView.findViewById(R.id.editTextDialogUserInput);
			
			/*************** Intent from Edit Activity ***********************************/
			if (intent.hasExtra("slumID")) {
				SID = intent.getExtras().getString("SID");
				SIDEdit=intent.getExtras().getString("SIDEdit");
				slumId = intent.getExtras().getString("slumID");	
				slumname=intent.getExtras().getString("slumName");
				flag = intent.getExtras().getString("slumbutton");	
								
				buttonVisible(flag);
				C_dragflag = true;
			}
			
			/***********************************************************************/
			
			if(flag.equals("addnew"))
			{
				FileInputStream file = openFileInput("all_surveys.json");
				String jsonString = FormActivity.parseFileToString(file);
				JSONObject obj = new JSONObject(jsonString);
	
				JSONArray meta = obj.getJSONArray("map");			
				
				for (int i = 0; i < meta.length(); i++) {
					JSONObject name = meta.getJSONObject(i);
					
					sections.add(name.getString("name") + ' ' + name.getString("extra")
							+ ":" + name.getString("shape"));
					
					sectionsId.add(name.getString("id")+" : "+name.getString("name") + ' ' + name.getString("extra")
							+ ":" + name.getString("shape"));							
				}			
				
				Collections.sort(sections, new Comparator<String>() {
			        @Override
			        public int compare(String s1, String s2) {
			        	String[] C_s1=s1.split(":");
			        	String[] C_s2=s2.split(":");
			        	
			            return C_s1[0].trim().compareToIgnoreCase(C_s2[0].trim());
			        }
			    });	
				
				Collections.sort(sectionsId, new Comparator<String>() {
			        @Override
			        public int compare(String s1, String s2) {
			        	String[] C_s1=s1.split(":");
			        	String[] C_s2=s2.split(":");
			        	
			            return C_s1[1].trim().compareToIgnoreCase(C_s2[1].trim());
			        }
			    });	
				
		
				// Render the sidebar			 
				mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				mDrawerList = (ListView)findViewById(R.id.left_drawer);
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, sections) ;
				mDrawerList.setAdapter(adapter);
				mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
				mDrawerList.setDividerHeight(15);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
			String[] C_split=sectionsId.get(position).split(":");		
			
			surveyId=C_split[0].toString().trim();
			mDrawerList.setItemChecked(position, true);
			
			if(surveyId.length() > 0)
			{		
				
				if(ltpoints.size() > 0)
				{
					alertMessage12();
					surveyId="";
					
				}else{
				
					demoltpoints=new ArrayList<LatLng>();
					viewltpoints=new ArrayList<LatLng>();
					ltpoints=new ArrayList<LatLng>();
					multiltpoints=new ArrayList<List>();
				
				
					googleMap.clear();
					viewData();
					
					getData();
					
					surveyShape = mydata.optString("shape");
					lineColor = mydata.optString("colour");
					extrasID = mydata.optString("id");
					iconstring=mydata.optString("icon");
					compoName=mydata.optString("name");
					SIDEdit="";	
				}	
					
				
				editMap.setEnabled(true);				
				mDrawerLayout.closeDrawer(mDrawerList);
				
			}

		}
	}
		
	// Call Back method to get the Message form other Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// check if the request code is same as what is passed here it is 2
		if (requestCode == 2) {

			surveyId = data.getStringExtra("surveyId");
			
			if (surveyId.length() > 0) {
				getData();
				
				surveyShape = mydata.optString("shape");
				lineColor = mydata.optString("colour");
				extrasID = mydata.optString("id");
				iconstring=mydata.optString("icon");
				compoName=mydata.optString("name");
				SIDEdit="";	
				
				if(surveyShape.toUpperCase().contains("POLYGON"))
				{
					C_flag=true;
				}
				
				
				edMap = false;
				edPoly = false;

				editMap.setEnabled(true);
				saveMap.setEnabled(true);
				clearmap.setEnabled(true);

				googleMap.setOnMapClickListener(this);
				googleMap.setOnMarkerClickListener(this);
				
				googleMap.clear();
				ltpoints = new ArrayList<LatLng>();
				drawLines(ltpoints);
			
				viewData();
				
				multiltpoints=new ArrayList<List>();
				
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		initilizeMap();
	}

	private void buttonInit() {
		newMap = (Button) findViewById(R.id.mnew);
		newMap.setTextColor(getApplication().getResources().getColor(R.color.Green));

		editMap = (Button) findViewById(R.id.medit);
		editMap.setTextColor(getApplication().getResources().getColor(R.color.red));
		editMap.setEnabled(false);

		saveMap = (Button) findViewById(R.id.msave);
		saveMap.setTextColor(getApplication().getResources().getColor(R.color.red));
		saveMap.setEnabled(false);

		clearmap = (Button) findViewById(R.id.mclear);
		clearmap.setTextColor(getApplication().getResources().getColor(R.color.red));
		clearmap.setEnabled(false);
	}

	private void buttonVisible(String flag) {
		if (flag.equals("addnew")) {
			newMap.setVisibility(View.VISIBLE);
			editMap.setVisibility(View.VISIBLE);
			saveMap.setVisibility(View.VISIBLE);
			clearmap.setVisibility(View.VISIBLE);

			saveMap.setText("Save");
			
			viewData();

		} else if (flag.equals("show")) {
			newMap.setVisibility(View.INVISIBLE);
			editMap.setVisibility(View.INVISIBLE);
			saveMap.setVisibility(View.INVISIBLE);
			clearmap.setVisibility(View.INVISIBLE);

			saveMap.setText("Save");

			viewData();
			
		} else if (flag.equals("edit")) {
			newMap.setVisibility(View.INVISIBLE);
			editMap.setVisibility(View.VISIBLE);
			saveMap.setVisibility(View.VISIBLE);
			clearmap.setVisibility(View.VISIBLE);
			initilizeMap();
			viewData();
			editViewData();		
			editflg=false;
			saveMap.setText("Update");

			edMap = false;
			edPoly = false;

			editMap.setEnabled(true);
			saveMap.setEnabled(false);
			clearmap.setEnabled(true);

			saveMap.setTextColor(getApplication().getResources().getColor(R.color.red));
			clearmap.setTextColor(getApplication().getResources().getColor(R.color.Green));

			googleMap.setOnMapClickListener(this);
			googleMap.setOnMarkerClickListener(this);
		}		
	}

	private void initilizeMap() {
		// TODO Auto-generated method stub
		if (googleMap == null) {

			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),"Sorry! unable to create maps", Toast.LENGTH_SHORT)	.show();
			}
		}
		/********************** Add Button *****************************************/
		newMap.setVisibility(View.INVISIBLE);
		newMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MapActivity.this,
						DrawableComponent.class);
				startActivityForResult(intent, 2);
			}
		});

		/********************** Edit Button *****************************************/
		editMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (edMap == false) {
					edMap = true;
					editMap.setTextColor(getApplication().getResources()
							.getColor(R.color.Green));

				} else if (edMap == true) {
					edMap = false;
					editMap.setTextColor(getApplication().getResources()
							.getColor(R.color.red));
				}
			}

		});

		/********************** Save Button *****************************************/

		saveMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (ltpoints.size() > 0) {
					saveMap.setTextColor(getApplication().getResources().getColor(R.color.Green));
					
					//if(surveyShape.equalsIgnoreCase("POLYGON"))
					if(surveyShape.toUpperCase().contains("POLYGON"))
					{								
						if(!ltpoints.get(0).toString().equals(ltpoints.get(ltpoints.size()-1).toString()))
						{
							Toast.makeText(getApplicationContext(), "Please Draw Polygon",Toast.LENGTH_SHORT).show();
							return;
						}
					}					
					
					
					if (flag.equals("edit")) {
						linename=editlinename;
						
						if(surveyShape.toUpperCase().contains("LINESTRING"))
						{								
							if(ltpoints.size() == 1)
							{
								Toast.makeText(getApplicationContext(), "Please draw valid line, atleast two point required ",Toast.LENGTH_SHORT).show();
								return;
							}
						}
						//list not null at edit mode view : it will update record on save click
						if(surveyShape.toUpperCase().contains("POLYGON"))
						{
							if(ltpoints.size() > 0)
							{
								popupwindow(popupView, userInput);
							}else
							{
								SaveDataInDatabase("");
								
							}
							
						}else
						{
							SaveDataInDatabase("");
							backToEdit();
						}
						
						
						
					}else if (flag.equals("addnew")){
						if(!surveyShape.toUpperCase().contains("POLYGON"))
						{		
							if(surveyShape.toUpperCase().contains("LINESTRING"))
							{								
								if(ltpoints.size() == 1)
								{
									Toast.makeText(getApplicationContext(), "Please draw valid line, atleast two point required",Toast.LENGTH_SHORT).show();
									return;
								}
							}
							SaveDataInDatabase("");							
							
						}else
						{
							if(ltpoints.size() > 0)
							{
								popupwindow(popupView, userInput);
							}else
							{
								SaveDataInDatabase("");								
							}
							
						}			
					}
					else {
						if(ltpoints.size() > 0)
						{
							popupwindow(popupView, userInput);
						}else
						{
							SaveDataInDatabase("");							
						}
					}
				} else {
					//list is null edit mode view : it will delete record on save click
					if (flag.equals("edit")) {		
						linename=editlinename;
						
						if(surveyShape.toUpperCase().contains("POLYGON"))
						{							
							if(ltpoints.size() > 0)
							{
								popupwindow(popupView, userInput);
							}else
							{
								SaveDataInDatabase("");								
							}
						}else
						{
							SaveDataInDatabase("");
							
						}						
											
					}else if (flag.equals("addnew")){
						if(!surveyShape.toUpperCase().contains("POLYGON"))
						{						
							SaveDataInDatabase(""); 							
							
						}else
						{
							if(ltpoints.size() > 0)
							{
								popupwindow(popupView, userInput);
							}else
							{
								SaveDataInDatabase("");								
							}
						}
					}
					else {
						if(ltpoints.size() > 0)
						{
							popupwindow(popupView, userInput);
						}else
						{
							SaveDataInDatabase("");							
						}
					}
				}			
			}
		});

		/********************** Clear Button *****************************************/
		clearmap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							// Yes button clicked
							if (flag.equals("edit")) {
								
								eClear = true;
								googleMap.clear();
								
								saveMap.setEnabled(true);
								saveMap.setTextColor(getApplication().getResources().getColor(R.color.Green));
																								
								viewData();
								demoltpoints=new ArrayList<LatLng>();
								ltpoints = new ArrayList<LatLng>();

							} else {
								eClear = false;
								googleMap.clear();

								saveMap.setTextColor(getApplication().getResources().getColor(R.color.red));
								saveMap.setEnabled(false);

								ltpoints = new ArrayList<LatLng>();
								drawLines(ltpoints);
								
								viewData();
							}
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							// No button clicked				
							break;					
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
				builder.setMessage("Are you sure, you want to clear drawn component? ")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();				
				
			}

		});

		/********************** Ploygon Button *****************************************/

		dpolygon = (Button) findViewById(R.id.mpolygon);
		
		if (!surveyShape.toUpperCase().contains("POLYGON")) {
			dpolygon.setVisibility(View.INVISIBLE);
			if (surveyShape.equalsIgnoreCase("LINESTRING"))
			{
				//dpolygon.setVisibility(View.VISIBLE);
				dpolygon.setText("Finish");	
				dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));
				dpolygon.setEnabled(false);
				
				if (flag.equals("edit")) {
					dpolygon.setVisibility(View.INVISIBLE);
				}
				
			}
			
		} else if (surveyShape.toUpperCase().contains("POLYGON")) {

			dpolygon.setVisibility(View.VISIBLE);
			dpolygon.setText("POLYGON");
			dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));
			
		}
		
		dpolygon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				//one condition for Mulyiple line drawn on map 
				//add all lat long in list and that list in multiltpoint
				//initilaze ltpoint and demoltpoint
				if(dpolygon.getText().toString().equalsIgnoreCase("Finish"))
				{
					multiltpoints.add(ltpoints);					
					ltpoints=new ArrayList<LatLng>();
					demoltpoints=new ArrayList<LatLng>();
					
					dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));
					dpolygon.setEnabled(false);
				
				}else
				{
					if (edPoly == false) {
						edPoly = true;
						dpolygon.setTextColor(getApplication().getResources().getColor(R.color.Green));

						// Disable Edit Button
						//editMap.setEnabled(false);
						edMap = false;
						editMap.setTextColor(getApplication().getResources().getColor(R.color.red));

					} 
					else if (edPoly == true) {
					//else {
						edPoly = false;
						dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));

						// Disable Edit Button
						editMap.setEnabled(true);
						edMap = true;
						editMap.setTextColor(getApplication().getResources().getColor(R.color.Green));
					}
				}
				
				
			}
		});
		/******************************************************************************/
	}

	/************************* implements methods **************************************************/
	/************************* Code for Latitude and Longitude **************************************************/
	
	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

		if(surveyId.length() > 0)
		{	
			
			
			/***********************************************/
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));
	
			options = new MarkerOptions();		
			
			ltpoints.add(point);
	
			markersPoints.add(options);
			
			if (flag.equals("edit")) {
				
				saveMap.setEnabled(true);
				saveMap.setTextColor(getApplication().getResources().getColor(R.color.Green));
				
				if(demoltpoints.size() > 0){
					
					ltpoints = new ArrayList<LatLng>();
					ltpoints = demoltpoints;	
								
					if (surveyShape.equalsIgnoreCase("POINT") && (ltpoints.size() > 1))
					{
						ltpoints.remove(ltpoints.size()-1);
					}
					
					if(surveyShape.toUpperCase().contains("POLYGON"))
					{
						if(editflg == false)
						{
							googleMap.clear();
							viewData();
							editflg =true;
						}
						//drawLines(ltpoints);
					}
				}			
			}
			
			if(ltpoints.size() > 0)
			{			
				drawLines(ltpoints);
			}
		}else
		{
			Toast.makeText(getApplicationContext(), "Please Select Drawn Component", Toast.LENGTH_SHORT).show();
		}		

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		
		if (flag.equals("edit")) {
			
			if(demoltpoints.size() > 0){
				ltpoints = new ArrayList<LatLng>();
				ltpoints = demoltpoints;	
			}
		}
		
		if (edMap == true) {			
			ltpoints.remove(marker.getPosition());

			options.position(marker.getPosition());
			markersPoints.remove(options);

			googleMap.clear();
			drawLines(ltpoints);
		}else{
			
			/************* Marker info show and hide **********************/
			
			if(lastMarker == null){
	            marker.showInfoWindow();
	            lastMarker = marker;
	            infoWindowIsShow=true;
	        }else
	        if (marker.getId().equals(lastMarker.getId())) {
	            if (infoWindowIsShow) {
	                marker.hideInfoWindow();
	                infoWindowIsShow = false;
	            } else {
	                marker.showInfoWindow();
	                infoWindowIsShow = true;
	            }
	        }
	        else{
	           
	            if (infoWindowIsShow) {
	                lastMarker.hideInfoWindow();
	              
	                marker.showInfoWindow();
	                infoWindowIsShow = true;
	                lastMarker = marker;
	            } else {
	                marker.showInfoWindow();
	                infoWindowIsShow = true;
	                lastMarker = marker;
	            }
	        }
			
			/*************************/
		}

		if (edPoly == true) {				
			ltpoints.add(marker.getPosition());	
			drawLines(ltpoints);
			
			//edMap = false;
			C_flag=true;
		}
					
		if (flag.equals("addnew") || flag.equals("edit") ) {			
			viewData();			
		}
		
		if(multiltpoints.size() > 0)
		{
			multilineViewData();						
		}					
		
		return true;
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub	
		
		if (!flag.equals("show")) {
			String[] m_name=marker.getTitle().split(",");
			
			if (flag.equals("edit")) {
				
				if(demoltpoints.size() > 0){
					ltpoints = new ArrayList<LatLng>();
					ltpoints = demoltpoints;
				}else
				{
					ltpoints.add(marker.getPosition());
				}							
			}
	
			ltpoints.set((Integer.parseInt(m_name[0])-1), marker.getPosition());
			
			googleMap.clear();
			viewData();
			/****************** for multiple line drawn on map ********************************************
			if(multiltpoints.size() > 0)
			{
				multilineViewData();						
			}
			C_dragflag=true;			
			
			/***************************************************/
			drawLines(ltpoints);
		
		}
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}	
	
	/*********************** OnBack Button *************************************/
	@Override
	public void onBackPressed() {

		if(surveyShape.toUpperCase().contains("POLYGON"))
		{	
			if(ltpoints.size() > 0)
			{
				if(!ltpoints.get(0).toString().equals(ltpoints.get(ltpoints.size()-1).toString()))
				{
					Toast.makeText(getApplicationContext(), "Please Draw Polygon",Toast.LENGTH_SHORT).show();
					return;
				}
			}			
		}
		
		if(flag.equals("addnew"))
		{
			if(ltpoints.size() > 0)
			{
				alertMessage12();
			}else
			{
				backToEdit();
			}
		}else
		{
			backToEdit();
		}
		
				
		
		return;
	}

	public void backToEdit()
	{		
		
		Intent intent = new Intent(getApplicationContext(), EditMap.class);

		intent.putExtra("slumID", slumId);
		intent.putExtra("slumName", slumname);
		startActivity(intent);
		// call finish it will finish this activity : Avoid open activity
		// multiple time
		finish();
	}

	/************************* Draw Shape Methods *************************************/

	public void drawLines(ArrayList<LatLng> ltpoints) {

		if (!surveyShape.equalsIgnoreCase("POINT")) {
				polyLineOptions = new PolylineOptions();
				//Added This condition for editing polygon in edit mode.
				//first first and last join will be join still we delete marker
				//Now at edit mode,first and last point remove connected line
				if (flag.equals("edit") && surveyShape.toUpperCase().contains("POLYGON"))
				{
					if(C_flag == true)
					{						
						ltpoints.remove(ltpoints.size()-1);
						polyLineOptions.addAll(ltpoints);

						C_flag=false;
					}
					else if(C_flag==false)
					{						
						polyLineOptions.addAll(ltpoints);
					}

				}
				else
				{
					polyLineOptions.addAll(ltpoints);
				}			
				
				polyLineOptions.width(2);
				
				polyLineOptions.color(Color.parseColor("#" + lineColor));
	
				googleMap.addPolyline(polyLineOptions);
		}
		
		/***************** icon of marker : iconstring remain fill with before acces marker icon
		 ***********************************/
		iconstring="";
		getData();
		iconstring=mydata.optString("icon");
		lineColor = mydata.optString("colour");
		/************************************************/
		
		
		if (edPoly != true) {			

			options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			
			for (int i = 0; i < ltpoints.size(); i++) {				
				if (!flag.equals("show"))
				{
					options.position(ltpoints.get(i)).draggable(true);	
				}else{
					options.position(ltpoints.get(i)).draggable(false);
				}
					
				options.title((i+1)+",");
				googleMap.addMarker(options);
			}
		} else {
			edPoly = false;
			dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));
			//Condition For Filling Polygon 
			if(ltpoints.get(0).equals(ltpoints.get(ltpoints.size()-1)) && surveyShape.equalsIgnoreCase("POLYGON"))
			{
				googleMap.addPolygon(new PolygonOptions().addAll(ltpoints).strokeColor(Color.parseColor("#"+lineColor)).strokeWidth(0.2f).fillColor(Color.parseColor("#"+lineColor)));	
				
			}else if(ltpoints.get(0).equals(ltpoints.get(ltpoints.size()-1)) && surveyShape.equalsIgnoreCase("POLYGONBORDER"))
			{
				googleMap.addPolygon(new PolygonOptions().addAll(ltpoints).strokeColor(Color.parseColor("#"+lineColor)).strokeWidth(0.2f));				
				
			}
			
		}

		if (ltpoints.size() > 0) {
			initilizeMap();
			saveMap.setTextColor(getApplication().getResources().getColor(
					R.color.Green));
			saveMap.setEnabled(true);

			clearmap.setTextColor(getApplication().getResources().getColor(
					R.color.Green));
			clearmap.setEnabled(true);

			//if(dpolygon.getText().toString().equalsIgnoreCase("FINISH"))
			if (surveyShape.equalsIgnoreCase("LINESTRING"))
			{
				if (ltpoints.size() > 1)
				{
					dpolygon.setTextColor(getApplication().getResources().getColor(R.color.Green));
					dpolygon.setEnabled(true);
				}else
				{
					saveMap.setTextColor(getApplication().getResources().getColor(
							R.color.red));
					saveMap.setEnabled(false);
				}
			}
			
			
		} else {
			if (flag.equals("edit")) {
				initilizeMap();
				saveMap.setTextColor(getApplication().getResources().getColor(
						R.color.Green));
				saveMap.setEnabled(true);

				clearmap.setTextColor(getApplication().getResources().getColor(
						R.color.Green));
				clearmap.setEnabled(true);

			} else {				
				
				saveMap.setTextColor(getApplication().getResources().getColor(
							R.color.red));
				saveMap.setEnabled(false);			

				clearmap.setTextColor(getApplication().getResources().getColor(
						R.color.red));
				clearmap.setEnabled(false);
			}

		}
		
		/**********************************************
		if(multiltpoints.size() > 0)
		{
			saveMap.setTextColor(getApplication().getResources().getColor(
					R.color.Green));
			saveMap.setEnabled(true);					
			
			clearmap.setTextColor(getApplication().getResources().getColor(
					R.color.Green));
			clearmap.setEnabled(true);
			
			if (ltpoints.size() > 1)
			{
				dpolygon.setTextColor(getApplication().getResources().getColor(R.color.Green));
				dpolygon.setEnabled(true);
				
				saveMap.setTextColor(getApplication().getResources().getColor(
						R.color.Green));
				saveMap.setEnabled(true);				
				
			}else
			{
				dpolygon.setTextColor(getApplication().getResources().getColor(R.color.red));
				dpolygon.setEnabled(false);			
			}
		}
		
		/************************************************/		
	}

	public void viewDrawLines(ArrayList<LatLng> ltpoints) {

		polyLineOptions = new PolylineOptions();
		polyLineOptions.addAll(ltpoints);
		polyLineOptions.width(2);
		polyLineOptions.color(Color.BLUE);
		googleMap.addPolyline(polyLineOptions);		
		
		double lat = 0.0;
		double lng = 0.0;

		
		for (int i = 0; i < ltpoints.size(); i++) {

			options = new MarkerOptions();
			if (!flag.equals("show"))
			{
				options.position(ltpoints.get(i)).draggable(true);	
			}else{
				options.position(ltpoints.get(i)).draggable(false);	
			}
	
			
			lat= options.getPosition().latitude;
			lng = options.getPosition().longitude;
		}
		
	}
	
	public void viewDrawLines12(ArrayList<LatLng> ltpoints, String color,String shape, String name,boolean C_ind) {

		if (!shape.equalsIgnoreCase("POINT")) {
			polyLineOptions = new PolylineOptions();
			polyLineOptions.addAll(ltpoints);
			polyLineOptions.width(2);
			polyLineOptions.color(Color.parseColor("#" + color));
			googleMap.addPolyline(polyLineOptions);
		}
		
		if(shape.equalsIgnoreCase("POLYGON"))
		{
			//C_flag=true : for first time remove connection of polygon,between first and last.
			C_flag=true;
			
			//For fill Polygon
			googleMap.addPolygon(new PolygonOptions().addAll(ltpoints).strokeColor(Color.parseColor("#"+color)).strokeWidth(0.2f).fillColor(Color.parseColor("#"+color)));
		}
		else if(shape.equalsIgnoreCase("BORDERPOLYGON"))
		{
			//C_flag=true : for first time remove connection of polygon,between first and last.
			C_flag=true;
			
			//For fill Polygon
			googleMap.addPolygon(new PolygonOptions().addAll(ltpoints).strokeColor(Color.parseColor("#"+color)).strokeWidth(0.2f));
		}
		double lat = 0.0;
		double lng = 0.0;

		for (int i = 0; i < ltpoints.size(); i++) {

			if (flag.equals("show") || flag.equals("addnew"))
			{
				options = new MarkerOptions().position(	new LatLng(ltpoints.get(i).latitude, ltpoints.get(i).longitude))  
						.title((i+1)+","+name)  	//title of marker
						.alpha(0.7f)  	//transparency of marker
						.flat(true)		//orientation : view flat
						.draggable(false);
				// Marker color icon
				if(iconstring.length() > 0)
				{
					byte[] bytes = Base64.decode(iconstring,Base64.DEFAULT) ;			
					Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);			
					BitmapDescriptor bmf = BitmapDescriptorFactory.fromBitmap(bitmap);
					options.icon(bmf);
				}else
				{
					options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));				
				}				
			}			
			else
			{
				options = new MarkerOptions().position(	new LatLng(ltpoints.get(i).latitude, ltpoints.get(i).longitude))  
						.title((i+1)+","+name)  	//title of marker
						.alpha(0.7f)  	//transparency of marker
						.flat(true)	;	//orientation : view flat
						//.draggable(true);
				
				if(iconstring.length() > 0)
				{
					byte[] bytes = Base64.decode(iconstring,Base64.DEFAULT) ;			
					Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);			
					BitmapDescriptor bmf = BitmapDescriptorFactory.fromBitmap(bitmap);
					options.icon(bmf);
					Canvas canvas=new Canvas();
					
				}else
				{
					options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));				
				}					
			}				
						
			lat = options.getPosition().latitude;
			lng = options.getPosition().longitude;
			
			if(C_ind==true)
			{
				googleMap.addMarker(options);
			}
			
			if(shape.equalsIgnoreCase("POINT"))
			{
				googleMap.addMarker(options);
			}		
		
		}
	}

	/*************************** JSON Code ***********************************/

	public void getData() {
		// Parse JSON
		try {
			FileInputStream file = openFileInput("all_surveys.json");
			String jsonString = FormActivity.parseFileToString(file);
			JSONObject obj = new JSONObject(jsonString);

			JSONArray meta = obj.getJSONArray("map");

			mydata = new JSONObject();
			for (int i = 0; i < meta.length(); i++) {
				JSONObject name = meta.getJSONObject(i);
				if (name.getString("id").equalsIgnoreCase(surveyId)) {
					mydata = name;
				}
			}

		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void viewData() {
		try {

			/******************************* Slum Border ******************************************/
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());

			FileInputStream file = openFileInput("all_surveys.json");
			String jsonString = FormActivity.parseFileToString(file);
			JSONObject obj = new JSONObject(jsonString);

			JSONObject meta = obj.getJSONObject("slum_map");

			String scode = meta.getString(slumId);

			JSONObject obj12 = new JSONObject(scode);

			JSONArray cordinate = obj12.getJSONArray("coordinates");

			String strCor = cordinate.toString().replace(" ", "")
					.replace(",", " ").replace("] [", ",").replace("[", "")
					.replace("]", "");

			String[] C_split = strCor.split(",");

			String[] val = null ;
			viewltpoints = new ArrayList<LatLng>();
			for (int i = 0; i < C_split.length; i++) {

				val = C_split[i].split(" ");

				LatLng allLatLng = new LatLng(Double.parseDouble(val[1].toString()), 
											Double.parseDouble(val[0].toString()));
				viewltpoints.add(allLatLng);
			}
			
			if(C_dragflag==false){
				CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(val[1].toString()),Double.parseDouble(val[0].toString()))).zoom(19).build();

				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
			//C_dragflag=false;
			
			viewDrawLines(viewltpoints);
			/*************************************************************************/
			getCoordinateArrays();		

			/**************************** Design On Slum ***************************************/

			db = new DatabaseHandler(getApplicationContext());

			ArrayList<List<String>> tbl = new ArrayList<List<String>>();
			JSONArray meta12 = obj.getJSONArray("map");

			String lin_color = "";
			String lin_shape = "";
			String lin_name = "";

			String[] C_drawn=null;
			
			if( SID == null)
			{
				return;
			}else
			{		
				if(SIDEdit == null)
				{
					SIDEdit="";
				}
				
				SID=SID.replace("[[", "").replace("]]", "");
				if(SIDEdit.length() > 0 )
				{
					String aa =SID.replace(SIDEdit , "").trim();
					//Toast.makeText(getApplicationContext(), aa+"", Toast.LENGTH_SHORT).show();
					C_drawn = aa.split(",");
				}else
				{
					C_drawn = SID.split(",");
				}				
			}			

			//iconstring="";
			// For loop primary key split
			for (int m = 0; m < C_drawn.length; m++) {
				//getting data from sqlite database : passing 4 parameter are ( table name, condition field, condition, order by)
		
				tbl = db.getAllDataList(db.TABLE_PLOTTEDSHAPE_GROUP,db.KEY_PK,C_drawn[m].toString(),"");

				
				
				for (int j = 0; j < tbl.size(); j++) {

					/*************************/
					for (int i = 0; i < meta12.length(); i++) {
						JSONObject name = meta12.getJSONObject(i);
						if (name.getString("id").equalsIgnoreCase(tbl.get(j).get(2).toString())) {
							lin_color = name.getString("colour");
							lin_shape = name.getString("shape");
							if(name.getString("icon").length() > 0)
							{
								iconstring= name.getString("icon");
							}else
							{
								iconstring=""; 
							}
							break;
						}
					}

					lin_name = tbl.get(j).get(3).toString();
					if(!flag.equals("addnew"))
					{
						linename=lin_name;
					}else
					{
						linename="";
					}
					
					/*********************/

					String latlangarray = tbl.get(j).get(5).toString()
							.replace(" ", "").replace(",", " ")
							.replace(") (", ",").replace("(", "")
							.replace(")", "");

					String[] C_sp = latlangarray.split(",");

					
					showltpoints = new ArrayList<LatLng>();
					
					//showltpoints = new ArrayList<LatLng>();
					for (int i = 0; i < C_sp.length; i++) {

						val = C_sp[i].split(" ");
						LatLng allLatLng = new LatLng(Double.parseDouble(val[0]	.toString()), 
													Double.parseDouble(val[1].toString()));
						
							
							showltpoints.add(allLatLng);					
					}

					if (eClear == true) {
						//showltpoints = new ArrayList<LatLng>();
						demoltpoints = new ArrayList<LatLng>();
						//ltpoints = new ArrayList<LatLng>();
						//ltpoints = showltpoints;
						viewDrawLines12(showltpoints, lin_color, lin_shape,lin_name,false);
					} else {					
						
						viewDrawLines12(showltpoints, lin_color, lin_shape,lin_name,false);		
						//drawLines(showltpoints);
						
					}
				}
			}
			/******************************************/

		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void editViewData() {

		try {

			DatabaseHandler db = new DatabaseHandler(getApplicationContext());

			FileInputStream file = openFileInput("all_surveys.json");
			String jsonString = FormActivity.parseFileToString(file);
			JSONObject obj = new JSONObject(jsonString);
			String[] val = null ;
				

			db = new DatabaseHandler(getApplicationContext());

			ArrayList<List<String>> tbl = new ArrayList<List<String>>();
			JSONArray meta12 = obj.getJSONArray("map");

			String lin_color = "";
			String lin_shape = "";
			String lin_name = "";

			String[] C_drawn=null;
			if( SID == null)
			{
				return;
			}
			else
			{
				//SIDEdit variable use come for edit winow by clicking edit button 
				//which Slum id we click we get from SID it is an intent.
				
				if(SIDEdit.length() > 0)
				{
					C_drawn=SIDEdit.split(",");			
				}else
				{
					C_drawn = SID.split(",");
				}		
								
			}			
			//iconstring="";
			// For loop primary key split
			for (int m = 0; m < C_drawn.length; m++) {
				//getting data from sqlite database : passing 4 parameter are ( table name, condition field, condition, order by)
				tbl = db.getAllDataList(db.TABLE_PLOTTEDSHAPE_GROUP,db.KEY_PK,C_drawn[m].toString(),"");

				for (int j = 0; j < tbl.size(); j++) {

					/*************************/
					for (int i = 0; i < meta12.length(); i++) {
						JSONObject name = meta12.getJSONObject(i);
						if (name.getString("id").equalsIgnoreCase(tbl.get(j).get(2).toString())) {
							lin_color = name.getString("colour");
							lin_shape = name.getString("shape");
							if(name.getString("icon").length() > 0)
							{
								iconstring= name.getString("icon");
							}else
							{
								iconstring="";
							}
							break;
						}
					}

					lin_name = tbl.get(j).get(3).toString();
					 //linename= lin_name;
					editlinename=lin_name;
					/*********************/

					String latlangarray = tbl.get(j).get(5).toString()
							.replace(" ", "").replace(",", " ")
							.replace(") (", ",").replace("(", "")
							.replace(")", "");

					String[] C_sp = latlangarray.split(",");

					demoltpoints = new ArrayList<LatLng>();
					for (int i = 0; i < C_sp.length; i++) {

						val = C_sp[i].split(" ");
						LatLng allLatLng = new LatLng(Double.parseDouble(val[0]	.toString()), 
													Double.parseDouble(val[1].toString()));
						demoltpoints.add(allLatLng);
					}

					if (eClear == true) {
						demoltpoints = new ArrayList<LatLng>();
						ltpoints = new ArrayList<LatLng>();	
						
					} else {						
						viewDrawLines12(demoltpoints, lin_color, lin_shape,lin_name,true);
						//googleMap.clear();
						//viewData();
						drawLines(demoltpoints);
					}
				}
			}
			/******************************************/

		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
				
	public void multilineViewData() {
		//this function to display all component we drawn with help of 
		//finish button click 
		//finish button insert all lat long in multiltpoint Array list
		try {

			ArrayList<LatLng> multiltpoints12=new ArrayList<LatLng>();
			for (int i = 0; i < multiltpoints.size(); i++) {
				multiltpoints12 = new ArrayList<LatLng>();
				multiltpoints12.addAll(multiltpoints.get(i));
				viewDrawLines12(multiltpoints12, lineColor, surveyShape, "",false);						
			}
			
		} catch (Exception e) {
			Log.e("MultiLine Error", "Error parsing data " + e.toString());
		} 
	}
	
	/*************************** POP UP And Save Function ***********************************/

	public void popupwindow(final View popupView, final EditText userInput) {
		
		final Dialog pwCreateAccount = new Dialog(MapActivity.this);
		pwCreateAccount.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pwCreateAccount.setContentView(R.layout.activity_popup);
		final Button okbtn = (Button) pwCreateAccount.findViewById(R.id.btn_OK_popup);
		final Button closebtn = (Button) pwCreateAccount.findViewById(R.id.btn_close_popup);
		final EditText txtInfo = (EditText)pwCreateAccount.findViewById(R.id.editTextDialogUserInput);
		okbtn.setTextColor(Color.GREEN);
		txtInfo.setText(linename);		
		okbtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!txtInfo.getText().toString().trim().equals("")) {

					SaveDataInDatabase(txtInfo.getText().toString().trim());
					linename="";
					txtInfo.setText(linename);
					pwCreateAccount.dismiss();
					if(flag.equals("edit"))
					{
						backToEdit();
					}
													
				}	
				else
				{
					Toast.makeText(MapActivity.this,
							"Please Enter Name ", Toast.LENGTH_SHORT).show();
				}
			}
		});
		


		closebtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pwCreateAccount.dismiss();
				txtInfo.setText("");
			}
		});
;
		pwCreateAccount.show();
	}

	private void SaveDataInDatabase(String popupText) {
		// TODO Auto-generated method stub	
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());

		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = df.format(c.getTime());
		
		try {		
			String latlongData = "";
			
			ArrayList<List<String>> tbl = new ArrayList<List<String>>();			
			tbl = db.getComponentName(slumId,extrasID);
			
			String header_name="";
			String header_num="";
			
			//for automatic nameing of drawn component
			//only polygon have to give manually name 
			if(!surveyShape.toUpperCase().contains("POLYGON"))
			 {
				if(tbl.size() > 0)
				{
					header_name =tbl.get(0).get(0).toString().trim().substring(0,1);
					try
					{
						header_num =tbl.get(0).get(0).toString().trim().substring(1,(tbl.get(0).get(0).toString().trim().length()));
						if(header_num.equals(""))
						{
							header_num ="0";
						}else{
							header_num=Integer.parseInt(header_num)+"";
						}
					}catch(NumberFormatException nu)
					{
						header_num ="0";
					}
				}else
				{				
					header_name =compoName.trim().substring(0,1);
					header_num ="0";
				}
			 }		  
			
			/*****************************************************************************/
			if (flag.equals("edit")) {
				
				for (int i = 0; i < ltpoints.size(); i++) {
					
					latlongData = latlongData.trim()+ ", "+ ltpoints.get(i).toString().trim().replace("lat/lng:", "").replace(" ", "");					
				}

				// condition for check data is updated or Deleted
				if (latlongData.length() > 0) {
					
					if(SIDEdit.length() > 0)
					{
						db.updatePlottedShapeGroup(SIDEdit,latlongData.substring(1, latlongData.length()) , popupText);
					}else{
						db.updatePlottedShapeGroup(SID,latlongData.substring(1, latlongData.length()), popupText);
					}
					Toast.makeText(getApplicationContext(), "Data Updated ",Toast.LENGTH_SHORT).show();
									
				} else {
					
					if(SIDEdit.length() > 0)
					{
						alertMessage(SIDEdit, latlongData);
					}else
					{
						alertMessage(SID, latlongData);
					}						
				}
			} else {
				
				if (!surveyShape.equalsIgnoreCase("POINT")) {

					//Condition When multiple line drawn of map					
					if(multiltpoints.size() > 0)
					{
						if(ltpoints.size() > 0)
						{
							multiltpoints.add(ltpoints);							
						}
						
						for (int i = 0; i < multiltpoints.size(); i++) {
							latlongData="";
							for (int j = 0; j < multiltpoints.get(i).size(); j++) {
								
								latlongData = latlongData.trim()+ ", "+ multiltpoints.get(i).get(j).toString().trim()
												.replace("lat/lng:", "")
												.replace(" ", "");							
							}
							
							db.insertPlottedShapeGroup(slumId, extrasID,header_name+(Integer.parseInt(header_num)+(i+1)), formattedDate,
									latlongData.substring(1, latlongData.length()));
							
							SID=SID +","+ db.getId(slumId, extrasID, header_name+(Integer.parseInt(header_num)+(i+1)), formattedDate,
									latlongData.substring(1, latlongData.length()));
						}
						
					}
					//when normal line and polygon drawn 
					else{
						
						for (int i = 0; i < ltpoints.size(); i++) {
							
							latlongData = latlongData.trim()+ ", "+ ltpoints.get(i).toString().trim()
											.replace("lat/lng:", "")
											.replace(" ", "");								
						}
						
						if(surveyShape.toUpperCase().contains("POLYGON"))
						 {
							 db.insertPlottedShapeGroup(slumId, extrasID,popupText, formattedDate,
										latlongData.substring(1, latlongData.length()));
							 
							 SID=SID +","+ db.getId(slumId, extrasID,popupText, formattedDate,
										latlongData.substring(1, latlongData.length()));
							 
						 }else{
							 db.insertPlottedShapeGroup(slumId, extrasID,header_name+(Integer.parseInt(header_num)+1), formattedDate,
										latlongData.substring(1, latlongData.length()));
							 
							 SID=SID +","+ db.getId(slumId, extrasID, header_name+(Integer.parseInt(header_num)+1), formattedDate,
										latlongData.substring(1, latlongData.length()));
						 }
						
						
					}		
				} else {
					for (int i = 0; i < ltpoints.size(); i++) {
						
						
						db.insertPlottedShapeGroup(slumId,extrasID,header_name+(Integer.parseInt(header_num)+(i+1)),formattedDate,
								ltpoints.get(i).toString().trim().replace("lat/lng:", "").replace(" ", ""));	
						
						SID=SID +","+ db.getId(slumId,extrasID,header_name+(Integer.parseInt(header_num)+(i+1)),formattedDate,
								ltpoints.get(i).toString().trim().replace("lat/lng:", "").replace(" ", ""));
					}
				}
				
				
				demoltpoints=new ArrayList<LatLng>();
				viewltpoints=new ArrayList<LatLng>();
				ltpoints=new ArrayList<LatLng>();
				multiltpoints=new ArrayList<List>();
				
				
				googleMap.clear();
				viewData();
				
				
				Toast.makeText(getApplicationContext(), "Data Save ",Toast.LENGTH_SHORT).show();				
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/*************************** Confirmation Window On Delete record ***********************************************/
	public void alertMessage(final String pk_id, final String latlong) {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());

					db.updatePlottedShapeGroup(pk_id, latlong , "");
					Toast.makeText(getApplicationContext(), "Data Deleted",Toast.LENGTH_SHORT).show();
					backToEdit();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked				
					break;					
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure, you want to delete?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}
	
	
	public void alertMessage12() {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					SaveDataInDatabase("");
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked				
					break;					
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure, you want to save data?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}
	
	/***************************** Kml File Read And Display On Map **********************************************/
	
	public ArrayList<ArrayList<LatLng>> getCoordinateArrays() {
	    ArrayList<ArrayList<LatLng>> allTracks = new ArrayList<ArrayList<LatLng>>();

	    try {
	        StringBuilder buf = new StringBuilder();
	        File dir = Environment.getExternalStorageDirectory();  
	        //File f_path = new File("storage/sdcard0/shelterkml/" + slumname + ".kml");
	        File f_path = new File(dir,"/shelterkml/" + slumname + ".kml");
	        
	        // InputStream json = getApplication().getAssets().open("LegendKML.kml");
	        
	        InputStream json = new BufferedInputStream(new FileInputStream(f_path));
	        
	        BufferedReader in = new BufferedReader(new InputStreamReader(json));
	        String str;
	        String buffer;
	        while ((str = in.readLine()) != null) {
	            buf.append(str);
	        }

	        in.close();
	        String html =buf.toString();
	        Pattern pattern = Pattern.compile("<coordinates>(.*?)</coordinates>");
	        Matcher matcher = pattern.matcher(html);
	        String[] val = null ;
	        while (matcher.find()) {
	           // System.out.println(matcher.group(1));
	            
	            String[] C_sp = matcher.group(1).toString().trim().split(" ");

				
	            ArrayList<LatLng> kmlpoints = new ArrayList<LatLng>();
				int k=1;
				String lat="";
				String lang="";
				for (int i = 0; i < C_sp.length; i++) {

					String[] C_sp1 = C_sp[i].toString().trim().split(",");
					
					for (int j = 0; j < C_sp1.length; j++) {
						if(k==1)
						{
							lat=C_sp1[j].toString();
							k++;
						}
						else if(k==2){
							lang=C_sp1[j].toString();
							
							LatLng allLatLng = new LatLng(Double.parseDouble(lang), 
									Double.parseDouble(lat));
							kmlpoints.add(allLatLng);
							k++;
							
						}else if(k==3)
						{
							k=1;
						}							
					}							
				}			
				viewDrawLines(kmlpoints);				
	        }
	        

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return allTracks;
	}
	
	
	/*************************** The End ***********************************/	
	
}
