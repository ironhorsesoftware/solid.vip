package daos

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

trait OAuth2DAO extends DelegableAuthInfoDAO[OAuth2Info]
