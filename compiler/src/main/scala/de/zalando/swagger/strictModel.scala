package de.zalando.swagger

import java.net.URL

import com.fasterxml.jackson.annotation.{JsonProperty, JsonAnySetter}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

import scala.collection.mutable
import scala.util.Try

/**
 * @author  slasch
 * @since   09.10.2015.
 */
object strictModel {

  import NumberValidation._
  import StringValidation._
  import ArrayValidation._
  import ObjectValidation._

  sealed trait API

  /**
   * As there is no easy way to validate some properties by defining type constraints,
   * These traits will be used as a mixin to perform runtime validation of the specification itself
   */
  trait PatternChecker {
    def matches(pattern: String, value: String): Boolean =
      value == null || pattern.r.unapplySeq(value).nonEmpty
  }
  trait UriChecker {
    def url: URI
    require(url == null || Try(new URL(url)).isSuccess, s"Valid URI was expected, but got $url")
  }
  trait EmailChecker extends PatternChecker {
    def email: Email
    private val pattern = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
    require(matches(pattern, email))
  }

  /**
   * For now, we only support object references
   */
  trait RefChecker extends PatternChecker {
    def $ref: Ref
    private val pattern = """#/.*"""
    require(matches(pattern, $ref))
  }

  // format: OFF
  /**
   * The main container of swagger information
   *
   * @param swagger
   * @param info
   * @param paths               Relative paths to the individual endpoints. They must be relative to the 'basePath'.
   * @param schemes             The transfer protocol of the API.
   * @param host                The host (name or ip) of the API. Example: 'swagger.io'
   * @param basePath            The base path to the API. Example: '/api'.
   * @param consumes            A list of MIME types accepted by the API.
   * @param produces            A list of MIME types the API can produce.
   * @param definitions         One or more JSON objects describing the schemas being consumed and produced by the API.
   * @param parameters          One or more JSON representations for parameters
   * @param responses           One or more JSON representations for responses
   * @param securityDefinitions
   * @param security
   * @param tags
   */
  case class SwaggerModel(
    @JsonScalaEnumeration(classOf[SwaggerEnumType]) @JsonProperty(required = true) swagger: SwaggerEnum.Value,
    @JsonProperty(required = true) info:  Info,
    @JsonProperty(required = true) paths: Paths,

    @JsonScalaEnumeration(classOf[SchemeType]) schemes: SchemesList,
    host:                 Host,
    basePath:             BasePath,
    consumes:             MediaTypeList,
    produces:             MediaTypeList,
    definitions:          Definitions, // Definitions are collections of types
    parameters:           ParameterDefinitions, // Collections of parameters which can be referenced later
    responses:            ResponseDefinitions,  // Default response definition, can be overridden
    securityDefinitions:  SecurityDefinitions,
    security:             Security,
    tags:                 Tags
  ) extends VendorExtensions with API with PatternChecker {
    require(matches("^/.*", basePath), "Base path should start with a slash (/)")
    require(matches("^[^{}/ :\\\\]+(?::\\d+)?$", host), s"Host name is expected, but got $host")
    require(paths == null || paths.keySet.forall(matches("^/.*", _)), "Paths should start with a slash (/)")
  }

  private[swagger] class SchemeType extends TypeReference[Scheme.type]

  private[swagger] class SwaggerEnumType extends TypeReference[SwaggerEnum.type]

  /**
   * The transfer protocol of the API.
   */
  case object Scheme extends Enumeration {
    type Scheme = Value
    val HTTP  = Value("http")
    val HTTPS = Value("https")
    val WS    = Value("ws")
    val WSS   = Value("wss")
  }

  private[swagger] class CollectionFormatReference extends TypeReference[CollectionFormat.type]
  case object CollectionFormat extends Enumeration {
    type CollectionFormat = Value
    val CSV     = Value("csv")
    val SSV     = Value("ssv")
    val TSV     = Value("tsv")
    val PIPES   = Value("pipes")
  }

  private[swagger] class CollectionFormatWithMultiReference extends TypeReference[CollectionFormatWithMulti.type]
  case object CollectionFormatWithMulti extends Enumeration {
    type CollectionFormatWithMulti = Value
    val CSV     = Value("csv")
    val SSV     = Value("ssv")
    val TSV     = Value("tsv")
    val PIPES   = Value("pipes")
    val MULTI   = Value("multi")
  }

  private[swagger] class PrimitiveTypeReference extends TypeReference[PrimitiveType.type]
  case object PrimitiveType extends Enumeration {
    class Val(i: Int, name: String) extends super.Val(i, name) with JsonSchemaType
    type PrimitiveType = Val
    val ARRAY   = new Val(1, "array")
    val BOOLEAN = new Val(2, "boolean")
    val INTEGER = new Val(3, "integer")
    val NUMBER  = new Val(4, "number")
    val STRING  = new Val(5, "string")
    val OBJECT  = new Val(6, "object")
    val NULL  = new Val(0, "null")
    def fromString(name: String) = name match {
      case "array"    => ARRAY
      case "boolean"  => BOOLEAN
      case "integer"  => INTEGER
      case "number"   => NUMBER
      case "string"   => STRING
      case "object"   => OBJECT
      case "null"     => NULL
    }
  }
  private[swagger] class ParameterTypeReference extends TypeReference[ParameterType.type]
  case object ParameterType extends Enumeration {
    type ParameterType = Value
    val ARRAY   = Value("array")
    val BOOLEAN = Value("boolean")
    val INTEGER = Value("integer")
    val NUMBER  = Value("number")
    val STRING  = Value("string")
    val FILE    = Value("file") // only for formData
  }
  /**
   * Single value available for swagger version 2.0
   */
  case object SwaggerEnum extends Enumeration {
    type SwaggerEnum = Value
    val v2_0  = Value("2.0")
  }

  // The value of this keyword MUST be either a string or an array.
  // If it is an array, elements of the array MUST be strings and MUST be unique.
  // String values MUST be one of the seven primitive types defined by the core specification.
  sealed trait JsonSchemaType

  type ArrayJsonSchemaType = ManyUnique[String] with JsonSchemaType
  /**
   *
   *
   * @param name
   * @param description
   * @param externalDocs
   */
  case class Tag(
    @JsonProperty(required = true) name: String,
    description: String,
    externalDocs: ExternalDocs
  ) extends VendorExtensions with API

  /**
   * Information about external documentation
   *
   * @param url
   * @param description
   */
  case class ExternalDocs(
    @JsonProperty(required = true) url: URI,
    description: String
  ) extends VendorExtensions with API with UriChecker


  /**
   *
   * General information about the API.
   *
   * @param title           A unique and precise title of the API.
   * @param version         A semantic version number of the API.
   * @param description     A longer description of the API. Should be different from the title.  GitHub Flavored Markdown is allowed.
   * @param termsOfService  The terms of service for the API.
   * @param contact
   * @param license
   */
  case class Info(
    @JsonProperty(required = true) title: String,
    @JsonProperty(required = true) version: String,
    description:          String,
    termsOfService:       String,
    contact:              Contact,
    license:              License
  ) extends VendorExtensions with API

  /**
   * Contact information for the owners of the API.
   *
   * @param name         The identifying name of the contact person/organization.
   * @param url          The URL pointing to the contact information.
   * @param email        The email address of the contact person/organization.
   */
  case class Contact(
    name:                 String,
    url:                  URI,
    email:                Email
  ) extends VendorExtensions with API with UriChecker with EmailChecker

  /**
   *
   * @param name          The name of the license type. It's encouraged to use an OSI compatible license.
   * @param url           The URL pointing to the license.
   */
  case class License(
    name:                 String,
    url:                  URI
  ) extends API with UriChecker

  /**
   *
   * @param get
   * @param put
   * @param post
   * @param delete
   * @param options
   * @param head
   * @param patch
   * @param parameters      The parameters needed to send a valid API call.
   * @param $ref
   */
  case class PathItem(
    get:                  Operation,
    put:                  Operation,
    post:                 Operation,
    delete:               Operation,
    options:              Operation,
    head:                 Operation,
    patch:                Operation,
    parameters:           ParametersList,   // parameter list which is valid for all operations (can be overridden)
    @JsonProperty("$ref") $ref: Ref         // TODO $ref is currently not supported
  ) extends VendorExtensions with API with RefChecker {
    def param(name: String, op: Operation) = Option(op) map { name -> _.parameters } toList
    def params(prefix: String) = (("" -> parameters) :: param("get", get) :::
      param("get", get) ::: param("put", put) ::: param("post", post) :::
      param("delete", delete) ::: param("options", options) ::: param("head", head) :::
      param("patch", patch)) flatMap { p =>
        Option(p._2).toSeq.flatten map { pl => (prefix + p._1) -> pl }
      }
    def operationNames: Set[String] =
      getClass.getDeclaredFields.
      filter(_.getType == classOf[Operation]).
      filter { f => f.setAccessible(true); f.get(this) != null }.
        map(_.getName).toSet
    def operation(name: String): Operation =
      getClass.getDeclaredField(name).get(this).asInstanceOf[Operation]
  }


  sealed trait ParametersListItem

  case class JsonReference(
    @JsonProperty(value = "$ref", required = true) $ref: Ref
  ) extends ParametersListItem with ResponseValue with RefChecker

  sealed trait Parameter[T] extends ParametersListItem {
    def name: String
    def in: String
  }

  abstract class NonBodyParameter[T] extends Parameter[T] with ValidationBase {
    @JsonProperty(required = true) def name:    String
    @JsonProperty(required = true) def in:      String
    @JsonScalaEnumeration(classOf[ParameterTypeReference]) @JsonProperty(required = true) def `type`: ParameterType.Value
  }

  trait NonBodyParameterCommons[T, CF] extends ValidationBase {
    def `type`: ParameterType.Value
    def items: PrimitivesItems[T]
    def default: Default[T]
    def format: String
    def isArray = `type`== ParameterType.ARRAY
    def inPath = in == "path"
    def required: Boolean
    def collectionFormat: CF
    def name: String
    def in: String
    if (collectionFormat != null) require(isArray)
    if (default != null) require(!required)
    if (isArray) require(items != null)
  }

  /**
   *
   * @param name          The name of the parameter.
   * @param in            Determines the location of the parameter.
   * @param schema
   * @param description   A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param required      Determines whether or not this parameter is required or optional.
   */
  case class BodyParameter[T](
    @JsonProperty(required = true) name:    String,
    @JsonProperty(required = true) in:      String,
    @JsonProperty(required = true) schema:  SchemaOrFileSchema[T],
    description:                            String,
    required:                               Boolean = false
  ) extends Parameter[T] with VendorExtensions with ValidationBase

  /**
   *
   * @param required          Determines whether or not this parameter is required or optional.
   * @param name              The name of the parameter.
   * @param in                Determines the location of the parameter.
   * @param description       A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   */
  case class PathParameter[T](
    @JsonProperty(required = true) required: Boolean = true,
    name:                   String,
    in:                     String,
    description:            String,
    @JsonScalaEnumeration(classOf[ParameterTypeReference]) `type`: ParameterType.Value,
    format:                 String,
    items:                  PrimitivesItems[T],
    @JsonScalaEnumeration(classOf[CollectionFormatReference]) collectionFormat: CollectionFormat.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf
  ) extends NonBodyParameter[T] with VendorExtensions with AllValidations[T] with NonBodyParameterCommons[T, CollectionFormat.Value]

  /**
   *
   * @param required          Determines whether or not this parameter is required or optional.
   * @param name              The name of the parameter.
   * @param in                Determines the location of the parameter.
   * @param description       A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   * @param allowEmptyValue allows sending a parameter by name only or with an empty value.
   */
  case class FormDataParameter[T](
    required:               Boolean = false,
    name:                   String,
    in:                     String,
    description:            String,
    @JsonScalaEnumeration(classOf[ParameterTypeReference]) `type`: ParameterType.Value,
    format:                 String,
    items:                  PrimitivesItems[T],
    @JsonScalaEnumeration(classOf[CollectionFormatReference]) collectionFormat: CollectionFormat.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf,
    allowEmptyValue:        Boolean = false // unique for form
  ) extends NonBodyParameter[T] with VendorExtensions with AllValidations[T] with NonBodyParameterCommons[T, CollectionFormat.Value]

  /**
   *
   * @param required          Determines whether or not this parameter is required or optional.
   * @param name              The name of the parameter.
   * @param in                Determines the location of the parameter.
   * @param description       A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   * @param allowEmptyValue allows sending a parameter by name only or with an empty value.
   */
  case class QueryParameter[T](
    required:               Boolean = false,
    name:                   String,
    in:                     String,
    description:            String,
    @JsonScalaEnumeration(classOf[ParameterTypeReference]) `type`: ParameterType.Value,
    format:                 String,
    items:                  PrimitivesItems[T],
    @JsonScalaEnumeration(classOf[CollectionFormatWithMultiReference]) collectionFormat: CollectionFormatWithMulti.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf,
    allowEmptyValue:        Boolean = false // unique for query and form
  ) extends NonBodyParameter[T] with VendorExtensions with AllValidations[T] with NonBodyParameterCommons[T, CollectionFormatWithMulti.Value]

  /**
   *
   * @param required          Determines whether or not this parameter is required or optional.
   * @param name              The name of the parameter.
   * @param in                Determines the location of the parameter.
   * @param description       A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   */
  case class HeaderParameter[T](
    required:               Boolean = false,
    name:                   String,
    in:                     String,
    description:            String,
    @JsonScalaEnumeration(classOf[ParameterTypeReference]) `type`: ParameterType.Value,
    format:                 String,
    items:                  PrimitivesItems[T],
    @JsonScalaEnumeration(classOf[CollectionFormatReference]) collectionFormat: CollectionFormat.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf
  ) extends NonBodyParameter[T] with VendorExtensions with AllValidations[T] with NonBodyParameterCommons[T, CollectionFormat.Value]

  sealed trait ResponseValue

  case class Response[T](
    @JsonProperty(required = true) description: String,
    schema: SchemaOrFileSchema[T],
    headers: Headers,
    examples: Examples
  ) extends ResponseValue with VendorExtensions


  /**
   *
   * @param responses     Response objects names can either be any valid HTTP status code or 'default'.
   * @param tags
   * @param summary       A brief summary of the operation.
   * @param description   A longer description of the operation, GitHub Flavored Markdown is allowed.
   * @param externalDocs
   * @param operationId   A unique identifier of the operation.
   * @param consumes      A list of MIME types the API can consume.
   * @param produces      A list of MIME types the API can produce.
   * @param parameters
   * @param schemes
   * @param deprecated
   * @param security
   */
  case class Operation(
    @JsonProperty(required = true) responses: Responses,
    tags:                 SimpleTags,
    summary:              String,
    description:          String,
    externalDocs:         ExternalDocs,
    operationId:          String,
    consumes:             MediaTypeList,
    produces:             MediaTypeList,
    parameters:           ParametersList,
    @JsonScalaEnumeration(classOf[SchemeType]) schemes: SchemesList,
    deprecated:           Boolean = false,
    security:             Security
  ) extends VendorExtensions with API with PatternChecker {
    require(responses.nonEmpty)
    require(responses.keySet.forall(matches("^([0-9]{3})$|^(default)$", _)))
  }


  /**
   *
   * @param description       A brief description of the parameter. This could contain examples of use.  GitHub Flavored Markdown is allowed.
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   */
  case class Header[T](
    @JsonProperty(required = true) @JsonScalaEnumeration(classOf[PrimitiveTypeReference]) `type`: PrimitiveType.Value,
    description:            String,
    format:                 String,
    items:                  PrimitivesItems[T],
    @JsonScalaEnumeration(classOf[CollectionFormatReference]) collectionFormat: CollectionFormat.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf
  ) extends VendorExtensions with AllValidations[T]

  /**
   * A deterministic version of a JSON Schema object.
   *
   * @param `type`
   * @param format
   * @param title
   * @param description
   * @param default
   * @param required
   * @param readOnly
   * @param externalDocs
   * @param example
   */
  case class FileSchema[T](
    @JsonProperty(required = true) `type`: String,
    format:                 String,
    title:                  Title,
    description:            Description,
    default:                Default[T],
    required:               StringArray,
    readOnly:               Boolean = false,
    externalDocs:           ExternalDocs,
    example:                Example[T]
  ) extends VendorExtensions

  class Schema[T](
    val format:                 String,
    val title:                  Title,
    val description:            Description,
    val default:                Default[T],
    val maximum:                Maximum,
    val exclusiveMaximum:       ExclusiveMaximum,
    val minimum:                Minimum,
    val exclusiveMinimum:       ExclusiveMinimum,
    val maxLength:              MaxLength,
    val minLength:              MinLength,
    val pattern:                Pattern,
    val maxItems:               MaxItems,
    val minItems:               MinItems,
    val uniqueItems:            UniqueItems,
    val enum:                   Enum[T],
    val multipleOf:             MultipleOf,
    val maxProperties:          MaxProperties,
    val minProperties:          MinProperties,
    val required:               StringArray,
    val additionalProperties:   SchemaOrBoolean[T],
    val `type`:                 JsonSchemaType,
    val properties:             SchemaProperties,
    val allOf:                  Option[SchemaArray],
    val items:                  Option[SchemaOrSchemaArray[_]],
    val discriminator:          String,
    val xml:                    Xml,
    val readOnly:               Boolean = false,
    val externalDocs:           ExternalDocs,
    val example:                Example[T]
  ) extends VendorExtensions with AllValidations[T] with ObjectValidation {
    require(allOf.forall(_.nonEmpty))
    validateSchemaArray(items)
    validateSchemaArray(allOf)
    private def validateSchemaArray(a: Option[_]) =
      a.isEmpty || a.get.isInstanceOf[SchemaOrFileSchema[_]] ||
        (a.get.isInstanceOf[SchemaArray] && a.get.asInstanceOf[SchemaArray].nonEmpty)
  }

  /**
   *
   * @param `type`
   * @param format
   * @param items
   * @param collectionFormat
   * @param default
   * @param maximum
   * @param exclusiveMaximum
   * @param minimum
   * @param exclusiveMinimum
   * @param maxLength
   * @param minLength
   * @param pattern
   * @param maxItems
   * @param minItems
   * @param uniqueItems
   * @param enum
   * @param multipleOf
   */
  case class PrimitivesItems[T](
    @JsonProperty(required = true) @JsonScalaEnumeration(classOf[PrimitiveTypeReference]) `type`: PrimitiveType.Val,
    format:                 String,
    items:                  PrimitivesItems[T],
    collectionFormat:       CollectionFormat.Value,
    default:                Default[T],
    maximum:                Maximum,
    exclusiveMaximum:       ExclusiveMaximum,
    minimum:                Minimum,
    exclusiveMinimum:       ExclusiveMinimum,
    maxLength:              MaxLength,
    minLength:              MinLength,
    pattern:                Pattern,
    maxItems:               MaxItems,
    minItems:               MinItems,
    uniqueItems:            UniqueItems,
    enum:                   Enum[T],
    multipleOf:             MultipleOf
  ) extends VendorExtensions with AllValidations[T] {
    require(`type` != PrimitiveType.NULL)
    require(`type` != PrimitiveType.OBJECT)
    if (isArray) require(items != null)
    if (collectionFormat != null) require(isArray)
    def isArray = `type` == PrimitiveType.ARRAY
  }

  /**
   *
   * @param name
   * @param namespace
   * @param prefix
   * @param attribute
   * @param wrapped
   */
  case class Xml(
    name:                   String,
    namespace:              String,
    prefix:                 String,
    attribute:              Boolean = false,
    wrapped:                Boolean = false
  ) extends API with VendorExtensions

  /********* security definitions *********/
  sealed trait SecurityDefinition extends VendorExtensions

  case class BasicAuthenticationSecurity(
    @JsonProperty(required = true) `type`: String, // "enum": basic
    description: Description
  ) extends SecurityDefinition

  case class ApiKeySecurity(
    @JsonProperty(required = true) `type`: String,  // "enum": apiKey
    @JsonProperty(required = true) name: String,
    @JsonProperty(required = true) in: String,      // "enum": header, query
    description: Description
  ) extends SecurityDefinition

  case class Oauth2ImplicitSecurity(
    @JsonProperty(required = true) `type`: String,  // "enum": oauth2
    @JsonProperty(required = true) flow: String,    // "enum": implicit
    @JsonProperty(required = true) authorizationUrl: URI,
    scopes: Oauth2Scopes,
    description: Description
  ) extends SecurityDefinition with UriChecker {
    val url = authorizationUrl
  }

  case class Oauth2PasswordSecurity(
    @JsonProperty(required = true) `type`: String,  // "enum": oauth2
    @JsonProperty(required = true) flow: String,    // "enum": password
    @JsonProperty(required = true) tokenUrl: URI,
    scopes: Oauth2Scopes,
    description: Description
  ) extends SecurityDefinition with UriChecker {
    val url = tokenUrl
  }

  case class Oauth2ApplicationSecurity(
    @JsonProperty(required = true) `type`: String,  // "enum": oauth2
    @JsonProperty(required = true) flow: String,    // "enum": application
    @JsonProperty(required = true) tokenUrl: URI,
    scopes: Oauth2Scopes,
    description: Description
  ) extends SecurityDefinition with UriChecker {
    val url = tokenUrl
  }

  case class Oauth2AccessCodeSecurity(
    @JsonProperty(required = true) `type`: String,  // "enum": oauth2
    @JsonProperty(required = true) flow: String,    // "enum": accessCode
    @JsonProperty(required = true) authorizationUrl: URI,
    @JsonProperty(required = true) tokenUrl: URI,
    scopes: Oauth2Scopes,
    description: Description
  ) extends SecurityDefinition with UriChecker {
    val url = tokenUrl
  }

  trait VendorExtensions { self =>
    private[this] val extensions = new mutable.HashMap[String, String]
    private[this] val errorMappings = new mutable.HashMap[String, Seq[Class[Exception]]]

    @JsonAnySetter
    def handleUnknown(key: String, value: Any) {
      value match {
        case str: String if key.startsWith("x-") =>
          extensions += key -> str
        case mapping: Map[_, _] if key.startsWith("x-") =>
          import scala.util.control.Exception._
          handling(classOf[ClassNotFoundException]) by { e =>
            throw new IllegalArgumentException(s"Could not find exception class $e for error code")
          } apply {
            val errors = mapping filter { _._2.isInstanceOf[Array[_]] } map { case (k, l) =>
              k.toString -> l.asInstanceOf[Array[_]].map { e =>
                Class.forName(e.toString).asInstanceOf[Class[Exception]]
              }.toSeq
            }
            errorMappings ++= errors
          }
        case _ =>
          throw new UnrecognizedPropertyException(
            s"Unknown property: $key",
            null,
            self.getClass,
            key,
            null
          )
      }
    }
    lazy val vendorExtensions = extensions.toMap
    lazy val vendorErrorMappings = errorMappings.toMap
  }

  type Many[T]                = List[T]
  type ManyUnique[T]          = Set[T]
  type AdditionalProperties[T]= Map[String, T]
  type Default[T]             = T
  type Example[T]             = T

  type Host                   = String
  type BasePath               = String
  type Format                 = String
  type SimpleTag              = String
  type URI                    = String
  type Email                  = String
  type Ref                    = String
  type MimeType               = String // The MIME type of the HTTP message.
  type Title                  = String
  type Description            = String

  type SchemesList            = ManyUnique[Scheme.Value]
  type MediaTypeList          = ManyUnique[MimeType]
  type Tags                   = ManyUnique[Tag]
  type Security               = ManyUnique[SecurityRequirement]
  type ParametersList         = ManyUnique[ParametersListItem]
  type SimpleTags             = ManyUnique[SimpleTag]
  type StringArray            = Many[String]
  type SchemaArray            = Many[SchemaOrFileSchema[_]]

  type ParameterDefinitions   = AdditionalProperties[Parameter[_]]
  type ResponseDefinitions    = AdditionalProperties[Response[_]]
  type Definitions            = AdditionalProperties[Schema[_]]
  type SecurityRequirement    = AdditionalProperties[ManyUnique[String]]
  type SecurityDefinitions    = AdditionalProperties[SecurityDefinition]
  type Headers                = AdditionalProperties[Header[_]]
  type Responses              = AdditionalProperties[Response[_]]
  type Examples               = AdditionalProperties[Any]
  type SchemaProperties       = AdditionalProperties[SchemaOrFileSchema[_]]
  type Paths                  = AdditionalProperties[PathItem] with VendorExtensions
  type Oauth2Scopes           = AdditionalProperties[String]

  type SchemaOrReference[T]   = Either[Schema[T], JsonReference]
  type SchemaOrBoolean[T]     = Either[SchemaOrReference[T], Boolean]
  type SchemaOrFileSchema[T]  = Either[SchemaOrReference[T], FileSchema[T]]
  type SchemaOrSchemaArray[T] = Either[SchemaOrReference[T], SchemaArray]

  // -------------------------- Validations --------------------------

  sealed trait ValidationBase
  /**
   * validations for Int, Long, Double, Float
   */
  object NumberValidation {
    type MultipleOf             = Option[Double]
    type Maximum                = Option[Double]
    type ExclusiveMaximum       = Option[Boolean]
    type Minimum                = Option[Double]
    type ExclusiveMinimum       = Option[Boolean]
  }
  trait NumberValidation extends ValidationBase {
    import NumberValidation._
    def multipleOf:             MultipleOf
    def maximum:                Maximum
    def exclusiveMaximum:       ExclusiveMaximum
    def minimum:                Minimum
    def exclusiveMinimum:       ExclusiveMinimum
    require(multipleOf.forall(_ > 0))
    require(exclusiveMaximum.isEmpty || maximum.isDefined)
    require(exclusiveMinimum.isEmpty || minimum.isDefined)
  }

  object StringValidation {
    type MaxLength              = Option[Int] // length <=
    type MinLength              = Option[Int] // length >=
    type Pattern                = Option[String]
  }
  trait StringValidation extends ValidationBase {
    import StringValidation._
    def maxLength: MaxLength
    def minLength: MinLength
    def pattern: Pattern
    require(maxLength.forall(_>=0))
    require(minLength.forall(_>=0))
    require(pattern.forall(p => Try(p.r).isSuccess))
  }

  object ArrayValidation {
    type MaxItems               = Option[Int]
    type MinItems               = Option[Int]
    type UniqueItems            = Option[Boolean]
    type Enum[T]                = Option[ManyUnique[T]]
  }
  trait ArrayValidation[T] extends ValidationBase {
    import ArrayValidation._
    def maxItems:               MaxItems
    def minItems:               MinItems
    def uniqueItems:            UniqueItems
    def enum:                   Enum[T]
    require(maxItems.forall(_>=0))
    require(minItems.forall(_>=0))
  }
  object ObjectValidation {
    type MaxProperties          = Option[Int]
    type MinProperties          = Option[Int]
  }
  trait ObjectValidation extends ValidationBase {
    import ObjectValidation._
    def maxProperties:          MaxProperties
    def minProperties:          MinProperties
    require(maxProperties.forall(_>=0))
    require(minProperties.forall(_>=0))
  }

  trait AllValidations[T] extends NumberValidation with StringValidation with ArrayValidation[T]

}
