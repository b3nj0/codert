package codert.vcs.svn

import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNRevision

import codert.vcs.Repo
import codert.vcs.Rev
import java.io.ByteArrayOutputStream
import org.tmatesoft.svn.core.SVNLogEntry
import codert.vcs.LogEntry
import codert.vcs.Diff
import java.util.ArrayList
import java.time.LocalDateTime
import codert.vcs.LogEntryPath

data class SvnRev(val rev: SVNRevision) : Rev {
    override fun offset(count: Int): Rev = SvnRev(SVNRevision.create(Math.max(rev.getNumber() + count, 1)))
}

class SvnRepo(url_: String) : Repo {
    val url = SVNURL.parseURIEncoded(url_)
    val repo = SVNRepositoryFactory.create(url)
    val svn = SVNClientManager.newInstance()

    override fun rev(rev: String): Rev = SvnRev(SVNRevision.create(rev.toLong()))

    override fun logs(rev1: Rev, rev2: Rev): List<LogEntry> {
        val entries = ArrayList<LogEntry>()
        if (rev1 is SvnRev && rev2 is SvnRev) {
            val changedPath = true
            val strictNode = true
            repo.log(array(""), rev1.rev.getNumber(), rev2.rev.getNumber(), changedPath, strictNode) { (e: SVNLogEntry) ->
                val changedPaths = e.getChangedPaths().values().map { (lep) ->
                    LogEntryPath(lep.getPath(), lep.getType(), lep.getKind().toString(), lep.getCopyPath(), rev(""+lep.getCopyRevision()))
                }
                entries.add(LogEntry(rev(""+e.getRevision()), e.getMessage(), e.getAuthor(), e.getDate().toInstant(), changedPaths))
            }
        }
        return entries
    }

    override fun diff(rev1: Rev, rev2: Rev): Diff {
        val buffer = ByteArrayOutputStream()
        if (rev1 is SvnRev && rev2 is SvnRev) {
            val diff = svn.getDiffClient()
            diff.doDiff(url, rev1.rev, url, rev2.rev, true, true, buffer)
        }
        return Diff(String(buffer.toByteArray(), "UTF-8"))
    }

    override fun latestRev(): Rev = SvnRev(SVNRevision.create(repo.getLatestRevision()))
}