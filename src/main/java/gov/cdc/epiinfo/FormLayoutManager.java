package gov.cdc.epiinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Pattern;

import gov.cdc.epiinfo.etc.AudioProcessor;
import gov.cdc.epiinfo.etc.DateButton;
import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.Rule_Context;


public class FormLayoutManager {

	private Activity container;
	private static int fieldCounter;
	private static int currentDateFieldId;
	private LinkedList<Integer> imageFieldIds;
	private Hashtable<View,String> clickCheckCodes;
	private LinkedList<Integer> requiredViewIds;
	private Hashtable<View,Integer> lengthChecks;
	private Hashtable<View,Double[]> ranges;
	private Hashtable<View,String> patterns;
	public Hashtable<String,View> controlsByName;
	public Hashtable<Integer,String[]> groupedItems;
	private int formHeight;
	private int formWidth;
	private Rule_Context Context;
	public ScrollView scroller;
	private View executingView;
	private Drawable pageBackground;
	private float density;
	private boolean useAbsolutePos;
	//private double fontFactor;
	private FormMetadata formMetadata;
	private LinearLayout vertLayout;

	public FormLayoutManager(Activity container, FormMetadata formMetadata, int formHeight, int formWidth, ScrollView scroller, ViewGroup layout, Rule_Context pProcessor, boolean useAbsolutePos)
	{
		this.Context = pProcessor;
		this.container = container;
		this.formMetadata = formMetadata;
		imageFieldIds = new LinkedList<Integer>();
		requiredViewIds = new LinkedList<Integer>();
		lengthChecks = new Hashtable<View,Integer>();
		ranges = new Hashtable<View,Double[]>();
		patterns = new Hashtable<View,String>();
		clickCheckCodes = new Hashtable<View,String>();
		controlsByName = new Hashtable<String,View>();
		groupedItems = new Hashtable<Integer,String[]>();
		fieldCounter = -1;
		this.formHeight = formHeight;
		this.formWidth = formWidth;
		this.scroller = scroller;
		this.useAbsolutePos = useAbsolutePos;

		density = DeviceManager.GetDensity(container);
		//fontFactor = DeviceManager.GetCurrentFontFactor(container);

		pageBackground = container.getResources().getDrawable(R.drawable.editor_rectangle);

		InitForm(layout);
	}

	private void InitForm(ViewGroup layout)
	{
		if (this.useAbsolutePos)
		{
			AddPageBreaks(layout);
		}
		for (int x=0;x<formMetadata.Fields.size();x++)
		{
			Field field = formMetadata.Fields.get(x);

			String checkCode = "";
			if (formMetadata.CheckCode.contains("Field " + field.getName() + "\n"))
			{
				int index = formMetadata.CheckCode.indexOf("Field " + field.getName() + "\n");
				String rest = formMetadata.CheckCode.substring(index);
				int index2 = rest.indexOf("End-Field");
				checkCode = rest.substring(0, index2 + 9);
			}
			if (field.getType().equals("2"))
			{
				field.setId(AddHeader(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldFontSize(), field.getFieldFontStyle(), field.getFieldWidth(), field.getPagePosition()));
			}
			else if (field.getType().equals("21"))
			{
				field.setId(AddGroup(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldFontSize(), field.getFieldWidth(), field.getPagePosition(), field.getList(), true, null));
			}
			else if (field.getType().equals("5"))
			{
				field.setId(AddNumericFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getLower(), field.getUpper(), field.getPattern(), field.getIsReadOnly(), field.getIsRequired(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("7"))
			{
				field.setId(AddDateField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getIsReadOnly(), field.getPromptFontSize(), field.getFieldFontSize(), field.getLowerDate(), field.getUpperDate(), field.getIsRequired() ));
			}
			else if (field.getType().equals("8"))
			{
				field.setId(AddTimeField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getIsReadOnly(), field.getPromptFontSize(), field.getFieldFontSize(), field.getIsRequired()));
			}
			else if (field.getType().equals("10"))
			{
				field.setId(AddCheckBox(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("11"))
			{
				field.setId(AddYesNoField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getIsReadOnly(), field.getPromptFontSize(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("12"))
			{
				field.setId(AddOptionField(layout, field.getPrompt(), field.getListValues(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getPattern(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("13"))
			{
				field.setId(AddButtonField(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldWidth(), field.getFieldHeight(), field.getFieldFontSize(), field.getPagePosition(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("14"))
			{
				field.setId(AddImageField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize()));
			}
			else if (field.getType().equals("17") || field.getType().equals("19"))
			{
				if (field.getListValues().size() > 100)
				{
					field.setId(AddAutoCompleteField(layout, field.getPrompt(), field.getListValues(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getIsRequired(), checkCode + System.getProperty("line.separator")));
				}
				else
				{
					field.setId(AddDropDownField(layout, field.getPrompt(), field.getListValues(), field.getCodeValues(), field.getDestinationField(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getIsReadOnly(), field.getPromptFontSize(), field.getIsRequired(), checkCode + System.getProperty("line.separator")));
				}
			}
			else if (field.getType().equals("18"))
			{
				field.setId(AddDropDownField(layout, field.getPrompt(), field.getListValues(), field.getCodeValues(), field.getDestinationField(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getIsReadOnly(), field.getPromptFontSize(), field.getIsRequired(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("20"))
			{
				field.setId(AddRelateField(layout, field.getPrompt(), field.getX(), field.getY(), field.getFieldWidth(), field.getFieldHeight(), field.getFieldFontSize(), field.getPagePosition(), field.getShouldReturnToParent(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("3"))
			{
				field.setId(AddTextFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getIsRequired(), field.getIsReadOnly(), field.getShouldRepeatLast(), true, false, field.getMaxLength(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("4"))
			{
				field.setId(AddTextFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getIsRequired(), field.getIsReadOnly(), field.getShouldRepeatLast(), false, true, field.getMaxLength(), checkCode + System.getProperty("line.separator")));
			}
			else if (field.getType().equals("81"))
			{
				field.setId(AddVideoField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getLowerDate(), field.getUpperDate()));
			}
			else if (field.getType().equals("82"))
			{
				field.setId(AddAudioField(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getLowerDate(), field.getUpperDate()));
			}
			else
			{
				field.setId(AddTextFieldWithPrompt(layout, field.getPrompt(), field.getX(), field.getY(), field.getPromptX(), field.getPromptY(), field.getFieldWidth(), field.getFieldHeight(), field.getPagePosition(), field.getPromptFontSize(), field.getFieldFontSize(), field.getIsRequired(), field.getIsReadOnly(), field.getShouldRepeatLast(), false, false, field.getMaxLength(), checkCode + System.getProperty("line.separator")));
			}
		}

		if (!this.useAbsolutePos)
		{
			View v1 = new View(container);
			v1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 40));
			v1.setBackgroundColor(0x00FFFFFF);
			layout.addView(v1);
		}
	}

	private void AddPageBreaks(ViewGroup myLayout)
	{
		for (int counter = 0; counter < formMetadata.PageCount; counter++)
		{
			View v1 = new View(container);
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(formWidth - 20, formHeight - 20);
			params1.leftMargin = 5;//0;//10;
			params1.topMargin = (formHeight * counter) + 5;//10;
			v1.setLayoutParams(params1);
			v1.setBackgroundDrawable(pageBackground);
			myLayout.addView(v1);
		}
	}

	public LinkedList<Integer> GetImageFieldIds()
	{
		return imageFieldIds;
	}

	public Activity getContainer()
	{
		return container;
	}

	public void ScrollTo(int y)
	{
		scroller.scrollTo(0, y);
	}

	public View GetExecutingView()
	{
		return executingView;
	}

	public View GetView(String pName)
	{
		if(controlsByName.containsKey(pName.toLowerCase()))
		{
			return controlsByName.get(pName.toLowerCase());
		}
		else
		{

			return null;
		}
	}

	public void onCreate(Bundle savedInstanceState) {

	}

	private TextView AddLabel(ViewGroup myLayout, String text, double x, double y, int pagePosition, double promptFontSize)
	{
		TextView tv = new TextView(container);
		tv.setText(text);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			float dp = 25f;
			float fpixels = DeviceManager.GetDensity(container) * dp;
			params.topMargin = (int) (fpixels + 0.5f);

			tv.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			tv.setLayoutParams(params);
		}
		if (promptFontSize > 0)
		{
			float calcSize = (float)(promptFontSize * 1.8);
			if (!this.useAbsolutePos)
			{
				tv.setTextSize(19);
			}
			else
			{
				double sp = DeviceManager.GetFontSize(promptFontSize);
				//tv.setTextSize((float)(promptFontSize * 1.8));
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
			}
		}
		myLayout.addView(tv);
		return tv;
	}

	public int AddHeader(ViewGroup myLayout, String text, double x, double y, double fieldFontSize, String fieldFontStyle, double fieldWidth, int pagePosition)
	{
		fieldCounter++;

		TextView tv = new TextView(container);

		tv.setText(text);
		tv.setId(fieldCounter);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			float dp = 20f;
			float fpixels = DeviceManager.GetDensity(container) * dp;
			params.topMargin = (int) (fpixels + 0.5f);

			tv.setLayoutParams(params);

			//params.topMargin = 5;
			//params.bottomMargin = 10;
			//tv.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), LayoutParams.WRAP_CONTENT);
			params2.leftMargin = (int)Math.round(formWidth * x);
			params2.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			tv.setLayoutParams(params2);
		}
		float calcSize = (float)(fieldFontSize * 1.8);
		if (!this.useAbsolutePos)
		{
			tv.setTextSize(20);
		}
		else
		{
			double sp = DeviceManager.GetFontSize(fieldFontSize);
			//tv.setTextSize((float)(fieldFontSize * 1.8));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
		}

		if (fieldFontStyle.toLowerCase().contains("bold") && fieldFontStyle.toLowerCase().contains("italic"))
		{
			tv.setTypeface(null, Typeface.BOLD_ITALIC);
		}
		else
		{
			if (fieldFontStyle.toLowerCase().contains("bold"))
			{
				tv.setTypeface(null, Typeface.BOLD);
			}
			if (fieldFontStyle.toLowerCase().contains("italic"))
			{
				tv.setTypeface(null, Typeface.ITALIC);
			}
		}
		myLayout.addView(tv);

		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), tv);

		return fieldCounter;
	}

	public int AddGroup(ViewGroup myLayout, String text, double x, double y, double fieldFontSize, double fieldWidth, int pagePosition, String[] list, boolean assignId, View linkedView)
	{
		if (assignId)
		{
			fieldCounter++;
		}

		View v1 = new View(container);
		if (!this.useAbsolutePos)
		{
			View v2 = new View(container);
			v2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 15));
			myLayout.addView(v2);
			v1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2));
		}
		else
		{
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth * 0.98), 2);
			//params1.leftMargin = (int)Math.round(formWidth * x);
			//params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight) + (fieldFontSize * 1.8) + 6);//3);
			v1.setLayoutParams(params1);
		}
		v1.setBackgroundColor(0xFF42638c);

		TextView tv = new TextView(container);

		tv.setText(text);
		if (assignId)
		{
			tv.setId(fieldCounter);
		}
		RelativeLayout.LayoutParams params2;
		if (!this.useAbsolutePos)
		{
			params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		else
		{
			params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth * 0.98), LayoutParams.WRAP_CONTENT);
			//params2.leftMargin = (int)Math.round(formWidth * x);
			//params2.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
		}

		tv.setLayoutParams(params2);

		if (!this.useAbsolutePos)
		{
			tv.setTextSize(20);
		}
		else
		{
			double sp = DeviceManager.GetFontSize(fieldFontSize);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
		}
		tv.setTextColor(0xFF42638c);
		tv.setTypeface(null, Typeface.BOLD);
		if (!this.useAbsolutePos)
		{
			myLayout.addView(tv);
			myLayout.addView(v1);
		}
		else
		{
			vertLayout = new LinearLayout(container);
			vertLayout.setOrientation(LinearLayout.VERTICAL);
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.leftMargin = (int)Math.round(formWidth * x);
			params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			vertLayout.setLayoutParams(params1);
			myLayout.addView(vertLayout);
			vertLayout.addView(tv);
			vertLayout.addView(v1);
		}

		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), tv);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", v1);

		if (linkedView != null)
		{
			linkedView.setTag(tv);
		}

		groupedItems.put(fieldCounter, list);

		return fieldCounter;
	}

	public int AddImageField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize)
	{
		fieldCounter++;
		AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		LinearLayout horzLayout1 = new LinearLayout(container);
		horzLayout1.setOrientation(0);
		RelativeLayout.LayoutParams hostParams;

		if (!this.useAbsolutePos)
		{
			hostParams = new RelativeLayout.LayoutParams((int) (300 * DeviceManager.GetDensity(container) + 0.5f),(int) (450 * DeviceManager.GetDensity(container) + 0.5f));
		}
		else
		{
			hostParams = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth),(int)Math.round(formHeight * fieldHeight));
			hostParams.leftMargin = (int)Math.round(formWidth * x);
			hostParams.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
		}
		horzLayout1.setLayoutParams(hostParams);
		horzLayout1.setGravity(Gravity.CENTER);
		myLayout.addView(horzLayout1);

		LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		childParams.leftMargin = 4;
		childParams.rightMargin = 4;
		childParams.topMargin = 4;

		LinearLayout horzLayout2 = new LinearLayout(container);
		horzLayout2.setOrientation(0);
		horzLayout2.setLayoutParams(childParams);
		horzLayout2.setGravity(Gravity.CENTER);
		horzLayout2.setBackgroundColor(0xFFFFFFFF);
		horzLayout1.addView(horzLayout2);

		LinearLayout horzLayout3 = new LinearLayout(container);
		horzLayout3.setOrientation(0);
		horzLayout3.setLayoutParams(childParams);
		horzLayout3.setGravity(Gravity.CENTER);
		horzLayout3.setBackgroundColor(0xFF000000);
		horzLayout2.addView(horzLayout3);

		ImageView iv = new ImageView(container);
		iv.setId(fieldCounter);
		iv.setImageResource(android.R.drawable.ic_menu_camera);
		iv.setScaleType(ScaleType.CENTER);
		iv.setLayoutParams(childParams);
		iv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((RecordEditor)container).StartCamera((ImageView) v);
			}
		});

		horzLayout3.addView(iv);
		imageFieldIds.add(fieldCounter);
		return fieldCounter;
	}

	public int AddTimeField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, boolean isReadOnly, double promptFontSize, double fieldFontSize, boolean isRequired)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
			horzLayout.setLayoutParams(params1);
		}
		else
		{
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.leftMargin = (int)Math.round(formWidth * x);
			params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			horzLayout.setLayoutParams(params1);
		}

		myLayout.addView(horzLayout);

		EditText edt = new EditText(container);
		edt.setInputType(InputType.TYPE_CLASS_DATETIME);
		edt.setEnabled(false);
		edt.setId(fieldCounter);
		edt.setTag(formMetadata.Fields.get(fieldCounter).getName().toLowerCase());

		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);
		}

		if (!this.useAbsolutePos)
		{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
			edt.setLayoutParams(weightedParams);
		}
		else
		{
			int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
			edt.setLayoutParams(params2);
			if (fieldFontSize > 0)
			{
				edt.setTextSize((float)(fieldFontSize * 1.8));
			}
			edt.setBackgroundResource(R.drawable.textbox);
			edt.setPadding(5, 0, 0, 0);
		}
		horzLayout.addView(edt);

		DateButton btn = new DateButton(container);
		btn.setContentDescription("Clock");
		btn.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_clock);
		btn.setScaleType(ScaleType.CENTER);

		int size = container.getResources().getDimensionPixelSize(R.dimen.cal_icon_size);
		btn.setLayoutParams(new LayoutParams(size,size));
		btn.setTextField(edt);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				currentDateFieldId = ((DateButton)v).getTextField().getId();
				container.showDialog(1);				
			}
		});

		if (!isReadOnly)
		horzLayout.addView(btn);

		final EditText myTimeField = edt;

		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setContentDescription("Clear the time");
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(gov.cdc.epiinfo.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams((int) (25 * density + 0.5f), (int) (25 * density + 0.5f)));
		eraseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myTimeField.setText("");

			}
		});
		
		if (!isReadOnly)
		horzLayout.addView(eraseButton);

		final ImageButton myEraseButton = eraseButton;
		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					myEraseButton.setVisibility(View.VISIBLE);
				}
				if(AfterRule != null)AfterRule.Execute();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});


		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		return fieldCounter;
	}

	public int AddDateField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, boolean isReadOnly, double promptFontSize, double fieldFontSize, Date lowerDate, Date upperDate, boolean isRequired)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
			horzLayout.setLayoutParams(params1);
		}
		else
		{
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.leftMargin = (int)Math.round(formWidth * x);
			params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			horzLayout.setLayoutParams(params1);
		}

		myLayout.addView(horzLayout);


		EditText edt = new EditText(container);
		edt.setInputType(InputType.TYPE_CLASS_DATETIME);
		edt.setEnabled(false);
		edt.setId(fieldCounter);
		edt.setTag(formMetadata.Fields.get(fieldCounter).getName().toLowerCase());

		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);
		}

		if (!this.useAbsolutePos)
		{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
			edt.setLayoutParams(weightedParams);
		}
		else
		{
			int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
			edt.setLayoutParams(params2);
			if (fieldFontSize > 0)
			{
				edt.setTextSize((float)(fieldFontSize * 1.8));
			}
			edt.setBackgroundResource(R.drawable.textbox);
			edt.setPadding(5, 0, 0, 0);
		}

		horzLayout.addView(edt);

		DateButton btn = new DateButton(container);
		btn.setContentDescription("Calendar");
		btn.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_calendar);
		btn.setScaleType(ScaleType.CENTER);

		int size = container.getResources().getDimensionPixelSize(R.dimen.cal_icon_size);
		btn.setLayoutParams(new LayoutParams(size,size));
		btn.setTextField(edt);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				currentDateFieldId = ((DateButton)v).getTextField().getId();
				container.showDialog(0);				
			}
		});


		if (!isReadOnly)
		horzLayout.addView(btn);

		final EditText myDateField = edt;

		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setContentDescription("Clear the date");
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(gov.cdc.epiinfo.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams((int) (25 * density + 0.5f), (int) (25 * density + 0.5f)));
		eraseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myDateField.setText("");

			}
		});
		
		if (!isReadOnly)
		horzLayout.addView(eraseButton);

		final ImageButton myEraseButton = eraseButton;
		final Date myLowerDate = lowerDate;
		final Date myUpperDate = upperDate;
		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					try
					{
						Date selectedDate = DateFormat.getDateInstance().parse(s.toString());
						if (selectedDate.compareTo(myLowerDate) < 0 || selectedDate.compareTo(myUpperDate) > 0)
						{
							myDateField.setText("");
							DateFormat dateFormat = DateFormat.getDateInstance();	
							((RecordEditor)getContainer()).Alert(String.format(container.getString(R.string.not_in_range), dateFormat.format(myLowerDate), dateFormat.format(myUpperDate)));
							return;
						}
					}
					catch (Exception ex)
					{

					}
					myEraseButton.setVisibility(View.VISIBLE);
				}
				if(AfterRule != null)AfterRule.Execute();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});

		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		return fieldCounter;
	}

	private void RecordAudio(TextView textView, DateButton btnRecord, DateButton btnPlay, ImageView looper)
	{
		try
		{
			RingtoneManager.getRingtone(container, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
			Thread.sleep(500);
		}
		catch (Exception ex)
		{

		}

		try
		{
			File path = new File("/sdcard/Download/EpiInfo/Media/");
			path.mkdirs();
			textView.setText("/sdcard/Download/EpiInfo/Media/" + UUID.randomUUID().toString() + ".m4a");
			AudioProcessor audioProcessor = AudioProcessor.GetInstance(textView.getText().toString(), btnPlay, btnRecord, looper);
			audioProcessor.onRecord(true);
			btnRecord.setEnabled(false);
			btnPlay.setEnabled(false);

			RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setRepeatCount(Animation.INFINITE);
			rotate.setDuration(3000);
			rotate.setInterpolator(new LinearInterpolator());

			looper.setVisibility(View.VISIBLE);
			looper.startAnimation(rotate);
		}
		catch (Exception ex)
		{

		}
	}

	public int AddAudioField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double fieldFontSize, Date lowerDate, Date upperDate)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
			horzLayout.setLayoutParams(params1);
		}
		else
		{
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.leftMargin = (int)Math.round(formWidth * x);
			params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			horzLayout.setLayoutParams(params1);
		}

		myLayout.addView(horzLayout);


		final EditText edt = new EditText(container);
		edt.setVisibility(View.GONE);
		edt.setId(fieldCounter);
		edt.setTag(formMetadata.Fields.get(fieldCounter).getName().toLowerCase());

		if (!this.useAbsolutePos)
		{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
			edt.setLayoutParams(weightedParams);
		}
		else
		{
			int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
			edt.setLayoutParams(params2);
			if (fieldFontSize > 0)
			{
				edt.setTextSize((float)(fieldFontSize * 1.8));
			}
			edt.setBackgroundResource(R.drawable.textbox);
			edt.setPadding(5, 0, 0, 0);
		}

		horzLayout.addView(edt);

		final DateButton btnRecord = new DateButton(container);
		final DateButton btnStopRecord = new DateButton(container);
		final DateButton btnPlay = new DateButton(container);
		final ImageView looper = new ImageView(container);

		btnRecord.setContentDescription("Record");
		btnRecord.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_audio_rec);
		btnRecord.setScaleType(ScaleType.CENTER);

		int size = container.getResources().getDimensionPixelSize(R.dimen.media_icon_size);
		btnRecord.setLayoutParams(new LayoutParams(size,size));
		btnRecord.setTextField(edt);
		btnRecord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!edt.getText().toString().equals(""))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(container);
					builder.setMessage(container.getString(R.string.replace_audio))       
					.setNegativeButton(container.getString(R.string.no), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setCancelable(true)       
					.setPositiveButton(container.getString(R.string.yes), new DialogInterface.OnClickListener() 
					{           
						public void onClick(DialogInterface dialog, int id) 
						{                
							dialog.cancel();    
							RecordAudio(edt, btnRecord, btnPlay, looper);
						}       
					});
					builder.create();
					builder.show();
				}
				else
				{
					RecordAudio(edt, btnRecord, btnPlay, looper);
				}


			}
		});


		horzLayout.addView(btnRecord);

		btnStopRecord.setContentDescription("Stop");
		btnStopRecord.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_stop);
		btnStopRecord.setScaleType(ScaleType.CENTER);

		btnStopRecord.setLayoutParams(new LayoutParams(size,size));
		btnStopRecord.setTextField(edt);
		btnStopRecord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try
				{
					AudioProcessor audioProcessor = AudioProcessor.GetInstance(edt.getText().toString(), btnPlay, btnRecord, looper);
					if (!btnRecord.isEnabled())
					{
						audioProcessor.onRecord(false);
						btnRecord.setEnabled(true);
						btnPlay.setEnabled(true);
					}
					else
					{
						AudioProcessor.StopAll();
					}
				}
				catch (Exception ex)
				{

				}
			}
		});


		horzLayout.addView(btnStopRecord);

		btnPlay.setContentDescription("Play");
		btnPlay.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_play);
		btnPlay.setScaleType(ScaleType.CENTER);
		btnPlay.setVisibility(View.GONE);

		btnPlay.setLayoutParams(new LayoutParams(size,size));
		btnPlay.setTextField(edt);
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try
				{
					AudioProcessor audioProcessor = AudioProcessor.GetInstance(edt.getText().toString(), btnPlay, btnRecord, looper);
					audioProcessor.onPlay(true);
				}
				catch (Exception ex)
				{

				}
			}
		});


		horzLayout.addView(btnPlay);

		View blankView = new View(container);
		blankView.setLayoutParams(new LayoutParams(size,size));
		blankView.setVisibility(View.INVISIBLE);
		horzLayout.addView(blankView);
		
		looper.setBackgroundResource(gov.cdc.epiinfo.R.drawable.circles);
		looper.setScaleType(ScaleType.CENTER);
		looper.setLayoutParams(new LayoutParams(size,size));
		looper.setVisibility(View.GONE);
		horzLayout.addView(looper);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					btnPlay.setVisibility(View.GONE);
				}
				else
				{
					btnPlay.setVisibility(View.VISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});

		/*final EditText myDateField = edt;

		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setContentDescription("Clear the date");
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(gov.cdc.epiinfo.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams((int) (25 * density + 0.5f), (int) (25 * density + 0.5f)));
		eraseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myDateField.setText("");

			}
		});
		horzLayout.addView(eraseButton);

		final ImageButton myEraseButton = eraseButton;
		final Date myLowerDate = lowerDate;
		final Date myUpperDate = upperDate;
		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					try
					{
						Date selectedDate = DateFormat.getDateInstance().parse(s.toString());
						if (selectedDate.compareTo(myLowerDate) < 0 || selectedDate.compareTo(myUpperDate) > 0)
						{
							myDateField.setText("");
							DateFormat dateFormat = DateFormat.getDateInstance();	
							((RecordEditor)getContainer()).Alert(String.format(container.getString(R.string.not_in_range), dateFormat.format(myLowerDate), dateFormat.format(myUpperDate)));
							return;
						}
					}
					catch (Exception ex)
					{

					}
					myEraseButton.setVisibility(View.VISIBLE);
				}
				if(AfterRule != null)AfterRule.Execute();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});
		 */
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		return fieldCounter;
	}

	public void RecordVideo(TextView v)
	{
		try {
			File path = new File("/sdcard/Download/EpiInfo/Media/");
			path.mkdirs();
			String fileName = "/sdcard/Download/EpiInfo/Media/" + UUID.randomUUID().toString() + ".mp4";
			Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			v.setText(fileName);
			if (takeVideoIntent.resolveActivity(container.getPackageManager()) != null) {
				//Uri file = Uri.fromFile(new File(fileName));

				Uri file = FileProvider.getUriForFile(container,
						container.getString(R.string.file_provider_authority),
						new File(fileName));
				takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);
				container.startActivityForResult(takeVideoIntent, 1);
			}
		}
		catch (SecurityException se)
		{
			((RecordEditor)container).Alert(container.getString(R.string.error_camera));
		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}
	}

	public int AddVideoField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double fieldFontSize, Date lowerDate, Date upperDate)
	{
		fieldCounter++;
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		LinearLayout horzLayout = new LinearLayout(container);
		horzLayout.setOrientation(0);
		horzLayout.setGravity(0x10);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.bottomMargin = 35;
			horzLayout.setLayoutParams(params1);
		}
		else
		{
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params1.leftMargin = (int)Math.round(formWidth * x);
			params1.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			horzLayout.setLayoutParams(params1);
		}

		myLayout.addView(horzLayout);


		EditText edt = new EditText(container);
		edt.setVisibility(View.GONE);
		edt.setId(fieldCounter);
		edt.setTag(formMetadata.Fields.get(fieldCounter).getName().toLowerCase());

		if (!this.useAbsolutePos)
		{
			horzLayout.setWeightSum(100);
			LinearLayout.LayoutParams weightedParams = new LinearLayout.LayoutParams(50, LinearLayout.LayoutParams.WRAP_CONTENT);
			weightedParams.weight = 75;
			edt.setLayoutParams(weightedParams);
		}
		else
		{
			int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth) - 55, preferredHeight);
			edt.setLayoutParams(params2);
			if (fieldFontSize > 0)
			{
				edt.setTextSize((float)(fieldFontSize * 1.8));
			}
			edt.setBackgroundResource(R.drawable.textbox);
			edt.setPadding(5, 0, 0, 0);
		}

		horzLayout.addView(edt);

		final DateButton btnRecord = new DateButton(container);
		final DateButton btnPlay = new DateButton(container);

		btnRecord.setContentDescription("Record");
		btnRecord.setBackgroundResource(gov.cdc.epiinfo.R.drawable.video);
		btnRecord.setScaleType(ScaleType.CENTER);

		int size = container.getResources().getDimensionPixelSize(R.dimen.media_icon_size);
		btnRecord.setLayoutParams(new LayoutParams(size,size));
		btnRecord.setTextField(edt);
		btnRecord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final TextView textView = ((DateButton)v).getTextField();

				if (!textView.getText().toString().equals(""))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(container);
					builder.setMessage(container.getString(R.string.replace_video))       
					.setNegativeButton(container.getString(R.string.no), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setCancelable(true)       
					.setPositiveButton(container.getString(R.string.yes), new DialogInterface.OnClickListener() 
					{           
						public void onClick(DialogInterface dialog, int id) 
						{                
							dialog.cancel();    
							RecordVideo(textView);
						}       
					});
					builder.create();
					builder.show();
				}
				else
				{
					RecordVideo(textView);
				}


			}
		});


		horzLayout.addView(btnRecord);

		btnPlay.setContentDescription("Play");
		btnPlay.setBackgroundResource(gov.cdc.epiinfo.R.drawable.btn_play);
		btnPlay.setScaleType(ScaleType.CENTER);
		btnPlay.setVisibility(View.GONE);

		btnPlay.setLayoutParams(new LayoutParams(size,size));
		btnPlay.setTextField(edt);
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try
				{
					final TextView textView = ((DateButton)v).getTextField();
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(textView.getText().toString()));
					intent.setDataAndType(Uri.parse(textView.getText().toString()), "video/mp4");
					container.startActivity(intent);
				}
				catch (Exception ex)
				{

				}
			}
		});


		horzLayout.addView(btnPlay);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					btnPlay.setVisibility(View.GONE);
				}
				else
				{
					btnPlay.setVisibility(View.VISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});

		/*final EditText myDateField = edt;

		ImageButton eraseButton = new ImageButton(container);
		eraseButton.setContentDescription("Clear the date");
		eraseButton.setVisibility(View.INVISIBLE);
		eraseButton.setBackgroundColor(Color.WHITE);
		eraseButton.setImageResource(gov.cdc.epiinfo.R.drawable.close);
		eraseButton.setScaleType(ScaleType.CENTER);
		eraseButton.setLayoutParams(new LayoutParams((int) (25 * density + 0.5f), (int) (25 * density + 0.5f)));
		eraseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myDateField.setText("");

			}
		});
		horzLayout.addView(eraseButton);

		final ImageButton myEraseButton = eraseButton;
		final Date myLowerDate = lowerDate;
		final Date myUpperDate = upperDate;
		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		edt.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s)
			{
				if (s.toString().equals(""))
				{
					myEraseButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					try
					{
						Date selectedDate = DateFormat.getDateInstance().parse(s.toString());
						if (selectedDate.compareTo(myLowerDate) < 0 || selectedDate.compareTo(myUpperDate) > 0)
						{
							myDateField.setText("");
							DateFormat dateFormat = DateFormat.getDateInstance();	
							((RecordEditor)getContainer()).Alert(String.format(container.getString(R.string.not_in_range), dateFormat.format(myLowerDate), dateFormat.format(myUpperDate)));
							return;
						}
					}
					catch (Exception ex)
					{

					}
					myEraseButton.setVisibility(View.VISIBLE);
				}
				if(AfterRule != null)AfterRule.Execute();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after){	}

			public void onTextChanged(CharSequence s, int start, int before, int count){ }
		});
		 */
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), horzLayout);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		return fieldCounter;
	}


	private int AddTextField(ViewGroup myLayout, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, String checkCodeAfter, boolean isRequired, boolean isReadOnly, boolean shouldRepeatLast, boolean upper, boolean multiline, int maxLength, double fieldFontSize)
	{
		fieldCounter++;
		EditText txt;
		if (shouldRepeatLast)
		{
			txt = new AutoCompleteTextView(container);
			((AutoCompleteTextView)txt).setThreshold(1);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(container,android.R.layout.select_dialog_item,AppManager.GetCurrentDatabase().getDistinctFieldValues(formMetadata.Fields.get(fieldCounter).getName()));
			((AutoCompleteTextView)txt).setAdapter(adapter);
		}
		else
		{
			txt = new EditText(container);
		}
		if (!multiline)
		{
			txt.setSingleLine();
			txt.setImeOptions(EditorInfo.IME_ACTION_DONE);
		}

		if (upper)
		{
			txt.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		}
		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			txt.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			txt.setLayoutParams(params);
			if (fieldFontSize > 0)
			{
				txt.setTextSize((float)(fieldFontSize * 1.8));
			}
			txt.setBackgroundResource(R.drawable.textbox);
			txt.setPadding(5, 0, 0, 0);
		}

		txt.setId(fieldCounter);
		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);    		
		}    	
		if (isReadOnly)
		{
			txt.setEnabled(false);
		}
		if (maxLength > 0)
		{
			lengthChecks.put(txt, maxLength);
		}

		myLayout.addView(txt);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), txt);
		clickCheckCodes.put(txt, checkCodeAfter);

		txt.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s) { 
				// Nothing 
			} 
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
				// Nothing 
			} 
			public void onTextChanged(CharSequence s, int start, int before, int count) { 
				try
				{
					if (lengthChecks.containsKey(executingView))
					{
						if (((EditText)executingView).getText().length() > lengthChecks.get(executingView))
						{
							((EditText)executingView).setText(((EditText)executingView).getText().subSequence(0, lengthChecks.get(executingView)));
						}
					}
				}
				catch (Exception ex)
				{

				}

			}
		}
				);



		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		txt.setContentDescription("Enter text for " + FieldName);

		final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub



				executingView = v;
				if (!hasFocus)
				{
					String clickCheckCode = clickCheckCodes.get(v);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule != null)AfterRule.Execute();
					if (requiredViewIds.contains(v.getId()))
					{
						if (((EditText)v).getText().toString().equals(""))
						{
							((EditText)v).setError(container.getString(R.string.required_field));
						}
						else
						{
							((EditText)v).setError(null);
						}
					}
				}
				else
				{
					if(BeforeRule != null) BeforeRule.Execute();
				}
			}
		});

		return fieldCounter;
	}

	public int AddTextFieldWithPrompt(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double fieldFontSize, boolean isRequired, boolean isReadOnly, boolean shouldRepeatLast, boolean upper, boolean multiline, int maxLength, String checkCodeAfter)
	{
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);
		int id = AddTextField(myLayout,x,y,fieldWidth,fieldHeight,pagePosition,checkCodeAfter, isRequired, isReadOnly, shouldRepeatLast, upper, multiline, maxLength, fieldFontSize);
		controlsByName.put(formMetadata.Fields.get(id).getName().toLowerCase() + "|prompt", label);
		return id;
	}

	private int AddNumericField(ViewGroup myLayout, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, double lower, double upper, String pattern, boolean isReadOnly, boolean isRequired, double fieldFontSize, String checkCodeAfter)
	{
		fieldCounter++;
		EditText txt = new EditText(container);
		txt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		txt.setImeOptions(EditorInfo.IME_ACTION_DONE);


		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			txt.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			txt.setLayoutParams(params);
			if (fieldFontSize > 0)
			{
				txt.setTextSize((float)(fieldFontSize * 1.8));
			}
			txt.setBackgroundResource(R.drawable.textbox);
			txt.setPadding(5, 0, 0, 0);
		}

		txt.setId(fieldCounter);
		myLayout.addView(txt);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), txt);
		ranges.put(txt,new Double[]{lower, upper});
		if (pattern.contains("#"))
		{
			patterns.put(txt, pattern);
		}
		if (isReadOnly)
		{
			txt.setEnabled(false);
		}
		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);    		
		}   
		clickCheckCodes.put(txt, checkCodeAfter);

		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		txt.setContentDescription("Enter value for " + FieldName);
		final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub



				if (!hasFocus)
				{
					executingView = v;

					if (ranges.containsKey(v))
					{
						try
						{
							double val = Double.parseDouble(((EditText)v).getText().toString());
							double lower = ranges.get(v)[0];
							double upper = ranges.get(v)[1];
							if (val < lower || val > upper)
							{
								((EditText)v).setText("");
								((RecordEditor)getContainer()).Alert(String.format(container.getString(R.string.not_in_range), lower, upper));
								return;
							}
						}
						catch (Exception ex)
						{
							//
						}
					}

					if (requiredViewIds.contains(v.getId()))
					{
						if (((EditText)v).getText().toString().equals(""))
						{
							((EditText)v).setError(container.getString(R.string.required_field));
						}
						else
						{
							((EditText)v).setError(null);
						}
					}

					if (patterns.containsKey(v))
					{
						try
						{
							String pattern = patterns.get(v);
							String javaPattern = "";
							if (pattern.contains("."))
							{
								String left = pattern.split("\\.")[0];
								String right = pattern.split("\\.")[1];
								int leftOccur = CountOccurrences(left,'#');
								int rightOccur = CountOccurrences(right,'#');
								javaPattern = "\\d{1," + leftOccur + "}.\\d{" + rightOccur + "}";
							}
							else
							{
								int occur = CountOccurrences(pattern,'#');
								javaPattern = "\\d{0," + occur + "}";
							}
							if (!Pattern.matches(javaPattern, ((EditText)v).getText().toString()))
							{
								((EditText)v).setText("");
								((RecordEditor)getContainer()).Alert(String.format(container.getString(R.string.not_match_pattern), pattern));
								return;
							}

						}
						catch (Exception ex)
						{
							//
						}
					}

					String clickCheckCode = clickCheckCodes.get(v);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule!=null) AfterRule.Execute();
				}
				else
				{
					if(BeforeRule!=null) BeforeRule.Execute();
				}
			}
		});

		return fieldCounter;
	}

	private int CountOccurrences(String myString, char find) 
	{     
		int count = 0;     
		for (int i=0; i < myString.length(); i++)     
		{         
			if (myString.charAt(i) == find)         
			{              
				count++;         
			}     
		}     
		return count; 
	}

	public int AddNumericFieldWithPrompt(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double fieldFontSize, double lower, double upper, String pattern, boolean isReadOnly, boolean isRequired, String checkCodeAfter)
	{
		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);		
		int id = AddNumericField(myLayout,x,y,fieldWidth,fieldHeight,pagePosition, lower, upper, pattern, isReadOnly, isRequired, fieldFontSize, checkCodeAfter);
		controlsByName.put(formMetadata.Fields.get(id).getName().toLowerCase() + "|prompt", label);
		return id;
	}

	public int AddCheckBox(ViewGroup myLayout, String text, double x, double y, double fieldWidth, double fieldHeight, int pagePosition, double fontSize, String checkCodeClick)
	{
		fieldCounter++;
		CheckBox cbx = new CheckBox(container);
		cbx.setText(text);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			float dp = 20f;
			float fpixels = DeviceManager.GetDensity(container) * dp;
			params.topMargin = (int) (fpixels + 0.5f);

			cbx.setLayoutParams(params);
			//cbx.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			cbx.setTextSize(19);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), (int)Math.round(formHeight * fieldHeight));
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			cbx.setLayoutParams(params);
			if (fontSize > 0)
			{
				double sp = DeviceManager.GetFontSize(fontSize);
				//cbx.setTextSize((float)(fontSize * 1.8));
				cbx.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
			}
		}

		cbx.setId(fieldCounter);
		myLayout.addView(cbx);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), cbx);
		clickCheckCodes.put(cbx, checkCodeClick);


		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();

		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);

		cbx.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				executingView = buttonView;
				String clickCheckCode = clickCheckCodes.get(buttonView);
				//processor.Execute(clickCheckCode, "Click");  
				if(ClickRule!=null) ClickRule.Execute();
			}

		});
		return fieldCounter;
	}

	public int AddOptionField(ViewGroup myLayout, String prompt, LinkedList<String> listValues, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, double controlFontSize, String pattern, String checkCodeAfter)
	{
		fieldCounter++;

		RadioGroup layout2 = new RadioGroup(container);
		AddGroup(myLayout, prompt,x,y,promptFontSize, fieldWidth, pagePosition, new String[]{}, false, layout2);
		LinearLayout layout1 = new LinearLayout(container);


		layout2.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		if (useAbsolutePos) {
			((RelativeLayout.LayoutParams)layout2.getLayoutParams()).leftMargin = (int) Math.round(formWidth * x);
			//((RelativeLayout.LayoutParams)layout2.getLayoutParams()).topMargin = (int) Math.round(formHeight * y + (pagePosition * formHeight));
			//params.leftMargin = (int) Math.round(formWidth * x);
			//params.topMargin = (int) Math.round(formHeight * y + (pagePosition * formHeight));
		}
		layout1.setOrientation(LinearLayout.VERTICAL);

		layout1.setLayoutParams(params);		
		layout1.setId(fieldCounter);
		myLayout.addView(layout1);
		if (this.useAbsolutePos)
		{
			myLayout.removeView(vertLayout);
			layout1.addView(vertLayout);
		}
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), layout2);
		//controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);



		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();

		int cumWidth = 0;
		for (int i=0;i<listValues.size();i++)
		{
			RadioButton btn = new RadioButton(container);
			btn.setId(((fieldCounter + 1) * 10000) + i);
			btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			btn.setText(listValues.get(i));
			if (useAbsolutePos && controlFontSize > 0)
			{
				//btn.setTextSize((float)(controlFontSize * 1.8));
				double sp = DeviceManager.GetFontSize(controlFontSize);
				btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);

				Rect bounds = new Rect();
				btn.getPaint().getTextBounds(btn.getText().toString(), 0, btn.getText().length(), bounds);

				cumWidth += bounds.width() + (12);
			}
			layout2.addView(btn);
		}
		layout2.setPadding(0, 5, 0, 0);

		clickCheckCodes.put(layout2, checkCodeAfter);
		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);

		layout2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				executingView = group;
				String clickCheckCode = clickCheckCodes.get(group);
				if(ClickRule!=null) ClickRule.Execute();
			}
		});

		double groupWidth = formWidth * fieldWidth;
		if (useAbsolutePos)
		{
			if (groupWidth > cumWidth)
			{
				layout2.setOrientation(RadioGroup.HORIZONTAL);
			}
			else
			{
				layout2.setOrientation(RadioGroup.VERTICAL);
			}
		}
		else
		{
			if (pattern != null) {
				if (pattern.toLowerCase().contains("horizontal")) {
					layout2.setOrientation(RadioGroup.HORIZONTAL);
				} else {
					layout2.setOrientation(RadioGroup.VERTICAL);
				}
			}
			else
			{
				layout2.setOrientation(RadioGroup.VERTICAL);
			}
		}
		layout1.addView(layout2);

		return fieldCounter;
	}

	public int AddDropDownField(ViewGroup myLayout, String prompt, LinkedList<String> listValues, LinkedList<String> codeValues, String destinationField, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, boolean isReadOnly, double promptFontSize, boolean isRequired, String checkCodeAfter)
	{
		fieldCounter++;

		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		Spinner spinner = new Spinner(container);
		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			spinner.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			spinner.setLayoutParams(params);
			spinner.setBackgroundResource(R.drawable.spinner);
		}
		spinner.setId(fieldCounter);
		spinner.setPrompt(prompt);


		String[] stringValues = new String[listValues.size()];
		stringValues = listValues.toArray(stringValues);

		ArrayAdapter<CharSequence> adapter;
		if (useAbsolutePos)
		{
			adapter = new ArrayAdapter<CharSequence>(container, R.layout.small_spinner_item, stringValues);
		}
		else
		{
			adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.simple_spinner_item, stringValues);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if (isReadOnly)
		{
			spinner.setEnabled(false);
		}
		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);
		}
		spinner.setAdapter(adapter);
		myLayout.addView(spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		clickCheckCodes.put(spinner, checkCodeAfter);

		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);
		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);

		final LinkedList<String> codes = codeValues;
		final String destField = destinationField;

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,   int pos, long id) {

				if (codes.size() > 1)
				{
					if (controlsByName != null)
					{
						if (controlsByName.containsKey(destField.toLowerCase()))
						{
							if (controlsByName.get(destField.toLowerCase()) instanceof TextView)
							{
								TextView tv = (TextView)controlsByName.get(destField.toLowerCase());
								tv.setText(codes.get(pos));
							}
						}
					}
				}

				if (pos > 0)
				{
					executingView = v;
					String clickCheckCode = clickCheckCodes.get(parent);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule != null) AfterRule.Execute();
					if(ClickRule != null) ClickRule.Execute();

				}


			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		return fieldCounter;
	}

	public int AddAutoCompleteField(ViewGroup myLayout, String prompt, LinkedList<String> listValues, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, double promptFontSize, boolean isRequired, String checkCodeAfter)
	{
		fieldCounter++;

		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		AutoCompleteTextView spinner = new AutoCompleteTextView(container);
		spinner.setThreshold(1);

		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(container,android.R.layout.select_dialog_item,AppManager.GetCurrentDatabase().getDistinctFieldValues(formMetadata.Fields.get(fieldCounter).getName()));
		//((AutoCompleteTextView)spinner).setAdapter(adapter);

		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			spinner.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			spinner.setLayoutParams(params);
			spinner.setBackgroundResource(R.drawable.textbox);
		}
		spinner.setId(fieldCounter);

		if (isRequired)
		{
			requiredViewIds.add(fieldCounter);
		}

		String[] stringValues = new String[listValues.size()];
		stringValues = listValues.toArray(stringValues);

		ArrayAdapter<CharSequence> adapter;

		if (useAbsolutePos)
		{
			adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.select_dialog_item, stringValues);
		}
		else
		{
			adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.select_dialog_item, stringValues);
		}
		adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		spinner.setAdapter(adapter);
		myLayout.addView(spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);

		clickCheckCodes.put(spinner, checkCodeAfter);

		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,   int pos, long id) {

				if (pos > 0)
				{
					executingView = v;
					String clickCheckCode = clickCheckCodes.get(parent);
					//processor.Execute(clickCheckCode, "After");
					if(AfterRule !=null) AfterRule.Execute();

				}


			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		return fieldCounter;
	}

	public int AddYesNoField(ViewGroup myLayout, String prompt, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, int pagePosition, boolean isReadOnly, double promptFontSize, String checkCodeAfter)
	{
		fieldCounter++;

		View label = AddLabel(myLayout, prompt, promptX, promptY, pagePosition, promptFontSize);

		Spinner spinner = new Spinner(container);
		int preferredHeight = formHeight * fieldHeight > 35 ? (int)Math.round(formHeight * fieldHeight) : 35;
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			spinner.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), preferredHeight);
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			spinner.setLayoutParams(params);
		}
		spinner.setId(fieldCounter);
		spinner.setPrompt(prompt);
		if (isReadOnly)
		{
			spinner.setEnabled(false);
		}

		ArrayAdapter<CharSequence> adapter;
		if (useAbsolutePos)
		{
			adapter = ArrayAdapter.createFromResource(container, R.array.yn_array, R.layout.small_spinner_item);
			spinner.setBackgroundResource(R.drawable.spinner);
		}
		else
		{
			adapter = ArrayAdapter.createFromResource(container, R.array.yn_array, android.R.layout.simple_spinner_item);
		}

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		myLayout.addView(spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), spinner);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase() + "|prompt", label);
		clickCheckCodes.put(spinner, checkCodeAfter);

		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		//final EnterRule BeforeRule = this.Context.GetCommand("level=field&event=before&identifier=" + FieldName);
		final EnterRule AfterRule = this.Context.GetCommand("level=field&event=after&identifier=" + FieldName);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,   int pos, long id) {
				if (pos > 0)
				{
					executingView = v;
					String clickCheckCode = clickCheckCodes.get(parent);
					//processor.Execute(clickCheckCode, "After");

					if(AfterRule!=null) AfterRule.Execute();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		return fieldCounter;
	}

	public int AddRelateField(ViewGroup myLayout, String text, double x, double y, double fieldWidth, double fieldHeight, double fontSize, int pagePosition, final boolean shouldReturnToParent, String checkCodeClick)
	{
		fieldCounter++;
		Button btn = new Button(container);
		btn.setText(text);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			btn.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), (int)Math.round(formHeight * fieldHeight));
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			btn.setLayoutParams(params);

			if (fontSize > 0)
			{
				//btn.setTextSize((float)(fontSize * 1.8))
				double sp = DeviceManager.GetFontSize(fontSize - 1);
				//tv.setTextSize((float)(promptFontSize * 1.8));
				btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
			}
		}

		btn.setId(fieldCounter);
		myLayout.addView(btn);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), btn);
		clickCheckCodes.put(btn, checkCodeClick);


		final String relateFieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + relateFieldName);

		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if(ClickRule != null) ClickRule.Execute();

				Intent recordList = new Intent(container, RecordList.class);
				recordList.putExtra("ViewName", "_" + relateFieldName);
				recordList.putExtra("FKEY", AppManager.GetFormGuid(container));
				recordList.putExtra("ShouldReturnToParent", shouldReturnToParent);
				container.startActivity(recordList);
			}
		});

		return fieldCounter;
	}

	public int AddButtonField(ViewGroup myLayout, String text, double x, double y, double fieldWidth, double fieldHeight, double fontSize, int pagePosition, String checkCodeClick)
	{
		fieldCounter++;
		Button btn = new Button(container);
		btn.setText(text);
		if (!this.useAbsolutePos)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); 
			btn.setLayoutParams(params);
		}
		else
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(formWidth * fieldWidth), (int)Math.round(formHeight * fieldHeight));
			params.leftMargin = (int)Math.round(formWidth * x);
			params.topMargin = (int)Math.round(formHeight * y + (pagePosition * formHeight));
			btn.setLayoutParams(params);

			if (fontSize > 0)
			{
				//btn.setTextSize((float)(fontSize * 1.8))
				double sp = DeviceManager.GetFontSize(fontSize - 1);
				//tv.setTextSize((float)(promptFontSize * 1.8));
				btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)sp);
			}
		}

		btn.setId(fieldCounter);
		myLayout.addView(btn);
		controlsByName.put(formMetadata.Fields.get(fieldCounter).getName().toLowerCase(), btn);
		clickCheckCodes.put(btn, checkCodeClick);


		String FieldName = formMetadata.Fields.get(fieldCounter).getName().toLowerCase();
		final EnterRule ClickRule = this.Context.GetCommand("level=field&event=click&identifier=" + FieldName);


		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				executingView = v;
				String clickCheckCode = clickCheckCodes.get(v);
				//processor.Execute(clickCheckCode, "Click");
				if(ClickRule != null) ClickRule.Execute();
			}
		});

		return fieldCounter;
	}

	public void AddSpinner(RelativeLayout myLayout, String[] items, double x, double y)
	{
		Spinner spinner = new Spinner(container);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(container, android.R.layout.simple_spinner_dropdown_item, items);
		spinner.setAdapter(adapter);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int)Math.round(formWidth * x);
		params.topMargin = (int)Math.round(formHeight * y);
		spinner.setLayoutParams(params);
		myLayout.addView(spinner);
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			//String date = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
			//String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

			try
			{
				DateFormat dateFormat = DateFormat.getDateInstance();
				Calendar cal = GregorianCalendar.getInstance();
				cal.set(year, monthOfYear, dayOfMonth);
				((EditText)container.findViewById(currentDateFieldId)).setText(dateFormat.format(cal.getTime()));
				container.removeDialog(0);
			}
			catch (Exception ex)
			{

			}
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {

			try
			{
				DateFormat timeFormat = DateFormat.getTimeInstance();
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.SECOND, 0);

				//String time = new SimpleDateFormat("h:mm a").format(datetime.getTime());
				//currentDateField.setText(time);

				((EditText)container.findViewById(currentDateFieldId)).setText(timeFormat.format(cal.getTime()));
				container.removeDialog(1);
			}
			catch (Exception ex)
			{

			}
		}
	};

	public Dialog onCreateDialog(int id)
	{
		if (id == 0)
		{
			return new DatePickerDialog(container,mDateSetListener,Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}
		else if (id == 1)
		{
			Date now = new Date();
			return new TimePickerDialog(container,mTimeSetListener,now.getHours(),now.getMinutes(),false);
		}
		return null;
	}

	public boolean RequiredFieldsComplete()
	{
		boolean retval = true;
		for (int x = 0; x < requiredViewIds.size(); x++)
		{
			Object view = container.findViewById(requiredViewIds.get(x));
			if (view.getClass() == EditText.class || view.getClass() == AutoCompleteTextView.class)
			{
				EditText v = container.findViewById(requiredViewIds.get(x));
				if (v.getText().toString().equals(""))
				{
					retval = false;
					v.setError(container.getString(R.string.required_field));
				}
				else
				{
					v.setError(null);
				}
			}
			else if (view.getClass() == Spinner.class)
			{
				Spinner v = container.findViewById(requiredViewIds.get(x));
				if (v.getSelectedItemPosition() == 0)
				{
					retval = false;
					((TextView)v.getSelectedView()).setError(container.getString(R.string.required_field));
				}
				else
				{
					((TextView)v.getSelectedView()).setError(null);
				}
			}
		}
		return retval;
	}

}