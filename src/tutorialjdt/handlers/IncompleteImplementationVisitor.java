package tutorialjdt.handlers;

import java.awt.print.Printable;
import java.security.PublicKey;
import java.util.concurrent.BlockingDeque;

import org.eclipse.jdt.core.dom.Comment;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.IDocumentElementRequestor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class IncompleteImplementationVisitor extends ASTVisitor{
	private int incompleteImplementationCount = 0;
	private ICompilationUnit unit;
	private CompilationUnit astRoot;
	
	public IncompleteImplementationVisitor(ICompilationUnit unit, CompilationUnit astRoot) {
		this.unit = unit;
		this.astRoot = astRoot;
	}
	
	public int getIncompleteImplementationCount() {
		return this.incompleteImplementationCount;
	}
	
	private String getLocation(int startPosition) {
		try {
			String fileNameString = unit.getResource().getLocation().toOSString();
			IDocument document = new Document(unit.getSource());
			int lineNumber = 0;
			try {
				lineNumber = document.getLineOfOffset(startPosition) + 1;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "File: " + fileNameString + ", Line: " + lineNumber;
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "File: " + unit.getElementName() + ", Line: Unknown";
		}
	}
	
	@Override
	public boolean visit(CatchClause node) {
	    Block catchBody = node.getBody();
	    if (catchBody != null) {
	        // Get the start and end positions of the catch block
	        int start = catchBody.getStartPosition();
	        int end = start + catchBody.getLength();
	        
	        // Check if there are any TODO comments in the catch block
	        boolean hasTodoComment = false;
	        for (Object commentObj : astRoot.getCommentList()) {
	            Comment comment = (Comment) commentObj;
	            int commentStart = comment.getStartPosition();
	            // Check if comment is within the catch block
	            if (commentStart >= start && commentStart <= end) {

	            	String sourceComment = null;
					try {
						sourceComment = unit.getSource().substring(
						    comment.getStartPosition(),
						    comment.getStartPosition() + comment.getLength()
						).toLowerCase();
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                if (sourceComment.contains("todo") || sourceComment.contains("fixme")) {
	                    hasTodoComment = true;
	                    break;
	                }
	            }
	        }
	        
	        // Detect incomplete implementation if the catch block has only a TODO comment (no statements)
	        if (hasTodoComment && catchBody.statements().isEmpty()) {
	            this.incompleteImplementationCount++;
	            System.out.println("[ANTIPATTERN WARNING] 'Incomplete Implementation' anti-pattern detected: " + getLocation(node.getStartPosition()));
//                getLocation(node.getStartPosition()) + "\n" + node.toString());
	            System.out.println("----");
	        }
	    }
	    return super.visit(node);
	}
}
