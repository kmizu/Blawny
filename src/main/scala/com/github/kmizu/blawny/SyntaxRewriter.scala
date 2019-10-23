package com.github.kmizu.blawny

import com.github.kmizu.blawny.AST._
import com.github.kmizu.blawny.Type._

/**
  * @author Kota Mizushima
  */
class SyntaxRewriter extends Processor[AST.Program, AST.Program] {
  object SymbolGenerator {
    private[this] var counter: Int = 0
    def symbol(): String = {
      val name = "var" + counter
      counter += 1
      name
    }
  }

  import SymbolGenerator.symbol


  def doRewrite(node: AST, variables: Set[String]): AST = node match {
    case Block(location, expressions) =>
      def rewriteBlock(es: List[AST], variables: Set[String]): List[AST] = es match {
        case VarDeclaration(location, variable, type_, value) :: xs =>
          List(Let(location, variable, type_, doRewrite(value, variables), Block(location, rewriteBlock(xs, variables + variable))))
        case FunctionDefinition(loation, name, expression) :: xs =>
          List(LetRec(location, name, doRewrite(expression, variables).asInstanceOf[AST.Lambda], Block(location, rewriteBlock(xs, variables))))
        case (x@EnumDeclaration(_, _, _, _)) :: xs =>
          List(EnumIn(x.location, x, Block(location, rewriteBlock(xs, variables))))
        case (x@SimpleAssignment(_, _, _)) :: xs if !variables.contains(x.variable) =>
          List(Let(location, x.variable, None, doRewrite(x.value, variables), Block(location, rewriteBlock(xs, variables + x.variable))))
        case x :: xs =>
          doRewrite(x, variables) :: rewriteBlock(xs, variables)
        case Nil =>
          Nil
      }
      Block(location, rewriteBlock(expressions, variables))
    case IfExpression(location, cond, pos, neg) =>
      IfExpression(location, doRewrite(cond, variables), doRewrite(pos, variables), doRewrite(neg, variables))
    case WhileExpression(location, condition, body: AST) =>
      WhileExpression(location, doRewrite(condition, variables), doRewrite(body, variables))
    case RecordSelect(location, expression, member) =>
      RecordSelect(location, doRewrite(expression, variables), member)
    case RecordCall(location, self, name, params) =>
      val selfVar = symbol()
      val result = Let(
        location, selfVar, None, self,
        FunctionCall(location, RecordSelect(location, Id(location, selfVar), name), Casting(location, Id(location, selfVar), TDynamic)::(params.map{p => doRewrite(p, variables)}))
      )
      result
    case RecordNew(location, name, members) =>
      RecordNew(location, name, members.map({ case e => doRewrite(e, variables) }))
    case e@ForeachExpression(location, name, collection, body) =>
      val itVariable = symbol()
      val location = e.location
      Block(location, List(
        Let(
          location, itVariable, None, MethodCall(location, Casting(location, doRewrite(collection, variables), TDynamic), "iterator", List()),
          WhileExpression(
            location,
            BinaryExpression(
              location,
              Operator.EQUAL,
              Casting(location, MethodCall(location, Id(location, itVariable), "hasNext", List()), TBoolean),
              BooleanNode(location, true)
            ),
            Block(location, List(
              Let(location, name, None, MethodCall(location, Id(location, itVariable), "next", List()), doRewrite(body, variables))
            ))
          ))
      ))
    case BinaryExpression(location, operator, lhs, rhs) =>
      BinaryExpression(location, operator, doRewrite(lhs, variables), doRewrite(rhs, variables))
    case MinusOp(location, operand) => MinusOp(location, doRewrite(operand, variables))
    case PlusOp(location, operand) => PlusOp(location, doRewrite(operand, variables))
    case literal@StringNode(location, value) => literal
    case literal@IntNode(location, value) => literal
    case literal@LongNode(location, value)  => literal
    case literal@ShortNode(location, value) => literal
    case literal@ByteNode(location, value) => literal
    case literal@BooleanNode(location, value) => literal
    case literal@DoubleNode(location, value) => literal
    case literal@FloatNode(lcation, value) => literal
    case node@Id(_, _) => node
    case node@Selector(_, _, _) => node
    case SimpleAssignment(location, variable, value) => SimpleAssignment(location, variable, doRewrite(value, variables))
    case PlusAssignment(location, variable, value) =>
      val generatedSymbol = symbol()
      val rewrittenValue = doRewrite(value, variables)
      Block(
        location,
        List(
          Let(location, generatedSymbol, None, rewrittenValue,
            SimpleAssignment(location, variable,
              BinaryExpression(location, Operator.ADD, Id(location, variable), Id(location, generatedSymbol) )
            )
          )
        )
      )
    case MinusAssignment(location, variable, value) =>
      val generatedSymbol = symbol()
      val rewrittenValue = doRewrite(value, variables)
      Block(
        location,
        List(
          Let(location, generatedSymbol, None, rewrittenValue,
            SimpleAssignment(location, variable,
              BinaryExpression(location, Operator.SUBTRACT, Id(location, variable), Id(location, generatedSymbol) )
            )
          )
        )
      )
    case MultiplicationAssignment(location, variable, value) =>
      val generatedSymbol = symbol()
      val rewrittenValue = doRewrite(value, variables)
      Block(
        location,
        List(
          Let(location, generatedSymbol, None, rewrittenValue,
            SimpleAssignment(location, variable,
              BinaryExpression(location, Operator.MULTIPLY, Id(location, variable), Id(location, generatedSymbol) )
            )
          )
        )
      )
    case DivisionAssignment(location, variable, value) =>
      val generatedSymbol = symbol()
      val rewrittenValue = doRewrite(value, variables)
      Block(
        location,
        List(
          Let(location, generatedSymbol, None, rewrittenValue,
            SimpleAssignment(location, variable,
              BinaryExpression(location, Operator.DIVIDE, Id(location, variable), Id(location, generatedSymbol) )
            )
          )
        )
      )
    case VarDeclaration(location, variable, optionalType, value) => VarDeclaration(location, variable, optionalType, doRewrite(value, variables))
    case Lambda(location, params, optionalType, proc) => Lambda(location, params, optionalType, doRewrite(proc, variables))
    case FunctionCall(location, func, params) => FunctionCall(location, doRewrite(func, variables), params.map{p => doRewrite(p, variables)})
    case ListLiteral(location, elements) =>  ListLiteral(location, elements.map{e => doRewrite(e, variables)})
    case SetLiteral(location, elements) =>  SetLiteral(location, elements.map{e => doRewrite(e, variables)})
    case MapLiteral(location, elements) => MapLiteral(location, elements.map{ case (k, v) => (doRewrite(k, variables), doRewrite(v, variables))})
    case ObjectNew(location, className, params) => ObjectNew(location, className, params.map{p => doRewrite(p, variables)})
    case MethodCall(location ,self, name, params) => MethodCall(location, doRewrite(self, variables), name, params.map{p => doRewrite(p, variables)})
    case Casting(location, target, to) => Casting(location, doRewrite(target, variables), to)
    case otherwise => throw RewriterPanic(otherwise.toString)
  }

  def transform(program: AST.Program): AST.Program = {
    program.copy(block = doRewrite(program.block, Set.empty).asInstanceOf[AST.Block])
  }

  override final val name: String = "Rewriter"

  override final def process(input: Program): Program = {
    transform(input)
  }
}
