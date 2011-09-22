import os
import sys
import subprocess
import tempfile

extensions = ('.rdf', '.ttl', '.owl')

def cmd(s):
    print '>', s
    result = subprocess.Popen(s.split(), stdout=subprocess.PIPE, shell=True).stdout.read()
    print result
    return result

class TempFile(object):
    def __init__(self, contents=None):
        fd, self.filename = tempfile.mkstemp('.ontovcs', text=True)
        f = os.fdopen(fd, 'w')
        if contents:
            f.write(contents);
        f.close()

    def __enter__(self):
        return self.filename

    def __exit__(self, ex_type, ex_value, ex_tb):
        if os.path.exists(self.filename):
            try:
                os.remove(self.filename)
            except:
                print sys.exc_info()

class Repo:
    def __init__(self, root):
        self.root= root
        ontocvsdir = os.path.join(root, '.ontovcs')
        self.changesdir = os.path.join(ontocvsdir, 'changes')
    def index_revision(self, node, pnode, files=None):
        print node
        if not pnode:
            return
        nodechangesdir = os.path.join(self.changesdir, node + '_' + pnode)
        if not files:
            files = self.get_node_files(node, pnode)
        ontologies = []
        for f in files:
            for ext in extensions:
                if f.endswith(ext):
                    ontologies.append(f)
                    break
        if ontologies:
            print 'Files: ' + ' '.join(ontologies)
        else:
            print 'Nothing to index'

        for filename in ontologies:
            print 'Indexing ' + filename
            filedir = os.path.join(nodechangesdir, os.path.split(filename)[0])
            if not os.path.exists(filedir):
                os.makedirs(filedir)
            with TempFile() as temp1:
                with TempFile() as temp2:
                    self.export_revision(pnode, filename, temp1)
                    self.export_revision(node, filename, temp2)
                    cmd('owl2diff %(prev)s %(current)s > %(out)s' %
                        {'prev': temp1, 'current': temp2, 'out': os.path.join(nodechangesdir, filename)})
    def index_all(self):
        for node, pnode, files in self.iter_nodes_files():
            self.index_revision(node, pnode, files)


class MercurialRepo(Repo):
    def configdir(self):
        return '.hg'
    def get_node_files(self, node, pnode):
        if node:
            with TempFile('changeset = "{files}"\nfile = "{file}\\n"\n') as style:
                return cmd('hg log -r %s --style %s' % (node, style)).split('\n')
        else:
            return cmd('hg status -man').split('\n')
    def export_revision(self, rev, source, dest):
        cmd('hg cat -r %(rev)s -o %(out)s %(file)s' %
                    {'rev': rev, 'out': dest, 'file': source})
    def iter_nodes_files(self):
        with TempFile('''changeset = "{node} {parents}\\n{files}\\n"\nfile = "{file}\\n"''') as style:
            revs = cmd('hg log -r : --style ' + style).split('\n\n')
        pnode = None
        for rev in revs:
            if not rev:
                continue
            lines = rev.split('\n')
            files = lines[1:]
            items = lines[0].split()
            node = items[0]
            for parent in items[1:]:
                yield node, parent, files
            yield node, pnode, files
            pnode = node

class GitRepo(Repo):
    def configdir(self):
        return '.git'
    def get_node_files(self, node, pnode):
        return cmd('git diff --name-only ' + pnode + '..' + node).split('\n')[:-1]
    def export_revision(self, rev, source, dest):
        cmd('git cat-file -p %(hash)s:%(source)s > %(out)s' %
            {'hash': rev, 'source': source, 'out': dest})
    def iter_nodes_files(self):
        lines = cmd('git log --format=%H_%P --reverse').split('\n')[:-1]
        for line in lines:
            node, parents = line.split('_')
            for parent in parents.split():
                yield node, parent, None

known_vcs = {
    'hg': MercurialRepo,
    'git': GitRepo,
}



def main(hook=None):
    root = ''
    for vcs in known_vcs:
        if os.path.exists(os.path.join(root, '.' + vcs)):
            repo = known_vcs[vcs](root)
            break
    try:
        repo
    except:
        print "Unknown version control system"
        return 1

    repo.index_all()

    return 0

if __name__ == '__main__' :
    if len(sys.argv) > 1:
        sys.exit(main(sys.argv[1]))
    else:
        sys.exit(main())
