package tutorialjdt.handlers;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
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
        	List<?> statements = catchBody.statements();
            boolean hasLogging = false;
            boolean hasThrow = false;

            for (Object obj : statements) {
                if (obj instanceof Statement) {
                    Statement stmt = (Statement) obj;
                    if (containsLogging(stmt)) {
                        hasLogging = true;
                    }
                    if (containsThrow(stmt)) {
                        hasThrow = true;
                    }
                    
                    // Check if-else
                    if (stmt instanceof IfStatement) {
                        if (containsLogAndThrowInSameBranch((IfStatement) stmt)) {
                            this.logAndThrowCounter += 1;
                            System.out.println("[ANTIPATTERN WARNING] 'Log and Throw' anti-pattern detected: " 
                                + getLocation(node.getStartPosition()) + "\n" + node.toString());
                            System.out.println("----");
                        }
                    }
                }
            }

            if (hasLogging && hasThrow) {
                this.logAndThrowCounter += 1;
                System.out.println("[ANTIPATTERN WARNING] 'Log and Throw' anti-pattern detected: " 
                    + getLocation(node.getStartPosition()) + "\n" + node.toString());
                System.out.println("----");
            }
        }
        return super.visit(node);        
    }

    private boolean containsLogAndThrowInSameBranch(IfStatement ifStmt) {
        // check if branch
        boolean hasLoggingInThen = containsLogging(ifStmt.getThenStatement());
        boolean hasThrowInThen = containsThrow(ifStmt.getThenStatement());

        if (hasLoggingInThen && hasThrowInThen) {
            return true;
        }

        // check else branch
        Statement elseStmt = ifStmt.getElseStatement();
        if (elseStmt != null) {
            if (elseStmt instanceof IfStatement) {
                // handle else if recursively
                if (containsLogAndThrowInSameBranch((IfStatement) elseStmt)) {
                    return true;
                }
            } else {
                // handle else block
                boolean hasLoggingInElse = containsLogging(elseStmt);
                boolean hasThrowInElse = containsThrow(elseStmt);
                if (hasLoggingInElse && hasThrowInElse) {
                    return true;
                }
            }
        }

        return false;
    }
   
    private boolean containsLogging(Statement stmt) {
        if (stmt instanceof ExpressionStatement) {
            ExpressionStatement exprStmt = (ExpressionStatement) stmt;
            if (exprStmt.getExpression() instanceof MethodInvocation) {
                MethodInvocation methodInvocation = (MethodInvocation) exprStmt.getExpression();
                return isLoggingMethod(methodInvocation);
            }
        } else if (stmt instanceof Block) {
            for (Object subStmt : ((Block) stmt).statements()) {
                if (containsLogging((Statement) subStmt)) {
                    return true;
                }
            }
        } else if (stmt instanceof ForStatement) {
            return containsLogging(((ForStatement) stmt).getBody());
        } else if (stmt instanceof WhileStatement) {
            return containsLogging(((WhileStatement) stmt).getBody());
        } else if (stmt instanceof DoStatement) {
            return containsLogging(((DoStatement) stmt).getBody());
        } else if (stmt instanceof TryStatement) {
            TryStatement tryStmt = (TryStatement) stmt;
            if (containsLogging(tryStmt.getBody())) {
                return true;
            }
            for (Object c : tryStmt.catchClauses()) {
                if (containsLogging(((CatchClause) c).getBody())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsThrow(Statement stmt) {
        if (stmt instanceof ThrowStatement) {
            return true;
        } else if (stmt instanceof Block) {
            for (Object subStmt : ((Block) stmt).statements()) {
                if (containsThrow((Statement) subStmt)) {
                    return true;
                }
            }
        } else if (stmt instanceof ForStatement) {
            return containsThrow(((ForStatement) stmt).getBody());
        } else if (stmt instanceof WhileStatement) {
            return containsThrow(((WhileStatement) stmt).getBody());
        } else if (stmt instanceof DoStatement) {
            return containsThrow(((DoStatement) stmt).getBody());
        } else if (stmt instanceof TryStatement) {
            TryStatement tryStmt = (TryStatement) stmt;
            if (containsThrow(tryStmt.getBody())) {
                return true;
            }
            for (Object c : tryStmt.catchClauses()) {
                if (containsThrow(((CatchClause) c).getBody())) {
                    return true;
                }
            }
        }
        return false;
    }
        

    
 // common keywords for logging
    private boolean isLoggingMethod(MethodInvocation methodInvocation) {
        String methodName = methodInvocation.getName().getIdentifier().toLowerCase();
        return methodName.contains("log") || methodName.contains("print") || methodName.contains("warn") || methodName.contains("error") 
            || methodName.contains("trace") || methodName.contains("debug") || methodName.contains("info");
    }
    
}
