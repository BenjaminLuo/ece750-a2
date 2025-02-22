package tutorialjdt.handlers;

import java.awt.print.Printable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class TryVisitor extends ASTVisitor {
    
    private int throwWithinFinallyCounter = 0;
    private int logAndThrowCounter = 0;
    private ICompilationUnit unit;
    
    public int getThrowWithinFinallyCount() {
    	return this.throwWithinFinallyCounter;
    }
    
    public int getLogAndThrowCount() {
    	return this.logAndThrowCounter;
    }
    
    public TryVisitor(ICompilationUnit unit) {
    	this.unit = unit;
    }
	
    private String getLocation(int startPosition) {
        try {
        	String fileNameString = unit.getResource().getLocation().toOSString();
            IDocument document = new Document(unit.getSource());
            int lineNumber = document.getLineOfOffset(startPosition) + 1;
            return "File: " + fileNameString + ", Line: " + lineNumber;
        } catch (Exception e) {
            e.printStackTrace();
            return "File: " + unit.getElementName() + ", Line: Unknown";
        }
    }
	
    @Override
    public boolean visit(TryStatement node) {
        // Get the finally block if it exists
        Block finallyBlock = node.getFinally();
        
        if (finallyBlock != null) {
            // Create a nested visitor to check for try statements in finally block
            TryStatementFinder finder = new TryStatementFinder();
            finallyBlock.accept(finder);
            
            if (finder.hasNestedTry()) {
            	this.throwWithinFinallyCounter += 1;
                System.out.println("[ANTIPATTERN WARNING] 'Throw within Finally' anti-pattern detected: " + getLocation(node.getStartPosition()) + "\n" + node.toString()); 
                System.out.println("----");
            }
        }
        
        if (node.getBody() != null) {
            node.getBody().accept(this); // Visit the try block
        }

//        for (Object catchClause : node.catchClauses()) {
//            ((CatchClause) catchClause).accept(this); // Visit each catch clause
//        }
        
        return super.visit(node);
    }
    
    // Helper class to find nested try statements
    private class TryStatementFinder extends ASTVisitor {
        private boolean nestedThrowFound = false;
        
        @Override
        public boolean visit(ThrowStatement node) {
        	nestedThrowFound = true;
            return false; // Stop visiting once we find a try statement
        }
        
        public boolean hasNestedTry() {
            return nestedThrowFound;
        }
    }
    
    @Override
    public boolean visit(CatchClause node) {
        Block catchBody = node.getBody();
        if (catchBody != null) {
            boolean hasLogging = false;
            boolean hasThrow = false;

            for (Object statement : catchBody.statements()) {
                if (statement instanceof ExpressionStatement) {
                    ExpressionStatement exprStmt = (ExpressionStatement) statement;
                    if (exprStmt.getExpression() instanceof MethodInvocation) {
                        MethodInvocation methodInvocation = (MethodInvocation) exprStmt.getExpression();
                        if (isLoggingMethod(methodInvocation)) {
                            hasLogging = true;
                        }
                    }
                } else if (statement instanceof ThrowStatement) {
                    hasThrow = true;
                }
            }

            if (hasLogging && hasThrow) {
            	this.logAndThrowCounter += 1;
                System.out.println("[ANTIPATTERN WARNING] 'Log and Throw' anti-pattern detected: " + getLocation(node.getStartPosition()) + "\n" + node.toString());
                System.out.println("----");
            }
        }
        return super.visit(node);
    }
        
    private boolean isLoggingMethod(MethodInvocation methodInvocation) {
        String methodName = methodInvocation.getName().getIdentifier();
        return methodName.equals("log");
    }
    
}
