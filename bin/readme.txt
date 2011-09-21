Add this directory to your PATH

owl2diff
owl2diff.cmd
	owl2diff.jar runner

owl2diff.git.sh
	owl2diff wrapper for Git.
	owl2enable configures Git repository to use this file.

owl2diff.svn.sh
owl2diff.svn.cmd
	owl2diff wrapper for Subversion.
	Usage: svn diff ... --diff-cmd owl2diff.svn.cmd

owl2enable
owl2enable.cmd
	Shell script that enables OntoVCS for Git or Mercurial repository in current directory
	
owl2merge
owl2merge.cmd
	Automatically detects your operating system and runs a corresponding version of owl2merge-*.jar
	
owl2merge.git.sh
	owl2merge wrapper for Git.
	owl2enable configures Git repository to use this file.
	
owl2merge.svn.sh
owl2merge.svn.cmd
	owl2merge wrapper for Subversion.
	Usage: svn merge ... --diff3-cmd owl2merge.svn.cmd
	
protege.cmd
	Protege runner. Edit path to Protege inside this file.
	
hgrc.sample
	Mercurial config sample. owl2enable appends contents of this file to your .hg/hgrc. Be careful!
