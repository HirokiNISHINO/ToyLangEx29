package kut.compiler.parser.ast;

import java.io.IOException;
import java.util.Vector;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.lexer.Token;
import kut.compiler.lexer.TokenClass;
import kut.compiler.symboltable.ExprType;

public class AstFunDef extends AstNode 
{
	protected Token functionName;
	protected Token returnType;
	
	protected AstNode		 	functionBody;
	protected Vector<AstLocal>	parameters;
	
	/**
	 * @param node
	 * @param platform
	 */
	public AstFunDef(Token functionName, Token returnType, AstNode functionBody, Vector<AstLocal> parameters)
	{
		this.functionName	= functionName	;
		this.returnType		= returnType	;
		this.functionBody 	= functionBody	;
		this.parameters		= parameters	;
	}


	/**
	 * @param gen
	 */
	public void preprocessStringLiterals(CodeGenerator gen) {
		functionBody.preprocessStringLiterals(gen);
	}
	
	/**
	 *
	 */
	public void printTree(int indent) {
		this.println(indent, "funcDef:" + returnType.getL() + " " + functionName.getL());

		this.println(indent, "parameters:");
		for (AstLocal l: parameters) {
			int c = l.getTypeToken().getC();
			this.println(indent + 1, TokenClass.getTokenClassString(c) + ": "  + l.getVarName().getIdentifier());
		}
		this.println(indent, "function body:");
		this.functionBody.printTree(indent + 1);
	}
	
	
	/**
	 * @return
	 */
	public String getFuncName() {
		return this.functionName.getL();
	}
	
	
	/**
	 *
	 */
	@Override
	public void cgen(CodeGenerator gen) throws IOException, CompileErrorException
	{
		gen.setCompilingFunction(true);
		gen.resetLocalVariableTable();
		this.preprocessLocalVariables(gen);
		gen.assignLocalVariableIndices();
				
		String fname = this.getFuncName();
		String entryLabel = gen.getFunctionEntryLabel(fname);
		
		gen.printCode("; user defined function: " + fname);
		gen.printLabel(entryLabel);
		
		int stackFramExtensionSize = gen.getStackFrameExtensionSize();
		gen.printCode("push rbp");
		gen.printCode("mov rbp, rsp");
		gen.printCode("sub rsp, " + stackFramExtensionSize);
		
		gen.printCode();

		functionBody.cgen(gen);
		
		gen.printCode();

		gen.printCode("mov rsp, rbp");
		gen.printCode("pop rbp");
		gen.printCode("ret");
		
		gen.printCode();

		return;
	}
	
	/**
	 *
	 */
	public void preprocessGlobalVariables(CodeGenerator gen) 
	{
		functionBody.preprocessGlobalVariables(gen);
	}
	
	
	/**
	 *
	 */
	public void preprocessLocalVariables(CodeGenerator gen) throws CompileErrorException
	{
		for (AstLocal p: this.parameters) {
			gen.declareFuncParam(p);
		}
		functionBody.preprocessLocalVariables(gen);
	}
	
	/**
	 * @return
	 */
	public ExprType getReturnType() 
	{
		switch(returnType.getC()) 
		{
		case TokenClass.BOOLEAN:
			return ExprType.BOOLEAN;
			
		case TokenClass.DOUBLE:
			return ExprType.DOUBLE;
			
		case TokenClass.STRING:
			return ExprType.STRING;
			
		case TokenClass.INT:
			return ExprType.INT;
			
		case TokenClass.VOID:
			return ExprType.VOID;
			
		default:
			break;
		}
		return ExprType.ERROR;
	}
	
	
	/**
	 * @return
	 * @throws CompileErrorException
	 */
	public Vector<Token> getParamTypeTokens() throws CompileErrorException
	{
		Vector<Token> paramTypeTokens = new Vector<Token>();
		for (AstLocal p: parameters) {
			Token t = p.getTypeToken();
			paramTypeTokens.add(t);
		}
		return paramTypeTokens;
	}
	
	/**
	 *
	 */
	public ExprType checkTypes(CodeGenerator gen) throws CompileErrorException
	{		
		ExprType rt = this.getReturnType();
		
		gen.resetLocalVariableTable();
		this.preprocessLocalVariables(gen);
		gen.setCurrentFunctionReturnType(rt);
		
		functionBody.checkTypes(gen);
		
		return rt;
	}
	
	/**
	 *
	 */
	public String toString()
	{
		return this.functionName.toString();
	}
	
}
