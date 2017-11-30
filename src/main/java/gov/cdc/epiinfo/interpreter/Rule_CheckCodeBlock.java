package gov.cdc.epiinfo.interpreter;

import com.creativewidgetworks.goldparser.engine.Reduction;


public class Rule_CheckCodeBlock extends EnterRule 
{
    EnterRule CheckCodeBlock = null;

    public Rule_CheckCodeBlock(Rule_Context pContext, Reduction pToken)
    {
        super(pContext);
        /* <CheckCodeBlock> ::=   <DefineVariables_Statement>
                            | <View_Checkcode_Statement>
                            | <Record_Checkcode_Statement>
                | <Page_Checkcode_Statement>  
                | <Field_Checkcode_Statement>
                | <Subroutine_Statement>  */

        Reduction reduction = (Reduction) pToken.get(0).getData();
        switch (Rule_Enum.Convert(pToken.getParent().getHead().getName()))
        {
            case DefineVariables_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
            case View_Checkcode_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
            case Record_Checkcode_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
            case Page_Checkcode_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
            case Field_Checkcode_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
            case Subroutine_Statement:
                this.CheckCodeBlock = EnterRule.BuildStatements(pContext, reduction);
                break;
             default:
            	 break;
        }
    }



    /// <summary>
    ///executes CheckCodeBlock statement
    /// </summary>
    /// <returns>object</returns>
    @Override
    public Object Execute()
    {
        CheckCodeBlock.Execute();

        return null;
    }

    @Override
    public boolean IsNull() { return CheckCodeBlock == null; } 
}
