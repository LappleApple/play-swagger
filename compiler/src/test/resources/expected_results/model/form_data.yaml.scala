package form_data

package object yaml {

    import java.io.File

    import de.zalando.play.controllers.PlayPathBindables

    import PlayPathBindables.queryBindableFile



    type MultipartPostAvatar = Option[File]
    type BothPostResponses200Name = Option[String]
    type MultipartPostName = String
    type BothPostRingtone = File
    type BothPostYear = Option[Int]


    case class MultipartPostResponses200(name: BothPostResponses200Name, year: BothPostYear, fileSize: BothPostYear, fileName: BothPostResponses200Name) 
    case class BothPostResponses200(name: BothPostResponses200Name, year: BothPostYear, avatarSize: BothPostYear, ringtoneSize: BothPostYear) 

    implicit val bindable_OptionFileQuery = PlayPathBindables.createOptionQueryBindable[File]
    
    implicit val bindable_OptionIntQuery = PlayPathBindables.createOptionQueryBindable[Int]

}