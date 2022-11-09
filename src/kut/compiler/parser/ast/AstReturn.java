package kut.compiler.parser.ast;

import java.io.IOException;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.lexer.Token;
import kut.compiler.symboltable.ExprType;

public class AstReturn extends AstNode 
{
	/**
	 * 
	 */
	protected Token t;
	
	protected AstNode	expr;
	
	protected ExprType 	funcReturnType	;
	protected ExprType 	exprType		;
	protected boolean	castReturnValueFromIntegerToDouble;
		
	/**
	 * @param t
	 */
	public AstReturn(AstNode expr, Token t)
	{
		this.expr 	= expr		;
		this.t 		= t			;
	}
	
	/**
	 * @param gen
	 */
	public void preprocessStringLiterals(CodeGenerator gen) {
		if (expr != null) {
			expr.preprocessStringLiterals(gen);
		}
	}
	
	/**
	 *
	 */
	public void printTree(int indent) {
		this.println(indent, "return:" + t);
		if (expr == null) {
			this.println(indent + 1, "no expr");
		}
		else {
			expr.printTree(indent + 1);
		}
	}


	/**
	 *
	 */
	@Override
	public void cgen(CodeGenerator gen) throws IOException, CompileErrorException
	{			
		if (this.expr != null) {
			this.expr.cgen(gen);
			if (this.castReturnValueFromIntegerToDouble) {
				gen.printCode("cvtsi2sd xmm0, rax");
				gen.printCode("movq rax, xmm0");
			}
		}
	
		if (gen.isCompilingFunction()) {
			gen.printCode("mov rsp, rbp");
			gen.printCode("pop rbp");
			gen.printCode("ret");
		}
		else {
			if (this.expr == null) {
				gen.printCode("mov rax, 0");
			}
			gen.printCode("jmp " + gen.getExitSysCallLabel());
		}
	}
	

	/**
	 *
	 */
	public void preprocessLocalVariables(CodeGenerator gen) throws CompileErrorException
	{
		if (expr != null) {
			expr.preprocessLocalVariables(gen);
		}
	}

	/**
	 *
	 */
	public ExprType checkTypes(CodeGenerator gen) throws CompileErrorException
	{
		funcReturnType = gen.getCurrentFunctionReturnType() ;
		
		//for a void function.
		if (this.expr == null) {
			if (funcReturnType != ExprType.VOID && gen.isCompilingFunction()) {
				throw new CompileErrorException("this function must return a value. : " + t);
			}
			return ExprType.VOID;
		}
		else if (funcReturnType == ExprType.VOID) {
			if (this.expr != null) {
				throw new CompileErrorException("a void function must not return a value " + t);	
			}
		}
		
		//other type.
		castReturnValueFromIntegerToDouble = false;

		exprType = this.expr.checkTypes(gen);
		if (funcReturnType != exprType) {
			if (funcReturnType == ExprType.DOUBLE && exprType == ExprType.INT) {
				castReturnValueFromIntegerToDouble = true;
			}
			else {
				throw new CompileErrorException("the return value doesn't match with the function definition. :" + t);
			}
			
		}
		
		return funcReturnType;
	}
}
