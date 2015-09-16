package wav.devtools.karaf

import java.io.{FilenameFilter, File}

package object manager {

  private [manager] def filterFileNames(dir: File)(filter: String => Boolean): Seq[String] =
    dir.list(new FilenameFilter() {
      override def accept(dir: File, name: String): Boolean = filter(name)
    })

}
