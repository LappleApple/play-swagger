package de.zalando.apifirst

import java.net.URL

import de.zalando.apifirst.Http.MimeType
import de.zalando.apifirst.ParameterPlace.ParameterPlace
import de.zalando.apifirst.naming.{Path, Reference, TypeName}

import scala.language.{implicitConversions, postfixOps}
import scala.util.parsing.input.Positional

sealed trait Expr

object Http {

  abstract class Verb(val name: String) extends Expr

  case object GET     extends Verb("GET")
  case object POST    extends Verb("POST")
  case object PUT     extends Verb("PUT")
  case object DELETE  extends Verb("DELETE")
  case object HEAD    extends Verb("HEAD")
  case object OPTIONS extends Verb("OPTIONS")
  case object TRACE   extends Verb("TRACE")
  case object PATCH   extends Verb("PATCH")

  private val verbs = GET :: POST :: PUT :: DELETE :: HEAD :: OPTIONS :: TRACE :: PATCH :: Nil

  def string2verb(name: String): Option[Verb] = verbs find { _.name == name.trim.toUpperCase }

  // TODO flesh out this hierarchy
  class MimeType(val name: String) extends Expr

  case object ApplicationJson extends MimeType("application/json")

  case class Body(value: Option[String]) extends Expr

}

object Hypermedia {

  // TODO relation should be another API call, not an url
  case class Relation(tpe: String, url: String)

}

object Domain {

  type ModelDefinition = Iterable[Domain.Type]

  case class TypeMeta(comment: Option[String], constraints: Seq[String]) {
    override def toString = s"""TypeMeta($escapedComment, $constraints)"""

    val escapedComment = comment match {
      case None => "None"
      case Some(s) => "Some(\"" + s.replaceAll("\"", "\\\"") + "\")"
    }
  }

  object TypeMeta {
    def apply(comment: Option[String]): TypeMeta = TypeMeta(comment, Seq.empty[String])
  }

  implicit def option2TypeMeta(o: Option[String]): TypeMeta = TypeMeta(o)

  trait PrimitiveType {
    def name: TypeName
  }

  abstract class Type(val name: TypeName, val meta: TypeMeta, denotation: Map[String, Any] = Map.empty) extends Expr {
    def nestedTypes: Seq[Type] = Nil
    def imports: Set[String] = Set.empty
    def toShortString(pad: String) = getClass.getSimpleName
  }
  
  case class TypeRef(override val name: Reference) extends Type(name, TypeMeta(None)) {
    override def toShortString(pad: String) = s"${super.toShortString(pad)}($name)"
  }
  
  abstract class ProvidedType(name: String, override val meta: TypeMeta)
    extends Type(Reference(name), meta)

  class Nmbr(name: String, override val meta: TypeMeta) extends ProvidedType(name, meta) with PrimitiveType

  case class Intgr(override val meta: TypeMeta) extends Nmbr("Int", meta)

  case class Lng(override val meta: TypeMeta) extends Nmbr("Long", meta)

  case class Flt(override val meta: TypeMeta) extends Nmbr("Float", meta)

  case class Dbl(override val meta: TypeMeta) extends Nmbr("Double", meta)

  case class Str(format: Option[String] = None, override val meta: TypeMeta) extends ProvidedType("String", meta) with PrimitiveType

  case class Bool(override val meta: TypeMeta) extends ProvidedType("Boolean", meta) with PrimitiveType

  case class Date(override val meta: TypeMeta) extends ProvidedType("DateMidnight", meta)  with PrimitiveType {
    override val imports = Set("org.joda.time.DateMidnight")
  }

  case class DateTime(override val meta: TypeMeta) extends ProvidedType("DateTime", meta)  with PrimitiveType {
    override val imports = Set("org.joda.time.DateTime")
  }

  case class File(override val meta: TypeMeta) extends ProvidedType("File", meta)  with PrimitiveType {
    override val imports = Set("java.io.File")
  }

  case class BinaryString(override val meta: TypeMeta) extends ProvidedType("BinaryString", meta) with PrimitiveType {
    override val imports = Set("de.zalando.play.controllers.BinaryString", "BinaryString._")
  }

  case class Base64String(override val meta: TypeMeta) extends ProvidedType("Base64String", meta)  with PrimitiveType {
    override val imports = Set("de.zalando.play.controllers.Base64String", "Base64String._")
  }

  case class Password(override val meta: TypeMeta) extends ProvidedType("String", meta) with PrimitiveType

  case class Null(override val meta: TypeMeta) extends ProvidedType("Null", meta) with PrimitiveType

  // case class Any(override val meta: TypeMeta) extends ProvidedType("Any", meta)

  /**
   * Composite classes describe some class composition
   *
   * It is different from the Container because it
   * has more than one underlying Type
   *
   */
  abstract class Composite(override val name: TypeName,
                           override val meta: TypeMeta,
                           val descendants: Seq[Type],
                           val root: Option[Reference])
    extends Type(name, meta) {
    override def toShortString(pad: String) = s"${getClass.getSimpleName}(${descendants.map(_.toShortString(pad+"\t")).mkString(s"\n$pad",s"\n$pad","")})"
    override def nestedTypes = descendants flatMap ( _.nestedTypes )
    override def imports: Set[String] = descendants.flatMap(_.imports).toSet
    def withTypes(t: Seq[Type]): Composite
  }

  case class AllOf(override val name: TypeName,
                   override val meta: TypeMeta,
                   override val descendants: Seq[Type],
                   override val root: Option[Reference] = None)
    extends Composite(name, meta, descendants, root) {
    def withTypes(t: Seq[Type]) = this.copy(descendants = t)
  }

  case class OneOf(override val name: TypeName,
                   override val meta: TypeMeta,
                   override val descendants: Seq[Type])
    extends Composite(name, meta, descendants, None) {
    def withTypes(t: Seq[Type]) = this.copy(descendants = t)
  }

  /**
   * Container is just a wrapper for another single type with some unique properties
   */
  abstract class Container(name: TypeName, val tpe: Type, override val meta: TypeMeta, override val imports: Set[String])
    extends Type(name, meta) {
    def allImports: Set[String] = imports ++ tpe.imports
    override def nestedTypes = Seq(tpe)
    override def toShortString(pad: String) = s"${getClass.getSimpleName}(${tpe.toShortString(pad)})"
    def withType(t: Type): Container
  }

  case class Arr(override val tpe: Type, override val meta: TypeMeta, format: String)
    extends Container(tpe.name / "ArrayWrapper", tpe, meta, Set("de.zalando.play.controllers.ArrayWrapper")) {
    def withType(t: Type) = this.copy(tpe = t)
  }

  case class ArrResult(override val tpe: Type, override val meta: TypeMeta)
    extends Container(tpe.name / "Seq", tpe, meta, Set.empty[String]) {
    def withType(t: Type) = this.copy(tpe = t)
  }

  case class Opt(override val tpe: Type, override val meta: TypeMeta)
    extends Container(tpe.name / "Option", tpe, meta, Set.empty[String]) {
    def withType(t: Type) = this.copy(tpe = t)
  }

  case class CatchAll(override val tpe: Type, override val meta: TypeMeta)
    extends Container(tpe.name / "Map", tpe, meta, Set("scala.collection.immutable.Map")) {
    def withType(t: Type) = this.copy(tpe = t)
    override def nestedTypes = Str(None, None) +: super.nestedTypes
  }

  case class Field(name: TypeName, tpe: Type) {
    def toString(pad: String) = s"""\n${pad}Field($name, ${tpe.toShortString(pad+"\t")})"""

    def imports = tpe match {
      case c: Container => c.allImports
      case o => o.imports
    }

    def nestedTypes = tpe.nestedTypes :+ tpe
  }

  case class TypeDef(override val name: TypeName,
                     fields: Seq[Field],
                     override val meta: TypeMeta) extends Type(name, meta) {
    override def toString = s"""\n\tTypeDef($name, \n\t\tSeq(${fields.mkString("\n\t\t\t", ",\n\t\t\t", "")}\n\t\t), $meta)\n"""

    override def toShortString(pad: String) = s"""TypeDef($name, Seq(${fields.map(_.toString(pad)).mkString(", ")}))"""

    override def nestedTypes = fields flatMap (_.nestedTypes) filter { _.name.parent == name  } distinct

    override def imports = (fields flatMap { _.imports }).toSet
  }

}

case object ParameterPlace extends Enumeration {
  type ParameterPlace = Value
  val PATH    = Value("path")
  val BODY    = Value("body")
  val FORM    = Value("formData")
  val QUERY   = Value("query")
  val HEADER  = Value("header")
}

object Security {
  type OAuth2Scopes = Map[String, String]
  sealed trait Definition {
    def description: Option[String]
  }
  sealed trait OAuth2Definition extends Definition {
    def description: Option[String]
    def scopes: OAuth2Scopes
  }
  case class Basic(description: Option[String]) extends Definition
  case class ApiKey(description: Option[String], name: String, in: ParameterPlace) extends Definition {
    require(in == ParameterPlace.QUERY || in == ParameterPlace.HEADER)
    require(name.nonEmpty)
  }
  case class OAuth2Implicit(description: Option[String], authorizationUrl: URL,
                            scopes: OAuth2Scopes) extends OAuth2Definition {
    require(authorizationUrl != null)
  }
  case class OAuth2Password(description: Option[String], tokenUrl: URL,
                            scopes: OAuth2Scopes) extends OAuth2Definition {
    require(tokenUrl != null)
  }
  case class OAuth2Application(description: Option[String], tokenUrl: URL,
                               scopes: OAuth2Scopes) extends OAuth2Definition {
    require(tokenUrl != null)
  }
  case class OAuth2AccessCode(description: Option[String], authorizationUrl: URL, tokenUrl: URL,
                              scopes: OAuth2Scopes) extends OAuth2Definition {
    require(tokenUrl != null)
    require(authorizationUrl != null)
  }

  sealed trait Constraint {
    def name: String
    def definition: Definition
  }
  case class BasicConstraint(name: String, definition: Definition) extends Constraint
  case class ApiKeyConstraint(name: String, definition: Definition) extends Constraint
  case class OAuth2Constraint(name: String, definition: OAuth2Definition, scopes: Set[String]) extends Constraint {
    require(scopes.forall(definition.scopes.keySet.contains))
  }
  object Constraint {
    def fromDefinition(name: String, definition: Definition, scopes: Set[String]): Constraint = definition match {
      case b: Basic => BasicConstraint(name, b)
      case a: ApiKey => ApiKeyConstraint(name, a)
      case o: OAuth2Definition => OAuth2Constraint(name, o, scopes)
    }
  }
}

object Application {

  case class ParameterRef(name: Reference) {
    val simple = name.simple
  }

  case class Parameter(
    name:             String,
    typeName:         Domain.Type,
    fixed:            Option[String],
    default:          Option[String],
    constraint:       String,
    encode:           Boolean,
    place:            ParameterPlace.Value
  ) extends Expr with Positional

  case class HandlerCall(
    packageName:      String,
    controller:       String,
    instantiate:      Boolean,
    method:           String,
    parameters:       Seq[ParameterRef]
  )

  case class ApiCall(
    verb:             Http.Verb,
    path:             Path,
    handler:          HandlerCall,
    mimeIn:           Set[MimeType],  // can be empty for swagger specification
    mimeOut:          Set[MimeType],  // can be empty for swagger specification
    errorMapping:     Map[String, Seq[Class[Exception]]], // can be empty for swagger specification
    resultTypes:      Map[Int, ParameterRef],
    defaultResult:    Option[ParameterRef],
    security:         Set[Security.Constraint] = Set.empty
  ) {
    def asReference = (path.prepend("paths") / verb.toString.toLowerCase).ref
  }




  type ParameterLookupTable     = Map[ParameterRef, Parameter]
  type TypeLookupTable          = Map[Reference, Domain.Type]
  type DiscriminatorLookupTable = Map[Reference, Reference]

  type SecurityDefinitionsTable = Map[String, Security.Definition]

  case class StrictModel(calls: Seq[ApiCall],
                         typeDefs: TypeLookupTable,
                         params: ParameterLookupTable,
                         discriminators: DiscriminatorLookupTable,
                         basePath: String,
                         packageName: Option[String],
                         securityDefinitionsTable: SecurityDefinitionsTable) {
    def findParameter(ref: ParameterRef): Parameter = params(ref)
    def findParameter(name: Reference): Option[Parameter] = params.find(_._1.name == name).map(_._2)
    def findType(ref: Reference) = typeDefs(ref)
  }

}

