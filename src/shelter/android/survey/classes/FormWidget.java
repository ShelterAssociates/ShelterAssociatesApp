package shelter.android.survey.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

abstract class FormWidget 
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
	
	protected HashMap<String, ArrayList<String>> _toggles;
	
	public FormWidget( Context context, String name )
	{
		//Log.i("Log", context);
		_layout = new LinearLayout(context);
		_layout.setLayoutParams( FormActivity.defaultLayoutParams );
		_layout.setOrientation( LinearLayout.VERTICAL );
		
		_property 		= name;
		_displayText	= name;
		_inDb = 		false;
		_required = false;
		_sub = 0;
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
