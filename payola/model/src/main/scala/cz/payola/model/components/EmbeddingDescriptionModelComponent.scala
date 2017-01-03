package cz.payola.model.components

import cz.payola.common.entities._
import cz.payola.domain._
import cz.payola.data.DataContextComponent

trait EmbeddingDescriptionModelComponent
{
    self: DataContextComponent with RdfStorageComponent =>

    lazy val embeddingDescriptionModel = new
        {
            def getAllAvailableToUser(userId: Option[String]): scala.collection.Seq[EmbeddingDescription] =
                embeddingDescriptionRepository.getAllAvailableToUser(userId)

            def createEmbedded(analysisResult: AnalysisResult): EmbeddingDescription =
                embeddingDescriptionRepository.createEmbeddedUriHash(analysisResult)

            def getEmbedded(analysisResultId: String): Option[EmbeddingDescription] =
                embeddingDescriptionRepository.getEmbeddedUriHash(analysisResultId)

            def embeddingExistsByResultId(evalId: String): Boolean =
                embeddingDescriptionRepository.embeddingExistsByResultId(evalId)

            def getEmbeddedAnalysisResult(uriHash: String): Option[(AnalysisResult, EmbeddingDescription)] = {
                val embeddedOpt = embeddingDescriptionRepository.getEmbeddedByUriHash(uriHash)

                embeddedOpt.map{ emb =>
                    analysisResultRepository.getById(emb.analysisResultId).map(((_, emb)))
                }.getOrElse(None)
            }

            def exists(embedId: String): Boolean =
                embeddingDescriptionRepository.getEmbeddedById(embedId).isDefined

            def removeByEvaluationId(evalId: String) {
                embeddingDescriptionRepository.removeByEvaluationId(evalId)
            }

            def setViewPlugin(id: String, visualPlugin: String): Option[entities.EmbeddingDescription] = {
                embeddingDescriptionRepository.setViewPlugin(id, visualPlugin)
            }

            def updateEvaluation(uriHash: String, newEvalId: String) {
                embeddingDescriptionRepository.updateEvaluation(uriHash, newEvalId)
            }
        }
}
