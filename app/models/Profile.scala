package models

import java.io.StringWriter

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.SocialProfile

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFLanguages
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDF

case class Profile (
    loginInfo : LoginInfo,
    name : String,
    picture : Option[String],
    title : Option[String],
    summary : Option[String],
    location : Option[String],
    email : Option[String],
    website : Option[String],
    twitterUrl : Option[String],
    gitHubUrl : Option[String],
    gitHubUsername : Option[String],
    projects : List[Project],
    workExperience : List[WorkExperience]) extends SocialProfile {

  def toModel : Model = {
    val webIdUri = "http://" + loginInfo.providerKey + ".solid.vip/#i"
    val NS = "http://schema.org/"

    val profile = ModelFactory.createDefaultModel();

    val NAMESPACE = profile.createResource(NS)
    val PERSON = profile.createResource(NS + "Person")
    val PLACE = profile.createResource(NS + "Place")
    val WEBSITE = profile.createResource(NS + "WebSite")
    val SOFTWARE_SOURCE_CODE = profile.createResource(NS + "SoftwareSourceCode")
    val ORGANIZATION = profile.createResource(NS + "Organization")

    val NAME = profile.createProperty(NS + "name")
    val JOB_TITLE = profile.createProperty(NS + "jobTitle")
    val DESCRIPTION = profile.createProperty(NS + "description")
    val HOME_LOCATION = profile.createProperty(NS + "homeLocation")
    val EMAIL = profile.createProperty(NS + "email")
    val AUTHOR = profile.createProperty(NS + "author")
    val URL = profile.createProperty(NS + "url")
    val SAME_AS = profile.createProperty(NS + "sameAs")
    val IMAGE = profile.createProperty(NS + "image")
    val CONTRIBUTOR = profile.createProperty(NS + "contributor")
    val WORKS_FOR = profile.createProperty(NS + "worksFor")
    val ABOUT = profile.createProperty(NS + "about")
    val SUBJECT_OF = profile.createProperty(NS + "subjectOf")

    val person = profile.createResource(webIdUri, PERSON)
    person.addProperty(NAME, name)

    val solidVipSite = profile.createResource(WEBSITE)
    solidVipSite.addProperty(URL, "http://" + loginInfo.providerKey + ".solid.vip/")
    solidVipSite.addProperty(ABOUT, webIdUri)

    person.addProperty(SUBJECT_OF, solidVipSite)

    picture map { image =>
      person.addProperty(IMAGE, image)
    }

    summary map { description =>
      person.addProperty(DESCRIPTION, description)      
    }

    location map { name =>
      val place = profile.createResource(PLACE)
      place.addProperty(NAME, name)
      person.addProperty(HOME_LOCATION, place)
    }

    email map { em =>
      person.addProperty(EMAIL, em)
    }

    website map { ws =>
      val webSite = profile.createResource(WEBSITE)
      webSite.addProperty(AUTHOR, person)
      webSite.addProperty(URL, ws)
      person.addProperty(URL, ws)
    }

    twitterUrl map { t =>
      person.addProperty(SAME_AS, t)      
    }

    gitHubUrl map { gh =>
      person.addProperty(SAME_AS, gh)
    }

    projects map { project =>
      val softwareSourceCode = profile.createResource(project.link, SOFTWARE_SOURCE_CODE)
      softwareSourceCode.addProperty(URL, project.link)
      softwareSourceCode.addProperty(NAME, project.title)
      softwareSourceCode.addProperty(DESCRIPTION, project.description)

      project.thumbnail.map { image =>
        softwareSourceCode.addProperty(IMAGE, image)
      }

      if (project.isContributedTo) {
        softwareSourceCode.addProperty(CONTRIBUTOR, person)

      } else {
        softwareSourceCode.addProperty(AUTHOR, person)
      }
    }

    workExperience.filter(_.endDate.isEmpty).map { worksFor =>
      val organization = profile.createResource(ORGANIZATION)
      organization.addProperty(NAME, worksFor.company)

      person.addProperty(WORKS_FOR, organization)
      person.addProperty(JOB_TITLE, worksFor.title)
    }

    profile
  }

  def toJsonLd = {
    val writer = new StringWriter()
    toModel.write(writer, RDFLanguages.JSONLD.getName)
    writer.toString
  }

  def toTtl = {
    val writer = new StringWriter()
    toModel.write(writer, RDFLanguages.TURTLE.getName)
    writer.toString
  }
}


object Profile {
  def apply(member : Member) : Profile = 
    Profile(
        loginInfo = member.loginInfo,
        name = member.name,
        picture = None,
        title = None,
        summary = None,
        location = None,
        email = member.email,
        website = None,
        twitterUrl = None,
        gitHubUrl = None,
        gitHubUsername = None,
        projects = List(),
        workExperience = List()
    )
}