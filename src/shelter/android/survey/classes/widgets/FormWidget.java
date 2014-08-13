package shelter.android.survey.classes.widgets;
import shelter.android.survey.classes.forms.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;

public class FormWidget 
{
	protected View 			_view;
	protected String 		_property;
	protected String 		_displayText;
	protected int 	 		_priority;
	protected LinearLayout 	_layout;
	protected String 		_wid;
	protected Boolean		_inDb;
	protected Boolean 		_required;
	protected FormActivity.FormWidgetToggleHandler _handler;
	protected int _sub;
	protected Button 	_button;
	protected LinearLayout _layoutImage;
	protected HashMap<String, ArrayList<String>> _toggles;
	public ArrayList<List<String>> _imgArray;
   protected Activity objActivity ;
	public FormWidget( Context context, String name )
	{
		//Log.i("Log", context);
		_layout = new LinearLayout(context);
		_layout.setLayoutParams( FormActivity.defaultLayoutParams );
		_layout.setOrientation( LinearLayout.VERTICAL );
		 _imgArray = new ArrayList<List<String>>(); 
		_property 		= name;
		_displayText	= name;
		_inDb = 		false;
		_required = false;
		_sub = 0;
	}
	
	/**
	 * Function added by SC :
	 * Added photo capture layout if bTakePhoto is true.
	 * @param context
	 * @param bTakePhoto
	 */
	public void AddPhotoLayout(Context context, Boolean bTakePhoto)
	{
		try{
		if(bTakePhoto)
		{
			//Added a button below question for capturing the photo
			_button  = new Button(context);
			_button.setText("Take Photo");	
			_layout.addView(_button);
			
			/*To get curent activity instances*/
			objActivity =  SurveyFormActivity.activity;
			
			//Adding image list with horizantal scrollable view below the "Take Photo button"
			//Added to display horizantal list of images capture for the respective desired fact.
			HorizontalScrollView _horizantalView = new HorizontalScrollView(context);
			_layoutImage = new LinearLayout(context);
			_layoutImage.setGravity(Gravity.BOTTOM);
			_horizantalView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			_horizantalView.addView(_layoutImage);
			_layout.addView(_horizantalView);
			
			//Click event for capturing image
			_button.setOnClickListener(new View.OnClickListener ()
			{
				public void onClick (View v)
				{
					Log.i("click", "image");
					//Call to camera capture
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					SurveyFormActivity.m_id = getId();
					intent.putExtra("id", getId());
					//Setting up the URL to which the the photos will get uploaded.
				    String sPath = FormActivity.PHOTO_PATH + "/"+FormActivity.PHOTO_FOLDER;
					File folder = new File(sPath);
					if (!folder.exists()) {
					     folder.mkdir();
					}
					SurveyFormActivity.m_sFileName = new Date().getTime() + ".png";
					File destImage = new File(sPath, SurveyFormActivity.m_sFileName);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destImage));
					SurveyFormActivity.cameraFile = destImage.getAbsolutePath();  
					SurveyFormActivity.objFilePhoto = destImage;
					objActivity.startActivityForResult(intent, 100);			
				}

			});
		
		}
		}catch(Exception e){
			Log.i("Shelter","Error :  : " +e.getMessage());
		}

	}
	// -----------------------------------------------
	//
	// view
	//
	// -----------------------------------------------
	/**
	 * return LinearLayout containing this widget's view elements
	 */
	public View getView() {
		return _layout;
	}
	
	public double labeltest()
	{
		return 0;
	}
	
	public void setSub(int set)
	{
		_sub = set;
		//Log.i("Log",  "SubsectionId set to " + set + " for question " + _wid);
	}

	public int getSub()
	{
		return _sub;
	}
	
	public void setRequired(Boolean set)
	{
		_required = set;
	}
	
	public Boolean getRequired()
	{
		return _required;
	}
	
	public Boolean getInDb()
	{
		return _inDb;
	}
	
	
	public void setInDb(Boolean set)
	{
		_inDb = set;
	}
	
	public String getType()
	{
		return "";
	}
	
	/**
	 * toggles the visibility of this widget
	 * @param value
	 */
	public void setVisibility( int value ){
		_layout.setVisibility( value );
	}
	
	// -----------------------------------------------
	//
	// set / get value
	//
	// -----------------------------------------------
	
	/**
	 * returns value of this widget as String
	 */
	public String getValue() {
		return "";
	}
	
	public String getStringValue()
	{
		return "";
	}
	
	public String getId()
	{
		return _wid;
	}

	/**
	 * sets value of this widget, method should be overridden in sub-class
	 * @param value
	 */
	public void setValue( String value ) {
		// -- override 
	}
	
	/**
	 * Function added by SC : 
	 * Set the _imageArray with bitmap, latitude and longitude values
	 * And add the image to horizantal scroll view.
	 * @param bitmap
	 * @param context
	 * @param lattitude
	 * @param longitude
	 */
	public void addSingleImage(String filename,Context context,String lattitude,String longitude){
		try{
			
			List<String> image = new ArrayList<String>();
			image.add(lattitude);
			image.add(longitude);
			//Reading the photo to bitmap.
			File objFile = new File(FormActivity.PHOTO_PATH+"/"+FormActivity.PHOTO_FOLDER+"/"+filename);
			Bitmap bitmap = BitmapFactory.decodeFile(objFile.getAbsolutePath());
	        image.add(filename);
			_imgArray.add(image);
			//Adding the photo to ImageView list which is displayed below the "take photo" button.
			createImageView(context, bitmap, image,_imgArray.size());
		}catch(OutOfMemoryError e)
		{
			Log.i("Shelter",""+e);
		}
	}
	
	/**
	 * Function added by SC
	 * Populate back the image list from the saved images to database for respective questions.
	 * @param context
	 * @param arrImages
	 */
	public void setImageValues(Context context, ArrayList<List<String>> arrImages)
	{
		try{
		_imgArray = arrImages;
		//Iterate through array and insert all the photos to horizantal scroll view.
		for (int j=0; j<_imgArray.size(); j++)
		{
			//Read file to bitmap and then add to horizantal scroll view.
			File objFile = new File(FormActivity.PHOTO_PATH+"/"+FormActivity.PHOTO_FOLDER+"/"+_imgArray.get(j).get(2));
			if(objFile.exists())
			{
				Bitmap bitmap = BitmapFactory.decodeFile(objFile.getAbsolutePath());
				createImageView(context, bitmap, _imgArray.get(j), j);
			}
		}	
		}catch(Exception e){
			Log.i("shelter","Error ::"+e.getMessage());
		}
	}
	
	/**
	 * Function added by SC:
	 * Create an image view with preview photo and delete functionality.
	 * @param context
	 * @param bitmap
	 * @param image
	 */
	protected void createImageView(final Context context, final Bitmap bitmap, final List<String> image,int index)
	{
		try{
		final LinearLayout lImageLayout = new LinearLayout(context);
		lImageLayout.setOrientation( LinearLayout.VERTICAL );
		//Created an image view
		ImageView img = new ImageView(context);
		img.setPadding(10, 10, 10, 10);
		img.setImageBitmap(Bitmap.createScaledBitmap(bitmap,100, 100, true));
		
		//Added image view click listener for preview of photo
		img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrameLayout mainFrame = new FrameLayout(context);
				mainFrame.setDrawingCacheBackgroundColor(Color.GRAY);
				mainFrame.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				final ImageView imgPreview = new ImageView(context);
				imgPreview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				imgPreview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()-300, bitmap.getHeight()-300, true));
				mainFrame.addView(imgPreview);
									
				//Added dialog to pop-up with photo set
				final Dialog dialog = new Dialog(context);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				dialog.setContentView(mainFrame);
				dialog.setCanceledOnTouchOutside(true);
				
				_layout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
		
		//Added delete button for each image
		Button _buttonDelete = new Button(context);
		_buttonDelete.setText("Delete");
		
		_buttonDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//Confirmation before delete
				new AlertDialog.Builder(context).setTitle("Delete confirmation")
				.setMessage("Are you sure you want to delete the photo?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1)
					{
						//Removes the respective image view from the layout and also from the array list.
						
						_imgArray.remove(image);
						_layoutImage.removeView(lImageLayout);
					}

				}
				).setNegativeButton("No", null).create().show();
				
			}
		});
		
		lImageLayout.addView(img);
		lImageLayout.addView(_buttonDelete);
		_layoutImage.addView(lImageLayout);
		}catch(Exception e){
			Log.i("Shelter","Error -"+e.getMessage());
		}
	}
	// -----------------------------------------------
	//
	// modifiers
	//
	// -----------------------------------------------
	/**
	 * sets the hint for the widget, method should be overriden in sub-class
	 */
	public void setHint( String value ){
		// -- override
	}
	
	/**
	 * sets an object that contains keys for special properties on an object
	 * @param modifiers
	 */
	public void setModifiers( JSONObject modifiers ) {
		// -- override
	}
	
	// -----------------------------------------------
	//
	// set / get priority
	//
	// -----------------------------------------------
	
	/**
	 * sets the visual priority of this widget
	 * essentially this means it's physical location in the form
	 */
	public void setPriority( int value ) {
		_priority = value;
	}
	
	/**
	 * returns visual priority
	 * @return
	 */
	public int getPriority() {
		return _priority;
	}
	
	// -----------------------------------------------
	//
	// property name mods
	//
	// -----------------------------------------------
	
	/**
	 * returns the un-modified name of the property this widget represents
	 */
	public String getPropertyName(){
		return _property;
	}
	
	/**
	 * returns a title case version of this property
	 * @return
	 */
	public String getDisplayText(){
		return _displayText;
	}
	
	/**
	 * takes a property name and modifies 
	 * @param s
	 * @return
	 */

	
	// -----------------------------------------------
	//
	// toggles
	//
	// -----------------------------------------------
	
	/**
	 * sets the list of toggles for this widgets
	 * the structure of the data looks like this:
	 * HashMap<value of property for visibility, ArrayList<list of properties to toggle on>>
	 */
	public void setToggles( HashMap<String, ArrayList<String>> toggles ) {
		_toggles = toggles;
	}
	
	/**
	 * return list of widgets to toggle on
	 * @param value
	 * @return
	 */
	public ArrayList<String> getToggledOn() 
	{
		if( _toggles == null ) return new ArrayList<String>();
		
		if( _toggles.get( getValue() ) != null ) {
			return _toggles.get( getValue() );
		} else {
			return new ArrayList<String>();
		}
	}
	
	/**
	 * return list of widgets to toggle off
	 * @param value
	 * @return
	 */
	public ArrayList<String> getToggledOff() 
	{
		ArrayList<String> result = new ArrayList<String>();
		if( _toggles == null ) return result;
		
		Set<String> set = _toggles.keySet();
		
		 for (String key : set)
		 {
			 if( !key.equals( getValue() ) ) 
			 {
				ArrayList<String> list = _toggles.get(key);
				if( list == null ) return new ArrayList<String>();
				for( int i = 0; i < list.size(); i++ ) {
					result.add( list.get(i) );
				}
			}
		 }
		
		return result;
	}
	
	/**
	 * sets a handler for value changes
	 * @param handler
	 */
	public void setToggleHandler( FormActivity.FormWidgetToggleHandler handler ){
		_handler = handler;
	}

	public void error(Boolean val) {
		
	}
}
