package gov.cdc.epiinfo;

import java.util.Date;
import java.util.LinkedList;

public class Field {

	private String fieldName;
	private String fieldType;
	private String prompt;
	private String fieldFontStyle;
	private String pattern;
	private LinkedList<String> listValues;
	private LinkedList<String> codeValues;
	private String destinationField;
	private int id;
	private double x;
	private double y;
	private double promptX;
	private double promptY;
	private double fieldWidth;
	private double fieldHeight;
	private double fieldFontSize;
	private double promptFontSize;
	private int position;
	private int pageId;
	private String pageName;
	private boolean isRequired;
	private boolean isReadOnly;
	private boolean shouldRepeatLast;
	private boolean shouldReturnToParent;
	private int maxLength;
	private double lower;
	private double upper;
	private Date lowerDate;
	private Date upperDate;
	private String[] list;
	private int likertAnswerIndex;

	public Field(String fieldName, String prompt, String fieldType, double x, double y, double promptX, double promptY, double fieldWidth, double fieldHeight, double fieldFontSize, String fieldFontStyle, double promptFontSize, int position, boolean isRequired, boolean isReadOnly, boolean shouldRepeatLast, boolean shouldReturnToParent, int maxLength, double lower, double upper, Date lowerDate, Date upperDate, String pattern, String[] list, int pageId, String pageName)
	{
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.prompt = prompt;
		this.x = x;
		this.y = y;
		this.promptX = promptX;
		this.promptY = promptY;
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.fieldFontSize = fieldFontSize;
		this.fieldFontStyle = fieldFontStyle;
		this.promptFontSize = promptFontSize;
		this.position = position;
		this.pageId = pageId;
		this.pageName = pageName;
		this.isRequired = isRequired;
		this.isReadOnly = isReadOnly;
		this.shouldRepeatLast = shouldRepeatLast;
		this.shouldReturnToParent = shouldReturnToParent;
		this.maxLength = maxLength;
		this.lower = lower;
		this.upper = upper;
		this.lowerDate = lowerDate;
		this.upperDate = upperDate;
		this.pattern = pattern;
		this.list = list;
	}
	
	public void setListValues(LinkedList<String> listValues)
	{
		this.listValues=listValues;
	}
	
	public LinkedList<String> getListValues()
	{
		return listValues;
	}
	
	public void setCodeValues(LinkedList<String> codeValues)
	{
		this.codeValues=codeValues;
	}
	
	public LinkedList<String> getCodeValues()
	{
		return codeValues;
	}
	
	public void setDestinationField(String destinationField)
	{
		this.destinationField = destinationField;
	}
	
	public String getDestinationField()
	{
		return destinationField;
	}
	
	public String getName()
	{
		return fieldName;
	}
	
	public String getPageName()
	{
		return pageName;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getType()
	{
		return fieldType;
	}
	
	public void setType(String fieldType)
	{
		this.fieldType = fieldType;
	}
	
	public String getPrompt()
	{
		return prompt;
	}
	
	public String[] getList()
	{
		return list;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public double getPromptX()
	{
		return promptX;
	}
	
	public double getPromptY()
	{
		return promptY;
	}
	
	public double getFieldWidth()
	{
		return fieldWidth;
	}
	
	public double getFieldHeight()
	{
		return fieldHeight;
	}
	
	public double getFieldFontSize()
	{
		return fieldFontSize;
	}
	
	public String getFieldFontStyle()
	{
		return fieldFontStyle;
	}
	
	public double getPromptFontSize()
	{
		return promptFontSize;
	}
	
	public int getPagePosition()
	{
		return position;
	}
	
	public boolean getIsRequired()
	{
		return isRequired;
	}
	
	public boolean getIsReadOnly()
	{
		return isReadOnly;
	}
	
	public boolean getShouldRepeatLast()
	{
		return shouldRepeatLast;
	}

	public boolean getShouldReturnToParent() { return shouldReturnToParent; }
	
	public int getMaxLength()
	{
		return maxLength;
	}
	
	public double getLower()
	{
		return lower;
	}
	
	public double getUpper()
	{
		return upper;
	}
	
	public Date getLowerDate()
	{
		return lowerDate;
	}
	
	public Date getUpperDate()
	{
		return upperDate;
	}
	
	public String getPattern()
	{
		return pattern;
	}
	
	public int getPageId()
	{
		return pageId;
	}

	public int getLikertAnswerIndex()
	{
		return this.likertAnswerIndex;
	}

	public void setLikertAnswerIndex( int answer )
	{
		this.likertAnswerIndex = answer;
	}
	
}
