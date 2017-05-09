package cz.payola.model.components

import cz.payola.domain.RdfStorageComponent
import cz.payola.domain.entities.User
import cz.payola.domain.entities.AnalysisResult
import cz.payola.data.DataContextComponent
import cz.payola.domain.entities.plugins.concrete.data.PayolaStorage
import java.io._
import scala.actors.Futures._

import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model._
import org.apache.jena.riot._
import org.apache.jena.riot.lang._
import cz.payola.domain.rdf._
import cz.payola.domain.rdf.Graph

trait AnalysisResultStorageModelComponent
{
    self: DataContextComponent with RdfStorageComponent =>

    lazy val analysisResultStorageModel = new
        {
            private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
                val p = new java.io.PrintWriter(f, "UTF-8")
                try { op(p) } finally { p.close() }
            }

            def queryProperties(evaluationId: String, query: String) : scala.collection.Seq[String] = {

                val graph = rdfStorage.executeSPARQLQuery(query, constructUri(evaluationId))
                graph.edges
                    .filter(_.uri == "http://www.w3.org/2005/sparql-results#value").map(_.destination.toString)
                    .filterNot(_.startsWith("http://schema.org"))
            }
            
            def saveGraph(graph: cz.payola.common.rdf.Graph, analysisId: String, evaluationId: String, user: Option[User] = None,
                embeddedHash: Option[String] = None) {

                //store control in DB
                analysisResultRepository.storeResult(new AnalysisResult(
                    analysisId, user, evaluationId, graph.vertices.size,
                    new java.sql.Timestamp(System.currentTimeMillis)), embeddedHash)

                val uri = constructUri(evaluationId)
                rdfStorage.storeGraphGraphProtocol(uri, graph.asInstanceOf[cz.payola.domain.rdf.Graph])
            }

            /**
             * Checks if the evaluation if stored.
             */
            def exists(evaluationId: String) = analysisResultRepository.exists(evaluationId)

            def analysisId(evaluationId: String) : String = {
                analysisResultRepository.byEvaluationId(evaluationId).map{r => r.analysisId}.getOrElse("")
            }

            def byEvaluationId(evaluationId: String): Option[AnalysisResult] = {
                analysisResultRepository.byEvaluationId(evaluationId)
            }

            /**
             * Get whole graph
             */
            def getGraph(evaluationId: String): Graph = {
                getGraph("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}", evaluationId)
            }

            def getGraph(sparqlQuery: String, evaluationId: String): Graph = {
                val graphSize = rdfStorage.executeSPARQLQuery("SELECT (COUNT(*) as ?graphsize) WHERE {?s ?p ?o.}", constructUri(evaluationId))
                val graphVerticesCount = graphSize.edges.find(_.uri.contains("value")).map(_.destination.toString().toLong)

                val graph = rdfStorage.executeSPARQLQuery(sparqlQuery, constructUri(evaluationId), graphVerticesCount)
                analysisResultRepository.updateTimestamp(evaluationId)
                graph
            }

            def getGraphJena(evaluationId: String, format: String = "RDF/JSON"): String = {
                val dataset = rdfStorage.executeSPARQLQueryJena("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o.}", constructUri(evaluationId))
                analysisResultRepository.updateTimestamp(evaluationId)

                val outputStream = new java.io.ByteArrayOutputStream()
                if (format.toLowerCase == "json-ld"){
                    com.github.jsonldjava.jena.JenaJSONLD.init()
                    RDFDataMgr.write(outputStream, dataset, com.github.jsonldjava.jena.JenaJSONLD.JSONLD)
                }else{
                    dataset.getDefaultModel().write(outputStream, format)
                }

                new String(outputStream.toByteArray(),"UTF-8")

            }

            def getGraph(sparqlQueryList: List[String], evaluationId: String): Graph = {
                val graphSize = rdfStorage.executeSPARQLQuery("SELECT (COUNT(*) as ?graphsize) WHERE {?s ?p ?o.}", constructUri(evaluationId))
                val graphVerticesCount = graphSize.edges.find(_.uri.contains("value")).map(_.destination.toString().toLong)

                val graph = sparqlQueryList.map{ query =>
                    rdfStorage.executeSPARQLQuery(query, constructUri(evaluationId), graphVerticesCount)
                }.reduce(_ + _)
                analysisResultRepository.updateTimestamp(evaluationId)
                graph
            }

            def removeGraph(evaluationId: String, analysisId: String) {
                rdfStorage.deleteGraph(constructUri(evaluationId))
                analysisResultRepository.deleteResult(evaluationId, analysisId)
            }

            private def constructUri(evaluationId: String): String = {
                "http://"+evaluationId
            }

            def getEmptyGraph(): Graph = {
                JenaGraph.empty
            }

            def getAllAvailableToUser(userId: Option[String]): scala.collection.Seq[AnalysisResult] =
                analysisResultRepository.getAllAvailableToUser(userId)
        }

    val maxStoredAnalyses: Long

    val maxStoredAnalysesPerUser: Long
}
