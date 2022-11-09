package kut.compiler.parser.ast;

import java.io.IOException;
import java.util.Vector;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.lexer.Token;
import kut.compiler.symboltable.ExprType;

public class AstFunCall extends AstNode 
{
	/**
	 * 
	 */
	protected Token t;
	
	protected AstIdentifier		funcName	;
	protected Vector<AstNode>	exprList	;
	protected Vector<ExprType>	etypeList	;
	protected Vector<Boolean>	castIntegerToDouble;
	
	/**
	 * @param t
	 */
	public AstFunCall(AstIdentifier funcName, Token t)
	{
		this.funcName = funcName;
		this.t = t;
		this.exprList = new Vector<AstNode>();
	}
	
	
	/**
	 * @param gen
	 */
	public void preprocessStringLiterals(CodeGenerator gen) {
		for (AstNode expr: exprList) {
			expr.preprocessStringLiterals(gen);
		}
	}

	/**
	 *
	 */
	@Override
	protected void printTree(int indent) {
		this.println(indent, "funcCall : " + this.funcName.getIdentifier());
		for (AstNode expr: exprList) {
			expr.printTree(indent + 1);
		}
	}

	/**
	 * @param expr
	 */
	public void addExpr(AstNode expr)
	{
		this.exprList.add(expr);
	}

	
	/**
	 *
	 */
	@Override
	public void cgen(CodeGenerator gen) throws IOException, CompileErrorException
	{	
		
		String id = this.funcName.getIdentifier();
		for (int i = this.exprList.size() - 1; i >=0; i--) {
			AstNode a = this.exprList.get(i);
			a.cgen(gen);
			if (this.castIntegerToDouble.elementAt(i) == true) {
				//cast integer to double.
				gen.printCode("cvtsi2sd xmm0, rax");
				gen.printCode("movq rax, xmm0");
			}
			gen.printCode("push rax");
		}
		gen.printCode("call " + gen.getFunctionEntryLabel(id));
		if (this.exprList.size()  > 0) {
			gen.printCode("add rsp, " + this.exprList.size() * 8);
		}
	}
	
	

	/**
	 *
	 */
	public void preprocessGlobalVariables(CodeGenerator gen)
	{
		return;
	}
	
	/**
	 *
	 */
	public ExprType checkTypes(CodeGenerator gen) throws CompileErrorException
	{
		etypeList = new Vector<ExprType>();
		for (AstNode expr: exprList) {
			ExprType etype = expr.checkTypes(gen);
			etypeList.add(etype);
		}
		
		
		String fname = this.funcName.getIdentifier();
		Vector<ExprType> paramTypes = gen.getFuncParamTypes(fname);

		if (paramTypes.size() != etypeList.size()) {
			throw new CompileErrorException("the function: " + fname + " requires " + paramTypes.size() + 
											" argument(s), but the function call gives " + etypeList.size() + " argument(s)." + this.t);
		}
		
		castIntegerToDouble = new Vector<Boolean>();
		for (int i = 0; i < paramTypes.size(); i++) {
			ExprType a = etypeList.elementAt(i);
			ExprType b = paramTypes.elementAt(i);
			if (a == b) {
				castIntegerToDouble.add(false);
				continue;
			}
			if (a == ExprType.INT && b == ExprType.DOUBLE) {
				castIntegerToDouble.add(true);
				continue;
			}
			throw new CompileErrorException("the type mismatch found in a function call (check the argument type). " + this.t);
		}
		
		return gen.getFunctionReturnType(fname);
	}
}
