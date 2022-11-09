package kut.compiler.parser.ast;

import java.io.IOException;

import kut.compiler.compiler.CodeGenerator;
import kut.compiler.exception.CompileErrorException;
import kut.compiler.exception.SyntaxErrorException;
import kut.compiler.symboltable.ExprType;

public class AstProgram extends AstNode 
{

	
	/**
	 * child node
	 */
	protected AstStatements 		topLevelCode;
	protected AstFunDefs			functionDefinitions;
	
	/**
	 * @param node
	 * @param platform
	 */
	public AstProgram(AstStatements topLevelCode, AstFunDefs functionDefinitions)
	{
		this.topLevelCode 			= topLevelCode;
		this.functionDefinitions 	= functionDefinitions;
	}

	
	/**
	 * @param gen
	 */
	public void preprocessStringLiterals(CodeGenerator gen) 
	{
		this.functionDefinitions.preprocessStringLiterals(gen);
		this.topLevelCode		.preprocessStringLiterals(gen);
	}
	
	/**
	 * @param gen
	 * @throws SyntaxErrorException 
	 */
	public void preprocessFunctionDefinition(CodeGenerator gen) throws SyntaxErrorException
	{
		this.functionDefinitions.preprocessFunctionDefinition(gen);
	}
	/**
	 *
	 */
	public void printTree(int indent) {
		this.println(indent, "program:");
		functionDefinitions	.printTree(indent + 1);
		topLevelCode		.printTree(indent + 1);
	}
	
	

	/**
	 *
	 */
	@Override
	public ExprType checkTypes(CodeGenerator gen) throws CompileErrorException 
	{
		//user-defined functions.
		functionDefinitions.checkTypes(gen);
		
		gen.setCompilingFunction(false);
		gen.setCurrentFunctionReturnType(ExprType.INT);
		
		gen.resetLocalVariableTable();
		this.topLevelCode.preprocessLocalVariables(gen);
		this.topLevelCode.checkTypes(gen);
		
		return ExprType.INT;
	}


	/**
	 *
	 */
	@Override
	public void cgen(CodeGenerator gen) throws IOException, CompileErrorException
	{
		//user-defined functions.
		functionDefinitions.cgen(gen);
		
		
		//process top-level function.
		gen.setCompilingFunction(false);
		gen.setCurrentFunctionReturnType(ExprType.INT);
		
		gen.resetLocalVariableTable();
		this.topLevelCode.preprocessLocalVariables(gen);
		gen.assignLocalVariableIndices();
		
		int sfeSize = gen.getStackFrameExtensionSize();
	
		gen.printCode();

		//main function
		gen.printLabel	(gen.getEntryPointLabelName());
		gen.printCode(	"mov rax, 0 ; initialize the accumulator register.");

		// extend the stack frame.
		gen.printComment("; the top-level function prologue.");
		gen.printCode("push rbp");
		gen.printCode("mov rbp, rsp");
		gen.printCode("sub rsp, " + sfeSize);

		//body of the code
		gen.printCode();
		gen.printComment("; the top-level function body.");
		
		
		this.topLevelCode.cgen(gen);
				
		// rewind the stack frame.
		gen.printCode();
		gen.printComment("; the top-level function epilogue.");
		gen.printCode("mov rsp, rbp");
		gen.printCode("pop rbp");
		
		//epilogue
		gen.printCode();
		gen.printCode(	"jmp " + gen.getExitSysCallLabel() + " ; exit the program, rax should hold the exit code.");


		return;
	}
	
	
	/**
	 *
	 */
	public void preprocessGlobalVariables(CodeGenerator gen) 
	{
		this.topLevelCode		.preprocessGlobalVariables(gen);		
		this.functionDefinitions.preprocessGlobalVariables(gen);
	}
	
}
