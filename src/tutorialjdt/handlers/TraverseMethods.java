package tutorialjdt.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class TraverseMethods extends ASTVisitor{
	
    private boolean isFindMethodInvocation = false; 
    private boolean isFindControlBlock = false;
    
    private int numMethods = 0;

    // Build pattern to define constructor with multiple parameters
    public TraverseMethods findMethodInvocation(boolean isFindMethodInvocation) {
    	this.isFindMethodInvocation = isFindMethodInvocation;
    	return this;
    }
    
    public TraverseMethods findControlBlock(boolean isFindControlBlock) {
    	this.isFindControlBlock = isFindControlBlock;
    	return this;
    }
    
    public int getNumberOfMethods() {
    	return this.numMethods;
    }
    
    
	@Override
	public boolean visit(MethodDeclaration node) {
		// TODO Auto-generated method stub
		if (node.getBody() == null){
			return true;
		}
		
		this.numMethods += 1;
		
		// Current Method Declaration Node signature
		String method_declaration_signature = node.getName().toString();

	    // Build a list of parameter types
	    List<String> parameterTypes = new ArrayList<>();
	    for (Object param : node.parameters()) {
	        SingleVariableDeclaration variable = (SingleVariableDeclaration) param;
	        parameterTypes.add(variable.getType().toString());
	    }

	    // Format the method name and parameter types into a single line
//	    String methodSignature = method_declaration_signature + "(" + String.join(", ", parameterTypes) + ")";
//	    System.out.println("      Method Signature: " + methodSignature);
//	    
//	    System.out.println("        Method Name: " + method_declaration_signature);
//		
//		for (Object param: node.parameters()) {
//			method_declaration_signature += ((SingleVariableDeclaration) param).getType().toString();
//			System.out.println("        Input param types: " + ((SingleVariableDeclaration) param).getType().toString());
//		}
		
	    // Traverse the method body to find method invocations
//		if (isFindMethodInvocation) {			
//			node.getBody().accept(new MethodInvocationVisitor());
//		}
		
//		if (isFindControlBlock) {
//			node.getBody().accept(new ControlBlocksVisitor());
//		}

	    return super.visit(node);
	}
}
