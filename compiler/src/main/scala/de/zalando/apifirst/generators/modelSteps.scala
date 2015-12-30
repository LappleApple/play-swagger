package de.zalando.apifirst.generators

import de.zalando.apifirst.Domain._
import de.zalando.apifirst.ScalaName._
import de.zalando.apifirst.naming.Reference
import de.zalando.apifirst.generators.DenotationNames._

/**
  * @author slasch 
  * @since 30.12.2015.
  */

trait ClassesStep extends EnrichmentStep[Type] {

  override def steps = classes +: super.steps

  /**
    * Puts class related information into the denotation table
    *
    * @return
    */
  protected def classes: SingleStep = typeDef => table => typeDef match {
    case (ref, t: TypeDef) if !ref.simple.contains("AllOf") && !ref.simple.contains("OneOf") =>
      val traitName = app.discriminators.get(ref).map(_ => Map("name" -> typeNameDenotation(table, ref)))
      Map("classes" -> (typeDefProps(ref, t)(table) + ("trait" -> traitName)))
    case (ref, t: Composite) =>
      Map("classes" -> (typeDefProps(ref, t)(table) + ("trait" -> t.root.map(r => Map("name" -> r.className)))))
    case _ => empty
  }

  protected def typeDefProps(k: Reference, t: Type)(table: DenotationTable): Map[String, Any] = {
    Map(
      "name" -> typeNameDenotation(table, k),
      "fields" -> typeFields(table, k).map { f =>
        Map(
          "name" -> escape(f.name.simple),
          "type_name" -> typeNameDenotation(table, f.tpe.name)
        )
      }
    )
  }


}

trait TraitsStep extends EnrichmentStep[Type] {

  override def steps = traits +: super.steps

  /**
    * Puts trait related information into the denotation table
    *
    * @return
    */
  protected def traits: SingleStep = typeDef => table => typeDef match {
    case (ref, t: TypeDef) if app.discriminators.contains(ref) =>
      Map("traits" -> typeDefProps(ref, t)(table))
    case _ => empty
  }

  protected def typeDefProps(k: Reference, t: Type)(table: DenotationTable): Map[String, Any] // FIXME should be defined only once
}

trait AliasesStep extends EnrichmentStep[Type] {

  override def steps = aliases +: super.steps

  /**
    * Puts type related information into the denotation table
    *
    * @return
    */
  protected val aliases: SingleStep = typeDef => table => typeDef match {
    case (ref, t: Container) =>
      Map("aliases" -> aliasProps(ref, t)(table))
    case (k, v: PrimitiveType) =>
      Map("aliases" -> mapForAlias(k, v)(table))
    case (k, v: TypeRef) =>
      Map("aliases" -> mapForAlias(v.name, v)(table))
    case _ => empty
  }

  private def aliasProps(k: Reference, v: Container)(table: DenotationTable): Map[String, Any] = {
    Map(
      "name" -> typeNameDenotation(table, k),
      "alias" -> v.alias,
      "underlying_type" -> v.imports.headOption.map { _ => v.nestedTypes.map { t =>
        typeNameDenotation(table, t.name)
      }.mkString(", ")
      }
    )
  }

  private def mapForAlias(k: Reference, v: Type)(table: DenotationTable): Map[String, Object] = {
    Map(
      "name" -> memberNameDenotation(table, k),
      "alias" -> typeNameDenotation(table, v.name),
      "underlying_type" -> None
    )
  }
}