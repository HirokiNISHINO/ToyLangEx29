package kut.compiler.parser.ast;

import java.io.IOException;
import java.util.Vector;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.exception.SyntaxErrorException;
import kut.compiler.symboltable.ExprType;

public class AstFunDefs extends AstNode 
{
	
	protected Vector<AstFunDef>	functions;
	
	/**
	 * @param node
	 * @param platform
	 */
	public AstFunDefs()
	{
		this.functions	= new Vector<AstFunDef>();
	}


	/**
	 * @param gen
	 */
	public void preprocessStringLiterals(CodeGenerator gen) {
		for (AstFunDef f: functions) {
			f.preprocessStringLiterals(gen);
		}
	}
	
	/**
	 *
	 */
	public void printTree(int indent) {
		this.println(indent, "functions");
		for (AstFunDef f: functions) {
			f.printTree(indent + 1);
		}
	}
	
	
	/**
	 * @param func
	 */
	public void addFunction(AstFunDef func)
	{
		this.functions.add(func);
	}
	
	/**
	 *
	 */
	public ExprType checkTypes(CodeGenerator gen) throws CompileErrorException
	{
		gen.setCompilingFunction(true);

		for (AstFunDef f: functions) {
			f.checkTypes(gen);
		}
		return ExprType.INT;
	}
	/**
	 *
	 */
	@Override
	public void cgen(CodeGenerator gen) throws IOException, CompileErrorException
	{
		for (AstFunDef f: functions) {
			gen.setCurrentFunctionReturnType(f.getReturnType());
			f.cgen(gen);
		}
		return;
	}
	
	/**
	 *
	 */
	public void preprocessGlobalVariables(CodeGenerator gen) 
	{
		for (AstFunDef f: functions) {
			f.preprocessStringLiterals(gen);
		}
	}
	
	/**
	 * @param gen
	 * @throws SyntaxErrorException
	 */
	public void preprocessFunctionDefinition(CodeGenerator gen) throws SyntaxErrorException
	{
		for (AstFunDef f: functions) {
			gen.declareFunction(f);
		}
	}
	
}
