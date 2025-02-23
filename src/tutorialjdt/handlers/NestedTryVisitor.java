package tutorialjdt.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

class NestedTryVisitor extends ASTVisitor {
	
	private int tryDepth = 0;
    private int nestedTryCount = 0;
    
    private ICompilationUnit unit;
    
    public int getNestedTryCount() {
        return this.nestedTryCount;
    }
    
    public NestedTryVisitor(ICompilationUnit unit) {
        this.unit = unit;
    }
    
    @Override
    public boolean visit(TryStatement node) {
        if (tryDepth >= 1) {
            nestedTryCount++;
            System.out.println("[ANTIPATTERN WARNING] 'Nested Try' detected: " 
                               + getLocation(node.getStartPosition()));
            System.out.println("----");
        }
        
        tryDepth++;
        
        return super.visit(node);
    }
    
    @Override
    public void endVisit(TryStatement node) {
        tryDepth--;
        super.endVisit(node);
    }
    
    private String getLocation(int startPosition) {
    	// Output the file, line number, and method declaration text
        try {
        	String fileNameString = unit.getResource().getLocation().toOSString();
            IDocument document = new Document(unit.getSource());
            int lineNumber = document.getLineOfOffset(startPosition) + 1;
            IRegion lineInfo = document.getLineInformation(lineNumber - 1);
            String lineText = document.get(lineInfo.getOffset(), lineInfo.getLength());
            // return "File: " + unit.getElementName() + ", Line: " + lineNumber + "\n" + lineText;
            
            // Check if the line starts with a decorator
            if (lineText.trim().startsWith("@")) {
                IRegion nextLineInfo = document.getLineInformation(lineNumber);
                String nextLineText = nextLineInfo != null ? document.get(nextLineInfo.getOffset(), nextLineInfo.getLength()) : "";
                return "\nFile: " + fileNameString + ", Line: " + lineNumber + "\n" + lineText + "\n" + nextLineText;
            } else {
                return "\nFile: " + fileNameString + ", Line: " + lineNumber + "\n" + lineText;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "File: " + unit.getElementName() + ", Line: Unknown";
        }
    }
    
    
	
}
