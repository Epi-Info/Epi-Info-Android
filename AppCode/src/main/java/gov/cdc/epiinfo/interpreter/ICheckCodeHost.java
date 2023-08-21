package gov.cdc.epiinfo.interpreter;

import android.widget.ImageView;

public interface ICheckCodeHost 
{
        boolean Register(IInterpreter enterInterpreter);
        boolean IsExecutionEnabled();
        boolean IsSuppressErrorsEnabled();
        boolean Assign(String pName, Object pValue);
        boolean Geocode(String address, String latName, String longName);
        void AutoSearch(String[] pIdentifierList, String[] pDisplayList, boolean pAlwaysShow);
        void Clear(String[] pIdentifierList);
        void Dialog(String pTextPrompt, String pTitleText);
        void Dialog(String pTextPrompt, Object pVariable, String pListType, String pTitleText);
        boolean Dialog(String text, String caption, String mask, String modifier, Object input);
        Object GetValue(String pName);
        void GoTo(String pDesitnation);
        void CaptureCoordinates(String latitudeField, String longitudeField);
        void CaptureBarcode(String field);
        void CaptureHandwriting(final String button, final String fileNameField, final String statusField);
        void ForceSave();
        void Hide(String[] pNameList, boolean pIsAnExceptList);
        void Highlight(String[] pNameList, boolean pIsAnExceptList);
        void UnHighlight(String[] pNameList, boolean pIsAnExceptList);
        void Enable(String[] pNameList, boolean pIsAnExceptList);
        void Disable(String[] pNameList, boolean pIsAnExceptList);
        void Clear(String[] pNameList, boolean pIsAnExceptList);
        void ExecuteUrl(String text);
        void DisplayMedia(String text);
        void Alert(String text);
        void NewRecord();
        int RecordCount();
        void UnHide(String[] pNameList, boolean pIsAnExceptList);
        void Quit();
		void StartCamera(ImageView v);

}
