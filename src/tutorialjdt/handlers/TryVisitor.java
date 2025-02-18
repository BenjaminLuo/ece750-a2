package tutorialjdt.handlers;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class TryVisitor extends ASTVisitor {
    
    private int throwWithinFinallyCounter = 0;
    private ICompilationUnit unit;
    
    public int getThrowWithinFinallyCount() {
    	return this.throwWithinFinallyCounter;
    }
    
    public TryVisitor(ICompilationUnit unit) {
    	this.unit = unit;
    }
	
    private String getLocation(int startPosition) {
        try {
            IDocument document = new Document(unit.getSource());
            int lineNumber = document.getLineOfOffset(startPosition) + 1;
            return "File: " + unit.getElementName() + ", Line: " + lineNumber;
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
}
