package codert.vcs

import codert.vcs.svn.SvnRepo
import java.time.Instant


trait Rev {
    fun offset(count: Int): Rev
}

trait Repo {
    fun rev(rev: String): Rev
    fun latestRev(): Rev
    fun logs(rev1: Rev, rev2: Rev): List<LogEntry>
    fun diff(rev1: Rev, rev2: Rev): Diff
}

data class Diff(val diff: String)

data class LogEntryPath(val path: String, val mod: Char, val type: Any, val copyPath: String?, val copyRev: Rev?)

data class LogEntry(val rev: Rev, val message: String, val author: String, val date: Instant, val changedPaths: List<LogEntryPath>)

fun repo(name: String): Repo {
    when (name) {
        "myrepo" -> return SvnRepo("file:///home/jenksb/workspace/svn-repo-1/myrepo")
        else -> throw IllegalArgumentException("Unknown reposition ${name}")
    }
}
