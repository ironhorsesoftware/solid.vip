package models

import org.apache.jena.rdf.model.{Model, ModelFactory, Statement}

class Profile {
  private val model : Model = ModelFactory.createDefaultModel()

  def addModel(mdl : Model) {
    model.add(mdl)
  }

  def addStatement(stmt : Statement) {
    model.add(stmt)
  }
}
