package gov.cdc.epiinfo.interpreter;

import gov.cdc.epiinfo.AppManager;
import gov.cdc.epiinfo.interpreter.CSymbol.VariableScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class CSymbolTable 
{
    private String _name;
    private CSymbolTable _parent;
    private HashMap<String, CSymbol> _SymbolList;
    
    
    public CSymbolTable()
    {
        this._name = null;
        this._parent = null;
        this._SymbolList = new HashMap<String, CSymbol>();
    }

    public CSymbolTable(CSymbolTable pParent)
    {
        this._name = null;
        this._parent = pParent;
        this._SymbolList = new HashMap<String, CSymbol>();
    }

    public CSymbolTable(String pName, CSymbolTable pParent)
    {
        this._name = pName;
        this._parent = pParent;
        this._SymbolList = new HashMap<String, CSymbol>();
    }

    public String getName() { return this._name; }
    public void  setName(String pValue){ this._name = pValue; }
    

    public CSymbolTable getEnclosingScope()
    {
        return this._parent;
    }

    public void define(CSymbol pSymbol, String pNamespace)
    {
        // ensure that Permanent and Global variables are placed in global scope
        if (pSymbol.VariableScope == VariableScope.Permanent || pSymbol.VariableScope == VariableScope.Global)
        {
            if (this._parent != null)
            {
                this._parent.define(pSymbol, pNamespace);
            }
            if (pSymbol.VariableScope == VariableScope.Permanent)
            {
            	AppManager.SetPermanentVariable(pSymbol.Name, "");
            	this._SymbolList.put(pSymbol.Name, pSymbol);
            }
        }
        else if (!isNullOrEmpty(pNamespace) && !this._name.equalsIgnoreCase(pNamespace))
        {
            if (this._parent != null)
            {
                this._parent.define(pSymbol, pNamespace);
            }
        }
        else
        {
            if (this._SymbolList.containsKey(pSymbol.Name))
            {
                // maybe throw error duplicate symbol in scope?
                // instead of error redefining
                this._SymbolList.put(pSymbol.Name, pSymbol);
            }
            else
            {

                this._SymbolList.put(pSymbol.Name, pSymbol);
            }
        }
    }

    public void undefine(String pName,  String pNamespace)
    {
        if(!isNullOrEmpty(pNamespace) && (!isNullOrEmpty(this._name) && !this._name.equalsIgnoreCase(pNamespace)))
        {
            if (this._parent != null)
            {
                this._parent.undefine(pName, pNamespace);
            }
        }
        else if (this._SymbolList.containsKey(pName))
        {
            this._SymbolList.remove(pName);
        }
        else if (this._parent == null)
        {
            // maybe throw error  symbol not in scope
        }
        else
        {
            this._parent.undefine(pName, pNamespace);
        }
    }

    public CSymbol resolve(String pName, String pNamespace)
    {
        CSymbol result = null;

        if(!isNullOrEmpty(pNamespace) && (!isNullOrEmpty(this._name) && !this._name.equalsIgnoreCase(pNamespace)))
        {
            if (this._parent != null)
            {
                result = this._parent.resolve(pName, pNamespace);
            }
        }
        else if (this._SymbolList.containsKey(pName))
        {
            result = this._SymbolList.get(pName);
        }
        else
        {
            if (this._parent != null)
            {
                result = this._parent.resolve(pName, pNamespace);
            }
        }
        if (AppManager.GetPermanentVariable(pName) != null)
        {
        	result = new CSymbol(pName, CSymbol.DataType.Text);
        	result.VariableScope = VariableScope.Permanent;
        	result.Value = AppManager.GetPermanentVariable(pName);
        }
        
        return result;
    }


    public Boolean SymbolIsInScope(String pName)
    {
        return this._SymbolList.containsKey(pName);
    }

    public HashMap<String, CSymbol> getSymbolList() {  return this._SymbolList;  }

    public List<CSymbol> FindVariables(VariableScope pScopeCombination, String pNamespace)
    {
        List<CSymbol> result = new ArrayList<CSymbol>();

        if (!isNullOrEmpty(pNamespace) && (!isNullOrEmpty(this._name) && !this._name.equalsIgnoreCase(pNamespace)))
        {
            if (this._parent != null)
            {
                result.addAll(this._parent.FindVariables(pScopeCombination, pNamespace));
            }
        }
        else
        {
            for(Entry<String, CSymbol> entry : _SymbolList.entrySet())
            {
            	
                if ((entry.getValue().VariableScope.getCode() & pScopeCombination.getCode()) > 0)
                {
                    result.add(entry.getValue());
                }
            }
        

            if (this._parent != null)
            {
                result.addAll(this._parent.FindVariables(pScopeCombination, pNamespace));
            }
        }
        return result;
    }


    public void RemoveVariablesInScope(VariableScope pScopeCombination, String pNamespace)
    {
        if (!isNullOrEmpty(pNamespace) && (!isNullOrEmpty(this._name) && !this._name.equalsIgnoreCase(pNamespace)))
        {
            if (this._parent != null)
            {
                this._parent.RemoveVariablesInScope(pScopeCombination, pNamespace);
            }
        }
        else
        {

            for (Entry<String, CSymbol> kvp : _SymbolList.entrySet())
            {

                

                if ((kvp.getValue().VariableScope.getCode() & pScopeCombination.getCode()) > 0)
                {
                    this.undefine(kvp.getKey(), pNamespace);
                }
            }

            if (this._parent != null)
            {
                this._parent.RemoveVariablesInScope(pScopeCombination, pNamespace);
            }
        }

    }
    
    
    public static boolean isNullOrEmpty(String param) 
    {
        return param == null || param.trim().length() == 0;
    }
}
