package kms.diff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import kms.diff.Settings.StatementFormat;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ru.tpu.cc.kms.EntityShortener;
import ru.tpu.cc.kms.IriFormat;
import ru.tpu.cc.kms.changes.CategorizedChangeSet;
import ru.tpu.cc.kms.changes.Change;
import ru.tpu.cc.kms.changes.ChangeSet;
import ru.tpu.cc.kms.changes.ChangesSummary;
import ru.tpu.cc.kms.changes.render.ChangeRenderer;
import ru.tpu.cc.kms.changes.render.FunctionalSyntaxChangeRenderer;
import ru.tpu.cc.kms.changes.render.IndentedChangeRenderer;
import ru.tpu.cc.kms.statements.Statement;
import ru.tpu.cc.kms.statements.StatementType;

class Settings {
    public static enum StatementFormat {
        COMPACT, INDENTED,
    };

    @Argument(required = true, index = 0, metaVar = "parent.owl", usage = "Parent version")
    public String parentFilename;
    @Argument(required = true, index = 1, metaVar = "child.owl", usage = "Child version")
    public String childFilename;
    @Option(name = "--prefixes", aliases = { "-p" }, usage = "Display namespace prefixes", required = false)
    public Boolean prefixes = false;
    @Option(name = "--summary", aliases = { "-s" }, usage = "Display changes summary", required = false)
    public Boolean summary = false;
    @Option(name = "--raw", aliases = { "-r" }, usage = "Display raw changes", required = false)
    public Boolean raw = false;
    @Option(name = "--by-entity", aliases = { "-e" }, usage = "Display changes related to each changed entity", required = false)
    public Boolean by_entity = false;
    @Option(name = "--verbose", aliases = { "-v" }, usage = "Verbose output", required = false)
    public Boolean verbose = false;
    @Option(name = "--measure", aliases = { "-m" }, usage = "Measure time", required = false)
    public Boolean measure = false;
    @Option(name = "--wait", aliases = { "-w" }, usage = "Do not exit, wait until user presses Enter", required = false)
    public Boolean wait = false;
    @Option(name = "--format", aliases = { "-f" }, metaVar = "format", usage = "Format of statements: compact or indented", required = false)
    public StatementFormat statementFormat = StatementFormat.COMPACT;
    @Option(name = "--iriformat", aliases = { "-i" }, metaVar = "iriformat", usage = "Format of IRIs: simple, qname, full", required = false)
    public IriFormat iriFormat = IriFormat.QNAME;

}

public class Main {

    /*
     * Returns changes count
     */
    private static int CompareFiles(String parent, String child,
            Settings settings) {
        System.err.println("diff " + parent + " " + child);
        try {
            CategorizedChangeSet cs;
            if (settings.measure) {
                Hashtable<String, Long> times = new Hashtable<String, Long>();
                cs = Comparer.compareAndMeasure(parent, child, times);
                // Times
                System.err.println("Loading time:    " + times.get("load")
                        / 1000000 + " milliseconds");
                System.err.println("Building time:   " + times.get("build")
                        / 1000000 + " milliseconds");
                System.err.println("Comparing time:  " + times.get("compare")
                        / 1000000 + " milliseconds");
                long totalDuration = times.get("load") + times.get("build")
                        + times.get("compare");
                System.err.println("Total time:      " + totalDuration
                        / 1000000 + " milliseconds");
                // Statistics
                System.err.println(parent);
                Comparer.printStatistics(cs.getParent(), System.err);
                System.err.println(child);
                Comparer.printStatistics(cs.getChild(), System.err);
            } else
                cs = Comparer.compare(parent, child);
            ChangesSummary ca = null;
            if (settings.prefixes || settings.by_entity)
                ca = new ChangesSummary(cs);
            if (settings.prefixes) {
                // Display namespace prefixes
                for (Map.Entry<String, String> e : ca.getPrefixes().entrySet()) {
                    System.out.print(e.getKey());
                    System.out.print("=");
                    System.out.println(e.getValue());
                }
                System.out.println();
            }
            if (settings.summary) {
                // Calculate summary
                Comparer.PrintSummary(cs, System.out, settings.iriFormat);
            }
            Collection<Change<Statement>> changes = cs.getAllChanges();
            ChangeRenderer cr;
            if (settings.statementFormat == StatementFormat.INDENTED)
                cr = new IndentedChangeRenderer(settings.iriFormat);
            else
                cr = new FunctionalSyntaxChangeRenderer(settings.iriFormat);
            // Raw changes
            if (settings.raw) {
                for (Change<Statement> c : changes)
                    System.out.println(cr.getRendering(c));
            }
            // By entity
            if (settings.by_entity) {
                for (Change<Statement> c : ca.getChangesByEntity(null))
                    System.out.println(cr.getRendering(c));
                EntityShortener s = new EntityShortener(settings.iriFormat);
                for (OWLEntity e : ca.getRemovedEntities()) {
                    System.out.println();
                    System.out.println("--- " + e.getEntityType() + ": " + s.shorten(e));
                    for (Change<Statement> c : ca.getChangesByEntity(e))
                        System.out.println(cr.getRendering(c));
                }
                for (OWLEntity e : ca.getNewEntities()) {
                    System.out.println();
                    System.out.println("+++ " + e.getEntityType() + ": " + s.shorten(e));
                    for (Change<Statement> c : ca.getChangesByEntity(e))
                        System.out.println(cr.getRendering(c));
                }
                for (OWLEntity e : ca.getModifiedEntities()) {
                    System.out.println();
                    System.out.println("*** " + e.getEntityType() + ": " + s.shorten(e));
                    for (Change<Statement> c : ca.getChangesByEntity(e))
                        System.out.println(cr.getRendering(c));
                }
            }
            System.out.println();
            return changes.size();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (UnparsableOntologyException e) {
            if (settings.verbose)
                System.err.println(e.toString());
            else
                System.err.println("Could not parse: "
                        + e.getDocumentIRI().toString());
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace(System.err);
        }
        return 0;
    }

    private static int CompareDirectories(File parent, File child,
            Settings settings) {
        int changesCount = 0;
        for (File file : parent.listFiles()) {
            String relative = parent.toURI().relativize(file.toURI()).getPath();
            File file2 = new File(child, relative);
            if (!file2.exists()) {
                System.err.println("Only in " + parent.getAbsolutePath() + ": "
                        + file.getName());
                changesCount++;
                continue;
            }
            if (file.isFile() && file2.isDirectory()) {
                System.err.println("File " + file.getAbsolutePath()
                        + " is a regular file while file "
                        + file2.getAbsolutePath() + " is a directory");
                changesCount++;
                continue;
            }
            if (file.isDirectory() && file2.isFile()) {
                System.err.println("File " + file.getAbsolutePath()
                        + " is a directory while file "
                        + file2.getAbsolutePath() + " is a regular file");
                changesCount++;
                continue;
            }
            if (file.isDirectory() && file2.isDirectory()) {
                changesCount += CompareDirectories(file, file2, settings);
                continue;
            }
            if (file.isFile() && file2.isFile()) {
                changesCount += CompareFiles(file.getAbsolutePath(),
                        file2.getAbsolutePath(), settings);
                continue;
            }
        }
        for (File file : child.listFiles()) {
            String relative = child.toURI().relativize(file.toURI()).getPath();
            File file2 = new File(parent, relative);
            if (!file2.exists()) {
                System.err.println("Only in " + child.getAbsolutePath() + ": "
                        + file.getName());
                changesCount++;
                continue;
            }
        }
        return changesCount;
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        Settings settings = new Settings();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
            if (!settings.by_entity && !settings.raw && !settings.summary) {
                System.err
                        .print("At least one option is required from the following list: ");
                System.err.println("--by-entity, --raw, --summary");
                throw new CmdLineException("Need more options.");
            }
            File parent = new File(settings.parentFilename);
            File child = new File(settings.childFilename);
            if (!parent.exists())
                throw new FileNotFoundException(settings.parentFilename);
            if (!child.exists())
                throw new FileNotFoundException(settings.childFilename);
            if (parent.isDirectory() && child.isDirectory()) {
                CompareDirectories(parent, child, settings);
            } else {
                CompareFiles(settings.parentFilename, settings.childFilename,
                        settings);
            }
        } catch (CmdLineException e) {
            System.err.println("owl2diff " + getVersion());
            System.err.print("Usage: owl2diff");
            parser.printSingleLineUsage(System.err);
            System.err.println();
            parser.printUsage(System.err);
            System.exit(64);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }

        if (settings.wait) {
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getVersion() {
        Class<Main> clazz = Main.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return "";
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
            "/META-INF/MANIFEST.MF";
        Manifest manifest;
        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (MalformedURLException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
        Attributes attr = manifest.getMainAttributes();
        return attr.getValue("Version");
    }
}
