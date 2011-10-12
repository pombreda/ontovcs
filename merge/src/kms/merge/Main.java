package kms.merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import ru.tpu.cc.kms.ConflictFinder;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.ChangeSet;
import ru.tpu.cc.kms.changes.ComparableOntology;
import ru.tpu.cc.kms.changes.render.ChangeRenderer;
import ru.tpu.cc.kms.changes.render.FunctionalSyntaxChangeRenderer;
import ru.tpu.cc.kms.changes.render.ManchesterSyntaxChangeRenderer;
import ru.tpu.cc.kms.statements.Statement;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

class Settings {
	public static enum Format {
		FUNCTIONAL,
		MANCHESTER,
	};
    @Option(name = "-o", usage = "Output", metaVar = "outfile", required = false)
    public String output;
    @Option(name = "--auto", usage = "Don't display GUI if no conflicts are found", required = false)
    public Boolean auto;
    @Option(name = "--format", aliases = {"-f"}, metaVar = "format", usage = "Format of changes: Functional (default) or Manchester", required = false)
    public Format format = Format.FUNCTIONAL;
    @Argument
    public ArrayList<String> extraArgs = new ArrayList<String>();
}

public class Main {

	protected Shell shlMerge;
	private TabItem tbtmCommonChanges;
	private TabItem tbtmConflictingChanges;
	private TabItem tbtmOtherChanges;
	private TabItem tbtmResult;
	private Table table_Common;
	private Table table_Conflicts1;
	private Table table_Conflicts2;
	private Table table_Other1;
	private Table table_Other2;
	private MenuItem mntmTools;
	private Font font;
	private ChangeSet<Statement> result;
	private Table table_Result;

	private ConflictFinder conflictFinder;
	private ComparableOntology base;
	private String outputFilename;
	private boolean modified = false;
	private boolean orderInvalid = false;
	private String baseFilename;
	private String localFilename;
	private String remoteFilename;
	private ChangeRenderer changeRenderer = new FunctionalSyntaxChangeRenderer();

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			Settings settings = new Settings();
			CmdLineParser parser = new CmdLineParser(settings);
	        try {
	            parser.parseArgument(args);
	            if (settings.format == Settings.Format.MANCHESTER) {
	            	window.changeRenderer = new ManchesterSyntaxChangeRenderer();
	            }
	            String baseFilename = "";
	            String localFilename = "";
	            String remoteFilename = "";
	            if (settings.extraArgs.size() > 0) {
	            	baseFilename = settings.extraArgs.get(0);
	            }
	            if (settings.extraArgs.size() > 1) {
	            	localFilename = settings.extraArgs.get(1);
	            }
		        if (settings.extraArgs.size() > 2) {
		        	remoteFilename = settings.extraArgs.get(2);
		        }
	            if (null != settings.output) {
	            	// merge
	            	if (settings.extraArgs.size() < 3)
	            		throw new CmdLineException(parser, "Need 3 arguments to merge");
	            	System.err.println("Performing merge on " + baseFilename + " " + localFilename + " " + remoteFilename);
	            	window.load(baseFilename, localFilename, remoteFilename, settings.output);
	            	if ((settings.auto != null) && !window.conflictFinder.isConflict()) {
	    				try {
	    					window.base.applyChanges(window.result);
	    					OWLOntology o = window.base.getOntology();
	    					if (window.outputFilename.equals("STDOUT"))
	    						o.getOWLOntologyManager().saveOntology(o, System.out);
	    					else
	    						o.getOWLOntologyManager().saveOntology(o, IRI.create(new File(window.outputFilename).toURI()));
	    				} catch (OWLOntologyCreationException e1) {
	    					e1.printStackTrace();
	    					// 70 - internal software error
	    					System.exit(70);
	    				} catch (OWLOntologyStorageException e2) {
	    					e2.printStackTrace();
	    					// can't create (user) output file
	    					System.exit(73);
	    				}
	            		System.exit(0);
	            	}
	            }
	            else if (settings.extraArgs.size() == 3) {
	        		// diff3
	        		System.err.println("Performing diff3 on " + baseFilename + " " + localFilename + " " + remoteFilename);
	        		window.load(baseFilename, localFilename, remoteFilename, null);
	        	}
	            else if (settings.extraArgs.size() == 2) {
	        		// diff
	        		System.err.println("Performing diff on " + baseFilename + " " + localFilename);
	        		window.load(baseFilename, localFilename, null, null);
	        	}
	            else
	            	throw new CmdLineException(parser, "Invalid arguments");
	        } catch (CmdLineException e) {
	            System.err.println("Usage: owl2merge base local [remote [-o outfile [--auto]]]");
	        }

			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
    	if (conflictFinder != null)
    		fillTables();
		// Center the shell on the primary monitor
	    Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shlMerge.getBounds();
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    shlMerge.setLocation(x, y);

		shlMerge.open();
		shlMerge.layout();
        if (!System.getProperty("os.name").startsWith("Windows"))
        	mntmTools.setEnabled(false);
		while (!shlMerge.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlMerge = new Shell();

		shlMerge.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				if (modified) {
			        int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL;
			        MessageBox messageBox = new MessageBox(shlMerge, style);
			        messageBox.setText("Closing");
			        messageBox.setMessage("Save?");
			        int mr = messageBox.open();
			        arg0.doit = mr == SWT.NO;
			        if (mr == SWT.YES) {
			        	if (outputFilename == null) {
					        selectOutput();
					        if (outputFilename == null)
					        	return;
						}
						try {
							save();
							arg0.doit = true;
						} catch (OWLOntologyCreationException e1) {
							arg0.doit = false;
							errorBox(e1);

						} catch (OWLOntologyStorageException e2) {
							arg0.doit = false;
							errorBox(e2);
						}
			        } else {
			        	// User choose not to save
			        	if (arg0.doit) {
			        		// 73 - can't create (user) output file
			        		System.exit(73);
			        	}
			        }
				}
			}
		});
		shlMerge.setSize(800, 500);
		shlMerge.setText("owl2merge");
		shlMerge.setLayout(new FillLayout(SWT.HORIZONTAL));

		TabFolder tabFolder = new TabFolder(shlMerge, SWT.NONE);

		tbtmCommonChanges = new TabItem(tabFolder, SWT.NONE);
		tbtmCommonChanges.setText("Common changes");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmCommonChanges.setControl(composite);
		FillLayout fl_composite = new FillLayout(SWT.HORIZONTAL);
		fl_composite.marginHeight = 8;
		fl_composite.marginWidth = 8;
		fl_composite.spacing = 8;
		composite.setLayout(fl_composite);

		font = SWTResourceManager.getFont("Consolas", 8, SWT.NORMAL);

		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private void uncheckItem(Table table, Change<Statement> o) {
				for (TableItem ti: table.getItems()) {
					if (ti.getData() == o) {
						ti.setChecked(false);
						break;
					}
				}
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				Change<Statement> o = ( (Change<Statement>) e.item.getData());
				if (e.detail == SWT.CHECK) {
					if ( ( (TableItem) e.item).getChecked() ) {
						result.add( o );
						addResult( o );
						invalidateOrder();
					}
					else
					{
						result.remove( o );
						for (TableItem ti: table_Result.getItems()) {
							if (ti.getData() == o) {
								table_Result.remove(table_Result.indexOf(ti));
								break;
							}
						}
						uncheckItem(table_Common, o);
						uncheckItem(table_Conflicts1, o);
						uncheckItem(table_Conflicts2, o);
						uncheckItem(table_Other1, o);
						uncheckItem(table_Other2, o);
					}
				}
			}
		};


		table_Common = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table_Common.addSelectionListener(selectionAdapter);
		table_Common.setFont(font);
		table_Common.setLinesVisible(true);

		tbtmConflictingChanges = new TabItem(tabFolder, SWT.NONE);
		tbtmConflictingChanges.setText("Conflicting changes");

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmConflictingChanges.setControl(composite_1);
		FillLayout fl_composite_1 = new FillLayout(SWT.VERTICAL);
		fl_composite_1.marginWidth = 8;
		fl_composite_1.marginHeight = 8;
		fl_composite_1.spacing = 8;
		composite_1.setLayout(fl_composite_1);

		table_Conflicts1 = new Table(composite_1, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table_Conflicts1.setLinesVisible(true);
		table_Conflicts1.setFont(font);
		table_Conflicts1.addSelectionListener(selectionAdapter);

		table_Conflicts2 = new Table(composite_1, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table_Conflicts2.setLinesVisible(true);
		table_Conflicts2.setFont(font);
		table_Conflicts2.addSelectionListener(selectionAdapter);

		tbtmOtherChanges = new TabItem(tabFolder, SWT.NONE);
		tbtmOtherChanges.setText("Other changes");

		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmOtherChanges.setControl(composite_2);
		FillLayout fl_composite_2 = new FillLayout(SWT.VERTICAL);
		fl_composite_2.spacing = 8;
		fl_composite_2.marginWidth = 8;
		fl_composite_2.marginHeight = 8;
		composite_2.setLayout(fl_composite_2);

		table_Other1 = new Table(composite_2, SWT.BORDER | SWT.CHECK);
		table_Other1.setLinesVisible(true);
		table_Other1.setFont(font);
		table_Other1.addSelectionListener(selectionAdapter);

		table_Other2 = new Table(composite_2, SWT.BORDER | SWT.CHECK);
		table_Other2.setLinesVisible(true);
		table_Other2.setFont(font);
		table_Other2.addSelectionListener(selectionAdapter);

		tbtmResult = new TabItem(tabFolder, SWT.NONE);
		tbtmResult.setText("Result");

		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmResult.setControl(composite_3);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.verticalSpacing = 8;
		gl_composite_3.marginWidth = 8;
		gl_composite_3.marginHeight = 8;
		gl_composite_3.horizontalSpacing = 8;
		composite_3.setLayout(gl_composite_3);

		table_Result = new Table(composite_3, SWT.BORDER | SWT.CHECK);
		GridData gd_table_Result = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_Result.heightHint = 355;
		gd_table_Result.widthHint = 994;
		table_Result.setLayoutData(gd_table_Result);
		table_Result.setLinesVisible(true);
		table_Result.setFont(SWTResourceManager.getFont("Consolas", 8, SWT.NORMAL));

		table_Result.addSelectionListener(selectionAdapter);

		Menu menu = new Menu(shlMerge, SWT.BAR);
		shlMerge.setMenuBar(menu);

		MenuItem mntmfile = new MenuItem(menu, SWT.CASCADE);
		mntmfile.setText("&File");

		Menu menu_1 = new Menu(mntmfile);
		mntmfile.setMenu(menu_1);

		MenuItem mntmopenCtrlo = new MenuItem(menu_1, SWT.NONE);
		mntmopenCtrlo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
		        FileDialog fd = new FileDialog(shlMerge, SWT.OPEN);
		        // fd.setFilterPath(".");
		        String[] filterNames = {
		        		"All supported files (*.owl; *.rdf; *.n3; *.turtle; *.owl)",
		        		"OWL files (*.owl)",
		        		"RDF files (*.rdf)",
		        		"N3 files (*.n3)",
		        		"Turtle files (*.turtle)",
		        		"All files (*.*)" };
		        String[] filterExt = {
		        		"*.owl;*.rdf;*.n3;*.turtle",
		        		"*.owl",
		        		"*.rdf",
		        		"*.n3",
		        		"*.turtle",
		        		"*.*" };
		        fd.setFilterNames(filterNames);
		        fd.setFilterExtensions(filterExt);
		        fd.setText("Select base file");
		        String baseFilename = fd.open();
		        if (baseFilename == null)
		        	return;
		        fd.setText("Select local file");
		        String localFilename = fd.open();
		        if (localFilename == null)
		        	return;
		        fd.setText("Select remote file");
		        String remoteFilename = fd.open();
		        if (remoteFilename == null)
		        	return;
		        load(baseFilename, localFilename, remoteFilename, null);
		        fillTables();
			}
		});
		mntmopenCtrlo.setText("&Open...\tCtrl+O");
		mntmopenCtrlo.setAccelerator(SWT.MOD1 + 'O');

		MenuItem mntmsaveCtrls = new MenuItem(menu_1, SWT.NONE);
		mntmsaveCtrls.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (outputFilename == null) {
			        selectOutput();
			        if (outputFilename == null)
			        	return;
				}
				try {
					save();
				} catch (OWLOntologyCreationException e1) {
					errorBox(e1);

				} catch (OWLOntologyStorageException e2) {
					errorBox(e2);
				}
			}
		});
		mntmsaveCtrls.setText("&Save...\tCtrl+S");
		mntmsaveCtrls.setAccelerator(SWT.MOD1 + 'S');

		MenuItem menuItem_5 = new MenuItem(menu_1, SWT.SEPARATOR);
		menuItem_5.setText("-");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		mntmExit.setText("E&xit");

		MenuItem mntmView = new MenuItem(menu, SWT.CASCADE);
		mntmView.setText("View");

		Menu menu_3 = new Menu(mntmView);
		mntmView.setMenu(menu_3);

		MenuItem mntmFormat = new MenuItem(menu_3, SWT.CASCADE);
		mntmFormat.setText("Format");

		Menu menu_4 = new Menu(mntmFormat);
		mntmFormat.setMenu(menu_4);

		MenuItem mntmFunctionalSyntax = new MenuItem(menu_4, SWT.RADIO);
		mntmFunctionalSyntax.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				changeRenderer = new FunctionalSyntaxChangeRenderer();
				fillTables();
			}
		});
		mntmFunctionalSyntax.setSelection(true);
		mntmFunctionalSyntax.setText("Functional Syntax");

		MenuItem mntmManchesterSyntax = new MenuItem(menu_4, SWT.RADIO);
		mntmManchesterSyntax.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				changeRenderer = new ManchesterSyntaxChangeRenderer(true);
				fillTables();
			}
		});
		mntmManchesterSyntax.setText("Manchester Syntax");
		if (changeRenderer.getClass().equals(ManchesterSyntaxChangeRenderer.class)) {
			mntmManchesterSyntax.setSelection(true);
			mntmFunctionalSyntax.setSelection(false);
		}

		MenuItem mntmSortResult = new MenuItem(menu_3, SWT.NONE);
		mntmSortResult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (orderInvalid) {
					updateResult();
				}
			}
		});
		mntmSortResult.setText("Sort Result");

		mntmTools = new MenuItem(menu, SWT.CASCADE);
		mntmTools.setText("Tools");

		Menu menu_2 = new Menu(mntmTools);
		mntmTools.setMenu(menu_2);

		MenuItem mntmOpenBaseWith = new MenuItem(menu_2, SWT.NONE);
		mntmOpenBaseWith.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				runTool("protege.cmd " + baseFilename);
			}
		});
		mntmOpenBaseWith.setText("Open &BASE version with Protege");

		MenuItem mntmOpenLocalVersion = new MenuItem(menu_2, SWT.NONE);
		mntmOpenLocalVersion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				runTool("protege.cmd " + localFilename);
			}
		});
		mntmOpenLocalVersion.setText("Open &LOCAL version with Protege");

		MenuItem mntmOpenRemoteVersion = new MenuItem(menu_2, SWT.NONE);
		mntmOpenRemoteVersion.setText("Open &REMOTE version with Protege");
		mntmOpenRemoteVersion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				runTool("protege.cmd " + remoteFilename);
			}
		});
	}

	protected void invalidateOrder() {
		this.orderInvalid  = true;
	}


	protected void addResult(Change<Statement> c) {
    	TableItem i = new TableItem(table_Result, SWT.NONE);
    	i.setText(changeRenderer.getRendering(c));
    	i.setData(c);
    	i.setChecked(true);
	}

	protected void updateResult() {
        table_Result.removeAll();
        for (Change<Statement> c: result.ordered())
        	addResult(c);
        orderInvalid = false;
	}


	private void load(String baseFilename, String localFilename,
			String remoteFilename, String outputFilename) {
		if (null == remoteFilename)
			remoteFilename = localFilename;
		this.baseFilename = new File(baseFilename).getAbsolutePath();
		this.localFilename = new File(localFilename).getAbsolutePath();
		this.remoteFilename = new File(remoteFilename).getAbsolutePath();
		if (null != outputFilename) {
			if (outputFilename.equals("STDOUT"))
				this.outputFilename = outputFilename;
			else
				this.outputFilename = new File(outputFilename).getAbsolutePath();
		}
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		config.setSilentMissingImportsHandling(true);
		OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
		OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
		OWLOntologyManager m3 = OWLManager.createOWLOntologyManager();
		try {
		    OWLOntology o1 = m1.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(baseFilename)), config);
		    OWLOntology o2 = m2.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(remoteFilename)), config);
		    OWLOntology o3 = m3.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(localFilename)), config);
		    base = new ComparableOntology(o1);
		    conflictFinder = new ConflictFinder(base, new ComparableOntology(o2), new ComparableOntology(o3));
		    modified = true;
		    result = new ChangeSet<Statement>();
		    result.addAll(conflictFinder.getCommonChanges());
		    result.addAll(conflictFinder.getRemoteNonconflictingChanges());
		    result.addAll(conflictFinder.getLocalNonconflictingChanges());
		}
		catch (Exception ee) {
			ee.printStackTrace();
		}
	}
	private void fillTables() {
	    table_Common.removeAll();
	    tbtmCommonChanges.setText("Common changes: " + conflictFinder.getCommonChanges().size());
	    for (Change<Statement> c: new ChangeSet<Statement>(conflictFinder.getCommonChanges())) {
	    	TableItem i = new TableItem(table_Common, SWT.NONE);
	    	i.setData(c);
	    	i.setText(changeRenderer.getRendering(c));
	    	i.setChecked(true);
	    }
	    tbtmConflictingChanges.setText("Conflicting changes: " + conflictFinder.getConflictsCount());
	    table_Conflicts1.removeAll();
	    for (Change<Statement> c: new ChangeSet<Statement>(conflictFinder.getRemoteConflicts())) {
	    	TableItem i = new TableItem(table_Conflicts1, SWT.NONE);
	    	i.setData(c);
	    	i.setText(changeRenderer.getRendering(c));
	    	i.setChecked(false);
	    }
	    table_Conflicts2.removeAll();
	    for (Change<Statement> c: new ChangeSet<Statement>(conflictFinder.getLocalConflicts()) ) {
	    	TableItem i = new TableItem(table_Conflicts2, SWT.NONE);
	    	i.setData(c);
	    	i.setText(changeRenderer.getRendering(c));
	    	i.setChecked(false);
	    }
	    tbtmOtherChanges.setText("Other changes: " + (conflictFinder.getLocalNonconflictingChanges().size() + conflictFinder.getRemoteNonconflictingChanges().size()));
	    table_Other1.removeAll();
	    for (Change<Statement> c: new ChangeSet<Statement>(conflictFinder.getRemoteNonconflictingChanges())) {
	    	TableItem i = new TableItem(table_Other1, SWT.NONE);
	    	i.setData(c);
	    	i.setText(changeRenderer.getRendering(c));
	    	i.setChecked(true);
	    }
	    table_Other2.removeAll();
	    for (Change<Statement> c: new ChangeSet<Statement>(conflictFinder.getLocalNonconflictingChanges())) {
	    	TableItem i = new TableItem(table_Other2, SWT.NONE);
	    	i.setData(c);
	    	i.setText(changeRenderer.getRendering(c));
	    	i.setChecked(true);
	    }
	    updateResult();
	}


	private void errorBox(Exception e) {
		e.printStackTrace();
		MessageBox messageBox = new MessageBox(shlMerge, SWT.ICON_ERROR | SWT.OK);
		messageBox.setMessage(e.getMessage());
		messageBox.setText("Error");
		messageBox.open();
	}


	private void save() throws OWLOntologyCreationException,
			OWLOntologyStorageException {
		ComparableOntology changed = new ComparableOntology(base);
		OWLOntology o = changed.applyChanges(result).getOntology();
		if (outputFilename.equals("STDOUT"))
			o.getOWLOntologyManager().saveOntology(o, System.out);
		else
			o.getOWLOntologyManager().saveOntology(o, IRI.create(new File(outputFilename).toURI()));
		modified = false;
	}


	private void selectOutput() {
		FileDialog fd = new FileDialog(shlMerge, SWT.OPEN);
		String[] filterNames = {
				"All supported files (*.owl; *.rdf; *.n3; *.turtle; *.owl)",
				"OWL files (*.owl)",
				"RDF files (*.rdf)",
				"N3 files (*.n3)",
				"Turtle files (*.turtle)",
				"All files (*.*)" };
		String[] filterExt = {
				"*.owl;*.rdf;*.n3;*.turtle",
				"*.owl",
				"*.rdf",
				"*.n3",
				"*.turtle",
				"*.*" };
		fd.setFilterNames(filterNames);
		fd.setFilterExtensions(filterExt);
		fd.setText("Save as...");
		outputFilename = fd.open();
	}

	private void runTool(String cmd) {
		Runtime run = Runtime.getRuntime();
		try {
			int style = SWT.APPLICATION_MODAL | SWT.OK;
		    MessageBox messageBox = new MessageBox(shlMerge, style);
		    messageBox.setText("Running " + cmd);
		    messageBox.setMessage("Please, be careful and do not edit the file. " +
		    		"At least do not save it.");
		    messageBox.open();
			run.exec(cmd);
		} catch (IOException e) {
		    int style = SWT.APPLICATION_MODAL | SWT.OK;
		    MessageBox messageBox = new MessageBox(shlMerge, style);
		    messageBox.setText("Error");
		    messageBox.setMessage(e.getMessage());
		    messageBox.open();
		}
	}
}
