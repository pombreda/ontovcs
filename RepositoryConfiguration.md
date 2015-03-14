# Git #

Cd into your Git repository and type `owl2enable`. This will enable `owl2diff` and `owl2merge` for files with extensions `.rdf`, `.ttl` and `.owl`. If you are using other extensions for your OWL ontologies, modify file `.git/info/attributes`.

# Mercurial #

## Clean repository ##

Cd into your clean Mercurial repository and type `owl2enable`. This will enable `owl2diff` and `owl2merge` for files with extensions `.rdf`, `.ttl` and `.owl`. If you are using other extensions for your OWL ontologies, modify sections `[diff-patterns]` and `[merge-patterns]` of file `.hg/hgrc`.

## Existing repository ##

Cd into your existing Mercurial repository.
Backup your `.hg/hgrc` file.
Then type `owl2enable`.
This will enable `owl2diff` and `owl2merge` for files with extensions `.rdf`, `.ttl` and `.owl`. If you are using other extensions for your OWL ontologies, modify sections `[diff-patterns]` and `[merge-patterns]` of file `.hg/hgrc`.

# Subversion #

## Command line support ##

There are wrappers for SVN included in the distribution for using with `svn` command-line client.

Here is an example:

`svn diff pizza.owl --diff-cmd owl2diff.svn.cmd`

Use `--diff3-cmd owl2merge.svn.cmd` with `merge` command.

## TortoiseSVN ##

If you are using TortoiseSVN, you can configure it to use `owl2diff` and `owl2merge` for `.owl` files at TortoiseSVN settings `->` External Programs `->` Diff Viewer | Merge Tool `->` Advanced...

### `owl2diff` ###

Extension or mime-type: `.owl`

External: `D:\path\to\ontovcs\bin\owl2diff.cmd %base %mine --summary --wait`

### `owl2merge` ###

Extension or mime-type: `.owl`

External: `D:\path\to\ontovcs\bin\owl2merge.cmd %base %mine %theirs -o %merged --auto`