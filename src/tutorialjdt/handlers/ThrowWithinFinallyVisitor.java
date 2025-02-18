package tutorialjdt.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class ThrowWithinFinallyVisitor extends ASTVisitor {
    
    @Override
    public boolean visit(TryStatement node) {
        // Get the finally block if it exists
        Block finallyBlock = node.getFinally();
        
        if (finallyBlock != null) {
            // Create a nested visitor to check for try statements in finally block
            TryStatementFinder finder = new TryStatementFinder();
            finallyBlock.accept(finder);
            
            if (finder.hasNestedTry()) {
                System.out.println("[ANTIPATTERN WARNING] 'Throw within Finally' anti-pattern detected at position: " 
                    + node.getStartPosition());
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
