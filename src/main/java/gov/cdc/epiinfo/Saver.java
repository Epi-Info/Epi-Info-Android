package gov.cdc.epiinfo;

import android.content.ContentValues;
import android.os.Bundle;

public class Saver {

	private static ContentValues GetContentValues(Bundle extras, FormMetadata formMetadata)
	{
		ContentValues initialValues = new ContentValues();
		for (int x=0;x<formMetadata.DataFields.size();x++)
		{
			if (formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("18") || formMetadata.DataFields.get(x).getType().equals("98"))
			{
				initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getInt(formMetadata.DataFields.get(x).getName()));
			}
			else if (formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19"))
			{
				if (formMetadata.DataFields.get(x).getListValues().size() > 100)
				{
					initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getString(formMetadata.DataFields.get(x).getName()));
				}
				else
				{
					initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getInt(formMetadata.DataFields.get(x).getName()));
				}
			}
			else if (formMetadata.DataFields.get(x).getType().equals("5"))
			{
				initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getDouble(formMetadata.DataFields.get(x).getName()));
			}
			else if (formMetadata.DataFields.get(x).getType().equals("10"))
			{
				if (extras.getInt(formMetadata.DataFields.get(x).getName()) == 1)
				{
					initialValues.put(formMetadata.DataFields.get(x).getName(), true);
				}
				else
				{
					initialValues.put(formMetadata.DataFields.get(x).getName(), false);
				}
			}
			else
			{
				initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getString(formMetadata.DataFields.get(x).getName()));
			}
		}

		return initialValues;
	}

	public static long Insert(Bundle extras, FormMetadata formMetadata, EpiDbHelper dbHelper, String newGuid, String fkeyGuid)
	{
		if (extras != null)
		{
			ContentValues initialValues = GetContentValues(extras, formMetadata);
			return dbHelper.createRecord(initialValues, true, newGuid, fkeyGuid);
		}
		else
			return -1;
	}

	public static void Update(Bundle extras, FormMetadata formMetadata, EpiDbHelper dbHelper)
	{
		if (extras != null)
		{
			ContentValues initialValues = GetContentValues(extras, formMetadata);
			Long mRowId = extras.getLong(EpiDbHelper.KEY_ROWID);
			String mRowGuid = extras.getString(EpiDbHelper.GUID);
			if (mRowId != null)
			{
				dbHelper.updateRecord(mRowId, initialValues, true);
			}
		}
	}

}
