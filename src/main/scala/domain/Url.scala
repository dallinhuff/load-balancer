package com.dallinhuff.loadbalancer.domain

opaque type Url = String

object Url:
  inline def apply(value: String): Url = value

extension (url: Url)
  def toString: String = url
