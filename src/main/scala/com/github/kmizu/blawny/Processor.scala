package com.github.kmizu.blawny

abstract class Processor[-In, +Out] {
  def name: String
  def process(input: In): Out
}
