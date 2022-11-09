package kut.compiler.symboltable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import kut.compiler.exception.CompileErrorException;
import kut.compiler.exception.SyntaxErrorException;
import kut.compiler.lexer.Token;
import kut.compiler.lexer.TokenClass;
import kut.compiler.parser.ast.AstFunDef;
import kut.compiler.parser.ast.AstGlobal;
import kut.compiler.parser.ast.AstLocal;

public class SymbolTable 
{	
	protected Map<String, AstGlobal> 			globalVariables		;
	protected Map<String, LocalVariableInfo>	localVariables		;
	protected Map<String, FunDefInfo>			functions			;		
	
	protected Map<String, String>				stringLiteralLabels		;
	protected int 								intStringLiteralIndex	;

	

	/**
	 * 
	 */
	public SymbolTable() {
		globalVariables 		= new HashMap<String, AstGlobal>();
		stringLiteralLabels  	= new HashMap<String, String>();
		functions				= new HashMap<String, FunDefInfo>();
		intStringLiteralIndex 	= 0;
	}
	
	
	/**
	 * @param idenfier
	 * @return
	 */
	public ExprType getVariableType(String idenfier) throws CompileErrorException
	{	
		int t = TokenClass.ERROR;
		if (localVariables.containsKey(idenfier)) {
			LocalVariableInfo info = localVariables.get(idenfier);
			t = info.node.getTypeToken().getC();
		}
		else if (globalVariables.containsKey(idenfier)) {
			t = globalVariables.get(idenfier).getTypeToken().getC();
		}
		else if (functions.containsKey(idenfier)) {
			return ExprType.FUNCTION;
		}	
		return this.converTokenClassToExprType(t);
	}
	

	/**
	 * @param t
	 * @return
	 */
	public ExprType converTokenClassToExprType(int tc)
	{
		switch(tc) {
		case TokenClass.INT:
			return ExprType.INT;
		
		case TokenClass.STRING:
			return ExprType.STRING;
			
		case TokenClass.DOUBLE:
			return ExprType.DOUBLE;
			
		case TokenClass.BOOLEAN:
			return ExprType.BOOLEAN;
		default:
			break;
		}
		return ExprType.ERROR;
	}
	
	/**
	 * @param label
	 */
	public void foundStringLiteral(String literal) 
	{
		
		if (this.getStingLiteralLabel(literal) != null) {
			return;
		}
		
		String label = "string_literal#" + intStringLiteralIndex;
		intStringLiteralIndex++;
		this.stringLiteralLabels.put(literal, label);
		return;
	}
	
	
	/**
	 * @param string
	 * @return
	 */
	public String getStingLiteralLabel(String literal) 
	{
		if (this.stringLiteralLabels.containsKey(literal) == false) {
			return null;
		}
		return this.stringLiteralLabels.get(literal);
	}
	
	/**
	 * @return
	 */
	public List<StringLiteralAndLabel> getStringLabels(){
		LinkedList<StringLiteralAndLabel> ls = new LinkedList<StringLiteralAndLabel>();
		for (String k: this.stringLiteralLabels.keySet()) {
			StringLiteralAndLabel l = new StringLiteralAndLabel();
			l.literal 	= k;
			l.label		= this.getStingLiteralLabel(k);
			ls.add(l);
		}
		return ls;
	}
	
	/**
	 * @param varname
	 * @throws SyntaxErrorException
	 */
	public void declareGlobalVariable(AstGlobal gvar) 
	{
		String varname = gvar.getVarName().getIdentifier();
		globalVariables.put(varname, gvar);
	}
	
	/**
	 * @param id
	 * @return
	 */
	public SymbolType getSymbolType(String id)
	{
		if (localVariables.containsKey(id)) {
			return  SymbolType.LocalVariable;
		}

		if (globalVariables.containsKey(id)) {
			return SymbolType.GlobalVariable; 
		}
		
		return SymbolType.Unknown;
	}
	
	
	/**
	 * @return
	 */
	public List<String> getGlobalVariables()
	{
		return new LinkedList<String>(globalVariables.keySet());
	}
	

	/**
	 * 
	 */
	public void printGlobalVariables() 
	{	
		System.out.println("the list of global variables");
		for (String id: globalVariables.keySet()) {
			System.out.println(globalVariables.get(id));
		}
	}
	
	
	/**
	 * @param lvar
	 * @throws SyntaxErrorException
	 */
	public void declareLocalVariable(AstLocal lvar) throws SyntaxErrorException
	{
		this.declareLocalVariable(lvar, false);
	}
	
	/**
	 * @param pvar
	 * @throws SyntaxErrorException
	 */
	public void declareFuncParam(AstLocal pvar) throws SyntaxErrorException
	{
		this.declareLocalVariable(pvar, true);
	}
	
	
	/**
	 * @param def
	 * @throws SyntaxErrorException
	 */
	public void declareFunction(AstFunDef def) throws SyntaxErrorException
	{
		String n = def.getFuncName();
		
		if (this.functions.containsKey(n)) {
			throw new SyntaxErrorException("duplication function definition : " + def);
		}
		
		FunDefInfo info = new FunDefInfo();
		info.funDef 	= def;
		info.entryLabel	= "user_func_entry#" + n;
		
		this.functions.put(n, info);
	}
	
	/**
	 * @param fname
	 */
	public String getFunctionEntryLabel(String fname) throws SyntaxErrorException
	{
		if (!this.functions.containsKey(fname)) {
			throw new SyntaxErrorException("the function: " + fname +" is not defined.");
		}
		
		FunDefInfo info = this.functions.get(fname);
		return info.entryLabel;
	}
	
	/**
	 * @param def
	 * @return
	 */
	public ExprType getFunctionReturnType(String fname) 
	{
		if (!this.functions.containsKey(fname)) {
			return ExprType.ERROR;
		}
		
		FunDefInfo info = this.functions.get(fname);
		return info.funDef.getReturnType();
	}
	/**
	 * @param t
	 */
	protected void declareLocalVariable(AstLocal lvar, boolean isFuncParam) throws SyntaxErrorException
	{
		String id = lvar.getVarName().getIdentifier();
		if (localVariables.containsKey(id)){
			throw new SyntaxErrorException("duplicate local variable declarations : " + lvar.getVarName());
		}
		
		LocalVariableInfo i = new LocalVariableInfo();
		i.node = lvar;
		
		if (isFuncParam) {
			i.stackIndex = LocalVariableInfo.FUN_PARM_INDEX_NOT_ASIGNED;
		}
		else {
			i.stackIndex = LocalVariableInfo.LOCAL_VAR_INDEX_NOT_ASIGNED;
		}
		
		this.localVariables.put(id, i);
		return;
	}
	
	
	
	/**
	 * 
	 */
	public void resetLocalVariableTable() {
		this.localVariables = new HashMap<String, LocalVariableInfo>();
	}
	
	
	
	/**
	 * @param vname
	 * @return
	 */
	public int getStackIndexOfLocalVariable(String vname)
	{		
		if (this.localVariables.containsKey(vname)) {
			LocalVariableInfo info = this.localVariables.get(vname);
			return info.stackIndex;
		}
		return 0;
	}
	
	/**
	 * @return
	 */
	public int getStackFrameExtensionSize()
	{
		int min = 0;
		for (LocalVariableInfo s: this.localVariables.values()) {
			min = min > s.stackIndex ? s.stackIndex : min;
		}
				
		return -min;
	}
	

	/**
	 * 
	 */
	public void assignLocalVariableIndices() {
		int localVarIndex = -8; 	// the previous rbp is located at rbp + 0, so got to start from -8.
		int funcParamIndex = 16;	// the return addres is located at rbp + 8.
		for (LocalVariableInfo s: this.localVariables.values()) {
			if (s.stackIndex == LocalVariableInfo.LOCAL_VAR_INDEX_NOT_ASIGNED) {
				s.stackIndex = localVarIndex;
				localVarIndex -= 8;
			}
			else if (s.stackIndex == LocalVariableInfo.FUN_PARM_INDEX_NOT_ASIGNED) {
				s.stackIndex = funcParamIndex;
				funcParamIndex += 8;
			}
		}
	}
	
	/**
	 * @param fname
	 * @return
	 */
	public Vector<ExprType> getFuncParamTypes(String fname) throws CompileErrorException
	{
		if (!this.functions.containsKey(fname)) {
			throw new CompileErrorException("the function name: " + fname + " is not registered.");
		}
		
		Vector<ExprType> paramTypes = new Vector<ExprType>();
		
		FunDefInfo finfo = this.functions.get(fname);
		for(Token t: finfo.funDef.getParamTypeTokens()) {
			paramTypes.add(this.converTokenClassToExprType(t.getC()));
		}
		return paramTypes;
	}

	
}
