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

  def toJsonLd : String = {
    val webIdUri = "http://" + loginInfo.providerID + ".solid.vip/#i"
    val NS = "http://schema.org/"

    val profile = ModelFactory.createDefaultModel();

    val NAMESPACE = profile.createResource(NS)
    val PERSON = profile.createResource(NS + "Person")
    val CONTACT_POINT = profile.createResource(NS + "ContactPoint")
    val WEBSITE = profile.createResource(NS + "WebSite")

    val NAME = profile.createProperty(NS + "name")
    val PHOTO = profile.createProperty(NS + "photo")
    val JOB_TITLE = profile.createProperty(NS + "jobTitle")
    val DESCRIPTION = profile.createProperty(NS + "description")
    val HOME_LOCATION = profile.createProperty(NS + "homeLocation")
    val AREA_SERVED = profile.createProperty(NS + "areaServed")
    val EMAIL = profile.createProperty(NS + "email")
    val AUTHOR = profile.createProperty(NS + "author")
    val URL = profile.createProperty(NS + "url")
    val SAME_AS = profile.createProperty(NS + "sameAs")

    val person = profile.createResource(webIdUri, PERSON)
    person.addProperty(NAME, name)

    picture map { photo =>
      person.addProperty(PHOTO, photo)
    }

    title map { jobTitle =>
      person.addProperty(JOB_TITLE, jobTitle)
    }

    summary map { description =>
      person.addProperty(DESCRIPTION, description)      
    }

    location map { areaServed =>
      val contactPoint = profile.createResource(CONTACT_POINT)
      contactPoint.addProperty(AREA_SERVED, areaServed)
      person.addProperty(HOME_LOCATION, contactPoint)
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

    gitHubUsername map { ghu =>
      
    }

    projects map { proj =>
      
    }

    workExperience map { we =>
      
    }

    val writer = new StringWriter()
    profile.write(writer, RDFLanguages.JSONLD.getName)
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