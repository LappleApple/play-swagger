package simple.petstore.api.yaml
import play.api.mvc.{Action, Controller}
import play.api.data.validation.Constraint
import de.zalando.play.controllers._
import PlayBodyParsing._
import PlayValidations._

object definitionsAction {
    import pathsBase._
    import definitions._
    import definitionsValidator._
    }
object pathsAction {
    import pathsBase._
    import definitions.{ErrorModel, NewPet, Pet}
    import paths._
    import pathsValidator._
    class SimplePetstoreApiYaml extends SimplePetstoreApiYamlBase {
        val addPet = addPetAction {
            (pet: NewPet) =>
            ???
        }
        }
    class Dashboard extends DashboardBase {
        val methodLevel = methodLevelAction {
            input: (PetsGetTags, PetsGetLimit) =>
            val (tags, limit) = input
            
            ???
        }
        val pathLevelGet = pathLevelGetAction {
            (id: Long) =>
            ???
        }
        val pathLevelDelete = pathLevelDeleteAction {
            (id: Long) =>
            ???
        }
        }
    }
