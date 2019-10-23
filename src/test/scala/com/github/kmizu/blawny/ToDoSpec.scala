package com.github.kmizu.blawny

import com.github.kmizu.blawny.runtime.NotImplementedError

class ToDoSpec extends SpecHelper {
  describe("ToDo() function") {
    it("throw NotImplementedException when it is evaluated") {
      intercept[NotImplementedError] {
        E(
          """
            |function fact(n)
            |  return if n < 2
            |  (
            |    ToDo()
            |  )
            |  else
            |  (
            |    n * fact(n - 1)
            |  )
            |fact(0)
          """.stripMargin
        )
      }
    }
  }
}
