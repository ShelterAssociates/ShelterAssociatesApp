package shelter.android.survey.classes.forms;

import java.util.HashMap;
import java.util.List;
import shelter.android.survey.classes.R;
import shelter.android.survey.classes.menus.DatabaseHandler;
import shelter.android.survey.classes.menus.MapActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableList extends BaseExpandableListAdapter {

	private Context _context;
	private List<String> _listDataHeader; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<String>> _listDataChild;
	public String s_id = "";
	public String g_id = "";
	View v = null ;
	 
	public ExpandableList(Context context,
			List<String> listDataHeader,
			HashMap<String, List<String>> listChildData) {
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;		
	}
	
	public String returnSid()
	{		
		return s_id;
	}
	public void notifyChange()
	{
		notifyDataSetChanged();
	}
	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, final ViewGroup parent) {
		
		final String childText = (String) getChild(groupPosition, childPosition);
		final String[] C_split = childText.split("_");
		 
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.activity_treeview,	null);				
		}
		
		TextView txtListChild = (TextView)  convertView.findViewById(R.id.txtChild);
		txtListChild.setText(C_split[3]);
		
		// Edit Button On expandable list view : create and click code
		Button bt_edit = (Button)  convertView.findViewById(R.id.btn_edit);
		bt_edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(_context, MapActivity.class);
				intent.putExtra("SID", s_id.replace("|", ""));
				intent.putExtra("SIDEdit", C_split[0].toString());
				intent.putExtra("slumID", C_split[1].toString());
				intent.putExtra("surveyId", C_split[2].toString());
				intent.putExtra("slumbutton", "edit");
				intent.putExtra("slumName", C_split[4].toString());

				_context.startActivity(intent);
				((Activity) _context).finish();
			}
		});

		// Delete Button On expandable list view : create and click code
		Button bt_del = (Button)  convertView.findViewById(R.id.btn_delete);
		bt_del.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// Confirmation for Deleting Drawn Shape : passing primary key
				// and slum id
				alertMessage(C_split[0].toString(), C_split[1].toString());
			}
		});				
        
		/***************************************************************************/	
		convertView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				LinearLayout layout=(LinearLayout)v.findViewById(R.id.linear);
				if (s_id.contains(",|" + C_split[0].toString()+ "|")) {
					s_id = s_id.replace(",|" + C_split[0].toString() + "|",	"");									
					layout.setBackgroundColor(_context.getResources().getColor(R.color.redback));
					
				} else {
					s_id = s_id + ",|" + C_split[0].toString()+ "|";									
					layout.setBackgroundColor(_context.getResources().getColor(R.color.greenback));
				}		
			}
		});
		
		if (s_id.contains(",|" + C_split[0].toString()+ "|")) {
			convertView.setBackgroundColor(_context.getResources().getColor(R.color.greenback));
		} else {
			convertView.setBackgroundColor(_context.getResources().getColor(R.color.redback));
		}
		return  convertView;
		/************************************/
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition))
				.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		 View v=null;	                  
		
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.activity_groupitem,null);
			 v = convertView;
		}else
		{
			v = convertView;
		}
		
		TextView lblListHeader = (TextView) v.findViewById(R.id.txtContinent);
		CheckBox chkSelect = (CheckBox)v.findViewById(R.id.chkSelect);
		
		lblListHeader.setTypeface(null, Typeface.BOLD);
		String headerTitle = (String) getGroup(groupPosition);
		lblListHeader.setText(headerTitle); 
		
		chkSelect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v1) {
				// TODO Auto-generated method stub
				CheckBox chkSelect = (CheckBox)v1.findViewById(R.id.chkSelect);
			    for(int i = 0;i<getChilds(groupPosition).size();i++){
			    	String str = getChilds(groupPosition).get(i);
			    	
			    	if(chkSelect.isChecked())
			    		s_id = s_id + ",|" + str.split("_")[0].toString()+ "|";
			    	else
			    		s_id = s_id.replace(",|" + str.split("_")[0].toString() + "|",	"");	
			    }

			    if (g_id.contains(",|" + groupPosition+ "|")) {
					g_id = g_id.replace(",|" + groupPosition + "|",	"");									
					
				} else {
					g_id = g_id + ",|" + groupPosition + "|";									
				}	
                notifyDataSetChanged();
			}
		});
		
		if (g_id.contains(",|" + groupPosition+ "|")) {
			chkSelect.setChecked(true);
		} else {
			chkSelect.setChecked(false);
		}
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	/***************************************************************************/
	@Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }
	
	public List<String> getChilds(int groupPosition) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition));
	}
	/************************** Confirmation Window *************************************/
	public void alertMessage(final String pk_id, final String slum_id) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked

					DatabaseHandler db = new DatabaseHandler(_context);
					db.updatePlottedShapeGroup(pk_id, "","");

					Intent intent = ((Activity) _context).getIntent();
					intent.putExtra("slumId", slum_id);
					((Activity) _context).finish();
					_context.startActivity(intent);

					Toast.makeText(_context, "Data Deleted", Toast.LENGTH_SHORT).show();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					// do nothing
					break;
				}

			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
		builder.setMessage("Are you sure? You Want To Delete")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}
	/***************************************/
}

