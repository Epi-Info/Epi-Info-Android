package gov.cdc.epiinfo;

import android.app.Activity;
import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import gov.cdc.epiinfo.interpreter.Rule_Context;


public class FormMetadata {

	public LinkedList<Field> Fields;
	public LinkedList<Field> DataFields;
	public LinkedList<Field> NumericFields;
	public LinkedList<Field> BooleanFields;
	public LinkedList<Field> TextFields;
	public int Height;
	public int Width;
	public String CheckCode;
	public Rule_Context Context;
	public int PageCount;
	public String[] PageName;
	public int FileVersion;
	private Activity callingActivity;
	public boolean IsInterviewForm;
	public boolean HasImageFields;
	public boolean HasMediaFields;
	private String surveyId;

	public FormMetadata(String viewXmlFile, Activity container)
	{
		try
		{
			callingActivity = container;
			Context = null;
			Fields = new LinkedList<Field>();
			DataFields = new LinkedList<Field>();
			NumericFields = new LinkedList<Field>();
			TextFields = new LinkedList<Field>();
			BooleanFields = new LinkedList<Field>();
			CheckCode = "";
			IsInterviewForm = false;
			HasImageFields = false;
			HasMediaFields = false;
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File file = new File(path, viewXmlFile);        		

			if (file.lastModified() > 0)
			{
				FileVersion = (int)(file.lastModified() % 1000000000)/1000;
			}
			else
			{
				FileVersion = 1;
			}

			InputStream obj_is = null; 
			Document obj_doc = null; 
			DocumentBuilderFactory doc_build_fact = null; 
			DocumentBuilder doc_builder = null; 
			obj_is = new FileInputStream(file); 
			doc_build_fact = DocumentBuilderFactory.newInstance(); 
			doc_builder = doc_build_fact.newDocumentBuilder(); 

			obj_doc = doc_builder.parse(obj_is); 
			NodeList obj_nod_list = null; 
			if(null != obj_doc) 
			{ 
				Element feed = obj_doc.getDocumentElement(); 
				NodeList obj_view_list = feed.getElementsByTagName("View");
				Height = Integer.parseInt(obj_view_list.item(0).getAttributes().getNamedItem("Height").getNodeValue());
				Width = Integer.parseInt(obj_view_list.item(0).getAttributes().getNamedItem("Width").getNodeValue());
                IsInterviewForm = Height == 780 && Width == 549;

				try
				{
					surveyId = obj_view_list.item(0).getAttributes().getNamedItem("SurveyId").getNodeValue();
				}
				catch (Exception ex)
				{
					int x=5;
					x++;

				}

				CheckCode = obj_view_list.item(0).getAttributes().getNamedItem("CheckCode").getNodeValue().replace("://", "::").replaceAll("(?s)(/\\*{1})(.*)(\\*/{1})", "").replaceAll("(//{1})(.*)", "");

				NodeList page_list = feed.getElementsByTagName("Page");
				PageCount = page_list.getLength();
				PageName = new String[PageCount];

				for (int i=0; i<PageCount; i++)
				{
					int pagePosition = Integer.parseInt(page_list.item(i).getAttributes().getNamedItem("Position").getNodeValue());
					int pageId = Integer.parseInt(page_list.item(i).getAttributes().getNamedItem("PageId").getNodeValue());
					obj_nod_list = page_list.item(i).getChildNodes();
					
					PageName[pagePosition] = page_list.item(i).getAttributes().getNamedItem("Name").getNodeValue();
					for (int x=0;x<obj_nod_list.getLength();x++)
					{
						NamedNodeMap test = obj_nod_list.item(x).getAttributes();
						if (test != null)
						{
							String fieldName = obj_nod_list.item(x).getAttributes().getNamedItem("Name").getNodeValue().replace(" ", "");
							String prompt = obj_nod_list.item(x).getAttributes().getNamedItem("PromptText").getNodeValue();
							String fieldType = obj_nod_list.item(x).getAttributes().getNamedItem("FieldTypeId").getNodeValue();
							double fieldX = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlLeftPositionPercentage").getNodeValue().replace(',','.'));
							double fieldY = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlTopPositionPercentage").getNodeValue().replace(',','.'));
							double fieldHeight = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlHeightPercentage").getNodeValue().replace(',','.'));
							double fieldWidth = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlWidthPercentage").getNodeValue().replace(',','.'));
							double controlFontSize = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("ControlFontSize").getNodeValue().replace(',','.'));
							String controlFontStyle = obj_nod_list.item(x).getAttributes().getNamedItem("ControlFontStyle").getNodeValue();
							String pattern = obj_nod_list.item(x).getAttributes().getNamedItem("Pattern").getNodeValue();
							String[] list = obj_nod_list.item(x).getAttributes().getNamedItem("List").getNodeValue().split(",");

							double lower = Double.MAX_VALUE * -1;
							double upper = Double.MAX_VALUE;
							Date lowerDate = new Date();
							lowerDate.setTime(0);
							lowerDate.setYear(0);
							lowerDate.setMonth(0);
							lowerDate.setDate(1);
							lowerDate.setHours(0);
							lowerDate.setMinutes(0);
							lowerDate.setSeconds(0);
							Date upperDate = new Date();
							upperDate.setYear(3000);
							upperDate.setHours(23);
							upperDate.setMinutes(59);

							try
							{
								String lowerText = obj_nod_list.item(x).getAttributes().getNamedItem("Lower").getNodeValue();
								lower = Double.MAX_VALUE * -1;
								if (!lowerText.equals(""))
								{
									lower = Double.parseDouble(lowerText.replace(',','.'));
								}
								String upperText = obj_nod_list.item(x).getAttributes().getNamedItem("Upper").getNodeValue();
								upper = Double.MAX_VALUE;
								if (!upperText.equals(""))
								{
									upper = Double.parseDouble(upperText.replace(',','.'));
								}
							}
							catch (Exception ex)
							{

							}
							
							try
							{
								String lowerText = obj_nod_list.item(x).getAttributes().getNamedItem("Lower").getNodeValue();
								if (!lowerText.equals(""))
								{
									if (lowerText.toLowerCase().equals("systemdate") || lowerText.toLowerCase().equals("systemtime"))
									{
										lowerDate = new Date();
									}
									else if (lowerText.contains("-"))
									{
										String[] parts = lowerText.split("-");
										lowerDate.setYear(Integer.parseInt(parts[0]) - 1900);
										lowerDate.setMonth(Integer.parseInt(parts[1]) - 1);
										lowerDate.setDate(Integer.parseInt(parts[2]));
									}
								}
								String upperText = obj_nod_list.item(x).getAttributes().getNamedItem("Upper").getNodeValue();
								if (!upperText.equals(""))
								{
									if (upperText.toLowerCase().equals("systemdate") || upperText.toLowerCase().equals("systemtime"))
									{
										upperDate = new Date();
									}
									else if (upperText.contains("-"))
									{
										String[] parts = upperText.split("-");
										upperDate.setYear(Integer.parseInt(parts[0]) - 1900);
										upperDate.setMonth(Integer.parseInt(parts[1]) - 1);
										upperDate.setDate(Integer.parseInt(parts[2]));
									}
								}
							}
							catch (Exception ex)
							{

							}

							double promptFontSize = 0;
							double promptX = 0;
							double promptY = 0;
							boolean isRequired = false;
							boolean isReadOnly = false;
							boolean shouldRepeatLast = false;
							boolean shouldReturnToParent = false;
							try
							{
								promptFontSize = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptFontSize").getNodeValue().replace(',','.'));
								promptX = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptLeftPositionPercentage").getNodeValue().replace(',','.'));
								promptY = Double.parseDouble(obj_nod_list.item(x).getAttributes().getNamedItem("PromptTopPositionPercentage").getNodeValue().replace(',','.'));
							}
							catch (Exception ex)
							{
								// 
							}

							try
							{
								isRequired = obj_nod_list.item(x).getAttributes().getNamedItem("IsRequired").getNodeValue().equals("True");
								isReadOnly = obj_nod_list.item(x).getAttributes().getNamedItem("IsReadOnly").getNodeValue().equals("True");
								shouldRepeatLast = obj_nod_list.item(x).getAttributes().getNamedItem("ShouldRepeatLast").getNodeValue().equals("True");
								shouldReturnToParent = obj_nod_list.item(x).getAttributes().getNamedItem("ShouldReturnToParent").getNodeValue().equals("True");
							}
							catch (Exception ex)
							{
								//
							}

							int maxLength = 0;
							try
							{
								maxLength = Integer.parseInt(obj_nod_list.item(x).getAttributes().getNamedItem("MaxLength").getNodeValue().replace(',','.'));
							}
							catch (Exception ex)
							{
								//
							}

							Field field = new Field(fieldName, prompt, fieldType, fieldX, fieldY, promptX, promptY, fieldWidth, fieldHeight, controlFontSize, controlFontStyle, promptFontSize, pagePosition, isRequired, isReadOnly, shouldRepeatLast, shouldReturnToParent, maxLength, lower, upper, lowerDate, upperDate, pattern, list, pageId, PageName[pagePosition]);
							if (field.getType().equals("17"))
							{
								AddListValues(field, feed, obj_nod_list.item(x).getAttributes().getNamedItem("TextColumnName").getNodeValue(), null);
							}
							if (field.getType().equals("18"))
							{
								AddListValues(field, feed, obj_nod_list.item(x).getAttributes().getNamedItem("TextColumnName").getNodeValue(), obj_nod_list.item(x).getAttributes().getNamedItem("RelateCondition").getNodeValue().split(":")[0]);
								field.setDestinationField(obj_nod_list.item(x).getAttributes().getNamedItem("RelateCondition").getNodeValue().split(":")[0]);
							}
							if (field.getType().equals("19"))
							{
								AddListValues(field, feed, obj_nod_list.item(x).getAttributes().getNamedItem("TextColumnName").getNodeValue(), null);
							}
							if (field.getType().equals("12"))
							{
								AddListValues(field, obj_nod_list.item(x).getAttributes().getNamedItem("List").getNodeValue());
							}
							Fields.add(field);
							if (!field.getType().equals("2") && !field.getType().equals("21") && !field.getType().equals("13"))
							{
								DataFields.add(field);
							}
							if (field.getType().equals("5"))
							{
								NumericFields.add(field);
							}
							if (field.getType().equals("1"))
							{
								TextFields.add(field);
							}
							if (field.getType().equals("10") || field.getType().equals("11"))
							{
								BooleanFields.add(field);
							}
							if (field.getType().equals("14"))
							{
								HasImageFields = true;
							}
							
							try
							{
								if (obj_nod_list.item(x).getAttributes().getNamedItem("IsVideoLink").getNodeValue().equals("True"))
								{
									field.setType("81");
									HasMediaFields = true;
								}
							}
							catch (Exception ex)
							{
								int w=5;
								w++;
							}
							
							try
							{
								if (obj_nod_list.item(x).getAttributes().getNamedItem("IsAudioLink").getNodeValue().equals("True"))
								{
									field.setType("82");
									HasMediaFields = true;
								}
							}
							catch (Exception ex)
							{
								int w=5;
								w++;
							}

							/*try
							{
								if (obj_nod_list.item(x).getAttributes().getNamedItem("Name").getNodeValue().toLowerCase().endsWith("likert") && field.getType().equals("12"))
								{
									field.setType("98");
								}
							}
							catch (Exception ex)
							{
								int w=5;
								w++;
							}*/
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			int x = 5;
			x++;
		}
	}

	public int GetFieldType(String fieldName)
	{
		for (int x = 0; x < Fields.size(); x++)
		{
			Field f = Fields.get(x);
			if (f.getName().toLowerCase().equals(fieldName.toLowerCase()))
			{
				return Integer.parseInt(f.getType());
			}
		}
		return 0;
	}

	public Field GetFieldByName(String fieldName)
	{
		for (int x = 0; x < Fields.size(); x++)
		{
			Field f = Fields.get(x);
			if (f.getName().equalsIgnoreCase(fieldName))
			{
				return f;
			}
		}
		return null;
	}

	public String GetSurveyId()
	{
		return this.surveyId;
	}

	private void AddListValues(Field field, String list)
	{
		LinkedList<String> listValues = new LinkedList<String>();
		String[] step1 = list.split("\\|");
		String[] step2 = step1[0].split(",");
		for (int x=0;x<step2.length;x++)
		{
			listValues.add(step2[x]);
		}
		field.setListValues(listValues);
	}

	private void AddListValues(Field field, Element feed, String codeColumnName, String codeDestination)
	{
		try
		{
			NodeList obj_nod_list = feed.getElementsByTagName("Item");
			LinkedList<String> listValues = new LinkedList<String>();
			listValues.add(callingActivity.getString(R.string.not_selected));
			LinkedList<String> codeValues = new LinkedList<String>();
			codeValues.add("");

			boolean found = false;
			String currentParent = "";
			for (int x=0;x<obj_nod_list.getLength();x++)
			{
				Node node = obj_nod_list.item(x);
				Node attrib = node.getAttributes().getNamedItem(codeColumnName) != null ? node.getAttributes().getNamedItem(codeColumnName) : node.getAttributes().getNamedItem(codeColumnName.toLowerCase());
				if (attrib != null)
				{
					if (!currentParent.equals("") && !currentParent.equals(node.getParentNode().getAttributes().item(0).getNodeValue()))
						break;
					found = true;
					currentParent = node.getParentNode().getAttributes().item(0).getNodeValue();
					if (!listValues.contains(attrib.getNodeValue()))
					{
						listValues.add(attrib.getNodeValue());
						if (codeDestination != null)
						{
							codeValues.add(node.getAttributes().getNamedItem(codeDestination).getNodeValue());
						}
					}
				}
				else
				{
					if (found)
						break;
				}
			}
			field.setListValues(listValues);
			field.setCodeValues(codeValues);
		}
		catch (Exception ex)
		{
			int x = 5;
			x++;
		}
	}


}
