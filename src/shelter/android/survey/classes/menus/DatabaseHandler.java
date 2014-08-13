package shelter.android.survey.classes.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shelter.android.survey.classes.forms.FormActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Survey";
	public static final String TABLE_FACTS = "facts";
	public static final String TABLE_SURVEYS = "surveys";
	public static final String TABLE_IMAGES = "images";
	public static final String TABLE_PLOTTEDSHAPE="plottedShape";
	public static final String TABLE_PLOTTEDSHAPE_GROUP="plottedShape_group";
	
	
	public static final String KEY_PK = "pk";
	public static final String KEY_QID = "qid";
	public static final String KEY_FK_FACTS = "facts_fk";
	public static final String KEY_FACT = "fact";
	public static final String KEY_SURVEY = "survey";
	public static final String KEY_COMPLETE = "complete";
	public static final String KEY_SUBSECTION = "subSection";

	public static final String KEY_SURVEYID = "surveyid";
	public static final String KEY_HOUSEHOLD = "household";
	public static final String KEY_SLUMID = "slumid";
	
	public static final String KEY_LAT = "lattitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_BITMAP = "bitmap";

	public static final String KEY_DRAWABLE_COMPONENT = "drawablecomponent";
	public static final String KEY_NAME = "name";
	public static final String KEY_STATUS = "status";
	public static final String KEY_CREAT_DATE = "creatdate";
	public static final String KEY_ORDER = "orders";
	public static final String KEY_FK_PLOTTEDSHAPE_GROUP = "plottedshapegroup_fk";
	public static final String KEY_LATLONG = "latlong";
	public static final String KEY_Code = "codes";

	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_SURVEYS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SURVEYS
				+"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				KEY_SURVEYID + " TEXT,"+
				KEY_SLUMID + " INTEGER," +
				KEY_HOUSEHOLD + " TEXT," +
				KEY_COMPLETE + " INTEGER " + ")";
		db.execSQL(CREATE_SURVEYS_TABLE);
		String CREATE_FACT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FACTS +
				"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_QID + " STRING, " +
				KEY_FACT + " STRING, " +
				KEY_SURVEY + " INTEGER," +
				KEY_SUBSECTION + " INTEGER," +
				" FOREIGN KEY ("+KEY_SURVEY+") REFERENCES "+TABLE_SURVEYS+
				" ("+KEY_PK+"));";
		db.execSQL(CREATE_FACT_TABLE);
		
		//Code added by SC : To insert photo related information.
		String CREATE_IMAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES
				+"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				KEY_FK_FACTS + " INTEGER," +
				KEY_LAT + " TEXT," +
				KEY_BITMAP + " TEXT," +
				 KEY_LONGITUDE+" TEXT," +
				" FOREIGN KEY ("+KEY_FK_FACTS+") REFERENCES "+TABLE_FACTS+
				" ("+KEY_PK+"));";
		db.execSQL(CREATE_IMAGES_TABLE);
				
			/***************** Sitaram 2014-May-06********/
		
		String CREATE_PLOTTEDSHAP_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PLOTTEDSHAPE_GROUP
				+"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				KEY_SLUMID + " INTEGER," +
				KEY_DRAWABLE_COMPONENT + " TEXT," +
				KEY_NAME + " TEXT," +				
				KEY_CREAT_DATE + " TEXT," +
				KEY_LATLONG + " TEXT" +				
				");";
				
		db.execSQL(CREATE_PLOTTEDSHAP_GROUP_TABLE);
		
		Log.i("Log", "Database Created!");

	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
	//	db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACTS);
	//	db.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEYS);
		onCreate(db);					
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}

	Map<Integer, Integer> getSubSections(String surveyId)
	{
		Map<Integer, Integer> subSections = new HashMap<Integer, Integer>();
		int subSectionId;
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_SURVEY + " = " + surveyId;
		Cursor cursor = db.rawQuery(selectQuery,  null);
		if(cursor.moveToFirst()){
			do{
				subSectionId = Integer.valueOf(cursor.getString(4));
				if(!subSections.containsKey(subSectionId) && !(subSectionId == 0 ))
				{
					subSections.put(subSectionId, Integer.valueOf(cursor.getString(1)));
				}

			} while ( cursor.moveToNext());
		}
		return subSections;
	}

	public void setComplete(String key, int ans)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(KEY_COMPLETE, ans);
		db.update(TABLE_SURVEYS, cv, KEY_PK  + " =" +  key, null);
		db.close();
	}

	public String isInSurvey(String slum, String survey, String householdId)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_SURVEYS + " WHERE " + KEY_SURVEYID + " = '" + survey + "' AND " + 
		KEY_HOUSEHOLD + " ='" + householdId + "' AND " + KEY_SLUMID + " = " + slum;
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst())
		{
			db.close();
			return cursor.getString(0); 
			
		}

		db.close();
		return null;

	}

	public void addSurvey(String slumId, String surveyId, String householdId)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_SURVEYID, surveyId);
		values.put(KEY_HOUSEHOLD, householdId);
		values.put(KEY_SLUMID, slumId);
		values.put(KEY_COMPLETE, 0);
		db.insert(TABLE_SURVEYS, null, values);
		db.close();
	}


	public void addFact(String qid, String answer, int subId, String key)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_QID, qid);
		values.put(KEY_FACT, answer);
		values.put(KEY_SURVEY, key);
		values.put(KEY_SUBSECTION, subId);
		db.insert(TABLE_FACTS, null, values);
		db.close();
	}

	public void createOrUpdateFact(String qid, String answer, int subId, String key)
	{
		
		if(!isInDb(qid, subId, key))
		{
			addFact(qid, answer, subId, key);
			
		}

		else if (isInDb(qid, subId, key) && !answer.equals(""))
		{
			setValue(qid, answer, subId, key);
		}
	}
	
	public ArrayList<List<String>> getQuestions(String surveyForeignKey)
	{
		ArrayList<List<String>> questions = new ArrayList<List<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_SURVEY + " = " + surveyForeignKey;
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				List<String> question = new ArrayList<String>();
				question.add(cursor.getString(0)); // pk
				question.add(cursor.getString(1)); // Question ID
				question.add(cursor.getString(2)); // Fact
				question.add(cursor.getString(3)); // Survey ID
				question.add(cursor.getString(4)); // sub_code
				
				questions.add(question);
			} while (cursor.moveToNext());
		}
		db.close();
		return questions;
	}


	public Map<String, ArrayList<String>> getQuestionMap(String survey_id)
	{
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_SURVEY + " = " + survey_id;
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				ArrayList<String> result = new ArrayList<String>();
				result.add(cursor.getString(2));
				result.add(cursor.getString(4));
				map.put(cursor.getString(1), result);
			} while (cursor.moveToNext());
		}
		db.close();
		return map;
	}

	public Boolean hasQs(String survey_id, String subId)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_SURVEY + " = " + 
		survey_id + " AND " +  KEY_SUBSECTION  +" = " +subId;
		Cursor cursor = db.rawQuery(selectQuery, null);

		if(cursor.moveToNext())
		{
			return true;
		}

		else return false;
	}

	public ArrayList<ArrayList<String>> getCompletedSurveys()
	{
		ArrayList<ArrayList<String>> surveys = new ArrayList<ArrayList<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_SURVEYS + " WHERE " + KEY_COMPLETE + " = 1";
		try
		{
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor.moveToFirst()) {
				do {
					ArrayList<String> singleSurveyData = new ArrayList<String>();
					singleSurveyData.add(cursor.getString(0)); // Add PK
					singleSurveyData.add(cursor.getString(1)); // Add Survey ID
					singleSurveyData.add(cursor.getString(2)); // Add Slum ID
					singleSurveyData.add(cursor.getString(3)); // Add Household ID
					surveys.add(singleSurveyData); 
				} while (cursor.moveToNext());
			}
		}
		catch(NullPointerException e)
		{
			return surveys; // If NullPointerException is thrown, return empty list of surveys.
		}
		db.close();
		return surveys;

	}

	public ArrayList<String> getSurvey(String pk)
	{
		ArrayList<String> survey = new ArrayList<String>();
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_SURVEYS + " WHERE " + KEY_PK + " = " + pk;
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				survey.add(cursor.getString(1)); // Survey ID
				survey.add(cursor.getString(2)); // Slum ID
				survey.add(cursor.getString(3)); // Household ID
			} while (cursor.moveToNext());
		}
		db.close();
		return survey;

	}

	public void removeSurvey(String pk)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE "  + KEY_SURVEY +
				" = '" + pk + "'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		String[] array = new String[cursor.getCount()];
		int i = 0;
		if(cursor.getCount()>0){
			while(cursor.moveToNext()){
				array[i] = cursor.getString(cursor.getColumnIndex(KEY_PK));
			    i++;
			}
			
			String args = TextUtils.join(", ", array);
			db.execSQL(String.format("DELETE FROM "+TABLE_IMAGES+" WHERE "+KEY_FK_FACTS+"  IN (%s);", args));
			deletePhotos();
		}
		db.delete(TABLE_SURVEYS, KEY_PK + "=" + pk, null);
		db.delete(TABLE_FACTS, KEY_SURVEY + "=" + pk, null);
	}


	public void setValue(String id, String value, int subId, String survey)
	{
//		Log.in("log",""+);
		SQLiteDatabase db = this.getWritableDatabase();
		String qid = id;
		String answer = value;
		ContentValues cv = new ContentValues();
		if(answer != null)
		{
			cv.put(KEY_FACT, answer);
			cv.put(KEY_QID, qid);
			cv.put(KEY_SURVEY, survey);
			cv.put(KEY_SUBSECTION, subId);
			db.update(TABLE_FACTS, cv, KEY_SURVEY  + " =" + survey + " AND " + KEY_QID + " =" + qid + " AND " + KEY_SUBSECTION + " =" + subId, null);
		}
		db.close();
	}

	public String getValue(String id, int subId, String survey)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_QID + 
				" = '" + id + "' AND " + KEY_SUBSECTION + " = " + subId + " AND " + KEY_SURVEY +
				" = " + survey + ";";
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.moveToFirst())
		{
			db.close();
			return cursor.getString(2);
		} 

		db.close();
		return null;
	}

	public void removeSub(String survey, int subId)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_FACTS, KEY_SURVEY  + " =" + survey + " AND " + KEY_SUBSECTION + " =" + subId, null);
		db.close();
	}

	public boolean isInDb(String qid, int subId, String key)
	{
		//Log.i("Log", "QiD - "+ qid + " SubId - " + subId + " Survey - " + key  );
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_QID + " = '" + 
		qid + "' AND " + KEY_SUBSECTION + " = " + subId + " AND " + KEY_SURVEY + " = '" + key +"'";  ;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			db.close();
			return true;
		}
		db.close();
		return false;

	}
	
	/**
	 * Function added by SC :
	 * Added function to fetch fact id, which is used while inserting photos to table.
	 * @param qid
	 * @param subId
	 * @param key
	 * @return
	 */
	public Integer isInDbFacts(String qid, int subId, String key)
	{
		try{
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_QID + " = '" + 
							qid + "' AND " + KEY_SUBSECTION + " = " + subId + " AND " + KEY_SURVEY + " = " + key +";"  ;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			db.close();
			return cursor.getInt(0);
		}
		db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return null;

	}
	
	/**
	 * Function added by SC:
	 * Code to save captured photo records to TABLE_IMAGES.
	 * @param imageList
	 * @param qid
	 * @param subId
	 * @param key
	 * @return
	 */
	public boolean insertImages(ArrayList<List<String>> imageList,String qid, int subId, String key)
	{
		try{
		int fk_id = isInDbFacts(qid,subId,key);
		//Delete previous photos if any for selected question.
		deleteImageList(fk_id);
		if(fk_id > 0)
		{
			SQLiteDatabase db = this.getWritableDatabase();
			//Iterate through all image list and insert to database table
			for (int i=0; i<imageList.size(); i++)
			{
				ContentValues cv = new ContentValues();
				cv.put(KEY_LAT, imageList.get(i).get(0));
				cv.put(KEY_FK_FACTS, fk_id);
				cv.put(KEY_LONGITUDE, imageList.get(i).get(1));
				cv.put(KEY_BITMAP, imageList.get(i).get(2));
				db.insert(TABLE_IMAGES,null,cv);
			}
			db.close();
		}
		else
		{
			return false;
		}
		Log.i("log","inserted data");
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return true;
	}
	
	/**
	 * Function added by SC:
	 * Fetch all the photos to respective fact.
	 * @param i_factID
	 * @return ArrayList<List<String>> 
	 */
	public ArrayList<List<String>> getImageList(String i_factID)
	{
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		try{	
			SQLiteDatabase db = this.getWritableDatabase();
			String selectQuery = "SELECT  * FROM " + TABLE_IMAGES + " WHERE " + KEY_FK_FACTS + " = "+ Integer.parseInt(i_factID) +";";
			Cursor cursor = db.rawQuery(selectQuery, null);
			//Iterate and insert to array object
			if (cursor.moveToFirst()) {
				do {
					List<String> image = new ArrayList<String>();
					image.add(cursor.getString(2)); // Latitude
					image.add(cursor.getString(4)); // Longitude
					image.add(cursor.getString(3)); // Bitmap
					images.add(image);
				} while (cursor.moveToNext());
			}
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;

	}
	
	/**
	 * Function added by SC:
	 * Code to delete photo record.
	 * @param i_factID
	 * @return
	 */
	public Cursor deleteImageList(Integer i_factID)
	{
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(TABLE_IMAGES, ""+KEY_FK_FACTS+"="+i_factID, null);
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());	
		}
		return null;
	}
	
	/***
	 * Function added by SC:
	 * Fetch photo list for the given question id, subsection id and survey
	 * @param id
	 * @param subId
	 * @param survey
	 * @return
	 */
	public ArrayList<List<String>> getImages(String id, int subId, String survey)
	{
		int ifactID =0;
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			String selectQuery = "SELECT  * FROM " + TABLE_FACTS + " WHERE " + KEY_QID + 
					" = '" + id + "' AND " + KEY_SUBSECTION + " = " + subId + " AND " + KEY_SURVEY +
					" = " + survey + "";
			Cursor cursor = db.rawQuery(selectQuery, null);
			if(cursor.moveToFirst())
			{
				ifactID =  cursor.getInt(0);
			} 
			db.close();
			if(ifactID != 0)
			{
				images = getImageList(""+ifactID);
			}
		}catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;
	}
	
	/**
	 * Function added by SC:
	 * Function to delete photos from the folder if their records are not present in database table.
	 */
	public void deletePhotos()
	{
	try{
		File path = new File(FormActivity.PHOTO_PATH+"/"+FormActivity.PHOTO_FOLDER);
		
	    File [] files = path.listFiles();
	    SQLiteDatabase db = this.getWritableDatabase();
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile()){ //this line weeds out other directories/folders
	            String sFileName = files[i].getName();
	            
	    		String selectQuery = "SELECT  * FROM " + TABLE_IMAGES + " WHERE " + KEY_BITMAP + " = '"+ sFileName +"';";
	    		Cursor cursor = db.rawQuery(selectQuery, null);
	    		if (cursor.getCount()<=0) {
	    			File objFile = new File(FormActivity.PHOTO_PATH+"/"+FormActivity.PHOTO_FOLDER+"/"+sFileName);
					if(objFile.exists())
					{
						objFile.delete();
					}
	    		}
	    		cursor.close();	
	        }
	    }
	}
	catch(Exception e){
		Log.i("Shelter",""+e);
	}
	}
	
	/*************************************************************************/
	public void insertPlottedShapeGroup(String slumID, String drawCompo, String name, String createdDate, String latlong)
	{				
		try{	
			SQLiteDatabase db = this.getWritableDatabase();
			
			ContentValues cv = new ContentValues();
			cv.put(KEY_SLUMID,slumID);
			cv.put(KEY_DRAWABLE_COMPONENT,drawCompo);
			cv.put(KEY_NAME,name );
			cv.put(KEY_CREAT_DATE, createdDate);		
			cv.put(KEY_LATLONG, latlong);		
			
			db.insert(TABLE_PLOTTEDSHAPE_GROUP,null,cv);
		
			db.close();
		
			Log.i("log","inserted data");
			
		}catch(Exception e)	{
			Log.e("Shelter",""+e.getMessage());
		}
	
	}
	
	public void updatePlottedShapeGroup(String id, String latlong, String name)
	{
//		Log.in("log",""+);
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cv = new ContentValues(); 
		if(latlong.length() > 0)
		{
			cv.put(KEY_LATLONG, latlong);
			
			if(name.length() > 0)
			{
				cv.put(KEY_NAME, name);
			}
			db.update(TABLE_PLOTTEDSHAPE_GROUP, cv, KEY_PK  + " ='" + id +"'", null);
		}
		else
		{
			db.delete(TABLE_PLOTTEDSHAPE_GROUP, KEY_PK  + " ='" + id + " '", null);
		}
		db.close();
	}
		
	public void deletePlottedShapeGroup(String sulmId)
	{
		try
		{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_PLOTTEDSHAPE_GROUP, KEY_SLUMID  + " ='" + sulmId + " '", null);
		db.close();
		Log.i("log","Deleted data");
		
		}catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		
	}

	public ArrayList<List<String>> getAllData(String tblName)
	{	
		
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		try{	
			SQLiteDatabase db = this.getWritableDatabase();			
				
			//String selectQuery = "SELECT  * FROM " + tblName +" Group by 2 ";  
			
			String selectQuery = "SELECT  "+KEY_PK + "," +KEY_SLUMID + "," +KEY_DRAWABLE_COMPONENT + "," +KEY_NAME + "," +KEY_CREAT_DATE+ 
					
									" FROM " + TABLE_PLOTTEDSHAPE_GROUP + " Group by "+KEY_SLUMID +" ORDER BY "+KEY_CREAT_DATE;
			Cursor cursor = db.rawQuery(selectQuery, null);
			//Iterate and insert to array object
			if (cursor.moveToFirst()) {
				do {
					List<String> image = new ArrayList<String>();
					image.add(cursor.getString(0));
					image.add(cursor.getString(1)); 
					image.add(cursor.getString(2));
					image.add(cursor.getString(3));
					image.add(cursor.getString(4));
				
					images.add(image);
				} while (cursor.moveToNext());
			}
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;
		
	}
	

	public ArrayList<List<String>> getAllDataList(String tblName,String condition_field,String condition,String order_by)
	{	
		
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		try{	
			SQLiteDatabase db = this.getWritableDatabase();			
				
			//String selectQuery = "SELECT  * FROM " + tblName +" Group by 2 ";  
			
			//String selectQuery = "SELECT * FROM " + tblName + " Where "+KEY_SLUMID+" ='"+Condition+"'";
			String selectQuery = "SELECT * FROM " + tblName + " Where "+condition_field+" ='"+condition+"'";
			if(order_by.length() > 0)
			{
				selectQuery =selectQuery+" order by "+order_by;
			}
			
			
			Cursor cursor = db.rawQuery(selectQuery, null);
			//Iterate and insert to array object
			if (cursor.moveToFirst()) {
				do {
					List<String> image = new ArrayList<String>();
					image.add(cursor.getString(0));//pk
					image.add(cursor.getString(1)); //slumcode
					image.add(cursor.getString(2));//Drawable Compo
					image.add(cursor.getString(3));//name
					image.add(cursor.getString(4));//created date
					image.add(cursor.getString(5));//lat long
					
					images.add(image);
				} while (cursor.moveToNext());
			}
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;
		
	}
	
	public ArrayList<List<String>> getComponentName(String slumId,String componentId)
	{	
		
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		try{	
			SQLiteDatabase db = this.getWritableDatabase();			
				
			//String selectQuery = "SELECT  * FROM " + tblName +" Group by 2 ";  
			
			String selectQuery = "SELECT "+KEY_NAME +"," +KEY_CREAT_DATE +				
									" FROM " + TABLE_PLOTTEDSHAPE_GROUP +
									" Where "+ KEY_SLUMID +"='"+slumId +"' and "+ KEY_DRAWABLE_COMPONENT+ " = '"+componentId+"'" +
									" ORDER BY "+KEY_CREAT_DATE +" ";
			Cursor cursor = db.rawQuery(selectQuery, null);
			//Iterate and insert to array object
			if (cursor.moveToLast()) {
				do {
					List<String> image = new ArrayList<String>();
					image.add(cursor.getString(0));
					image.add(cursor.getString(1));			
					
					images.add(image);
					
				} while (cursor.moveToNext());
			}
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;
		
	}
	
	
	public ArrayList<List<String>> getId(String slumId, String compo, String names, String dates, String lats)
	{	
		
		ArrayList<List<String>> images = new ArrayList<List<String>>();
		try{	
			SQLiteDatabase db = this.getWritableDatabase();			
			String selectQuery = "SELECT "+KEY_PK + " FROM " + TABLE_PLOTTEDSHAPE_GROUP + " Where "+								
									KEY_SLUMID+" ='"+slumId+"' and "+
									KEY_DRAWABLE_COMPONENT+" ='"+compo+"' and " +
									KEY_NAME+" ='"+names+"' and "+
									KEY_CREAT_DATE+" ='"+dates+"' and "+
									KEY_LATLONG+" ='"+lats+"'";
			
			
			
			Cursor cursor = db.rawQuery(selectQuery, null);
			//Iterate and insert to array object
			if (cursor.moveToFirst()) {
				do {
					List<String> image = new ArrayList<String>();
					image.add(cursor.getString(0));//pk
										
					images.add(image);
				} while (cursor.moveToNext());
			}
			db.close();
		}
		catch(Exception e)
		{
			Log.e("Shelter",""+e.getMessage());
		}
		return images;
		
	}
	
}
