package kut.compiler.parser.ast;

import java.io.IOException;
import java.util.Vector;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.lexer.Token;
import kut.compiler.symboltable.ExprType;

public class AstPrint extends AstNode 
{
	/**
	 * 
	 */
	protected Token t;
	
	protected Vector<AstNode>	exprList;
	protected Vector<ExprType>	etypeList;
	
	/**
	 * @param t
	 */
	public AstPrint(Token t)
	{
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
		this.println(indent, "print : ");
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
		for (int i = 0; i < exprList.size(); i++) {
			AstNode 	expr 	= exprList	.elementAt(i);
			ExprType	etype	= etypeList	.elementAt(i);
			
			expr.cgen(gen);
			if (etype == ExprType.INT) {
				gen.printCode("call " + gen.getPrintIntLabel());			
			}
			else if (etype == ExprType.STRING) {
				gen.printCode("call " + gen.getPrintStringLabel());			

			}
			else if (etype == ExprType.DOUBLE) {
				gen.printCode("call " + gen.getPrintDoubleLabel());			

			}
			else if (etype == ExprType.BOOLEAN) {
				gen.printCode("call " + gen.getPrintBooleanLabel());			

			}
			else {
				throw new CompileErrorException("the code shouldn't reach here. a bug.");
			}
		}
		
		gen.printCode("lea rax, [rel " + gen.getPrintLFLabel() + "]");
		gen.printCode("call " + gen.getPrintStringLabel());
		
		return; 
	}
	

	/**
	 *
	 */
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
		return ExprType.VOID;
	}
}
