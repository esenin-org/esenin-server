package com.vovapolu.nlpserver.config



case class LibraryConfig(dockerImage: String, name: String, )
case class LibrariesConfig(libs: List[LibrariesConfig] = Nil)
