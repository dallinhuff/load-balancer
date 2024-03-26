package com.dallinhuff.loadbalancer.domain

import munit.FunSuite

class UrlsTest extends FunSuite:
  private def sequentialUrls(from: Int, to: Int): Urls =
    Urls((from to to).map(i => Url(s"url$i"))*)

  test("Urls(url1, url2, ...).currentOption returns Some(url1)"):
    val urls = sequentialUrls(1, 5)
    val obtained = urls.currentOption
    val expected = Some(Url("url1"))

    assertEquals(obtained, expected)

  test("Urls.empty.currentOption returns None"):
    val obtained = Urls.empty.currentOption
    assertEquals(obtained, None)

  test("Urls(url1, url2, ...).currentUnsafe returns url1"):
    val urls = sequentialUrls(1, 5)
    val obtained = urls.currentUnsafe
    val expected = Url("url1")

    assertEquals(obtained, expected)

  test("Urls.empty.currentUnsafe throws NoSuchElementException"):
    intercept[NoSuchElementException]:
      Urls.empty.currentUnsafe

  test("Urls(url1, url2, ...) - url1 should drop url1"):
    val urls = sequentialUrls(1, 5)
    val obtained = urls - Url("url1")
    val expected = sequentialUrls(2, 5)

    assertEquals(obtained, expected)

  test("Urls(url1, url2, ...) + url1 should not add url1 again"):
    val urls = sequentialUrls(1, 5)
    val obtained = urls + Url("url1")
    val expected = urls

    assertEquals(obtained, expected)

  test("Urls(url1, url2, ...) + urlN should append url at end"):
    val urls = sequentialUrls(1, 5)
    val obtained = urls + Url("url6")
    val expected = sequentialUrls(1, 6)

    assertEquals(obtained, expected)

