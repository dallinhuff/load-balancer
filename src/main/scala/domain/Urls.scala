package com.dallinhuff.loadbalancer.domain


import scala.util.Try

opaque type Urls = Vector[Url]

object Urls:
  inline def apply(urls: Url*): Urls = Vector.from(urls)
  inline def empty: Urls = Vector.empty[Url]

extension (urls: Urls)
  def currentOption: Option[Url] = urls.headOption
  def currentUnsafe: Url = urls.head
  def rotated: Urls = Try(urls.tail :+ urls.head).getOrElse(Urls.empty)
  def remove(url: Url): Urls = urls.filter(_ != url)
  def add(url: Url): Urls = if urls contains url then urls else urls :+ url
  inline infix def +(url: Url): Urls = urls.add(url)
  inline infix def -(url: Url): Urls = urls.remove(url)
end extension

