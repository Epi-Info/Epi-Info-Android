package gov.cdc.epiinfo.interpreter;

public interface IInterpreter 
{
    String getName();
    ICheckCodeHost getHost();
    void setHost(ICheckCodeHost pValue);
    void Parse(String pCommandText);
    void Execute(String pCommandText);
    Rule_Context getContext();
    void setContext(Rule_Context pValue);
}
