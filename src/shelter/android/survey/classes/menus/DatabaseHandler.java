package shelter.android.survey.classes.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Survey";
	public static final String TABLE_FACTS = "facts";
	public static final String TABLE_SURVEYS = "surveys";
	
	public static final String KEY_PK = "pk";
	public static final String KEY_QID = "qid";
	public static final String KEY_FACT = "fact";
	public static final String KEY_SURVEY = "survey";
	public static final String KEY_COMPLETE = "complete";
	public static final String KEY_SUBSECTION = "subSection";

	public static final String KEY_SURVEYID = "surveyid";
	public static final String KEY_HOUSEHOLD = "household";
	public static final String KEY_SLUMID = "slumid";



	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_SURVEYS_TABLE = "CREATE TABLE " + TABLE_SURVEYS
				+"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				KEY_SURVEYID + " TEXT,"+
				KEY_SLUMID + " INTEGER," +
				KEY_HOUSEHOLD + " TEXT," +
				KEY_COMPLETE + " INTEGER " + ")";
		db.execSQL(CREATE_SURVEYS_TABLE);
		String CREATE_FACT_TABLE = "CREATE TABLE " + TABLE_FACTS +
				"(" + KEY_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_QID + " STRING, " +
				KEY_FACT + " STRING, " +
				KEY_SURVEY + " INTEGER," +
				KEY_SUBSECTION + " INTEGER," +
				" FOREIGN KEY ("+KEY_SURVEY+") REFERENCES "+TABLE_SURVEYS+
				" ("+KEY_PK+"));";
		db.execSQL(CREATE_FACT_TABLE);

		Log.i("Log", "Database Created!");

	}

//				@Override
//				public void onOpen(SQLiteDatabase db)
//				{
//					db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACTS);
//					db.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEYS);
//					onCreate(db);					
//				}

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
		db.delete(TABLE_SURVEYS, KEY_PK + "=" + pk, null);
		db.delete(TABLE_FACTS, KEY_SURVEY + "=" + pk, null);
	}


	public void setValue(String id, String value, int subId, String survey)
	{
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
				" = " + id + " AND " + KEY_SUBSECTION + " = " + subId + " AND " + KEY_SURVEY +
				" = '" + survey + "'";
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
			return true;
		}
		db.close();
		return false;

	}

}
