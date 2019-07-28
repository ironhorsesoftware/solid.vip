package services

import com.mohiva.play.silhouette.impl.providers.SocialProfileBuilder

trait SolidProfileBuilder {
  self: SocialProfileBuilder =>

  type Profile = models.Profile
}