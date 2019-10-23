package com.github.kmizu.blawny

/**
  * Created by Mizushima on 2016/05/30.
  */
class ExpressionSpec extends SpecHelper {
  describe("assignment") {
    it("is evaluated correctly") {
      assertResult(
        E(
          """
            |a=1
            |a
          """.stripMargin))(BoxedInt(1))
      assertResult(
        E(
          """
            |a=1
            |a = a + 1
            |a
          """.stripMargin))(BoxedInt(2))
      assertResult(
        E(
          """
            |a=1
            |a += 1
            |a
          """.stripMargin))(BoxedInt(2))
      assertResult(
        E(
          """
            |a=1
            |a -= 1
            |a
          """.stripMargin))(BoxedInt(0))
      assertResult(
        E(
          """
            |a=3
            |a *= 2
            |a
          """.stripMargin))(BoxedInt(6))
      assertResult(
        E(
          """
            |a=5
            |a /= 2
            |a
          """.stripMargin))(BoxedInt(2))
    }
  }

  describe("while expression") {
    it("is evaluated correctly") {
      assertResult(
        E(
          """
            |i = 1
            |while i < 10
            |(
            |  i = i + 1
            |)
            |i
          """.stripMargin))(BoxedInt(10))
      assertResult(
        E(
          """
            |i = 10
            |while i >= 0
            |(
            |  i = i - 1
            |)
            |i
          """.stripMargin))(BoxedInt(-1))
      assertResult(
        E(
          s"""
             |buf = new java.lang.StringBuffer
             |i = 0
             |while i <= 5
             |(
             |  buf->append("#{i}")
             |  i = i + 1
             |)
             |buf->toString()
      """.stripMargin))(ObjectValue("012345"))
    }
  }

  describe("anonymous function") {
    it("is evaluated correctly") {
      assertResult(
        E("""
            |add = (x, y) => x + y
            |add(3, 3)
          """.stripMargin))(BoxedInt(6))
    }
  }

  describe("logical expression") {
    it("is evaluated correctly"){
      assertResult(
        E(
          """
            |i = 1
            |0 <= i && i <= 10
          """.stripMargin))(BoxedBoolean(true))
      assertResult(
        E(
          """
            |i = -1
            |0 <= i && i <= 10
          """.stripMargin))(BoxedBoolean(false))
      var input =
        """
            |i = -1
            |i < 0 || i > 10
        """.stripMargin
      assertResult(
        E(input)
      )(
        BoxedBoolean(true)
      )
      input =
        """
          |i = 1
          |i < 0 || i > 10
        """.stripMargin
      assertResult(
        E(input)
      )(BoxedBoolean(false))
    }

    describe("foreach expression") {
      it("is evaluated correctly") {
        assertResult(
          E(
            """
              |newList = new java.util.ArrayList
              |foreach a in [1, 2, 3, 4, 5]
              |(
              |  newList->add((a :> Int) * 2)
              |)
              |newList
            """.stripMargin))(ObjectValue(listOf(2, 4, 6, 8, 10)))
      }
    }

    describe("if expression") {
      it("is evaluated correctly") {
        assertResult(
          E(
            """
              |if true
              |(
              |  1.0
              |)
              |else
              |(
              |  2.0
              |)
            """.stripMargin))(BoxedDouble(1.0))
        assertResult(
          E(
            """
              |if false
              |(
              |  1.0
              |)
              |else
              |(
              |  2.0
              |)
            """.stripMargin))(BoxedDouble(2.0))
      }
    }

    describe("while expression") {
      it("is evaluated correctly") {
        assertResult(
          E(
            """
              |i = 0
              |while i < 10
              |(
              |  i = i + 1
              |)
              |i
            """.stripMargin))(BoxedInt(10))
      }
    }

    describe("function definition") {
      it("is evaluated correctly") {
        assertResult(
          E(
            """
              |function add(x, y)
              |  return x + y
              |add(2, 3)
            """.stripMargin))(BoxedInt(5))
        assertResult(
          E(
            """
              |function fact(n)
              |  return if n < 2
              |  (
              |    1
              |  )
              |  else
              |  (
              |    n * fact(n - 1)
              |  )
              |fact(4)
            """.stripMargin))(BoxedInt(24))
        assertResult(
          E(
            """
              |function none()
              |  return 24
              |none()
            """.stripMargin))(BoxedInt(24))
        assertResult(
          E(
            """
              |function hello()
              |  "Hello"
              |  return 0
              |hello()
            """.stripMargin))(BoxedInt(0))
      }
    }
  }
}
