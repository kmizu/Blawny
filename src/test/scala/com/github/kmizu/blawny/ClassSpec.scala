package com.github.kmizu.blawny

/**
  * Created by Mizushima on 2016/05/30.
  */
class ClassSpec extends SpecHelper {

  describe("new record") {
    val expectations: List[(String, Value)] = List(
      """
        |class Person {
        |  name: *
        |  age: Int
        |}
        |#Person("Hoge", 7)
      """.stripMargin -> RecordValue("Person", List("name" -> ObjectValue("Hoge"), "age" -> BoxedInt(7))),
      """
        |class Tuple<'a, 'b> {
        |  _1: 'a
        |  _2: 'b
        |}
        |#Tuple(1, 2)
      """.stripMargin -> RecordValue("Tuple", List("_1" -> BoxedInt(1), "_2" -> BoxedInt(2)))
    )

    expectations.foreach{ case (in, expected) =>
      it(s"${in} evaluates to ${expected}") {
        assert(expected == E.evaluateString(in))
      }
    }
  }

  describe("access record") {
    val expectations: List[(String, Value)] = List(
      """
        |class Person {
        |  name: *
        |  age: Int
        |}
        |p = #Person("Hoge", 7)
        |p.name
      """.stripMargin -> ObjectValue("Hoge"),
      """
        |class Tuple<'a, 'b> {
        |  _1: 'a
        |  _2: 'b
        |}
        |t = #Tuple(1, 2)
        |t._1
      """.stripMargin -> BoxedInt(1)
    )

    expectations.foreach{ case (in, expected) =>
      it(s"${in} evaluates to ${expected}") {
        assert(expected == E(in))
      }
    }

    intercept[TyperException] {
      E {
        """
          | class Person {
          |   name: *
          |   age: Int
          | }
          | p = #Person("Hoge", 1.0)
        """.stripMargin
      }
    }
    intercept[TyperException] {
      E {
        """
          | class Tuple<'a, 'b> {
          |   _1: 'a
          |   _2: 'b
          | }
          | t = #Tuple(1, 2)
          | val k: Double = t._1
        """.stripMargin
      }
    }
  }
}
