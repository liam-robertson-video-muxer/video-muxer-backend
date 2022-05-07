package com.openAnimation.app.tools

import java.io.File

object CustomClasspath {

  def getResourcePath(resource: String): String = {
    val jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    if (jarFile.isFile()) {
      resource
    } else {
      this.getClass.getClassLoader.getResource(resource).getPath.tail
    }
  }
}
