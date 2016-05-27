package de.zalando.model

import de.zalando.apifirst.Application._
import de.zalando.apifirst.Domain._
import de.zalando.apifirst.Http._
import de.zalando.apifirst.Hypermedia._
import de.zalando.apifirst.{ParameterPlace, Security}
import de.zalando.apifirst.naming._
//noinspection ScalaStyle
object nakadi_yaml extends WithModel {
 
 val types = Map[Reference, Type](
	Reference("⌿definitions⌿EventMetaData") → 
		TypeDef(Reference("⌿definitions⌿EventMetaData"), 
			Seq(
					Field(Reference("⌿definitions⌿EventMetaData⌿root_id"), TypeRef(Reference("⌿definitions⌿Event⌿event_type"))),
					Field(Reference("⌿definitions⌿EventMetaData⌿parent_id"), TypeRef(Reference("⌿definitions⌿Event⌿event_type"))),
					Field(Reference("⌿definitions⌿EventMetaData⌿scopes"), TypeRef(Reference("⌿definitions⌿EventMetaData⌿scopes"))),
					Field(Reference("⌿definitions⌿EventMetaData⌿id"), TypeRef(Reference("⌿definitions⌿Event⌿event_type"))),
					Field(Reference("⌿definitions⌿EventMetaData⌿created"), TypeRef(Reference("⌿definitions⌿Event⌿event_type")))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Topic") → 
		TypeDef(Reference("⌿definitions⌿Topic"), 
			Seq(
					Field(Reference("⌿definitions⌿Topic⌿name"), Str(None, new TypeMeta(None, List())))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Metrics") → 
		TypeDef(Reference("⌿definitions⌿Metrics"), 
			Seq(
					Field(Reference("⌿definitions⌿Metrics⌿name"), TypeRef(Reference("⌿definitions⌿Event⌿event_type")))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Event") → 
		TypeDef(Reference("⌿definitions⌿Event"), 
			Seq(
					Field(Reference("⌿definitions⌿Event⌿event_type"), TypeRef(Reference("⌿definitions⌿Event⌿event_type"))),
					Field(Reference("⌿definitions⌿Event⌿partitioning_key"), TypeRef(Reference("⌿definitions⌿Event⌿event_type"))),
					Field(Reference("⌿definitions⌿Event⌿metadata"), TypeRef(Reference("⌿definitions⌿Event⌿metadata")))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Cursor") → 
		TypeDef(Reference("⌿definitions⌿Cursor"), 
			Seq(
					Field(Reference("⌿definitions⌿Cursor⌿partition"), Str(None, new TypeMeta(None, List()))),
					Field(Reference("⌿definitions⌿Cursor⌿offset"), Str(None, new TypeMeta(None, List())))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Problem") → 
		TypeDef(Reference("⌿definitions⌿Problem"), 
			Seq(
					Field(Reference("⌿definitions⌿Problem⌿detail"), Str(None, new TypeMeta(None, List())))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿TopicPartition") → 
		TypeDef(Reference("⌿definitions⌿TopicPartition"), 
			Seq(
					Field(Reference("⌿definitions⌿TopicPartition⌿partition"), Str(None, new TypeMeta(None, List()))),
					Field(Reference("⌿definitions⌿TopicPartition⌿oldest_available_offset"), Str(None, new TypeMeta(None, List()))),
					Field(Reference("⌿definitions⌿TopicPartition⌿newest_available_offset"), Str(None, new TypeMeta(None, List())))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿SimpleStreamEvent") → 
		TypeDef(Reference("⌿definitions⌿SimpleStreamEvent"), 
			Seq(
					Field(Reference("⌿definitions⌿SimpleStreamEvent⌿cursor"), TypeRef(Reference("⌿definitions⌿Cursor"))),
					Field(Reference("⌿definitions⌿SimpleStreamEvent⌿events"), TypeRef(Reference("⌿definitions⌿SimpleStreamEvent⌿events")))
			), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Event⌿event_type") → 
		Opt(Str(None, new TypeMeta(None, List())), new TypeMeta(None, List())),
	Reference("⌿definitions⌿Event⌿metadata") → 
		Opt(TypeRef(Reference("⌿definitions⌿EventMetaData")), new TypeMeta(None, List())),
	Reference("⌿definitions⌿SimpleStreamEvent⌿events") → 
		Opt(TypeRef(Reference("⌿definitions⌿SimpleStreamEvent⌿events⌿Opt")), new TypeMeta(None, List())),
	Reference("⌿definitions⌿EventMetaData⌿scopes") → 
		Opt(TypeRef(Reference("⌿definitions⌿EventMetaData⌿scopes⌿Opt")), new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿topic") → 
		Str(None, new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout") → 
		Opt(Intgr(new TypeMeta(None, List())), new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_limit") → 
		Intgr(new TypeMeta(None, List())),
	Reference("⌿definitions⌿SimpleStreamEvent⌿events⌿Opt") → 
		Arr(TypeRef(Reference("⌿definitions⌿Event")), new TypeMeta(None, List()), "csv"),
	Reference("⌿definitions⌿EventMetaData⌿scopes⌿Opt") → 
		Arr(Str(None, new TypeMeta(None, List())), new TypeMeta(None, List()), "csv"),
	Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿event") → 
		Opt(TypeRef(Reference("⌿definitions⌿Event")), new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics/{topic}/events⌿post⌿responses⌿201") → 
		Null(new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics/{topic}/partitions⌿get⌿responses⌿200") → 
		ArrResult(TypeRef(Reference("⌿definitions⌿TopicPartition")), new TypeMeta(None, List())),
	Reference("⌿paths⌿/topics⌿get⌿responses⌿200") → 
		ArrResult(TypeRef(Reference("⌿definitions⌿Topic")), new TypeMeta(None, List()))
) 
 
 val parameters = Map[ParameterRef, Parameter](
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿start_from")) → Parameter("start_from", Str(None, new TypeMeta(None, List())), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions⌿get⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")) → Parameter("stream_timeout", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_limit")) → Parameter("stream_limit", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_flush_timeout")) → Parameter("batch_flush_timeout", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}⌿get⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿x_nakadi_cursors")) → Parameter("x_nakadi_cursors", Str(None, new TypeMeta(None, List())), None, None, ".+", encode = false, ParameterPlace.withName("header")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_limit")) → Parameter("batch_limit", Intgr(new TypeMeta(None, List())), None, Some("1"), ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿partition")) → Parameter("partition", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿stream_limit")) → Parameter("stream_limit", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}⌿get⌿partition")) → Parameter("partition", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_keep_alive_limit")) → Parameter("batch_keep_alive_limit", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿post⌿event")) → Parameter("event", TypeRef(Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿event")), None, None, ".+", encode = false, ParameterPlace.withName("body")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿event")) → Parameter("event", TypeRef(Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿event")), None, None, ".+", encode = false, ParameterPlace.withName("body")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_limit")) → Parameter("batch_limit", Intgr(new TypeMeta(None, List())), None, Some("1"), ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿post⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_flush_timeout")) → Parameter("batch_flush_timeout", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/events⌿get⌿topic")) → Parameter("topic", Str(None, new TypeMeta(None, List())), None, None, "[^/]+", encode = true, ParameterPlace.withName("path")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿stream_timeout")) → Parameter("stream_timeout", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query")),
	ParameterRef(	Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_keep_alive_limit")) → Parameter("batch_keep_alive_limit", TypeRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")), None, None, ".+", encode = true, ParameterPlace.withName("query"))
) 
val basePath: String =null
val discriminators: DiscriminatorLookupTable = Map[Reference, Reference](
	)
val securityDefinitions: SecurityDefinitionsTable = Map[String, Security.Definition](
	
)
val stateTransitions: StateTransitionsTable = Map[State, Map[State, TransitionProperties]]()
val calls: Seq[ApiCall] = Seq(
	ApiCall(GET, Path(Reference("⌿metrics")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_metrics",parameters = 
			Seq(

				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			503 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			200 -> ParameterRef(Reference("⌿definitions⌿Metrics"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					200 -> Self,
					401 -> Self,
					503 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(GET, Path(Reference("⌿topics⌿{topic}⌿partitions⌿{partition}⌿events")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_events_from_single_partition",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿start_from")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿partition")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿stream_limit")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿topic")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_limit")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_flush_timeout")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿stream_timeout")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}/events⌿get⌿batch_keep_alive_limit"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			500 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			404 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			400 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			200 -> ParameterRef(Reference("⌿definitions⌿SimpleStreamEvent"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					500 -> Self,
					404 -> Self,
					401 -> Self,
					400 -> Self,
					200 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(GET, Path(Reference("⌿topics⌿{topic}⌿partitions⌿{partition}")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_partition",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}⌿get⌿topic")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions/{partition}⌿get⌿partition"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			200 -> ParameterRef(Reference("⌿definitions⌿TopicPartition"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					200 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(GET, Path(Reference("⌿topics")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_topics",parameters = 
			Seq(

				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			200 -> ParameterRef(Reference("⌿paths⌿/topics⌿get⌿responses⌿200")),
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			503 -> ParameterRef(Reference("⌿definitions⌿Problem"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					200 -> Self,
					401 -> Self,
					503 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(GET, Path(Reference("⌿topics⌿{topic}⌿events")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_events_from_multiple_partitions",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_timeout")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿stream_limit")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_flush_timeout")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿x_nakadi_cursors")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_limit")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿batch_keep_alive_limit")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿get⌿topic"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			500 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			404 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			400 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			200 -> ParameterRef(Reference("⌿definitions⌿SimpleStreamEvent"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					500 -> Self,
					404 -> Self,
					401 -> Self,
					400 -> Self,
					200 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(POST, Path(Reference("⌿topics⌿{topic}⌿events")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackPost_event",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿post⌿topic")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿post⌿event"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			201 -> ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿post⌿responses⌿201")),
			403 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			503 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			422 -> ParameterRef(Reference("⌿definitions⌿Problem"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					201 -> Self,
					403 -> Self,
					503 -> Self,
					401 -> Self,
					422 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(GET, Path(Reference("⌿topics⌿{topic}⌿partitions")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackGet_partitions",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions⌿get⌿topic"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			200 -> ParameterRef(Reference("⌿paths⌿/topics/{topic}/partitions⌿get⌿responses⌿200"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					200 -> Self
			), None), 
		Set.empty[Security.Constraint]), 
	ApiCall(POST, Path(Reference("⌿topics⌿{topic}⌿events⌿batch")), 
		HandlerCall(
			"nakadi.yaml",
			"NakadiYaml",
			instantiate = false,
			"nakadiHackPost_events",parameters = 
			Seq(
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿topic")),
				ParameterRef(Reference("⌿paths⌿/topics/{topic}/events/batch⌿post⌿event"))
				)
			), 
		Set(MimeType("application/json")), 
		Set(MimeType("application/json")), 
		Map.empty[String, Seq[Class[Exception]]], 
		TypesResponseInfo(
			Map[Int, ParameterRef](
			201 -> ParameterRef(Reference("⌿paths⌿/topics/{topic}/events⌿post⌿responses⌿201")),
			403 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			503 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			401 -> ParameterRef(Reference("⌿definitions⌿Problem")),
			422 -> ParameterRef(Reference("⌿definitions⌿Problem"))
		), None), 
		StateResponseInfo(
				Map[Int, State](
					201 -> Self,
					403 -> Self,
					503 -> Self,
					401 -> Self,
					422 -> Self
			), None), 
		Set.empty[Security.Constraint]))

val packageName: Option[String] = Some("nakadi.yaml")

val model = new StrictModel(calls, types, parameters, discriminators, basePath, packageName, stateTransitions, securityDefinitions)
    
} 