package com.github.kmizu.blawny.runtime

case class AssertionError(message: String) extends Error(message)