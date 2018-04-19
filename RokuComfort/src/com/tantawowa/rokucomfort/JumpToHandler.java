package com.tantawowa.rokucomfort;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.core.resources.*;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class JumpToHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public JumpToHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from
	 * the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();
		IEditorInput input = workbenchPart.getSite().getPage().getActiveEditor().getEditorInput();
		IPath path = ((FileEditorInput) input).getPath();
		ILog log = Activator.getDefault().getLog();

		ITextEditor editor = getTextEditor(window.getActivePage().getActiveEditor());
		IDocument document = getEditorDocument(window);

		boolean isXml = path.getFileExtension().toLowerCase().equals("xml");
		String otherExtension = isXml ? ".brs" : ".xml";

		// 1. find symbol in other doc

		String word = getCurrentWord(editor, document);
		if (word.startsWith("<")) {
			openComponent(window, word);
			return null;
		}
		IPath otherPath = new Path(path.removeFileExtension().toString() + otherExtension.toString());
		boolean isFound = isXml ? goToMatchingSymoblInOtherDocument(otherPath, word, !isXml) : false;
		if (!isFound) {
			isFound = goToMatchingSymoblInDocument(document, editor, word, isXml);
			if (!isXml && !isFound) {
				goToMatchingSymoblInOtherDocument(otherPath, word, !isXml);
			}
		}

		return null;
	}

	private void openComponent(IWorkbenchWindow window, String name) {
		ILog log = Activator.getDefault().getLog();

		// TODO Auto-generated method stub
		IProject project = getActiveProject();
		List<IFile> files = new ArrayList<IFile>();
		getFilesMatchingName((IContainer) project, files, name.substring(1), "xml");
		log.log(new Status(0, "NONE", "found files " + files.toString()));
		if (files.size() >0) {
			IFile file = files.get(0);

			IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActivePart();
			IWorkbenchPage page = workbenchPart.getSite().getPage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
					.getDefaultEditor(file.getName());
			log.log(new Status(0, "NONE", "OPENING COMPONENT " + file.getFullPath().toString()));

			try {
				page.openEditor(new FileEditorInput(file), desc.getId());

			} catch (PartInitException e) {
				log.log(new Status(0, "NONE", "ERROR " + file.getFullPath().toString()));
			}
		}

	}

	private void getAllFiles(IContainer container, List<IFile> list) {
		IResource[] members;
		try {
			members = container.members();

			for (IResource member : members) {
				if (member instanceof IContainer) {
					getAllFiles((IContainer) member, list);
				} else if (member instanceof IFile) {
					IFile file = (IFile) member;
					if (file.getFileExtension().equals("xml")) {
						list.add((IFile) member);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getFilesMatchingName(IContainer container, List<IFile> list, String name, String extension) {
		IResource[] members;
		ILog log = Activator.getDefault().getLog();

		try {
			members = container.members();

			for (IResource member : members) {
				if (member instanceof IContainer) {
					getFilesMatchingName((IContainer) member, list, name, extension);
				} else if (member instanceof IFile) {
					IFile file = (IFile) member;
					if (file.getName().equalsIgnoreCase(name + "." + extension)) {
						log.log(new Status(0, "NONE", "MATCHED " + file.getName() + " " + name));
						
						list.add((IFile) member);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IProject getActiveProject() {
		IProject project = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();

		IEditorPart activeEditor = activePage.getActiveEditor();

		if (activeEditor != null) {
			IEditorInput input = activeEditor.getEditorInput();

			project = input.getAdapter(IProject.class);
			if (project == null) {
				IResource resource = input.getAdapter(IResource.class);
				if (resource != null) {
					project = resource.getProject();
				}
			}
		}
		return project;
	}

	private boolean goToMatchingSymoblInDocument(IDocument document, ITextEditor editor, String word, boolean isXML) {
		// TODO Auto-generated method stub
		String[] searchItems = getSearchTermsForDocument(isXML, word);
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
		for (String currentSearch : searchItems) {
			try {
				IRegion offset = adapter.find(0, currentSearch, true, false, true, false);
				if (offset != null) {
					editor.selectAndReveal(offset.getOffset(), 0);
					return true;
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}

	private boolean goToMatchingSymoblInOtherDocument(IPath path, String word, boolean isXML) {
		IFile file = SwitchFileHandler.GetFilewithExtension(path);
		if (file == null) {
			return false;
		}
		IDocumentProvider provider = new TextFileDocumentProvider();
		try {
			provider.connect(file);
		} catch (CoreException e1) {
			e1.printStackTrace();
			return false;
		}
		IDocument document = provider.getDocument(file);
		if (document == null) {
			return false;
		}

		// TODO Auto-generated method stub
		String[] searchItems = getSearchTermsForDocument(isXML, word);
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
		for (String currentSearch : searchItems) {
			try {
				IRegion offset = adapter.find(0, currentSearch, true, false, true, false);
				if (offset != null) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();

					IEditorPart editorPart = IDE.openEditor(page, file);
					ITextEditor editor = getTextEditor(editorPart);
					editor.selectAndReveal(offset.getOffset(), 0);

					return true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		provider.disconnect(file);
		return false;
	}

	private String getCurrentWord(ITextEditor editor, IDocument document) {
		try {
			int pos = GetCursorPosition(editor);
			// 1. get 20 chars before and after
			int minIndex = Math.max(0, pos - 20);
			int maxIndex = Math.min(document.getLength(), pos + 20);
			String region = document.get(minIndex, maxIndex);
			pos -= minIndex;

			int endIndex;
			int startIndex;

			for (startIndex = pos; startIndex > 0; startIndex--) {
				char c = region.charAt(startIndex);
				if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_') {
					break;
				}
			}

			for (endIndex = pos; endIndex < region.length(); endIndex++) {
				char c = region.charAt(endIndex);
				if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_' && c != '$') {
					break;
				}
			}

			String firstChar = region.substring(startIndex, startIndex + 1);
			String line = region.substring(startIndex + (firstChar.equals("<") ? 0 : 1), endIndex);
			return line;
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}

	private String getCurrentLineContents(ITextEditor editor, IDocument document) {

		int pos = GetCursorPosition(editor);
		if (editor != null && editor instanceof ITextEditor && pos >= 1) {
			IRegion line;
			try {
				line = document.getLineInformation((pos - 1));
				return document.get(line.getOffset(), line.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}

		}

		return "";
	}

	public int GetCursorPosition(ITextEditor editor) {

		ISelection s = editor.getSelectionProvider().getSelection();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		int iCursorPosition = selection.getOffset();
		return iCursorPosition;
	}

	public static IDocument getEditorDocument(IWorkbenchWindow window) {
		IEditorPart ed = getTextEditor(window.getActivePage().getActiveEditor());

		IDocument doc = null;
		if (ed instanceof AbstractDecoratedTextEditor)
			doc = ((AbstractDecoratedTextEditor) ed).getDocumentProvider().getDocument(ed.getEditorInput());

		return doc;
	}

	public static ITextEditor getTextEditor(IEditorPart part) {

		if (part instanceof MultiPageEditorPart) {
			MultiPageEditorPart mp = (MultiPageEditorPart) part;
			IEditorPart[] parts = mp.findEditors(part.getEditorInput());
			if (parts.length > 0) {
				return (ITextEditor) parts[0];
			}
		}
		if (!(part instanceof AbstractTextEditor)) {
			return null;
		}
		return (ITextEditor) part;
	}

	private String[] getSearchTermsForDocument(boolean isXMl, String word) {
		return isXMl ? new String[] { "id=\"" + word, "onChange=\"" + word, "name=\"" + word, "\"" + word + "\"",

		} : new String[] { "Function " + word, "Sub " + word, "id=\"" + word, "name=\"" + word, };

	}
}
