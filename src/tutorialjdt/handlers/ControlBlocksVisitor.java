package tutorialjdt.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ControlBlocksVisitor extends ASTVisitor {

    // ANSI escape code for colored text
	private static final String GREEN_TEXT = "\u001B[32m";
	private static final String RED_TEXT = "\u001B[31m";
	private static final String BLUE_TEXT = "\u001B[34m";
	private static final String RESET_TEXT = "\u001B[0m";

    // Loop
    @Override
    public boolean visit(ForStatement node) {
        if (node.getBody() != null) {
            System.out.println("          For Loop Body: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
            node.getBody().accept(this); // Visit body to analyze nested nodes
        }
        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        if (node.getBody() != null) {
            System.out.println("          Enhanced For Loop Body: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
            node.getBody().accept(this); // Visit body to analyze nested nodes
        }
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        if (node.getBody() != null) {
            System.out.println("          While Loop Body: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
            node.getBody().accept(this); // Visit body to analyze nested nodes
        }
        return false;
    }

    @Override
    public boolean visit(DoStatement node) {
        if (node.getBody() != null) {
            System.out.println("          Do-While Loop Body: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
            node.getBody().accept(this); // Visit body to analyze nested nodes
        }
        return false;
    }

    // Decision Statements
    @Override
    public boolean visit(IfStatement node) {
        System.out.println("          If Statement Condition: " + GREEN_TEXT + node.getExpression() + RESET_TEXT);

        if (node.getThenStatement() != null) {
            System.out.println("          Then Block: " + GREEN_TEXT + node.getThenStatement() + RESET_TEXT);
            node.getThenStatement().accept(this); // Visit the "then" block
        }

        if (node.getElseStatement() != null) {
            System.out.println("          Else Block: " + GREEN_TEXT + node.getElseStatement() + RESET_TEXT);
            node.getElseStatement().accept(this); // Visit the "else" block
        }

        return false;
    }

    @Override
    public boolean visit(SwitchCase node) {
        System.out.println("          Switch Case: " + GREEN_TEXT + node + RESET_TEXT);
        return false;
    }

    // Handling Try-Catch-Finally
    @Override
    public boolean visit(TryStatement node) {
        if (node.getBody() != null) {
            System.out.println("          Try Block: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
            node.getBody().accept(this); // Visit the try block
        }

        for (Object catchClause : node.catchClauses()) {
            ((CatchClause) catchClause).accept(this); // Visit each catch clause
        }

        if (node.getFinally() != null) {
            System.out.println("          Finally Block: " + GREEN_TEXT + node.getFinally() + RESET_TEXT);
            node.getFinally().accept(this); // Visit the finally block
        }

        return false;
    }

    @Override
    public boolean visit(CatchClause node) {
        System.out.println("          Catch Clause: " + GREEN_TEXT + node.getBody() + RESET_TEXT);
        if (node.getBody() != null) {
        	String exceptionType = node.getException().getType().toString();
        	if ("Exception".equals(exceptionType)) {
        		System.out.println("            Catch Generic Exception Type: " + RED_TEXT + exceptionType + RESET_TEXT);
        	}else {
        		System.out.println("            Catch Exception Type: " + BLUE_TEXT + exceptionType + RESET_TEXT);
			}
            node.getBody().accept(this); // Visit the catch body
        }
        return false;
    }
}