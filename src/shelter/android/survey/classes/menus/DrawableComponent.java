package shelter.android.survey.classes.menus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shelter.android.survey.classes.forms.FormActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class DrawableComponent extends FormActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Prepare the 'START' button
		Button bt_start = new Button(this);
		bt_start.setText("START");
		bt_start.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
		bt_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getName(0).equals(" Make a Selection")) {
					Toast.makeText(getApplicationContext(),	"Please select a survey!", Toast.LENGTH_SHORT).show();
				} else {
					String[] c_split = getName(0).split(":");
					Intent intent = new Intent(v.getContext(),
							MapActivity.class);
					intent.putExtra("surveyShape", c_split[1]);
					intent.putExtra("surveyId", get(0));
					setResult(2, intent);
					// call finish it will finish this activity : Avoid open
					// activity multiple time
					finish();
				}
			}
		});

		// Prepare the 'CANCEL' button
		Button bt_cancel = new Button(this);
		bt_cancel.setText("CANCEL");
		bt_cancel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), MapActivity.class);
				intent.putExtra("surveyId", "");
				setResult(2, intent);
				// call finish it will finish this activity : Avoid open
				// activity multiple time
				finish();
			}
		});

		// Parse JSON
		try {
			FileInputStream file = openFileInput("all_surveys.json");
			String jsonString = FormActivity.parseFileToString(file);
			JSONObject obj = new JSONObject(jsonString);

			JSONArray meta = obj.getJSONArray(SCHEMA_KEY_MYMAP);
			JSONObject mydata = new JSONObject();

			for (int i = 0; i < meta.length(); i++) {
				JSONObject name = meta.getJSONObject(i);

				mydata.put(name.getString("id"),
						name.getString("name") + ' ' + name.getString("extra")
								+ ":" + name.getString("shape"));
			}
			JSONObject sendJson = new JSONObject();
			sendJson.put("id", "0");
			sendJson.put("choices", mydata);
			sendJson.put("required", "True");
			sendJson.put("type", "S");
			sendJson.put("weight", "1");

			LinearLayout layout = generateForm("{ \"Select a Survey\" : "
					+ sendJson.toString() + " }");

			TextView spacer = new TextView(this);
			spacer.setHeight(15);
			layout.addView(spacer);
			layout.addView(bt_start);

			TextView spacer1 = new TextView(this);
			spacer1.setHeight(15);
			layout.addView(spacer1);
			layout.addView(bt_cancel);

			setContentView(layout);

		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*********************** OnBack Button *************************************/
	@Override
	public void onBackPressed() {
		// Display alert message when back button has been pressed
		Intent intent = new Intent(getApplicationContext(), MapActivity.class);
		intent.putExtra("surveyId", "");
		setResult(2, intent);
		// call finish it will finish this activity : Avoid open activity
		// multiple time
		finish();
		return;
	}
	/*********************** The End *************************************/
}
